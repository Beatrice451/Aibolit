-- -- ПОДГОТОВКА С РОЛЯМИ И ДРУГИМИ СКУЧНЫМИ ШТУКАМИ --
--
-- CREATE USER pharmacy_app WITH
--     NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD ТУТ_ПАРОЛЬ_ПРИКРУТИТЬ;
--
-- CREATE SCHEMA IF NOT EXISTS pharmacy;
--
-- -- отдельная схема под миграции flyway.
-- -- Почему не public, которая стоит по умолчанию? Придётся выдать пользователю права на public, а это не есть хорошо
-- -- Почему не в pharmacy? Не хочется засирать
-- CREATE SCHEMA IF NOT EXISTS flyway;
--
-- GRANT ALL ON SCHEMA pharmacy TO pharmacy_app;
-- GRANT ALL ON SCHEMA flyway TO pharmacy_app;
--
-- GRANT ALL ON ALL TABLES IN SCHEMA pharmacy TO pharmacy_app;

-- ПЕРЕЧИСЛЕНИЯ --

CREATE TYPE order_status AS ENUM ( -- статусы заказов, вместо TEXT
    'NEW', -- заказ создан, сборка не начата
    'ASSEMBLING', -- сборка заказа
    'READY', -- готов к выдаче
    'DELIVERY_PENDING', -- ожидание доставки со склада в аптеку
    'DELIVERY_DELAYED', -- доставка со склада задерживается
    'COMPLETED', -- выполнен
    'CANCELLED_USER', -- отменен пользователем
    'CANCELLED_SYSTEM', -- отменен системой
    'EXPIRED' -- истёк (не забрали заказ например)
    );


CREATE TYPE loyalty_transactions_type AS ENUM ( -- аналогично
    'accrual', -- страндартное начисление за покупки или акции
    'redemption', -- списание
    'expiration', -- сгорание. Это пока не реализовано, но заделом на будущее почему бы и нет?
    'correction' -- ручная корректировка. Для ситуаций вроде "заказ повреждён, мы в качестве компенсации начислим вам 100500 баллов"
    );

CREATE TYPE order_owners_type AS ENUM (
    'USER',
    'GUEST'
    );


-- ПОЛЬЗОВАТЕЛИ --

-- Таблица аутентификации.
-- Самая интересная часть: пользователь не владеет заказом.
-- Им будет владеть абстракция, а users нужна для аутентификации, не более
-- Такие сложности связаны с наличием "гостя" - незарегистрированным пользователем
CREATE TABLE IF NOT EXISTS pharmacy.users
(
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    phone         VARCHAR(20) UNIQUE  NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Собственно абстракция владельца заказа. Это либо пользователь, либо гость
CREATE TABLE pharmacy.order_owners
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_type  order_owners_type NOT NULL,
    user_id     INT               NULL REFERENCES pharmacy.users (id),
    guest_email VARCHAR(255)      NULL,
    guest_phone VARCHAR(32)       NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    CHECK ( (owner_type = 'USER' AND user_id IS NOT NULL) OR
            (owner_type = 'GUEST' AND (guest_email IS NOT NULL OR guest_phone IS NOT NULL)))
);



CREATE TABLE IF NOT EXISTS pharmacy.roles
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pharmacy.user_roles
(
    user_id     INT NOT NULL REFERENCES pharmacy.users (id),
    role_id     INT NOT NULL REFERENCES pharmacy.roles (id),
    assigned_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);


-- ТОВАРЫ И ВСЯКОЕ --


CREATE TABLE IF NOT EXISTS pharmacy.categories
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name      TEXT NOT NULL UNIQUE,
    parent_id INT REFERENCES pharmacy.categories (id)
);

CREATE TABLE IF NOT EXISTS pharmacy.products
(
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id  INT            NOT NULL REFERENCES pharmacy.categories (id),
    name         TEXT           NOT NULL,
    description  TEXT,
    manufacturer TEXT           NOT NULL,
    price        DECIMAL(10, 2) NOT NULL CHECK ( price > 0 ),
    image_url    TEXT,
    is_active    BOOLEAN        NOT NULL DEFAULT TRUE
);



CREATE TABLE IF NOT EXISTS pharmacy.medicines
(
    id                    INT PRIMARY KEY REFERENCES pharmacy.products (id),
    dosage                INT      NOT NULL,                     -- дозировка, видимо в миллиграммах
    requires_prescription BOOLEAN  NOT NULL DEFAULT FALSE,       -- требуется ли рецепт при отпуске
    form                  TEXT     NOT NULL,                     -- форма выпуска (таблетки, суспензия, проч.)
    quantity              SMALLINT NOT NULL CHECK (quantity > 0) -- КОЛИЧЕСТВО ТАБЛЕТОК/КАПСУЛ/ЧЕГОНИБУДЬЕЩЁ В УПАКОВКЕ. НЕ НА СКЛАДЕ
);

CREATE TABLE IF NOT EXISTS pharmacy.pharmacies
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name      TEXT    NOT NULL,
    address   TEXT    NOT NULL,
    phone     TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS pharmacy.warehouses -- таблица под склады
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pharmacy_id INT REFERENCES pharmacy.pharmacies (id), -- может быть null, если склад привязан не к конкретной аптеке, а является общим например
    name        TEXT NOT NULL,
    address     TEXT NOT NULL

);


CREATE TABLE IF NOT EXISTS pharmacy.stocks
(
    product_id   INT NOT NULL REFERENCES pharmacy.products (id),
    warehouse_id INT NOT NULL REFERENCES pharmacy.warehouses (id),
    quantity     INT NOT NULL CHECK ( quantity >= 0 ),
    PRIMARY KEY (product_id, warehouse_id)
);


CREATE TABLE IF NOT EXISTS pharmacy.active_substances -- чтобы не хранить активные вещества лекарств в виде текста в той же таблице
(
    id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pharmacy.medicine_active_substances -- связывает лекарства и активные вещества
(
    medicine_id  INT REFERENCES pharmacy.medicines (id) ON DELETE CASCADE,
    substance_id INT REFERENCES pharmacy.active_substances (id),
    PRIMARY KEY (medicine_id, substance_id)

);

CREATE TABLE IF NOT EXISTS pharmacy.symptoms -- по аналогии с действующим веществом, но для симптомов
(
    id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS pharmacy.medicine_symptoms
(
    medicine_id INT REFERENCES pharmacy.medicines (id) ON DELETE CASCADE,
    symptom_id  INT REFERENCES pharmacy.symptoms (id),
    PRIMARY KEY (medicine_id, symptom_id)

);


-- ЗАКАЗЫ --

CREATE TABLE IF NOT EXISTS pharmacy.orders
(
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_owner_id INT            NOT NULL REFERENCES pharmacy.order_owners (id),
    pharmacy_id    INT            NOT NULL REFERENCES pharmacy.pharmacies (id), -- аптека, где выдают заказ
    status         order_status   NOT NULL,                                     -- статус заказа. Собирается, готов к выдаче, отменен, блаблабла
    total_amount   DECIMAL(11, 2) NOT NULL CHECK ( total_amount >= 0 ),         -- я уже забыл чё это. Походу чота с ценой. По-моему, цена до учёта скидок
    discount       DECIMAL(11, 2) CHECK ( discount IS NULL OR discount >= 0 ),  -- размер скидки в рублях
    final_amount   DECIMAL(11, 2) GENERATED ALWAYS AS (                         -- столбец не должен принимать значение с бэка. Значение вычисляется на уровне бд
        total_amount - COALESCE(discount, 0)                                    -- coalesce возвращает первое значение, которое не NULL, т.е. если скидки нет, то вернётся 0 (если он вдруг не передан явно)
        ) STORED,
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pharmacy.order_items
(
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id      INT           NOT NULL REFERENCES pharmacy.orders (id),
    product_id    INT           NOT NULL REFERENCES pharmacy.products (id),
    quantity      SMALLINT      NOT NULL CHECK ( quantity > 0 ),
    price_at_sale DECIMAL(9, 2) NOT NULL -- цена на момент продажи, чтобы не сломать историю в случае изменения цены в каталоге товаров
);

CREATE TABLE IF NOT EXISTS pharmacy.carts
(
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_owner_id INT       NOT NULL REFERENCES pharmacy.order_owners (id),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pharmacy.cart_items
(
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id    INT      NOT NULL REFERENCES pharmacy.carts (id),
    product_id INT      NOT NULL REFERENCES pharmacy.products (id),
    quantity   SMALLINT NOT NULL CHECK ( quantity > 0 )
);


-- ПРОГРАММА ЛОЯЛЬНОСТИ --

CREATE TABLE IF NOT EXISTS pharmacy.loyalty_programs -- что-то вроде уровней лояльности с разными плюшками
(
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           TEXT          NOT NULL,
    points_per_rub DECIMAL(5, 2) NOT NULL -- сколько баллов начисляется за один потраченный рубль
    -- мне лень выдумывать велосипед, поэтому условимся, что система лояльности работает тупо 1 балл == 1 рубль
);

CREATE TABLE IF NOT EXISTS pharmacy.loyalty_accounts
(
    id                  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             INT           NOT NULL UNIQUE REFERENCES pharmacy.users (id) ON DELETE CASCADE,
    program_id          INT           NOT NULL REFERENCES pharmacy.loyalty_programs (id),
    current_points      DECIMAL(9, 2) NOT NULL DEFAULT 0, -- текущий баланс бонусов. Точность (9, 2) на всякий случай. Лучше перебздеть
    total_points_earned DECIMAL(9, 2) NOT NULL DEFAULT 0, -- сколько всего было заработано баллов за всё время
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS pharmacy.loyalty_transactions
(
    id                 INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    loyalty_account_id INT                       NOT NULL REFERENCES pharmacy.loyalty_accounts (id),
    order_id           INT REFERENCES pharmacy.orders (id),
    loyalty_points     DECIMAL(9, 2)             NOT NULL,
    type               loyalty_transactions_type NOT NULL, -- тип операции. Зачисление, списание, сгорание и проч. Заменить на перечисление? Заменил
    description        TEXT,                               -- описание операции. На всякий случай
    created_at         TIMESTAMP                 NOT NULL DEFAULT NOW()
);


-- ИНДЕКСЫ --

CREATE INDEX IF NOT EXISTS idx_users_phone ON pharmacy.users (phone);
CREATE INDEX IF NOT EXISTS idx_products_category ON pharmacy.products (category_id);
CREATE INDEX IF NOT EXISTS idx_orders_user ON pharmacy.orders (order_owner_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON pharmacy.order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_orders_pharmacy_status
    ON pharmacy.orders (pharmacy_id, status);
CREATE INDEX IF NOT EXISTS idx_medicine_active_substance ON pharmacy.medicine_active_substances (substance_id);
CREATE INDEX IF NOT EXISTS idx_medicine_symptoms ON pharmacy.medicine_symptoms (symptom_id);



-- ФУНКЦИИ ДЛЯ ТРИГГЕРОВ --
CREATE OR REPLACE FUNCTION update_updated_at_column() -- обновление столбцов updated_at
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 1 --
CREATE OR REPLACE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON pharmacy.users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- 2 --
CREATE OR REPLACE TRIGGER update_orders_updated_at
    BEFORE UPDATE
    ON pharmacy.orders
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE OR REPLACE TRIGGER update_carts_updated_at
    BEFORE UPDATE
    ON pharmacy.carts
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

