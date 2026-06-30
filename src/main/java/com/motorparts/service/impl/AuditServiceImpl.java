package com.motorparts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.motorparts.common.enums.ResultCode;
import com.motorparts.common.exception.BusinessException;
import com.motorparts.entity.Inventory;
import com.motorparts.entity.OrderDetail;
import com.motorparts.entity.PurchaseOrder;
import com.motorparts.entity.StockOutDetail;
import com.motorparts.entity.StockOutOrder;
import com.motorparts.mapper.InventoryMapper;
import com.motorparts.mapper.OrderDetailMapper;
import com.motorparts.mapper.StockOutDetailMapper;
import com.motorparts.service.AuditService;
import com.motorparts.service.PurchaseOrderService;
import com.motorparts.service.StockOutOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审核中心服务实现 —— 库存闸门，唯一允许写 {@code inventory.current_quantity} 的地方，全部事务化。
 */
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final PurchaseOrderService purchaseOrderService;
    private final OrderDetailMapper orderDetailMapper;
    private final StockOutOrderService stockOutOrderService;
    private final StockOutDetailMapper stockOutDetailMapper;
    private final InventoryMapper inventoryMapper;

    /**
     * 采购单状态：1-采购中、2-待入库审核、3-已入库、4-已取消（对齐《详细设计文档》6.1）。
     */
    private static final int PO_PURCHASING = 1;
    private static final int PO_PENDING_INBOUND = 2;
    private static final int PO_STORED = 3;
    private static final int PO_CANCELLED = 4;

    /**
     * 领用单状态：1-待审核、2-已出库、3-已取消。
     */
    private static final int SO_PENDING = 1;
    private static final int SO_STOCKED_OUT = 2;
    private static final int SO_CANCELLED = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveInbound(Long orderId) {
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "采购单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != PO_PENDING_INBOUND) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "订单非「待入库审核」状态，不允许入库");
        }

        List<OrderDetail> details = orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
        if (details == null || details.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "采购单无明细，无法入库");
        }

        LocalDateTime now = LocalDateTime.now();
        for (OrderDetail d : details) {
            int rows = inventoryMapper.increaseQuantity(d.getPartId(), d.getQuantity(), now);
            if (rows == 0) {
                // 该零件尚无库存记录，自动建账后再入库
                createInventoryIfAbsent(d.getPartId(), now);
                inventoryMapper.increaseQuantity(d.getPartId(), d.getQuantity(), now);
            }
        }

        // 仅更新状态与实际到货日期（不能用 updateById：其重写会重建明细并清零总价）
        purchaseOrderService.update(new LambdaUpdateWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getId, orderId)
                .set(PurchaseOrder::getStatus, PO_STORED)
                .set(PurchaseOrder::getActualDeliveryDate, LocalDate.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectInbound(Long orderId, String reason) {
        PurchaseOrder order = purchaseOrderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "采购单不存在");
        }
        if (order.getStatus() != null && order.getStatus() == PO_STORED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "已入库订单不可驳回");
        }
        // 驳回：库存不变，记录原因，状态回退至「采购中」供采购专员重新处理
        // 仅更新状态与备注（不能用 updateById：其重写会重建明细并清零总价）
        purchaseOrderService.update(new LambdaUpdateWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getId, orderId)
                .set(PurchaseOrder::getStatus, PO_PURCHASING)
                .set(PurchaseOrder::getRemark, appendReason(order.getRemark(), reason)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveOutbound(Long stockOutId) {
        StockOutOrder so = stockOutOrderService.getById(stockOutId);
        if (so == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "领用单不存在");
        }
        if (so.getStatus() == null || so.getStatus() != SO_PENDING) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "领用单状态不允许出库");
        }

        List<StockOutDetail> details = stockOutDetailMapper.selectByStockOutId(stockOutId);
        if (details == null || details.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "领用单无明细，无法出库");
        }

        // 1. 先逐条验证库存充足
        for (StockOutDetail d : details) {
            Inventory inv = inventoryMapper.selectByPartId(d.getPartId());
            if (inv == null || inv.getCurrentQuantity() == null || inv.getCurrentQuantity() < d.getQuantity()) {
                throw new BusinessException(ResultCode.INSUFFICIENT_INVENTORY, "库存不足，零件ID=" + d.getPartId());
            }
        }

        // 2. 再逐条扣减；DB 层 current_quantity >= qty 兜底防并发超扣
        LocalDateTime now = LocalDateTime.now();
        for (StockOutDetail d : details) {
            int rows = inventoryMapper.decreaseQuantity(d.getPartId(), d.getQuantity(), now);
            if (rows == 0) {
                throw new BusinessException(ResultCode.INSUFFICIENT_INVENTORY, "库存不足，零件ID=" + d.getPartId());
            }
        }

        so.setStatus(SO_STOCKED_OUT);
        stockOutOrderService.updateById(so);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOutbound(Long stockOutId, String reason) {
        StockOutOrder so = stockOutOrderService.getById(stockOutId);
        if (so == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "领用单不存在");
        }
        if (so.getStatus() != null && so.getStatus() == SO_STOCKED_OUT) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "已出库领用单不可驳回");
        }
        // 驳回：库存不变，记录原因，状态置为「已取消」
        so.setStatus(SO_CANCELLED);
        so.setRemark(appendReason(so.getRemark(), reason));
        stockOutOrderService.updateById(so);
    }

    /**
     * 为尚无库存记录的零件建账（数量 0），随后由调用方做增减。
     */
    private void createInventoryIfAbsent(Long partId, LocalDateTime now) {
        Inventory inv = inventoryMapper.selectByPartId(partId);
        if (inv != null) {
            return;
        }
        Inventory created = Inventory.builder()
                .partId(partId)
                .currentQuantity(0)
                .safetyStock(10)
                .build();
        created.setDeleted(0);
        created.setCreateTime(now);
        created.setUpdateTime(now);
        inventoryMapper.insert(created);
    }

    private String appendReason(String original, String reason) {
        if (!StringUtils.hasText(reason)) {
            return original;
        }
        String tag = "[驳回] " + reason;
        return StringUtils.hasText(original) ? original + " | " + tag : tag;
    }
}
