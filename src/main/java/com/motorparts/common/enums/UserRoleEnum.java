package com.motorparts.common.enums;

/**
 * 用户角色枚举
 */
public enum UserRoleEnum {

    /**
     * 系统管理员（审核中枢，掌控库存变动）
     */
    ADMIN("admin", "管理员"),

    /**
     * 采购专员（只管进货，不领用物料）
     */
    PURCHASE("purchase", "采购专员"),

    /**
     * 领用人员（车间/生产领料，只做领料，不采购）
     */
    REQUISITION("requisition", "领用人员");

    private final String code;
    private final String description;

    UserRoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static UserRoleEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 检查code是否存在
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
}