-- ПОДГОТОВКА С РОЛЯМИ И ДРУГИМИ СКУЧНЫМИ ШТУКАМИ --

-- CREATE USER pharmacy_app WITH
--     NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD ТУТ_ПАРОЛЬ_ПРИКРУТИТЬ;

CREATE SCHEMA IF NOT EXISTS pharmacy;

-- отдельная схема под миграции flyway.
-- Почему не public, которая стоит по умолчанию? Придётся выдать пользователю права на public, а это не есть хорошо
-- Почему не в pharmacy? Не хочется засирать
CREATE SCHEMA IF NOT EXISTS flyway;

GRANT ALL ON SCHEMA pharmacy TO pharmacy_app;
GRANT ALL ON SCHEMA flyway TO pharmacy_app;

GRANT ALL ON ALL TABLES IN SCHEMA pharmacy TO pharmacy_app;