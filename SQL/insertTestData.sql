-- Добавление брендов
INSERT INTO brand (name, country, website, email)
SELECT 
    'Brand ' || generate_series,
    (SELECT enum_range(NULL::country_enum))[floor(random() * array_length(enum_range(NULL::country_enum), 1) + 1)],
    'http://www.brand' || generate_series || '.com',
    'contact@brand' || generate_series || '.com'
FROM generate_series(1, 100);


-- Добавление музыкантов
INSERT INTO musician (name, subscribers)
SELECT 
    'Musician ' || generate_series,
    floor(random() * 1000000)::int
FROM generate_series(1, 1300);

-- Добавление пользователей
INSERT INTO app_user (username, password, is_admin, subscriptions)
SELECT 
    'user' || generate_series,
    md5(random()::text),
    (ARRAY[TRUE, FALSE])[floor(random() * 2 + 1)],
    floor(random() * 1000)::int
FROM generate_series(1, 13000);

-- Добавление продуктов
INSERT INTO product (name, description, brand_id, guitar_form, type_of_product, 
    lads, color, strings, tip_material, body_material, pickup_configuration, type_combo_amplifier)
SELECT 
    'Product ' || generate_series,
    'Description for product ' || generate_series,
    floor(random() * 100 + 1)::int,
    (SELECT enum_range(NULL::guitar_form_enum))[floor(random() * array_length(enum_range(NULL::guitar_form_enum), 1) + 1)],
    (SELECT enum_range(NULL::type_of_product_enum))[floor(random() * array_length(enum_range(NULL::type_of_product_enum), 1) + 1)],
    floor(random() * 24 + 1)::int,
    (SELECT enum_range(NULL::color_enum))[floor(random() * array_length(enum_range(NULL::color_enum), 1) + 1)],
    floor(random() * 12 + 1)::int,
    (SELECT enum_range(NULL::tip_material_enum))[floor(random() * array_length(enum_range(NULL::tip_material_enum), 1) + 1)],
    (SELECT enum_range(NULL::body_material_enum))[floor(random() * array_length(enum_range(NULL::body_material_enum), 1) + 1)],
    (SELECT enum_range(NULL::pickup_configuration_enum))[floor(random() * array_length(enum_range(NULL::pickup_configuration_enum), 1) + 1)],
    (SELECT enum_range(NULL::type_combo_amplifier_enum))[floor(random() * array_length(enum_range(NULL::type_combo_amplifier_enum), 1) + 1)]
FROM generate_series(1, 130000);

-- Добавление статей
INSERT INTO articles (header, text, author_id, created_at, accepted)
SELECT 
    'Article Header ' || generate_series,
    'Article Text ' || generate_series,
    floor(random() * 13000 + 1)::int,
    current_date - (random() * 365)::int,
    random() > 0.5
FROM generate_series(1, 1000);

-- Добавление отзывов
INSERT INTO feedback (author_id, product_id, article_id, text, stars, created_at)
SELECT 
    floor(random() * 13000 + 1)::int,
    floor(random() * 130000 + 1)::int,
    floor(random() * 1000 + 1)::int,
    'Feedback text ' || generate_series,
    floor(random() * 5 + 1)::int,
    current_date - (random() * 365)::int
FROM generate_series(1, 10000);

-- Добавление магазинов
INSERT INTO shop (name, description, website, email, address)
SELECT 
    'Shop ' || generate_series,
    'Description for shop ' || generate_series,
    'http://shop' || generate_series || '.com',
    'shop' || generate_series || '@example.com',
    'Address ' || generate_series
FROM generate_series(1, 300);

-- Добавление продуктов в магазины
INSERT INTO shop_product (shop_id, product_id, price, available)
SELECT 
    s.id,
    p.id,
    (random() * 10000)::numeric(10,2),
    (ARRAY[TRUE, FALSE])[floor(random() * 2 + 1)]
FROM product p
CROSS JOIN LATERAL (
    SELECT id
    FROM shop
    ORDER BY random()
    LIMIT floor(random() * 4 + 1)
) s;

-- Добавление тем форума
INSERT INTO forum_topic (title, description, author_id, is_closed)
SELECT 
    'Forum Topic ' || generate_series,
    'Description for forum topic ' || generate_series,
    floor(random() * 13000 + 1)::int,
    (ARRAY[TRUE, FALSE])[floor(random() * 2 + 1)]
FROM generate_series(1, 200);

-- Добавление постов на форуме
INSERT INTO forum_post (topic_id, content, author_id)
SELECT 
    floor(random() * 200 + 1)::int,
    'Content for forum post ' || generate_series,
    floor(random() * 13000 + 1)::int
FROM generate_series(1, 2500);



-- Добавление связей продуктов и статей
INSERT INTO
    product_articles (product_id, article_id)
SELECT p.id, a.id
FROM product p
    CROSS JOIN LATERAL (
        SELECT id
        FROM articles
        WHERE
            id NOT IN(
                SELECT article_id
                FROM product_articles
                WHERE
                    product_id = p.id
            )
        ORDER BY random ()
        LIMIT floor(random () * 4 + 1)
    ) a;

-- Добавление связей продуктов и пользователей
INSERT INTO
    product_user (product_id, user_id)
SELECT p.id, u.id
FROM app_user u
    CROSS JOIN LATERAL (
        SELECT id
        FROM product
        WHERE
            id NOT IN(
                SELECT product_id
                FROM product_user
                WHERE
                    user_id = u.id
            )
        ORDER BY random ()
        LIMIT floor(random () * 4 + 2)
    ) p;

-- Добавление связей музыкантов и продуктов
INSERT INTO
    musician_product (musician_id, product_id)
SELECT m.id, p.id
FROM musician m
    CROSS JOIN LATERAL (
        SELECT id
        FROM product
        WHERE
            id NOT IN(
                SELECT product_id
                FROM musician_product
                WHERE
                    musician_id = m.id
            )
        ORDER BY random ()
        LIMIT floor(random () * 4 + 2)
    ) p;

-- Добавление связей жанров и пользователей
INSERT INTO
    genre_user (genre, user_id)
SELECT g.genre, u.id
FROM app_user u
    CROSS JOIN LATERAL (
        SELECT genre
        FROM unnest(enum_range(NULL::genre_enum)) AS g(genre)
        WHERE
            genre NOT IN (
                SELECT gu.genre
                FROM genre_user gu
                WHERE gu.user_id = u.id
            )
        ORDER BY random()
        LIMIT floor(random() * 2 + 2)
    ) g;

-- Добавление связей типов музыкантов и пользователей
INSERT INTO
    type_of_musician_user (type_of_musician, user_id)
SELECT t.type_of_musician, u.id
FROM app_user u
    CROSS JOIN LATERAL (
        SELECT type_of_musician
        FROM unnest(enum_range(NULL::type_of_musician_enum)) AS t(type_of_musician)
        WHERE
            type_of_musician NOT IN (
                SELECT tum.type_of_musician
                FROM type_of_musician_user tum
                WHERE tum.user_id = u.id
            )
        ORDER BY random()
        LIMIT floor(random() * 2 + 2)
    ) t;


-- Добавление связей продуктов и жанров
INSERT INTO
    product_genre (product_id, genre)
SELECT p.id, g.genre
FROM product p
    CROSS JOIN LATERAL (
        SELECT genre
        FROM unnest(enum_range(NULL::genre_enum)) AS g(genre)
        WHERE
            genre NOT IN (
                SELECT pg.genre
                FROM product_genre pg
                WHERE pg.product_id = p.id
            )
        ORDER BY random()
        LIMIT floor(random() * 2 + 2)
    ) g;

-- Добавление связей музыкантов и жанров
INSERT INTO
    musician_genre (musician_id, genre)
SELECT m.id, g.genre
FROM musician m
    CROSS JOIN LATERAL (
        SELECT genre
        FROM unnest(enum_range(NULL::genre_enum)) AS g(genre)
        WHERE
            genre NOT IN (
                SELECT mg.genre
                FROM musician_genre mg
                WHERE mg.musician_id = m.id
            )
        ORDER BY random()
        LIMIT floor(random() * 2 + 2)
    ) g;

-- Добавление подписок пользователей на музыкантов
INSERT INTO
    user_musician_subscription (user_id, musician_id)
SELECT u.id, m.id
FROM app_user u
    CROSS JOIN LATERAL (
        SELECT id
        FROM musician
        WHERE
            id NOT IN(
                SELECT musician_id
                FROM user_musician_subscription
                WHERE
                    user_id = u.id
            )
        ORDER BY random ()
        LIMIT floor(random () * 5 + 1)
    ) m;

-- Добавление связей типов музыкантов и музыкантов
INSERT INTO
    type_of_musician_musician (
        type_of_musician,
        musician_id
    )
SELECT t.type_of_musician, m.id
FROM musician m
    CROSS JOIN LATERAL (
        SELECT type_of_musician
        FROM unnest(enum_range(NULL::type_of_musician_enum)) AS t(type_of_musician)
        WHERE
            type_of_musician NOT IN (
                SELECT type_of_musician
                FROM type_of_musician_musician
                WHERE musician_id = m.id
            )
        ORDER BY random()
        LIMIT floor(random() * 3 + 1)
    ) t;
