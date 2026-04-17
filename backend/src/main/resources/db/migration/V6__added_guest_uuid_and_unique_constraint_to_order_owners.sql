ALTER TABLE pharmacy.order_owners
    ADD COLUMN guest_uuid UUID UNIQUE;

CREATE INDEX idx_order_owners_guest_uuid ON pharmacy.order_owners (guest_uuid);

ALTER TABLE pharmacy.order_owners
    DROP CONSTRAINT order_owners_check;

ALTER TABLE pharmacy.order_owners
    ADD CONSTRAINT order_owners_check CHECK (
        (owner_type = 'USER' AND user_Id IS NOT NULL) OR
        (owner_type = 'GUEST' AND order_owners.guest_uuid IS NOT NULL)
        );

ALTER TABLE pharmacy.order_owners
    ADD CONSTRAINT user_id_key UNIQUE (user_id);
