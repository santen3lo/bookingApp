TRUNCATE users, instruments, bookings, checkouts RESTART IDENTITY CASCADE;


INSERT INTO users (login, password_hash, created_at) VALUES
('admin',      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', NOW()),
('labtech1',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', NOW()),
('researcher', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', NOW());

INSERT INTO instruments (type) VALUES
('BEAKER'), ('BOILING_FLASK'), ('VOLUMETRIC_FLASK'), ('TEST_TUBE'), ('PETRI_DISH'),
('WATCH_GLASS'), ('FUNNEL'), ('BURETTE'), ('PIPETTE'), ('DROPPER'),
('SPIRIT_LAMP'), ('HOT_PLATE'), ('WATER_BATH'), ('THERMOMETER'), ('SAND_BATH'),
('SCALE'), ('ANALYTICAL_BALANCE'), ('PH_METER'), ('CALORIMETER'), ('VISCOMETER'),
('FILTER_PAPER'), ('BUCHNER_FUNNEL'), ('BUNSEN_FLASK'), ('SIEVE'), ('CENTRIFUGE'),
('CHROMATOGRAPH'), ('TONG'), ('SPATULA'), ('IRON_RING'), ('WIRE_GAUZE'),
('SAFETY_GOGGLES'), ('GLOVES'), ('LAB_COAT'), ('FUME_HOOD'), ('INCUBATOR'), ('DESICCATOR');

INSERT INTO bookings (instrument_id, start_at, end_at, status, owner_id, created_at, updated_at) VALUES
(1, '2026-06-01 09:00:00', '2026-06-01 12:00:00', 'ACTIVE',    1, NOW(), NOW()),
(2, '2026-06-02 10:00:00', '2026-06-02 14:00:00', 'ACTIVE',    2, NOW(), NOW()),
(3, '2026-06-03 08:00:00', '2026-06-03 10:00:00', 'CANCELLED', 3, NOW(), NOW()),
(4, '2026-06-04 13:00:00', '2026-06-04 16:00:00', 'ACTIVE',    1, NOW(), NOW()),
(5, '2026-06-05 09:00:00', '2026-06-05 11:00:00', 'ACTIVE',    2, NOW(), NOW());

INSERT INTO checkouts (instrument_id, userId, comment, taken_at, returned_at, return_condition, ownerUsername, created_at) VALUES
(6, 2,   'Field test',   '2026-05-15 10:00:00', NULL, NULL, 'labtech1',   NOW()),
(7, 3, 'Lab work',     '2026-05-16 09:00:00', '2026-05-16 15:00:00', 'OK', 'researcher',  NOW());

SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1), true);
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

SELECT constraint_name FROM information_schema.table_constraints
WHERE table_name = 'instruments' AND constraint_type = 'UNIQUE';



-- 1️⃣ Очистка данных (порядок важен из-за FK)
DELETE FROM checkouts;
DELETE FROM bookings;
DELETE FROM instruments;
TRUNCATE checkouts, bookings, instruments RESTART IDENTITY CASCADE;

-- 2️⃣ Вставка разрешённых инструментов (получат id от 1 до 10)
-- ⚠️ Типы приведены к верхнему регистру, как в твоём Java-enum
INSERT INTO instruments (type) VALUES
( 'SCALE'),
( 'PETRI_DISH'),
( 'GLOVES'),
( 'MICROSCOPE'),
( 'SPIRIT_LAMP'),
( 'BEAKER'),
( 'PIPETTE'),
( 'TONG'),
('THERMOMETER'),
('FUNNEL');

-- 3️⃣ Бронирования (instrument_id: 1..10, owner_id: 1..3)
INSERT INTO bookings (instrument_id, start_at, end_at, status, owner_id, created_at, updated_at) VALUES
(1, '2026-06-01 09:00:00', '2026-06-01 12:00:00', 'ACTIVE',    1, NOW(), NOW()),
(2, '2026-06-02 10:00:00', '2026-06-02 14:00:00', 'ACTIVE',    2, NOW(), NOW()),
(3, '2026-06-03 08:00:00', '2026-06-03 10:00:00', 'CANCELLED', 3, NOW(), NOW()),
(4, '2026-06-04 13:00:00', '2026-06-04 16:00:00', 'ACTIVE',    1, NOW(), NOW()),
(5, '2026-06-05 09:00:00', '2026-06-05 11:00:00', 'ACTIVE',    2, NOW(), NOW()),
(6, '2026-06-06 14:00:00', '2026-06-06 18:00:00', 'ACTIVE',    3, NOW(), NOW()),
(7, '2026-06-07 10:00:00', '2026-06-07 13:00:00', 'CANCELLED', 1, NOW(), NOW()),
(8, '2026-06-08 09:00:00', '2026-06-08 12:00:00', 'ACTIVE',    2, NOW(), NOW()),
(9, '2026-06-09 11:00:00', '2026-06-09 15:00:00', 'ACTIVE',    3, NOW(), NOW()),
(10,'2026-06-10 08:00:00', '2026-06-10 10:00:00', 'ACTIVE',    1, NOW(), NOW());

-- 4️⃣ Выдачи (инструменты 1..10, пользователи 1..3)
INSERT INTO checkouts (instrument_id, userId, comment, taken_at, returned_at, return_condition, ownerUsername, created_at) VALUES
(1, 2, 'Field sampling',      '2026-05-15 10:00:00', NULL,             NULL,      'labtech1',   NOW()),
(2, 3, 'Lab work',            '2026-05-16 09:00:00', '2026-05-16 15:00:00', 'OK',     'researcher', NOW()),
(3, 1, 'Maintenance check',   '2026-05-17 08:30:00', '2026-05-17 12:30:00', 'DAMAGED', 'admin',      NOW()),
(4, 2, 'Routine test',        '2026-05-18 11:00:00', NULL,             NULL,      'labtech1',   NOW()),
(5, 3, 'Experiment #42',      '2026-05-19 14:00:00', NULL,             NULL,      'researcher', NOW()),
(6, 1, 'Calibration',         '2026-05-20 09:00:00', '2026-05-20 11:00:00', 'OK',     'admin',      NOW()),
(7, 2, 'Titration',           '2026-05-21 10:00:00', '2026-05-21 16:00:00', 'OK',     'labtech1',   NOW()),
(8, 3, 'Sample prep',         '2026-05-22 13:00:00', '2026-05-22 18:00:00', 'DAMAGED', 'researcher', NOW());

-- 5️⃣ Сброс счётчиков (чтобы новые auto-генерируемые ID не конфликтовали)
-- ✅ Вставь это ВМЕСТО всех ALTER SEQUENCE в конце скрипта
SELECT setval(pg_get_serial_sequence('instruments', 'id'), COALESCE((SELECT MAX(id) FROM instruments), 1), true);
SELECT setval(pg_get_serial_sequence('bookings', 'id'), COALESCE((SELECT MAX(id) FROM bookings), 1), true);
SELECT setval(pg_get_serial_sequence('checkouts', 'id'), COALESCE((SELECT MAX(id) FROM checkouts), 1), true);
SELECT MAX(id) FROM instruments;
select * from instruments