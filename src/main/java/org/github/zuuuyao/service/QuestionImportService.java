package org.github.zuuuyao.service;

import org.github.zuuuyao.playwright.QuestionDetail;

import java.util.List;

public interface QuestionImportService {
    /**
     * 将抓取的题目列表转换并入库，返回插入/更新的数量。
     */
    int saveFetchedQuestions(List<QuestionDetail> questions);
}

