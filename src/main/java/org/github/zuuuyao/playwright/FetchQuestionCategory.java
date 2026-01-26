package org.github.zuuuyao.playwright;

import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.github.zuuuyao.entity.QuestionCategoryEntity;
import org.github.zuuuyao.repository.QuestionCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class FetchQuestionCategory {

    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;

    /**
     * 通过 Playwright 抓取分类树并返回解析后的 List<QuestionCategory>（根节点列表）
     */
    public List<QuestionCategory> fetchCategories() {
        Object result = null;
        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate("https://examon.mvwchina.com/passport/login");

            // 填账号密码（可按需修改或改为从配置注入）
            page.fill("input[formcontrolname='userName']", "17773102899");
            page.fill("input[formcontrolname='password']", "000000");
            page.click("button[type=submit]");

            page.waitForURL("**/dashboard/**");
            page.waitForLoadState(LoadState.NETWORKIDLE);

            try {
                result = page.evaluate("""
            () => fetch('/api/dictionary/type/question-category-all?username=17773102899', { credentials: 'include' })
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

            browser.close();
        } catch (Exception e) {
            throw new RuntimeException("Playwright fetch failed", e);
        }

        // 解析 result -> ApiResult -> body -> QuestionCategory list
        ApiResult apiResult = null;
        Object rawBody = null;
        if (result instanceof Map) {
            Map<?, ?> resultMap = (Map<?, ?>) result;
            apiResult = new ApiResult(resultMap);
            rawBody = resultMap.get("body");
        }

        if ((rawBody == null || rawBody.toString().isEmpty()) && apiResult != null && apiResult.rawBodyString != null) {
            rawBody = apiResult.rawBodyString;
        }

        if (rawBody == null) return null;

        String json = JSONUtil.toJsonStr(rawBody);
        if (json.trim().startsWith("[")) {
            return JSONUtil.toList(json, QuestionCategory.class);
        } else {
            QuestionCategory single = JSONUtil.toBean(json, QuestionCategory.class);
            return single == null ? null : List.of(single);
        }
    }

    /**
     * 抓取并保存到数据库（递归插入）
     */
    @Transactional(rollbackFor = Exception.class)
    public void fetchAndSave() {
        List<QuestionCategory> roots = fetchCategories();
        if (roots == null || roots.isEmpty()) {
            System.out.println("no categories fetched");
            return;
        }
        for (QuestionCategory qc : roots) {
            if(!qc.key.equals("0")){
                continue;
            }
            saveRecursively(qc, 1);
        }
    }

    /**
     * 递归保存一个节点及其子节点
     * @param qc 源节点
     * @param parentId 父节点数据库 id (nullable)
     * @return 插入后实体的数据库 id
     */
    private Integer saveRecursively(QuestionCategory qc, Integer parentId) {
        if (qc == null) return null;

        QuestionCategoryEntity entity = new QuestionCategoryEntity();
        entity.setName(qc.title);
        entity.setParentId(parentId);
        // sequence 字段映射到 sort
        entity.setSort(qc.sequence);
        entity.setStatus(1);
        entity.setType(1);
        // 如果没有明确组织 id，使用 0 或 null（这里使用 0 表示根组织）
        entity.setOrganizationId(null);
        // 如果有业务 id（keyy）可以保存
        entity.setCategoryId(qc.key);
        entity.setCreator(1);
        entity.setUpdater(1);

        // 使用 MyBatis 的 insert 操作；insert 后 entity.id 应该被回填（基于项目配置）
        questionCategoryRepository.insert(entity);

        Integer newId = entity.getId();

        if (qc.children != null && !qc.children.isEmpty()) {
            for (QuestionCategory child : qc.children) {
                saveRecursively(child, newId);
            }
        }

        return newId;
    }
}
