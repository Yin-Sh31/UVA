-- 创建设备动态表
CREATE TABLE `device_activity` (
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
  INDEX `idx_device_id` (`device_id` ASC) USING BTREE,
  INDEX `idx_create_time` (`create_time` DESC) USING BTREE,
  INDEX `idx_activity_type` (`activity_type` ASC) USING BTREE,
  CONSTRAINT `fk_activity_device` FOREIGN KEY (`device_id`) REFERENCES `drone_device` (`device_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备动态记录表' ROW_FORMAT = Dynamic;

-- 说明：
-- 该表用于记录设备的所有状态变化和操作动态
-- 主要功能：
-- 1. 记录设备上线、下线、维护、租借、归还等操作
-- 2. 记录设备状态变更历史
-- 3. 记录操作人信息
-- 4. 支持按时间倒序查询最近动态