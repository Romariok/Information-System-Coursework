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

CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    is_admin BOOLEAN DEFAULT FALSE,
    login VARCHAR(100) UNIQUE NOT NULL,
    password TEXT NOT NULL,
    password_salt TEXT NOT NULL,
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
    CONSTRAINT fk_articles_user FOREIGN KEY (author) REFERENCES "user" (id) ON DELETE SET NULL
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
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);

CREATE TABLE user_musician_subscription (
    user_id INTEGER,
    musician_id INTEGER,
    CONSTRAINT pk_user_musician PRIMARY KEY (user_id, musician_id),
    CONSTRAINT fk_user_musician_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
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
    CONSTRAINT fk_type_of_musician_user_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE TABLE type_of_musician_musician (
    type_of_musician_id INTEGER,
    musician_id INTEGER,
    CONSTRAINT pk_type_of_musician_musician PRIMARY KEY (type_of_musician_id, musician_id),
    CONSTRAINT fk_type_of_musician_musician_type_of_musician FOREIGN KEY (type_of_musician_id) REFERENCES type_of_musician (id) ON DELETE CASCADE,
    CONSTRAINT fk_type_of_musician_musician_musician FOREIGN KEY (musician_id) REFERENCES musician (id) ON DELETE CASCADE
);

CREATE TABLE genre_user (
    genre_id INTEGER,
    user_id INTEGER,
    CONSTRAINT pk_genre_user PRIMARY KEY (genre_id, user_id),
    CONSTRAINT fk_genre_user_genre FOREIGN KEY (genre_id) REFERENCES genre (id) ON DELETE CASCADE,
    CONSTRAINT fk_genre_user_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
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
    CONSTRAINT fk_product_user_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE TABLE product_articles (
    product_id INTEGER,
    article_id INTEGER,
    CONSTRAINT pk_product_articles PRIMARY KEY (product_id, article_id),
    CONSTRAINT fk_product_articles_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_articles_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);
