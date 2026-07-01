# CinemaSupchik Web

Упрощённая демо-морда кинотеатра для проверки backend-сценариев без Swagger.

## Запуск

```bash
cd cinema-web
cp .env.example .env
npm install
npm run dev
```

Открыть `http://localhost:5173`. Если порт занят, Vite выберет следующий доступный порт.

## Что проверяется

- каталог фильмов и сеансов;
- выбор места по реальной карте мест сеанса `/api/shows/{id}/seats`;
- создание брони `/api/bookings`;
- оплата брони через PATCH `PAID`;
- отмена брони через DELETE;
- лист ожидания для занятых мест;
- получение билета после оплаты;
- audit-события;
- realtime WebSocket уведомления notification-service;
- ссылки на Mailpit и RabbitMQ.
