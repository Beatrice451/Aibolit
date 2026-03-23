-- ПОДГОТОВКА С РОЛЯМИ И ДРУГИМИ СКУЧНЫМИ ШТУКАМИ --

CREATE SCHEMA IF NOT EXISTS pharmacy;
CREATE SCHEMA IF NOT EXISTS flyway;

GRANT ALL ON SCHEMA pharmacy TO pharmacy_app;
GRANT ALL ON SCHEMA flyway TO pharmacy_app;

GRANT ALL ON ALL TABLES IN SCHEMA pharmacy TO pharmacy_app;