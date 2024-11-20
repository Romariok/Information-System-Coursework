-- Time before: 73,711
-- Time after: 1,993
-- Retrieve products of a specific type with additional filters like color, average price range, and rating.
EXPLAIN ANALYZE 
SELECT 
    p.id, p.name, p.avg_price, p.rate, p.color, p.type_of_product 
FROM 
    product p
WHERE 
    p.type_of_product = 'ELECTRIC_GUITAR'
    AND p.color = 'RED'
    AND p.avg_price BETWEEN 500 AND 1500
    AND p.rate >= 4.0
ORDER BY 
    p.avg_price ASC;


-- Time before: 0,348
-- Time after: 0,089
-- Retrieve a list of shops where a specific product is available, along with the price.
EXPLAIN ANALYZE 
SELECT 
    s.id AS shop_id, s.name AS shop_name, sp.price, sp.available
FROM 
    shop_product sp
    JOIN shop s ON sp.shop_id = s.id
WHERE 
    (sp.product_id BETWEEN 10 and 50)
    AND sp.available = TRUE
ORDER BY 
    sp.price ASC;

-- Time before: 2,559
-- Time after: 2,354
-- Retrieve musicians with their genres and subscriber counts, sorted by popularity.
EXPLAIN ANALYZE 
SELECT 
    m.id AS musician_id, 
    m.name AS musician_name, 
    m.subscribers, 
    STRING_AGG(mg.genre::TEXT, ', ') AS genres
FROM 
    musician m
    JOIN musician_genre mg ON m.id = mg.musician_id
GROUP BY 
    m.id
ORDER BY 
    m.subscribers DESC;

-- Time before: 0,027
-- Time after: 0,026
-- Find products associated with a specific musician
EXPLAIN ANALYZE 
SELECT 
    p.id AS product_id, 
    p.name AS product_name, 
    p.avg_price, 
    p.rate
FROM 
    musician_product mp
    JOIN product p ON mp.product_id = p.id
WHERE 
    mp.musician_id = 5;

-- Time before: 0,053
-- Time after: 0,044
-- Retrieve all articles related to a specific product, along with their authors.
EXPLAIN ANALYZE 
SELECT 
    a.id AS article_id, 
    a.header AS article_title, 
    a.text AS article_content, 
    u.username AS author_name, 
    a.created_at
FROM 
    product_articles pa
    JOIN articles a ON pa.article_id = a.id
    JOIN app_user u ON a.author_id = u.id
WHERE 
    pa.product_id = 2
ORDER BY 
    a.created_at DESC;

-- Time before: 0,447
-- Time after: 0,026
-- Retrieve user feedback for a specific product, including the rating and feedback text.
EXPLAIN ANALYZE 
SELECT 
    f.id AS feedback_id, 
    u.username AS author_name, 
    f.stars, 
    f.text, 
    f.created_at
FROM 
    feedback f
    JOIN app_user u ON f.author_id = u.id
WHERE 
    f.product_id = 3
ORDER BY 
    f.created_at DESC;

-- Time before: 0,035
-- Time after: 0,032
-- Retrieve the list of musicians a user is subscribed to.
EXPLAIN ANALYZE 
SELECT 
    m.id AS musician_id, 
    m.name AS musician_name, 
    m.subscribers
FROM 
    user_musician_subscription ums
    JOIN musician m ON ums.musician_id = m.id
WHERE 
    ums.user_id = 4
ORDER BY 
    m.subscribers DESC;

-- Time before: 0,034
-- Time after: 0,026
-- Forum topics created by a user
EXPLAIN ANALYZE 
SELECT 
    ft.id AS topic_id, 
    ft.title, 
    ft.description, 
    ft.created_at, 
    ft.is_closed
FROM 
    forum_topic ft
WHERE 
    ft.author_id = 4
ORDER BY 
    ft.created_at DESC;


-- Time before: 16,732
-- Time after: 2,834
-- Top-rated products across all types
EXPLAIN ANALYZE 
SELECT 
    p.id AS product_id, 
    p.name AS product_name, 
    p.rate, 
    p.avg_price, 
    p.type_of_product
FROM 
    product p
WHERE 
    p.rate >= 4.5
ORDER BY 
    p.rate DESC, p.avg_price ASC;


-- Lists all users, their subscribed musicians, products they gave feedback on,
-- articles they wrote feedback for, shops selling those products, and the musician's genre.

-- Time before: 171,304
-- Time after: 128,234
EXPLAIN ANALYZE
SELECT 
    au.username AS user_name,
    m.name AS musician_name,
    m.subscribers AS musician_subscribers,
    p.name AS product_name,
    p.type_of_product AS product_type,
    p.avg_price AS average_price,
    f.text AS feedback_text,
    f.stars AS feedback_rating,
    a.header AS article_title,
    a.accepted AS article_status,
    s.name AS shop_name,
    sp.price AS shop_price,
    g.genre AS musician_genre
FROM app_user au
-- Join with user-music subscription
LEFT JOIN user_musician_subscription ums 
    ON au.id = ums.user_id
LEFT JOIN musician m 
    ON ums.musician_id = m.id
-- Join with feedback
LEFT JOIN feedback f 
    ON f.author_id = au.id
LEFT JOIN product p 
    ON f.product_id = p.id
LEFT JOIN articles a 
    ON f.article_id = a.id
-- Join product with shop_product
LEFT JOIN shop_product sp 
    ON p.id = sp.product_id
LEFT JOIN shop s 
    ON sp.shop_id = s.id
-- Join musician with musician_genre
LEFT JOIN musician_genre mg 
    ON m.id = mg.musician_id
LEFT JOIN genre_user g 
    ON mg.genre = g.genre AND g.user_id = au.id
WHERE 
    p.avg_price > 500 -- Filter for products with average price > 500
    AND f.stars >= 4 -- Filter for highly rated feedback
    AND a.accepted = TRUE -- Filter for accepted articles
ORDER BY 
    au.username, p.avg_price DESC;
