package com.motorparts.entity;

import java.util.List;

/**
 * 领用单（包含领用明细及零部件详情）
 */
public class StockOutOrderWithDetails extends StockOutOrder {

    /**
     * 领用明细列表
     */
    private List<StockOutDetail> details;

    public List<StockOutDetail> getDetails() {
        return details;
    }

    public void setDetails(List<StockOutDetail> details) {
        this.details = details;
    }
}
