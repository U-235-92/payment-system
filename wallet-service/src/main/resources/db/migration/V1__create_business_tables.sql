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

-- 4. Таблица метаданных транзакций
CREATE TABLE IF NOT EXISTS public.transaction_metadata (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR NOT NULL,
    timestamp BIGINT NOT NULL
);

-- 5. Таблица депозитных транзакций
CREATE TABLE IF NOT EXISTS public.deposit_transactions (
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata BIGINT NOT NULL,
    CONSTRAINT fk_deposit_transactions_metadata FOREIGN KEY (metadata) REFERENCES public.transaction_metadata(id)
);

-- 6. Таблица транзакций перевода
CREATE TABLE IF NOT EXISTS public.transfer_transactions (
    id VARCHAR(36) PRIMARY KEY,
    sender_wallet_id VARCHAR(36) NOT NULL,
    recipient_wallet_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata BIGINT NOT NULL,
    CONSTRAINT fk_transfer_transactions_metadata FOREIGN KEY (metadata) REFERENCES public.transaction_metadata(id)
);

-- 7. Таблица транзакций вывода
CREATE TABLE IF NOT EXISTS public.withdraw_transactions (
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata BIGINT NOT NULL,
    CONSTRAINT fk_withdraw_transactions_metadata FOREIGN KEY (metadata) REFERENCES public.transaction_metadata(id)
);