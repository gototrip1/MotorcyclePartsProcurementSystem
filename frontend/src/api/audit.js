import request from './request'

// 对应后端 AuditController（基础路径 /api/audit）
export const auditApi = {
  // POST /audit/inbound/approve/{orderId} 入库审核通过 -> 库存 +
  approveInbound: (orderId) => request.post(`/audit/inbound/approve/${orderId}`),
  // POST /audit/inbound/reject/{orderId}?reason=
  rejectInbound: (orderId, reason) =>
    request.post(`/audit/inbound/reject/${orderId}`, null, { params: { reason } }),
  // POST /audit/outbound/approve/{stockOutId} 出库审核通过 -> 库存 -
  approveOutbound: (stockOutId) => request.post(`/audit/outbound/approve/${stockOutId}`),
  // POST /audit/outbound/reject/{stockOutId}?reason=
  rejectOutbound: (stockOutId, reason) =>
    request.post(`/audit/outbound/reject/${stockOutId}`, null, { params: { reason } })
}
