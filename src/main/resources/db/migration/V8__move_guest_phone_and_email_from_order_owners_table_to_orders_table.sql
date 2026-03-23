ALTER TABLE pharmacy.order_owners
    DROP COLUMN guest_email;
ALTER TABLE pharmacy.order_owners
    DROP COLUMN guest_phone;

ALTER TABLE pharmacy.orders
    ADD COLUMN email VARCHAR(255);

ALTER TABLE pharmacy.orders
    ADD COLUMN phone VARCHAR(32);

UPDATE pharmacy.orders
SET phone = 'unknown';

ALTER TABLE pharmacy.orders
    ALTER COLUMN phone SET NOT NULL;


