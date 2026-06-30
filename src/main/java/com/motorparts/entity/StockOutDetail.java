package com.motorparts.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 领用明细实体类
 */
@TableName("stock_out_detail")
public class StockOutDetail extends BaseEntity {

    /**
     * 明细ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 领用单ID
     */
    @NotNull(message = "领用单ID不能为空")
    @TableField("stock_out_id")
    private Long stockOutId;

    /**
     * 零部件ID
     */
    @NotNull(message = "零部件ID不能为空")
    @TableField("part_id")
    private Long partId;

    /**
     * 领用数量
     */
    @NotNull(message = "领用数量不能为空")
    @Min(value = 1, message = "领用数量至少为1")
    private Integer quantity;

    /**
     * 成本单价（出库估值用，可空）
     */
    @TableField("unit_cost")
    private BigDecimal unitCost;

    /**
     * 备注
     */
    private String remark;

    public StockOutDetail() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long stockOutId;
        private Long partId;
        private Integer quantity;
        private BigDecimal unitCost;
        private String remark;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder stockOutId(Long stockOutId) {
            this.stockOutId = stockOutId;
            return this;
        }

        public Builder partId(Long partId) {
            this.partId = partId;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitCost(BigDecimal unitCost) {
            this.unitCost = unitCost;
            return this;
        }

        public Builder remark(String remark) {
            this.remark = remark;
            return this;
        }

        public StockOutDetail build() {
            StockOutDetail detail = new StockOutDetail();
            detail.setId(this.id);
            detail.setStockOutId(this.stockOutId);
            detail.setPartId(this.partId);
            detail.setQuantity(this.quantity);
            detail.setUnitCost(this.unitCost);
            detail.setRemark(this.remark);
            return detail;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStockOutId() {
        return stockOutId;
    }

    public void setStockOutId(Long stockOutId) {
        this.stockOutId = stockOutId;
    }

    public Long getPartId() {
        return partId;
    }

    public void setPartId(Long partId) {
        this.partId = partId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockOutDetail that = (StockOutDetail) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
