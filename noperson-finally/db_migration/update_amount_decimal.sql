-- 修改transaction_record表的amount字段数据类型，从decimal(10,2)改为decimal(15,2)
ALTER TABLE `transaction_record` MODIFY COLUMN `amount` DECIMAL(15, 2) NOT NULL COMMENT '交易金额';