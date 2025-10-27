CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT,
  amount NUMERIC(10,2),
  status VARCHAR(20),
  created_at TIMESTAMP DEFAULT NOW()
);