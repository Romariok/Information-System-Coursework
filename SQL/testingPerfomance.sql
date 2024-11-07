-- Time before: 12,644
-- Time after: 3,285
EXPLAIN ANALYZE 
SELECT * FROM product WHERE brand_id = 3 AND price < 500 AND rate >= 4;

-- Time before: 16,621
-- Time after: 1,174
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE price BETWEEN 300 AND 1000 
  AND rate >= 4 
  AND brand_id = 5;

-- Time before: 9,648
-- Time after: 2,197
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE type_of_product_id = 2
  AND guitar_form_id = 3;

-- Time before: 20,779
-- Time after: 19,325
EXPLAIN ANALYZE 
SELECT m.name, COUNT(s.user_id) AS subscriber_count
FROM musician m
JOIN user_musician_subscription s ON m.id = s.musician_id
GROUP BY m.name
ORDER BY subscriber_count DESC
LIMIT 10;

-- Time before: 17,258
-- Time after: 1,768
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE brand_id = 5 
  AND guitar_form_id = 3 
  AND type_of_product_id = 2 
  AND price BETWEEN 500 AND 1500;

-- Time before: 0,676
-- Time after: 0,644
EXPLAIN ANALYZE 
SELECT p.* 
FROM product p
JOIN product_user uf ON p.id = uf.product_id
WHERE uf.user_id = 123;

-- Time before: 114,516
-- Time after: 72,256
-- Time after `idx_product_genre_genre_id`: 56,293
EXPLAIN ANALYZE 
SELECT p.* 
FROM product p
JOIN product_genre pg ON p.id = pg.product_id
WHERE pg.genre_id = 2;

-- Time before: 4,712
-- Time after: 0,195
EXPLAIN ANALYZE 
SELECT * FROM product WHERE brand_id = 3 AND price < 500 AND rate >= 4;