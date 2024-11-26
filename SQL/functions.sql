-- Процедура добавления отзыва с обновлением рейтинга продукта
CREATE OR REPLACE PROCEDURE add_product_feedback(
    p_user_id BIGINT,
    p_product_id BIGINT,
    p_text TEXT,
    p_stars INTEGER
) AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM product WHERE id = p_product_id) THEN
        RAISE EXCEPTION 'Продукт не найден';
    END IF;
    
    INSERT INTO feedback (author_id, product_id, text, stars, created_at)
    VALUES (p_user_id, p_product_id, p_text, p_stars, CURRENT_TIMESTAMP);

    UPDATE product
    SET rate = (
        SELECT ROUND(AVG(stars)::numeric(2,1), 1)
        FROM feedback
        WHERE product_id = p_product_id
    )
    WHERE id = p_product_id;
END;
$$ LANGUAGE plpgsql;

-- Процедура добавления отзыва статье
CREATE OR REPLACE PROCEDURE add_article_feedback(
    p_user_id BIGINT,
    p_article_id BIGINT,
    p_text TEXT,
    p_stars INTEGER
) AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM articles WHERE id = p_article_id) THEN
        RAISE EXCEPTION 'Статья не найдена';
    END IF;
    
    INSERT INTO feedback (author_id, article_id, text, stars, created_at)
    VALUES (p_user_id, p_article_id, p_text, p_stars, CURRENT_TIMESTAMP);
END;
$$ LANGUAGE plpgsql;

-- Процедура подписки на музыканта
CREATE OR REPLACE PROCEDURE subscribe_to_musician(
    p_user_id BIGINT,
    p_musician_id BIGINT
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
    p_article_id BIGINT,
    p_accepted BOOLEAN,
    p_moderator_id BIGINT
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