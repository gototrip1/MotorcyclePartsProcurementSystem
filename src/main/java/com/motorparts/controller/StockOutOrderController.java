package com.motorparts.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.motorparts.common.PageResult;
import com.motorparts.common.Result;
import com.motorparts.common.enums.ResultCode;
import com.motorparts.entity.Part;
import com.motorparts.entity.StockOutDetail;
import com.motorparts.entity.StockOutOrder;
import com.motorparts.entity.StockOutOrderWithDetails;
import com.motorparts.mapper.PartMapper;
import com.motorparts.mapper.StockOutDetailMapper;
import com.motorparts.service.StockOutOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 领用单（出库单）控制器 —— 领用人员发起，管理员审核出库
 */
@Tag(name = "领用出库管理")
@RestController
@RequestMapping("/api/stock-out-orders")
@RequiredArgsConstructor
@Validated
public class StockOutOrderController {

    private final StockOutOrderService stockOutOrderService;
    private final StockOutDetailMapper stockOutDetailMapper;
    private final PartMapper partMapper;

    /**
     * 创建领用单（含明细），库存不变动
     */
    @Operation(summary = "创建领用单")
    @PostMapping("/create")
    public Result<StockOutOrderWithDetails> create(@Valid @RequestBody StockOutOrderWithDetails order) {
        boolean success = stockOutOrderService.createStockOut(order);
        if (!success) {
            return Result.error(ResultCode.DATA_EXISTS);
        }
        return Result.success(order);
    }

    /**
     * 更新领用单（含明细）
     */
    @Operation(summary = "更新领用单")
    @PutMapping("/update/{id}")
    public Result<StockOutOrderWithDetails> update(
            @Parameter(description = "领用单ID") @PathVariable Long id,
            @Valid @RequestBody StockOutOrderWithDetails order) {
        order.setId(id);
        boolean success = stockOutOrderService.updateStockOut(order);
        if (!success) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }
        return Result.success(order);
    }

    /**
     * 删除领用单（逻辑删除）
     */
    @Operation(summary = "删除领用单")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@Parameter(description = "领用单ID") @PathVariable Long id) {
        boolean success = stockOutOrderService.removeById(id);
        if (!success) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }
        return Result.success();
    }

    /**
     * 获取单个领用单（含明细及零部件详情）
     */
    @Operation(summary = "获取单个领用单")
    @GetMapping("/get/{id}")
    public Result<Map<String, Object>> getById(@Parameter(description = "领用单ID") @PathVariable Long id) {
        StockOutOrder order = stockOutOrderService.getById(id);
        if (order == null) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }

        List<StockOutDetail> details = stockOutDetailMapper.selectByStockOutId(id);
        List<Map<String, Object>> detailViews = new ArrayList<>();
        for (StockOutDetail detail : details) {
            Map<String, Object> view = new HashMap<>();
            view.put("detail", detail);
            view.put("part", partMapper.selectById(detail.getPartId()));
            detailViews.add(view);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", detailViews);
        return Result.success(result);
    }

    /**
     * 分页查询领用单
     */
    @Operation(summary = "分页查询领用单")
    @GetMapping("/page")
    public Result<PageResult<StockOutOrder>> page(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "领用单号") @RequestParam(required = false) String stockOutNumber,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "领用部门") @RequestParam(required = false) String department,
            @Parameter(description = "领用人ID") @RequestParam(required = false) Long userId) {
        Page<StockOutOrder> page = new Page<>(current, size);
        Page<StockOutOrder> result = stockOutOrderService.pageStockOuts(page, stockOutNumber, status, department, userId);
        return Result.success(PageResult.from(result));
    }

    /**
     * 获取领用单明细
     */
    @Operation(summary = "获取领用单明细")
    @GetMapping("/details/{id}")
    public Result<List<StockOutDetail>> details(@Parameter(description = "领用单ID") @PathVariable Long id) {
        List<StockOutDetail> details = stockOutDetailMapper.selectList(
                new LambdaQueryWrapper<StockOutDetail>().eq(StockOutDetail::getStockOutId, id));
        return Result.success(details);
    }

    /**
     * 取消领用单（status → 3 已取消），库存不变动
     */
    @Operation(summary = "取消领用单")
    @PatchMapping("/cancel/{id}")
    public Result<Void> cancel(@Parameter(description = "领用单ID") @PathVariable Long id) {
        boolean success = stockOutOrderService.updateStatus(id, 3);
        if (!success) {
            return Result.error(ResultCode.DATA_NOT_EXISTS);
        }
        return Result.success();
    }
}
