package com.motorparts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.motorparts.entity.PurchaseOrder;
import com.motorparts.mapper.PurchaseOrderMapper;
import com.motorparts.service.PurchaseOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购订单服务实现类
 */
@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    @Override
    public Page<PurchaseOrder> pageOrders(Page<PurchaseOrder> page, String orderNumber,
                                          Integer status, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(orderNumber)) {
            wrapper.like(PurchaseOrder::getOrderNumber, orderNumber);
        }

        if (status != null) {
            wrapper.eq(PurchaseOrder::getStatus, status);
        }

        if (startDate != null) {
            wrapper.ge(PurchaseOrder::getOrderTime, startDate.atStartOfDay());
        }

        if (endDate != null) {
            wrapper.le(PurchaseOrder::getOrderTime, endDate.plusDays(1).atStartOfDay());
        }

        wrapper.orderByDesc(PurchaseOrder::getOrderTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrder(PurchaseOrder order) {
        if (order == null || !StringUtils.hasText(order.getOrderNumber())) {
            return false;
        }

        // 检查订单编号是否已存在
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getOrderNumber, order.getOrderNumber());
        if (baseMapper.selectCount(wrapper) > 0) {
            return false;
        }

        // 设置默认值
        if (order.getOrderTime() == null) {
            order.setOrderTime(java.time.LocalDateTime.now());
        }

        if (order.getStatus() == null) {
            order.setStatus(1); // 待审核
        }

        return save(order);
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }

        LambdaUpdateWrapper<PurchaseOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(PurchaseOrder::getStatus, status)
               .eq(PurchaseOrder::getId, id);

        // 如果是已入库状态，更新实际交货日期
        if (status == 4) {
            wrapper.set(PurchaseOrder::getActualDeliveryDate, LocalDate.now());
        }

        return update(wrapper);
    }

    @Override
    public OrderStatistics getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();

        if (startDate != null) {
            wrapper.ge(PurchaseOrder::getOrderTime, startDate.atStartOfDay());
        }

        if (endDate != null) {
            wrapper.le(PurchaseOrder::getOrderTime, endDate.plusDays(1).atStartOfDay());
        }

        List<PurchaseOrder> orders = baseMapper.selectList(wrapper);

        long totalOrders = orders.size();
        long totalAmount = orders.stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .longValue();
        long completedOrders = orders.stream()
                .filter(order -> order.getStatus() != null && order.getStatus() == 4)
                .count();
        long pendingOrders = orders.stream()
                .filter(order -> order.getStatus() != null && (order.getStatus() == 1 || order.getStatus() == 2 || order.getStatus() == 3))
                .count();

        return new OrderStatistics() {
            @Override
            public long getTotalOrders() {
                return totalOrders;
            }

            @Override
            public long getTotalAmount() {
                return totalAmount;
            }

            @Override
            public long getCompletedOrders() {
                return completedOrders;
            }

            @Override
            public long getPendingOrders() {
                return pendingOrders;
            }
        };
    }
}