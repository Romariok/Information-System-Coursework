CREATE TABLE type_of_musician (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE genre (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE brand (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE guitar_form (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE type_of_product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE musician (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    subscribers INTEGER NOT NULL
);

CREATE TABLE app_user (
    id SERIAL PRIMARY KEY,
    is_admin BOOLEAN DEFAULT FALSE,
    login VARCHAR(100) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    subscriptions INTEGER
);

CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    rate NUMERIC(2, 1),
    brand_id INTEGER,
    guitar_form_id INTEGER,
    type_of_product_id INTEGER,
    lads INTEGER,
    color VARCHAR(100) NOT NULL,
    strings INTEGER,
    tip_material VARCHAR(100),
    body_material VARCHAR(100),
    pickup_configuration VARCHAR(100),
    type_combo_amplifier VARCHAR(100),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_guitar_form FOREIGN KEY (guitar_form_id) REFERENCES guitar_form (id) ON DELETE SET NULL,
    CONSTRAINT fk_product_type_of_product FOREIGN KEY (type_of_product_id) REFERENCES type_of_product (id) ON DELETE SET NULL
);

CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    header VARCHAR(200) NOT NULL,
    text TEXT NOT NULL,
    author INTEGER,
    date DATE NOT NULL,
    tags VARCHAR(200),
    accepted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_articles_user FOREIGN KEY (author) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE TABLE feedback (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    product_id INTEGER,
    article_id INTEGER,
    text TEXT,
    stars INTEGER CHECK (
        stars >= 1
        AND stars <= 5
    ),
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
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
    genre_id INTEGER,
    CONSTRAINT pk_musician_genre PRIMARY KEY (musician_id, genre_id),
    CONSTRAINT fk_musician_genre_musician FOREIGN KEY (musician_id) REFERENCES musician (id) ON DELETE CASCADE,
    CONSTRAINT fk_musician_genre_genre FOREIGN KEY (genre_id) REFERENCES genre (id) ON DELETE CASCADE
);

CREATE TABLE product_genre (
    product_id INTEGER,
    genre_id INTEGER,
    CONSTRAINT pk_product_genre PRIMARY KEY (product_id, genre_id),
    CONSTRAINT fk_product_genre_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_genre_genre FOREIGN KEY (genre_id) REFERENCES genre (id) ON DELETE CASCADE
);

CREATE TABLE type_of_musician_user (
    type_of_musician_id INTEGER,
    user_id INTEGER,
    CONSTRAINT pk_type_of_musician_user PRIMARY KEY (type_of_musician_id, user_id),
    CONSTRAINT fk_type_of_musician_user_type_of_musician FOREIGN KEY (type_of_musician_id) REFERENCES type_of_musician (id) ON DELETE CASCADE,
    CONSTRAINT fk_type_of_musician_user_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

CREATE TABLE type_of_musician_musician (
    type_of_musician_id INTEGER,
    musician_id INTEGER,
    CONSTRAINT pk_type_of_musician_musician PRIMARY KEY (
        type_of_musician_id,
        musician_id
    ),
    CONSTRAINT fk_type_of_musician_musician_type_of_musician FOREIGN KEY (type_of_musician_id) REFERENCES type_of_musician (id) ON DELETE CASCADE,
    CONSTRAINT fk_type_of_musician_musician_musician FOREIGN KEY (musician_id) REFERENCES musician (id) ON DELETE CASCADE
);

CREATE TABLE genre_user (
    genre_id INTEGER,
    user_id INTEGER,
    CONSTRAINT pk_genre_user PRIMARY KEY (genre_id, user_id),
    CONSTRAINT fk_genre_user_genre FOREIGN KEY (genre_id) REFERENCES genre (id) ON DELETE CASCADE,
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

INSERT INTO
    type_of_musician (name)
VALUES ('MUSICAL PRODUCER'),
    ('GUITARIST'),
    ('DRUMMER'),
    ('BASSIST'),
    ('SINGER'),
    ('RAPPER'),
    ('KEYBOARDIST');

INSERT INTO
    genre (name)
VALUES ('BLUES'),
    ('ROCK'),
    ('POP'),
    ('JAZZ'),
    ('RAP'),
    ('METAL'),
    ('CLASSICAL'),
    ('REGGAE'),
    ('ELECTRONIC'),
    ('HIP-HOP');

INSERT INTO
    brand (name)
VALUES ('FENDER'),
    ('GIBSON'),
    ('YAMAHA'),
    ('IBANEZ'),
    ('ESP'),
    ('PRS'),
    ('SQUIER'),
    ('MARTIN'),
    ('COLLINGS'),
    ('TAYLOR');

INSERT INTO
    guitar_form (name)
VALUES ('STRATOCASTER'),
    ('TELECASTER'),
    ('THINLINE'),
    ('LES PAUL'),
    ('V'),
    ('JAGUAR'),
    ('JAZZ-MASTER'),
    ('MOCKINGBIRD'),
    ('STAR'),
    ('MUSTANG');

INSERT INTO
    type_of_product (name)
VALUES ('PEDALS_AND_EFFECTS'),
    ('ELECTRIC_GUITAR'),
    ('STUDIO_RECORDING_GEAR'),
    ('KEYS_AND_MIDI'),
    ('AMPLIFIER'),
    ('DRUMS_AND_PERCUSSION'),
    ('BASS_GUITAR'),
    ('ACOUSTIC_GUITAR'),
    ('SOFTWARE_AND_ACCESSORIES');