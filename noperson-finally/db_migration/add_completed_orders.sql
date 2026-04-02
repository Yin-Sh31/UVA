-- 添加飞手完成订单数量字段
ALTER TABLE `user_flyer` ADD COLUMN `completed_orders` INT NULL DEFAULT 0 COMMENT '完成订单数量' AFTER `avatar`;