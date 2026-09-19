-- Последовательность для генерации ID ревизий
-- (Не нужно т.к. rev в таблице revinfo генерируется на стороне сервиса wallet-service)
-- CREATE SEQUENCE IF NOT EXISTS public.revinfo_seq START WITH 1 INCREMENT BY 1;

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