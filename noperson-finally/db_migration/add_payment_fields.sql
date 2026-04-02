-- 数据库迁移脚本：为spray_demand表添加支付相关字段
-- 创建时间："+new Date().toString()+"

-- 1. 添加实际支付金额字段
alter table spray_demand
add column payment_amount double null comment '实际支付金额';

-- 2. 添加支付状态字段
alter table spray_demand
add column payment_status int null comment '支付状态：0-未支付，1-已支付，2-支付失败';

-- 3. 添加支付时间字段
alter table spray_demand
add column payment_time datetime null comment '支付时间';

-- 4. 添加支付方式字段
alter table spray_demand
add column payment_method varchar(50) null comment '支付方式：balance-余额支付';

-- 5. 添加支付记录ID字段
alter table spray_demand
add column payment_record_id bigint null comment '支付记录ID';

-- 6. 为新添加的字段添加索引以提高查询效率
create index idx_payment_status on spray_demand(payment_status);
create index idx_payment_time on spray_demand(payment_time);
create index idx_payment_method on spray_demand(payment_method);