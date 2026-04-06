# Сценарии тестирования — GuitarMatch IS

Cначала unit-тесты с моками, затем MockMvc-интеграция контроллеров, потом сценарии отказов инфраструктуры, и наконец Testcontainers с реальными Postgres и Redis.

## Содержание

- [Сценарии тестирования — GuitarMatch IS](#сценарии-тестирования--guitarmatch-is)
  - [Содержание](#содержание)
  - [Инфраструктура](#инфраструктура)
  - [Unit-тесты сервисов](#unit-тесты-сервисов)
    - [AuthService (тесты 1–6)](#authservice-тесты-16)
    - [ArticleService (тесты 7–22)](#articleservice-тесты-722)
    - [MusicianService (тесты 23–40)](#musicianservice-тесты-2340)
    - [ProductService (тесты 41–53)](#productservice-тесты-4153)
    - [FeedbackService (тесты 54–62)](#feedbackservice-тесты-5462)
    - [ForumTopicService / ForumPostService (тесты 63–73)](#forumtopicservice--forumpostservice-тесты-6373)
    - [BrandService (тесты 74–75)](#brandservice-тесты-7475)
    - [ShopService (тесты 76–77)](#shopservice-тесты-7677)
    - [UserService (тесты 78–90)](#userservice-тесты-7890)
  - [Unit-тесты спецификаций (тесты 91–103)](#unit-тесты-спецификаций-тесты-91103)
  - [Интеграционные тесты контроллеров (тесты 104–161)](#интеграционные-тесты-контроллеров-тесты-104161)
    - [AuthController (тесты 104–109)](#authcontroller-тесты-104109)
    - [ProductController (тесты 110–118)](#productcontroller-тесты-110118)
    - [ArticleController (тесты 119–128)](#articlecontroller-тесты-119128)
    - [MusicianController (тесты 129–141)](#musiciancontroller-тесты-129141)
    - [FeedbackController (тесты 142–146)](#feedbackcontroller-тесты-142146)
    - [ForumTopicController / ForumPostController (тесты 147–151)](#forumtopiccontroller--forumpostcontroller-тесты-147151)
    - [UserController (тесты 152–157)](#usercontroller-тесты-152157)
    - [BrandController / ShopController (тесты 158–161)](#brandcontroller--shopcontroller-тесты-158161)
  - [Тесты отказоустойчивости (тесты 162–200)](#тесты-отказоустойчивости-тесты-162200)
    - [A. PostgreSQL недоступен (тесты 162–165)](#a-postgresql-недоступен-тесты-162165)
    - [B. Redis недоступен (тесты 166–169)](#b-redis-недоступен-тесты-166169)
    - [C. Haskell-парсер недоступен (тесты 170–174)](#c-haskell-парсер-недоступен-тесты-170174)
    - [D. WebSocket недоступен (тесты 175–179)](#d-websocket-недоступен-тесты-175179)
    - [E. JWT граничные случаи (тесты 180–185)](#e-jwt-граничные-случаи-тесты-180185)
    - [F. Пагинация граничные значения (тесты 186–190)](#f-пагинация-граничные-значения-тесты-186190)
    - [G. Конкурентный доступ (тесты 191–192)](#g-конкурентный-доступ-тесты-191192)
    - [H. GlobalControllerExceptionHandler (тесты 193–200)](#h-globalcontrollerexceptionhandler-тесты-193200)
  - [Testcontainers — интеграция с реальной БД и Redis (тесты 201–221)](#testcontainers--интеграция-с-реальной-бд-и-redis-тесты-201221)
    - [Native @Query / @Procedure (тесты 201–208)](#native-query--procedure-тесты-201208)
    - [JPA-маппинги и триггеры (тесты 209–216)](#jpa-маппинги-и-триггеры-тесты-209216)
    - [Redis-сериализация (тесты 217–221)](#redis-сериализация-тесты-217221)
  - [Итого](#итого)

---

## Инфраструктура

| Параметр | Значение |
|---|---|
| Фреймворк | JUnit 5 + Mockito + Spring Boot Test |
| Запуск | `mvn -B test` из директории `backend/` |
| Покрытие | `mvn jacoco:report` → `target/site/jacoco/` |
| Порог покрытия | ≥ 60% line coverage для пакетов `service` и `controller` |
| БД (интеграция) | Testcontainers `postgres:16-alpine` + Flyway-миграции |
| Redis (интеграция) | Testcontainers `redis:7-alpine` |

---

## Unit-тесты сервисов

Сервисы проверяются в изоляции, без Spring-контекста и БД. Репозитории подменяются моками. Смысл в том, чтобы проверить реакцию на граничные случаи: дубликаты, отсутствующие сущности, недостаточные права доступа.

### AuthService (тесты 1–6)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 1 | `register_success` | `username="newUser"`, `password="pass"`, пользователь не существует | Пользователь сохраняется; возвращается `AuthResponseDTO` с непустым `token` |
| 2 | `register_duplicateUsername` | Пользователь с таким именем уже есть | Бросает `UserAlreadyExistException` |
| 3 | `register_emptyUsername` | `username=""` | Бросает исключение валидации |
| 4 | `register_nullPassword` | `password=null` | Бросает исключение |
| 5 | `login_success` | Корректные `username` и `password` | `authenticationManager.authenticate` вызывается 1 раз; возвращается токен |
| 6 | `login_badCredentials` | Неверный пароль | Исключение пробрасывается наружу |

---

### ArticleService (тесты 7–22)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 7 | `getAcceptedArticles_returnsOnlyAccepted` | Репозиторий возвращает список статей | Возвращается непустой список; все элементы имеют `accepted=true` |
| 8 | `getAcceptedArticles_usesStoredHtmlContent` | Статья с заполненным `htmlContent` | Конвертер markdown→html не вызывается повторно |
| 9 | `createArticle_htmlContentPrecomputed` | Создание статьи с валидным контентом | После создания `htmlContent` заполнен; повторный вызов конвертера не происходит |
| 10 | `createArticle_userNotFound` | JWT указывает на несуществующего пользователя | Бросает `UsernameNotFoundException` |
| 11 | `moderateArticle_notAdmin` | Пользователь без прав администратора | Бросает `ForbiddenException` |
| 12 | `moderateArticle_articleNotFound` | Статья с указанным ID не существует | Бросает `ArticleNotFoundException` |
| 13 | `getArticlesByHeaderContaining_emptyString` | `header=""` | Возвращается пустой список; исключение не бросается |
| 14 | `getStatusArticles_notAdmin` | Пользователь без прав администратора | Бросает `ForbiddenException` |
| 15 | `createArticle_userNotFound` | Пользователь не найден по JWT | Бросает `UsernameNotFoundException` |
| 16 | `getArticlesByHeaderContaining_nonEmpty` | `header="Test"` | Возвращается список статей |
| 17 | `moderateArticle_success` | Админ принимает статью | `moderateArticle` вызывается 1 раз |
| 18 | `getArticlesByAuthorId_userNotFound` | Автор отсутствует | Возвращается пустой список (фактическое поведение) |
| 19 | `getArticlesByAuthorId_success` | Автор существует | Возвращается список статей автора |
| 20 | `getArticleById_notFound` | Статья отсутствует | Бросает `ArticleNotFoundException` |
| 21 | `getStatusArticles_success` | Админ + pending статьи | Возвращается список `StatusArticlesDTO` |
| 22 | `convertMarkdownToHtmlAsync_success` | Рабочий parser | Возвращается HTML |

---

### MusicianService (тесты 23–40)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 23 | `getMusician_noNPlusOne` | N музыкантов возвращается из репозитория | `musicianGenreRepository.findByMusician` вызывается ровно N раз (документирует N+1) |
| 24 | `getMusicianInfo_notFound` | Музыкант с указанным ID не существует | Бросает `MusicianNotFoundException` |
| 25 | `createMusician_duplicate` | Музыкант с таким именем уже существует | Бросает `MusicianAlreadyExistsException` |
| 26 | `createMusician_unauthorized` | Пользователь не найден по токену | Исключение пробрасывается |
| 27 | `subscribeToMusician_alreadySubscribed` | Подписка уже существует | Бросает `SubscriptionAlreadyExistsException` |
| 28 | `unsubscribeFromMusician_noSubscription` | Подписка не найдена | Бросает `SubscriptionNotFoundException` |
| 29 | `addProductToMusician_alreadyLinked` | Связь музыкант–продукт уже существует | Бросает `ProductMusicianAlreadyExists` |
| 30 | `deleteProductFromMusician_notFound` | Связь музыкант–продукт не найдена | Бросает `ProductMusicianNotFoundException` |
| 31 | `searchMusicians_emptyName` | `name=""` | Возвращается пустой список |
| 32 | `getMusician_sortByUnsupported` | `sortBy=null` | Бросается `NullPointerException` (фактическое поведение) |
| 33 | `getMusicianInfo_subscribersCount` | `subscribers=0` | DTO содержит `subscribers=0` |
| 34 | `unsubscribeFromMusician_success` | Подписка существует | Выполняется delete, возвращается `true` |
| 35 | `addProductToMusician_productNotFound` | Продукт отсутствует | Бросает `ProductNotFoundException` |
| 36 | `addProductToMusician_success` | Связи нет | Связь создаётся, `save` вызывается 1 раз |
| 37 | `deleteProductFromMusician_success` | Связь существует | Связь удаляется без ошибок |
| 38 | `getMusiciansByGenre_returnsList` | Есть связь музыкант–жанр | Возвращается список жанров |
| 39 | `getMusiciansByTypeOfMusician_returnsList` | Есть связь музыкант–тип | Возвращается список типов |
| 40 | `isSubscribed_trueFalse` | Подписка есть/нет | Возвращает `true` или `false` |

---

### ProductService (тесты 41–53)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 41 | `getProductsByFilter_allNullFilters` | Все параметры фильтра `null` | Возвращается весь список без фильтрации |
| 42 | `getProductsByFilter_minRateGreaterThanMax` | `minRate=4.0`, `maxRate=2.0` | Возвращается пустой список |
| 43 | `getProductsByFilter_minPriceGreaterThanMax` | `minPrice=1000`, `maxPrice=500` | Возвращается пустой список |
| 44 | `getProductsByFilter_ladsZeroOrNegative` | `lads=0` или `lads=-1` | Фильтр по lads игнорируется |
| 45 | `getProductsById_notFound` | Продукт с указанным ID не существует | Бросает `ProductNotFoundException` |
| 46 | `getGenresByProductName_withUnderscores` | `name="Gibson_Les_Paul"` | Нормализуется в `"Gibson Les Paul"` через `replaceAll("_", " ")` |
| 47 | `getProductsById_nullId` | `id=null` | Бросается `NullPointerException` из-за unboxing `long` |
| 48 | `getProductsByFilter_invalidEnum` | Невалидный enum передан как `null` | Фильтр игнорируется, возвращается список |
| 49 | `getProductsByBrandName_withPagination` | `from=20`, `size=10` | Корректная пагинация: `page=2`, `size=10` |
| 50 | `getProductArticles_productNotFound` | Продукт отсутствует | Бросает `ProductNotFoundException` |
| 51 | `getMusiciansByProductId_productNotFound` | Продукт отсутствует | Бросает `ProductNotFoundException` |
| 52 | `getProductsByTypeOfProduct_returnsList` | Валидный `typeOfProduct` | Возвращается список продуктов |
| 53 | `getProductsByNameContains_blankName` | `name=""` | Возвращается пустой список |

---

### FeedbackService (тесты 54–62)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 54 | `createProductFeedback_starsOutOfRange` | `stars=6` или `stars=-1` | Бросает исключение валидации |
| 55 | `createProductFeedback_productNotFound` | Продукт с указанным ID не существует | Бросает `ProductNotFoundException` |
| 56 | `createArticleFeedback_articleNotFound` | Статья с указанным ID не существует | Бросает `ArticleNotFoundException` |
| 57 | `getFeedbackByProductId_noFeedback` | Нет отзывов к продукту | Возвращается пустой список; исключение не бросается |
| 58 | `createArticleFeedback_starsOutOfRange` | `stars=6` | Сервис не валидирует, отзыв сохраняется (фактическое поведение) |
| 59 | `createProductFeedback_userNotFound` | Пользователь отсутствует | Бросает `UsernameNotFoundException` |
| 60 | `getFeedbackByArticleId_noFeedback` | Нет отзывов | Возвращается пустой список |
| 61 | `createArticleFeedback_success` | Валидный отзыв | Отзыв сохраняется, возвращается `true` |
| 62 | `convertToDTO_feedbackFields` | Feedback с product+article | Все поля DTO маппятся корректно |

---

### ForumTopicService / ForumPostService (тесты 63–73)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 63 | `createTopic_duplicate` | Тема с таким заголовком уже существует | Бросает `ForumTopicAlreadyExistsException` |
| 64 | `createPost_closedTopic` | Тема имеет `is_closed=true` | Бросает `ForbiddenException` |
| 65 | `createPost_topicNotFound` | Тема с указанным ID не существует | Бросает `ForumTopicNotFoundException` |
| 66 | `getPostsByTopicId_topicNotFound` | Тема с указанным ID не существует | Бросает `ForumTopicNotFoundException` |
| 67 | `getTopics_withPagination` | `from=0`, `size=10` | Возвращается список тем |
| 68 | `getTopics_emptyNameFilter` | Пустой фильтр | Возвращается пустой список |
| 69 | `closeTopic_notOwnerOrAdmin` | Пользователь не автор/админ | Бросает `ForbiddenException` |
| 70 | `closeTopic_success` | Админ/автор закрывает тему | Статус обновляется; `closeTopic` вызывается |
| 71 | `getForumTopicById_notFound` | Тема отсутствует | Бросает `ForumTopicNotFoundException` |
| 72 | `isTopicOwner_trueFalse` | Автор или иной пользователь | Возвращает `true`/`false` |
| 73 | `getForumTopicsByAuthor_returnsList` | Автор существует | Возвращается список тем автора |

---

### BrandService (тесты 74–75)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 74 | `getBrands_returnsList` | Репозиторий содержит бренды | Возвращается непустой список брендов |
| 75 | `getBrands_brandNotFound` | Бренд с указанным ID не существует | Бросает `BrandNotFoundException` |

---

### ShopService (тесты 76–77)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 76 | `getShops_withPagination` | `from=0`, `size=10` | Возвращается список магазинов с пагинацией |
| 77 | `getProductsByShopId_shopNotFound` | Магазин с указанным ID не существует | Бросает `ShopNotFoundException` |

---

### UserService (тесты 78–90)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 78 | `getUserRole_userNotFound` | Пользователь с указанным именем не существует | Бросает `UserNotFoundException` |
| 79 | `addUserProduct_alreadyAdded` | Продукт уже добавлен пользователем | Бросает `UserProductAlreadyExistsException` |
| 80 | `deleteUserProduct_notFound` | Продукт не найден в списке пользователя | Бросает `UserProductNotFoundException` |
| 81 | `getRoleByUsername_success` | Пользователь существует | Возвращается корректная `Role` |
| 82 | `addProductToUser_success` | Связи нет | Связь создаётся, `save` вызывается |
| 83 | `deleteProductFromUser_success` | Связь существует | Связь удаляется |
| 84 | `getSubscribedMusicians_success` | Есть подписки | Возвращается список `MusicianInfoDTO` |
| 85 | `getUserProducts_success` | Есть продукты | Возвращается список `ProductDTO` |
| 86 | `setGenresToUser_success` | Новый список жанров | Старые связи удалены, новые сохранены |
| 87 | `setTypesOfMusiciansToUser_success` | Новый список типов | Старые связи удалены, новые сохранены |
| 88 | `getGenresByUser_success` | Есть жанры | Возвращается список жанров |
| 89 | `getTypesOfMusiciansByUser_success` | Есть типы | Возвращается список типов |
| 90 | `getUserInfoById_notFound` | Пользователь отсутствует | Бросает `UsernameNotFoundException` |

## Unit-тесты спецификаций (тесты 91–103)

Тестируют `ProductSpecification` на уровне JPA `Predicate` без обращения к БД. Спецификации часто ломаются при рефакторинге имён полей, и такие тесты ловят это раньше, чем Testcontainers.

| # | Название | Входные данные | Ожидаемый результат |
|---|---|---|---|
| 91 | `hasBrand_null` | `brandId=null` | Предикат `null` (фильтр не применяется) |
| 92 | `hasBrand_valid` | `brandId=1` | Предикат `brand.id = 1` |
| 93 | `hasName_null` | `name=null` | Предикат `null` |
| 94 | `hasName_empty` | `name=""` | Предикат `null` |
| 95 | `hasName_valid` | `name="Gibson"` | LIKE-предикат, содержащий `"Gibson"` |
| 96 | `hasRateBetween_oneParamNull` | `minRate=null`, `maxRate=5.0` | Предикат `null` |
| 97 | `hasRateBetween_valid` | `minRate=3.0`, `maxRate=5.0` | BETWEEN-предикат `[3.0, 5.0]` |
| 98 | `hasLads_zero` | `lads=0` | Предикат `null` |
| 99 | `hasLads_negative` | `lads=-1` | Предикат `null` |
| 100 | `hasLads_valid` | `lads=22` | Предикат `lads = 22` |
| 101 | `hasPriceBetween_oneParamNull` | `minPrice=null`, `maxPrice=1000` | Предикат `null` |
| 102 | `hasStrings_zero` | `strings=0` | Предикат `null` |
| 103 | `hasStrings_valid` | `strings=6` | Предикат `strings = 6` |

---

## Интеграционные тесты контроллеров (тесты 104–161)

Проверяют HTTP-слой: статус-коды, валидацию входных данных и маппинг исключений в ответы. Кэш отключён, чтобы Redis не был нужен при запуске контекста.

### AuthController (тесты 104–109)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 104 | `POST /api/auth/register` | Валидный `username`, `password` | 200 | Тело содержит поле `token` |
| 105 | `POST /api/auth/register` | `username=" "` (пустой) | 400 | — |
| 106 | `POST /api/auth/register` | Дублирующийся `username` | 409 | — |
| 107 | `POST /api/auth/login` | Корректные `username` и `password` | 200 | Тело содержит `token` |
| 108 | `POST /api/auth/login` | Неверный пароль | 401 | — |
| 109 | `POST /api/auth/login` | Отсутствует поле `username` | 401 | — |

---

### ProductController (тесты 110–118)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 110 | `GET /api/product/filter` | Отсутствуют обязательные параметры | 400 | — |
| 111 | `GET /api/product/filter` | Все параметры заполнены корректно | 200 | JSON-массив продуктов |
| 112 | `GET /api/product/filter` | `typeOfProduct=INVALID` | 400 | — |
| 113 | `GET /api/product/type/{typeOfProduct}` | Валидный `typeOfProduct` | 200 | JSON-массив |
| 114 | `GET /api/product/{name}` | Имя с пробелами | 200 | Продукт найден |
| 115 | `GET /api/product/id/{id}` | Несуществующий `id` | 404 | — |
| 116 | `GET /api/product/{id}/musicians` | Валидный `id` | 200 | JSON-массив |
| 117 | `GET /api/product/{id}/articles` | Валидный `id` | 200 | JSON-массив |
| 118 | `GET /api/product/{id}/shops` | Валидный `id` | 200 | JSON-массив |

---

### ArticleController (тесты 119–128)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 119 | `GET /api/article` | Отсутствуют параметры пагинации | 400 | — |
| 120 | `GET /api/article` | Валидные параметры пагинации | 200 | Только статьи с `accepted=true` |
| 121 | `GET /api/article/unaccepted` | Неавторизованный запрос | 403 | — |
| 122 | `GET /api/article/unaccepted` | Авторизован, но не администратор | 403 | — |
| 123 | `GET /api/article/unaccepted` | Авторизован как администратор | 200 | JSON-массив непроверенных статей |
| 124 | `POST /api/article` | Неавторизованный запрос | 401 | — |
| 125 | `POST /api/article` | Авторизован, валидные данные | 200 | Созданная статья |
| 126 | `POST /api/article` | `header=" "` (пустой) | 400 | — |
| 127 | `POST /api/article/moderate` | Не администратор | 403 | — |
| 128 | `POST /api/article/moderate` | Статья не найдена | 404 | — |

---

### MusicianController (тесты 129–141)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 129 | `GET /api/musician` | Отсутствует параметр `sortBy` | 400 | — |
| 130 | `GET /api/musician` | Валидные параметры | 200 | JSON-массив музыкантов |
| 131 | `GET /api/musician/id/{id}` | Несуществующий `id` | 404 | — |
| 132 | `POST /api/musician` | Неавторизованный запрос | 401 | — |
| 133 | `POST /api/musician` | Авторизован, валидные данные | 200 | Созданный музыкант |
| 134 | `POST /api/musician` | Дублирующееся имя | 409 | — |
| 135 | `POST /api/musician/subscription` | Неавторизованный запрос | 401 | — |
| 136 | `POST /api/musician/subscription` | Повторная подписка | 409 | — |
| 137 | `DELETE /api/musician/subscription` | Неавторизованный запрос | 401 | — |
| 138 | `DELETE /api/musician/subscription` | Подписка не найдена | 404 | — |
| 139 | `POST /api/musician/product` | Неавторизованный запрос | 401 | — |
| 140 | `DELETE /api/musician/product` | Связь не найдена | 404 | — |
| 141 | `GET /api/musician/{musicianName}/products` | Имя с подчёркиваниями (`John_Lennon`) | 200 | Нормализация `_` → ` ` |

---

### FeedbackController (тесты 142–146)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 142 | `POST /api/feedback/product` | Неавторизованный запрос | 401 | — |
| 143 | `POST /api/feedback/product` | `stars=6` | 400 | — |
| 144 | `POST /api/feedback/product` | `stars=-1` | 400 | — |
| 145 | `POST /api/feedback/product` | Авторизован, `stars=4` | 200 | Созданный отзыв |
| 146 | `GET /api/feedback/product/{productId}` | Нет отзывов | 200 | Пустой JSON-массив |

---

### ForumTopicController / ForumPostController (тесты 147–151)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 147 | `GET /api/forum/topic` | Валидные параметры пагинации | 200 | JSON-массив тем |
| 148 | `POST /api/forum/topic` | Неавторизованный запрос | 401 | — |
| 149 | `POST /api/forum/topic` | Дублирующееся название | 409 | — |
| 150 | `POST /api/forum/post` | Тема закрыта | 403 | — |
| 151 | `POST /api/forum/post` | Неавторизованный запрос | 401 | — |

---

### UserController (тесты 152–157)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 152 | `GET /api/user/role/{username}` | Несуществующий пользователь | 404 | — |
| 153 | `POST /api/user/product` | Неавторизованный запрос | 401 | — |
| 154 | `DELETE /api/user/product` | Продукт не найден в списке | 404 | — |
| 155 | `GET /api/user/products` | Неавторизованный запрос | 401 | — |
| 156 | `GET /api/user/subscribed` | Неавторизованный запрос | 401 | — |
| 157 | `GET /api/user/subscribed` | Авторизован | 200 | JSON-массив `MusicianInfoDTO` |

---

### BrandController / ShopController (тесты 158–161)

| # | Метод / URL | Входные данные | Ожидаемый HTTP-статус | Проверка тела |
|---|---|---|---|---|
| 158 | `GET /api/brand` | Без параметров | 200 | JSON-массив брендов |
| 159 | `GET /api/brand` | Несуществующий `id` | 404 | — |
| 160 | `GET /api/shop` | Валидные параметры | 200 | JSON-массив магазинов |
| 161 | `GET /api/shop/{shopId}/products` | Несуществующий магазин | 404 | — |

---

## Тесты отказоустойчивости (тесты 162–200)

Проверяют поведение системы при недоступности отдельных компонентов. Реальные контейнеры не поднимаются, отказы имитируются исключениями в моках. Так можно проверить обработку сбоев без зависимости от Docker в CI.

### A. PostgreSQL недоступен (тесты 162–165)

| # | Название | Что мокируется | Ожидаемый результат |
|---|---|---|---|
| 162 | `postgres_findAll_connectionError` | `productRepository.findAll` → `CannotGetJdbcConnectionException` | Ответ 500 с JSON `ErrorResponse` |
| 163 | `postgres_save_dataIntegrityViolation_articleSave` | `articleRepository.save` → `DataIntegrityViolationException` | Зависимые репозитории не вызываются; ответ 400/500 |
| 164 | `postgres_save_dataIntegrityViolation_musicianSave` | `musicianRepository.save` → `DataIntegrityViolationException` | Связанные репозитории и WebSocket не вызываются |
| 165 | `postgres_queryTimeout` | `userRepository.findByUsername` → `QueryTimeoutException` | Ответ 500, не 401 |

---

### B. Redis недоступен (тесты 166–169)

Проверяют, что при отказе Redis сервисы продолжают работать через fallback к БД и не падают с исключением.

| # | Название | Что мокируется | Ожидаемый результат |
|---|---|---|---|
| 166 | `redis_cacheErrorHandler_getError_doesNotThrow` | `CacheErrorHandler` + `RedisConnectionFailureException` | Исключение не пробрасывается (fallback) |
| 167 | `redis_getMusician_fallback` | Redis недоступен | `musicianRepository.findAll` вызывается 1 раз; ответ 200 |
| 168 | `redis_getBrands_fallback` | Redis недоступен | `brandRepository.findAll` вызывается 1 раз |
| 169 | `redis_getAcceptedArticles_fallback` | Redis недоступен | `articleRepository.findByAccepted` вызывается 1 раз |

> Cache hit (тест 201) проверяется с реальным Redis: unit-контекст не поднимает `@Cacheable`, поэтому убедиться, что второй вызов приходит из кэша, а не из БД, можно только там.

---

### C. Haskell-парсер недоступен (тесты 170–174)

Markdown-контент статей конвертируется внешним Haskell-процессом. Тесты проверяют, что при любом сбое парсера статья всё равно сохраняется, а fallback остаётся исходный markdown.

| # | Название | Входные данные | Ожидаемый результат |
|---|---|---|---|
| 170 | `parser_ioException_fallbackToRaw` | Файл парсера не найден → `IOException` | `htmlContent = markdownText` (не пустая строка) |
| 171 | `parser_nonZeroExitCode_fallbackToRaw` | Парсер завершается с `exitCode != 0` | `htmlContent = markdownText` |
| 172 | `parser_emptyOutput_fallbackToRaw` | Парсер возвращает `""` при `exitCode=0` | `htmlContent = markdownText` |
| 173 | `parser_interruptedException_setsFlag` | `process.waitFor()` → `InterruptedException` | `htmlContent = markdownText`; флаг прерывания потока установлен |
| 174 | `parser_success_htmlContent` | Парсер возвращает HTML | `htmlContent` содержит HTML; fallback-ветка не выполняется |

---

### D. WebSocket недоступен (тесты 175–179)

Уведомления через WebSocket отправляются при создании статей, музыкантов и подписок. Тесты проверяют, что сбой доставки уведомления не откатывает основную операцию.

| # | Название | Что мокируется | Ожидаемый результат |
|---|---|---|---|
| 175 | `websocket_createArticle_failsGracefully` | `simpMessagingTemplate.convertAndSend` → `MessageDeliveryException` | Статья сохранена; ответ 200 |
| 176 | `websocket_createMusician_failsGracefully` | Аналогично | Музыкант сохранён; ответ 200 |
| 177 | `websocket_subscribeToMusician_failsGracefully` | Аналогично | Подписка создана; ответ 200 |
| 178 | `websocket_moderateArticle_failsGracefully` | Аналогично | Статья обработана; ответ 200 |
| 179 | `websocket_createArticle_notifiesSingleTime` | Штатная работа WebSocket | `convertAndSend` вызывается ровно 1 раз |

---

### E. JWT граничные случаи (тесты 180–185)

| # | Название | Входные данные | Ожидаемый HTTP-статус |
|---|---|---|---|
| 180 | `jwt_missingAuthHeader` | Нет заголовка `Authorization` | 401 |
| 181 | `jwt_malformedToken` | `Authorization: Bearer not.a.token` | 401 |
| 182 | `jwt_expiredToken` | Токен с истёкшим `exp` | 401 |
| 183 | `jwt_userDeletedFromDb` | Валидный токен, но пользователь удалён | 401 |
| 184 | `jwt_tamperedSignature` | Токен с изменённой подписью | 401 |
| 185 | `jwt_missingBearerPrefix` | `Authorization: <токен без Bearer>` | 401 |

---

### F. Пагинация граничные значения (тесты 186–190)

| # | Название | Входные данные | Ожидаемый HTTP-статус |
|---|---|---|---|
| 186 | `pagination_sizeZero` | `size=0` | 400 |
| 187 | `pagination_fromNegative` | `from=-1` | 400 |
| 188 | `pagination_fromVeryLarge` | `from=1000000` | 200 — пустой список |
| 189 | `pagination_sizeNegative` | `size=-5` | 400 |
| 190 | `pagination_sizeMaxInt` | `size=Integer.MAX_VALUE` | 200 — пустая страница |

---

### G. Конкурентный доступ (тесты 191–192)

| # | Название | Предусловие / Входные данные | Ожидаемый результат |
|---|---|---|---|
| 191 | `race_doubleSubscribe_constraintViolation` | Второй `save` → `DataIntegrityViolationException` | `SubscriptionAlreadyExistsException` → 409 |
| 192 | `race_doubleCreateMusician_constraintViolation` | Оба потока прошли `existsByName=false` → constraint violation | Документируется как известный gap: check-then-act без блокировки |

---

### H. GlobalControllerExceptionHandler (тесты 193–200)

Проверяют, что каждое доменное исключение маппируется в правильный HTTP-статус.

| # | Исключение | Ожидаемый HTTP-статус |
|---|---|---|
| 193 | `ForbiddenException` | 403 |
| 194 | `MusicianNotFoundException` | 404 |
| 195 | `ArticleNotFoundException` | 404 |
| 196 | `ProductNotFoundException` | 404 |
| 197 | `UserAlreadyExistException` | 409 |
| 198 | `MusicianAlreadyExistsException` | 409 |
| 199 | `SubscriptionAlreadyExistsException` | 409 |
| 200 | `SubscriptionNotFoundException` | 404 |

---

## Testcontainers — интеграция с реальной БД и Redis (тесты 201–221)

Тут поднимаются реальные сервисы через Docker. Схема накатывается Flyway-миграциями, поэтому тесты проходят через тот же путь миграции, что и прод. В итоге проверяется то, что нельзя проверить моками: корректность CAST-синтаксиса в нативных запросах, работа триггеров и round-trip сериализации в Redis.

### Native @Query / @Procedure (тесты 201–208)

| # | Название | Что верифицируется |
|---|---|---|
| 201 | `subscribeToMusician_storedProcedure_insertsSubscription` | Хранимая процедура `subscribe_to_musician` вызывается корректно; строка появляется в таблице подписок |
| 202 | `saveByMusicianIdAndGenre_nativeInsert_rowIsPersistedAndReadable` | `INSERT ... CAST(:genre AS genre_enum)` — корректный синтаксис; строка читается через `findByMusician` |
| 203 | `saveByMusicianIdAndTypeOfMusician_nativeInsert_rowIsPersistedAndReadable` | `INSERT ... CAST(:typeOfMusician AS type_of_musician_enum)` |
| 204 | `saveByUserIdAndGenre_nativeInsert_rowIsPersistedAndReadable` | `INSERT INTO genre_user ... CAST(:genre AS genre_enum)` |
| 205 | `deleteAllByUser_nativeDelete_removesAllGenresForUser` | `DELETE FROM genre_user WHERE user_id = :userId` — строки удалены |
| 206 | `saveByUserIdAndTypeOfMusician_nativeInsert_rowIsPersistedAndReadable` | `INSERT INTO type_of_musician_user ... CAST(... AS type_of_musician_enum)` |
| 207 | `deleteAllByUser_typeOfMusician_nativeDelete_removesAllTypesForUser` | `DELETE FROM type_of_musician_user WHERE user_id = :userId` |
| 208 | `moderateArticle_callsPostgresFunction_updatesAcceptedFlag` | Postgres-функция `moderate_article` обновляет `accepted=true`; функция проверяет `is_admin` |

---

### JPA-маппинги и триггеры (тесты 209–216)

| # | Название | Что верифицируется |
|---|---|---|
| 209 | `brand_countryEnum_columnTransformerRoundTrip` | Enum-поле с `CAST(? AS country_enum)` — значение `USA` сохраняется и восстанавливается |
| 210 | `brand_allCountryEnumValues_persistAndReloadCorrectly` | `JAPAN`, `GERMANY`, `RUSSIA` — все значения проходят через `country_enum` без ошибок |
| 211 | `product_allEnumColumnsWithColumnTransformer_roundTripCorrectly` | Все 6 enum-полей `Product` с `@ColumnTransformer` |
| 212 | `product_nullableEnumColumns_persistNullCorrectly` | Nullable enum-поля сохраняются как `NULL` |
| 213 | `subscriptionTrigger_onInsert_incrementsCountersOnBothSides` | INSERT в таблицу подписок → счётчики `subscriptions` и `subscribers` = 1 |
| 214 | `subscriptionTrigger_onDelete_decrementsCountersOnBothSides` | DELETE из таблицы подписок → счётчики возвращаются в 0 |
| 215 | `avgPriceTrigger_onShopProductInsert_updatesProductAvgPrice` | INSERT в `shop_product` → `product.avg_price` обновляется триггером |
| 216 | `article_foreignKeyAndLocalDateTimeColumn_persistAndReloadCorrectly` | FK `article.author_id → app_user`; `LocalDateTime createdAt`; флаг `accepted=false` |

---

### Redis-сериализация (тесты 217–221)

Сериализатор настроен на три нетривиальных параметра: `JavaTimeModule` для `LocalDateTime`, ISO-формат дат вместо timestamp, и `activateDefaultTyping(NON_FINAL)`. Без последнего Spring возвращает `LinkedHashMap` вместо конкретного DTO после десериализации.

| # | Название | Что верифицируется |
|---|---|---|
| 217 | `localDateTime_withJavaTimeModule_roundTripsCorrectly` | `LocalDateTime` сериализуется в ISO-формат, десериализуется обратно в `LocalDateTime` |
| 218 | `customEnums_roundTripAsEnumNotString` | `Genre.METAL` и `Country.JAPAN` десериализуются как enum, не как `String` |
| 219 | `cacheHit_secondGetReturnsConcreteDtoNotLinkedHashMap` | Второй GET возвращает конкретный `SampleDto`, не `LinkedHashMap` |
| 220 | `missingKey_returnsNull_doesNotThrow` | Несуществующий ключ возвращает `null` без `NullPointerException` |
| 221 | `overwrite_secondPutReplacesValue` | Повторный PUT корректно заменяет значение в Redis |

---

## Итого

Порог покрытия — ≥ 60% по строкам для пакетов `service` и `controller`. Testcontainers-тесты в этот порог не входят, но закрывают риски, которые unit-тесты принципиально не могут поймать: корректность CAST-синтаксиса в нативных запросах, поведение триггеров и round-trip сериализации в Redis.
