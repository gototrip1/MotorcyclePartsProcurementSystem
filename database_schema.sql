-- 摩托车零部件采购管理系统 - 数据库表结构（目标设计版）
-- 版本: 2.0.0
-- 说明:
--   纯内部进销存系统（采购入库 + 领用出库，无对外销售）
--   1) 仅 3 种角色: admin(系统管理员) / purchase(采购专员) / requisition(领用人员)
--   2) 已去除 customer(客户表)
--   3) 物流信息并入 purchase_order(采购单)，不单独建 logistics 表
--   4) 新增 stock_out_order(领用单) + stock_out_detail(领用明细)
--   5) 库存的增加/减少只由管理员审核通过的入库/出库触发
-- 数据库: motorparts（已创建）

CREATE DATABASE IF NOT EXISTS `motorparts`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `motorparts`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 清理旧对象（便于重复执行） ====================
DROP VIEW  IF EXISTS `order_detail_full_view`;
DROP VIEW  IF EXISTS `order_statistics_view`;
DROP VIEW  IF EXISTS `inventory_warning_view`;

DROP TABLE IF EXISTS `stock_out_detail`;
DROP TABLE IF EXISTS `stock_out_order`;
DROP TABLE IF EXISTS `order_detail`;
DROP TABLE IF EXISTS `inventory`;
DROP TABLE IF EXISTS `purchase_order`;
DROP TABLE IF EXISTS `part`;
DROP TABLE IF EXISTS `supplier`;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 表结构定义 ====================

-- 1. 用户/员工表 (user)
-- 注意：user 是 MySQL 关键字，需用反引号括起来
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(加密)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `role` varchar(20) NOT NULL DEFAULT 'purchase' COMMENT '角色(admin-系统管理员, purchase-采购专员, requisition-领用人员)',
  `department` varchar(50) DEFAULT NULL COMMENT '部门(管理部/采购部/生产车间)',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint DEFAULT '1' COMMENT '状态(1-正常, 2-禁用)',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户/员工表';

-- 2. 供应商表 (supplier)
CREATE TABLE `supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` varchar(50) NOT NULL COMMENT '供应商编码',
  `name` varchar(100) NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `credit_rating` char(1) DEFAULT 'B' COMMENT '信用评级(A-优秀, B-良好, C-一般, D-较差)',
  `status` tinyint DEFAULT '1' COMMENT '合作状态(1-合作中, 2-已终止, 3-审核中)',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_status` (`status`),
  KEY `idx_credit_rating` (`credit_rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';

-- 3. 产品/零部件表 (part)
CREATE TABLE `part` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '零部件ID',
  `part_code` varchar(50) NOT NULL COMMENT '零件编码',
  `name` varchar(100) NOT NULL COMMENT '零件名称',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `specification` varchar(200) DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) DEFAULT '个' COMMENT '单位(个/套/件/台等)',
  `purchase_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '采购单价(管理员锁定)',
  `suggested_retail_price` decimal(10,2) DEFAULT '0.00' COMMENT '建议零售价',
  `stock_warning_value` int DEFAULT '10' COMMENT '库存预警值',
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID',
  `category` varchar(50) DEFAULT NULL COMMENT '分类(发动机类/车架类/电气类/制动类/传动类/外观件)',
  `description` text COMMENT '零件描述',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_part_code` (`part_code`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_category` (`category`),
  CONSTRAINT `fk_part_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品/零部件表';

-- 4. 采购订单表 (purchase_order) —— 已并入物流信息
CREATE TABLE `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_number` varchar(50) NOT NULL COMMENT '订单编号',
  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态(1-采购中, 2-待入库审核, 3-已入库, 4-已取消)',
  `paid` tinyint DEFAULT '0' COMMENT '付款标志(0-未付款, 1-已付款)',
  `order_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `expected_delivery_date` date DEFAULT NULL COMMENT '预计交货日期(下单时与供应商约定)',
  `actual_delivery_date` date DEFAULT NULL COMMENT '实际到货日期(货到时确认)',
  -- 以下为并入的物流信息字段
  `logistics_company` varchar(100) DEFAULT NULL COMMENT '物流公司',
  `tracking_number` varchar(100) DEFAULT NULL COMMENT '运单号',
  `ship_time` datetime DEFAULT NULL COMMENT '供应商发货时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID(采购专员)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_number` (`order_number`),
  KEY `idx_status` (`status`),
  KEY `idx_order_time` (`order_time`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_tracking_number` (`tracking_number`),
  CONSTRAINT `fk_order_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单表(含物流信息)';

-- 5. 订单明细表 (order_detail)
CREATE TABLE `order_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `part_id` bigint NOT NULL COMMENT '零部件ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '采购数量',
  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '采购单价',
  `subtotal` decimal(12,2) GENERATED ALWAYS AS (`quantity` * `unit_price`) STORED COMMENT '小计金额(自动计算)',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_part_id` (`part_id`),
  UNIQUE KEY `uk_order_part` (`order_id`, `part_id`),
  CONSTRAINT `fk_detail_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_detail_part` FOREIGN KEY (`part_id`) REFERENCES `part` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- 6. 库存表 (inventory)
CREATE TABLE `inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `part_id` bigint NOT NULL COMMENT '零部件ID',
  `current_quantity` int NOT NULL DEFAULT '0' COMMENT '当前库存数量(仅由审核通过的出入库变更)',
  `safety_stock` int DEFAULT '10' COMMENT '安全库存量',
  `last_inbound_time` datetime DEFAULT NULL COMMENT '最近入库时间',
  `last_outbound_time` datetime DEFAULT NULL COMMENT '最近出库时间',
  `warehouse_location` varchar(100) DEFAULT 'A区-1号库' COMMENT '仓库位置',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_part_id` (`part_id`),
  KEY `idx_current_quantity` (`current_quantity`),
  CONSTRAINT `fk_inventory_part` FOREIGN KEY (`part_id`) REFERENCES `part` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表';

-- 7. 领用单表 (stock_out_order) —— 领用人员发起，管理员审核出库
CREATE TABLE `stock_out_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领用单ID',
  `stock_out_number` varchar(50) NOT NULL COMMENT '领用单号',
  `user_id` bigint NOT NULL COMMENT '领用人ID',
  `department` varchar(50) DEFAULT NULL COMMENT '领用部门',
  `purpose` varchar(50) DEFAULT '生产' COMMENT '用途(生产/维修/报废/其他)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1-待审核, 2-已出库, 3-已取消)',
  `stock_out_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领用时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_out_number` (`stock_out_number`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_stock_out_time` (`stock_out_time`),
  CONSTRAINT `fk_stockout_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领用单(出库单)';

-- 8. 领用明细表 (stock_out_detail)
CREATE TABLE `stock_out_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `stock_out_id` bigint NOT NULL COMMENT '领用单ID',
  `part_id` bigint NOT NULL COMMENT '零部件ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '领用数量',
  `unit_cost` decimal(10,2) DEFAULT NULL COMMENT '成本单价(出库估值用，可空)',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志(0-未删除, 1-已删除)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_out_id` (`stock_out_id`),
  KEY `idx_part_id` (`part_id`),
  UNIQUE KEY `uk_stockout_part` (`stock_out_id`, `part_id`),
  CONSTRAINT `fk_sod_order` FOREIGN KEY (`stock_out_id`) REFERENCES `stock_out_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sod_part` FOREIGN KEY (`part_id`) REFERENCES `part` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领用明细表';

-- ==================== 初始化数据（每表 3-5 条） ====================

-- 用户（密码为占位示例，明文 admin123，实际应存加密串）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `department`, `phone`, `email`, `status`) VALUES
('admin',      '$2a$10$YourEncryptedPasswordHere', '系统管理员',   'admin',       '管理部',   '13800138000', 'admin@motorparts.com',     1),
('purchase1',  '$2a$10$YourEncryptedPasswordHere', '采购员张三',   'purchase',    '采购部',   '13800138001', 'purchase1@motorparts.com', 1),
('purchase2',  '$2a$10$YourEncryptedPasswordHere', '采购员李四',   'purchase',    '采购部',   '13800138002', 'purchase2@motorparts.com', 1),
('worker1',    '$2a$10$YourEncryptedPasswordHere', '领用员王五',   'requisition', '生产车间', '13800138003', 'worker1@motorparts.com',   1),
('worker2',    '$2a$10$YourEncryptedPasswordHere', '领用员赵六',   'requisition', '生产车间', '13800138004', 'worker2@motorparts.com',   1);

-- 供应商
INSERT INTO `supplier` (`supplier_code`, `name`, `contact_person`, `phone`, `email`, `address`, `credit_rating`, `status`) VALUES
('SUP001', '重庆隆鑫发动机有限公司',   '陈经理', '023-68001001', 'sup001@longxin.com', '重庆市九龙坡区工业园1号',   'A', 1),
('SUP002', '浙江钱江摩托配件厂',       '林经理', '0577-86002002', 'sup002@qjmotor.com', '浙江省温州市瓯海区科技路18号', 'A', 1),
('SUP003', '广州大长江零部件公司',     '黄经理', '020-82003003', 'sup003@haojue.com',  '广东省广州市番禺区大道88号',  'B', 1),
('SUP004', '台州飞跃制动系统厂',       '王经理', '0576-88004004', 'sup004@feiyue.com',  '浙江省台州市路桥区机电路6号',  'B', 1);

-- 零部件
INSERT INTO `part` (`part_code`, `name`, `model`, `specification`, `unit`, `purchase_price`, `suggested_retail_price`, `stock_warning_value`, `supplier_id`, `category`, `description`) VALUES
('P001', '活塞',     'CG125',   '标准缸径 56.5mm', '个', 45.00,  78.00,  20, 1, '发动机类', '通用 125cc 发动机活塞'),
('P002', '火花塞',   'CR7HSA',  '镍合金电极',      '个', 8.00,   15.00,  30, 1, '电气类',   '通用摩托车火花塞'),
('P003', '刹车片',   'FA181',   '前轮陶瓷',        '套', 35.00,  60.00,  15, 4, '制动类',   '前轮刹车片一套'),
('P004', '传动链条', '428H-120','120 节加强型',    '条', 48.00,  85.00,  10, 3, '传动类',   '428 加强型传动链条'),
('P005', '大灯总成', 'LED-H4',  '12V 35W LED',     '套', 120.00, 210.00, 8,  2, '电气类',   'LED 前大灯总成');

-- 库存（每个零件一条；P002、P005 故意低于预警值以演示库存预警）
INSERT INTO `inventory` (`part_id`, `current_quantity`, `safety_stock`, `last_inbound_time`, `last_outbound_time`, `warehouse_location`) VALUES
(1, 50,  20, '2026-06-01 10:00:00', '2026-06-02 14:00:00', 'A区-1号库'),
(2, 8,   30, '2026-06-01 10:00:00', NULL,                  'A区-2号库'),
(3, 120, 15, '2026-06-01 10:00:00', '2026-06-02 14:00:00', 'B区-1号库'),
(4, 30,  10, NULL,                  NULL,                  'B区-2号库'),
(5, 5,   8,  NULL,                  NULL,                  'C区-1号库');

-- 采购订单（含物流信息；金额与下方明细一致）
INSERT INTO `purchase_order` (`order_number`, `total_amount`, `status`, `paid`, `order_time`, `expected_delivery_date`, `actual_delivery_date`, `logistics_company`, `tracking_number`, `ship_time`, `created_by`, `remark`) VALUES
('PO20260601001', 3200.00, 3, 1, '2026-06-01 09:30:00', '2026-06-05', '2026-06-04', '顺丰物流', 'SF1234567890', '2026-06-02 08:00:00', 2, '已入库并付款'),
('PO20260603001', 2250.00, 2, 0, '2026-06-03 11:00:00', '2026-06-08', NULL,         '德邦物流', 'DB9876543210', '2026-06-05 09:00:00', 2, '货已到，待入库审核'),
('PO20260605001', 3600.00, 1, 0, '2026-06-05 15:20:00', '2026-06-12', NULL,         NULL,       NULL,           NULL,                  3, '采购中，等待供应商发货'),
('PO20260520001', 600.00,  4, 0, '2026-05-20 10:10:00', '2026-05-25', NULL,         NULL,       NULL,           NULL,                  3, '供应商缺货，已取消');

-- 订单明细（subtotal 由数据库自动计算，不插入）
INSERT INTO `order_detail` (`order_id`, `part_id`, `quantity`, `unit_price`, `remark`) VALUES
(1, 1, 20,  120.00, '活塞采购'),
(1, 2, 100, 8.00,   '火花塞采购'),
(2, 3, 50,  45.00,  '刹车片采购'),
(3, 4, 30,  60.00,  '链条采购'),
(3, 5, 10,  180.00, '大灯采购'),
(4, 1, 5,   120.00, '已取消订单明细');

-- 领用单
INSERT INTO `stock_out_order` (`stock_out_number`, `user_id`, `department`, `purpose`, `status`, `stock_out_time`, `remark`) VALUES
('SO20260602001', 4, '生产车间', '生产', 2, '2026-06-02 14:00:00', '已审核出库'),
('SO20260604001', 5, '生产车间', '维修', 1, '2026-06-04 10:30:00', '待管理员审核'),
('SO20260606001', 4, '生产车间', '报废', 1, '2026-06-06 09:00:00', '待管理员审核');

-- 领用明细
INSERT INTO `stock_out_detail` (`stock_out_id`, `part_id`, `quantity`, `unit_cost`, `remark`) VALUES
(1, 1, 5,  45.00, '生产领用活塞'),
(1, 3, 10, 35.00, '生产领用刹车片'),
(2, 4, 3,  48.00, '维修领用链条'),
(3, 2, 2,  8.00,  '报废处理');

-- ==================== 视图定义 ====================

-- 库存预警视图
CREATE OR REPLACE VIEW `inventory_warning_view` AS
SELECT
    i.id,
    i.part_id,
    p.part_code,
    p.name AS part_name,
    i.current_quantity,
    i.safety_stock,
    p.stock_warning_value,
    i.warehouse_location,
    CASE
        WHEN i.current_quantity <= p.stock_warning_value THEN '严重不足'
        WHEN i.current_quantity <= i.safety_stock THEN '不足'
        ELSE '充足'
    END AS stock_status,
    i.last_inbound_time,
    i.last_outbound_time
FROM inventory i
JOIN part p ON i.part_id = p.id
WHERE i.deleted = 0 AND p.deleted = 0;

-- 采购订单统计视图
CREATE OR REPLACE VIEW `order_statistics_view` AS
SELECT
    DATE(o.order_time) AS order_date,
    COUNT(*) AS order_count,
    SUM(o.total_amount) AS total_amount,
    AVG(o.total_amount) AS avg_amount
FROM purchase_order o
WHERE o.deleted = 0
GROUP BY DATE(o.order_time);

-- 订单明细全信息视图（含零部件和供应商信息）
CREATE OR REPLACE VIEW `order_detail_full_view` AS
SELECT
    od.id AS detail_id,
    od.order_id,
    o.order_number,
    od.part_id,
    p.part_code,
    p.name AS part_name,
    p.model,
    p.specification,
    od.quantity,
    od.unit_price,
    od.subtotal,
    p.supplier_id,
    s.name AS supplier_name,
    s.supplier_code,
    od.create_time AS detail_create_time
FROM order_detail od
JOIN purchase_order o ON od.order_id = o.id
JOIN part p ON od.part_id = p.id
LEFT JOIN supplier s ON p.supplier_id = s.id
WHERE od.deleted = 0 AND o.deleted = 0 AND p.deleted = 0;

-- ==================== 表关系说明 ====================
/*
表之间的关系：
- user(创建人/采购专员) 1 -> N purchase_order(采购单)
- user(领用人)         1 -> N stock_out_order(领用单)
- supplier(供应商)     1 -> N part(零部件)
- part(零部件)         1 -> 1 inventory(库存)
- purchase_order(采购单) 1 -> N order_detail(订单明细) N -> 1 part(零部件)
- stock_out_order(领用单) 1 -> N stock_out_detail(领用明细) N -> 1 part(零部件)

核心规则：
- 库存(inventory.current_quantity)只能由【管理员审核通过】的入库/出库自动变更
- 采购单 status: 1-采购中 2-待入库审核 3-已入库 4-已取消；付款用独立字段 paid
- 领用单 status: 1-待审核 2-已出库 3-已取消
*/
