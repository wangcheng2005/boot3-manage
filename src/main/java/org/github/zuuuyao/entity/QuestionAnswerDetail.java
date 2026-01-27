package org.github.zuuuyao.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author wangcheng
 * @Date 2024/10/8
 */
@Data
public class QuestionAnswerDetail {

    /**
     * 类型
     */
    private Integer type;

    /**
     * 正确答案
     */
    private List<String> answer;

    /**
     * 选项
     */
    private List<QuestionAnswerOptionItem> options;

    /**
     * 填空题答案
     */
    private List<FillQuestionAnswerOptionItem> fillAnswers;

}
