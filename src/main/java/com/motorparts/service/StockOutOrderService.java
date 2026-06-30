package com.motorparts.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.motorparts.entity.StockOutOrder;

/**
 * 领用单（出库单）服务接口
 */
public interface StockOutOrderService extends IService<StockOutOrder> {

    /**
     * 分页查询领用单
     *
     * @param page       分页参数
     * @param stockOutNumber 领用单号（可选）
     * @param status     状态（可选）
     * @param department 领用部门（可选）
     * @param userId     领用人ID（可选）
     * @return 分页结果
     */
    Page<StockOutOrder> pageStockOuts(Page<StockOutOrder> page, String stockOutNumber,
                                      Integer status, String department, Long userId);

    /**
     * 创建领用单（包含领用明细），库存不在此变动
     *
     * @param order 领用单（含明细）
     * @return 是否成功
     */
    boolean createStockOut(StockOutOrder order);

    /**
     * 更新领用单（含明细），库存不在此变动
     *
     * @param order 领用单（含明细）
     * @return 是否成功
     */
    boolean updateStockOut(StockOutOrder order);

    /**
     * 更新领用单状态
     *
     * @param id     领用单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}
