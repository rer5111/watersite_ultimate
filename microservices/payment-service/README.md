# payment-service

Сервис платёжного реестра. Он владеет таблицей `payments` в отдельной PostgreSQL-БД и хранит плательщика как снимок имени, не связываясь с базой `player-service`.

Модель перенесена из старого монолита: имя плательщика, дата и сумма в рублях. Все операции доступны только `ADMIN`. Это реестр уже проведённых платежей; интеграция с эквайрингом не настроена, поэтому сервис не списывает деньги и не возвращает ссылку на оплату.

## API

Через Gateway сервис доступен под `/api/payment-service`.

| Метод | Путь после Gateway | Результат |
|---|---|---|
| GET | `/api/v1/admin/payments` | Страница платежей, сначала новые |
| GET | `/api/v1/admin/payments/{id}` | Платёж по ID |
| POST | `/api/v1/admin/payments` | Добавить запись, `201` |
| PUT | `/api/v1/admin/payments/{id}` | Полностью заменить запись с проверкой `version` |
| DELETE | `/api/v1/admin/payments/{id}` | Удалить запись, `204` |

`externalReference` необязателен, но уникален при наличии. Ошибки возвращаются как RFC 9457 Problem Details с полями `code`, `message`, `details`. Полная спецификация: [payment-service.yaml](../../docs/openapi/payment-service.yaml).

## Локальный запуск

Задайте уникальный `PAYMENT_DB_PASSWORD` в корневом `.env`, затем из корня репозитория выполните:

```powershell
docker compose up -d --build
```

## Changelog

- `1.0.0` — отдельная БД и защищённый платёжный реестр с CRUD, валидацией суммы и optimistic locking.
