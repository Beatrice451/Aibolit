# План реализации подтверждения аккаунта по email

## 1. Изменения в базе данных

### Миграция V17: Добавление полей верификации в таблицу users
- [x] Добавить поле `email_verified BOOLEAN NOT NULL DEFAULT FALSE`
- [x] Добавить поле `email_verified_at TIMESTAMP NULL`
- [ ] Проставить всем существующим пользователям `email_verified = TRUE` и `email_verified_at = CURRENT_TIMESTAMP`

### Миграция V18: Создание таблицы email_verification_tokens
- [x] Создать таблицу с полями:
  - `id` (PK, IDENTITY)
  - `user_id` (FK на users с ON DELETE CASCADE)
  - `token` (VARCHAR(255), UNIQUE, NOT NULL)
  - `expires_at` (TIMESTAMP, NOT NULL)
  - `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)
  - `verified_at` (TIMESTAMP, NULL)
  - `is_used` (BOOLEAN, NOT NULL, DEFAULT FALSE)
- [x] Создать индекс на `token` с условием `WHERE is_used = FALSE`
- [x] Создать индекс на `expires_at` с условием `WHERE is_used = FALSE`
- [x] Создать индекс на `user_id`

---

## 2. Модели и сущности

- [x] **Обновить User.java**
  - [x] Добавить поле `emailVerified` (Boolean, default false)
  - [x] Добавить поле `emailVerifiedAt` (Instant, nullable)

- [x] **Создать EmailVerificationToken.java**
  - [x] Entity с маппингом на таблицу `email_verification_tokens`
  - [x] ManyToOne связь с User (LAZY)
  - [x] Методы `isExpired()` и `isValid()`

---

## 3. Репозитории

- [x] **Создать EmailVerificationTokenRepository.java**
  - [x] Метод `findByTokenAndIsUsedFalse(String token)`
  - [x] Метод `findFirstByUserAndIsUsedFalseOrderByCreatedAtDesc(User user)`
  - [x] Метод `deleteExpiredTokens(@Param("now") Instant now)` с @Modifying
  - [x] Метод `invalidateAllUserTokens(@Param("user") User user)` с @Modifying

---

## 4. Сервисы

- [x] **Создать EmailVerificationService.java**
  - [x] Метод `sendVerificationEmail(User user)`:
    - [x] Инвалидировать все старые токены пользователя
    - [x] Сгенерировать криптографически стойкий токен (SecureRandom + Base64, 32 байта)
    - [x] Создать запись в БД с expiration = 1 час
    - [x] Отправить email через EmailService
  - [x] Метод `verifyEmail(String token)`:
    - [x] Найти токен, проверить что не использован и не истек
    - [x] Проверить что email еще не верифицирован
    - [x] Установить `emailVerified = true`, `emailVerifiedAt = now()`
    - [x] Пометить токен как использованный
  - [x] Метод `resendVerificationEmail(User user)`:
    - [x] Найти пользователя по email
    - [x] Проверить что email не верифицирован
    - [x] Проверить rate limit: последняя отправка была не менее 1 минуты назад
    - [x] Вызвать `sendVerificationEmail(user)`
  - [x] Метод `cleanupExpiredTokens()` с @Scheduled (cron: каждый день в 2:00 AM)
  - [x] Конфигурация: `token-validity-hours = 1`, `base-url` из properties

- [ ] **Обновить RegistrationService.java**
  - [ ] В методе `registerUser()` после сохранения пользователя:
    - [ ] Установить `emailVerified = false` (явно)
    - [ ] Вызвать `emailVerificationService.sendVerificationEmail(user)`

- [x] **Обновить EmailService.java**
  - [x] Добавить метод `sendVerificationEmail(String to, String firstName, String verificationUrl, int validityHours)`:
    - [x] Асинхронный (@Async)
    - [x] Использовать Thymeleaf шаблон "verification-email"
    - [x] Передать в контекст: firstName, verificationUrl, validityHours
    - [x] Обработать ошибки с логированием

---

## 5. Контроллеры

- [ ] **Обновить AuthController.java**
  - [ ] Добавить эндпоинт `GET /api/auth/verify-email?token={token}`:
    - [ ] Вызвать `emailVerificationService.verifyEmail(token)`
    - [ ] Вернуть 200 OK с сообщением об успехе
    - [ ] Swagger документация
  - [ ] Добавить эндпоинт `POST /api/auth/resend-verification`:
    - [ ] Принимать DTO с email
    - [ ] Вызвать `emailVerificationService.resendVerificationEmail(email)`
    - [ ] Вернуть 200 OK с сообщением
    - [ ] Swagger документация

---

## 6. DTO

- [ ] **Создать ResendVerificationRequest.java**
  - [ ] Record с полем `email` (String)
  - [ ] Валидация: @NotBlank, @Email

---

## 7. Исключения

- [x] **Создать в пакете domain/user/exception:**
  - [x] `InvalidVerificationTokenException` - токен не найден, истек или использован
  - [x] `EmailAlreadyVerifiedException` - email уже подтвержден
  - [x] `TooManyRequestsException` - слишком частые запросы на повторную отправку

- [ ] **Создать в пакете domain/user/exception:**
  - [ ] `EmailNotVerifiedException` - email не подтвержден (для блокировки заказов)

---

## 8. Обработка ошибок

- [x] **Обновить глобальный ExceptionHandler**
  - [x] Добавить обработчик `InvalidVerificationTokenException` → 400 Bad Request
  - [x] Добавить обработчик `EmailAlreadyVerifiedException` → 409 Conflict
  - [x] Добавить обработчик `TooManyRequestsException` → 429 Too Many Requests
  - [ ] Добавить обработчик `EmailNotVerifiedException` → 403 Forbidden

---

## 9. Email шаблон

- [ ] **Создать verification-email.html в resources/templates**
  - [ ] Использовать стиль существующего шаблона order-ready-email.html
  - [ ] Брендинг "Айболит" с градиентом и цветовой схемой
  - [ ] Кнопка "Подтвердить email" с ссылкой на verificationUrl
  - [ ] Альтернативная текстовая ссылка (если кнопка не работает)
  - [ ] Указать срок действия: "Ссылка действительна в течение 1 часа"
  - [ ] Предупреждение: "Если вы не регистрировались, проигнорируйте письмо"

---

## 10. Конфигурация

- [x] **Обновить application.yaml**
  - [x] Добавить секцию `app.email-verification`:
    - [x] `token-validity-hours: 1`
    - [x] `base-url: ${APP_BASE_URL:http://localhost:8080}`

- [x] **Обновить .env.example**
  - [x] Добавить переменную `APP_BASE_URL=http://localhost:8080`

- [ ] **Включить @EnableScheduling**
  - [ ] Проверить что включено в AsyncConfig.java или главном классе

---

## 11. Ограничение функционала для неверифицированных пользователей

- [ ] **Со��дать EmailNotVerifiedException**
  - [ ] Исключение для блокировки заказов

- [ ] **Обновить OrderService.java**
  - [ ] В методе создания заказа (для авторизованных пользователей):
    - [ ] Проверить `user.getEmailVerified()`
    - [ ] Если false → выбросить `EmailNotVerifiedException`
    - [ ] Для гостей (неавторизованных) проверка не нужна

---

## 12. API для фронтенда

- [ ] **Обновить UserResponse.java**
  - [ ] Добавить поле `emailVerified` (boolean)
  - [ ] Обновить UserMapper

---

## 13. Тестирование

- [ ] **Unit тесты**
  - [ ] `EmailVerificationServiceTest`:
    - [ ] Тест генерации и отправки токена
    - [ ] Тест успешной верификации
    - [ ] Тест с истекшим токеном
    - [ ] Тест с использованным токеном
    - [ ] Тест повторной отправки с rate limiting (1 минута)
    - [ ] Тест попытки верификации уже верифицированного email

- [ ] **Integration тесты**
  - [ ] Тест полного flow: регистрация → получение токена → верификация
  - [ ] Тест эндпоинтов `/verify-email` и `/resend-verification`
  - [ ] Тест блокировки создания заказа для неверифицированного пользователя

---

## 14. Документация

- [ ] **Обновить Swagger/OpenAPI**
  - [ ] Документировать новые эндпоинты с примерами запросов/ответов
  - [ ] Указать все возможные коды ответов и их значения

---

## Итоговый чеклист файлов

### Новые файлы:
- [x] `V17__add_account_verification_fields_and_email_verification_tokens.sql`
- [x] `EmailVerificationToken.java` (model)
- [x] `EmailVerificationTokenRepository.java`
- [x] `EmailVerificationService.java`
- [x] `InvalidVerificationTokenException.java`
- [x] `EmailAlreadyVerifiedException.java`
- [x] `TooManyRequestsException.java`
- [ ] `EmailNotVerifiedException.java`
- [ ] `verification-email.html` (template)
- [ ] `EmailVerificationServiceTest.java`

### Изменяемые файлы:
- [x] `User.java` - добавить 2 поля
- [ ] `RegistrationService.java` - вызов отправки email
- [x] `EmailService.java` - новый метод отправки
- [ ] `AuthController.java` - 2 новых эндпоинта
- [ ] `OrderService.java` - проверка верификации при создании заказа
- [x] `GlobalExceptionHandler.java` - обработчики исключений
- [x] `application.yaml` - конфигурация верификации
- [x] `.env.example` - APP_BASE_URL
- [ ] `UserResponse.java` - добавить emailVerified
- [ ] `UserMapper.java` - обновить маппинг

---

## Требования

### Поведение системы:
- ✅ Разрешить вход неверифицированным пользователям
- ✅ Ограничить возможность оформления заказов для неверифицированных
- ✅ На фронте в ЛК отображать плашку о том, что аккаунт не подтвержден
- ✅ Срок действия токена: 1 час (отражено в письме)
- ✅ Rate limiting для повторной отправки: 1 минута
- ✅ Email используется как логин (изменение email не реализуется)
- ✅ Существующим пользователям пр��ст��вить верификацию автоматически

### Текст ошибки при попытке создать заказ:
**"Для оформления заказа необходимо подтвердить email адрес"**

---

## Production best practices

✅ **Безопасность:**
- Криптографически стойкие токены (SecureRandom + Base64)
- Токены с ограниченным сроком действия (1 час)
- Одноразовые токены (Флаг `is_used`)
- Инвалидация старых токенов при генерации новых

✅ **Производительность:**
- Индексы на часто запрашиваемые поля
- Асинхронная отправка email
- Scheduled задача для очистки истекших токенов

✅ **Надежность:**
- Транзакционность операций
- Обработка ошибок отправки email
- Rate limiting для повторной отправки (1 минута)
- Логирование всех операций

✅ **Масштабируемость:**
- Отдельная таблица для токенов
- Возможность горизонтального масштабирован��я
- Stateless подход

✅ **UX:**
- Понятные сообщения об ошибках
- Красивый HTML email шаблон
- Возможность повторной отправки
- Информация о сроке действия ссылки

✅ **Maintainability:**
- Чистая архитектура
- Разделение ответственности
- Документация через Swagger
- Миграции БД через Flyway