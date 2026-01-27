package org.github.zuuuyao.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author wangcheng
 * @Date 2024/10/8
 */
@Data
public class FillQuestionAnswerOptionItem {

    /**
     * index, 从1开始
     */
    private Integer index;

    /**
     * 答案内容
     */
    private List<String> value;

}
