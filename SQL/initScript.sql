CREATE TYPE color_enum AS ENUM(
    'BLACK',
    'WHITE',
    'RED',
    'BLUE',
    'GREEN',
    'YELLOW',
    'PURPLE',
    'ORANGE',
    'GREY',
    'BROWN'
);

CREATE TYPE type_of_product_enum AS ENUM(
    'PEDALS_AND_EFFECTS',
    'ELECTRIC_GUITAR',
    'STUDIO_RECORDING_GEAR',
    'KEYS_AND_MIDI',
    'AMPLIFIER',
    'DRUMMS_AND_PERCUSSION',
    'BASS_GUITAR',
    'ACOUSTIC_GUITAR',
    'SOFTWARE_AND_ACCESSORIES'
);

CREATE TYPE tip_material_enum AS ENUM(
    'WOOD',
    'METAL',
    'PLASTIC',
    'COMPOSITE'
);

CREATE TYPE body_material_enum AS ENUM(
    'MAHOGANY',
    'MAPLE',
    'ASH',
    'ALDER',
    'BASSWOOD'
);

CREATE TYPE pickup_configuration_enum AS ENUM(
    'SSS',
    'HSS',
    'HH',
    'P90',
    'SINGLE'
);

CREATE TYPE type_combo_amplifier_enum AS ENUM(
    'TUBE',
    'SOLID STATE',
    'HYBRID',
    'MODELING'
);

CREATE TYPE guitar_form_enum AS ENUM(
    'STRATOCASTER',
    'TELECASTER',
    'THINLINE',
    'LES PAUL',
    'V',
    'JAGUAR',
    'JAZZ-MASTER',
    'MOCKINGBIRD',
    'STAR',
    'MUSTANG'
);

CREATE TYPE country_enum AS ENUM(
    'USA',
    'JAPAN',
    'GERMANY',
    'UNITED KINGDOM',
    'CHINA',
    'SOUTH KOREA',
    'INDONESIA',
    'MEXICO',
    'CANADA',
    'ITALY',
    'FRANCE',
    'BRAZIL',
    'AUSTRALIA',
    'RUSSIA'
);

CREATE TYPE type_of_musician_enum AS ENUM(
    'MUSICAL PRODUCER',
    'GUITARIST',
    'DRUMMER',
    'BASSIST',
    'SINGER',
    'RAPPER',
    'KEYBOARDIST'
);

CREATE TYPE genre_enum AS ENUM(
    'BLUES',
    'ROCK',
    'POP',
    'JAZZ',
    'RAP',
    'METAL',
    'CLASSICAL',
    'REGGAE',
    'ELECTRONIC',
    'HIP-HOP'
);

CREATE TABLE brand (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    country country_enum NOT NULL,
    website TEXT,
    email TEXT
);

CREATE TABLE musician (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    subscribers INTEGER NOT NULL CHECK (subscribers >= 0) DEFAULT 0
);

CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    is_admin BOOLEAN DEFAULT FALSE,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    subscriptions INTEGER CHECK (subscriptions >= 0) DEFAULT 0
);

CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    rate NUMERIC(2, 1) CHECK (
        rate >= 0
        AND rate <= 5
    ) DEFAULT 0,
    brand_id INTEGER NOT NULL,
    guitar_form guitar_form_enum,
    type_of_product type_of_product_enum NOT NULL,
    lads INTEGER,
    avg_price FLOAT CHECK (avg_price >= 0) DEFAULT 0,
    color color_enum NOT NULL,
    strings INTEGER,
    tip_material tip_material_enum,
    body_material body_material_enum,
    pickup_configuration pickup_configuration_enum,
    type_combo_amplifier type_combo_amplifier_enum,
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand (id) ON DELETE SET NULL
);

CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    header TEXT NOT NULL,
    text TEXT NOT NULL,
    author_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    accepted BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_articles_user FOREIGN KEY (author_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE feedback (
    id SERIAL PRIMARY KEY,
    author_id INTEGER NOT NULL,
    product_id INTEGER,
    article_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    text TEXT NOT NULL,
    stars INTEGER CHECK (
        stars >= 0
        AND stars <= 5
    ) NOT NULL,
    CONSTRAINT fk_feedback_user FOREIGN KEY (author_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);

CREATE TABLE forum_topic (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    author_id INTEGER NOT NULL,
    is_closed BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_forum_topic_user FOREIGN KEY (author_id) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE forum_post (
    id SERIAL PRIMARY KEY,
    topic_id INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    author_id INTEGER NOT NULL,
    vote INTEGER DEFAULT 0,
    CONSTRAINT fk_forum_post_topic FOREIGN KEY (topic_id) REFERENCES forum_topic (id) ON DELETE CASCADE,
    CONSTRAINT fk_forum_post_user FOREIGN KEY (author_id) REFERENCES app_user (id) ON DELETE SET NULL
);


CREATE TABLE shop (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    website TEXT,
    email TEXT,
    address TEXT
);

CREATE TABLE shop_product (
    shop_id INTEGER,
    product_id INTEGER,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    available BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT pk_shop_product PRIMARY KEY (shop_id, product_id),
    CONSTRAINT fk_shop_product_shop FOREIGN KEY (shop_id) REFERENCES shop (id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_product_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

-- CREATE TABLE custom_product_collection (
--     id SERIAL PRIMARY KEY,
--     name TEXT NOT NULL,
--     description TEXT,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     author_id INTEGER,
--     CONSTRAINT fk_custom_product_collection_user FOREIGN KEY (author_id) REFERENCES app_user (id) ON DELETE SET NULL
-- );

-- CREATE TABLE custom_product_collection_product (
--     collection_id INTEGER,
--     product_id INTEGER,
--     CONSTRAINT pk_custom_product_collection_product PRIMARY KEY (collection_id, product_id),
--     CONSTRAINT fk_custom_product_collection_product_collection FOREIGN KEY (collection_id) REFERENCES custom_product_collection (id) ON DELETE CASCADE,
--     CONSTRAINT fk_custom_product_collection_product_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
-- );


CREATE TABLE user_musician_subscription (
    user_id INTEGER,
    musician_id INTEGER,
    CONSTRAINT pk_user_musician PRIMARY KEY (user_id, musician_id),
    CONSTRAINT fk_user_musician_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_musician_musician FOREIGN KEY (musician_id) REFERENCES musician (id) ON DELETE CASCADE
);

CREATE TABLE musician_genre (
    musician_id INTEGER,
    genre genre_enum NOT NULL, 
    CONSTRAINT pk_musician_genre PRIMARY KEY (musician_id, genre),
    CONSTRAINT fk_musician_genre_musician FOREIGN KEY (musician_id) REFERENCES musician (id) ON DELETE CASCADE
);

CREATE TABLE product_genre (
    product_id INTEGER,
    genre genre_enum NOT NULL,
    CONSTRAINT pk_product_genre PRIMARY KEY (product_id, genre),
    CONSTRAINT fk_product_genre_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE TABLE type_of_musician_user (
    type_of_musician type_of_musician_enum NOT NULL,
    user_id INTEGER,
    CONSTRAINT pk_type_of_musician_user PRIMARY KEY (type_of_musician, user_id),
    CONSTRAINT fk_type_of_musician_user_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE type_of_musician_musician (
    type_of_musician type_of_musician_enum NOT NULL,
    musician_id INTEGER,
    CONSTRAINT pk_type_of_musician_musician PRIMARY KEY (type_of_musician, musician_id),
    CONSTRAINT fk_type_of_musician_musician_musician FOREIGN KEY (musician_id) REFERENCES musician (id) ON DELETE CASCADE
);

CREATE TABLE genre_user (
    genre genre_enum NOT NULL,
    user_id INTEGER,
    CONSTRAINT pk_genre_user PRIMARY KEY (genre, user_id),
    CONSTRAINT fk_genre_user_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE musician_product (
    musician_id INTEGER,
    product_id INTEGER,
    CONSTRAINT pk_musician_product PRIMARY KEY (musician_id, product_id),
    CONSTRAINT fk_musician_product_musician FOREIGN KEY (musician_id) REFERENCES musician (id) ON DELETE CASCADE,
    CONSTRAINT fk_musician_product_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE
);

CREATE TABLE product_user (
    product_id INTEGER,
    user_id INTEGER,
    CONSTRAINT pk_product_user PRIMARY KEY (product_id, user_id),
    CONSTRAINT fk_product_user_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_user_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE product_articles (
    product_id INTEGER,
    article_id INTEGER,
    CONSTRAINT pk_product_articles PRIMARY KEY (product_id, article_id),
    CONSTRAINT fk_product_articles_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_articles_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION update_product_average_price()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        UPDATE product
        SET avg_price = (
            SELECT AVG(price)::numeric(10,2)
            FROM shop_product
            WHERE product_id = OLD.product_id AND available = true
        )
        WHERE id = OLD.product_id;
    ELSE
        UPDATE product
        SET avg_price = (
            SELECT AVG(price)::numeric(10,2)
            FROM shop_product
            WHERE product_id = NEW.product_id AND available = true
        )
        WHERE id = NEW.product_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_product_average_price AFTER
INSERT
    OR
UPDATE
OR DELETE ON shop_product FOR EACH ROW
EXECUTE FUNCTION update_product_average_price ();

CREATE OR REPLACE FUNCTION update_product_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE product
    SET rate = (
        SELECT AVG(stars)::numeric(2,1)
        FROM feedback
        WHERE product_id = NEW.product_id
    )
    WHERE id = NEW.product_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_product_rating AFTER
INSERT
    OR
UPDATE
OR DELETE ON feedback FOR EACH ROW
EXECUTE FUNCTION update_product_rating ();

CREATE OR REPLACE FUNCTION update_user_subscriptions()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE app_user
        SET subscriptions = (
            SELECT COUNT(*) 
            FROM user_musician_subscription 
            WHERE user_id = NEW.user_id
        )
        WHERE id = NEW.user_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE app_user
        SET subscriptions = (
            SELECT COUNT(*) 
            FROM user_musician_subscription 
            WHERE user_id = OLD.user_id
        )
        WHERE id = OLD.user_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_user_subscriptions AFTER
INSERT
    OR DELETE ON user_musician_subscription FOR EACH ROW
EXECUTE FUNCTION update_user_subscriptions ();

CREATE OR REPLACE FUNCTION update_musician_subscribers()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE musician 
        SET subscribers = (
            SELECT COUNT(*) 
            FROM user_musician_subscription 
            WHERE musician_id = NEW.musician_id
        )
        WHERE id = NEW.musician_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE musician 
        SET subscribers = (
            SELECT COUNT(*) 
            FROM user_musician_subscription 
            WHERE musician_id = OLD.musician_id
        )
        WHERE id = OLD.musician_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_subscribers AFTER
INSERT
    OR DELETE ON user_musician_subscription FOR EACH ROW
EXECUTE FUNCTION update_musician_subscribers ();

