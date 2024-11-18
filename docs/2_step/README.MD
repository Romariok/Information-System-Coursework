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

[Триггеры](/SQL/functions.sql)

- Триггеры по `id` для основных характеристик продукта

- Триггеры для связей `many-to-many` между пользователями, продуктами

- Запрос для фильтрации продукта по бренду и цене

```sql
EXPLAIN ANALYZE 
SELECT * FROM product WHERE brand_id = 3 AND price < 500 AND rate >= 4;
```

Было: 12,644 мс

Стало: 3,285 мс

- Фильтрация продукта по цене, рейтингу и бренду

```sql
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE price BETWEEN 300 AND 1000 
  AND rate >= 4 
  AND brand_id = 5;
```

Было: 16,621 мс

Стало: 1,174 мс

- Список определённого продукта (гитары) с определённой формой

```sql
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE type_of_product_id = 2
  AND guitar_form_id = 3;
```

Было: 9,648 мс

Стало: 2,197 мс

- Топ 10 музыкантов по подписчикам

```sql
EXPLAIN ANALYZE 
SELECT m.name, COUNT(s.user_id) AS subscriber_count
FROM musician m
JOIN user_musician_subscription s ON m.id = s.musician_id
GROUP BY m.name
ORDER BY subscriber_count DESC
LIMIT 10;
```

Было: 20,779 мс

Стало: 19,325 мс

- Фильтр гитары с определённо формой, ценой, брендом

```sql
EXPLAIN ANALYZE 
SELECT * 
FROM product 
WHERE brand_id = 5 
  AND guitar_form_id = 3 
  AND type_of_product_id = 2 
  AND price BETWEEN 500 AND 1500;
```

Было: 17,258 мс

Стало: 1,768 мс

- Список избранного для конкретного юзера

```sql
EXPLAIN ANALYZE 
SELECT p.* 
FROM product p
JOIN product_user uf ON p.id = uf.product_id
WHERE uf.user_id = 123;
```

Было: 0,676 мс

Стало: 0,644 мс

- Выбор гитары с определённым жанром

```sql
EXPLAIN ANALYZE 
SELECT p.* 
FROM product p
JOIN product_genre pg ON p.id = pg.product_id
WHERE pg.genre_id = 2;
```

Было: 114,516 мс

Стало: 72,256 мс

Стало (после добавления `idx_product_genre_genre_id`): 56,293

- Выбор продукта с определённым брендом и ценой

```sql
EXPLAIN ANALYZE 
SELECT * FROM product WHERE brand_id = 3 AND price < 500 AND rate >= 4;
```

Было: 4,712 мс

Стало: 0,195 мс
