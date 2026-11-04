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
| `chat-service`        | Подключение к чату через Discord             | —                              |
| `skin-service`        | Получение скинов Minecraft через внешний API | —                              |

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
- Сервисы общаются только через REST/HTTP API (или брокер сообщений, если он есть в проекте), не через общий код или общую БД.
- **Межсервисные вызовы**: использовать `RestClient` или HTTP Interface Clients (`@HttpExchange`). Использование устаревшего `RestTemplate` запрещено.
- Общие библиотеки между сервисами не создавать. Допустимы только контракты (DTO/OpenAPI), и только по согласованию.
- Новый сервис добавляй только если задача явно этого требует. Иначе расширяй существующий по зоне ответственности.
- Каждый сервис должен собираться, запускаться и тестироваться независимо от остальных.
- Конфигурация через `application.yml` и переменные окружения, секреты в коде не хранить.
- Внешние запросы идут через Gateway, а внутренние адреса сервисов не светятся на фронтенде.

---

## api-gateway

- Spring Cloud Gateway на WebFlux, `spring-boot-starter-web` не добавлять.
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
  - В конфигах `spring.jpa.hibernate.ddl-auto: validate`.
  - Избегать проблемы N+1 (`@EntityGraph`, `JOIN FETCH`).
  - Оптимистические блокировки через `@Version` для критичных сущностей.
- **Валидация и ошибки**:
  - Валидация входных данных через `jakarta.validation`.
  - Обработка ошибок через `@RestControllerAdvice` в формате RFC 9457 Problem Details (расширенном под контракт `{ "code", "message", "details" }`).
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
