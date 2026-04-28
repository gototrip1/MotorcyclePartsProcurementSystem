package com.motorparts.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 客户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {

    /**
     * 客户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户编码
     */
    @NotBlank(message = "客户编码不能为空")
    @TableField("customer_code")
    private String customerCode;

    /**
     * 客户名称
     */
    @NotBlank(message = "客户名称不能为空")
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
     * 客户类型
     * 1-经销商, 2-零售店, 3-个人用户
     */
    @TableField("customer_type")
    private Integer customerType;

    /**
     * 折扣等级
     * 1-无折扣, 2-银牌, 3-金牌, 4-钻石
     */
    @TableField("discount_level")
    private Integer discountLevel;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("registered_time")
    private LocalDateTime registeredTime;
}