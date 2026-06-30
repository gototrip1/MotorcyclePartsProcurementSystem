package com.motorparts.init;

import com.motorparts.entity.*;
import com.motorparts.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库初始化器
 * 启动时自动创建表结构并插入模拟数据
 */
@Component("databaseInitializer")
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("${motorparts.init.enabled:true}")
    private boolean initEnabled;

    @Value("${motorparts.init.insert-sample-data:true}")
    private boolean insertSampleData;

    // 随机数生成器
    private final Random random = new Random();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 供应商数据
    private static final String[] SUPPLIER_NAMES = {
            "博世汽车配件（上海）有限公司", "电装（上海）国际贸易有限公司", "布雷博制动系统有限公司",
            "昭和자동차株弍会社上海代表处", "KYB（中国）投资有限公司", "日清纺油脂（上海）有限公司",
            "米其林（中国）投资有限公司", "倍耐力轮胎有限公司", "普利司通（上海）国际贸易有限公司",
            "固特异（大连）轮胎有限公司", "博格华纳汽车配件（宁波）有限公司", "采埃孚汽车系统（上海）有限公司",
            "大陆汽车投资（上海）有限公司", "麦格纳汽车技术（上海）有限公司", "法雷奥汽车内部控制（上海）有限公司",
            "德尔福汽车系统（中国）投资有限公司", "李尔（上海）汽车饰件有限公司", "江森自控（上海）管理有限公司",
            "天合汽车科技（上海）有限公司", "辉门汽车配件（上海）有限公司", "康明斯发动机（上海）国际贸易有限公司",
            "五十铃汽车工程塑料（上海）有限公司", "爱信精机（上海）贸易有限公司", "捷太格特汽车配件（上海）有限公司",
            "恩梯恩汽车配件（上海）有限公司", "捷豹路虎汽车备件贸易（上海）有限公司", "盖茨汽车配件（上海）有限公司",
            "豹牌刹车片有限公司", "马牌轮胎（上海）贸易有限公司", "优科豪马轮胎销售有限公司"
    };

    // 零件分类
    private static final String[] PART_CATEGORIES = {
            "发动机类", "车架类", "电气类", "制动类", "传动类", "外观件"
    };

    // 零件数据
    private static final String[][] PARTS_DATA = {
            // 发动机类
            {"PC2001", "活塞环组件", "标准型", "四冲程通用", "套", "发动机类"},
            {"PC2002", "发动机缸体", "CB400", "400cc", "个", "发动机类"},
            {"PC2003", "曲轴总成", "CG125", "125cc", "个", "发动机类"},
            {"PC2004", "凸轮轴", "GY6-125", "125cc", "个", "发动机类"},
            {"PC2005", "气门导管", "通用型", "6mm", "个", "发动机类"},
            {"PC2006", "连杆总成", "CB125T", "125T", "个", "发动机类"},
            {"PC2007", "机油泵", "GY6-125", "125cc", "个", "发动机类"},
            {"PC2008", "水泵总成", "CB400", "400cc", "个", "发动机类"},
            {"PC2009", "化油器总成", "CG125", "125cc", "套", "发动机类"},
            {"PC2010", "离合器片", "CB400", "400cc", "套", "发动机类"},
            {"PC2011", "启动电机", "GY6-125", "125cc", "个", "发动机类"},
            {"PC2012", "发电机总成", "CB125T", "125T", "个", "发动机类"},

            // 车架类
            {"FC3001", "前减震器", "钻豹", "KH100", "对", "车架类"},
            {"FC3002", "后减震器", "五羊款", "125通用", "个", "车架类"},
            {"FC3003", "方向把", "铝合金", "22mm", "个", "车架类"},
            {"FC3004", "方向柱", "CG125", "125cc", "套", "车架类"},
            {"FC3005", "主支架", "五羊款", "125通用", "个", "车架类"},
            {"FC3006", "侧支架", "CG125", "125通用", "个", "车架类"},
            {"FC3007", "链轮", "42齿", "428规格", "个", "车架类"},
            {"FC3008", "后平叉", "钻豹", "KH100", "个", "车架类"},
            {"FC3009", "前叉总成", "太子款", "125通用", "对", "车架类"},
            {"FC3010", "车把开关", "CG125", "通用", "套", "车架类"},

            // 电气类
            {"EC4001", "磁电机总成", "CG125", "125cc", "套", "电气类"},
            {"EC4002", "点火线圈", "GY6-125", "125cc", "个", "电气类"},
            {"EC4003", "火花塞", "NGK", "C7HSA", "个", "电气类"},
            {"EC4004", "整流器", "12V", "5A", "个", "电气类"},
            {"EC4005", "大灯总成", "钻豹", "35W", "套", "电气类"},
            {"EC4006", "尾灯总成", "五羊款", "LED", "套", "电气类"},
            {"EC4007", "转向灯", "通用", "10W", "对", "电气类"},
            {"EC4008", "仪表总成", "CG125", "机械式", "套", "电气类"},
            {"EC4009", "喇叭", "12V", "双音", "个", "电气类"},
            {"EC4010", "闪光器", "12V", "3针", "个", "电气类"},
            {"EC4011", "手把开关", "CG125", "左右套", "套", "电气类"},
            {"EC4012", "熄火开关", "通用", "12V", "个", "电气类"},

            // 制动类
            {"BC5001", "前刹车片", "布雷博", "GSX250", "对", "制动类"},
            {"BC5002", "后刹车片", "BREMBO", "CB400", "对", "制动类"},
            {"BC5003", "刹车蹄块", "通用", "130规格", "套", "制动类"},
            {"BC5004", "刹车泵", "日清", "直推式", "个", "制动类"},
            {"BC5005", "离合器手柄", "铝合金", "通用", "个", "制动类"},
            {"BC5006", "刹车手柄", "铝合金", "22mm", "个", "制动类"},
            {"BC5007", "刹车线", "CG125", "通用", "根", "制动类"},
            {"BC5008", "离合线", "CG125", "通用", "根", "制动类"},
            {"BC5009", "刹车总泵", "日信", "14mm", "个", "制动类"},
            {"BC5010", "刹车分泵", "日清", "14mm", "个", "制动类"},

            // 传动类
            {"TC6001", "传动皮带", "盖茨", "AV13x950", "根", "传动类"},
            {"TC6002", "普利珠", "12g", "8x14", "套", "传动类"},
            {"TC6003", "碗公", "GY6-125", "125cc", "个", "传动类"},
            {"TC6004", "离合器", "GY6-125", "125cc", "套", "传动类"},
            {"TC6005", "主动轮", "GY6-125", "125cc", "个", "传动类"},
            {"TC6006", "从动轮", "GY6-125", "125cc", "个", "传动类"},
            {"TC6007", "链条", "428", "120节", "根", "传动类"},
            {"TC6008", "链条接头", "428", "通用", "个", "传动类"},
            {"TC6009", "油门线", "通用", "1500mm", "根", "传动类"},
            {"TC6010", "油门把手", "通用", "22mm", "个", "传动类"},

            // 外观件
            {"AC7001", "前挡泥板", "五羊款", "125通用", "个", "外观件"},
            {"AC7002", "后挡泥板", "钻豹", "KH100", "个", "外观件"},
            {"AC7003", "导流罩", "CB400", "400cc", "套", "外观件"},
            {"AC7004", "后视镜", "铝合金", "通用", "对", "外观件"},
            {"AC7005", "座垫", "PU", "125通用", "个", "外观件"},
            {"AC7006", "油箱盖", "CG125", "金属", "个", "外观件"},
            {"AC7007", "侧盖板", "五羊款", "125通用", "对", "外观件"},
            {"AC7008", "脚蹬", "铝合金", "防滑", "对", "外观件"},
            {"AC7009", "脚踏板", "塑料", "通用", "个", "外观件"},
            {"AC7010", "牌照灯", "LED", "12V", "个", "外观件"}
    };

    // 物流公司
    private static final String[] LOGISTICS_COMPANIES = {
            "顺丰速运", "中通快递", "圆通速递", "申通快递", "韵达快递",
            "德邦物流", "安能物流", "天地华宇", "佳吉快运", "百世快运"
    };

    // 仓库位置
    private static final String[] WAREHOUSE_LOCATIONS = {
            "A区-1号库", "A区-2号库", "B区-1号库", "B区-2号库", "C区-1号库"
    };

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!initEnabled) {
            log.info("数据初始化已禁用，跳过...");
            return;
        }

        log.info("========== 开始数据库初始化 ==========");

        try {
            // 创建数据库（如果不存在）
            createDatabase();

            // 创建表结构
            createTables();

            // 插入模拟数据
            if (insertSampleData) {
                insertSampleData();
            }

            log.info("========== 数据库初始化完成 ==========");
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建数据库
     */
    private void createDatabase() {
        try {
            // 创建数据库
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS motorparts CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci");
            log.info("数据库 motorparts 创建成功或已存在");

            // 切换到新创建的数据库
            jdbcTemplate.execute("USE motorparts");
            log.info("已切换到数据库 motorparts");
        } catch (Exception e) {
            log.warn("创建数据库时出错（可能已存在）: {}", e.getMessage());
        }
    }

    /**
     * 创建所有表
     */
    private void createTables() {
        // 用户表（先创建，因为其他表可能依赖它）
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `user` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID'," +
                "  `username` varchar(50) NOT NULL COMMENT '用户名'," +
                "  `password` varchar(100) NOT NULL COMMENT '密码(加密)'," +
                "  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名'," +
                "  `role` varchar(50) DEFAULT 'purchase' COMMENT '角色'," +
                "  `department` varchar(50) DEFAULT '采购部' COMMENT '部门'," +
                "  `phone` varchar(20) DEFAULT NULL COMMENT '电话'," +
                "  `email` varchar(100) DEFAULT NULL COMMENT '邮箱'," +
                "  `status` tinyint DEFAULT '1' COMMENT '状态'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `uk_username` (`username`)," +
                "  KEY `idx_role` (`role`)," +
                "  KEY `idx_status` (`status`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户/员工表'");

        // 供应商表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `supplier` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID'," +
                "  `supplier_code` varchar(50) NOT NULL COMMENT '供应商编码'," +
                "  `name` varchar(100) NOT NULL COMMENT '供应商名称'," +
                "  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人'," +
                "  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话'," +
                "  `email` varchar(100) DEFAULT NULL COMMENT '邮箱'," +
                "  `address` varchar(200) DEFAULT NULL COMMENT '地址'," +
                "  `credit_rating` char(1) DEFAULT 'B' COMMENT '信用评级'," +
                "  `status` tinyint DEFAULT '1' COMMENT '合作状态'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `uk_supplier_code` (`supplier_code`)," +
                "  KEY `idx_status` (`status`)," +
                "  KEY `idx_credit_rating` (`credit_rating`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表'");

        // 产品表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `part` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '零部件ID'," +
                "  `part_code` varchar(50) NOT NULL COMMENT '零件编码'," +
                "  `name` varchar(100) NOT NULL COMMENT '零件名称'," +
                "  `model` varchar(100) DEFAULT NULL COMMENT '型号'," +
                "  `specification` varchar(200) DEFAULT NULL COMMENT '规格'," +
                "  `unit` varchar(20) DEFAULT '个' COMMENT '单位'," +
                "  `purchase_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '采购单价'," +
                "  `suggested_retail_price` decimal(10,2) DEFAULT '0.00' COMMENT '建议零售价'," +
                "  `stock_warning_value` int DEFAULT '10' COMMENT '库存预警值'," +
                "  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID'," +
                "  `category` varchar(50) DEFAULT NULL COMMENT '分类'," +
                "  `description` text COMMENT '零件描述'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `uk_part_code` (`part_code`)," +
                "  KEY `idx_supplier_id` (`supplier_id`)," +
                "  KEY `idx_category` (`category`)," +
                "  CONSTRAINT `fk_part_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE SET NULL ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品/零部件表'");

        // 采购订单表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `purchase_order` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID'," +
                "  `order_number` varchar(50) NOT NULL COMMENT '订单编号'," +
                "  `total_amount` decimal(12,2) DEFAULT '0.00' COMMENT '订单总金额'," +
                "  `status` tinyint DEFAULT '1' COMMENT '订单状态(1-采购中,2-待入库审核,3-已入库,4-已取消)'," +
                "  `paid` tinyint DEFAULT '0' COMMENT '付款状态(0-未付款,1-已付款)'," +
                "  `order_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间'," +
                "  `expected_delivery_date` date DEFAULT NULL COMMENT '预计交货日期'," +
                "  `actual_delivery_date` date DEFAULT NULL COMMENT '实际交货日期'," +
                "  `logistics_company` varchar(100) DEFAULT NULL COMMENT '物流公司'," +
                "  `tracking_number` varchar(100) DEFAULT NULL COMMENT '运单号'," +
                "  `ship_time` datetime DEFAULT NULL COMMENT '发货时间'," +
                "  `created_by` bigint DEFAULT NULL COMMENT '创建人ID'," +
                "  `remark` varchar(500) DEFAULT NULL COMMENT '备注'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `uk_order_number` (`order_number`)," +
                "  KEY `idx_status` (`status`)," +
                "  KEY `idx_order_time` (`order_time`)," +
                "  KEY `idx_created_by` (`created_by`)," +
                "  KEY `idx_tracking_number` (`tracking_number`)," +
                "  CONSTRAINT `fk_order_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单表'");

        // 订单明细表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `order_detail` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID'," +
                "  `order_id` bigint NOT NULL COMMENT '订单ID'," +
                "  `part_id` bigint NOT NULL COMMENT '零部件ID'," +
                "  `quantity` int NOT NULL DEFAULT '1' COMMENT '采购数量'," +
                "  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '采购单价'," +
                "  `subtotal` decimal(12,2) GENERATED ALWAYS AS (quantity * unit_price) STORED COMMENT '小计金额'," +
                "  `remark` varchar(200) DEFAULT NULL COMMENT '备注'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  KEY `idx_order_id` (`order_id`)," +
                "  KEY `idx_part_id` (`part_id`)," +
                "  UNIQUE KEY `uk_order_part` (`order_id`, `part_id`)," +
                "  CONSTRAINT `fk_detail_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "  CONSTRAINT `fk_detail_part` FOREIGN KEY (`part_id`) REFERENCES `part` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表'");

        // 库存表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `inventory` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存ID'," +
                "  `part_id` bigint NOT NULL COMMENT '零部件ID'," +
                "  `current_quantity` int NOT NULL DEFAULT '0' COMMENT '当前库存数量'," +
                "  `safety_stock` int DEFAULT '10' COMMENT '安全库存量'," +
                "  `last_inbound_time` datetime DEFAULT NULL COMMENT '最近入库时间'," +
                "  `last_outbound_time` datetime DEFAULT NULL COMMENT '最近出库时间'," +
                "  `warehouse_location` varchar(100) DEFAULT 'A区-1号库' COMMENT '仓库位置'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `uk_part_id` (`part_id`)," +
                "  KEY `idx_current_quantity` (`current_quantity`)," +
                "  CONSTRAINT `fk_inventory_part` FOREIGN KEY (`part_id`) REFERENCES `part` (`id`) ON DELETE CASCADE ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存表'");

        // 领用单表（领用人员发起，管理员审核出库）
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `stock_out_order` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '领用单ID'," +
                "  `stock_out_number` varchar(50) NOT NULL COMMENT '领用单号'," +
                "  `user_id` bigint NOT NULL COMMENT '领用人ID'," +
                "  `department` varchar(50) DEFAULT NULL COMMENT '领用部门'," +
                "  `purpose` varchar(50) DEFAULT '生产' COMMENT '用途(生产/维修/报废/其他)'," +
                "  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1-待审核, 2-已出库, 3-已取消)'," +
                "  `stock_out_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领用时间'," +
                "  `remark` varchar(500) DEFAULT NULL COMMENT '备注'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  UNIQUE KEY `uk_stock_out_number` (`stock_out_number`)," +
                "  KEY `idx_user_id` (`user_id`)," +
                "  KEY `idx_status` (`status`)," +
                "  KEY `idx_stock_out_time` (`stock_out_time`)," +
                "  CONSTRAINT `fk_stockout_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领用单(出库单)'");

        // 领用明细表
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `stock_out_detail` (" +
                "  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID'," +
                "  `stock_out_id` bigint NOT NULL COMMENT '领用单ID'," +
                "  `part_id` bigint NOT NULL COMMENT '零部件ID'," +
                "  `quantity` int NOT NULL DEFAULT '1' COMMENT '领用数量'," +
                "  `unit_cost` decimal(10,2) DEFAULT NULL COMMENT '成本单价(出库估值用，可空)'," +
                "  `remark` varchar(200) DEFAULT NULL COMMENT '备注'," +
                "  `deleted` tinyint DEFAULT '0' COMMENT '删除标志'," +
                "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "  PRIMARY KEY (`id`)," +
                "  KEY `idx_stock_out_id` (`stock_out_id`)," +
                "  KEY `idx_part_id` (`part_id`)," +
                "  UNIQUE KEY `uk_stockout_part` (`stock_out_id`, `part_id`)," +
                "  CONSTRAINT `fk_sod_order` FOREIGN KEY (`stock_out_id`) REFERENCES `stock_out_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
                "  CONSTRAINT `fk_sod_part` FOREIGN KEY (`part_id`) REFERENCES `part` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='领用明细表'");

        log.info("所有数据表创建成功");
    }

    /**
     * 插入模拟数据
     */
    @Transactional(rollbackFor = Exception.class)
    protected void insertSampleData() {
        // 检查是否已有数据
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
        if (userCount != null && userCount > 0) {
            log.info("数据库中已有数据，跳过数据插入");
            return;
        }

        log.info("开始插入模拟数据...");

        // 插入用户数据
        insertUsers();
        log.info("用户数据插入完成");

        // 插入供应商数据
        insertSuppliers();
        log.info("供应商数据插入完成");

        // 插入产品数据
        insertParts();
        log.info("产品数据插入完成");

        // 插入库存数据
        insertInventories();
        log.info("库存数据插入完成");

        // 插入采购订单和明细（含物流信息）
        insertPurchaseOrders();
        log.info("采购订单数据插入完成");

        // 插入领用单和明细
        insertStockOuts();
        log.info("领用单数据插入完成");

        log.info("所有模拟数据插入完成");
    }

    /**
     * 插入用户数据
     */
    private void insertUsers() {
        // 仅 3 种角色：admin-管理员 / purchase-采购专员 / requisition-领用人员
        String[] roles = {"admin", "purchase", "requisition"};
        String[] departments = {"管理部", "采购部", "生产车间"};
        String[] names = {"系统管理员", "张三", "李四", "王五", "赵六", "钱七", "孙八", "周九"};
        String[] emails = {"admin@motorparts.com", "zhangsan@motorparts.com", "lisi@motorparts.com",
                "wangwu@motorparts.com", "zhaoliu@motorparts.com", "qianqi@motorparts.com",
                "sunba@motorparts.com", "zhoujiu@motorparts.com"};

        for (int i = 0; i < 12; i++) {
            String role = roles[i % roles.length];
            String dept = departments[i % departments.length];
            String name = names[i % names.length] + (i >= names.length ? (i / names.length + 1) : "");
            String email = emails[i % emails.length];

            jdbcTemplate.update(
                    "INSERT INTO user (username, password, real_name, role, department, phone, email, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    "user" + (i + 1), "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW",
                    name, role, dept, "138" + String.format("%08d", random.nextInt(100000000)),
                    email, 1
            );
        }
    }

    /**
     * 插入供应商数据
     */
    private void insertSuppliers() {
        String[] creditRatings = {"A", "B", "C", "D"};
        int[] statuses = {1, 1, 1, 2, 3}; // 权重：合作中的供应商更多

        for (int i = 0; i < SUPPLIER_NAMES.length; i++) {
            String code = "SUP" + String.format("%05d", i + 1);
            String name = SUPPLIER_NAMES[i];
            String contact = "联系人" + (char) ('A' + random.nextInt(26));
            String phone = "138" + String.format("%08d", random.nextInt(100000000));
            String email = "supplier" + (i + 1) + "@motorparts.com";
            String address = "上海市嘉定区安亭镇汽车城某路" + (i + 1) + "号";
            String creditRating = creditRatings[random.nextInt(creditRatings.length)];
            int status = statuses[random.nextInt(statuses.length)];

            jdbcTemplate.update(
                    "INSERT INTO supplier (supplier_code, name, contact_person, phone, email, address, credit_rating, status, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    code, name, contact, phone, email, address, creditRating, status,
                    getRandomDateTime2024()
            );
        }
    }

    /**
     * 插入产品数据
     */
    private void insertParts() {
        // 获取所有供应商ID
        List<Long> supplierIds = jdbcTemplate.queryForList("SELECT id FROM supplier", Long.class);

        for (String[] partData : PARTS_DATA) {
            String code = partData[0];
            String name = partData[1];
            String model = partData[2];
            String spec = partData[3];
            String unit = partData[4];
            String category = partData[5];

            // 生成价格（普通零件10-500元，核心部件500-5000元）
            BigDecimal purchasePrice;
            if (category.equals("发动机类") || category.equals("传动类")) {
                purchasePrice = BigDecimal.valueOf(500 + random.nextInt(4500)).setScale(2, RoundingMode.HALF_UP);
            } else {
                purchasePrice = BigDecimal.valueOf(10 + random.nextInt(490)).setScale(2, RoundingMode.HALF_UP);
            }

            // 建议零售价通常是采购价的1.2-1.5倍
            BigDecimal retailPrice = purchasePrice.multiply(BigDecimal.valueOf(1.2 + random.nextDouble() * 0.3))
                    .setScale(2, RoundingMode.HALF_UP);

            Long supplierId = supplierIds.get(random.nextInt(supplierIds.size()));
            int stockWarning = 10 + random.nextInt(20);

            jdbcTemplate.update(
                    "INSERT INTO part (part_code, name, model, specification, unit, purchase_price, suggested_retail_price, stock_warning_value, supplier_id, category, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    code, name, model, spec, unit, purchasePrice, retailPrice, stockWarning, supplierId, category,
                    getRandomDateTime2024()
            );
        }
    }

    /**
     * 插入库存数据
     */
    private void insertInventories() {
        List<Map<String, Object>> parts = jdbcTemplate.queryForList("SELECT id, stock_warning_value, category FROM part");

        // 按分类分配仓库位置，确保相同类别的零部件在同一区域
        Map<String, String> categoryLocations = new HashMap<>();
        for (String category : PART_CATEGORIES) {
            categoryLocations.put(category, WAREHOUSE_LOCATIONS[random.nextInt(WAREHOUSE_LOCATIONS.length)]);
        }

        for (Map<String, Object> part : parts) {
            Long partId = (Long) part.get("id");
            int warningValue = (Integer) part.get("stock_warning_value");
            String category = (String) part.get("category");

            // 库存数量：有些低于预警值（预警），有些正常
            int quantity;
            if (random.nextInt(10) < 3) { // 30%的概率库存不足
                quantity = random.nextInt(warningValue);
            } else {
                quantity = warningValue + random.nextInt(100);
            }

            // 根据分类获取固定的仓库位置，确保相同类别的零部件在同一位置
            String location = categoryLocations.getOrDefault(category, WAREHOUSE_LOCATIONS[0]);

            jdbcTemplate.update(
                    "INSERT INTO inventory (part_id, current_quantity, safety_stock, warehouse_location, last_inbound_time, create_time) VALUES (?, ?, ?, ?, ?, ?)",
                    partId, quantity, warningValue, location, getRandomDateTime2024(), getRandomDateTime2024()
            );
        }
    }

    /**
     * 插入采购订单数据
     */
    private void insertPurchaseOrders() {
        // 获取所有用户ID
        List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM user", Long.class);
        // 获取所有零部件（每个零部件已经关联了供应商）
        List<Map<String, Object>> allParts = jdbcTemplate.queryForList("SELECT id, purchase_price FROM part");

        // 插入120个订单
        for (int i = 0; i < 120; i++) {
            String orderNumber = "PO" + LocalDate.now().getYear() + String.format("%06d", i + 1);
            Long createdBy = userIds.get(random.nextInt(userIds.size()));

            // 订单状态：1-采购中、2-待入库审核、3-已入库、4-已取消
            int[] orderStatuses = {1, 2, 3, 4};
            int[] statusWeights = {30, 25, 40, 5}; // 权重
            int status = getWeightedRandom(orderStatuses, statusWeights);

            LocalDateTime orderTime = getRandomDateTime2024();
            LocalDate expectedDate = orderTime.toLocalDate().plusDays(7 + random.nextInt(14));
            // 已入库(3)才有实际到货日期
            LocalDate actualDate = status == 3 ? expectedDate.plusDays(random.nextInt(7) - 3) : null;
            // 已入库(3)默认已付款，其余未付款
            int paid = status == 3 ? 1 : 0;

            // 物流信息：待入库审核(2)或已入库(3)的单据已发货，物流字段并入采购单
            String logisticsCompany = null;
            String trackingNumber = null;
            LocalDateTime shipTime = null;
            if (status == 2 || status == 3) {
                logisticsCompany = LOGISTICS_COMPANIES[random.nextInt(LOGISTICS_COMPANIES.length)];
                trackingNumber = (random.nextBoolean() ? "SF" : "YT") + random.nextInt(100000000);
                shipTime = orderTime.plusDays(1 + random.nextInt(3));
            }

            // 生成订单金额
            int itemCount = 2 + random.nextInt(6);
            BigDecimal totalAmount = BigDecimal.ZERO;

            // 先插入订单，获取ID
            jdbcTemplate.update(
                    "INSERT INTO purchase_order (order_number, total_amount, status, paid, order_time, expected_delivery_date, actual_delivery_date, logistics_company, tracking_number, ship_time, created_by, create_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    orderNumber, 0, status, paid, orderTime, expectedDate, actualDate,
                    logisticsCompany, trackingNumber, shipTime, createdBy, orderTime
            );

            // 获取插入的订单ID
            Long orderId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

            // 插入订单明细（订单可以包含来自不同供应商的零部件）
            Set<Long> usedPartIds = new HashSet<>();
            for (int j = 0; j < itemCount && usedPartIds.size() < allParts.size(); j++) {
                Map<String, Object> part = allParts.get(random.nextInt(allParts.size()));
                Long partId = (Long) part.get("id");
                if (usedPartIds.contains(partId)) continue;
                usedPartIds.add(partId);

                // 获取零件采购价
                BigDecimal unitPrice = (BigDecimal) part.get("purchase_price");
                if (unitPrice == null) unitPrice = BigDecimal.valueOf(100);

                int quantity = 5 + random.nextInt(20);
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                totalAmount = totalAmount.add(subtotal);

                jdbcTemplate.update(
                        "INSERT INTO order_detail (order_id, part_id, quantity, unit_price, create_time) VALUES (?, ?, ?, ?, ?)",
                        orderId, partId, quantity, unitPrice, orderTime
                );
            }

            // 更新订单总金额
            jdbcTemplate.update("UPDATE purchase_order SET total_amount = ? WHERE id = ?", totalAmount, orderId);
        }
    }

    /**
     * 插入领用单（出库单）与明细数据
     */
    private void insertStockOuts() {
        // 领用人只能是 requisition 角色；若没有则退化为全部用户
        List<Long> userIds = jdbcTemplate.queryForList(
                "SELECT id FROM user WHERE role = 'requisition'", Long.class);
        if (userIds.isEmpty()) {
            userIds = jdbcTemplate.queryForList("SELECT id FROM user", Long.class);
        }
        List<Map<String, Object>> allParts = jdbcTemplate.queryForList("SELECT id, purchase_price FROM part");
        if (userIds.isEmpty() || allParts.isEmpty()) {
            return;
        }

        String[] departments = {"生产车间", "维修部", "总装线", "质检部"};
        String[] purposes = {"生产", "维修", "报废", "其他"};
        // 状态：1-待审核、2-已出库、3-已取消
        int[] statuses = {1, 2, 3};
        int[] statusWeights = {40, 50, 10};

        for (int i = 0; i < 30; i++) {
            String stockOutNumber = "SO" + LocalDate.now().getYear() + String.format("%06d", i + 1);
            Long userId = userIds.get(random.nextInt(userIds.size()));
            String department = departments[random.nextInt(departments.length)];
            String purpose = purposes[random.nextInt(purposes.length)];
            int status = getWeightedRandom(statuses, statusWeights);
            LocalDateTime stockOutTime = getRandomDateTime2024();

            jdbcTemplate.update(
                    "INSERT INTO stock_out_order (stock_out_number, user_id, department, purpose, status, stock_out_time, create_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    stockOutNumber, userId, department, purpose, status, stockOutTime, stockOutTime
            );
            Long stockOutId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

            // 明细：同一单内零件不重复（受唯一索引约束）
            int itemCount = 1 + random.nextInt(3);
            Set<Long> usedPartIds = new HashSet<>();
            for (int j = 0; j < itemCount && usedPartIds.size() < allParts.size(); j++) {
                Map<String, Object> part = allParts.get(random.nextInt(allParts.size()));
                Long partId = (Long) part.get("id");
                if (usedPartIds.contains(partId)) continue;
                usedPartIds.add(partId);

                BigDecimal unitCost = (BigDecimal) part.get("purchase_price");
                int quantity = 1 + random.nextInt(5);

                jdbcTemplate.update(
                        "INSERT INTO stock_out_detail (stock_out_id, part_id, quantity, unit_cost, create_time) VALUES (?, ?, ?, ?, ?)",
                        stockOutId, partId, quantity, unitCost, stockOutTime
                );
            }
        }
    }

    /**
     * 生成2024年的随机日期时间
     */
    private LocalDateTime getRandomDateTime2024() {
        int year = 2024 + random.nextInt(2); // 2024-2025
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(28) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    /**
     * 加权随机选择
     */
    private int getWeightedRandom(int[] values, int[] weights) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;

        for (int i = 0; i < values.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue < cumulativeWeight) {
                return values[i];
            }
        }

        return values[values.length - 1];
    }
}