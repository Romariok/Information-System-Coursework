
DROP INDEX IF EXISTS idx_product_description;

DROP INDEX IF EXISTS idx_shop_address;

DROP INDEX IF EXISTS idx_feedback_author_product_article;

DROP INDEX IF EXISTS idx_user_musician_subscription_user_id;

DROP INDEX IF EXISTS idx_product_genre_product_id;

DROP INDEX IF EXISTS idx_musician_genre_musician_id;

CREATE INDEX IF NOT EXISTS idx_articles_author_accepted
    ON articles(author_id)
    WHERE accepted = TRUE;

CREATE INDEX IF NOT EXISTS idx_shop_product_available_product_price
    ON shop_product(product_id, price)
    WHERE available = TRUE;

CREATE INDEX IF NOT EXISTS idx_product_type_price_rate
    ON product(type_of_product, avg_price, rate);

CREATE INDEX IF NOT EXISTS idx_feedback_product_notnull
    ON feedback(product_id)
    WHERE product_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_musician_subscribers
    ON musician(subscribers DESC);

CREATE INDEX IF NOT EXISTS idx_articles_author_created
    ON articles(author_id, created_at DESC);
