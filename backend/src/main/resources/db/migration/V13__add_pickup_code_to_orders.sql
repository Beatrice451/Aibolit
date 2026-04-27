ALTER TABLE pharmacy.orders
    ADD COLUMN pickup_code              VARCHAR(6),
    ADD COLUMN pickup_code_generated_at TIMESTAMP;

CREATE UNIQUE INDEX idx_orders_pickup_code ON pharmacy.orders (pickup_code)
    WHERE pickup_code IS NOT NULL;