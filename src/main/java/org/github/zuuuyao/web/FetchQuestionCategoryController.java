package org.github.zuuuyao.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.github.zuuuyao.playwright.FetchQuestion;
import org.github.zuuuyao.playwright.FetchQuestionCategory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "爬取分类")
@AllArgsConstructor
@RestController
@RequestMapping("/admin/fetch")
public class FetchQuestionCategoryController {

    private final FetchQuestionCategory fetchQuestionCategory;
    private final FetchQuestion fetchQuestion;

    @Operation(summary = "抓取并保存题库分类")
    @PostMapping(value = "/question-categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean fetchAndSave() {
        fetchQuestionCategory.fetchAndSave();
        return true;
    }

    @Operation(summary = "抓取题目(仅打印, 返回抓取到的数量)")
    @PostMapping(value = "/questions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer fetchQuestions() {
        return fetchQuestion.fetchQuestions();
    }
}
