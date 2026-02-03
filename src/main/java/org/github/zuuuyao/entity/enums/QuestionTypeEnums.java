
package org.github.zuuuyao.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.github.zuuuyao.entity.QuestionEntity;
import org.github.zuuuyao.playwright.QuestionDetail;

/**
 * 题型类型
 *
 * @author wangc
 * Date 2025-11-22 10:10:52
 * Copyright (C) house
 */
@Getter
@AllArgsConstructor
public enum QuestionTypeEnums {


    /**
     * A1型
     */
    A1(1, "a1", "A1型题", DictColorEnums.PRIMARY, QuestionAnswerTypeEnums.SINGLE),

    /**
     * A2型
     */
    A2(2, "a2", "A2型题", DictColorEnums.SUCCESS, QuestionAnswerTypeEnums.SINGLE),

    /**
     * A3/A4型
     */
    A3A4(3, "a3/a4", "A3/A4型题", DictColorEnums.DANGER, QuestionAnswerTypeEnums.SINGLE),

    /**
     * B型
     */
    B(4, "b", "B型题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.SINGLE),

    /**
     * C型
     */
    C(5, "c", "C型题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.SINGLE),

    /**
     * X型
     */
    X(6, "x", "X型题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.INDEFINITE),

    /**
     * 填空
     */
    FILL(7, "fill", "填空题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.FILL),

    /**
     * 判断
     */
    JUDGE(8, "judge", "判断题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.JUDGE),

    /**
     * 问答
     */

    QA(9, "q&a", "问答题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.TEXT),

    /**
     * 名称解释
     */
    EXPLAIN(10, "explain", "名称解释", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.TEXT),

    /**
     * 简答
     */
    BRIEF(11, "brief", "简答题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.TEXT),

    /**
     * 案例分析
     */
    CASE(12, "case", "案例分析题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.INDEFINITE),

    /**
     * 不定项选择
     */
    INDEFINITE(13, "indefinite", "不定项选择题", DictColorEnums.DEFAULT, QuestionAnswerTypeEnums.INDEFINITE),

    ;

    private final int code;
    private final String value;
    private final String name;
    private final DictColorEnums dictColorEnums;
    private final QuestionAnswerTypeEnums questionAnswerTypeEnums;

    public static  QuestionTypeEnums parseByCode(int code) {
        for (QuestionTypeEnums typeEnum : QuestionTypeEnums.values()) {
            if (typeEnum.getCode() == code) {
                return typeEnum;
            }
        }
        return null;
    }
}
