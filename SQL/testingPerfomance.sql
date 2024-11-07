-- Time before: 12,6435
-- Time after: 
EXPLAIN ANALYZE 
SELECT * FROM product WHERE brand_id = 3 AND price < 500 AND rate >= 4;

-- Time before: 16,6208
-- Time after: 
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE price BETWEEN 300 AND 1000 
  AND rate >= 4 
  AND brand_id = 5;

-- Time before: 
-- Time after: 
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE type_of_product_id = 2
  AND guitar_form_id = 3;

-- Time before: 
-- Time after: 
EXPLAIN ANALYZE 
SELECT m.name, COUNT(s.user_id) AS subscriber_count
FROM musician m
JOIN user_musician_subscription s ON m.id = s.musician_id
GROUP BY m.name
ORDER BY subscriber_count DESC
LIMIT 10;

-- Time before: 
-- Time after: 
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE brand_id = 5 
  AND guitar_form_id = 3 
  AND type_of_product_id = 2 
  AND price BETWEEN 500 AND 1500;

-- Time before: 
-- Time after: 
EXPLAIN ANALYZE 
SELECT p.* 
FROM product p
JOIN user_favorites uf ON p.id = uf.product_id
WHERE uf.user_id = 123;

-- Time before: 
-- Time after: 
EXPLAIN ANALYZE 
SELECT p.* 
FROM product p
JOIN product_genres pg ON p.id = pg.product_id
WHERE pg.genre_id = 2;
