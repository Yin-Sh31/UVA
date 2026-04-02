-- 创建设备租借表
CREATE TABLE `device_rental` (
  `rental_id` bigint NOT NULL AUTO_INCREMENT COMMENT '租借记录ID（主键）',
  `device_id` bigint NOT NULL COMMENT '设备ID（关联drone_device.device_id）',
  `owner_id` bigint NOT NULL COMMENT '机主ID（关联user_owner.id）',
  `flyer_id` bigint NOT NULL COMMENT '飞手ID（关联sys_user.user_id）',
  `rental_start_time` datetime NOT NULL COMMENT '租借开始时间',
  `rental_end_time` datetime NULL DEFAULT NULL COMMENT '租借结束时间',
  `rental_status` int NOT NULL DEFAULT 1 COMMENT '租借状态：1-租借中 2-已归还 3-已取消',
  `rental_amount` decimal(10,2) NULL DEFAULT NULL COMMENT '租借费用',
  `payment_status` int NULL DEFAULT 0 COMMENT '支付状态：0-未支付 1-已支付 2-支付失败',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rental_id`) USING BTREE,
  INDEX `idx_device_id` (`device_id` ASC) USING BTREE,
  INDEX `idx_flyer_id` (`flyer_id` ASC) USING BTREE,
  INDEX `idx_owner_id` (`owner_id` ASC) USING BTREE,
  INDEX `idx_rental_status` (`rental_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备租借记录表' ROW_FORMAT = Dynamic;

-- 外键约束（可选，根据系统需求决定是否启用）
ALTER TABLE `device_rental`
ADD CONSTRAINT `fk_rental_device` FOREIGN KEY (`device_id`) REFERENCES `drone_device` (`device_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 说明：
-- 该表用于完整记录设备的租借历史和当前状态
-- 主要功能：
-- 1. 记录每次租借的完整信息，包括设备、飞手、机主
-- 2. 跟踪租借开始和结束时间
-- 3. 管理租借状态和支付状态
-- 4. 提供租借历史查询功能
-- 5. 支持租借费用管理