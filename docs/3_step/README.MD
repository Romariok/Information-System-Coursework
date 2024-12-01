<div align="center">

## Министерство науки и высшего образования Российской Федерации
## Федеральное государственное автономное образовательное учреждение высшего образования
## «Национальный исследовательский университет ИТМО»
## Факультет программной инженерии и компьютерной техники
**Отчёт по курсовой работе**

**3 этап**

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

- [API](#api)
   - [ArticleController](#articlecontroller)
   - [FeedbackController](#feedbackcontroller)
   - [ForumPostController](#forumpostcontroller)
   - [ForumTopicController](#forumtopiccontroller)
   - [BrandController](#brandcontroller)
   - [MusicianController](#musiciancontroller)
   - [ProductController](#productcontroller)
   - [ShopController](#shopcontroller)
   - [UserController](#usercontroller)
   - [AuthController](#authcontroller)

## API

### ArticleController
- **GET /api/article**: Получить список статей с пагинацией.
- **GET /api/article/unaccepted**: Получить список неодобренных статей.
- **GET /api/article/header/{header}**: Поиск статей по заголовку.
- **GET /api/article/author/{authorId}**: Получить статьи автора.
- **POST /api/article/moderate**: Модерация статьи.
- **POST /api/article**: Создание новой статьи.

### FeedbackController
- **GET /api/feedback/product/{productId}**: Получить отзывы о товаре.
- **GET /api/feedback/article/{articleId}**: Получить отзывы о статье.
- **POST /api/feedback/product**: Добавить отзыв о товаре.
- **POST /api/feedback/article**: Добавить отзыв о статье.

### ForumPostController
- **POST /api/forum/post**: Создание нового сообщения в теме форума.

### ForumTopicController
- **GET /api/forum/topic**: Получить список тем форума с пагинацией.
- **GET /api/forum/topic/author/{authorId}**: Получить темы автора.
- **POST /api/forum/topic**: Создать новую тему.
- **GET /api/forum/topic/{topicId}/posts**: Получить сообщения темы.

### BrandController
- **GET /api/brand**: Получить список брендов с пагинацией.

### MusicianController
- **GET /api/musician**: Получить список музыкантов с пагинацией.
- **GET /api/musician/name/{name}**: Поиск музыкантов по имени.
- **POST /api/musician/subscription**: Подписаться на музыканта.
- **POST /api/musician/product**: Добавить продукт музыканту.
- **DELETE /api/musician/product**: Удалить продукт у музыканта.
- **GET /api/musician/{musicianName}/products**: Получить продукты музыканта.

### ProductController
- **GET /api/product**: Получить товары по бренду.
- **GET /api/product/{name}/genres**: Получить жанры товара.
- **GET /api/product/{name}/articles**: Получить статьи о товаре.
- **GET /api/product/type/{typeOfProduct}**: Получить товары по типу.
- **GET /api/product/{name}**: Поиск товаров по названию.
- **GET /api/product/filter**: Фильтрация товаров.

### ShopController
- **GET /api/shop**: Получить список магазинов с пагинацией.
- **GET /api/shop/{shopId}/products**: Получить товары магазина.

### UserController
- **GET /api/user/role/{username}**: Получить роль пользователя.
- **GET /api/user/products**: Получить продукты пользователя.
- **POST /api/user/product**: Добавить продукт пользователю.
- **DELETE /api/user/product**: Удалить продукт у пользователя.
- **GET /api/user/genres**: Получить жанры пользователя.
- **GET /api/user/types**: Получить типы музыканта пользователя.
- **POST /api/user/genres**: Установить жанры пользователю.
- **POST /api/user/types**: Установить типы музыканта пользователю.

### AuthController
- **POST /api/auth/register**: Регистрация нового пользователя.
- **POST /api/auth/login**: Вход пользователя.
