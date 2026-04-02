-- 添加设备租借状态字段
ALTER TABLE `drone_device`
ADD COLUMN `rental_status` INT NOT NULL DEFAULT 0 COMMENT '租借状态：0-可租借 1-已租借 2-审核中';

-- 添加索引以提高查询性能
ALTER TABLE `drone_device`
ADD INDEX `idx_rental_status` (`rental_status` ASC);

-- 更新现有数据，根据current_flyer_id是否为空设置租借状态
UPDATE `drone_device`
SET `rental_status` = CASE
    WHEN `flyer_id` IS NOT NULL THEN 1
    ELSE 0
END;

-- 说明：
-- rental_status字段定义：
-- 0: 可租借 - 设备当前未被租借，可以进行租借操作
-- 1: 已租借 - 设备已被飞手租借，current_flyer_id有值
-- 2: 审核中 - 租借请求正在审核过程中