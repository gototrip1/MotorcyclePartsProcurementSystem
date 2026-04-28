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

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 产品/零部件实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("part")
public class Part extends BaseEntity {

    /**
     * 零部件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 零件编码
     */
    @NotBlank(message = "零件编码不能为空")
    @TableField("part_code")
    private String partCode;

    /**
     * 零件名称
     */
    @NotBlank(message = "零件名称不能为空")
    private String name;

    /**
     * 型号
     */
    private String model;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单位（个/套/件/台等）
     */
    private String unit;

    /**
     * 采购单价
     */
    @NotNull(message = "采购单价不能为空")
    @DecimalMin(value = "0.00", message = "采购单价不能小于0")
    @TableField("purchase_price")
    private BigDecimal purchasePrice;

    /**
     * 建议零售价
     */
    @TableField("suggested_retail_price")
    private BigDecimal suggestedRetailPrice;

    /**
     * 库存预警值
     */
    @TableField("stock_warning_value")
    private Integer stockWarningValue;

    /**
     * 供应商ID
     */
    @TableField("supplier_id")
    private Long supplierId;

    /**
     * 分类
     * 发动机类、车架类、电气类、制动类、传动类、外观件
     */
    private String category;

    /**
     * 零件描述
     */
    private String description;
}