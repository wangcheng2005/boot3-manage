package org.github.zuuuyao.playwright;

import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.annotation.Resource;
import org.github.zuuuyao.service.QuestionImportService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FetchQuestion {

    @Resource
    private QuestionImportService questionImportService;


    /**
     * Fetch questions via Playwright. Returns total number of questions fetched (qsNum or total from API), or -1 on error.
     */
    public int fetchQuestions() {
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

//            page.waitForURL("**/question-bank/**");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            // 尝试从 localStorage/cookies 中读取 token（如果后端需要额外的 token header）
            String token = null;
            try {
                Object t = page.evaluate("() => localStorage.getItem('token')");
                if (t != null) token = t.toString();
            } catch (Exception ignored) {}

            if (token == null) {
                try {
                    List<Cookie> cookies = context.cookies();
                    for (Cookie c : cookies) {
                        if ("token".equals(c.name)) {
                            token = c.value;
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 调接口获取第一页，解析 total 确定页数
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
                return -1;
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

            // 循环抓取每一页
            int totalFetched = 0;
            for (int pi = 1; pi <= 1; ++pi) {
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
                    List<QuestionDetail> list = new ArrayList<>();
                    if (pr != null && pr.body != null && pr.body.list != null) {
                        System.out.println("page " + pi + " list size=" + pr.body.list.size());
                        totalFetched += pr.body.list.size();
                        int idx = 0;
                        for (Question q : pr.body.list) {
                            System.out.println("  item " + (++idx) + ": " + q);

                            // 额外为每道题请求详情接口并打印解析结果
                            try {
                                Object detail = page.evaluate("args => { const id = args[0]; const token = args[1]; const headers = { 'X-Requested-With': 'XMLHttpRequest', 'Referer': 'https://examon.mvwchina.com/question-bank/self-question-list' }; if (token) headers['token'] = token; return fetch('/api/question/' + id, { credentials: 'include', headers }).then(async r => { const status = r.status; const ok = r.ok; try { const json = await r.clone().json(); return { status, ok, body: json }; } catch (e) { const text = await r.clone().text(); return { status, ok, body: text }; } }); }", new Object[] { q.id, token });
                                ApiResult detailResult = null;
                                if (detail instanceof Map) detailResult = new ApiResult((Map<?, ?>) detail);

                                // extract body and build QuestionDetail
                                Object rawBody = null;
                                if (detail instanceof Map) rawBody = ((Map<?, ?>) detail).get("body");
                                if ((rawBody == null || rawBody.toString().isEmpty()) && detailResult != null && detailResult.rawBodyString != null) rawBody = detailResult.rawBodyString;

                                if (rawBody != null) {
                                    String json = JSONUtil.toJsonStr(rawBody);
                                    QuestionDetail qd = JSONUtil.toBean(json, QuestionDetail.class);
                                    list.add(qd);
                                    System.out.println("    detail parsed for " + q.id + " => " + qd);
                                } else {
                                    System.out.println("    no body found for detail of " + q.id);
                                }

                            } catch (PlaywrightException e) {
                                System.out.println("    detail fetch failed for " + q.id + ": " + e.getMessage());
                            }

                        }
                    }
                    questionImportService.saveFetchedQuestions(list);

                } catch (PlaywrightException e) {
                    System.out.println("evaluate failed for pi=" + pi + ": " + e.getMessage());
                }

                page.waitForTimeout(200);
            }

            browser.close();
            return totalFetched;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
