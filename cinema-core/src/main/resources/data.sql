-- =============================================================
-- CinemaSupchik demo dataset
-- Purpose: enough natural data for REST/GraphQL/RabbitMQ/gRPC/WebSocket demo
-- =============================================================

-- =========================
-- MOVIES
-- =========================
insert into movies (id, title, description, duration_minutes, age_rating, genre, active)
values
    (1, 'Интерстеллар', 'Научно-фантастическая драма о поиске нового дома для человечества, времени и гравитации.', 169, '12+', 'SCI_FI', true),
    (2, 'Начало', 'Команда специалистов внедряется в сны, чтобы изменить решение человека.', 148, '12+', 'THRILLER', true),
    (3, 'Тёмный рыцарь', 'Готэм сталкивается с хаосом, когда Джокер проверяет границы героя и города.', 152, '16+', 'ACTION', true),
    (4, 'Дюна: Часть вторая', 'Продолжение пути Пола Атрейдеса среди фременов и борьба за Арракис.', 166, '16+', 'SCI_FI', true),
    (5, 'Оппенгеймер', 'Биографическая драма о создании атомного проекта и моральной цене научного прорыва.', 180, '18+', 'DRAMA', true),
    (6, 'Ла-Ла Ленд', 'Музыкальная история о любви, мечте и цене выбора между карьерой и отношениями.', 128, '12+', 'MUSICAL', true),
    (7, 'Человек-паук: Через вселенные', 'Анимационное приключение Майлза Моралеса в мультивселенной супергероев.', 117, '6+', 'ANIMATION', true),
    (8, 'Джон Уик 4', 'Неоновый экшен о последней попытке освободиться от правил преступного мира.', 169, '18+', 'ACTION', true),
    (9, 'Гранд Будапешт', 'Ироничная история о консьерже, наследстве и Европе между эпохами.', 99, '16+', 'COMEDY', true),
    (10, 'Чужой', 'Космический хоррор о столкновении экипажа корабля с неизвестной формой жизни.', 117, '18+', 'HORROR', true);

select setval(pg_get_serial_sequence('movies', 'id'), (select coalesce(max(id), 1) from movies));

-- =========================
-- HALLS
-- =========================
insert into halls (id, name, hall_type, capacity, active)
values
    (1, 'Зал 1 Standard', 'STANDARD', 48, true),
    (2, 'Зал 2 VIP', 'VIP', 24, true),
    (3, 'Зал 3 IMAX', 'IMAX', 80, true),
    (4, 'Зал 4 Dolby Atmos', 'DOLBY_ATMOS', 40, true);

select setval(pg_get_serial_sequence('halls', 'id'), (select coalesce(max(id), 1) from halls));

-- =========================
-- SEATS
-- =========================
-- Hall 1: 6 rows x 8 seats. Last row is sofa.
insert into seats (id, hall_id, row_number, seat_number, seat_type, active)
select
    ((row_no - 1) * 8 + seat_no) as id,
    1 as hall_id,
    row_no as row_number,
    seat_no as seat_number,
    case when row_no = 6 then 'SOFA' else 'STANDARD' end as seat_type,
    true as active
from generate_series(1, 6) as row_no
cross join generate_series(1, 8) as seat_no;

-- Hall 2: 4 rows x 6 seats. Premium hall.
insert into seats (id, hall_id, row_number, seat_number, seat_type, active)
select
    100 + ((row_no - 1) * 6 + seat_no) as id,
    2 as hall_id,
    row_no as row_number,
    seat_no as seat_number,
    case when row_no = 4 then 'SOFA' else 'VIP' end as seat_type,
    true as active
from generate_series(1, 4) as row_no
cross join generate_series(1, 6) as seat_no;

-- Hall 3: 8 rows x 10 seats. Large IMAX hall.
insert into seats (id, hall_id, row_number, seat_number, seat_type, active)
select
    200 + ((row_no - 1) * 10 + seat_no) as id,
    3 as hall_id,
    row_no as row_number,
    seat_no as seat_number,
    case when row_no >= 7 then 'VIP' else 'STANDARD' end as seat_type,
    true as active
from generate_series(1, 8) as row_no
cross join generate_series(1, 10) as seat_no;

-- Hall 4: 5 rows x 8 seats. Atmos hall with sofa back row.
insert into seats (id, hall_id, row_number, seat_number, seat_type, active)
select
    300 + ((row_no - 1) * 8 + seat_no) as id,
    4 as hall_id,
    row_no as row_number,
    seat_no as seat_number,
    case
        when row_no = 5 then 'SOFA'
        when row_no = 4 then 'VIP'
        else 'STANDARD'
    end as seat_type,
    true as active
from generate_series(1, 5) as row_no
cross join generate_series(1, 8) as seat_no;

select setval(pg_get_serial_sequence('seats', 'id'), (select coalesce(max(id), 1) from seats));

-- =========================
-- CUSTOMERS
-- =========================
insert into customers (id, email, phone, registered, password_hash, created_at)
values
    (1, 'guest1@example.com', '+79990000001', false, null, null),
    (2, 'user1@example.com', '+79990000002', true, '$2a$10$demo.hash.value.000000000000000000000000000000000000', '2026-05-01T10:00:00+03:00'),
    (3, 'user2@example.com', '+79990000003', true, '$2a$10$demo.hash.value.111111111111111111111111111111111111', '2026-05-01T10:30:00+03:00'),
    (4, 'maria.ivanova@example.com', '+79990000004', true, '$2a$10$demo.hash.value.222222222222222222222222222222222222', '2026-05-02T11:00:00+03:00'),
    (5, 'alex.petrov@example.com', '+79990000005', true, '$2a$10$demo.hash.value.333333333333333333333333333333333333', '2026-05-02T12:00:00+03:00'),
    (6, 'guest2@example.com', '+79990000006', false, null, null),
    (7, 'sofia.kim@example.com', '+79990000007', true, '$2a$10$demo.hash.value.444444444444444444444444444444444444', '2026-05-03T09:15:00+03:00');

select setval(pg_get_serial_sequence('customers', 'id'), (select coalesce(max(id), 1) from customers));

-- =========================
-- SHOWS
-- =========================
insert into shows (id, movie_id, hall_id, start_time, end_time, base_price, currency, status)
values
    (1, 1, 1, '2026-06-20T18:00:00+03:00', '2026-06-20T20:49:00+03:00', 520.00, 'RUB', 'SCHEDULED'),
    (2, 2, 2, '2026-06-20T21:20:00+03:00', '2026-06-20T23:48:00+03:00', 890.00, 'RUB', 'SCHEDULED'),
    (3, 4, 3, '2026-06-21T19:00:00+03:00', '2026-06-21T21:46:00+03:00', 1050.00, 'RUB', 'SCHEDULED'),
    (4, 5, 4, '2026-06-21T20:30:00+03:00', '2026-06-21T23:30:00+03:00', 780.00, 'RUB', 'SCHEDULED'),
    (5, 7, 1, '2026-06-22T13:00:00+03:00', '2026-06-22T14:57:00+03:00', 380.00, 'RUB', 'SCHEDULED'),
    (6, 8, 2, '2026-06-22T22:00:00+03:00', '2026-06-23T00:49:00+03:00', 950.00, 'RUB', 'SCHEDULED'),
    (7, 6, 4, '2026-06-23T17:30:00+03:00', '2026-06-23T19:38:00+03:00', 500.00, 'RUB', 'SCHEDULED'),
    (8, 10, 3, '2026-06-23T23:10:00+03:00', '2026-06-24T01:07:00+03:00', 990.00, 'RUB', 'SCHEDULED'),
    (9, 3, 3, '2026-06-24T20:00:00+03:00', '2026-06-24T22:32:00+03:00', 880.00, 'RUB', 'SCHEDULED'),
    (10, 9, 1, '2026-06-24T16:00:00+03:00', '2026-06-24T17:39:00+03:00', 420.00, 'RUB', 'SCHEDULED'),
    (11, 1, 3, '2026-06-25T21:00:00+03:00', '2026-06-25T23:49:00+03:00', 1100.00, 'RUB', 'SCHEDULED'),
    (12, 4, 4, '2026-06-25T18:30:00+03:00', '2026-06-25T21:16:00+03:00', 830.00, 'RUB', 'SCHEDULED');

select setval(pg_get_serial_sequence('shows', 'id'), (select coalesce(max(id), 1) from shows));

-- =========================
-- BOOKINGS
-- =========================
insert into bookings (id, show_id, seat_id, customer_id, customer_email, status, reserved_until, final_price, currency, payment_reference, loyalty_points_used)
values
    (1, 1, 1, 1, 'guest1@example.com', 'PENDING_PAYMENT', '2026-06-20T18:10:00+03:00', 520.00, 'RUB', null, 0),
    (2, 1, 2, 2, 'user1@example.com', 'PAID', '2026-06-20T18:10:00+03:00', 470.00, 'RUB', 'pay_seed_001', 50),
    (3, 1, 3, 3, 'user2@example.com', 'PAID', '2026-06-20T18:10:00+03:00', 520.00, 'RUB', 'pay_seed_002', 0),
    (4, 2, 101, 4, 'maria.ivanova@example.com', 'PAID', '2026-06-20T21:30:00+03:00', 790.00, 'RUB', 'pay_seed_003', 100),
    (5, 2, 102, 5, 'alex.petrov@example.com', 'PENDING_PAYMENT', '2026-06-20T21:30:00+03:00', 890.00, 'RUB', null, 0),
    (6, 3, 201, 6, 'guest2@example.com', 'PAID', '2026-06-21T19:10:00+03:00', 1050.00, 'RUB', 'pay_seed_004', 0),
    (7, 3, 202, 7, 'sofia.kim@example.com', 'CANCELLED', '2026-06-21T19:10:00+03:00', 1050.00, 'RUB', null, 0),
    (8, 4, 301, 2, 'user1@example.com', 'PAID', '2026-06-21T20:40:00+03:00', 730.00, 'RUB', 'pay_seed_005', 50),
    (9, 6, 103, 3, 'user2@example.com', 'PAID', '2026-06-22T22:10:00+03:00', 950.00, 'RUB', 'pay_seed_006', 0),
    (10, 8, 205, 5, 'alex.petrov@example.com', 'PENDING_PAYMENT', '2026-06-23T23:20:00+03:00', 990.00, 'RUB', null, 0);

select setval(pg_get_serial_sequence('bookings', 'id'), (select coalesce(max(id), 1) from bookings));

-- =========================
-- TICKETS
-- =========================
insert into tickets (id, booking_id, ticket_number, status, qr_code)
values
    (1, 2, 'CIN-2', 'ACTIVE', 'ticket:2:1:2'),
    (2, 3, 'CIN-3', 'ACTIVE', 'ticket:3:1:3'),
    (3, 4, 'CIN-4', 'ACTIVE', 'ticket:4:2:101'),
    (4, 6, 'CIN-6', 'ACTIVE', 'ticket:6:3:201'),
    (5, 8, 'CIN-8', 'ACTIVE', 'ticket:8:4:301'),
    (6, 9, 'CIN-9', 'ACTIVE', 'ticket:9:6:103');

select setval(pg_get_serial_sequence('tickets', 'id'), (select coalesce(max(id), 1) from tickets));

-- =========================
-- WAITLIST
-- =========================
insert into waitlist_entries (id, show_id, seat_id, customer_id, customer_email, status)
values
    (1, 1, null, 4, 'maria.ivanova@example.com', 'ACTIVE'),
    (2, 1, 2, 5, 'alex.petrov@example.com', 'ACTIVE'),
    (3, 2, 101, 7, 'sofia.kim@example.com', 'ACTIVE'),
    (4, 3, null, 2, 'user1@example.com', 'ACTIVE'),
    (5, 6, 103, 4, 'maria.ivanova@example.com', 'ACTIVE');

select setval(pg_get_serial_sequence('waitlist_entries', 'id'), (select coalesce(max(id), 1) from waitlist_entries));

-- =========================
-- BUSINESS INVARIANTS
-- =========================
-- DB-level race-condition protection: only one active booking may hold a seat for a show.
create unique index if not exists uq_active_booking_show_seat
on bookings (show_id, seat_id)
where status in ('PENDING_PAYMENT', 'PAID');
