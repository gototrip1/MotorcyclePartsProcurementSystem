package com.motorparts.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 采购订单（包含订单明细及零部件详情）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderWithDetails extends PurchaseOrder {

    /**
     * 订单明细列表（包含零部件详情）
     */
    private List<OrderDetailWithPart> orderDetail;
}
