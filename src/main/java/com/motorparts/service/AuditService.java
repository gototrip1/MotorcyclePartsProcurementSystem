package com.motorparts.service;

/**
 * 审核中心服务接口 —— 系统中<b>唯一</b>允许触发库存变动的入口。
 *
 * <p>采购入库审核通过 → 库存增加；领用出库审核通过 → 库存减少。
 * 创建单据的 Service 一律不碰库存。</p>
 */
public interface AuditService {

    /**
     * 审核采购入库：通过 → 库存增加，采购单状态置为「已入库」。
     *
     * @param orderId 采购单ID
     */
    void approveInbound(Long orderId);

    /**
     * 驳回采购入库申请：仅记录原因，库存不变。
     *
     * @param orderId 采购单ID
     * @param reason  驳回原因
     */
    void rejectInbound(Long orderId, String reason);

    /**
     * 审核领用出库：先验库存 → 通过 → 库存减少，领用单状态置为「已出库」。
     *
     * @param stockOutId 领用单ID
     */
    void approveOutbound(Long stockOutId);

    /**
     * 驳回领用出库申请：仅记录原因并取消单据，库存不变。
     *
     * @param stockOutId 领用单ID
     * @param reason     驳回原因
     */
    void rejectOutbound(Long stockOutId, String reason);
}
