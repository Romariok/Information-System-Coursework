<div align="center">

## Министерство науки и высшего образования Российской Федерации
## Федеральное государственное автономное образовательное учреждение высшего образования
## «Национальный исследовательский университет ИТМО»
## Факультет программной инженерии и компьютерной техники
**Отчёт по курсовой работе**

**2 этап**

по дисциплине
"Информационные системы"
</div>

<div align="right" style="margin-top: 50px;">
<b>Выполнили студенты группы P3312:</b>

Кобелев Роман Павлович
Балин Артем Алексеевич

<b>Преподаватель:</b>
Бострикова Дарья Константиновна
</div>






<div align="center" style="margin-top: 200px; text: center">
г. Санкт-Петербург

2024г.
</div>

<div style="page-break-after: always;"></div>


## Содержание

- [Сущности](#сущности)
  - [Бренд (brand)](#бренд-brand)
  - [Музыкант (musician)](#музыкант-musician)
  - [Пользователь (app\_user)](#пользователь-app_user)
  - [Продукт (product)](#продукт-product)
  - [Статьи (articles)](#статьи-articles)
  - [Отзывы (feedback)](#отзывы-feedback)
  - [Форумная тема (forum\_topic)](#форумная-тема-forum_topic)
  - [Форумный пост (forum\_post)](#форумный-пост-forum_post)
  - [Магазин (shop)](#магазин-shop)
  - [Продукт магазина (shop\_product)](#продукт-магазина-shop_product)
- [Целостность данных](#целостность-данных)
  - [Триггеры](#триггеры)
- [SQL-скрипты](#sql-скрипты)
- [Функции и процедуры](#функции-и-процедуры)
- [ER-модель](#er-модель)
- [Даталогическая модель](#даталогическая-модель)
- [Тестирование производительности, создание индексов](#тестирование-производительности-создание-индексов)

## Сущности

### Бренд (brand)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | TEXT | NOT NULL | - |
| country | country_enum | NOT NULL | - |
| website | TEXT | - | - |
| email | TEXT | - | - |

### Музыкант (musician)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | TEXT | NOT NULL | - |
| subscribers | INTEGER | NOT NULL CHECK (subscribers >= 0) DEFAULT 0 | - |

### Пользователь (app_user)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| is_admin | BOOLEAN | DEFAULT FALSE | - |
| username | TEXT | UNIQUE NOT NULL | - |
| password | TEXT | NOT NULL | - |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | - |
| subscriptions | INTEGER | CHECK (subscriptions >= 0) DEFAULT 0 | - |

### Продукт (product)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | TEXT | NOT NULL | - |
| description | TEXT | - | - |
| rate | NUMERIC(2,1) | CHECK (rate >= 0 AND rate <= 5) DEFAULT 0 | - |
| brand_id | INTEGER | - | brand(id) |
| guitar_form | guitar_form_enum | - | - |
| type_of_product | type_of_product_enum | NOT NULL | - |
| lads | INTEGER | - | - |
| avg_price | NUMERIC(10,2) | CHECK (avg_price >= 0) DEFAULT 0 | - |
| color | color_enum | NOT NULL | - |
| strings | INTEGER | - | - |
| tip_material | tip_material_enum | - | - |
| body_material | body_material_enum | - | - |
| pickup_configuration | pickup_configuration_enum | - | - |
| type_combo_amplifier | type_combo_amplifier_enum | - | - |

### Статьи (articles)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| header | TEXT | NOT NULL | - |
| text | TEXT | NOT NULL | - |
| author_id | INTEGER | - | app_user(id) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | - |
| accepted | BOOLEAN | DEFAULT FALSE | - |

### Отзывы (feedback)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| author_id | INTEGER | NOT NULL | app_user(id) |
| product_id | INTEGER | - | product(id) |
| article_id | INTEGER | - | articles(id) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | - |
| text | TEXT | NOT NULL | - |
| stars | INTEGER | CHECK (stars >= 0 AND stars <= 5) | - |

### Форумная тема (forum_topic)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| title | TEXT | NOT NULL | - |
| description | TEXT | - | - |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | - |
| author_id | INTEGER | - | app_user(id) |
| is_closed | BOOLEAN | DEFAULT FALSE | - |

### Форумный пост (forum_post)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| topic_id | INTEGER | NOT NULL | forum_topic(id) |
| content | TEXT | NOT NULL | - |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | - |
| author_id | INTEGER | - | app_user(id) |
| vote | INTEGER | DEFAULT 0 | - |

### Магазин (shop)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | TEXT | NOT NULL | - |
| description | TEXT | - | - |
| website | TEXT | - | - |
| email | TEXT | - | - |
| address | TEXT | - | - |

### Продукт магазина (shop_product)

| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| shop_id | INTEGER | NOT NULL | shop(id) |
| product_id | INTEGER | NOT NULL | product(id) |
| price | NUMERIC(10,2) | NOT NULL CHECK (price >= 0) | - |
| available | BOOLEAN | DEFAULT TRUE | - |

## Целостность данных

Целостность данных обеспечивается через ограничения при создании сущностей в базе данных.

```sql
CONSTRAINT fk_... FOREIGN KEY (..._id) REFERENCES ... (id) ON DELETE SET NULL
```

```sql
CONSTRAINT fk_... FOREIGN KEY (..._id) REFERENCES ... (id) ON DELETE CASCADE
```

```sql
is_admin BOOLEAN DEFAULT FALSE
```

### Триггеры

- Изменение среднего рейтинга продукта: AFTER INSERT OR UPDATE OR DELETE ON feedback

```sql
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
```

- Изменение количества подписок пользователя: AFTER INSERT OR DELETE ON user_musician_subscription

```sql
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

```

- Изменение количества подписчиков музыканта: AFTER INSERT OR DELETE ON user_musician_subscription

```sql
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

```

- Изменение средней цены продукта: AFTER INSERT OR UPDATE OR DELETE ON shop_product

```sql
CREATE OR REPLACE FUNCTION update_product_average_price()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        UPDATE product
        SET avg_price = (
            SELECT AVG(price)::numeric(10,2)
            FROM shop_product
            WHERE product_id = OLD.product_id AND available = true
        )
        WHERE id = OLD.product_id;
    ELSE
        UPDATE product
        SET avg_price = (
            SELECT AVG(price)::numeric(10,2)
            FROM shop_product
            WHERE product_id = NEW.product_id AND available = true
        )
        WHERE id = NEW.product_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

## SQL-скрипты

- [Создание базы данных, таблиц, функций ...](/SQL/initScript.sql)
- [Вставка тестовых данных](/SQL/insertTestData.sql)
- [Скрипт для удаления](/SQL/deleteAll.sql)

## Функции и процедуры

- Процедура добавления отзыва с обновлением рейтинга продукта
- Процедура подписки на музыканта
- Функция для модерации статей

[Функции](/SQL/functions.sql)

## ER-модель

![2 Этап](/SQL/IM.png)

## Даталогическая модель

![2 Этап](/SQL/erd.png)

## Тестирование производительности, создание индексов

### [Индексы](/SQL/creatingIndexes.sql)

### Поиск товаров определенного типа с помощью дополнительных фильтров, таких как цвет, средний ценовой диапазон и рейтинг

```sql
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
```

Было: 73,711 мс

Стало: 1,993 мс

### Получить список магазинов, в которых доступен конкретный товар, а также информацию о цене

```sql
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
```

Было: 0,348 мс

Стало: 0,089 мс

### Поиск музыкантов с указанием их жанров и количества подписчиков, отсортированных по популярности

```sql
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
```

Было: 2,559 мс

Стало: 2,354 мс

### Найти товары, связанные с конкретным музыкантом

```sql
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
```

Было: 0,027 мс

Стало: 0,026 мс

### Найти все статьи, относящиеся к конкретному продукту, вместе с их авторами

```sql
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
```

Было: 0,053 мс

Стало: 0,044 мс

### Получение отзывов пользователей о конкретном продукте, включая рейтинг и текст отзыва

```sql
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
```

Было: 0,447 мс

Стало: 0,026 мс

### Получить список музыкантов, на которых подписан пользователь

```sql
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
```

Было: 0,035 мс

Стало: 0,032 мс

### Получить темы форума, созданные пользователем

```sql
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
```

Было: 0,034 мс

Стало: 0,026 мс

### Поиск продуктов с самым высоким рейтингом по всем типам

```sql
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
```

Было: 16,732 мс

Стало: 2,834 мс

### Список всех пользователей, музыкантов, на которых они подписаны, продукты, на которые они оставили отзывы, статьи, на которые они написали отзывы, магазины, продающие эти продукты, и жанр музыканта

```sql
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
LEFT JOIN user_musician_subscription ums
ON au.id = ums.user_id
LEFT JOIN musician m
ON ums.musician_id = m.id
LEFT JOIN feedback f
ON f.author_id = au.id
LEFT JOIN product p
ON f.product_id = p.id
LEFT JOIN articles a
ON f.article_id = a.id
LEFT JOIN shop_product sp
ON p.id = sp.product_id
LEFT JOIN shop s
ON sp.shop_id = s.id
LEFT JOIN musician_genre mg
ON m.id = mg.musician_id
LEFT JOIN genre_user g
ON mg.genre = g.genre AND g.user_id = au.id
WHERE
p.avg_price > 500
AND f.stars >= 4
AND a.accepted = TRUE
ORDER BY
au.username, p.avg_price DESC;
```

Было: 171,304 мс

Стало: 128,234 мс
