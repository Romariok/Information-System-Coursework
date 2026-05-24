CREATE INDEX idx_product_brand_id ON product(brand_id);
CREATE INDEX idx_product_type_of_product ON product(type_of_product);
CREATE INDEX idx_product_guitar_form ON product(guitar_form);
CREATE INDEX idx_product_type_price_rate ON product(type_of_product, avg_price, rate);
CREATE INDEX idx_product_avg_price ON product(avg_price);
CREATE INDEX idx_product_rate ON product(rate);
CREATE INDEX idx_product_name ON product(name);

CREATE INDEX idx_shop_name ON shop(name);

CREATE INDEX idx_shop_product_shop_id ON shop_product(shop_id);
CREATE INDEX idx_shop_product_product_id ON shop_product(product_id);
CREATE INDEX idx_shop_product_shop_price ON shop_product(shop_id, price);
CREATE INDEX idx_shop_product_price ON shop_product(price);
CREATE INDEX idx_shop_product_available_product_price ON shop_product(product_id, price) WHERE available = TRUE;


CREATE INDEX idx_feedback_author_id ON feedback(author_id);

CREATE INDEX idx_feedback_product_notnull ON feedback(product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_feedback_article_id ON feedback(article_id);
CREATE INDEX idx_feedback_stars ON feedback(stars);


CREATE INDEX idx_articles_author_id ON articles(author_id);
CREATE INDEX idx_articles_header ON articles(header);
CREATE INDEX idx_articles_author_accepted ON articles(author_id) WHERE accepted = TRUE;
CREATE INDEX idx_articles_author_created ON articles(author_id, created_at DESC);

CREATE INDEX idx_forum_topic_author_id ON forum_topic(author_id);
CREATE INDEX idx_forum_topic_title ON forum_topic(title);

CREATE INDEX idx_forum_post_topic_id ON forum_post(topic_id);
CREATE INDEX idx_forum_post_author_id ON forum_post(author_id);

CREATE INDEX idx_musician_name ON musician(name);
CREATE INDEX idx_musician_subscribers ON musician(subscribers DESC);


CREATE INDEX idx_user_musician_subscription_musician_id ON user_musician_subscription(musician_id);

CREATE INDEX idx_musician_genre_genre ON musician_genre(genre);
CREATE INDEX idx_musician_genre_musician_genre ON musician_genre(musician_id, genre);


CREATE INDEX idx_product_genre_genre ON product_genre(genre);

CREATE INDEX idx_type_of_musician_user_user_id ON type_of_musician_user(user_id);
CREATE INDEX idx_type_of_musician_user_type_of_musician ON type_of_musician_user(type_of_musician);

CREATE INDEX idx_type_of_musician_musician_musician_id ON type_of_musician_musician(musician_id);
CREATE INDEX idx_type_of_musician_musician_type_of_musician ON type_of_musician_musician(type_of_musician);

CREATE INDEX idx_genre_user_genre ON genre_user(genre);
CREATE INDEX idx_genre_user_user_id ON genre_user(user_id);

CREATE INDEX idx_musician_product_musician_id ON musician_product(musician_id);
CREATE INDEX idx_musician_product_product_id ON musician_product(product_id);

CREATE INDEX idx_product_user_product_id ON product_user(product_id);
CREATE INDEX idx_product_user_user_id ON product_user(user_id);

CREATE INDEX idx_product_articles_product_id ON product_articles(product_id);
CREATE INDEX idx_product_articles_article_id ON product_articles(article_id);
