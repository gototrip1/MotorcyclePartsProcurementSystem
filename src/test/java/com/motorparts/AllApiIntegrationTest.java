package com.motorparts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 全接口集成测试。
 *
 * <p>覆盖系统全部 REST 接口（登录认证 / 用户 / 供应商 / 零件 / 采购单 / 领用单 / 审核中心 / 库存 / 统计），
 * 模拟前端真实请求数据，并按业务主线串联：
 * 登录 → 建供应商 → 建零件 → 建采购单 → 录物流 → 提交入库 → 审核入库(库存+) → 标记付款
 *        → 建领用单 → 审核出库(库存-) → 驳回流程 → 库存/统计查询 → 逻辑删除。</p>
 *
 * <p>前置：本测试为 @SpringBootTest 全量上下文集成测试，需本地 MySQL 8 已启动且库 `motorparts` 可用
 * （启动时 DatabaseInitializer 会自动建表并灌入样例数据）。查询参数一律用 .param() 传递，避免中文/空格编码问题。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AllApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 唯一后缀，避免与样例数据或多次运行的唯一索引冲突 */
    private static final String SUFFIX = String.valueOf(System.currentTimeMillis() % 1_000_000);

    // ----- 跨用例共享的 ID -----
    private static Long supplierId;
    private static Long partId;
    private static Long orderId;          // 走完整入库流程的采购单
    private static Long rejectOrderId;    // 用于驳回入库的采购单
    private static Long stockOutId;       // 走完整出库流程的领用单
    private static Long rejectStockOutId; // 用于驳回出库的领用单
    private static Long requisitionUserId;// 领用人
    private static Long throwawayUserId;  // 用于删除用例
    private static Long inventoryId;      // 库存记录ID

    // ==================== 通用工具 ====================

    private JsonNode send(MockHttpServletRequestBuilder builder, String json) throws Exception {
        if (json != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        MvcResult result = mockMvc.perform(builder)
                .andExpect(status().isOk())
                .andReturn();
        result.getResponse().setCharacterEncoding("UTF-8");
        String body = result.getResponse().getContentAsString();
        return (body == null || body.isEmpty()) ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    /** 断言 Result.code == 200 并返回 data 节点 */
    private JsonNode ok(MockHttpServletRequestBuilder builder, String json) throws Exception {
        JsonNode root = send(builder, json);
        assertEquals(200, root.path("code").asInt(),
                () -> "期望成功(code=200)，实际响应: " + root);
        return root.path("data");
    }

    // ==================== 1. 登录认证 /api/auth ====================

    @Test
    @Order(1)
    void auth_login() throws Exception {
        // 样例库密码为占位 BCrypt 串，明文比对预期不通过；此处仅校验接口连通并返回统一结构
        String body = "{\"username\":\"user1\",\"password\":\"user123\"}";
        JsonNode root = send(post("/api/auth/login"), body);
        assertTrue(root.has("code"), "登录接口应返回统一结构 Result{code,...}");
    }

    @Test
    @Order(2)
    void auth_logout() throws Exception {
        ok(post("/api/auth/logout").header("Authorization", "test-token"), null);
    }

    // ==================== 2. 用户管理 /api/users ====================

    @Test
    @Order(10)
    void user_create_requisition() throws Exception {
        String body = "{"
                + "\"username\":\"req_" + SUFFIX + "\","
                + "\"password\":\"123456\","
                + "\"realName\":\"测试领用员\","
                + "\"role\":\"requisition\","
                + "\"department\":\"生产车间\","
                + "\"phone\":\"13800001111\","
                + "\"email\":\"req" + SUFFIX + "@motorparts.com\","
                + "\"status\":1}";
        JsonNode data = ok(post("/api/users/create"), body);
        requisitionUserId = data.path("id").asLong();
        assertTrue(requisitionUserId > 0);
    }

    @Test
    @Order(11)
    void user_create_throwaway() throws Exception {
        String body = "{"
                + "\"username\":\"tmp_" + SUFFIX + "\","
                + "\"password\":\"123456\","
                + "\"realName\":\"待删除用户\","
                + "\"role\":\"purchase\","
                + "\"department\":\"采购部\","
                + "\"email\":\"tmp" + SUFFIX + "@motorparts.com\"}";
        JsonNode data = ok(post("/api/users/create"), body);
        throwawayUserId = data.path("id").asLong();
    }

    @Test
    @Order(12)
    void user_page() throws Exception {
        ok(get("/api/users/page").param("current", "1").param("size", "10").param("role", "requisition"), null);
    }

    @Test
    @Order(13)
    void user_get() throws Exception {
        ok(get("/api/users/get/{id}", requisitionUserId), null);
    }

    @Test
    @Order(14)
    void user_assignRole() throws Exception {
        ok(patch("/api/users/assign-role/{id}", throwawayUserId).param("role", "requisition"), null);
    }

    @Test
    @Order(15)
    void user_disable_then_enable() throws Exception {
        ok(patch("/api/users/disable/{id}", throwawayUserId), null);
        ok(patch("/api/users/enable/{id}", throwawayUserId), null);
    }

    @Test
    @Order(16)
    void user_resetPassword() throws Exception {
        ok(patch("/api/users/reset-password/{id}", throwawayUserId).param("password", "newpass123"), null);
    }

    @Test
    @Order(17)
    void user_update() throws Exception {
        String body = "{"
                + "\"username\":\"tmp_" + SUFFIX + "\","
                + "\"password\":\"123456\","
                + "\"realName\":\"待删除用户(已改名)\","
                + "\"role\":\"purchase\","
                + "\"department\":\"采购部\","
                + "\"email\":\"tmp" + SUFFIX + "@motorparts.com\","
                + "\"status\":1}";
        ok(put("/api/users/update/{id}", throwawayUserId), body);
    }

    @Test
    @Order(18)
    void auth_login_success() throws Exception {
        // 用刚创建的领用员（明文密码 123456）真正登录，断言下发 token 与角色
        String body = "{\"username\":\"req_" + SUFFIX + "\",\"password\":\"123456\"}";
        JsonNode data = ok(post("/api/auth/login"), body);
        assertFalse(data.path("token").asText().isEmpty(), "登录成功应下发 token");
        assertEquals("requisition", data.path("role").asText());
    }

    // ==================== 3. 供应商管理 /api/suppliers ====================

    @Test
    @Order(20)
    void supplier_create() throws Exception {
        String body = "{"
                + "\"supplierCode\":\"SUPT" + SUFFIX + "\","
                + "\"name\":\"测试供应商-" + SUFFIX + "\","
                + "\"contactPerson\":\"陈经理\","
                + "\"phone\":\"02368001001\","
                + "\"email\":\"sup" + SUFFIX + "@test.com\","
                + "\"address\":\"重庆市九龙坡区工业园1号\","
                + "\"creditRating\":\"A\","
                + "\"status\":3}";
        JsonNode data = ok(post("/api/suppliers/create"), body);
        supplierId = data.path("id").asLong();
        assertTrue(supplierId > 0);
    }

    @Test
    @Order(21)
    void supplier_page() throws Exception {
        ok(get("/api/suppliers/page").param("current", "1").param("size", "10"), null);
    }

    @Test
    @Order(22)
    void supplier_get() throws Exception {
        ok(get("/api/suppliers/get/{id}", supplierId), null);
    }

    @Test
    @Order(23)
    void supplier_search() throws Exception {
        ok(get("/api/suppliers/search").param("name", "测试供应商"), null);
    }

    @Test
    @Order(24)
    void supplier_updateStatus_auditPass() throws Exception {
        // 管理员审核通过：审核中(3) → 合作中(1)
        ok(patch("/api/suppliers/update-status/{id}", supplierId).param("status", "1"), null);
    }

    @Test
    @Order(25)
    void supplier_updateCreditRating() throws Exception {
        ok(patch("/api/suppliers/update-credit-rating/{id}", supplierId).param("creditRating", "B"), null);
    }

    @Test
    @Order(26)
    void supplier_update() throws Exception {
        String body = "{"
                + "\"supplierCode\":\"SUPT" + SUFFIX + "\","
                + "\"name\":\"测试供应商-" + SUFFIX + "(已更新)\","
                + "\"contactPerson\":\"陈经理\","
                + "\"phone\":\"02368001002\","
                + "\"email\":\"sup" + SUFFIX + "@test.com\","
                + "\"creditRating\":\"B\","
                + "\"status\":1}";
        ok(put("/api/suppliers/update/{id}", supplierId), body);
    }

    // ==================== 4. 零件管理 /api/parts ====================

    @Test
    @Order(30)
    void part_create() throws Exception {
        String body = "{"
                + "\"partCode\":\"PT" + SUFFIX + "\","
                + "\"name\":\"测试活塞-" + SUFFIX + "\","
                + "\"model\":\"CG125\","
                + "\"specification\":\"标准缸径 56.5mm\","
                + "\"unit\":\"个\","
                + "\"purchasePrice\":45.00,"
                + "\"suggestedRetailPrice\":78.00,"
                + "\"stockWarningValue\":20,"
                + "\"supplierId\":" + supplierId + ","
                + "\"category\":\"发动机类\","
                + "\"description\":\"集成测试零件\"}";
        JsonNode data = ok(post("/api/parts/create"), body);
        partId = data.path("id").asLong();
        assertTrue(partId > 0);
    }

    @Test
    @Order(31)
    void part_page() throws Exception {
        ok(get("/api/parts/page").param("current", "1").param("size", "10").param("category", "发动机类"), null);
    }

    @Test
    @Order(32)
    void part_get() throws Exception {
        ok(get("/api/parts/get/{id}", partId), null);
    }

    @Test
    @Order(33)
    void part_search() throws Exception {
        ok(get("/api/parts/search").param("name", "测试活塞"), null);
    }

    @Test
    @Order(34)
    void part_updatePrice_adminLock() throws Exception {
        // 管理员锁定采购单价
        ok(patch("/api/parts/update-price/{id}", partId).param("purchasePrice", "46.50"), null);
    }

    @Test
    @Order(35)
    void part_getBySupplier() throws Exception {
        ok(get("/api/parts/supplier/{supplierId}", supplierId), null);
    }

    @Test
    @Order(36)
    void part_update() throws Exception {
        String body = "{"
                + "\"partCode\":\"PT" + SUFFIX + "\","
                + "\"name\":\"测试活塞-" + SUFFIX + "(已更新)\","
                + "\"model\":\"CG125\","
                + "\"unit\":\"个\","
                + "\"purchasePrice\":46.50,"
                + "\"stockWarningValue\":20,"
                + "\"supplierId\":" + supplierId + ","
                + "\"category\":\"发动机类\"}";
        ok(put("/api/parts/update/{id}", partId), body);
    }

    // ==================== 5. 采购入库流程 /api/orders ====================

    @Test
    @Order(40)
    void order_create() throws Exception {
        String body = "{"
                + "\"orderNumber\":\"PO" + SUFFIX + "A\","
                + "\"createdBy\":" + requisitionUserId + ","
                + "\"expectedDeliveryDate\":\"2026-07-01\","
                + "\"remark\":\"集成测试采购单\","
                + "\"orderDetail\":[{"
                + "  \"partId\":" + partId + ",\"quantity\":50,\"unitPrice\":46.50,\"remark\":\"活塞采购\"}]"
                + "}";
        JsonNode data = ok(post("/api/orders/create"), body);
        orderId = data.path("id").asLong();
        assertTrue(orderId > 0);
    }

    @Test
    @Order(41)
    void order_page() throws Exception {
        ok(get("/api/orders/page").param("current", "1").param("size", "10"), null);
    }

    @Test
    @Order(42)
    void order_get() throws Exception {
        ok(get("/api/orders/get/{id}", orderId), null);
    }

    @Test
    @Order(43)
    void order_details() throws Exception {
        ok(get("/api/orders/details/{id}", orderId), null);
    }

    @Test
    @Order(44)
    void order_searchDetails() throws Exception {
        ok(get("/api/orders/search-details").param("partName", "测试活塞"), null);
    }

    @Test
    @Order(45)
    void order_statistics() throws Exception {
        ok(get("/api/orders/statistics"), null);
    }

    @Test
    @Order(46)
    void order_updateLogistics() throws Exception {
        ok(patch("/api/orders/update-logistics/{id}", orderId)
                .param("logisticsCompany", "顺丰物流")
                .param("trackingNumber", "SF" + SUFFIX)
                .param("shipTime", "2026-06-20 08:00:00")
                .param("expectedDeliveryDate", "2026-07-01"), null);
    }

    @Test
    @Order(47)
    void order_submitInbound() throws Exception {
        // 采购中(1) → 待入库审核(2)
        ok(patch("/api/orders/submit-inbound/{id}", orderId), null);
    }

    // ==================== 6. 审核中心入库 /api/audit ====================

    @Test
    @Order(50)
    void audit_approveInbound() throws Exception {
        // 待入库审核(2) → 已入库(3)，库存 +50
        ok(post("/api/audit/inbound/approve/{orderId}", orderId), null);
    }

    @Test
    @Order(51)
    void order_markPaid() throws Exception {
        ok(patch("/api/orders/mark-paid/{id}", orderId), null);
    }

    @Test
    @Order(52)
    void audit_rejectInbound_flow() throws Exception {
        // 新建采购单 → 提交入库 → 驳回（库存不变，状态回退采购中）
        String body = "{"
                + "\"orderNumber\":\"PO" + SUFFIX + "B\","
                + "\"createdBy\":" + requisitionUserId + ","
                + "\"orderDetail\":[{\"partId\":" + partId + ",\"quantity\":10,\"unitPrice\":46.50}]"
                + "}";
        JsonNode data = ok(post("/api/orders/create"), body);
        rejectOrderId = data.path("id").asLong();
        ok(patch("/api/orders/submit-inbound/{id}", rejectOrderId), null);
        ok(post("/api/audit/inbound/reject/{orderId}", rejectOrderId).param("reason", "单据信息不符"), null);
    }

    @Test
    @Order(53)
    void order_update() throws Exception {
        // 在已驳回(采购中)的单据上测试整单编辑（PUT，会重建明细）
        String body = "{"
                + "\"orderNumber\":\"PO" + SUFFIX + "B\","
                + "\"createdBy\":" + requisitionUserId + ","
                + "\"remark\":\"驳回后修订\","
                + "\"orderDetail\":[{\"partId\":" + partId + ",\"quantity\":8,\"unitPrice\":46.50}]"
                + "}";
        ok(put("/api/orders/update/{id}", rejectOrderId), body);
    }

    @Test
    @Order(54)
    void order_updateStatus() throws Exception {
        // 通用状态更新接口：置为已取消(4)
        ok(patch("/api/orders/update-status/{id}", rejectOrderId).param("status", "4"), null);
    }

    // ==================== 7. 领用出库流程 /api/stock-out-orders ====================

    @Test
    @Order(60)
    void stockOut_create() throws Exception {
        String body = "{"
                + "\"stockOutNumber\":\"SO" + SUFFIX + "A\","
                + "\"userId\":" + requisitionUserId + ","
                + "\"department\":\"生产车间\","
                + "\"purpose\":\"生产\","
                + "\"remark\":\"集成测试领用单\","
                + "\"details\":[{\"partId\":" + partId + ",\"quantity\":5,\"unitCost\":46.50,\"remark\":\"生产领用\"}]"
                + "}";
        JsonNode data = ok(post("/api/stock-out-orders/create"), body);
        stockOutId = data.path("id").asLong();
        assertTrue(stockOutId > 0);
    }

    @Test
    @Order(61)
    void stockOut_page() throws Exception {
        ok(get("/api/stock-out-orders/page").param("current", "1").param("size", "10").param("department", "生产车间"), null);
    }

    @Test
    @Order(62)
    void stockOut_get() throws Exception {
        ok(get("/api/stock-out-orders/get/{id}", stockOutId), null);
    }

    @Test
    @Order(63)
    void stockOut_details() throws Exception {
        ok(get("/api/stock-out-orders/details/{id}", stockOutId), null);
    }

    @Test
    @Order(64)
    void stockOut_update() throws Exception {
        String body = "{"
                + "\"stockOutNumber\":\"SO" + SUFFIX + "A\","
                + "\"userId\":" + requisitionUserId + ","
                + "\"department\":\"生产车间\","
                + "\"purpose\":\"维修\","
                + "\"remark\":\"集成测试领用单(已改用途)\","
                + "\"details\":[{\"partId\":" + partId + ",\"quantity\":3,\"unitCost\":46.50}]"
                + "}";
        ok(put("/api/stock-out-orders/update/{id}", stockOutId), body);
    }

    // ==================== 8. 审核中心出库 /api/audit ====================

    @Test
    @Order(70)
    void audit_approveOutbound() throws Exception {
        // 待审核(1) → 已出库(2)，库存 -（入库已 +50，足够扣减）
        ok(post("/api/audit/outbound/approve/{stockOutId}", stockOutId), null);
    }

    @Test
    @Order(71)
    void audit_rejectOutbound_flow() throws Exception {
        // 新建领用单 → 驳回（库存不变，状态 → 已取消）
        String body = "{"
                + "\"stockOutNumber\":\"SO" + SUFFIX + "B\","
                + "\"userId\":" + requisitionUserId + ","
                + "\"department\":\"维修部\","
                + "\"purpose\":\"维修\","
                + "\"details\":[{\"partId\":" + partId + ",\"quantity\":2,\"unitCost\":46.50}]"
                + "}";
        JsonNode data = ok(post("/api/stock-out-orders/create"), body);
        rejectStockOutId = data.path("id").asLong();
        ok(post("/api/audit/outbound/reject/{stockOutId}", rejectStockOutId).param("reason", "库存留作他用"), null);
    }

    @Test
    @Order(72)
    void stockOut_cancel() throws Exception {
        // 新建领用单并取消（status → 3 已取消），库存不变
        String body = "{"
                + "\"stockOutNumber\":\"SO" + SUFFIX + "C\","
                + "\"userId\":" + requisitionUserId + ","
                + "\"department\":\"总装线\","
                + "\"purpose\":\"其他\","
                + "\"details\":[{\"partId\":" + partId + ",\"quantity\":1,\"unitCost\":46.50}]"
                + "}";
        JsonNode data = ok(post("/api/stock-out-orders/create"), body);
        Long cancelId = data.path("id").asLong();
        ok(patch("/api/stock-out-orders/cancel/{id}", cancelId), null);
        ok(delete("/api/stock-out-orders/delete/{id}", cancelId), null);
    }

    // ==================== 9. 库存管理 /api/inventory ====================

    @Test
    @Order(80)
    void inventory_page() throws Exception {
        JsonNode data = ok(get("/api/inventory/page").param("current", "1").param("size", "10").param("partName", "测试活塞"), null);
        JsonNode records = data.path("records");
        if (records.isArray() && records.size() > 0) {
            inventoryId = records.get(0).path("id").asLong();
        }
    }

    @Test
    @Order(81)
    void inventory_warning() throws Exception {
        ok(get("/api/inventory/warning"), null);
    }

    @Test
    @Order(82)
    void inventory_check() throws Exception {
        ok(get("/api/inventory/check"), null);
    }

    @Test
    @Order(83)
    void inventory_get_and_updateSafetyStock() throws Exception {
        if (inventoryId == null) {
            return; // 库存记录未取到则跳过（正常情况下入库已建账）
        }
        ok(get("/api/inventory/get/{id}", inventoryId), null);
        ok(patch("/api/inventory/update-safety-stock/{id}", inventoryId).param("safetyStock", "15"), null);
    }

    @Test
    @Order(84)
    void inventory_inbound_outbound_directOps() throws Exception {
        // 库存模块自带的入/出库直接操作接口
        ok(post("/api/inventory/inbound").param("partId", String.valueOf(partId))
                .param("quantity", "10").param("warehouseLocation", "A区-1号库"), null);
        ok(post("/api/inventory/outbound").param("partId", String.valueOf(partId))
                .param("quantity", "3"), null);
    }

    // ==================== 10. 统计分析 /api/statistics ====================

    @Test
    @Order(90)
    void statistics_dashboard() throws Exception {
        ok(get("/api/statistics/dashboard"), null);
    }

    @Test
    @Order(91)
    void statistics_suppliers() throws Exception {
        ok(get("/api/statistics/suppliers"), null);
    }

    @Test
    @Order(92)
    void statistics_parts() throws Exception {
        ok(get("/api/statistics/parts"), null);
    }

    @Test
    @Order(93)
    void statistics_inventory() throws Exception {
        ok(get("/api/statistics/inventory"), null);
    }

    @Test
    @Order(94)
    void statistics_monthlyTrend() throws Exception {
        ok(get("/api/statistics/monthly-trend"), null);
    }

    // ==================== 11. 逻辑删除（收尾） ====================

    @Test
    @Order(100)
    void cleanup_logicalDeletes() throws Exception {
        ok(delete("/api/stock-out-orders/delete/{id}", rejectStockOutId), null);
        ok(delete("/api/orders/delete/{id}", rejectOrderId), null);
        ok(delete("/api/users/delete/{id}", throwawayUserId), null);
    }
}
