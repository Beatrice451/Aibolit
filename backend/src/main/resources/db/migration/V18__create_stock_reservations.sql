-- V18: Create stock_reservations table for reserving stock during order placement
CREATE TABLE IF NOT EXISTS pharmacy.stock_reservations (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id INT NOT NULL REFERENCES pharmacy.products(id),
    warehouse_id INT NOT NULL REFERENCES pharmacy.warehouses(id),
    order_id INT NOT NULL REFERENCES pharmacy.orders(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    reserved_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'RELEASED', 'COMPLETED'))
);

CREATE INDEX idx_stock_reservations_order ON pharmacy.stock_reservations(order_id);
CREATE INDEX idx_stock_reservations_product_warehouse ON pharmacy.stock_reservations(product_id, warehouse_id);
CREATE INDEX idx_stock_reservations_status ON pharmacy.stock_reservations(status);