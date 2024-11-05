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
  - [Тип музыканта (type\_of\_musician)](#тип-музыканта-type_of_musician)
  - [Жанр (genre)](#жанр-genre)
  - [Бренд (brand)](#бренд-brand)
  - [Форма гитары (guitar\_form)](#форма-гитары-guitar_form)
  - [Тип продукта (type\_of\_product)](#тип-продукта-type_of_product)
  - [Музыкант (musician)](#музыкант-musician)
  - [Пользователь (user)](#пользователь-user)
  - [Продукт (product)](#продукт-product)
  - [Статьи (articles)](#статьи-articles)
  - [Отзывы (feedback)](#отзывы-feedback)
- [Связи](#связи)
- [Целостность данных](#целостность-данных)
  - [Триггеры](#триггеры)
- [SQL-скрипты](#sql-скрипты)
- [Функции и процедуры](#функции-и-процедуры)
- [ER-модель](#er-модель)
- [Даталогическая модель](#даталогическая-модель)

## Сущности

### Тип музыканта (type_of_musician)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | VARCHAR(100) | NOT NULL | - |

### Жанр (genre)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | VARCHAR(100) | NOT NULL | - |

### Бренд (brand)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | VARCHAR(100) | NOT NULL | - |

### Форма гитары (guitar_form)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | VARCHAR(100) | NOT NULL | - |

### Тип продукта (type_of_product)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | VARCHAR(100) | NOT NULL | - |

### Музыкант (musician)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | VARCHAR(100) | NOT NULL | - |
| subscribers | INTEGER | NOT NULL | - |

### Пользователь (user)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| is_admin | BOOLEAN | DEFAULT FALSE | - |
| login | VARCHAR(100) | UNIQUE NOT NULL | - |
| password | TEXT | NOT NULL | - |
| password_salt | TEXT | NOT NULL | - |
| subscriptions | INTEGER | - | - |

### Продукт (product)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| name | VARCHAR(100) | NOT NULL | - |
| description | TEXT | - | - |
| price | NUMERIC(10,2) | NOT NULL | - |
| rate | NUMERIC(2,1) | - | - |
| brand_id | INTEGER | - | brand(id) |
| guitar_form_id | INTEGER | - | guitar_form(id) |
| type_of_product_id | INTEGER | - | type_of_product(id) |
| lads | INTEGER | - | - |
| strings | INTEGER | - | - |
| tip_material | VARCHAR(100) | - | - |
| body_material | VARCHAR(100) | - | - |
| pickup_configuration | VARCHAR(100) | - | - |
| type_combo_amplifier | VARCHAR(100) | - | - |

### Статьи (articles)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| header | VARCHAR(200) | NOT NULL | - |
| text | TEXT | NOT NULL | - |
| author | INTEGER | - | user(id) |
| date | DATE | NOT NULL | - |
| tags | VARCHAR(200) | - | - |
| accepted | BOOLEAN | DEFAULT FALSE | - |

### Отзывы (feedback)
| Название | Тип | Настройки | Ссылки |
|----------|-----|-----------|---------|
| id | SERIAL | PRIMARY KEY | - |
| user_id | INTEGER | - | user(id) |
| product_id | INTEGER | - | product(id) |
| article_id | INTEGER | - | articles(id) |
| text | TEXT | - | - |
| stars | INTEGER | CHECK (stars >= 1 AND stars <= 5) | - |

## Связи

- user-musician: M:M
- product-brand: M:1
- product-guitar_form: M:1
- product-type_of_product: M:1
- product-article: M:M
- articles-user: 1:M
- feedback-user: M:1
- feedback-product: M:1
- feedback-articles: M:1
- genre-product: M:M
- musician-genre: M:M
- user-genre: M:M
- user-type_of_musician: M:M
- musician-type_of_musician: M:M
- musician-product: M:M


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
        UPDATE "user"
        SET subscriptions = (
            SELECT COUNT(*) 
            FROM user_musician_subscription 
            WHERE user_id = NEW.user_id
        )
        WHERE id = NEW.user_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE "user"
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

## SQL-скрипты

- [initScript.sql](/SQL/initScript.sql)
- [insertTestData.sql](/SQL/insertTestData.sql)
- [deleteAll.sql](/SQL/deleteAll.sql)

## Функции и процедуры

- Процедура добавления отзыва с обновлением рейтинга продукта
- Процедура подписки на музыканта
- Функция для модерации статей

[functions.sql](/SQL/functions.sql)

## ER-модель

![2 Этап](/SQL/IM.png)

## Даталогическая модель

![2 Этап](/SQL/erd.png)
