# chat-service

Микросервис двустороннего чата сайта и одного Discord-текстового канала. Он хранит в памяти последние 100 сообщений, поэтому история очищается при перезапуске контейнера. Discord-бот выключен по умолчанию: остальные сервисы запускаются и без токена бота.

## API

Через Gateway сервис доступен под `/api/chat-service`.

| Метод | Путь после Gateway | Доступ | Результат |
|---|---|---|---|
| GET | `/api/v1/chat/status` | Публичный | Состояние конфигурации и подключения бота |
| GET | `/api/v1/chat/messages?afterId=0` | Публичный | Сообщения с большим ID для опроса клиентом |
| POST | `/api/v1/chat/messages` | USER или ADMIN | Отправить сообщение из сайта в Discord, `202` |

Сообщение ограничено 500 символами. Входящие сообщения показывают источник `DISCORD` или `MINECRAFT`; Discord-боты кроме заданного Minecraft-бота игнорируются. Ошибки возвращаются как RFC 9457 Problem Details с полями `code`, `message`, `details`. Спецификация находится в `docs/openapi/chat-service.yaml`.

## Подключение Discord

Создайте Discord-бота, добавьте его в нужный сервер, выдайте доступ к текстовому каналу и включите привилегированное намерение Message Content в Discord Developer Portal. Затем задайте в локальном `.env`:

```dotenv
DISCORD_BOT_ENABLED=true
DISCORD_TOKEN=your_bot_token
DISCORD_CHANNEL_ID=your_text_channel_id
DISCORD_MINECRAFT_BOT_USER_ID=1042883408596062298
```

Токен не добавляйте в Git. После настройки пересоздайте сервис командой `docker compose up -d --build chat-service`. При выключенном или отключённом боте API отправки возвращает `503 DISCORD_UNAVAILABLE`.

## Локальная проверка

Из каталога `microservices/chat-service` выполните `./mvnw -B clean verify` (или `./mvnw.cmd -B clean verify` в PowerShell). В корне репозитория весь стек запускается командой `docker compose up -d --build`; единственная опубликованная точка входа — Gateway на порту 8080.
