ALTER TABLE pharmacy.order_items
    DROP CONSTRAINT IF EXISTS order_items_order_id_fkey;

ALTER TABLE pharmacy.order_items
    ADD CONSTRAINT order_items_order_id_fkey
        FOREIGN KEY (order_id)
            REFERENCES pharmacy.orders (id)
            ON DELETE CASCADE;

ALTER TABLE pharmacy.loyalty_transactions
DROP CONSTRAINT IF EXISTS loyalty_transactions_order_id_fkey;

ALTER TABLE pharmacy.loyalty_transactions
    ADD CONSTRAINT loyalty_transactions_order_id_fkey FOREIGN KEY (order_id)
        REFERENCES pharmacy.orders (id) ON DELETE SET NULL;
