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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 订单明细实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("order_detail")
public class OrderDetail extends BaseEntity {

    /**
     * 明细ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    @TableField("order_id")
    private Long orderId;

    /**
     * 零部件ID
     */
    @NotNull(message = "零部件ID不能为空")
    @TableField("part_id")
    private Long partId;

    /**
     * 采购数量
     */
    @NotNull(message = "采购数量不能为空")
    @Min(value = 1, message = "采购数量至少为1")
    private Integer quantity;

    /**
     * 采购单价
     */
    @NotNull(message = "采购单价不能为空")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 小计金额（数据库计算字段）
     */
    @TableField("subtotal")
    private BigDecimal subtotal;

    /**
     * 备注
     */
    private String remark;
}