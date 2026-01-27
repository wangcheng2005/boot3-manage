package org.github.zuuuyao.entity.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface BaseEnum<T extends Enum<T>> {

    String getValue(T t);

    String getLable(T t);

    DictColorEnums getDictColorEnums(T t);

    /**
     * 获取指定子类枚举的字典数据
     *
     * @param enumClass the specific subclass of Enum
     * @return List of DictDataRes
     */
    static <T extends Enum<T> & BaseEnum<T>> List<DictDataRes> getDictData(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(e -> {
                    DictDataRes dictDataRes = new DictDataRes();
                    dictDataRes.setDictType(e.getClass().getSimpleName());
                    dictDataRes.setValue(String.valueOf(e.getCode()));
                    dictDataRes.setLabel(e.getName());
                    dictDataRes.setColorType(e.getDictColorEnums(e).getValue());
                    return dictDataRes;
                }).collect(Collectors.toList());
    }

    /**
     * 根据value获取枚举
     */
    static <T extends Enum<T> & BaseEnum<T>> T getEnum(Class<T> clazz, String value) {
        return Arrays.stream(clazz.getEnumConstants())
                .filter(e -> e.getValue(e).equals(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据value获取枚举
     */
    static <T extends Enum<T> & BaseEnum<T>> T getEnum(Class<T> clazz, Integer code) {
        return Arrays.stream(clazz.getEnumConstants())
                .filter(e -> e.getCode() == code)
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据name获取枚举
     */
    static <T extends Enum<T> & BaseEnum<T>> T getEnumByName(Class<T> clazz, String name) {
        return Arrays.stream(clazz.getEnumConstants())
                .filter(e -> Objects.equals(e.getName(), name))
                .findFirst()
                .orElse(null);
    }

    String getValue();

    String getName();

    int getCode();

    default String getEnumName() {
        return ((Enum<?>) this).name();
    }
}
