/*
 Navicat Premium Dump SQL

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : noperson

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 30/10/2025 16:12:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '轮播图ID（主键）',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮播图标题',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮播图图片URL',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户类型：farmer(农户) 或 flyer(飞手)',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for demand
-- ----------------------------
DROP TABLE IF EXISTS `demand`;
CREATE TABLE `demand`  (
  `demand_id` bigint NOT NULL AUTO_INCREMENT COMMENT '需求ID（主键）',
  `farmer_id` bigint NOT NULL COMMENT '农户ID（关联sys_user.user_id）',
  `flyer_id` bigint NULL DEFAULT NULL COMMENT '飞手ID（接单后赋值，关联sys_user.user_id）',
  `report_id` bigint NULL DEFAULT NULL COMMENT '关联巡检报告ID',
  `device_id` bigint NULL DEFAULT NULL COMMENT '关联设备ID',
  `land_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地块名称',
  `land_boundary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地块边界（经纬度）',
  `crop_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作物类型',
  `pest_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '病虫害类型',
  `land_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地块位置',
  `land_area` bigint NULL DEFAULT NULL COMMENT '地块面积（亩）',
  `expected_time` datetime NULL DEFAULT NULL COMMENT '期望作业时间',
  `budget` decimal(10, 2) NULL DEFAULT NULL COMMENT '预算金额',
  `status` int NOT NULL COMMENT '状态（0-待接取，1-处理中，2-作业中，3-待确认，4-已完成，5-已取消）',
  `accept_time` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始作业时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成作业时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `order_type` int NULL DEFAULT NULL COMMENT '1是喷洒，2是巡检',
  `inspection_purpose` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '巡检目的（只有巡检才会有）',
  `expected_resolution` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '期待分辨率（只有巡检才会有）',
  `land_id` bigint NULL DEFAULT NULL COMMENT '地块ID(只有喷洒有)',
  `payment_amount` double NULL DEFAULT NULL COMMENT '实际支付金额',
  `payment_status` int NULL DEFAULT NULL COMMENT '支付状态：0-未支付，1-已支付，2-支付失败',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式：balance-余额支付',
  `payment_record_id` bigint NULL DEFAULT NULL COMMENT '支付记录ID',
  PRIMARY KEY (`demand_id`) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id` ASC) USING BTREE,
  INDEX `idx_flyer_id`(`flyer_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_payment_status`(`payment_status` ASC) USING BTREE,
  INDEX `idx_payment_time`(`payment_time` ASC) USING BTREE,
  INDEX `idx_payment_method`(`payment_method` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '喷洒需求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for device_activity
-- ----------------------------
DROP TABLE IF EXISTS `device_activity`;
CREATE TABLE `device_activity`  (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '动态ID（主键）',
  `device_id` bigint NOT NULL COMMENT '设备ID（关联drone_device.device_id）',
  `activity_type` int NOT NULL COMMENT '动态类型：1-设备上线 2-设备下线 3-设备维护 4-设备归还 5-设备租借 6-状态变更',
  `activity_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动态描述',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（关联sys_user.user_id）',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `before_status` int NULL DEFAULT NULL COMMENT '变更前状态',
  `after_status` int NULL DEFAULT NULL COMMENT '变更后状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`activity_id`) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_activity_type`(`activity_type` ASC) USING BTREE,
  CONSTRAINT `fk_activity_device` FOREIGN KEY (`device_id`) REFERENCES `drone_device` (`device_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备动态记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for device_maintain_record
-- ----------------------------
DROP TABLE IF EXISTS `device_maintain_record`;
CREATE TABLE `device_maintain_record`  (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '保养记录ID（主键）',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（飞手/管理员，关联sys_user.user_id）',
  `maintain_type` int NULL DEFAULT NULL COMMENT '维护类型（1：常规保养 2：故障维修）',
  `fault_description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '故障描述（维修时必填）',
  `maintain_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维护内容',
  `cost` decimal(10, 2) NULL DEFAULT NULL COMMENT '维护费用',
  `replace_parts` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更换配件（逗号分隔）',
  `maintain_time` datetime NULL DEFAULT NULL COMMENT '维护时间',
  `status` int NULL DEFAULT 0 COMMENT '状态（0：待审核 1：已确认）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备保养记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for device_rental
-- ----------------------------
DROP TABLE IF EXISTS `device_rental`;
CREATE TABLE `device_rental`  (
  `rental_id` bigint NOT NULL AUTO_INCREMENT COMMENT '租借记录ID（主键）',
  `device_id` bigint NOT NULL COMMENT '设备ID（关联drone_device.device_id）',
  `owner_id` bigint NOT NULL COMMENT '机主ID（关联user_owner.id）',
  `flyer_id` bigint NOT NULL COMMENT '飞手ID（关联sys_user.user_id）',
  `rental_start_time` datetime NOT NULL COMMENT '租借开始时间',
  `rental_end_time` datetime NULL DEFAULT NULL COMMENT '租借结束时间',
  `rental_status` int NOT NULL DEFAULT 1 COMMENT '租借状态：1-租借中 2-已归还 3-已取消',
  `rental_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '租借费用',
  `payment_status` int NULL DEFAULT 0 COMMENT '支付状态：0-未支付 1-已支付 2-支付失败',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rental_id`) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_flyer_id`(`flyer_id` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_rental_status`(`rental_status` ASC) USING BTREE,
  CONSTRAINT `fk_rental_device` FOREIGN KEY (`device_id`) REFERENCES `drone_device` (`device_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备租借记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for drone_device
-- ----------------------------
DROP TABLE IF EXISTS `drone_device`;
CREATE TABLE `drone_device`  (
  `device_id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID（主键）',
  `owner_id` bigint NOT NULL COMMENT '机主ID（关联user_owner.user_id）',
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备型号',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `device_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编号（唯一标识）',
  `purchase_time` date NULL DEFAULT NULL COMMENT '购买时间',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：0-停用 1-正常 2-维修中 3-已报废',
  `max_load` decimal(8, 2) NULL DEFAULT NULL COMMENT '最大载重（kg）',
  `endurance` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '续航时间（分钟）',
  `flyer_id` bigint NULL DEFAULT NULL COMMENT '当前绑定飞手ID（关联sys_user.user_id）',
  `last_maintain_time` datetime NULL DEFAULT NULL COMMENT '最后保养时间',
  `working_hours` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '累计工作时长（小时）',
  `insurance_expire_time` date NULL DEFAULT NULL COMMENT '保险到期时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `manufacturer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备制造商',
  `picture` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备图片',
  `rental_status` int NOT NULL DEFAULT 0 COMMENT '租借状态：0-可租借 1-已租借 2-审核中',
  PRIMARY KEY (`device_id`) USING BTREE,
  UNIQUE INDEX `uk_device_no`(`device_no` ASC) USING BTREE,
  INDEX `idx_owner_id`(`owner_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_current_flyer_id`(`flyer_id` ASC) USING BTREE,
  INDEX `idx_rental_status`(`rental_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '无人机设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for evaluation_record
-- ----------------------------
DROP TABLE IF EXISTS `evaluation_record`;
CREATE TABLE `evaluation_record`  (
  `evaluation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID（主键）',
  `order_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单类型',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `evaluator_id` bigint NULL DEFAULT NULL COMMENT '评价人ID（关联sys_user.user_id）',
  `evaluatee_id` bigint NULL DEFAULT NULL COMMENT '被评价人ID（关联sys_user.user_id）',
  `score` int NULL DEFAULT NULL COMMENT '评分（1-5星）',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价内容',
  `evaluatee_role` int NULL DEFAULT NULL COMMENT '被评价人角色（1-飞手 2-农户）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`evaluation_id`) USING BTREE,
  INDEX `idx_evaluatee_id`(`evaluatee_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评价记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID（主键）',
  `conversation_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话ID（用于标识唯一的对话）',
  `sender_id` bigint NOT NULL COMMENT '发送者ID（关联sys_user.user_id）',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID（关联sys_user.user_id）',
  `message_type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本消息 2-图片 3-订单相关',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `related_order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `notification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID（主键）',
  `user_id` bigint NOT NULL COMMENT '接收用户ID（关联sys_user.user_id）',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `notify_type` int NOT NULL COMMENT '通知类型：1-系统通知 2-订单通知 3-审核通知 4-评价通知',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID（如订单ID、评价ID等）',
  `is_read` int NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间（null表示永久有效）',
  `is_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_notify_type`(`notify_type` ASC) USING BTREE,
  INDEX `idx_related_id`(`related_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record`  (
  `payment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID（主键）',
  `trade_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易单号',
  `order_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单类型（INSPECTION-巡检 SPRAY-喷洒）',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单/需求ID',
  `payer_id` bigint NULL DEFAULT NULL COMMENT '支付人ID（农户，关联sys_user.user_id）',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式（WECHAT-微信 ALIPAY-支付宝）',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方交易号',
  `status` int NOT NULL COMMENT '状态（0-待支付 1-已支付 2-支付失败）',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`payment_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_payer_id`(`payer_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for settlement_record
-- ----------------------------
DROP TABLE IF EXISTS `settlement_record`;
CREATE TABLE `settlement_record`  (
  `settlement_id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算单ID（主键）',
  `flyer_id` bigint NOT NULL COMMENT '飞手ID（关联sys_user.user_id）',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `order_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单类型',
  `order_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `platform_fee` decimal(10, 2) NOT NULL COMMENT '平台手续费（10%）',
  `flyer_income` decimal(10, 2) NOT NULL COMMENT '飞手收入（订单金额-手续费）',
  `status` int NOT NULL COMMENT '状态（0-待结算 1-已结算）',
  `settlement_time` datetime NULL DEFAULT NULL COMMENT '结算时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`settlement_id`) USING BTREE,
  INDEX `idx_flyer_id`(`flyer_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '结算记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID（主键）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（登录用，唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` int NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（登录用，唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `role_type` int NOT NULL COMMENT '角色类型：1-农户，2-飞手，3-机主',
  `audit_status` int NULL DEFAULT NULL COMMENT '审核状态：0-待审核，1-通过，2-拒绝（仅飞手/机主有值）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` int NULL DEFAULT 1 COMMENT '用户状态（1-正常，0-禁用）',
  `is_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-删除',
  `balance` double NOT NULL DEFAULT 0 COMMENT '余额',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '位置',
  `planting_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '农户独有，种植类型',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role_type`(`role_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_device_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `t_device_operate_log`;
CREATE TABLE `t_device_operate_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID（主键）',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作员ID（关联sys_user.user_id）',
  `before_status` int NULL DEFAULT NULL COMMENT '操作前状态',
  `after_status` int NULL DEFAULT NULL COMMENT '操作后状态',
  `operate_type` int NULL DEFAULT NULL COMMENT '操作类型（1：状态变更 2：绑定飞手 3：解绑飞手 4：维护记录）',
  `remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作备注',
  `operate_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `operate_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作IP',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_id`(`device_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for transaction_record
-- ----------------------------
DROP TABLE IF EXISTS `transaction_record`;
CREATE TABLE `transaction_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '交易记录ID（主键）',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联sys_user.user_id）',
  `amount` decimal(10, 2) NOT NULL COMMENT '交易金额',
  `transaction_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易类型（INCOME-收入 EXPENSE-支出）',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态（1-成功 0-失败）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易描述',
  `related_demand_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_transaction_type`(`transaction_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_related_order_id`(`related_demand_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户交易记录表（收支明细）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_flyer
-- ----------------------------
DROP TABLE IF EXISTS `user_flyer`;
CREATE TABLE `user_flyer`  (
  `flyer_id` bigint(20) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT COMMENT '飞手信息ID（主键）',
  `user_id` bigint NOT NULL COMMENT '关联用户ID（关联sys_user.user_id）',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名（冗余，方便查询）',
  `license_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执照类型（如：AOPA证书、CAAC证书）',
  `license_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执照编号',
  `license_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执照文件URL（OSS存储路径）',
  `insurance_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '保险编号',
  `insurance_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '保险文件URL（OSS存储路径）',
  `skill_level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '技能等级：喷洒认证、巡检认证、全能认证',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在位置（经纬度，格式：lat,lon）',
  `reputation` decimal(5, 2) NULL DEFAULT 50.00 COMMENT '信誉分（0-100，初始50）',
  `price_per_acre` decimal(10, 2) NULL DEFAULT 100.00 COMMENT '每亩作业价格（单位：元）',
  `is_free` int NULL DEFAULT 1 COMMENT '是否空闲：0-忙碌、1-空闲',
  `audit_status` int NULL DEFAULT 0 COMMENT '审核状态：0-待审核、1-通过、2-拒绝',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核结果（冗余，便于展示）',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核不通过原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `qualifications` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资质',
  `credit_score` int NULL DEFAULT 0 COMMENT '信用分（初始0）',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动填充）',
  `is_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除：0-正常、1-删除',
  `introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '飞手简介',
  `experience` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '飞龄',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像url',
  PRIMARY KEY (`flyer_id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '飞手信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_owner
-- ----------------------------
DROP TABLE IF EXISTS `user_owner`;
CREATE TABLE `user_owner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '机主表主键',
  `user_id` bigint NOT NULL COMMENT '关联用户ID（关联sys_user.user_id）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `license_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执照类型',
  `license_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执照编号',
  `license_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执照文件URLs（多个用逗号分隔）',
  `device_total` int NULL DEFAULT 0 COMMENT '设备总数',
  `available_device_count` int NULL DEFAULT 0 COMMENT '可用设备数',
  `credit_score` int NULL DEFAULT 0 COMMENT '信用分',
  `common_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '常用作业区域',
  `audit_status` int NULL DEFAULT 0 COMMENT '审核状态：0-待审核、1-通过、2-拒绝',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '逻辑删除：0-正常、1-删除',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资质证（照片）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '机主信息表（无人机设备拥有者）' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
