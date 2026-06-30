package com.motorparts.controller;

import com.motorparts.common.Result;
import com.motorparts.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 审核中心控制器 —— 库存变动的唯一入口（管理员审核）。
 */
@Tag(name = "审核中心")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Validated
public class AuditController {

    private final AuditService auditService;

    /**
     * 审核通过采购入库 → 库存增加
     */
    @Operation(summary = "审核通过采购入库（库存+）")
    @PostMapping("/inbound/approve/{orderId}")
    public Result<Void> approveInbound(@Parameter(description = "采购单ID") @PathVariable Long orderId) {
        auditService.approveInbound(orderId);
        return Result.success();
    }

    /**
     * 驳回采购入库申请（库存不变）
     */
    @Operation(summary = "驳回采购入库申请")
    @PostMapping("/inbound/reject/{orderId}")
    public Result<Void> rejectInbound(
            @Parameter(description = "采购单ID") @PathVariable Long orderId,
            @Parameter(description = "驳回原因") @RequestParam(required = false) String reason) {
        auditService.rejectInbound(orderId, reason);
        return Result.success();
    }

    /**
     * 审核通过领用出库 → 库存减少
     */
    @Operation(summary = "审核通过领用出库（库存-）")
    @PostMapping("/outbound/approve/{stockOutId}")
    public Result<Void> approveOutbound(@Parameter(description = "领用单ID") @PathVariable Long stockOutId) {
        auditService.approveOutbound(stockOutId);
        return Result.success();
    }

    /**
     * 驳回领用出库申请（库存不变）
     */
    @Operation(summary = "驳回领用出库申请")
    @PostMapping("/outbound/reject/{stockOutId}")
    public Result<Void> rejectOutbound(
            @Parameter(description = "领用单ID") @PathVariable Long stockOutId,
            @Parameter(description = "驳回原因") @RequestParam(required = false) String reason) {
        auditService.rejectOutbound(stockOutId, reason);
        return Result.success();
    }
}
