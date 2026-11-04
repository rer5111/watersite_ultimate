# application-service

Сервис заявок на сервер. Он владеет таблицей `applications` в отдельной PostgreSQL-БД и не читает базу `player-service`.

Имя пользователя берётся из проверенного `sub` access-токена. Сервис сохраняет его как снимок `applicantUsername`, поэтому клиент не может отправить заявку от имени другого игрока и сервису не нужна внешняя база пользователей.

## API

Через Gateway сервис доступен под `/api/application-service`. Все операции требуют access-токен. Пользователь видит только свои заявки; `ADMIN` видит и модерирует все заявки.

| Метод | Путь после Gateway | Доступ | Результат |
|---|---|---|---|
| GET | `/api/v1/applications` | USER: свои, ADMIN: все | Страница заявок, сначала новые |
| GET | `/api/v1/applications/{id}` | Владелец или ADMIN | Заявка по ID; чужая заявка возвращает `404` |
| POST | `/api/v1/applications` | USER или ADMIN | Создать заявку со статусом `PENDING`, `201` |
| PUT | `/api/v1/applications/{id}` | ADMIN | Полностью заменить текст и статус с проверкой `version` |
| DELETE | `/api/v1/applications/{id}` | Владелец или ADMIN | Владелец может отозвать только `PENDING`; `204` |

Статусы: `PENDING`, `APPROVED`, `REJECTED`. Ошибки — RFC 9457 Problem Details с полями `code`, `message`, `details`. Полная спецификация: [application-service.yaml](../../docs/openapi/application-service.yaml).

## Локальный запуск

Задайте уникальный `APPLICATION_DB_PASSWORD` в корневом `.env`, затем из корня репозитория выполните:

```powershell
docker compose up -d --build
```

Проверка: `GET http://localhost:8080/api/application-service/api/v1/applications` с access-токеном.

## Changelog

- `1.0.0` — отдельная БД, создание и просмотр заявок, модерирование статусов и проверка владельца.
