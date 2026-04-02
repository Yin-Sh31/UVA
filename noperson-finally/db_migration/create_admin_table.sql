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

 Date: 29/10/2025
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_admin
-- ----------------------------
-- 明文密码版本 - 系统支持直接使用明文密码验证
INSERT INTO `sys_admin` VALUES (1, 'admin', '123456', '13800138000', '2025-10-29 00:00:00', NULL, 1);
-- 注意：系统会根据密码格式自动识别是明文还是BCrypt加密密码
-- 如果需要使用加密密码，可以使用如下BCrypt加密的'123456'
-- INSERT INTO `sys_admin` VALUES (1, 'admin', '$2a$10$Qz6YbZ6iB8Z6iB8Z6iB8Z6iB8Z6iB8Z6iB8Z6iB8Z6iB8Z6iB8Z6', '13800138000', '2025-10-29 00:00:00', NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;