ALTER TABLE pharmacy.reviews
ADD CONSTRAINT reviews_unique_user_product UNIQUE (user_id, product_id);