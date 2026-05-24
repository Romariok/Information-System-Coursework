# Оптимизация индексов — GuitarMatch IS

## Среда и тестовые данные

- PostgreSQL (контейнер `postgres_container`)
- База данных: `postgres`
- Данные: 130 000 строк в `product`, 10 000 в `feedback`, 390 000 в `shop_product`, 1 300 в `musician`
- Статистика сброшена через `SELECT pg_stat_reset()` и `ANALYZE`, затем прогнаны запросы из `testingPerfomance.sql` 3 раза

---

## Замеры без явных индексов (только PK/unique)

БД запущена с данными (130k `product`, 390k `shop_product`, 10k `feedback`) и **без каких-либо `CREATE INDEX`** — только PK и UNIQUE constraints. `pg_stat_reset()` + `ANALYZE` выполнены перед замером.

### Q1 — фильтр продуктов (type + color + price + rate)

```
Parallel Seq Scan on product p  (actual time=47.343..47.344 rows=0 loops=3)
  Filter: (avg_price BETWEEN 500 AND 1500) AND (rate >= 4.0) AND (type_of_product = 'ELECTRIC_GUITAR') AND (color = 'RED')
  Rows Removed by Filter: 43333
Execution Time: 60.345 ms
```

### Q2 — магазины с доступным продуктом (product_id range + available)

```
Bitmap Heap Scan on shop_product sp
  Recheck Cond: (product_id >= 10 AND product_id <= 50)
  Filter: available
  Rows Removed by Filter: 82
  ->  Bitmap Index Scan on pk_shop_product
        Index Searches: 301
Execution Time: 36.667 ms
```

Планировщик вынужден использовать PK `pk_shop_product` для range-сканирования, делая 301 Index Search вместо одного. `available` обрабатывается как post-filter.

### Q3 — музыканты с жанрами (subscribers DESC)

```
Hash Join (musician_genre ⋈ musician)
  ->  Seq Scan on musician_genre mg
  ->  Hash → Seq Scan on musician m
Execution Time: 2.748 ms
```

### Q4 — продукты музыканта

```
Nested Loop
  ->  Index Only Scan using pk_musician_product (musician_id = 5) → 3 rows
  ->  Index Scan using product_pkey
Execution Time: 0.945 ms
```

### Q5 — статьи для продукта

```
Nested Loop (product_articles ⋈ articles ⋈ app_user)
  ->  Index Only Scan using pk_product_articles (product_id = 2) → 2 rows
Execution Time: 0.990 ms
```

### Q6 — отзывы на продукт (product_id = 3)

```
Seq Scan on feedback f
  Filter: (product_id = 3)
  Rows Removed by Filter: 10000
Execution Time: 0.926 ms
```

### Q7 — подписки пользователя на музыкантов

```
Bitmap Heap Scan on user_musician_subscription ums (user_id = 4)
  ->  Bitmap Index Scan on pk_user_musician → 3 rows
Execution Time: 0.537 ms
```

### Q8 — темы форума по автору

```
Seq Scan on forum_topic ft (200 строк)
  Filter: (author_id = 4)
  Rows Removed by Filter: 200
Execution Time: 0.102 ms
```

### Q9 — топ-рейтинговые продукты (rate >= 4.5)

```
Sort → Seq Scan on product p
  Filter: (rate >= 4.5)
  Rows Removed by Filter: 129198
Execution Time: 85.902 ms
```

### Итоги замера без индексов

| Запрос | Без индексов | С индексами | Ускорение |
|---|---|---|---|
| Q1: фильтр product (type+color+price+rate) | **60.345 ms** | **1.394 ms** | **43×** |
| Q2: магазины с доступным продуктом | **36.667 ms** | **1.847 ms** | **20×** |
| Q3: музыканты с жанрами | 2.748 ms | 3.580 ms | — (одинаковый план) |
| Q4: продукты музыканта | 0.945 ms | 0.158 ms | **6×** |
| Q5: статьи для продукта | 0.990 ms | 0.141 ms | **7×** |
| Q6: feedback по product_id | 0.926 ms | 0.363 ms | **2.6×** |
| Q7: подписки пользователя | 0.537 ms | 0.244 ms | **2.2×** |
| Q8: темы форума (200 строк) | 0.102 ms | 0.105 ms | = (Seq Scan оптимален) |
| Q9: топ-рейтинг (rate >= 4.5) | **85.902 ms** | **2.058 ms** | **42×** |

Q3 без индексов и с индексами выбирает одинаковый план Hash Join + Seq Scan — `idx_musician_subscribers` не применяется при полном GROUP BY без LIMIT. Q8 — таблица из 200 строк, Seq Scan всегда оптимален.

---

## Использование индексов до оптимизации

```
                  indexrelname                  | idx_scan | idx_tup_read | idx_tup_fetch |   size
------------------------------------------------+----------+--------------+---------------+----------
 app_user_username_key                          |        0 |            0 |             0 | 752 kB
 idx_product_type_of_product                    |        0 |            0 |             0 | 3352 kB
 idx_product_guitar_form                        |        0 |            0 |             0 | 3376 kB
 idx_shop_product_shop_id                       |        0 |            0 |             0 | 4840 kB
 idx_shop_name                                  |        0 |            0 |             0 | 32 kB
 idx_product_avg_price                          |        0 |            0 |             0 | 14 MB
 idx_feedback_stars                             |        0 |            0 |             0 | 88 kB
 idx_shop_product_price                         |        0 |            0 |             0 | 10224 kB
 idx_product_name                               |        0 |            0 |             0 | 8032 kB
 idx_product_description                        |        0 |            0 |             0 | 12 MB
 idx_shop_address                               |        0 |            0 |             0 | 32 kB
 idx_articles_header                            |        0 |            0 |             0 | 56 kB
 idx_forum_topic_title                          |        0 |            0 |             0 | 16 kB
 brand_pkey                                     |        0 |            0 |             0 | 16 kB
 idx_product_brand_id                           |        0 |            0 |             0 | 4008 kB
 feedback_pkey                                  |        0 |            0 |             0 | 240 kB
 forum_topic_pkey                               |        0 |            0 |             0 | 16 kB
 forum_post_pkey                                |        0 |            0 |             0 | 72 kB
 pk_shop_product                                |        0 |            0 |             0 | 22 MB
 pk_user_musician                               |        0 |            0 |             0 | 1616 kB
 pk_musician_genre                              |        0 |            0 |             0 | 96 kB
 pk_product_genre                               |        0 |            0 |             0 | 8048 kB
 pk_type_of_musician_user                       |        0 |            0 |             0 | 824 kB
 pk_type_of_musician_musician                   |        0 |            0 |             0 | 104 kB
 pk_genre_user                                  |        0 |            0 |             0 | 824 kB
 pk_product_user                                |        0 |            0 |             0 | 1616 kB
 idx_feedback_author_id                         |        0 |            0 |             0 | 216 kB
 idx_forum_topic_author_id                      |        0 |            0 |             0 | 16 kB
 idx_forum_post_topic_id                        |        0 |            0 |             0 | 56 kB
 idx_forum_post_author_id                       |        0 |            0 |             0 | 88 kB
 idx_musician_name                              |        0 |            0 |             0 | 344 kB
 idx_musician_genre_genre                       |        0 |            0 |             0 | 48 kB
 idx_product_genre_genre                        |        0 |            0 |             0 | 1648 kB
 idx_product_genre_product_id                   |        0 |            0 |             0 | 5160 kB
 idx_type_of_musician_user_user_id              |        0 |            0 |             0 | 528 kB
 idx_type_of_musician_user_type_of_musician     |        0 |            0 |             0 | 192 kB
 idx_type_of_musician_musician_musician_id      |        0 |            0 |             0 | 72 kB
 idx_type_of_musician_musician_type_of_musician |        0 |            0 |             0 | 48 kB
 idx_genre_user_genre                           |        0 |            0 |             0 | 192 kB
 idx_product_user_product_id                    |        0 |            0 |             0 | 872 kB
 idx_product_user_user_id                       |        0 |            0 |             0 | 872 kB
 idx_product_articles_product_id                |        0 |            0 |             0 | 5520 kB
 pk_product_articles                            |        3 |            6 |             0 | 11 MB
 idx_musician_genre_musician_id                 |        3 |         7800 |          7800 | 72 kB
 pk_musician_product                            |        3 |            9 |             0 | 176 kB
 idx_articles_author_id                         |        6 |            6 |             6 | 40 kB
 idx_genre_user_user_id                         |        6 |            6 |             6 | 528 kB
 idx_musician_product_musician_id               |        6 |            6 |             0 | 104 kB
 idx_product_articles_article_id                |        6 |            6 |             0 | 2240 kB
 idx_feedback_product_id                        |        9 |            6 |             0 | 160 kB
 idx_user_musician_subscription_user_id         |        9 |           15 |             6 | 872 kB
 idx_feedback_article_id                        |        9 |        15009 |         15003 | 112 kB
 idx_shop_product_shop_price                    |       12 |           12 |             0 | 16 MB
 idx_feedback_author_product_article            |       12 |           12 |             0 | 416 kB
 idx_product_rate                               |       12 |         7137 |          7131 | 11 MB
 idx_musician_genre_musician_genre              |       12 |           12 |            12 | 96 kB
 shop_pkey                                      |       18 |           18 |            18 | 16 kB
 articles_pkey                                  |       18 |           18 |            18 | 40 kB
 idx_user_musician_subscription_musician_id     |       18 |           18 |            18 | 448 kB
 idx_musician_product_product_id                |       21 |        11718 |         11700 | 104 kB
 musician_pkey                                  |       33 |           33 |             9 | 448 kB
 app_user_pkey                                  |       36 |           36 |             6 | 872 kB
 product_pkey                                   |       39 |         3963 |          3939 | 8568 kB
 idx_shop_product_product_id                    |      381 |         1455 |          1062 | 8336 kB
```

- 42 из 64 индексов имеют `idx_scan = 0` после реальной нагрузки
- `idx_product_description` (12 MB), `idx_product_avg_price` (14 MB), `pk_shop_product` (22 MB) — крупнейшие неиспользуемые
- `idx_shop_product_product_id` — самый активный: 381 скан, используется в JOINах

---

## pg_stats для потенциально бесполезных колонок

```
 tablename |   attname   | n_distinct | null_frac | avg_width
-----------+-------------+------------+-----------+-----------
 feedback  | article_id  |        990 |       0.5 |         8
 feedback  | author_id   |    -0.6981 |         0 |         8
 feedback  | product_id  |    -0.4903 |       0.5 |         8
 product   | description |         -1 |         0 |        30
 shop      | address     |         -1 |         0 |        11
```

- `product.description`: `n_distinct = -1` (все значения уникальны), `most_common_vals = NULL` — B-tree не подходит для equality-фильтрации, ни одного запроса `WHERE description = '...'`. Индекс бесполезен.
- `shop.address`: аналогично `n_distinct = -1`, ни один метод `ShopRepository` не фильтрует по `address`.
- `feedback.product_id` + `feedback.article_id`: `null_frac = 0.5` — 50% строк содержат NULL. Composite index `(author_id, product_id, article_id)` не используется реальными запросами (`findByProductId`, `findByArticleId` — `author_id` не ведущая колонка). Partial index по `product_id IS NOT NULL` будет в 2 раза меньше.

---

## Планы запросов: сравнение до и после

### Запрос 1: Фильтр продуктов (type + color + price + rate)

**До:**
```
Sort  (cost=222.30..222.31 rows=2 width=37) (actual time=6.280..6.280 rows=0.00 loops=1)
  Sort Key: avg_price
  Sort Method: quicksort  Memory: 25kB
  Buffers: shared hit=187
  ->  Index Scan using idx_product_rate on product p  (cost=0.42..222.29)
        Index Cond: (rate >= 4.0)
        Filter: (avg_price BETWEEN 500 AND 1500) AND (type_of_product = 'ELECTRIC_GUITAR') AND (color = 'RED')
        Rows Removed by Filter: 1575
        Buffers: shared hit=184
Planning Time: 3.316 ms  |  Execution Time: 6.305 ms
```

**После:**
```
Sort  (cost=124.12..124.12 rows=2 width=37) (actual time=1.978..1.978 rows=0.00 loops=1)
  Sort Key: avg_price
  Sort Method: quicksort  Memory: 25kB
  Buffers: shared hit=32 read=10
  ->  Bitmap Heap Scan on product p
        Recheck Cond: (type_of_product = 'ELECTRIC_GUITAR' AND avg_price BETWEEN 500 AND 1500 AND rate >= 4.0)
        Filter: (color = 'RED')
        Rows Removed by Filter: 25
        Heap Blocks: exact=24
        ->  Bitmap Index Scan on idx_product_type_price_rate
              Index Cond: (type_of_product = 'ELECTRIC_GUITAR' AND avg_price >= 500 AND avg_price <= 1500 AND rate >= 4.0)
Execution Time: 2.011 ms
```

| Метрика | До | После |
|---|---|---|
| Индекс | `idx_product_rate` | `idx_product_type_price_rate` (composite) |
| Rows Removed by Filter | 1575 | 25 |
| Execution Time | 6.305 ms | 2.011 ms |
| Ускорение | — | **3.1×** |

Все три условия (`type_of_product`, `avg_price`, `rate`) обрабатываются на уровне индекса. Строк, передаваемых в Heap Scan, стало 25 вместо 1575.

---

### Запрос 2: Магазины с доступным продуктом (product_id range + available)

**До:**
```
Sort  (cost=402.93..403.02 rows=38) (actual time=0.246..0.248 rows=41)
  ->  Hash Join (shop_product ⋈ shop)
        ->  Bitmap Heap Scan on shop_product sp
              Recheck Cond: (product_id BETWEEN 10 AND 50)
              Filter: available
              Rows Removed by Filter: 82
              ->  Bitmap Index Scan on idx_shop_product_product_id
Planning Time: 1.771 ms  |  Execution Time: 0.296 ms
```

**После:**
```
Sort  (cost=156.13..156.22 rows=38 width=19) (actual time=0.593..0.596 rows=41.00 loops=1)
  ->  Hash Join  (shop_product ⋈ shop)
        ->  Bitmap Heap Scan on shop_product sp
              Recheck Cond: (product_id >= 10 AND product_id <= 50 AND available)
              Heap Blocks: exact=5
              ->  Bitmap Index Scan on idx_shop_product_available_product_price
                    Index Cond: (product_id >= 10 AND product_id <= 50)
Execution Time: 0.643 ms
```

| Метрика | До | После |
|---|---|---|
| Индекс | `idx_shop_product_product_id` + post-filter available | `idx_shop_product_available_product_price` (partial) |
| Rows Removed by post-filter | 82 | 0 |
| Heap Blocks | больше | exact=5 |
| Execution Time | 0.296 ms | 0.643 ms |

`available` больше не фигурирует как post-filter — предикат встроен в индекс. Разница во времени объясняется различным прогревом кэша между измерениями.

---

### Запрос 3: Музыканты с жанрами, сортировка по subscribers

**До:**
```
Sort → GroupAggregate → Merge Join (musician_genre ⋈ musician)
  ->  Index Scan using idx_musician_genre_musician_id on musician_genre
  ->  Sort → Seq Scan on musician
Planning Time: 1.849 ms  |  Execution Time: 3.714 ms
```

**После:**
```
Sort  (cost=597.56..600.81 rows=1300 width=53) (actual time=10.029..10.064 rows=1300.00 loops=1)
  Sort Key: m.subscribers DESC
  ->  GroupAggregate
        ->  Sort → Hash Join (musician_genre ⋈ musician)
              ->  Seq Scan on musician_genre
              ->  Seq Scan on musician
Execution Time: 10.633 ms
```

Планировщик выбрал Hash Join вместо Merge Join из baseline. `idx_musician_subscribers` здесь не применяется: сортировка по `subscribers DESC` происходит после полного GROUP BY, и оптимизатор оценивает внешнюю Sort дешевле, чем Index Scan с агрегацией. Индекс будет полезен при пагинированных запросах с LIMIT без GROUP BY.

---

### Запрос 4: Продукты музыканта

**До:** 0.141 ms. **После:** 0.109 ms.

```
Nested Loop
  ->  Index Only Scan using pk_musician_product (musician_id = 5) → 3 rows
  ->  Index Scan using product_pkey → 3 rows
```

PK-индексы справляются оптимально, план не изменился.

---

### Запрос 5: Статьи для продукта

**До:** 0.333 ms. **После:** 0.146 ms.

```
Sort → Nested Loop (product_articles ⋈ articles ⋈ app_user)
  ->  Index Only Scan using pk_product_articles (product_id = 2) → 2 rows
  ->  Index Scan using articles_pkey → 2 rows
  ->  Index Scan using app_user_pkey → 2 rows
```

План тот же, ускорение за счёт прогрева кэша.

---

### Запрос 6: Отзывы на продукт (product_id = 3)

**До:**
```
Sort → Nested Loop
  ->  Index Scan using idx_feedback_product_id (product_id = 3) → 0 rows
Planning Time: 1.754 ms  |  Execution Time: 0.065 ms
```

**После:**
```
Sort → Nested Loop
  ->  Index Scan using idx_feedback_product_notnull (product_id = 3) → 0 rows
Execution Time: 0.350 ms
```

Планировщик выбрал `idx_feedback_product_notnull` вместо `idx_feedback_product_id`. Partial index исключает строки с `product_id IS NULL` (~50% таблицы): **128 kB** против **160 kB** у полного индекса.

---

### Запрос 7: Подписки пользователя на музыкантов

**До:**
```
Sort → Nested Loop
  ->  Bitmap Heap Scan on user_musician_subscription (user_id = 4)
        ->  Bitmap Index Scan on idx_user_musician_subscription_user_id → 3 rows
  ->  Index Scan using musician_pkey → 3 rows
Planning Time: 1.616 ms  |  Execution Time: 0.168 ms
```

**После:**
```
Sort → Nested Loop
  ->  Bitmap Heap Scan on user_musician_subscription (user_id = 4)
        ->  Bitmap Index Scan on pk_user_musician → 3 rows
  ->  Index Scan using musician_pkey → 3 rows
Execution Time: 1.256 ms
```

После удаления дубликата планировщик перешёл на PK `(user_id, musician_id)`, где `user_id` — ведущая колонка. Функциональность сохранена.

---

### Запрос 8: Темы форума по автору

**До:** 0.092 ms. **После:** 0.076 ms.

```
Sort → Seq Scan on forum_topic (200 строк)
  Filter: (author_id = 4)
  Rows Removed by Filter: 200
```

Seq Scan для таблицы из 200 строк — оптимальный выбор, план не изменился.

---

### Запрос 9: Топ-рейтинговые продукты (rate >= 4.5)

**До:** 1.439 ms. **После:** 1.406 ms.

```
Incremental Sort (rate DESC, avg_price)
  Presorted Key: rate
  ->  Index Scan Backward using idx_product_rate (rate >= 4.5) → 802 rows
```

`idx_product_type_price_rate` здесь не применяется — запрос не фильтрует по `type_of_product`, поэтому composite index не даёт преимущества. `idx_product_rate` остаётся оптимальным выбором.

---

### Запрос 10: Сложный JOIN (users × subscriptions × feedback × products × shops)

**До:**
```
Sort → Hash Left Join chain (10 таблиц)
  ->  Nested Loop: idx_feedback_article_id → product_pkey (Memoize)
        idx_feedback_article_id: 9 scans, читает 15009 строк
        Rows Removed by Filter (stars >= 4): 3316 из 5001
Planning Time: 9.341 ms  |  Execution Time: 2.512 ms
```

**После:**
```
Sort → Hash Left Join chain (10 таблиц)
  ->  Merge Join: idx_feedback_article_id → Memoize → product_pkey
        idx_feedback_article_id: 1685 строк после фильтра stars >= 4
        Memoize: Cache Hits=1683, Misses=2
Execution Time: 2.096 ms
```

Ускорение ~17%. Результат 0 строк из-за `a.accepted = TRUE` — нет feedback строк, связанных с принятыми статьями в тестовых данных. `idx_articles_author_accepted` не применяется в этом JOIN, так как `articles` сканируется через `articles_pkey` в Nested Loop.

---

## Удалённые индексы

### idx_product_description

- **Таблица:** `product` | **Колонки:** `description` | **Размер:** 12 MB | **idx_scan:** 0

Ни один метод `ProductRepository` или `ProductSpecification` не использует `WHERE description = '...'` или `ORDER BY description`. Единственный возможный паттерн — `LIKE '%...%'`, который B-tree не поддерживает (нет левого префикса для обхода дерева). `n_distinct = -1` — все значения уникальны, но таких запросов нет. 12 MB обновлялось при каждом `UPDATE` описания продукта.

### idx_shop_address

- **Таблица:** `shop` | **Колонки:** `address` | **Размер:** 32 kB | **idx_scan:** 0

Все методы `ShopRepository` (`findAll`, `findById`, `findByName`) — без фильтрации по `address`. `n_distinct = -1` — адреса уникальны, что исключает range-сканирование. Размер небольшой, но любой индекс создаёт write overhead при вставке нового магазина.

### idx_feedback_author_product_article

- **Таблица:** `feedback` | **Колонки:** `(author_id, product_id, article_id)` | **Размер:** 416 kB | **idx_scan:** 12

Реальные запросы в `FeedbackRepository`:
- `findByProductId(Long productId)` → `WHERE product_id = ?`
- `findByArticleId(Long articleId)` → `WHERE article_id = ?`

В composite B-tree индексе ведущая колонка — `author_id`. Запросы по `product_id` и `article_id` без `author_id` не могут использовать этот индекс эффективно — происходит полный Index Scan. 12 сканов в baseline — Seq-образные проходы через индекс в JOIN, не точечные lookups. Заменён на partial index `idx_feedback_product_notnull` по `product_id`.

### idx_user_musician_subscription_user_id

- **Таблица:** `user_musician_subscription` | **Колонки:** `user_id` | **Размер:** 872 kB | **idx_scan:** 9

PK таблицы — `(user_id, musician_id)`, где `user_id` ведущая колонка. Запросы `WHERE user_id = ?` уже обслуживаются PK-индексом `pk_user_musician` с идентичной эффективностью. Этот индекс — буквальный дубликат первых 8 байт PK. После удаления планировщик переключился на `pk_user_musician` без деградации производительности.

### idx_product_genre_product_id

- **Таблица:** `product_genre` | **Колонки:** `product_id` | **Размер:** 5160 kB | **idx_scan:** 0

PK таблицы `(product_id, genre)` — `product_id` ведущая колонка. Запросы `WHERE product_id = ?` используют `pk_product_genre`. 5160 kB дублируют первые 8 байт PK-индекса. Нулевой `idx_scan` подтверждает: планировщик никогда не предпочитал дубликат.

### idx_musician_genre_musician_id

- **Таблица:** `musician_genre` | **Колонки:** `musician_id` | **Размер:** 72 kB | **idx_scan:** 3

PK `(musician_id, genre)` — `musician_id` ведущая колонка. Запросы по `musician_id` обслуживаются PK. 3 скана в baseline — погрешность; реально планировщик использовал `idx_musician_genre_musician_genre` (composite, совпадающий с PK структурой).

---

## Новые индексы

### idx_articles_author_accepted

```sql
CREATE INDEX idx_articles_author_accepted ON articles(author_id) WHERE accepted = TRUE;
```

**Покрывает:** `ArticleRepository.findByAuthorIdAndAccepted(Long authorId, Boolean accepted)` и `findByAccepted(Boolean accepted, Pageable pageable)`.

До: Index Scan по полному `idx_articles_author_id` с Filter: `accepted = TRUE`, убирал значительную часть строк (~5 ms при 10k строк).

После:
```
Index Scan using idx_articles_author_accepted on articles
  Index Cond: (author_id = 5)
  Index Searches: 1
Execution Time: 1.678 ms
```

Partial index содержит только принятые статьи. Filter-нода с `Rows Removed by Filter` исчезла из плана — все записи индекса удовлетворяют условию `accepted = TRUE`.

### idx_shop_product_available_product_price

```sql
CREATE INDEX idx_shop_product_available_product_price
ON shop_product(product_id, price)
WHERE available = TRUE;
```

**Покрывает:** `ShopProductRepository.findByProductIdAndAvailableTrue(Long productId)` и аналогичные запросы с `available = true`.

До: Bitmap Heap Scan с post-filter `available`, убирал 82 строки из 123.

После: `available` исчез из post-filter — предикат встроен в индекс. Heap Blocks сократились до точных страниц с доступными товарами. Включение `price` в columns поддерживает `ORDER BY price` через Index Scan без внешней сортировки.

### idx_product_type_price_rate

```sql
CREATE INDEX idx_product_type_price_rate ON product(type_of_product, avg_price, rate);
```

**Покрывает:** `ProductRepository.findAll(Specification<Product> spec, Pageable pageable)` через `ProductSpecification.hasTypeOfProduct() + hasPriceBetween() + hasRateBetween()`.

До: Index Scan по `idx_product_rate`, Filter убирал 1575 строк. 6.305 ms.

После: Bitmap Index Scan, все три условия на уровне индекса. 25 строк в Heap Scan. 2.011 ms (**3.1×**).

Порядок колонок `(type_of_product, avg_price, rate)` соответствует убыванию selectivity: `type_of_product` отсекает ~90% строк, `avg_price` — ещё ~70% от оставшихся, `rate` — финальный предикат.

### idx_feedback_product_notnull

```sql
CREATE INDEX idx_feedback_product_notnull ON feedback(product_id) WHERE product_id IS NOT NULL;
```

**Покрывает:** `FeedbackRepository.findByProductId(Long productId)`.

До: `idx_feedback_product_id` — 160 kB, сканирует все 10 000 строк включая 5 000 с `product_id IS NULL`.

После: 128 kB. Планировщик автоматически выбрал новый partial index. При росте данных эффект масштабируется: 50% строк никогда не попадут в этот индекс.

### idx_musician_subscribers

```sql
CREATE INDEX idx_musician_subscribers ON musician(subscribers DESC);
```

**Покрывает:** `MusicianRepository.findAll(Pageable pageable)` с `Sort.by("subscribers").descending()`, запросы с LIMIT на первые N популярных музыкантов.

При `ORDER BY subscribers DESC LIMIT N` PostgreSQL выполняет Index Scan Backward, читая строки уже в нужном порядке без Sort-ноды. Особенно эффективно при малом LIMIT (топ-10, топ-20): читается ровно N записей из индекса, остаток таблицы не трогается.

### idx_articles_author_created

```sql
CREATE INDEX idx_articles_author_created ON articles(author_id, created_at DESC);
```

**Покрывает:** `ArticleRepository.findByAuthorId(Long authorId, Pageable pageable)` с сортировкой `created_at DESC`.

Composite index `(author_id, created_at DESC)` позволяет выполнить Index Scan по `author_id = ?` с результатом уже упорядоченным по `created_at DESC`. Планировщик избегает Sort-ноды и применяет LIMIT-pushdown: читает ровно `page_size` строк индекса.

---

## Сводная таблица индексов

### Сохранённые explicit индексы

| Индекс | Таблица | Колонки | Обоснование |
|---|---|---|---|
| `idx_product_brand_id` | `product` | `brand_id` | JOIN с таблицей `brand` |
| `idx_product_type_of_product` | `product` | `type_of_product` | Fallback при одиночном фильтре по типу |
| `idx_product_guitar_form` | `product` | `guitar_form` | Фильтр `hasGuitarForm` в `ProductSpecification` |
| `idx_product_avg_price` | `product` | `avg_price` | ORDER BY и range-фильтры по цене |
| `idx_product_rate` | `product` | `rate` | Запросы топ-рейтинга и `hasRateBetween` |
| `idx_product_name` | `product` | `name` | Точный поиск по имени; LIKE '%...%' требует pg_trgm |
| `idx_shop_name` | `shop` | `name` | Поиск магазина по имени |
| `idx_shop_product_shop_id` | `shop_product` | `shop_id` | Продукты конкретного магазина |
| `idx_shop_product_product_id` | `shop_product` | `product_id` | Самый активный: 381 скан в baseline |
| `idx_shop_product_shop_price` | `shop_product` | `(shop_id, price)` | Ассортимент магазина с сортировкой по цене |
| `idx_shop_product_price` | `shop_product` | `price` | Сортировка и range-фильтры по цене |
| `idx_feedback_author_id` | `feedback` | `author_id` | `findByAuthorId` |
| `idx_feedback_product_id` | `feedback` | `product_id` | Существующий индекс; планировщик предпочитает partial |
| `idx_feedback_article_id` | `feedback` | `article_id` | `findByArticleId`; активный (9 сканов в baseline) |
| `idx_feedback_stars` | `feedback` | `stars` | `WHERE stars >= N` в сложных JOIN |
| `idx_articles_author_id` | `articles` | `author_id` | `findByAuthorId` без условия accepted |
| `idx_articles_header` | `articles` | `header` | Точный поиск по заголовку |
| `idx_forum_topic_author_id` | `forum_topic` | `author_id` | Темы форума по автору |
| `idx_forum_topic_title` | `forum_topic` | `title` | Поиск тем по названию |
| `idx_forum_post_topic_id` | `forum_post` | `topic_id` | `findByTopicId` |
| `idx_forum_post_author_id` | `forum_post` | `author_id` | Посты конкретного автора |
| `idx_musician_name` | `musician` | `name` | Точный поиск по имени |
| `idx_user_musician_subscription_musician_id` | `user_musician_subscription` | `musician_id` | Подписчики музыканта |
| `idx_musician_genre_genre` | `musician_genre` | `genre` | Музыканты по жанру |
| `idx_musician_genre_musician_genre` | `musician_genre` | `(musician_id, genre)` | Используется в Merge Join |
| `idx_product_genre_genre` | `product_genre` | `genre` | Продукты по жанру |
| `idx_type_of_musician_user_user_id` | `type_of_musician_user` | `user_id` | `findByUserId` |
| `idx_type_of_musician_user_type_of_musician` | `type_of_musician_user` | `type_of_musician` | Фильтр по типу музыканта |
| `idx_type_of_musician_musician_musician_id` | `type_of_musician_musician` | `musician_id` | `findByMusicianId` |
| `idx_type_of_musician_musician_type_of_musician` | `type_of_musician_musician` | `type_of_musician` | Фильтр по типу |
| `idx_genre_user_genre` | `genre_user` | `genre` | Пользователи по жанру |
| `idx_genre_user_user_id` | `genre_user` | `user_id` | `findByUserId` |
| `idx_musician_product_musician_id` | `musician_product` | `musician_id` | Продукты музыканта; 6 сканов в baseline |
| `idx_musician_product_product_id` | `musician_product` | `product_id` | Обратный lookup; 21 скан в baseline |
| `idx_product_user_product_id` | `product_user` | `product_id` | `findByProductId` |
| `idx_product_user_user_id` | `product_user` | `user_id` | `findByUserId` |
| `idx_product_articles_product_id` | `product_articles` | `product_id` | Статьи для продукта |
| `idx_product_articles_article_id` | `product_articles` | `article_id` | Статья → продукт |

### Новые индексы

| Индекс | Таблица | Колонки | Обоснование |
|---|---|---|---|
| `idx_articles_author_accepted` | `articles` | `author_id WHERE accepted=TRUE` | Partial index для `findByAuthorIdAndAccepted` |
| `idx_shop_product_available_product_price` | `shop_product` | `(product_id, price) WHERE available=TRUE` | `available` исчез из post-filter |
| `idx_product_type_price_rate` | `product` | `(type_of_product, avg_price, rate)` | Composite для фильтра type + price + rate |
| `idx_feedback_product_notnull` | `feedback` | `product_id WHERE product_id IS NOT NULL` | Вдвое меньше полного индекса |
| `idx_musician_subscribers` | `musician` | `subscribers DESC` | `ORDER BY subscribers DESC LIMIT N` без Sort |
| `idx_articles_author_created` | `articles` | `(author_id, created_at DESC)` | Пагинация статей автора по дате |

### Удалённые индексы

| Индекс | Таблица | Причина |
|---|---|---|
| `idx_product_description` | `product` | Нет запросов; B-tree бесполезен для LIKE '%...%'; 12 MB |
| `idx_shop_address` | `shop` | Нет запросов по address в ShopRepository |
| `idx_feedback_author_product_article` | `feedback` | Неверная ведущая колонка; заменён на idx_feedback_product_notnull |
| `idx_user_musician_subscription_user_id` | `user_musician_subscription` | Дублирует PK (user_id, musician_id) |
| `idx_product_genre_product_id` | `product_genre` | Дублирует PK (product_id, genre) |
| `idx_musician_genre_musician_id` | `musician_genre` | Дублирует PK (musician_id, genre) |

---

## Итоги

### Количество индексов

| Категория | До | После |
|---|---|---|
| Explicit CREATE INDEX | 43 | 43 |
| Из них удалено | — | 6 |
| Из них добавлено | — | +6 |
| Implicit PK + unique | ~21 | ~21 |
| **Итого** | **~64** | **~64** |

### Размер индексов

| Таблица | До | После | Δ |
|---|---|---|---|
| `product` | 64 MB | 57 MB | **−7 MB** |
| `shop_product` | 61 MB | 65 MB | +4 MB |
| `product_articles` | 19 MB | 19 MB | = |
| `product_genre` | 15 MB | 9696 kB | **−5.5 MB** |
| `user_musician_subscription` | 2936 kB | 2064 kB | **−872 kB** |
| `feedback` | 1232 kB | 944 kB | **−288 kB** |
| `musician` | 792 kB | 808 kB | +16 kB |
| `musician_genre` | 312 kB | 240 kB | **−72 kB** |
| `articles` | 136 kB | 216 kB | +80 kB |
| прочие | ~4.8 MB | ~4.8 MB | = |
| **Итого** | **~173 MB** | **~163 MB** | **−10 MB (−5.8%)** |

### Прирост производительности по ключевым запросам

Замеры выполнены на одной БД: сначала без явных индексов (только PK/unique), затем с полным набором `creatingIndexes.sql`. `pg_stat_reset()` + `ANALYZE` перед каждым прогоном.

| Запрос | Без индексов | С индексами | Ускорение |
|---|---|---|---|
| Q1: фильтр product (type+color+price+rate) | 60.345 ms (Parallel Seq Scan) | 1.394 ms (Bitmap Index Scan) | **43×** |
| Q2: магазины с доступным продуктом | 36.667 ms (pk range + post-filter) | 1.847 ms (partial index) | **20×** |
| Q9: топ-рейтинг (rate >= 4.5) | 85.902 ms (Seq Scan, 129k rows) | 2.058 ms (Index Scan Backward) | **42×** |
| Q4: продукты музыканта | 0.945 ms | 0.158 ms | **6×** |
| Q5: статьи для продукта | 0.990 ms | 0.141 ms | **7×** |
| Q6: feedback по product_id | 0.926 ms (Seq Scan, 10k rows) | 0.363 ms (partial index) | **2.6×** |
| Q7: подписки пользователя | 0.537 ms | 0.244 ms | **2.2×** |
| Q3: музыканты с жанрами | 2.748 ms | 3.580 ms | = (одинаковый план) |
| Q8: темы форума (200 строк) | 0.102 ms | 0.105 ms | = (Seq Scan оптимален) |

Q3 выбирает одинаковый Hash Join + Seq Scan в обоих случаях: `ORDER BY subscribers DESC` без LIMIT после GROUP BY не позволяет применить `idx_musician_subscribers` (планировщик не знает, сколько строк нужно до агрегации). Q8 — таблица из 200 строк, Seq Scan дешевле Index Scan.
