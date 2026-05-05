-- V19: Add warehouse_id to orders table to track which warehouse an order uses
ALTER TABLE pharmacy.orders ADD COLUMN warehouse_id INT REFERENCES pharmacy.warehouses(id);

CREATE INDEX idx_orders_warehouse ON pharmacy.orders(warehouse_id);