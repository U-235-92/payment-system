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

-- 4. Таблица событий аутбокса (outbox_events)
CREATE TABLE IF NOT EXISTS public.outbox_events (
    transaction_id VARCHAR(36) NOT NULL,
    type VARCHAR(255) NOT NULL,                -- Enum как строка (EventType)
    status VARCHAR(255) NOT NULL,              -- Enum как строка (TransactionStatus)
    timestamp BIGINT NOT NULL,
    processed BOOLEAN NOT NULL,
    CONSTRAINT pk_outbox_events PRIMARY KEY (transaction_id)
);

-- 5. Дополнительная таблица свойств для ElementCollection из OutboxEvent
CREATE TABLE IF NOT EXISTS public.outbox_event_properties (
    outbox_event_transaction_id VARCHAR(36) NOT NULL, -- Ссылка на id основной сущности
    property_key VARCHAR(255) NOT NULL,                -- Ключ карты Map (key)
    property_value TEXT,                               -- Значение карты Map (value)
    CONSTRAINT pk_outbox_event_properties PRIMARY KEY (outbox_event_transaction_id, property_key),
    CONSTRAINT fk_outbox_event_properties FOREIGN KEY (outbox_event_transaction_id) REFERENCES public.outbox_events(transaction_id)
);