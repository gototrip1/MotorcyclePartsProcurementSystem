package com.motorparts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.motorparts.entity.StockOutDetail;
import com.motorparts.entity.StockOutOrder;
import com.motorparts.entity.StockOutOrderWithDetails;
import com.motorparts.mapper.StockOutDetailMapper;
import com.motorparts.mapper.StockOutOrderMapper;
import com.motorparts.service.StockOutOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 领用单（出库单）服务实现类
 *
 * <p>注意：本类只负责单据的创建/编辑/状态流转，<b>绝不触碰库存数量</b>。
 * 库存的减少只能由【审核中心】审核通过后触发（见 AuditServiceImpl#approveOutbound）。</p>
 */
@Service
@RequiredArgsConstructor
public class StockOutOrderServiceImpl extends ServiceImpl<StockOutOrderMapper, StockOutOrder> implements StockOutOrderService {

    private final StockOutDetailMapper stockOutDetailMapper;

    @Override
    public Page<StockOutOrder> pageStockOuts(Page<StockOutOrder> page, String stockOutNumber,
                                             Integer status, String department, Long userId) {
        LambdaQueryWrapper<StockOutOrder> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(stockOutNumber)) {
            wrapper.like(StockOutOrder::getStockOutNumber, stockOutNumber);
        }
        if (status != null) {
            wrapper.eq(StockOutOrder::getStatus, status);
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(StockOutOrder::getDepartment, department);
        }
        if (userId != null) {
            wrapper.eq(StockOutOrder::getUserId, userId);
        }

        wrapper.orderByDesc(StockOutOrder::getStockOutTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createStockOut(StockOutOrder order) {
        if (order == null || !StringUtils.hasText(order.getStockOutNumber())) {
            return false;
        }

        // 领用单号唯一性校验
        LambdaQueryWrapper<StockOutOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOutOrder::getStockOutNumber, order.getStockOutNumber());
        if (baseMapper.selectCount(wrapper) > 0) {
            return false;
        }

        if (order.getStockOutTime() == null) {
            order.setStockOutTime(LocalDateTime.now());
        }
        if (order.getStatus() == null) {
            order.setStatus(1); // 待审核
        }

        order.setDeleted(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        if (!save(order)) {
            return false;
        }

        saveDetails(order.getId(), order);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStockOut(StockOutOrder order) {
        if (order == null || order.getId() == null) {
            return false;
        }

        StockOutOrder existing = getById(order.getId());
        if (existing == null) {
            return false;
        }
        // 已出库单据不允许再编辑
        if (existing.getStatus() != null && existing.getStatus() == 2) {
            return false;
        }

        if (order.getStockOutTime() == null) {
            order.setStockOutTime(existing.getStockOutTime());
        }
        if (order.getStatus() == null) {
            order.setStatus(existing.getStatus());
        }
        order.setUpdateTime(LocalDateTime.now());

        if (!updateById(order)) {
            return false;
        }

        // 重建明细
        stockOutDetailMapper.physicalDeleteByStockOutId(order.getId());
        saveDetails(order.getId(), order);
        return true;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        LambdaUpdateWrapper<StockOutOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(StockOutOrder::getStatus, status)
               .eq(StockOutOrder::getId, id);
        return update(wrapper);
    }

    /**
     * 写入领用明细（仅当传入的是 StockOutOrderWithDetails 时）
     */
    private void saveDetails(Long stockOutId, StockOutOrder order) {
        if (!(order instanceof StockOutOrderWithDetails)) {
            return;
        }
        List<StockOutDetail> details = ((StockOutOrderWithDetails) order).getDetails();
        if (details == null || details.isEmpty()) {
            return;
        }
        for (StockOutDetail src : details) {
            StockOutDetail detail = StockOutDetail.builder()
                    .stockOutId(stockOutId)
                    .partId(src.getPartId())
                    .quantity(src.getQuantity())
                    .unitCost(src.getUnitCost())
                    .remark(src.getRemark())
                    .build();
            detail.setDeleted(0);
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            stockOutDetailMapper.insert(detail);
        }
    }
}
