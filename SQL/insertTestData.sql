-- Добавление музыкантов
INSERT INTO musician (name, subscribers, genre_id, type_of_musician_id)
SELECT 
    'Musician ' || generate_series,
    floor(random() * 1000000)::int,
    floor(random() * 10 + 1)::int,
    floor(random() * 7 + 1)::int
FROM generate_series(1, 130);

-- Добавление пользователей
INSERT INTO "user" (login, password, password_salt, is_admin, type_of_user_id, subscriptions)
SELECT 
    'user' || generate_series,
    md5(random()::text),
    md5(random()::text),
    false,
    floor(random() * 2 + 1)::int,
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
