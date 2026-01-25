package org.github.zuuuyao.playwright;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

import java.util.Map;

public class FetchQuestion {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setHeadless(false)
            );

            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setUserAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                            "Chrome/143.0.0.0 Safari/537.36"
                            )
            );

            Page page = context.newPage();

            page.navigate("https://examon.mvwchina.com/passport/login");

// 填账号密码
            page.fill("input[formcontrolname='userName']", "17773102899");
            page.fill("input[formcontrolname='password']", "000000");
            page.click("button[type=submit]");

            page.waitForURL("**/question-bank/**");
            page.waitForLoadState(LoadState.NETWORKIDLE);

// 再调接口, 先返回 status/ok/body（body 可能是 parsed JSON，也可能是字符串）
            Object result = null;
            try {
                result = page.evaluate("""
            () => fetch('/api/question?pi=1&ps=20&auditStatus=verified&nature=2', { credentials: 'include' })
              .then(async r => {
                const status = r.status;
                const ok = r.ok;
                try {
                  const json = await r.clone().json();
                  return { status, ok, body: json };
                } catch (e) {
                  const text = await r.clone().text();
                  return { status, ok, body: text };
                }
              })
          """);
            } catch (PlaywrightException e) {
                System.out.println("evaluate failed: " + e.getMessage());
            }

            System.out.println("raw result class=" + (result == null ? "null" : result.getClass()) + ", value=" + result);

            ApiResult apiResult = null;
            if (result instanceof Map) apiResult = new ApiResult((Map<?, ?>) result);
            System.out.println("parsed apiResult: " + apiResult);

            // 解析 result 中的 total 属性, 再按每页20条计算总页数, 然后循环抓取所有页的数据
            int pageSize = 20;
            int totalPages = 1;
            try {
                int total = 0;
                if (apiResult != null && apiResult.body != null) total = apiResult.body.total;
                totalPages = Math.max(1, (total + pageSize - 1) / pageSize);
                System.out.println("total=" + total + ", totalPages=" + totalPages + ", qsNum=" + (apiResult != null && apiResult.body != null ? apiResult.body.qsNum : 0));
            } catch (Exception e) {
                System.out.println("Error parsing result: " + e);
            }

            // 循环抓取每一页，返回同样的包装结构，便于排错
            for (int pi = 1; pi <= totalPages; ++pi) {
                try {
                    Object pageResult = page.evaluate(String.format("""
              () => fetch('/api/question?pi=%d&ps=%d&auditStatus=verified&nature=2', { credentials: 'include' })
                .then(async r => {
                  const status = r.status;
                  const ok = r.ok;
                  try { const json = await r.clone().json(); return { status, ok, body: json }; }
                  catch (e) { const text = await r.clone().text(); return { status, ok, body: text }; }
                })
            """, pi, pageSize));

                    ApiResult pr = null;
                    if (pageResult instanceof Map) pr = new ApiResult((Map<?, ?>) pageResult);
                    System.out.println("page " + pi + " parsed=" + pr);

                    // 将 list 元素里面的 map 转换为 Question 对象并处理
                    if (pr != null && pr.body != null && pr.body.list != null) {
                        System.out.println("page " + pi + " list size=" + pr.body.list.size());
                        int idx = 0;
                        for (Question q : pr.body.list) {
                            System.out.println("  item " + (++idx) + ": " + q);
                        }
                    }

                } catch (PlaywrightException e) {
                    System.out.println("evaluate failed for pi=" + pi + ": " + e.getMessage());
                }

                page.waitForTimeout(200);
            }

            browser.close();
        }
    }

    // 从返回的对象中提取 total（兼容多种结构和上面返回的 {status,ok,body} 包装）
    static int extractTotal(Object result) {
        if (result == null) return 0;
        // 如果是包装结构 {status, ok, body: ...}
        if (result instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) result;
            Object body = m.get("body");
            Object candidate = body != null ? body : result;
            // body 可能是 Map，也可能是字符串
            if (candidate instanceof Map) {
                Map<?, ?> mm = (Map<?, ?>) candidate;
                Object totalObj = mm.get("total");
                if (totalObj instanceof Number) return ((Number) totalObj).intValue();
                if (totalObj != null) {
                    try { return Integer.parseInt(totalObj.toString()); } catch (NumberFormatException ignored) {}
                }
                // 有些接口把结果放在 data 字段
                Object data = mm.get("data");
                if (data instanceof Map) {
                    Map<?, ?> dm = (Map<?, ?>) data;
                    Object t2 = dm.get("total");
                    if (t2 instanceof Number) return ((Number) t2).intValue();
                    if (t2 != null) {
                        try { return Integer.parseInt(t2.toString()); } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return 0;
    }

    static void closeAllModals(Page page) {

        long endTime = System.currentTimeMillis() + 8000; // 最多处理 8 秒

        while (System.currentTimeMillis() < endTime) {

            Locator buttons = page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("我已知悉")
            );

            if (buttons.count() > 0) {

                // 永远点第一个可见的
                Locator btn = buttons.first();

                if (btn.isVisible()) {
                    btn.click(new Locator.ClickOptions().setForce(true));

                    // 给 Angular 极短时间重排 DOM
                    page.waitForTimeout(200);
                    continue;
                }
            }

            // 如果当前页面已经找不到按钮，说明彻底没了
            break;
        }
    }




}
