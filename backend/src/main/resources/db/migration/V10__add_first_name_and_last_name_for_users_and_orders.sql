ALTER TABLE pharmacy.users
ADD COLUMN first_name VARCHAR(255) NOT NULL DEFAULT 'not stated';

ALTER TABLE pharmacy.users
ADD COLUMN last_name VARCHAR(255) NOT NULL DEFAULT 'not stated';

ALTER TABLE pharmacy.orders
ADD COLUMN first_name VARCHAR(255) NOT NULL DEFAULT 'not stated';

ALTER TABLE pharmacy.orders
ADD COLUMN last_name VARCHAR(255) NOT NULL DEFAULT 'not stated';
