package org.github.zuuuyao.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: DataScopeEnums
 * Description:
 *
 * @author wangc
 * Date 2024/04/28 17:21
 */
@Getter
@AllArgsConstructor
public enum DictColorEnums {
    DEFAULT("default"),
    PRIMARY("primary"),
    SUCCESS("success"),
    INFO("info"),
    WARNING("warning"),
    DANGER("danger"),
    ;
    private final String value;
}

