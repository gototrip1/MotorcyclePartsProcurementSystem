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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购订单实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    @TableField("order_number")
    private String orderNumber;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 订单状态
     * 1-待审核, 2-已审核, 3-采购中, 4-已入库, 5-已取消
     */
    private Integer status;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("order_time")
    private LocalDateTime orderTime;

    /**
     * 预计交货日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    /**
     * 实际交货日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("actual_delivery_date")
    private LocalDate actualDeliveryDate;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 备注
     */
    private String remark;
}