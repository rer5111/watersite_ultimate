# donation-service

Сервис каталога пожертвований и записей о завершённых донатах. Он владеет таблицами `donation_types` и `donations` в отдельной PostgreSQL-БД. `playerId` хранится как внешний идентификатор; сервис не читает БД `player-service`.

## API

Через Gateway сервис доступен под `/api/donation-service`. Публичный каталог типов возвращает только включённые варианты. Управление типами и записями доступно пользователям с ролью `ADMIN`.

| Метод | Путь после Gateway | Доступ | Результат |
|---|---|---|---|
| GET | `/api/v1/donation-types` | Публичный | Страница активных типов, сортировка по `displayOrder` |
| GET | `/api/v1/admin/donation-types` | ADMIN | Страница всех типов |
| GET | `/api/v1/admin/donation-types/{id}` | ADMIN | Тип по ID |
| POST | `/api/v1/admin/donation-types` | ADMIN | Создать тип, `201` |
| PUT | `/api/v1/admin/donation-types/{id}` | ADMIN | Полная замена типа с проверкой `version` |
| DELETE | `/api/v1/admin/donation-types/{id}` | ADMIN | Удалить тип, `204`; если есть донаты — `409` |
| GET | `/api/v1/admin/donations` | ADMIN | Страница записей, сначала новые |
| GET | `/api/v1/admin/donations/{id}` | ADMIN | Запись по ID |
| POST | `/api/v1/admin/donations` | ADMIN | Записать донат по `playerId` и `donationTypeId`, `201` |
| DELETE | `/api/v1/admin/donations/{id}` | ADMIN | Удалить запись, `204` |

Ошибки возвращаются как RFC 9457 Problem Details с полями `code`, `message`, `details`. Созданная запись хранит снимок цены типа; `paymentReference` необязателен и уникален при наличии. Удаление типа с существующими записями запрещено.

Начальные варианты из текущего интерфейса создаются Flyway-миграцией: 100, 250 и 500 ₽. Каталог сайта загружается через Gateway. `payment-service` ведёт административный реестр платежей; эквайринг не подключён, поэтому кнопка на сайте пока не создаёт платёж.

## Локальный запуск

Заполните `DONATION_DB_PASSWORD` в `.env` и выполните из корня репозитория:

```powershell
docker compose up -d --build
```

Проверка: `GET http://localhost:8080/api/donation-service/api/v1/donation-types`.

## Changelog

- `1.0.0` — отдельная БД, каталог, административные операции, аудит донатов и Gateway-интеграция.
