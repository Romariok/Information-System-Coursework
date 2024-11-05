-- Добавление музыкантов
INSERT INTO musician (name, subscribers)
SELECT 
    'Musician ' || generate_series,
    floor(random() * 1000000)::int
FROM generate_series(1, 130);

-- Добавление пользователей
INSERT INTO "user" (login, password, password_salt, is_admin, subscriptions)
SELECT 
    'user' || generate_series,
    md5(random()::text),
    md5(random()::text),
    (ARRAY[TRUE, FALSE])[floor(random() * 2 + 1)],
    floor(random() * 1000)::int
FROM generate_series(1, 130);

-- Добавление продуктов
INSERT INTO product (name, description, price, rate, brand_id, guitar_form_id, type_of_product_id, 
                    lads, strings, tip_material, body_material, pickup_configuration, type_combo_amplifier)
SELECT 
    'Product ' || generate_series,
    'Description for product ' || generate_series,
    (random() * 10000)::numeric(10,2),
    (random() * 4 + 1)::numeric(2,1),
    floor(random() * 10 + 1)::int,
    floor(random() * 10 + 1)::int,
    floor(random() * 9 + 1)::int,
    floor(random() * 24 + 1)::int,
    floor(random() * 12 + 1)::int,
    (ARRAY['Wood', 'Metal', 'Plastic', 'Composite'])[floor(random() * 4 + 1)],
    (ARRAY['Mahogany', 'Maple', 'Ash', 'Alder', 'Basswood'])[floor(random() * 5 + 1)],
    (ARRAY['SSS', 'HSS', 'HH', 'P90', 'Single'])[floor(random() * 5 + 1)],
    (ARRAY['Tube', 'Solid State', 'Hybrid', 'Modeling'])[floor(random() * 4 + 1)]а
FROM generate_series(1, 130);

-- Добавление статей
INSERT INTO articles (header, text, author, date, tags, accepted)
SELECT 
    'Article Header ' || generate_series,
    'Article Text ' || generate_series,
    floor(random() * 130 + 1)::int,
    current_date - (random() * 365)::int,
    'tag1, tag2, tag3',
    random() > 0.5
FROM generate_series(1, 130);

-- Добавление отзывов
INSERT INTO feedback (user_id, product_id, article_id, text, stars)
SELECT 
    floor(random() * 130 + 1)::int,
    floor(random() * 130 + 1)::int,
    floor(random() * 130 + 1)::int,
    'Feedback text ' || generate_series,
    floor(random() * 5 + 1)::int
FROM generate_series(1, 130);

-- Добавление связей продуктов и статей
INSERT INTO product_articles (product_id, article_id)
SELECT 
    p.id,
    a.id
FROM product p
CROSS JOIN LATERAL (
    SELECT id FROM articles
    WHERE id NOT IN (SELECT article_id FROM product_articles WHERE product_id = p.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) a;

-- Добавление связей продуктов и пользователей
INSERT INTO product_user (product_id, user_id)
SELECT 
    p.id,
    u.id 
FROM "user" u
CROSS JOIN LATERAL (
    SELECT id FROM product 
    WHERE id NOT IN (SELECT product_id FROM product_user WHERE user_id = u.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) p;

-- Добавление связей музыкантов и продуктов
INSERT INTO musician_product (musician_id, product_id)
SELECT 
    m.id,
    p.id
FROM musician m
CROSS JOIN LATERAL (
    SELECT id FROM product
    WHERE id NOT IN (SELECT product_id FROM musician_product WHERE musician_id = m.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) p;

-- Добавление связей жанров и пользователей
INSERT INTO genre_user (genre_id, user_id)
SELECT 
    g.id,
    u.id
FROM "user" u
CROSS JOIN LATERAL (
    SELECT id FROM genre
    WHERE id NOT IN (SELECT genre_id FROM genre_user WHERE user_id = u.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) g;

-- Добавление связей типов музыкантов и пользователей
INSERT INTO type_of_musician_user (type_of_musician_id, user_id)
SELECT 
    t.id,
    u.id
FROM "user" u
CROSS JOIN LATERAL (
    SELECT id FROM type_of_musician
    WHERE id NOT IN (SELECT type_of_musician_id FROM type_of_musician_user WHERE user_id = u.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) t;

-- Добавление связей продуктов и жанров
INSERT INTO product_genre (product_id, genre_id)
SELECT 
    p.id,
    g.id
FROM product p
CROSS JOIN LATERAL (
    SELECT id FROM genre
    WHERE id NOT IN (SELECT genre_id FROM product_genre WHERE product_id = p.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) g;

-- Добавление связей музыкантов и жанров
INSERT INTO musician_genre (musician_id, genre_id)
SELECT 
    m.id,
    g.id
FROM musician m
CROSS JOIN LATERAL (
    SELECT id FROM genre
    WHERE id NOT IN (SELECT genre_id FROM musician_genre WHERE musician_id = m.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) g;

-- Добавление подписок пользователей на музыкантов
INSERT INTO user_musician_subscription (user_id, musician_id)
SELECT 
    u.id,
    m.id
FROM "user" u
CROSS JOIN LATERAL (
    SELECT id FROM musician
    WHERE id NOT IN (SELECT musician_id FROM user_musician_subscription WHERE user_id = u.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) m;

-- Добавление связей типов музыкантов и музыкантов
INSERT INTO type_of_musician_musician (type_of_musician_id, musician_id)
SELECT 
    t.id,
    m.id
FROM musician m
CROSS JOIN LATERAL (
    SELECT id FROM type_of_musician
    WHERE id NOT IN (SELECT type_of_musician_id FROM type_of_musician_musician WHERE musician_id = m.id)
    ORDER BY random()
    LIMIT floor(random() * 4 + 2)
) t;