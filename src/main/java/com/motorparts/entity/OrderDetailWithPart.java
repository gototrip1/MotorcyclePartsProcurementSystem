package com.motorparts.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单明细（包含零部件详情）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailWithPart extends OrderDetail {

    /**
     * 零部件详情
     */
    private Part partDetail;
}
