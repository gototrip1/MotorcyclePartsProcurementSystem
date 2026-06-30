package com.motorparts.common.enums;

/**
 * 领用单（出库单）状态枚举
 */
public enum StockOutStatusEnum {

    /**
     * 待审核
     */
    PENDING_REVIEW(1, "待审核"),

    /**
     * 已出库
     */
    STOCKED_OUT(2, "已出库"),

    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

    private final Integer code;
    private final String description;

    StockOutStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static StockOutStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StockOutStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}
