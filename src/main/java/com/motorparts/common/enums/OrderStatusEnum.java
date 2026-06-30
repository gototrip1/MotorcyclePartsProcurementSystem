package com.motorparts.common.enums;

/**
 * 订单状态枚举
 */
public enum OrderStatusEnum {

    /**
     * 采购中（采购员建单起始态，自行处理物流/发货/等货，无需审核）
     */
    PURCHASING(1, "采购中"),

    /**
     * 待入库审核（采购员货到提交，球交给管理员）
     */
    PENDING_INBOUND(2, "待入库审核"),

    /**
     * 已入库（管理员审核通过，库存 +）
     */
    STORED(3, "已入库"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String description;

    OrderStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static OrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
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