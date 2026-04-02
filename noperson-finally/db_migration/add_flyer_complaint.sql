CREATE TABLE IF NOT EXISTS flyer_complaint (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  reporter_id BIGINT NOT NULL,
  target_id BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  status INT DEFAULT 0,
  result VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_reporter (order_id, reporter_id),
  INDEX idx_target_id (target_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
);
