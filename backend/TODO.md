# План реализации подтверждения аккаунта по email

## 1. Изменения в базе данных

### Миграция V17: Добавление полей верификации в таблицу users
- [ ] Добавить поле `email_verified BOOLEAN NOT NULL DEFAULT FALSE`
- [ ] Добавить поле `email_verified_at TIMESTAMP NULL`
- [ ] Проставить всем существующим пользователям `email_verified = TRUE` и `email_verified_at = CURRENT_TIMESTAMP`

### Миграция V18: Создание таблицы email_verification_tokens
- [ ] Создать таблицу с полями:
  - `id` (PK, IDENTITY)
  - `user_id` (FK на users с ON DELETE CASCADE)
  - `token` (VARCHAR(255), UNIQUE, NOT NULL)
  - `expires_at` (TIMESTAMP, NOT NULL)
  - `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)
  - `verified_at` (TIMESTAMP, NULL)
  - `is_used` (BOOLEAN, NOT NULL, DEFAULT FALSE)
- [ ] Создать индекс на `token` с условием `WHERE is_used = FALSE`
- [ ] Создать индекс на `expires_at` с условием `WHERE is_used = FALSE`
- [ ] Создать индекс на `user_id`

---

## 2. Модели и сущности

- [ ] **Обновить User.java**
  - Добавить поле `emailVerified` (Boolean, default false)
  - Добавить поле `emailVerifiedAt` (Instant, nullable)

- [ ] **Создать EmailVerificationToken.java**
  - Entity с маппингом на таблицу `email_verification_tokens`
  - ManyToOne связь с User (LAZY)
  - Методы `isExpired()` и `isValid()`

---

## 3. Репозитории

- [ ] **Создать EmailVerificationTokenRepository.java**
  - Метод `findByTokenAndIsUsedFalse(String token)`
  - Метод `findFirstByUserAndIsUsedFalseOrderByCreatedAtDesc(User user)`
  - Метод `deleteExpiredTokens(@Param("now") Instant now)` с @Modifying
  - Метод `invalidateAllUserTokens(@Param("user") User user)` с @Modifying

---

## 4. Сервисы

- [ ] **Создать EmailVerificationService.java**
  - Метод `sendVerificationEmail(User user)`:
    - Инвалидировать все старые токены пользователя
    - Сгенерировать криптографически стойкий токен (SecureRandom + Base64, 32 байта)
    - Создать запись в БД с expiration = 1 час
    - Отправить email через EmailService
  - Метод `verifyEmail(String token)`:
    - Найти токен, проверить что не использован и не истек
    - Проверить что email еще не верифицирован
    - Установить `emailVerified = true`, `emailVerifiedAt = now()`
    - Пометить токен как использованный
  - Метод `resendVerificationEmail(String email)`:
    - Найти пользователя по email
    - Проверить что email не верифицирован
    - Проверить rate limit: последняя отправка была не менее 1 минуты назад
    - Вызвать `sendVerificationEmail(user)`
  - Метод `cleanupExpiredTokens()` с @Scheduled (cron: каждый день в 2:00 AM)
  - Конфигурация: `token-validity-hours = 1`, `base-url` из properties

- [ ] **Обновить RegistrationService.java**
  - В методе `registerUser()` после сохранения пользователя:
    - Установить `emailVerified = false` (явно)
    - Вызвать `emailVerificationService.sendVerificationEmail(user)`

- [ ] **Обновить EmailService.java**
  - Добавить метод `sendVerificationEmail(String to, String firstName, String verificationUrl, int validityHours)`:
    - Асинхронный (@Async)
    - Использовать Thymeleaf шаблон "email-verification"
    - Передать в контекст: firstName, verificationUrl, validityHours
    - Обработать ошибки с логированием

---

## 5. Контроллеры

- [ ] **Обновить AuthController.java**
  - Добавить эндпоинт `GET /api/auth/verify-email?token={token}`:
    - Вызвать `emailVerificationService.verifyEmail(token)`
    - Вернуть 200 OK с сообщением об успехе
    - Swagger документация
  - Добавить эндпоинт `POST /api/auth/resend-verification`:
    - Принимать DTO с email
    - Вызвать `emailVerificationService.resendVerificationEmail(email)`
    - Вернуть 200 OK с сообщением
    - Swagger документация

---

## 6. DTO

- [ ] **Создать ResendVerificationRequest.java**
  - Record с полем `email` (String)
  - Валидация: @NotBlank, @Email

---

## 7. Исключения

- [ ] **Создать в пакете domain/auth/exception:**
  - `InvalidVerificationTokenException` - токен не найден, истек или использован
  - `EmailAlreadyVerifiedException` - email уже подтвержден
  - `TooManyRequestsException` - слишком частые запросы на повторную отправку
  - `EmailSendingException` - ошибка отправки email
  - `EmailNotVerifiedException` - email не подтвержден (для блокировки заказов)

---

## 8. Обработка ошибок

- [ ] **Обновить глобальный ExceptionHandler**
  - Добавить обработчик `InvalidVerificationTokenException` → 400 Bad Request
  - Добавить обработчик `EmailAlreadyVerifiedException` → 409 Conflict
  - Добавить обработчик `TooManyRequestsException` → 429 Too Many Requests
  - Добавить обработчик `EmailSendingException` → 500 Internal Server Error
  - Добавить обработчик `EmailNotVerifiedException` → 403 Forbidden

---

## 9. Email шаблон

- [ ] **Создать email-verification.html в resources/templates**
  - Использовать стиль существующего шаблона order-ready-email.html
  - Брендинг "Айболит" с градиентом и цветовой схемой
  - Кнопка "Подтвердить email" с ссылкой на verificationUrl
  - Альтернативная текстовая ссылка (если кнопка не работает)
  - Указать срок действия: "Ссылка действительна в течение 1 часа"
  - Предупреждение: "Если вы не регистрировались, проигнорируйте письмо"

---

## 10. Конфигурация

- [ ] **Обновить application.yaml**
  - Добавить секцию `app.email-verification`:
    - `token-validity-hours: 1`
    - `base-url: ${APP_BASE_URL:http://localhost:8080}`

- [ ] **Обновить .env.example**
  - Добавить переменную `APP_BASE_URL=http://localhost:8080`

- [ ] **Включить @EnableScheduling**
  - Добавить аннотацию в `AsyncConfig.java` или главный класс приложения

---

## 11. Ограничение функционала для неверифицированных пользователей

- [ ] **Обновить OrderService.java**
  - В методе создания заказа (для авторизованных пользователей):
    - Проверить `user.getEmailVerified()`
    - Если false → выбросить `EmailNotVerifiedException`
    - Для гостей (неавторизованных) проверка не нужна

---

## 12. API для фронтенда

- [ ] **Обновить UserController.java или создать эндпоинт**
  - Добавить в DTO `UserResponse` поле `emailVerified` (boolean)
  - Фронтенд сможет получать статус верификации через GET /api/users/me или аналогичный эндпоинт

---

## 13. Тестирование

- [ ] **Unit тесты**
  - `EmailVerificationServiceTest`:
    - Тест генерации и отправки токена
    - Тест успешной верификации
    - Тест с истекшим токеном
    - Тест с использованным токеном
    - Тест повторной отправки с rate limiting (1 минута)
    - Тест попытки верификации уже верифицированного email

- [ ] **Integration тесты**
  - Тест полного flow: регистрация → получение токена → верификация
  - Тест эндпоинтов `/verify-email` и `/resend-verification`
  - Тест блокировки создания заказа для неверифицированного пользователя

---

## 14. Документация

- [ ] **Обновить Swagger/OpenAPI**
  - Документировать новые эндпоинты с примерами запросов/ответов
  - Указать все возможные коды ответов и их значения

---

## Итоговый чеклист файлов

### Новые файлы (13):
- [ ] `V17__add_email_verification_fields.sql`
- [ ] `V18__create_email_verification_tokens_table.sql`
- [ ] `EmailVerificationToken.java` (model)
- [ ] `EmailVerificationTokenRepository.java`
- [ ] `EmailVerificationService.java`
- [ ] `ResendVerificationRequest.java` (DTO)
- [ ] `InvalidVerificationTokenException.java`
- [ ] `EmailAlreadyVerifiedException.java`
- [ ] `TooManyRequestsException.java`
- [ ] `EmailSendingException.java`
- [ ] `EmailNotVerifiedException.java`
- [ ] `email-verification.html` (template)
- [ ] `EmailVerificationServiceTest.java`

### Изменяемые файлы (9):
- [ ] `User.java` - добавить 2 поля
- [ ] `RegistrationService.java` - вызов отправки email
- [ ] `EmailService.java` - новый метод отправки
- [ ] `AuthController.java` - 2 новых эндпоинта
- [ ] `OrderService.java` - проверка верификации при создании заказа
- [ ] `GlobalExceptionHandler.java` - обработчики новых исключений
- [ ] `application.yaml` - конфигурация верификации
- [ ] `.env.example` - APP_BASE_URL
- [ ] `AsyncConfig.java` или главный класс - @EnableScheduling

---

## Требования

### Поведение системы:
- ✅ Разрешить вход неверифицированным пользователям
- ✅ Ограничить возможность оформления заказов для неверифицированных
- ✅ На фронте в ЛК отображать плашку о том, что аккаунт не подтвержден
- ✅ Срок действия токена: 1 час (отражено в письме)
- ✅ Rate limiting для повторной отправки: 1 минута
- ✅ Email используется как логин (изменение email не реализуется)
- ✅ Существующим пользователям проставить верификацию автоматически

### Текст ошибки при попытке создать заказ:
**"Для оформления заказа необходимо подтвердить email адрес"**

---

## Production best practices

✅ **Безопасность:**
- Криптографически стойкие токены (SecureRandom + Base64)
- Токены с ограниченным сроком действия (1 час)
- Одноразовые токены (флаг `is_used`)
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
- Возможность горизонтального масштабирования
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
