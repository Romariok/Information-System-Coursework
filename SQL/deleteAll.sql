DROP INDEX IF EXISTS idx_product_brand_id;
DROP INDEX IF EXISTS idx_product_type_of_product;
DROP INDEX IF EXISTS idx_product_guitar_form;

DROP INDEX IF EXISTS idx_shop_product_shop_id;
DROP INDEX IF EXISTS idx_shop_product_product_id;

DROP INDEX IF EXISTS idx_feedback_author_id;
DROP INDEX IF EXISTS idx_feedback_product_id;
DROP INDEX IF EXISTS idx_feedback_article_id;

DROP INDEX IF EXISTS idx_articles_author_id;

DROP INDEX IF EXISTS idx_forum_topic_author_id;

DROP INDEX IF EXISTS idx_forum_post_topic_id;
DROP INDEX IF EXISTS idx_forum_post_author_id;

DROP INDEX IF EXISTS idx_musician_name;

DROP INDEX IF EXISTS idx_user_musician_subscription_user_id;
DROP INDEX IF EXISTS idx_user_musician_subscription_musician_id;

DROP INDEX IF EXISTS idx_musician_genre_genre;
DROP INDEX IF EXISTS idx_musician_genre_musician_id;

DROP INDEX IF EXISTS idx_product_genre_genre;
DROP INDEX IF EXISTS idx_product_genre_product_id;

DROP INDEX IF EXISTS idx_type_of_musician_user_user_id;
DROP INDEX IF EXISTS idx_type_of_musician_user_type_of_musician;

DROP INDEX IF EXISTS idx_type_of_musician_musician_musician_id;
DROP INDEX IF EXISTS idx_type_of_musician_musician_type_of_musician;

DROP INDEX IF EXISTS idx_genre_user_genre;
DROP INDEX IF EXISTS idx_genre_user_user_id;

DROP INDEX IF EXISTS idx_musician_product_musician_id;
DROP INDEX IF EXISTS idx_musician_product_product_id;

DROP INDEX IF EXISTS idx_product_user_product_id;
DROP INDEX IF EXISTS idx_product_user_user_id;

DROP INDEX IF EXISTS idx_product_articles_product_id;
DROP INDEX IF EXISTS idx_product_articles_article_id;

DROP INDEX IF EXISTS idx_shop_name;

DROP INDEX IF EXISTS idx_product_avg_price;
DROP INDEX IF EXISTS idx_product_rate;
DROP INDEX IF EXISTS idx_feedback_stars;
DROP INDEX IF EXISTS idx_shop_product_price;

DROP INDEX IF EXISTS idx_product_name;
DROP INDEX IF EXISTS idx_product_description;
DROP INDEX IF EXISTS idx_shop_address;
DROP INDEX IF EXISTS idx_articles_header;
DROP INDEX IF EXISTS idx_forum_topic_title;

DROP TABLE IF EXISTS product_articles CASCADE;

DROP TABLE IF EXISTS product_user CASCADE;

DROP TABLE IF EXISTS musician_product CASCADE;

DROP TABLE IF EXISTS genre_user CASCADE;

DROP TABLE IF EXISTS type_of_musician_user CASCADE;

DROP TABLE IF EXISTS type_of_musician_musician CASCADE;

DROP TABLE IF EXISTS product_genre CASCADE;

DROP TABLE IF EXISTS musician_genre CASCADE;

DROP TABLE IF EXISTS user_musician_subscription CASCADE;

DROP TABLE IF EXISTS shop_product CASCADE;

DROP TABLE IF EXISTS shop CASCADE;

DROP TABLE IF EXISTS feedback CASCADE;

DROP TABLE IF EXISTS articles CASCADE;

DROP TABLE IF EXISTS product CASCADE;

DROP TABLE IF EXISTS app_user CASCADE;

DROP TABLE IF EXISTS musician CASCADE;

DROP TABLE IF EXISTS brand CASCADE;

DROP TABLE IF EXISTS forum_post CASCADE;

DROP TABLE IF EXISTS forum_topic CASCADE;

-- DROP TABLE IF EXISTS custom_product_collection CASCADE;

-- DROP TABLE IF EXISTS custom_product_collection_product CASCADE;

DROP TYPE IF EXISTS color_enum;

DROP TYPE IF EXISTS type_of_product_enum;

DROP TYPE IF EXISTS tip_material_enum;

DROP TYPE IF EXISTS body_material_enum;

DROP TYPE IF EXISTS pickup_configuration_enum;

DROP TYPE IF EXISTS type_combo_amplifier_enum;

DROP TYPE IF EXISTS guitar_form_enum;

DROP TYPE IF EXISTS country_enum;

DROP TYPE IF EXISTS type_of_musician_enum;

DROP TYPE IF EXISTS genre_enum;

DROP TRIGGER IF EXISTS update_product_avg_price ON product;


DROP TRIGGER IF EXISTS trigger_update_product_average_price ON shop_product;
DROP FUNCTION IF EXISTS update_product_average_price();

DROP TRIGGER IF EXISTS trigger_update_product_rating ON feedback;
DROP FUNCTION IF EXISTS update_product_rating();

DROP TRIGGER IF EXISTS trigger_update_user_subscriptions ON user_musician_subscription;
DROP FUNCTION IF EXISTS update_user_subscriptions();

DROP TRIGGER IF EXISTS trigger_update_subscribers ON user_musician_subscription;
DROP FUNCTION IF EXISTS update_musician_subscribers();
DROP TRIGGER IF EXISTS trigger_update_product_average_price ON shop_product;
DROP FUNCTION IF EXISTS update_product_average_price();

DROP TRIGGER IF EXISTS trigger_update_product_rating ON feedback;
DROP FUNCTION IF EXISTS update_product_rating();

DROP TRIGGER IF EXISTS trigger_update_user_subscriptions ON user_musician_subscription;
DROP FUNCTION IF EXISTS update_user_subscriptions();

DROP TRIGGER IF EXISTS trigger_update_subscribers ON user_musician_subscription;
DROP FUNCTION IF EXISTS update_musician_subscribers();

