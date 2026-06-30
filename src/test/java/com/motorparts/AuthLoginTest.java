package com.motorparts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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
 * 登录认证（BCrypt 密码）专项测试。
 *
 * <p>验证密码改为 BCrypt 加密存储后，登录链路的正确性：</p>
 * <ol>
 *   <li>新建用户时密码被加密存储 → 用明文仍能登录成功；</li>
 *   <li>密码错误时登录失败；</li>
 *   <li>账号不存在时登录失败；</li>
 *   <li>改密后旧密码失效、新密码可登录（改密闭环）。</li>
 * </ol>
 *
 * <p>前置：@SpringBootTest 全量上下文集成测试，需本地 MySQL 8 已启动且库 `motorparts` 可用。
 * 运行：{@code mvn test -Dtest=AuthLoginTest}。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("登录认证 BCrypt 测试")
class AuthLoginTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 唯一后缀，避免与样例数据或多次运行的唯一索引冲突 */
    private static final String SUFFIX = String.valueOf(System.currentTimeMillis() % 1_000_000);
    private static final String USERNAME = "login_" + SUFFIX;
    private static final String RAW_PASSWORD = "Init@123456";
    private static final String NEW_PASSWORD = "New@654321";

    private static Long userId;

    // ==================== 通用工具 ====================

    private JsonNode send(MockHttpServletRequestBuilder builder, String json) throws Exception {
        if (json != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        MvcResult result = mockMvc.perform(builder).andExpect(status().isOk()).andReturn();
        result.getResponse().setCharacterEncoding("UTF-8");
        String body = result.getResponse().getContentAsString();
        return (body == null || body.isEmpty()) ? objectMapper.createObjectNode() : objectMapper.readTree(body);
    }

    /** 断言 Result.code == 200 并返回 data 节点 */
    private JsonNode ok(MockHttpServletRequestBuilder builder, String json) throws Exception {
        JsonNode root = send(builder, json);
        assertEquals(200, root.path("code").asInt(), () -> "期望成功(code=200)，实际响应: " + root);
        return root.path("data");
    }

    private String loginBody(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    // ==================== 测试用例 ====================

    @Test
    @Order(1)
    @DisplayName("准备：创建测试用户（密码经 BCrypt 加密入库）")
    void prepare_createUser() throws Exception {
        String body = "{"
                + "\"username\":\"" + USERNAME + "\","
                + "\"password\":\"" + RAW_PASSWORD + "\","
                + "\"realName\":\"登录测试用户\","
                + "\"role\":\"purchase\","
                + "\"department\":\"采购部\","
                + "\"email\":\"" + USERNAME + "@motorparts.com\","
                + "\"status\":1}";
        JsonNode data = ok(post("/api/users/create"), body);
        userId = data.path("id").asLong();
        assertTrue(userId > 0, "用户应创建成功并返回自增ID");
    }

    @Test
    @Order(2)
    @DisplayName("登录成功：用明文密码登录，下发 token 与角色")
    void login_success() throws Exception {
        JsonNode data = ok(post("/api/auth/login"), loginBody(USERNAME, RAW_PASSWORD));
        assertFalse(data.path("token").asText().isEmpty(), "登录成功应下发非空 token");
        assertEquals(USERNAME, data.path("username").asText());
        assertEquals("purchase", data.path("role").asText());
    }

    @Test
    @Order(3)
    @DisplayName("登录失败：密码错误")
    void login_fail_wrongPassword() throws Exception {
        JsonNode root = send(post("/api/auth/login"), loginBody(USERNAME, "wrong-password"));
        assertNotEquals(200, root.path("code").asInt(), "密码错误不应返回成功");
        assertTrue(root.path("token").isMissingNode() || root.path("data").path("token").isMissingNode(),
                "登录失败不应下发 token");
    }

    @Test
    @Order(4)
    @DisplayName("登录失败：账号不存在")
    void login_fail_userNotExists() throws Exception {
        JsonNode root = send(post("/api/auth/login"), loginBody("no_such_user_" + SUFFIX, RAW_PASSWORD));
        assertNotEquals(200, root.path("code").asInt(), "账号不存在不应返回成功");
    }

    @Test
    @Order(5)
    @DisplayName("改密闭环：改密后旧密码失效、新密码可登录")
    void changePassword_then_login() throws Exception {
        // 调用改密接口（后端用 BCrypt 校验原密码、加密新密码）
        String changeBody = "{"
                + "\"id\":" + userId + ","
                + "\"oldPassword\":\"" + RAW_PASSWORD + "\","
                + "\"newPassword\":\"" + NEW_PASSWORD + "\"}";
        ok(patch("/api/users/change-password"), changeBody);

        // 旧密码应失败
        JsonNode oldRoot = send(post("/api/auth/login"), loginBody(USERNAME, RAW_PASSWORD));
        assertNotEquals(200, oldRoot.path("code").asInt(), "改密后旧密码应登录失败");

        // 新密码应成功
        JsonNode data = ok(post("/api/auth/login"), loginBody(USERNAME, NEW_PASSWORD));
        assertFalse(data.path("token").asText().isEmpty(), "新密码应登录成功并下发 token");
    }

    @Test
    @Order(6)
    @DisplayName("改密失败：原密码不正确")
    void changePassword_fail_wrongOldPassword() throws Exception {
        String changeBody = "{"
                + "\"id\":" + userId + ","
                + "\"oldPassword\":\"definitely-wrong\","
                + "\"newPassword\":\"Whatever@123\"}";
        JsonNode root = send(patch("/api/users/change-password"), changeBody);
        assertNotEquals(200, root.path("code").asInt(), "原密码错误时改密应失败");
    }

    @Test
    @Order(99)
    @DisplayName("收尾：逻辑删除测试用户")
    void cleanup() throws Exception {
        if (userId != null) {
            ok(delete("/api/users/delete/{id}", userId), null);
        }
    }
}
