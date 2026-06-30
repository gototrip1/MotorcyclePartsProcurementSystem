package com.motorparts.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 领用单（出库单）实体类 —— 领用人员发起，管理员审核出库
 */
@TableName("stock_out_order")
public class StockOutOrder extends BaseEntity {

    /**
     * 领用单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 领用单号
     */
    @NotBlank(message = "领用单号不能为空")
    @TableField("stock_out_number")
    private String stockOutNumber;

    /**
     * 领用人ID
     */
    @NotNull(message = "领用人ID不能为空")
    @TableField("user_id")
    private Long userId;

    /**
     * 领用部门
     */
    private String department;

    /**
     * 用途（生产/维修/报废/其他）
     */
    private String purpose;

    /**
     * 状态
     * 1-待审核, 2-已出库, 3-已取消
     */
    private Integer status;

    /**
     * 领用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = com.motorparts.common.CustomLocalDateTimeDeserializer.class)
    @TableField("stock_out_time")
    private LocalDateTime stockOutTime;

    /**
     * 备注
     */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockOutNumber() {
        return stockOutNumber;
    }

    public void setStockOutNumber(String stockOutNumber) {
        this.stockOutNumber = stockOutNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getStockOutTime() {
        return stockOutTime;
    }

    public void setStockOutTime(LocalDateTime stockOutTime) {
        this.stockOutTime = stockOutTime;
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
        StockOutOrder that = (StockOutOrder) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
