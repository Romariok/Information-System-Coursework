-- Функция для добавления нового пользователя с проверками
CREATE OR REPLACE FUNCTION create_user(
    p_login VARCHAR(100),
    p_password TEXT,
    p_is_admin BOOLEAN DEFAULT FALSE
) RETURNS INTEGER AS $$
DECLARE
    v_user_id INTEGER;
    v_salt TEXT;
BEGIN
    IF EXISTS (SELECT 1 FROM "user" WHERE login = p_login) THEN
        RAISE EXCEPTION 'Пользователь с таким логином уже существует';
    END IF;
    
    v_salt := md5(random()::text);
    
    INSERT INTO "user" (login, password, password_salt, is_admin, type_of_user_id)
    VALUES (p_login, md5(p_password || v_salt), v_salt, p_is_admin, 
            CASE WHEN p_is_admin THEN 2 ELSE 1 END)
    RETURNING id INTO v_user_id;
    
    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql;

-- Функция аутентификации пользователя
CREATE OR REPLACE FUNCTION authenticate_user(
    p_login VARCHAR(100),
    p_password TEXT
) RETURNS BOOLEAN AS $$
DECLARE
    v_stored_password TEXT;
    v_salt TEXT;
BEGIN
    SELECT password, password_salt INTO v_stored_password, v_salt
    FROM "user"
    WHERE login = p_login;
    
    IF v_stored_password IS NULL THEN
        RETURN FALSE;
    END IF;
    
    RETURN v_stored_password = md5(p_password || v_salt);
END;
$$ LANGUAGE plpgsql;

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
        SELECT ROUND(AVG(stars)::numeric, 1)
        FROM feedback
        WHERE product_id = p_product_id
    )
    WHERE id = p_product_id;
END;
$$ LANGUAGE plpgsql;

-- Функция поиска продуктов по различным критериям
CREATE OR REPLACE FUNCTION search_products(
    p_name VARCHAR(100) DEFAULT NULL,
    p_min_price NUMERIC DEFAULT NULL,
    p_max_price NUMERIC DEFAULT NULL,
    p_brand_id INTEGER DEFAULT NULL,
    p_type_id INTEGER DEFAULT NULL,
    p_min_rate NUMERIC DEFAULT NULL
) RETURNS TABLE (
    id INTEGER,
    name VARCHAR(100),
    price NUMERIC(10,2),
    rate NUMERIC(2,1),
    brand_name VARCHAR(100)
) AS $$
BEGIN
    RETURN QUERY
    SELECT p.id, p.name, p.price, p.rate, b.name as brand_name
    FROM product p
    LEFT JOIN brand b ON p.brand_id = b.id
    WHERE (p_name IS NULL OR p.name ILIKE '%' || p_name || '%')
    AND (p_min_price IS NULL OR p.price >= p_min_price)
    AND (p_max_price IS NULL OR p.price <= p_max_price)
    AND (p_brand_id IS NULL OR p.brand_id = p_brand_id)
    AND (p_type_id IS NULL OR p.type_of_product_id = p_type_id)
    AND (p_min_rate IS NULL OR p.rate >= p_min_rate);
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
    
    UPDATE "user"
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
    FROM "user"
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