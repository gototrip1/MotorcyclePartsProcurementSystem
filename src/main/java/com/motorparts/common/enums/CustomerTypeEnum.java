package com.motorparts.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客户类型枚举
 */
@Getter
@AllArgsConstructor
public enum CustomerTypeEnum {

    /**
     * 经销商
     */
    DEALER(1, "经销商"),

    /**
     * 零售店
     */
    RETAIL_STORE(2, "零售店"),

    /**
     * 个人用户
     */
    INDIVIDUAL(3, "个人用户");

    private final Integer code;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static CustomerTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CustomerTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查code是否存在
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}