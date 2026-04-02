-- Alter demand feedback table to add image field
ALTER TABLE `demand_feedback` ADD COLUMN `feedback_images` TEXT COMMENT '反馈图片URLs，多个用逗号分隔' AFTER `feedback_content`;
