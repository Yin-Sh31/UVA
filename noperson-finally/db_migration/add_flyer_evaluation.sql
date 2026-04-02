CREATE TABLE IF NOT EXISTS flyer_evaluation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  flyer_id BIGINT NOT NULL,
  farmer_id BIGINT NOT NULL,
  score_quality INT NOT NULL,
  score_punctuality INT NOT NULL,
  score_attitude INT NOT NULL,
  score_efficiency INT NOT NULL,
  comment VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_order_farmer (order_id, farmer_id),
  INDEX idx_flyer_id (flyer_id),
  INDEX idx_farmer_id (farmer_id),
  INDEX idx_created_at (created_at)
);

ALTER TABLE user_flyer
ADD COLUMN total_score DECIMAL(3,2) DEFAULT 0.00,
ADD COLUMN star_level INT DEFAULT 0,
ADD COLUMN evaluation_count INT DEFAULT 0,
ADD COLUMN positive_rate DECIMAL(5,2) DEFAULT 0.00,
ADD COLUMN complaint_count INT DEFAULT 0,
ADD COLUMN credit_status INT DEFAULT 0;
