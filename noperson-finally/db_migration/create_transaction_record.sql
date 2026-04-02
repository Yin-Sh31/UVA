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

 Date: 16/10/2025 20:25:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户交易记录表（收支明细）' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;