package org.github.zuuuyao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 问题分类表
 *
 * @author wangc
 * Date 2025-11-22 10:10:52
 * Copyright (C) house
 */
@Data
@TableName("cl_question_category")
public class QuestionCategoryEntity extends BaseEntity {

    /**
     * 业务id
     */
    private String categoryId;

    /**
     * 分类名
     */
    private String name;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 枚举分类类型: system: 系统分类, custom: 自定义分类
     */
    private Integer type;
    /**
     * 机构ID
     */
    private Integer organizationId;
    /**
     * 枚举用户状态: disable:禁用, enable:启用
     */
    private Integer status;

    /**
     * 父级id
     */
    private Integer parentId;
}
