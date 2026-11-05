# AGENTS.md

## Обзор проекта

Waltercraft — сайт игрового сервера Minecraft с микросервисной архитектурой.

- Фронтенд: React + TypeScript + Vite + Tailwind CSS
- Бэкенд: Spring Boot (Java 21+), **микросервисная архитектура (обязательное требование)**
- Фронтенд общается только с API Gateway, напрямую с сервисами — никогда
- Язык интерфейса: русский

---

## Роли агента и база знаний (.agents/)

При выполнении задач опирайся на специализированные роли и конвенции из каталога `.agents/`:

- **Роли агентов (`.agents/agents/<name>/agent.md` и симлинки в `.agents/roles/`)**:
  - `spring-boot-engineer` — генерация слоёв сервисов, контроллеров, DTO, сущностей и миграций.
  - `code-reviewer` — аудит диффа перед коммитом (проверка контрактов, утечек, N+1).
  - `security-engineer` — безопасность, валидация входных данных, работа с JWT и ролями.
  - `docker-expert` / `devops-engineer` — сборка multi-stage Dockerfile, Paketo Buildpacks, CI/CD.
- **Скиллы и паттерны (`.agents/skills/<name>/SKILL.md`)**:
  - `conventions/SKILL.md` — глобальные инженерные правила кода, Problem Details, No-Lombok.
  - `spring-boot/SKILL.md` — REST API, JPA, Security, тестирование, облачные паттерны.
  - `jpa-patterns/SKILL.md` — защита от N+1, `OSIV: false`, блокировки (`@Version`).
  - `testing-patterns/SKILL.md` — тесты с Testcontainers и `@ServiceConnection`.
  - `code-quality/SKILL.md`, `java-modern/SKILL.md`, `design-patterns/SKILL.md`, `logging-patterns/SKILL.md`.

---

## Структура репозитория

- `frontend/` — SPA на React
- `microservices/` — бэкенд, родительский `pom.xml` и сервисы
- `config-repo/` — конфиги для Config Server (`application.yml` и `<service-name>.yml`)
- `.agents/` — роли (`roles/`) и база знаний/скиллы (`skills/`) для ИИ-агентов
- `.antigravity/` — конфигурация и правила для Antigravity IDE
- `docker-compose.yml` — локальный запуск всей системы
- `docs/` — архитектура, OpenAPI-спецификации
- `legacy-monolith/` — старый монолит на Spring Boot. Только справочный материал для переноса логики в микросервисы. Не развивать и не запускать, изменения вносить только в `microservices/`. Удалить, когда миграция завершится

---

## Сервисы

Все сервисы лежат в `microservices/`. Имя папки = `artifactId` = `spring.application.name` = имя конфига в `config-repo/`, везде kebab-case.

Инфраструктура:

| Сервис             | Назначение                                            |
| ------------------ | ----------------------------------------------------- |
| `config-server`    | Spring Cloud Config, отдаёт конфиги из `config-repo/` |
| `discovery-server` | Eureka, регистрация и поиск сервисов                  |
| `api-gateway`      | Единая точка входа: маршрутизация, проверка JWT, CORS |

Доменные сервисы:

| Сервис                | Зона ответственности                         | Таблицы                        |
| --------------------- | -------------------------------------------- | ------------------------------ |
| `auth-service`        | Авторизация, выдача и обновление токенов     | —                              |
| `player-service`      | Аккаунты, игроки, сезоны                     | `accounts`, `users`, `seasons` |
| `donation-service`    | Пожертвования и их типы                      | `donations`, `donation_types`  |
| `application-service` | Заявки на сервер                             | `applications`                 |
| `payment-service`     | Платежи                                      | `payments`                     |
| `chat-service`         | Подключение к чату через Discord             | —                              |
| `notification-service` | Отправка кодов подтверждения и email-сообщений | `email_verification_codes`     |
| `event-service`        | Хронология событий и сезоны сервера          | `events`                       |
| `skin-service`         | Получение скинов Minecraft через внешний API | —                              |

---

## Команды

### Фронтенд (`frontend/`)

- Установка: `npm install`
- Dev: `npm run dev`
- Сборка: `npm run build`
- Типы: `npm run typecheck`
- Линтер: `npm run lint`

### Бэкенд (в папке нужного сервиса)

- Сборка и тесты: `./mvnw clean verify`
- Запуск: `./mvnw spring-boot:run`

### Вся система

- Поднять: `docker compose up --build`
- Остановить: `docker compose down`

Перед завершением задачи запусти сборку и проверки только тех частей, которые менял.

---

## Архитектурные правила (микросервисы)

- Один сервис = одна зона ответственности (bounded context).
- **У каждого сервиса своя база данных.** Читать или писать в БД другого сервиса запрещено.
- **Переменные окружения баз данных изолированы**: каждый сервис объявляет свои переменные подключения с префиксом сервиса (например, `PLAYER_DB_USER`, `PLAYER_DB_PASSWORD`, `PLAYER_DB_NAME`), запрещено использовать единый глобальный `POSTGRES_*`.
- Сервисы общаются только через REST/HTTP API (или брокер сообщений, если он есть в проекте), не через общий код или общую БД.
- **Межсервисные вызовы**: использовать `RestClient` или HTTP Interface Clients (`@HttpExchange`). Использование устаревшего `RestTemplate` запрещено.
- Общие библиотеки между сервисами не создавать. Допустимы только контракты (DTO/OpenAPI), и только по согласованию.
- Новый сервис добавляй только если задача явно этого требует. Иначе расширяй существующий по зоне ответственности.
- Каждый сервис должен собираться, запускаться и тестироваться независимо от остальных.
- Конфигурация через `application.yml` и переменные окружения, секреты в коде не хранить.
- Внешние запросы идут через Gateway, а внутренние адреса сервисов не светятся на фронтенде.
- **Сортировка и иерархия данных**: сортировка и упорядочивание коллекций производятся строго на стороне бэкенда (`ORDER BY` в репозитории/БД). Запрещено зашивать словари приоритетов и ручную сортировку на клиенте.
- **Внешние вызовы из микросервисов**: при обращении к внешним провайдерам (например, API скинов Mojang/VZGE) обязательно указывать честный кастомный заголовок `User-Agent: Waltercraft<Service>/1.0 (+https://waltercraft.ru)`. Без него внешние CDN возвращают HTTP 400/403.

---

## api-gateway

- Spring Cloud Gateway на WebFlux, `spring-boot-starter-web` не добавлять.
- Обязательно подключать зависимость `spring-cloud-starter-loadbalancer` для корректной маршрутизации `lb://<service>`. Без нее gateway возвращает `503 Service Unavailable`.
- Только маршрутизация, проверка JWT, CORS, rate limiting. Бизнес-логики нет.
- Новый сервис = новый route в `config-repo/api-gateway.yml`, префикс `/api/<имя>/**`.
- Публичные пути (логин, регистрация, список сезонов, скины и т.п.) перечислены в `SecurityConfig`. Новые добавлять только по явной просьбе.
- Внутренние сервисы наружу не пробрасывать, в `docker-compose.yml` публикуется порт только у gateway.

---

## Бэкенд (Spring Boot — стандарты разработки)

- **Версии**: Java 21+, Spring Boot 3.x / 4.x.
- **Пакеты**: `com.waltercraft.<service>.{controller,service,repository,dto,entity,config}`.
- **Слои**: `Controller` → `Service` → `Repository`. Бизнес-логика строго в Service.
- **No Lombok**:
  - Для DTO и неизменяемых моделей использовать Java **`record`**.
  - Для JPA-сущностей — явные геттеры, сеттеры и конструкторы.
  - Логгеры объявлять явно через `LoggerFactory.getLogger(...)`.
- **Внедрение зависимостей**:
  - Строго через конструкторы (Record-конструкторы или явный конструктор класса).
  - Аннотация `@Autowired` на полях запрещена.
- **DTO и маппинг**:
  - Наружу отдаём только DTO, JPA-сущности из контроллеров никогда не возвращаются.
- **База данных и JPA**:
  - Схема управляется исключительно миграциями **Flyway** в `src/main/resources/db/migration/`.
  - **Правила миграций Flyway**:
    - Никогда не модифицировать уже примененные миграции `V<n>__*.sql` (вызывает `FlywayValidateException: Migration checksum mismatch`).
    - Не зашивать пароли и секреты в SQL — использовать Flyway placeholders `${placeholderName}`, конфигурируемые через `spring.flyway.placeholders.*`.
    - Для локальной разработки включать `spring.flyway.baseline-on-migrate: true` и `spring.flyway.validate-on-migrate: ${FLYWAY_VALIDATE:false}`.
    - SQL-скрипты миграций должны быть ANSI-совместимыми (`WHERE NOT EXISTS (...)` вместо специфичного `ON CONFLICT DO NOTHING`), чтобы не ломать встроенный H2 в юнит-тестах.
  - В конфигах `spring.jpa.hibernate.ddl-auto: ${JPA_DDL_AUTO:validate}` (с возможностью переопределения на `update` в dev).
  - Избегать проблемы N+1 (`@EntityGraph`, `JOIN FETCH`).
  - При `open-in-view: false` все обращения к связанным сущностям (`@OneToOne`, `@ManyToOne`) в контроллерах/сервисах должны быть обернуты в `@Transactional` или использовать `@EntityGraph`.
  - Оптимистические блокировки через `@Version` для критичных сущностей.
- **Валидация, безопасность и мониторинг**:
  - Валидация входных данных через `jakarta.validation`.
  - Обработка ошибок через `@RestControllerAdvice` в формате RFC 9457 Problem Details (расширенном под контракт `{ "code", "message", "details" }`).
  - Эндпоинты `/actuator/health/**` и `/actuator/info` обязаны быть в `permitAll()` в `SecurityConfig` каждого сервиса, иначе healthcheck контейнеров будет падать.
- **Тестирование**:
  - Юнит-тесты: JUnit 5 + Mockito.
  - Интеграционные тесты с реальными БД: **Testcontainers** с аннотацией `@ServiceConnection` (без устаревшего `@DynamicPropertySource`).
  - Тестирование REST: `RestTestClient` (`@AutoConfigureRestTestClient`) или `MockMvc`.
  - Каждый новый эндпоинт покрывай тестом.
- **Инфраструктура и контракты**:
  - Каждый сервис регистрируется в Eureka и берёт конфиг из Config Server.
  - Меняешь эндпоинт — обнови OpenAPI-спецификацию в `docs/openapi/`.
- **Именование**:
  - Сервисы, папки и URL в API: `kebab-case`.
  - Java-пакеты: `lowercase` (`com.waltercraft.player`).
  - Таблицы и колонки в БД: `snake_case`.
  - Поля Java и JSON: `camelCase`.

---

## Контейнеризация и окружение (Podman / Docker Compose)

- **Многоэтапная сборка (`Dockerfile.java`)**:
  - Базовый образ этапа сборки: `maven:3.9-eclipse-temurin-21-alpine` (Maven предустановлен, исключает скачивание wrapper при каждой сборке).
  - Кэширование слоёв: сначала копируется `pom.xml`, используется `--mount=type=cache,target=/root/.m2`, затем копируется `src`.
  - Директива `HEALTHCHECK` внутри Dockerfile не используется (вызывает `WARN: HEALTHCHECK is not supported for OCI image format` в Podman). Все healthcheck настраиваются в `docker-compose.yml`.
- **Тома баз данных PostgreSQL**:
  - Образ PostgreSQL при наличии существующего тома (`volume`) игнорирует новые `POSTGRES_USER` и `POSTGRES_PASSWORD`. При изменении учетных данных в `.env` старый том нужно пересоздавать командой `podman compose down -v`.
  - Проверка готовности БД в healthcheck: `test: ["CMD-SHELL", "pg_isready -U \"$${POSTGRES_USER}\" -d \"$${POSTGRES_DB}\""]`.
- **Управление секретами**:
  - Файлы `.env`, `.env.local`, `.env.*.local` строго внесены в `.gitignore`.
  - В корне репозитория обязателен актуальный `.env.example` с переменными для каждого сервиса и каждой БД.
  - Запрещено использовать жестко зашитые дефолтные пароли (вроде `0`, `admin`, `secret`) в коде и `application.properties`.

---

## Фронтенд (React + TypeScript)

- Строгий TypeScript (`strict: true`), **без `any`**: используй `unknown` и сужение типов.
- Функциональные компоненты и хуки, один компонент на файл.
- Файлы и папки в `kebab-case` (`donation-card.tsx`), компоненты внутри в `PascalCase`.
- Стили только через Tailwind, отдельный CSS не писать. Утилита `cn` в `lib/utils.ts`.
- Структура:
  - `components/ui/` — базовые UI-компоненты (например, `button` с variants).
  - `components/layout/` — шапка, футер, сайдбары.
  - `pages/`, `api/`, `lib/`, `styles/`.
- Все запросы к бэкенду только через `src/api/`, из компонентов напрямую `fetch`/`axios` не вызывать.
- Базовый URL API из `import.meta.env.VITE_API_URL` (адрес gateway), без хардкода.
- Типы ответов лежат в `src/api/types/` и соответствуют OpenAPI-контрактам.
- Дизайн берём из Figma, новые цвета и отступы вводим через конфиг Tailwind.
- Тексты интерфейса на русском.

---

## Авторизация

- Токены выдаёт и обновляет `auth-service`, проверяет `api-gateway`.
- Токен на фронтенде добавляется централизованно через интерсептор, а не в каждом запросе.
- Права доступа проверяются на бэкенде. Проверки на фронтенде нужны только для управления отображением (UX).

---

## Ограничения

- Не коммить `.env`, ключи, пароли.
- Не меняй публичные контракты API без явной просьбы.
- Не рефактори код, не относящийся к задаче.

---

## Git

- Conventional Commits: `feat(donation-service): ...`, `fix(frontend): ...`.
- В скоупе коммита указывай сервис или `frontend`.
- Одна логическая правка на коммит.
