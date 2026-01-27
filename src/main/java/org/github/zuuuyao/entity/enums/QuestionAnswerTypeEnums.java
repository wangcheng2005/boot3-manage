
package org.github.zuuuyao.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 答案类型
 *
 * @author wangc
 * Date 2025-11-22 10:10:52
 * Copyright (C) house
 */
@Getter
@AllArgsConstructor
public enum QuestionAnswerTypeEnums implements BaseEnum<QuestionAnswerTypeEnums> {


    /**
     * 单选
     */
    SINGLE(1, "single", "单选", DictColorEnums.PRIMARY),

    /**
     * 多选
     */
    MULTI(2, "multi", "多选", DictColorEnums.SUCCESS),

    /**
     * 不定项
     */
    INDEFINITE(3, "indefinite", "不定项", DictColorEnums.DANGER),

    /**
     * 填空
     */
    FILL(4, "fill", "填空", DictColorEnums.DEFAULT),

    /**
     * 判断
     */
    JUDGE(5, "judge", "判断", DictColorEnums.DEFAULT),

    /**
     * 问答
     */
    TEXT(6, "text", "问答", DictColorEnums.DEFAULT),


    ;

    private final int code;
    private final String value;
    private final String name;
    private final DictColorEnums dictColorEnums;

    @Override
    public String getValue(QuestionAnswerTypeEnums enums) {
        return enums.value;
    }

    @Override
    public String getLable(QuestionAnswerTypeEnums enums) {
        return enums.name;
    }

    @Override
    public DictColorEnums getDictColorEnums(QuestionAnswerTypeEnums enums) {
        return enums.dictColorEnums;
    }
}
