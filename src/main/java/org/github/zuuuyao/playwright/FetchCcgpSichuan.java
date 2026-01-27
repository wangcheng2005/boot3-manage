package org.github.zuuuyao.playwright;

import cn.hutool.json.JSONUtil;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class FetchCcgpSichuan {

    private static final String TARGET_URL = "https://www.ccgp-sichuan.gov.cn/maincms-web/massageListPage?typeId=gsxx-noticeType";

    /**
     * Fetch the raw HTML of the target page. Caller can parse the HTML later.
     */
    public String fetchPageHtml() {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/143.0.0.0 Safari/537.36")
            );

            Page page = context.newPage();
            page.navigate(TARGET_URL);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            // small wait to allow client-side rendering
            page.waitForTimeout(500);
            String html = page.content();
            return html;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch page HTML: " + e.getMessage(), e);
        }
    }

    /**
     * A small helper: collect up to `maxItems` anchors on the page and return a JSON array
     * of {title, href} objects. This is intentionally generic — after you inspect results
     * you can tell me which exact selectors/fields you want extracted.
     */
    public String fetchAnchorsAsJson(int maxItems) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(TARGET_URL);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(500);

            // Use a single-line JS string to avoid Java string literal newline issues
            String js = "max => { const out = []; const anchors = Array.from(document.querySelectorAll('a')); for (var i = 0; i < anchors.length && out.length < max; i++) { var a = anchors[i]; var title = (a.innerText || a.textContent || ''); title = title.trim(); var href = a.href || a.getAttribute('href') || ''; if (title) out.push({ title: title, href: href }); } return out; }";

            Object extracted = page.evaluate(js, maxItems);

            // Use Hutool JSON to stringify the result (the project already uses Hutool elsewhere).
            return JSONUtil.toJsonStr(extracted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract anchors: " + e.getMessage(), e);
        }
    }

    /**
     * Capture a screenshot of the first element matching `selector` on the target page and save it to `outputPath`.
     * Returns the absolute path of the saved file.
     */
    public String captureSelectorScreenshot(String selector, String outputPath) {
        if (selector == null || selector.trim().isEmpty()) {
            throw new IllegalArgumentException("selector must not be empty");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("outputPath must not be empty");
        }

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            page.navigate(TARGET_URL);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            // wait a bit for dynamic content
            page.waitForTimeout(500);

            // Ensure the element is present
            page.waitForSelector(selector, new Page.WaitForSelectorOptions().setTimeout(5000));
            Locator locator = page.locator(selector).first();

            java.nio.file.Path out = Paths.get(outputPath).toAbsolutePath();
            locator.screenshot(new Locator.ScreenshotOptions().setPath(out));

            return out.toString();
        } catch (PlaywrightException pe) {
            throw new RuntimeException("Playwright error while capturing selector screenshot: " + pe.getMessage(), pe);
        } catch (Exception e) {
            throw new RuntimeException("Failed to capture selector screenshot: " + e.getMessage(), e);
        }
    }

}
