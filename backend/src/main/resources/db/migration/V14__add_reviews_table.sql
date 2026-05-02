CREATE TABLE pharmacy.reviews(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rating SMALLINT CHECK ( rating BETWEEN 1 AND 5) NOT NULL ,
    comment TEXT NOT NULL ,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    product_id INT REFERENCES pharmacy.products(id) ON DELETE CASCADE ,
    user_id INT REFERENCES pharmacy.users(id) ON DELETE CASCADE
);