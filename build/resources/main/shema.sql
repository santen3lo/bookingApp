CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(64) UNIQUE NOT NULL,
    password_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 2. Таблица приборов
CREATE TABLE IF NOT EXISTS instruments (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(64) NOT NULL
);

-- 3. Таблица бронирований
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES instruments(id) ON DELETE RESTRICT,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 4. Таблица выдач
CREATE TABLE IF NOT EXISTS checkouts (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES instruments(id) ON DELETE RESTRICT,
    userId BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    comment VARCHAR(128),
    taken_at TIMESTAMP NOT NULL DEFAULT NOW(),
    returned_at TIMESTAMP,
    return_condition VARCHAR(32),
    ownerUsername VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

SELECT constraint_name
FROM information_schema.table_constraints
WHERE table_name = 'instruments' AND constraint_type = 'UNIQUE';

SELECT MAX(id) FROM instruments;

-- Какой следующий id выдаст счётчик?
SELECT last_value FROM instruments_id_seq;
SELECT setval('instruments_id_seq', (SELECT MAX(id) FROM instruments));