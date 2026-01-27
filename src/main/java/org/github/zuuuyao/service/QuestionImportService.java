package org.github.zuuuyao.service;

import org.github.zuuuyao.entity.QuestionEntity;
import org.github.zuuuyao.playwright.Question;

import java.util.List;

public interface QuestionImportService {
    /**
     * 将抓取的题目列表转换并入库，返回插入/更新的数量。
     */
    int saveFetchedQuestions(List<Question> questions);
}

