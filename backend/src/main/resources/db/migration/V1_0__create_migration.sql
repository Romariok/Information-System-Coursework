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
    CONSTRAINT fk_feedback_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    CONSTRAINT chk_feedback_target CHECK (
        (product_id IS NOT NULL AND article_id IS NULL)
        OR (product_id IS NULL AND article_id IS NOT NULL)
    )
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

-- product table
CREATE INDEX idx_product_brand_id ON product(brand_id);
CREATE INDEX idx_product_type_of_product ON product(type_of_product);
CREATE INDEX idx_product_guitar_form ON product(guitar_form);

-- shop_product table
CREATE INDEX idx_shop_product_shop_id ON shop_product(shop_id);
CREATE INDEX idx_shop_product_product_id ON shop_product(product_id);

-- feedback table
CREATE INDEX idx_feedback_author_id ON feedback(author_id);
CREATE INDEX idx_feedback_product_id ON feedback(product_id);
CREATE INDEX idx_feedback_article_id ON feedback(article_id);
CREATE INDEX idx_feedback_author_product_article ON feedback(author_id, product_id, article_id);

-- articles table
CREATE INDEX idx_articles_author_id ON articles(author_id);

-- forum_topic table
CREATE INDEX idx_forum_topic_author_id ON forum_topic(author_id);

-- forum_post table
CREATE INDEX idx_forum_post_topic_id ON forum_post(topic_id);
CREATE INDEX idx_forum_post_author_id ON forum_post(author_id);

-- musician table
CREATE INDEX idx_musician_name ON musician(name);

-- user_musician_subscription table
CREATE INDEX idx_user_musician_subscription_user_id ON user_musician_subscription(user_id);
CREATE INDEX idx_user_musician_subscription_musician_id ON user_musician_subscription(musician_id);

-- musician_genre table
CREATE INDEX idx_musician_genre_genre ON musician_genre(genre);
CREATE INDEX idx_musician_genre_musician_id ON musician_genre(musician_id);
CREATE INDEX idx_musician_genre_musician_genre ON musician_genre(musician_id, genre);

-- product_genre table
CREATE INDEX idx_product_genre_genre ON product_genre(genre);
CREATE INDEX idx_product_genre_product_id ON product_genre(product_id);

-- type_of_musician_user table
CREATE INDEX idx_type_of_musician_user_user_id ON type_of_musician_user(user_id);
CREATE INDEX idx_type_of_musician_user_type_of_musician ON type_of_musician_user(type_of_musician);

-- type_of_musician_musician table
CREATE INDEX idx_type_of_musician_musician_musician_id ON type_of_musician_musician(musician_id);
CREATE INDEX idx_type_of_musician_musician_type_of_musician ON type_of_musician_musician(type_of_musician);

-- genre_user table
CREATE INDEX idx_genre_user_genre ON genre_user(genre);
CREATE INDEX idx_genre_user_user_id ON genre_user(user_id);

-- musician_product table
CREATE INDEX idx_musician_product_musician_id ON musician_product(musician_id);
CREATE INDEX idx_musician_product_product_id ON musician_product(product_id);

-- product_user table
CREATE INDEX idx_product_user_product_id ON product_user(product_id);
CREATE INDEX idx_product_user_user_id ON product_user(user_id);

-- product_articles table
CREATE INDEX idx_product_articles_product_id ON product_articles(product_id);
CREATE INDEX idx_product_articles_article_id ON product_articles(article_id);

-- shop table
CREATE INDEX idx_shop_name ON shop(name);
-- shop_product table
CREATE INDEX idx_shop_product_shop_price ON shop_product(shop_id, price);

-- Optimize filters and sorting
CREATE INDEX idx_product_avg_price ON product(avg_price);
CREATE INDEX idx_product_rate ON product(rate);
CREATE INDEX idx_feedback_stars ON feedback(stars);
CREATE INDEX idx_shop_product_price ON shop_product(price);

-- Optimize text search on name or description fields
CREATE INDEX idx_product_name ON product(name);
CREATE INDEX idx_product_description ON product(description);
CREATE INDEX idx_shop_address ON shop(address);
CREATE INDEX idx_articles_header ON articles(header);
CREATE INDEX idx_forum_topic_title ON forum_topic(title);

-- Процедура добавления отзыва с обновлением рейтинга продукта
CREATE OR REPLACE PROCEDURE add_product_feedback(
    p_user_id INTEGER,
    p_product_id INTEGER,
    p_text TEXT,
    p_stars INTEGER
) AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM product WHERE id = p_product_id) THEN
        RAISE EXCEPTION 'Продукт не найден';
    END IF;
    
    INSERT INTO feedback (user_id, product_id, text, stars)
    VALUES (p_user_id, p_product_id, p_text, p_stars);
    
    UPDATE product
    SET rate = (
        SELECT ROUND(AVG(stars)::numeric(2,1), 1)
        FROM feedback
        WHERE product_id = p_product_id
    )
    WHERE id = p_product_id;
END;
$$ LANGUAGE plpgsql;

-- Процедура добавления отзыва с обновлением рейтинга статьи
CREATE OR REPLACE PROCEDURE add_article_feedback(
    p_user_id INTEGER,
    p_article_id INTEGER,
    p_text TEXT,
    p_stars INTEGER
) AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM articles WHERE id = p_article_id) THEN
        RAISE EXCEPTION 'Статья не найдена';
    END IF;
    
    INSERT INTO feedback (user_id, article_id, text, stars)
    VALUES (p_user_id, p_article_id, p_text, p_stars);
    
    UPDATE articles
    SET rate = (
        SELECT ROUND(AVG(stars)::numeric(2,1), 1)
        FROM feedback
        WHERE article_id = p_article_id
    )
    WHERE id = p_article_id;
END;
$$ LANGUAGE plpgsql;

-- Процедура подписки на музыканта
CREATE OR REPLACE PROCEDURE subscribe_to_musician(
    p_user_id INTEGER,
    p_musician_id INTEGER
) AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM user_musician_subscription 
               WHERE user_id = p_user_id AND musician_id = p_musician_id) THEN
        RAISE EXCEPTION 'Подписка уже существует';
    END IF;
    
    INSERT INTO user_musician_subscription (user_id, musician_id)
    VALUES (p_user_id, p_musician_id);
    
    UPDATE musician
    SET subscribers = subscribers + 1
    WHERE id = p_musician_id;
    
    UPDATE app_user
    SET subscriptions = subscriptions + 1
    WHERE id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- Функция для модерации статей
CREATE OR REPLACE FUNCTION moderate_article(
    p_article_id INTEGER,
    p_accepted BOOLEAN,
    p_moderator_id INTEGER
) RETURNS BOOLEAN AS $$
DECLARE
    v_is_admin BOOLEAN;
BEGIN
    SELECT is_admin INTO v_is_admin
    FROM app_user
    WHERE id = p_moderator_id;
    
    IF NOT v_is_admin THEN
        RAISE EXCEPTION 'Недостаточно прав для модерации';
    END IF;
    
    UPDATE articles
    SET accepted = p_accepted
    WHERE id = p_article_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;