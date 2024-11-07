CREATE INDEX idx_product_price_rate_brand ON product (price, rate, brand_id);

CREATE INDEX idx_product_brand ON product (brand_id);
CREATE INDEX idx_product_guitar_form ON product (guitar_form_id);
CREATE INDEX idx_product_type ON product (type_of_product_id);


CREATE INDEX idx_user_musician_musician_user ON user_musician_subscription (musician_id, user_id);
CREATE INDEX idx_product_genre_genre_id ON product_genre (genre_id);
CREATE INDEX idx_product_brand_price_rate ON product (brand_id, price, rate);