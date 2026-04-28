package com.motorparts.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 供应商实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("supplier")
public class Supplier extends BaseEntity {

    /**
     * 供应商ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商编码
     */
    @NotBlank(message = "供应商编码不能为空")
    @TableField("supplier_code")
    private String supplierCode;

    /**
     * 供应商名称
     */
    @NotBlank(message = "供应商名称不能为空")
    private String name;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 信用评级
     * A-优秀, B-良好, C-一般, D-较差
     */
    @TableField("credit_rating")
    private String creditRating;

    /**
     * 合作状态
     * 1-合作中, 2-已终止, 3-审核中
     */
    private Integer status;
}