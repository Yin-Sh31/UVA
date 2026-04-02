-- 创建农户常用地址表
DROP TABLE IF EXISTS `farmer_favorite_address`;
CREATE TABLE `farmer_favorite_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `farmer_id` bigint NOT NULL COMMENT '农户ID（关联sys_user.user_id）',
  `order_type` int NOT NULL COMMENT '需求类型：1-喷洒，2-巡检',
  `land_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地块名称',
  `land_boundary` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地块边界（经纬度）',
  `crop_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作物类型',
  `land_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地块位置',
  `land_area` bigint NOT NULL COMMENT '地块面积（亩）',
  `pest_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '病虫害类型（仅喷洒需求有）',
  `inspection_purpose` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '巡检目的（仅巡检需求有）',
  `expected_resolution` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '期望分辨率（仅巡检需求有）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_farmer_order_type`(`farmer_id` ASC, `order_type` ASC) USING BTREE,
  CONSTRAINT `fk_farmer_address` FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农户常用地址表';
