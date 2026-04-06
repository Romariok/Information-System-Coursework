# GMatch — сервис подбора гитар

Курсовая по «Информационным системам», ИТМО, группа P3312. Авторы: Кобелев Роман, Балин Артём.

![CI](https://github.com/Romariok/Information-System-Coursework/actions/workflows/ci.yml/badge.svg)
![JaCoCo coverage](https://img.shields.io/badge/coverage-JaCoCo-brightgreen?logo=java)

---

## Что это

Выбрать гитару сложно: параметров куча, мнения расходятся, и непонятно, что реально играет твой любимый музыкант. GMatch пытается это исправить — ищешь по характеристикам (тип, форма, материал, цвет, цена, рейтинг), видишь список gear конкретного исполнителя, читаешь и пишешь обзоры прямо на странице продукта, подписываешься на музыкантов.

---

## Стек

| Слой | Технология |
|---|---|
| Бэкенд | Java 17, Spring Boot 3, Spring Security + JWT, Flyway |
| База данных | PostgreSQL 15 |
| Кэш | Redis 7 |
| Фронтенд | TypeScript, React, Tailwind CSS |
| Мониторинг | Prometheus + Micrometer, Grafana, cAdvisor |
| Контейнеризация | Docker, docker-compose |
| CI/CD | GitHub Actions |
| Анализ безопасности | Snyk, SpotBugs, Dependabot |

---

## Архитектура

### Слои

Бэкенд разбит на `controller → service → repository`. Бизнес-логика не живёт в контроллерах — это конкретно помогло, когда пришлось переписывать запросы при оптимизациях: сервисный слой трогали изолированно.

### Аутентификация

Stateless JWT вместо сессий — нет общего хранилища, проще масштабировать. Секрет, TTL токена и сила BCrypt вынесены в переменные окружения. В docker-окружении ставим `BCRYPT_STRENGTH=8`, в проде по умолчанию 10.

### WebSocket

Уведомления о новых отзывах и статьях идут через Spring WebSocket + STOMP. Топики (`/feedbacks`, `/articles`) и допустимые origins в конфигурации. Парсер данных вынесен в `@Async`-задачи — HTTP-поток не блокируется при добавлении новых исполнителей.

### Кэширование

Часто запрашиваемые списки (бренды, продукты, музыканты) кэшируются в Redis. Сериализация настроена с `JavaTimeModule` под Java 8 date/time, TTL и префикс ключей берутся из `application.properties`.

---

## База данных

### Модель

10 основных сущностей плюс junction-таблицы для many-to-many:

- `product` — гитара, усилитель, педаль и т.д., 12+ enum-атрибутов
- `musician` — музыкант с жанрами, типами игры и списком оборудования
- `app_user` — пользователь с ролями и подписками
- `shop` / `shop_product` — магазины и цены на конкретный продукт
- `articles` / `feedback` — статьи и отзывы с модерацией
- `forum_topic` / `forum_post` — форум

Ссылочная целостность: `ON DELETE CASCADE` там, где дочерние записи без родителя теряют смысл; `ON DELETE SET NULL` там, где запись можно оставить (например, отзыв без автора).

### Триггеры

Средний рейтинг, средняя цена и счётчики подписок хранятся денормализованно прямо в строке сущности и поддерживаются триггерами:

| Триггер | Событие | Что обновляет |
|---|---|---|
| `update_product_rating` | INSERT/UPDATE/DELETE на `feedback` | `product.rate` |
| `update_product_average_price` | INSERT/UPDATE/DELETE на `shop_product` | `product.avg_price` |
| `update_user_subscriptions` | INSERT/DELETE на `user_musician_subscription` | `app_user.subscriptions` |
| `update_musician_subscribers` | INSERT/DELETE на `user_musician_subscription` | `musician.subscribers` |

Небольшой write-overhead, зато фильтрация не требует агрегирующих JOIN-ов при каждом чтении.

### Хранимые процедуры

- добавление отзыва с обновлением рейтинга
- подписка на музыканта
- модерация статей

### Индексы

Индексы создавались после профилирования, не наугад. Покрыли FK-колонки, поля фильтрации (`type_of_product`, `guitar_form`, `rate`, `avg_price`, `stars`), поля поиска (`name`, `header`, `title`) и составные индексы для частых паттернов: `(musician_id, genre)`, `(shop_id, price)`, `(author_id, product_id, article_id)`.

Самый заметный результат — фильтрация продуктов по типу + цвету + цене + рейтингу:

```
Было:  73.7 мс
Стало:  2.0 мс
```

---

## Тестирование

### Запуск тестов

```bash
# просто тесты
mvn -f backend/pom.xml test

# тесты + HTML-отчёт покрытия
mvn -f backend/pom.xml test jacoco:report

# с Testcontainers (поднимает реальные PostgreSQL и Redis)
SPRING_PROFILES_ACTIVE=test mvn -f backend/pom.xml test jacoco:report
```

Отчёт открывается в `backend/target/site/jacoco/index.html`. В CI сохраняется как артефакт `jacoco-report` (шаг `Backend - Tests & Coverage`).

### Что тестируется

| Слой | Файлы | Подход |
|---|---|---|
| Сервисы | `service/*ServiceTest.java` | unit-тесты с Mockito |
| Контроллеры | `controller/*ControllerTest.java` | `@WebMvcTest` + MockMvc |
| JPA-маппинги | `JpaMappingIntegrationTest.java` | `@DataJpaTest` + Testcontainers PostgreSQL |
| Redis-кэш | `RedisCacheIntegrationTest.java` | `@SpringBootTest` + Testcontainers Redis |
| JWT и безопасность | `JwtSecurityTest.java` | MockMvc + фильтры Spring Security |
| Пагинация | `PaginationEdgeCasesTest.java` | граничные значения |
| Отказоустойчивость | `InfrastructureFailureTest.java`, `RedisCacheResilienceTest.java` | падение Redis, сбой парсера |
| Спецификации фильтров | `ProductSpecificationTest.java` | unit-тесты предикатов |
| Нативные запросы | `NativeQueryRepositoryTest.java` | `@DataJpaTest` + Testcontainers |

Сценарии подробно: [docs/test-scenarios.md](./docs/test-scenarios.md)

---

## API

| Контроллер | Основные операции |
|---|---|
| `AuthController` | POST /api/auth/register, /api/auth/login |
| `ProductController` | GET /api/product/filter (многокритериальный), /api/product/type/{type}, /api/product/{name} |
| `MusicianController` | GET /api/musician, поиск по имени, подписка, добавление/удаление продуктов |
| `ArticleController` | список, поиск по заголовку, создание, модерация |
| `FeedbackController` | отзывы на продукт и на статью |
| `UserController` | роли, пользовательские продукты, жанры, типы музыканта |
| `ShopController` | магазины и их продукты |
| `ForumTopicController` | темы форума с постами |
| `BrandController` | список брендов |

### OpenAPI спецификация

Все контроллеры покрыты аннотациями `io.swagger.v3.oas.annotations`. Swagger UI доступен на `http://localhost:5252/swagger-ui.html`.

Спецификация: [docs/openapi.json](./docs/openapi.json)

---

## Запуск

```bash
cd .docker
docker compose up -d

# Backend:    http://localhost:5252
# Frontend:   http://localhost:5173
# Grafana:    http://localhost:3000  (admin/admin)
# Prometheus: http://localhost:9090
# PgAdmin:    http://localhost:5050
```

Бэкенд без Docker (нужен запущенный postgres):

```bash
export POSTGRES_NAME=postgres POSTGRES_USER=postgres POSTGRES_PASSWORD=pgpwd
export PORT=5252 JWT_SECRET=your-secret JWT_EXPIRATION_MS=3600000

mvn -f backend/pom.xml spring-boot:run
```

---

## CI/CD

Три workflow в GitHub Actions:

`ci.yml` — на каждый push и PR в `main`/`release`. Собирает бэкенд (`mvn package -DskipTests`, JDK 17 Temurin), ставит зависимости фронтенда, прогоняет ESLint + Prettier и сборку, потом собирает Docker-образы без пуша — просто проверяем, что Dockerfile-ы не сломаны.

`release.yml` — на теги `v*` и push в `main`/`release`. Пушит образы в GHCR, экспортирует их в `.tar.gz` и создаёт GitHub Release с архивами и автогенерированными release notes.

`vulnerabilities.yml` — статический анализ: SpotBugs через `mvn verify` и Snyk по `pom.xml`. Оба отчёта сохраняются как артефакты. Dependabot в дополнение — сам открывает PR при обновлениях зависимостей в `pom.xml` и `package.json`.

### Docker

Всё поднимается одной командой:

```bash
cd .docker && docker compose up -d
```

Сервисы: `postgres`, `pgadmin`, `backend`, `frontend`, `redis`, `prometheus`, `grafana`, `cadvisor`. Postgres поднимается с health check (`pg_isready`), остальные ждут его. Ресурсные лимиты заданы явно. Инициализационные SQL монтируются через `docker-entrypoint-initdb.d`.

Backend собирается в multi-stage Dockerfile: первый stage компилирует Haskell-парсер, второй собирает Maven-артефакт, третий — минимальный JRE-образ для запуска. Frontend: Node для сборки Vite, Nginx для раздачи статики.

### Мониторинг

Бэкенд отдаёт метрики через `/actuator/prometheus`, Prometheus их собирает, cAdvisor добавляет метрики контейнеров (CPU, RAM, сеть, диск). Grafana поднимается с уже настроенными datasource и дашбордами через provisioning — руками ничего настраивать не нужно.

Из полезного: `http_server_requests_seconds_bucket` для гистограммы по эндпоинтам, `jvm_memory_used_bytes` и `hikaricp_connections_active` для понимания состояния приложения.

---

## SQL

| Файл | Назначение |
|---|---|
| [`SQL/initScript.sql`](./SQL/initScript.sql) | схема, перечисления, триггеры, функции |
| [`SQL/creatingIndexes.sql`](./SQL/creatingIndexes.sql) | индексы |
| [`SQL/functions.sql`](./SQL/functions.sql) | хранимые процедуры |
| [`SQL/insertTestData.sql`](./SQL/insertTestData.sql) | нагрузочные данные (~7 мин заполнения) |
| [`SQL/insertSmallData.sql`](./SQL/insertSmallData.sql) | небольшой датасет для ручного тестирования |

---

## Подробнее

- [Архитектурные решения и компромиссы](./docs/architecture-decisions.md)
- [Этапы рефакторинга](./docs/refactoring-details.md)
- [Надёжность и ограничения](./docs/reliability.md)
- [Оптимизация производительности](./docs/optimization.md)
- [Предметная область, требования и прецеденты](./docs/requirements.md)
- [2 этап — схема БД, триггеры, индексы, замеры](./docs/2_step/README.MD)
- [3 этап — REST API](./docs/3_step/README.MD)
- [Оптимизации подробно](./docs/refactoring/3-it/optimizations.md)
