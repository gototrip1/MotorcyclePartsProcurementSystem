package com.motorparts.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.motorparts.entity.Inventory;
import com.motorparts.entity.Part;
import com.motorparts.mapper.InventoryMapper;
import com.motorparts.mapper.PartMapper;
import com.motorparts.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存服务实现类
 */
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private PartMapper partMapper;

    @Override
    public Page<Inventory> pageInventories(Page<Inventory> page, String partName, String warehouseLocation) {
        // 这里需要联表查询，简化处理：先查询所有，后续可优化
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(warehouseLocation)) {
            wrapper.like(Inventory::getWarehouseLocation, warehouseLocation);
        }

        wrapper.orderByDesc(Inventory::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inbound(Long partId, Integer quantity, String warehouseLocation) {
        if (partId == null || quantity == null || quantity <= 0) {
            return false;
        }

        // 检查零件是否存在
        Part part = partMapper.selectById(partId);
        if (part == null) {
            return false;
        }

        // 查找库存记录
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getPartId, partId);
        Inventory inventory = baseMapper.selectOne(wrapper);

        if (inventory == null) {
            // 创建新的库存记录
            inventory = Inventory.builder()
                    .partId(partId)
                    .currentQuantity(quantity)
                    .safetyStock(10)
                    .warehouseLocation(StringUtils.hasText(warehouseLocation) ? warehouseLocation : "A区-1号库")
                    .lastInboundTime(LocalDateTime.now())
                    .build();
            return save(inventory);
        } else {
            // 更新现有库存
            LambdaUpdateWrapper<Inventory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Inventory::getCurrentQuantity, inventory.getCurrentQuantity() + quantity)
                         .set(Inventory::getLastInboundTime, LocalDateTime.now())
                         .eq(Inventory::getId, inventory.getId());

            if (StringUtils.hasText(warehouseLocation)) {
                updateWrapper.set(Inventory::getWarehouseLocation, warehouseLocation);
            }

            return update(updateWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean outbound(Long partId, Integer quantity) {
        if (partId == null || quantity == null || quantity <= 0) {
            return false;
        }

        // 查找库存记录
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getPartId, partId);
        Inventory inventory = baseMapper.selectOne(wrapper);

        if (inventory == null || inventory.getCurrentQuantity() < quantity) {
            return false;
        }

        // 更新库存
        LambdaUpdateWrapper<Inventory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Inventory::getCurrentQuantity, inventory.getCurrentQuantity() - quantity)
                     .set(Inventory::getLastOutboundTime, LocalDateTime.now())
                     .eq(Inventory::getId, inventory.getId());

        return update(updateWrapper);
    }

    @Override
    public List<Inventory> getWarningList() {
        // 查询库存低于安全库存的记录
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("current_quantity < safety_stock")
               .orderByAsc(Inventory::getCurrentQuantity);

        return baseMapper.selectList(wrapper);
    }

    @Override
    public boolean updateSafetyStock(Long partId, Integer safetyStock) {
        if (partId == null || safetyStock == null || safetyStock < 0) {
            return false;
        }

        LambdaQueryWrapper<Inventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Inventory::getPartId, partId);
        Inventory inventory = baseMapper.selectOne(queryWrapper);

        if (inventory == null) {
            return false;
        }

        LambdaUpdateWrapper<Inventory> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Inventory::getSafetyStock, safetyStock)
                     .eq(Inventory::getId, inventory.getId());

        return update(updateWrapper);
    }
}