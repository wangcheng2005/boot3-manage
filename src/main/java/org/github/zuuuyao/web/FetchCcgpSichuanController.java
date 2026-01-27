package org.github.zuuuyao.web;

import org.github.zuuuyao.playwright.FetchCcgpSichuan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ccgp-sichuan")
public class FetchCcgpSichuanController {

    private final FetchCcgpSichuan fetchService;

    @Autowired
    public FetchCcgpSichuanController(FetchCcgpSichuan fetchService) {
        this.fetchService = fetchService;
    }

    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getHtml() {
        String html = fetchService.fetchPageHtml();
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @GetMapping(value = "/anchors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAnchors(@RequestParam(name = "max", defaultValue = "50") int max) {
        String json = fetchService.fetchAnchorsAsJson(max);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
    }

    @GetMapping(value = "/capture", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> captureSelector(@RequestParam(name = "selector") String selector,
                                                  @RequestParam(name = "output", required = false) String output) {
        if (output == null || output.trim().isEmpty()) {
            // default path in current working directory
            output = "capture.png";
        }
        String path = fetchService.captureSelectorScreenshot(selector, output);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(path);
    }
}
