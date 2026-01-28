package org.github.zuuuyao.playwright;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.github.zuuuyao.entity.QuestionCategoryEntity;
import org.github.zuuuyao.repository.QuestionCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class FetchQuestionCategory {

    @Autowired
    private QuestionCategoryRepository questionCategoryRepository;

    // credentials - consider moving to config later
    private static final String USERNAME = "17773102899";
    private static final String PASSWORD = "000000";


    private void login(Page page) {
        page.navigate("https://examon.mvwchina.com/passport/login");
        page.fill("input[formcontrolname='userName']", USERNAME);
        page.fill("input[formcontrolname='password']", PASSWORD);
        page.click("button[type=submit]");
        page.waitForURL("**/dashboard/**");
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    private List<QuestionCategory> fetchCategories(Page page) {
        Object result = null;
        try {
            result = page.evaluate("""
            () => fetch('/api/dictionary/type/system-question-category?username=17773102899', { credentials: 'include' })
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

        return parseQuestionCategoryList(result);
    }

    private List<QuestionCategory> fetchLeafCategories(Page page, String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) return null;
        Object result = null;
        try {
            result = page.evaluate("""
            (categoryId) => {
              const token = localStorage.getItem('token') || sessionStorage.getItem('token');
              const headers = {
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'application/json, text/plain, */*'
              };
              if (token) headers['token'] = token;

              return fetch(`/api/dictionary/${categoryId}?treeType=0`, { credentials: 'include', headers })
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
                });
            }
          """, categoryId);
        } catch (PlaywrightException e) {
            System.out.println("leaf evaluate failed: " + e.getMessage());
        }

        return parseQuestionCategoryList(result);
    }

    private List<QuestionCategory> parseQuestionCategoryList(Object result) {
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
//    @Transactional(rollbackFor = Exception.class)
    public void fetchAndSave() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            login(page);

            List<QuestionCategory> roots = fetchCategories(page);
            if (roots == null || roots.isEmpty()) {
                System.out.println("no categories fetched");
                browser.close();
                return;
            }
            List<String> list1 = Arrays.asList(
                    "050db755955f45d18ea25d70740dea71",
                    "22110919441701590309296968634583",
                    "23051210454701656853146431926274",
                    "23061413582701668860433077997569",
                    "23091214064701701477436254846994",
                    "23091214064701701477436254847005",
                    "23091214064701701477436254847011",
                    "23091214064701701477436254847013",
                    "23091214064701701477436254847015",
                    "24022809584801762658579872083970",
                    "24050918214401788514683786145793",
                    "280bc0d523ab4722a4848240505784ec",
                    "671b349448ae4c3b8f351359e49d8dd5"
            );
            for (QuestionCategory qc : roots) {
                if(!list1.contains(qc.key)){
                    continue;
                }
                saveRecursively(qc, 1, page);
            }

            browser.close();
        } catch (Exception e) {
            throw new RuntimeException("Playwright fetch failed", e);
        }
    }

    /**
     * 递归保存一个节点及其子节点
     * @param qc 源节点
     * @param parentId 父节点数据库 id (nullable)
     * @return 插入后实体的数据库 id
     */
    private Integer saveRecursively(QuestionCategory qc, Integer parentId, Page page) {
        if (qc == null) return null;

//        LambdaQueryWrapper<QuestionCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(QuestionCategoryEntity::getCategoryId, qc.key);
//        QuestionCategoryEntity existing = questionCategoryRepository.selectOne(queryWrapper);
//        if (existing != null) {
//            return existing.getId();
//        }

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
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        // 使用 MyBatis 的 insert 操作；insert 后 entity.id 应该被回填（基于项目配置）
        questionCategoryRepository.insert(entity);

        Integer newId = entity.getId();

        List<QuestionCategory> children = qc.children;
        if (!qc.isLeaf && (children == null || children.isEmpty())) {
            // 叶子节点为空时，按接口补抓子分类
            children = fetchLeafCategories(page, qc.key);
            qc.children = children;
        }

        if (children != null && !children.isEmpty()) {
            for (QuestionCategory child : children) {
                saveRecursively(child, newId, page);
            }
        }

        return newId;
    }
}
