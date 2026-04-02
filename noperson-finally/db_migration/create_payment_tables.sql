-- 创建支付订单表
CREATE TABLE IF NOT EXISTS payment_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id VARCHAR(50) NOT NULL,        -- 关联的订单ID
  order_type VARCHAR(20) NOT NULL,      -- 订单类型：INSPECTION/SPRAY/DEVICE_RENT
  user_id BIGINT NOT NULL,              -- 支付用户ID
  amount DECIMAL(10,2) NOT NULL,        -- 支付金额
  payment_method VARCHAR(20) NOT NULL,  -- 支付方式
  out_trade_no VARCHAR(100) NOT NULL,   -- 外部交易号
  status VARCHAR(20) NOT NULL,          -- 状态：WAITING/SUCCESS/FAILED/CANCELLED
  create_time DATETIME NOT NULL,        -- 创建时间
  pay_time DATETIME,                    -- 支付时间
  refund_time DATETIME,                 -- 退款时间
  FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
  INDEX idx_order_id (order_id),
  INDEX idx_order_type (order_type),
  INDEX idx_out_trade_no (out_trade_no),
  INDEX idx_user_id (user_id)
);

-- 创建支付日志表
CREATE TABLE IF NOT EXISTS payment_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,              -- 用户ID
  order_id VARCHAR(50) NOT NULL,        -- 订单ID
  order_type VARCHAR(20) NOT NULL,      -- 订单类型
  amount DECIMAL(10,2) NOT NULL,        -- 金额
  status VARCHAR(20) NOT NULL,          -- 状态
  payment_method VARCHAR(20) NOT NULL,  -- 支付方式
  transaction_id VARCHAR(100),          -- 交易ID
  create_time DATETIME NOT NULL,        -- 创建时间
  remark VARCHAR(200),                  -- 备注
  FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
  INDEX idx_order_id (order_id),
  INDEX idx_user_id (user_id),
  INDEX idx_create_time (create_time)
);