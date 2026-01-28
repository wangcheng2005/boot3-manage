package org.github.zuuuyao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试题表
 *
 * @author wangc
 * Date 2025-11-22 10:10:52
 * Copyright (C) house
 */
@Data
@TableName(value = "cl_question", autoResultMap = true)
public class QuestionEntity extends BaseEntity {

    /**
     * 机构ID
     */
    private Integer organizationId;
    /**
     * 分类IDs，逗号分隔
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> questionCategoryIds;
    /**
     * 标签IDs，逗号分隔
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> questionLabelsIds;
    /**
     * 题目系统类型
     */
    private Integer systemType;
    /**
     * 枚举题型类型: A1: A1型, A2: A2型, A3/A4: A3/A4型, B: B型, C: C型, X: X型, fill: 填空, judge: 判断, Q&A: 问答, explain: 名称解释, brief: 简答, case: 案例分析, indefinite: 不定项选择, clinical: 临床思维, mult_answer: 病历书写, clinical_thinking: 病例分析, combine-question: 组合型
     */
    private Integer type;
    /**
     * 枚举答案类型: single: 单选, multi: 多选, indefinite: 不定项, fill: 填空, judge: 判断, text: 问答
     */
    private Integer answerType;
    /**
     * 是否真题: 0: 否, 1: 是
     */
    private Integer isReal;
    /**
     * 是否精品题: 0: 否, 1: 是
     */
    private Integer isEssence;
    /**
     * 是否考试题: 0: 否, 1: 是
     */
    /**
     * 是否练习题: 0: 否, 1: 是
     */
    private Integer isPractice;
    /**
     * 是否英文题: 0: 否, 1: 是
     */
    private Integer isEnglish;
    /**
     * 枚举科目类型: chinese_medicine: 中医, western_medicine: 西医, integrated_medicine: 中西医结合
     */
    private Integer medicineType;
    /**
     * 题目权限
     */
    private Integer questionPermission;
    /**
     * 枚举状态: disable:禁用, enable:启用
     */
    private Integer status;
    /**
     * 枚举审核状态: wait_review:待审核, pass:审核通过, reject:审核不通过
     */
    private Integer reviewStatus;
    /**
     * 审核人
     */
    private Integer reviewer;
    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 是否引用: 0: 否, 1: 是
     */
    private Integer isReference;
    /**
     * 枚举难度等级: easy: 简单, normal: 中等, hard: 较难
     */
  private Integer difficulty;
    /**
     * 难度系数, 保存为整数，万分比，例如：5000表示0.5
     */
    private Integer difficultyCoefficient;
    /**
     * 是否有图片: 0: 否, 1: 是
     */
    private Integer hasImage;
    /**
     * 题目内容
     */
    private String content;
    /**
     * 父级题目ID, 子题目才有父级题目ID
     */
    private Integer parentId;
    /**
     * 排序, 子题目才生效
     */
    private Integer sort;
    /**
     * 答案，JSON格式存储
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QuestionAnswerDetail answer;
    /**
     * 解析
     */
    private String explanation;
    /**
     * 备注
     */
    private String remark;
    /**
     * 子题数
     */
    private Integer subCount;

}
