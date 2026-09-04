# Fishing App Backend

Backend для приложения рыболовов. Spring Boot + PostgreSQL + JWT.

## 📱 Идея приложения

Рыбаки фотографируют улов, коллекционируют как покемонов, смотрят уловы других, лайкают, соревнуются в рейтинге.

## 🛠 Стек технологий

- Java 17
- Spring Boot 3.3.0
- PostgreSQL
- JWT (jjwt 0.12.5)
- Spring Security
- BCrypt
- Maven

## ✅ Уже реализовано

### Пользователи
- [x] Регистрация и логин (JWT)
- [x] BCrypt шифрование паролей
- [x] Поиск пользователей по username (без утечки email — см. раздел "Безопасность")
- [x] Автопересчёт рейтинга

### Уловы
- [x] CRUD уловов
- [x] Поиск уловов рядом (Haversine formula)
- [x] Загрузка фото (multipart, до 10MB, с проверкой реального содержимого файла)
- [x] Скрытие геолокации
- [x] DTO для ответов

### Рейтинг
- [x] Топ-100 по сумме 15 самых тяжёлых рыб
- [x] Позиция в рейтинге
- [x] Рейтинг по географической области
- [x] Динамический расчёт позиции

### Социальное
- [x] Лайки (toggle, защита от повторов)
- [x] Комментарии
- [x] Аквариум (топ-5 рыб)

## 🔒 Безопасность

Проект прошёл security-ревью, устранены следующие проблемы:

- **Path traversal защита**: `photoUrl` больше не принимается от клиента напрямую через JSON (только через выделенные эндпоинты загрузки файлов); `FileStorageService` проверяет, что итоговый путь при удалении файла не выходит за пределы папки загрузок.
- **Валидация загружаемых файлов**: файл, помеченный как изображение, дополнительно декодируется через `ImageIO`, чтобы отсечь файлы с поддельным `Content-Type`.
- **Приватность данных**: email пользователя не отдаётся в результатах поиска (`/api/search/users`) — только `id`, `username`, `createdAt`, `rating`.
- **JWT_SECRET**: при старте приложение проверяет, что секрет задан и имеет длину ≥ 32 байт (256 бит для HS256); при несоответствии — явная ошибка при старте вместо тихого использования слабого ключа.
- **Rate limiting**: `/api/auth/login` и `/api/auth/register` ограничены по IP (не более 10 попыток за 5 минут на каждый эндпоинт) — защита от брутфорса.
- **Централизованная обработка ошибок**: `GlobalExceptionHandler` (`@RestControllerAdvice`) возвращает корректные HTTP-коды (`404` — не найдено, `403` — нет прав, `400` — бизнес-ошибка, `500` — непредвиденная ошибка без утечки деталей) вместо единообразного `400` на всё.
- **User enumeration защита**: логин с несуществующим username и логин с неверным паролем возвращают одинаковый код и сообщение — нельзя перебором узнать, какие логины существуют.

## ⚙️ Конфигурация

Обязательные переменные окружения:

| Переменная | Описание | Пример |
|---|---|---|
| `DB_URL` | JDBC-адрес PostgreSQL | `jdbc:postgresql://localhost:5432/fishingapp` |
| `DB_USERNAME` | Логин БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | — |
| `JWT_SECRET` | Секрет для подписи JWT, **минимум 32 байта** | сгенерировать: `openssl rand -base64 32` |
| `JWT_EXPIRATION` | Время жизни токена в мс (опционально) | `86400000` (24 часа, по умолчанию) |
| `SERVER_PORT` | Порт сервера (опционально) | `8080` (по умолчанию) |
| `UPLOAD_DIR` | Папка для загруженных файлов (опционально) | `uploads` (по умолчанию) |

**Важно:** `JWT_SECRET` короче 32 байт не даст приложению стартовать (специально, чтобы не работать с криптографически слабым ключом).

## 📡 Endpoints

### Auth (без токена)
- `POST /api/auth/register`
- `POST /api/auth/login`

### Catches (JWT)
- `POST /api/catches` — создать
- `POST /api/catches/with-photo` — создать с фото
- `POST /api/catches/{id}/photo` — добавить/заменить фото
- `PUT /api/catches/{id}` — обновить
- `DELETE /api/catches/{id}` — удалить
- `GET /api/catches/my` — мои
- `GET /api/catches/user/{userId}` — пользователя
- `GET /api/catches/nearby?lat=&lng=&radiusKm=` — рядом
- `GET /api/catches/{id}` — по ID

### Rating (без токена)
- `GET /api/rating/top100`
- `GET /api/rating/top100/nearby?lat=&lng=&radiusKm=`

### Search (JWT)
- `GET /api/search/users?query=` — поиск (без email в ответе)

### Aquarium (JWT)
- `GET /api/aquarium/my`
- `GET /api/aquarium/user/{userId}`

### Likes (JWT)
- `POST /api/likes/catch/{catchId}/toggle`
- `GET /api/likes/catch/{catchId}/status`
- `GET /api/likes/ratings` (без токена)

### Comments (JWT)
- `POST /api/comments/catch/{catchId}`
- `GET /api/comments/catch/{catchId}`
- `GET /api/comments/my`
- `DELETE /api/comments/{commentId}`

### Files
- `GET /uploads/{filename}` — фото

## 🗂 Структура пакетов