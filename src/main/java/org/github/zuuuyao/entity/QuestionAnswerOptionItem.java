package org.github.zuuuyao.entity;

import lombok.Data;

/**
 * @Author wangcheng
 * @Date 2024/10/8
 */
@Data
public class QuestionAnswerOptionItem {

    /**
     * 选项值
     */
    private String value;

    /**
     * 答案内容
     */
    private String content;

    /**
     * 是否正确答案
     */
    private Integer correct;

}
