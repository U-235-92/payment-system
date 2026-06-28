CREATE SCHEMA IF NOT EXISTS wallet;

SET search_path TO wallet, public;

-- 1. Таблица кредитных карт (credit_cards)
CREATE TABLE IF NOT EXISTS public.credit_cards (
    id VARCHAR(36) NOT NULL,
    wallet_id VARCHAR(36),
    number VARCHAR(19) NOT NULL,               -- С учетом формата "0000 0000 0000 0000"
    cvv VARCHAR(3) NOT NULL,
    expiration_date VARCHAR(5) NOT NULL,       -- Хранение "MM/yy" для вашего JPA-конвертера
    type VARCHAR(50) NOT NULL,                 -- Enum как строка (CardType)
    balance NUMERIC(18, 2) NOT NULL,           -- Точное соответствие precision и scale
    created BIGINT NOT NULL,                   -- Поле из InstantEmbeddedData
    updated BIGINT NOT NULL,                   -- Поле из InstantEmbeddedData
    CONSTRAINT pk_credit_cards PRIMARY KEY (id)
);

-- 2. Таблица деталей кошелька (wallet_details)
CREATE TABLE IF NOT EXISTS public.wallet_details (
    id VARCHAR(36) NOT NULL,
    wallet_id VARCHAR(36),
    creator VARCHAR(255) NOT NULL,
    modifier VARCHAR(255) NOT NULL,
    archived_at BIGINT,
    currency_code VARCHAR(3) NOT NULL,
    wallet_status VARCHAR(18) NOT NULL,        -- Enum как строка (WalletStatus)
    created BIGINT NOT NULL,                   -- Поле из InstantEmbeddedData
    updated BIGINT NOT NULL,                   -- Поле из InstantEmbeddedData
    CONSTRAINT pk_wallet_details PRIMARY KEY (id)
);

-- 3. Таблица кошельков (wallets)
CREATE TABLE IF NOT EXISTS public.wallets (
    id VARCHAR(36) NOT NULL,
    person_id VARCHAR(36) NOT NULL,
    wallet_details_id VARCHAR(36) NOT NULL,    -- Хранит id из таблицы wallet_details (@OneToOne)
    archived_at BIGINT,
    credit_card_id VARCHAR(36) NOT NULL,       -- Хранит id из таблицы credit_cards (@OneToOne)
    created BIGINT NOT NULL,                   -- Поле из InstantEmbeddedData
    updated BIGINT NOT NULL,                   -- Поле из InstantEmbeddedData
    CONSTRAINT pk_wallets PRIMARY KEY (id),
    CONSTRAINT fk_user_details FOREIGN KEY (wallet_details_id) REFERENCES public.wallet_details(id),
    CONSTRAINT fk_credit_cards FOREIGN KEY (credit_card_id) REFERENCES public.credit_cards(id)
);

-- 4. Таблица транзакций (transactions)
CREATE TABLE IF NOT EXISTS public.transactions (
    id VARCHAR(36) NOT NULL,
    type VARCHAR(255) NOT NULL,                -- Enum как строка (EventType)
    status VARCHAR(255) NOT NULL,              -- Enum как строка (TransactionStatus)
    timestamp BIGINT NOT NULL,
    processed BOOLEAN NOT NULL,
    trace_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_transaction PRIMARY KEY (id)
);

-- 5. Дополнительная таблица свойств для ElementCollection из Transaction
CREATE TABLE IF NOT EXISTS public.transaction_properties (
    transaction_id VARCHAR(36) NOT NULL,               -- Ссылка на id основной сущности
    property_key VARCHAR(255) NOT NULL,                -- Ключ карты Map (key)
    property_value TEXT,                               -- Значение карты Map (value)
    CONSTRAINT pk_transaction_properties PRIMARY KEY (transaction_id, property_key),
    CONSTRAINT fk_transaction_properties FOREIGN KEY (transaction_id) REFERENCES public.transactions(id)
);

-- 1. Главная служебная таблица ревизий (Генерируется Envers по умолчанию)
CREATE TABLE IF NOT EXISTS public.revinfo (
    rev BIGINT NOT NULL,               -- Идентификатор ревизии (ID транзакции)
    revtstmp BIGINT,                    -- Время совершения транзакции (в миллисекундах)
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

-- 2. Таблица аудита кредитных карт (credit_cards_aud)
CREATE TABLE IF NOT EXISTS public.credit_cards_aud (
    id VARCHAR(36) NOT NULL,
    rev BIGINT NOT NULL REFERENCES public.revinfo(rev),               -- Ссылка на номер ревизии из revinfo
    revtype SMALLINT NOT NULL,          -- Тип операции (0 - INSERT, 1 - UPDATE, 2 - DELETE)
    wallet_id VARCHAR(36),
    number VARCHAR(19),                 -- Все поля данных в таблицах аудита nullable,
    cvv VARCHAR(3),                     -- так как при DELETE-операции записывается только ID
    expiration_date VARCHAR(5),
    type VARCHAR(50),
    balance NUMERIC(14, 2),
    created BIGINT,
    updated BIGINT,
    CONSTRAINT pk_credit_cards_aud PRIMARY KEY (id, rev)
);


-- 3. Таблица аудита деталей кошелька (wallet_details_aud)
CREATE TABLE IF NOT EXISTS public.wallet_details_aud (
    id VARCHAR(36) NOT NULL,
    rev BIGINT NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT NOT NULL,
    wallet_id VARCHAR(36),
    creator VARCHAR(255),
    modifier VARCHAR(255),
    archived_at BIGINT,
    currency_code VARCHAR(3),
    wallet_status VARCHAR(18),
    created BIGINT,
    updated BIGINT,
    CONSTRAINT pk_wallet_details_aud PRIMARY KEY (id, rev)
);


-- 4. Таблица аудита кошельков (wallets_aud)
CREATE TABLE IF NOT EXISTS public.wallets_aud (
    id VARCHAR(36) NOT NULL,
    rev BIGINT NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT NOT NULL,
    person_id VARCHAR(36),
    wallet_details_id VARCHAR(36),      -- Становится простой колонкой, хранящей ID
    archived_at BIGINT,
    credit_card_id VARCHAR(36),         -- Становится простой колонкой, хранящей ID
    created BIGINT,
    updated BIGINT,
    CONSTRAINT pk_wallets_aud PRIMARY KEY (id, rev)
);