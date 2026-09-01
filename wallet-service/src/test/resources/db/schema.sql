CREATE SCHEMA IF NOT EXISTS wallet;

SET search_path TO wallet, public;

-- Основные таблицы

-- 1. Таблица кредитных карт (credit_cards)
CREATE TABLE IF NOT EXISTS public.credit_cards (
    id UUID NOT NULL,
    wallet_id UUID,
    number VARCHAR(19) NOT NULL,               -- С учетом формата "0000 0000 0000 0000"
    cvv VARCHAR(3) NOT NULL,
    expiration_date VARCHAR(5) NOT NULL,       -- Хранение "MM/yy" для вашего JPA-конвертера
    type VARCHAR(50) NOT NULL,                 -- Enum как строка (CardType)
    balance NUMERIC(18, 2) NOT NULL,           -- Точное соответствие precision и scale
    created_at TIMESTAMP NOT NULL,                -- Поле из InstantEmbeddedData
    updated_at TIMESTAMP NOT NULL,                -- Поле из InstantEmbeddedData
    CONSTRAINT pk_credit_cards PRIMARY KEY (id)
);

-- 2. Таблица деталей кошелька (wallet_details)
CREATE TABLE IF NOT EXISTS public.wallet_details (
    id UUID NOT NULL,
    wallet_id UUID,
    creator VARCHAR(255) NOT NULL,
    modifier VARCHAR(255) NOT NULL,
    archived_at BIGINT,
    currency_code VARCHAR(3) NOT NULL,
    wallet_status VARCHAR(18) NOT NULL,        -- Enum как строка (WalletStatus)
    created_at TIMESTAMP NOT NULL,                -- Поле из InstantEmbeddedData
    updated_at TIMESTAMP NOT NULL,                -- Поле из InstantEmbeddedData
    CONSTRAINT pk_wallet_details PRIMARY KEY (id)
);

-- 3. Таблица кошельков (wallets)
CREATE TABLE IF NOT EXISTS public.wallets (
    id UUID NOT NULL,
    person_id UUID NOT NULL,
    wallet_details_id UUID NOT NULL,    -- Хранит id из таблицы wallet_details (@OneToOne)
    archived_at TIMESTAMP,
    credit_card_id UUID NOT NULL,       -- Хранит id из таблицы credit_cards (@OneToOne)
    created_at TIMESTAMP NOT NULL,                   -- Поле из InstantEmbeddedData
    updated_at TIMESTAMP NOT NULL,                   -- Поле из InstantEmbeddedData
    CONSTRAINT pk_wallets PRIMARY KEY (id),
    CONSTRAINT fk_user_details FOREIGN KEY (wallet_details_id) REFERENCES public.wallet_details(id),
    CONSTRAINT fk_credit_cards FOREIGN KEY (credit_card_id) REFERENCES public.credit_cards(id)
);

-- 4. Таблица метаданных транзакций
CREATE TABLE IF NOT EXISTS public.transaction_metadata (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

-- 5. Таблица депозитных транзакций
CREATE TABLE IF NOT EXISTS public.deposit_transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata BIGINT NOT NULL,
    CONSTRAINT fk_deposit_transactions_metadata FOREIGN KEY (metadata) REFERENCES public.transaction_metadata(id)
);

-- 6. Таблица транзакций перевода
CREATE TABLE IF NOT EXISTS public.transfer_transactions (
    id UUID PRIMARY KEY,
    sender_wallet_id UUID NOT NULL,
    recipient_wallet_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata BIGINT NOT NULL,
    CONSTRAINT fk_transfer_transactions_metadata FOREIGN KEY (metadata) REFERENCES public.transaction_metadata(id)
);

-- 7. Таблица транзакций вывода
CREATE TABLE IF NOT EXISTS public.withdraw_transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata BIGINT NOT NULL,
    CONSTRAINT fk_withdraw_transactions_metadata FOREIGN KEY (metadata) REFERENCES public.transaction_metadata(id)
);

-- Аудит:

-- 1. Главная служебная таблица ревизий (Генерируется Envers по умолчанию)
CREATE TABLE IF NOT EXISTS public.revinfo (
    rev BIGINT NOT NULL,               -- Идентификатор ревизии (ID транзакции)
    revtstmp BIGINT,                    -- Время совершения транзакции (в миллисекундах)
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

-- 2. Таблица аудита кредитных карт (credit_cards_aud)
CREATE TABLE IF NOT EXISTS public.credit_cards_aud (
    id UUID NOT NULL,
    rev BIGINT NOT NULL REFERENCES public.revinfo(rev),               -- Ссылка на номер ревизии из revinfo
    revtype SMALLINT NOT NULL,          -- Тип операции (0 - INSERT, 1 - UPDATE, 2 - DELETE)
    wallet_id UUID,
    number VARCHAR(19),                 -- Все поля данных в таблицах аудита nullable,
    cvv VARCHAR(3),                     -- так как при DELETE-операции записывается только ID
    expiration_date VARCHAR(5),
    type VARCHAR(50),
    balance NUMERIC(14, 2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_credit_cards_aud PRIMARY KEY (id, rev)
);

-- 3. Таблица аудита деталей кошелька (wallet_details_aud)
CREATE TABLE IF NOT EXISTS public.wallet_details_aud (
    id UUID NOT NULL,
    rev BIGINT NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT NOT NULL,
    wallet_id UUID,
    creator VARCHAR(255),
    modifier VARCHAR(255),
    archived_at TIMESTAMP,
    currency_code VARCHAR(3),
    wallet_status VARCHAR(18),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_wallet_details_aud PRIMARY KEY (id, rev)
);

-- 4. Таблица аудита кошельков (wallets_aud)
CREATE TABLE IF NOT EXISTS public.wallets_aud (
    id UUID NOT NULL,
    rev BIGINT NOT NULL REFERENCES public.revinfo(rev),
    revtype SMALLINT NOT NULL,
    person_id UUID,
    wallet_details_id UUID,      -- Становится простой колонкой, хранящей ID
    archived_at TIMESTAMP,
    credit_card_id UUID,         -- Становится простой колонкой, хранящей ID
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT pk_wallets_aud PRIMARY KEY (id, rev)
);