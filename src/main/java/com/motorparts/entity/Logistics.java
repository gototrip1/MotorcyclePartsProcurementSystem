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
import java.time.LocalDateTime;

/**
 * 物流信息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("logistics")
public class Logistics extends BaseEntity {

    /**
     * 物流ID
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
     * 物流公司
     */
    @TableField("logistics_company")
    private String logisticsCompany;

    /**
     * 运单号
     */
    @TableField("tracking_number")
    private String trackingNumber;

    /**
     * 发货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("ship_time")
    private LocalDateTime shipTime;

    /**
     * 预计到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    /**
     * 实际到达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    /**
     * 物流状态
     * 1-待发货, 2-运输中, 3-已签收, 4-异常
     */
    private Integer status;

    /**
     * 收货人
     */
    private String receiver;

    /**
     * 备注
     */
    private String remark;
}