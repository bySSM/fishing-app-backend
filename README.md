# Fishing App Backend

Backend для приложения рыболовов. Spring Boot + PostgreSQL + JWT.

## 📱 Идея приложения
Рыбаки фотографируют улов, коллекционируют как покемонов, смотрят уловы других, лайкают, соревнуются в рейтинге.

## 🛠 Стек технологий
- Java 17
- Spring Boot 3.3.0
- PostgreSQL
- JWT (jjwt 0.12.5)
- Lombok
- Maven
- Spring Security
- BCrypt

## ✅ Уже реализовано

### Пользователи
- [x] Регистрация и логин (JWT)
- [x] BCrypt шифрование
- [x] Поиск пользователей по username
- [x] Автопересчёт рейтинга при запуске

### Уловы
- [x] CRUD уловов
- [x] Поиск уловов рядом (Haversine formula)
- [x] Загрузка фото (multipart, до 10MB)
- [x] Скрытие геолокации
- [x] DTO для ответов

### Рейтинг
- [x] Топ-100 по сумме 15 самых тяжёлых рыб
- [x] Позиция в рейтинге (#1, #2...)
- [x] Рейтинг по географической области
- [x] Динамический расчёт позиции

### Социальное
- [x] Лайки (toggle, защита от повторов)
- [x] Комментарии
- [x] Аквариум (топ-5 рыб)

## 📡 Endpoints

### Auth (без токена)
- `POST /api/auth/register`, `POST /api/auth/login`

### Catches (JWT)
- `POST /api/catches` — создать
- `POST /api/catches/with-photo` — создать с фото
- `PUT /api/catches/{id}` — обновить
- `DELETE /api/catches/{id}` — удалить
- `GET /api/catches/my` — мои
- `GET /api/catches/user/{userId}` — пользователя
- `GET /api/catches/nearby?lat=&lng=&radiusKm=` — рядом
- `GET /api/catches/{id}` — по ID

### Rating (без токена)
- `GET /api/rating/top100` — топ-100

### Search (JWT)
- `GET /api/search/users?query=` — поиск

### Aquarium (JWT)
- `GET /api/aquarium/my`
- `GET /api/aquarium/user/{userId}`

### Likes (JWT)
- `POST /api/likes/catch/{catchId}/toggle`
- `GET /api/likes/catch/{catchId}/status`

### Comments (JWT)
- `POST /api/comments/catch/{catchId}`
- `GET /api/comments/catch/{catchId}`
- `DELETE /api/comments/{commentId}`

### Files
- `GET /uploads/{filename}` — фото

## 📋 TODO
- [ ] ИИ-распознавание рыбы
- [ ] Уведомления
- [ ] Swagger UI
- [ ] Деплой