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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 库存实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
public class Inventory extends BaseEntity {

    /**
     * 库存ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 零部件ID
     */
    @NotNull(message = "零部件ID不能为空")
    @TableField("part_id")
    private Long partId;

    /**
     * 当前库存数量
     */
    @NotNull(message = "当前库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于0")
    @TableField("current_quantity")
    private Integer currentQuantity;

    /**
     * 安全库存量
     */
    @TableField("safety_stock")
    private Integer safetyStock;

    /**
     * 最近入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_inbound_time")
    private LocalDateTime lastInboundTime;

    /**
     * 最近出库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_outbound_time")
    private LocalDateTime lastOutboundTime;

    /**
     * 仓库位置
     */
    @TableField("warehouse_location")
    private String warehouseLocation;
}