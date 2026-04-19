# abstract-service

Базовый модуль-шаблон для микросервисов Underfish.

## Что входит
- Spring Web
- Spring Data JPA
- Flyway
- Actuator + Prometheus
- Jackson Kotlin
- Validation
- OpenFeign для межсервисного общения
- PostgreSQL и H2
- Базовый набор тестовых зависимостей
- Базовые пакеты и заготовки:
  - `database.entities.AbstractEntity` (`id`, `createdAt`)
  - `dto.response.ErrorResponse`
  - `exception` (`BadRequestException`, `UnauthorizedException`, `NotFoundException`, `GlobalExceptionHandler`)

## Что намеренно не входит
- Локальный security/JWT
- Auth-логика
- Роли и фильтры аутентификации

## Профили
- `dev` — PostgreSQL + Docker Compose
- `test` — H2 in-memory
- `prod` — PostgreSQL

## Правило для локальной БД (вариант A)
- Один общий Postgres локально на `5432`
- У каждого микросервиса свой `DB_NAME`
- Пример для этого шаблона: `DB_NAME=abstract_service`

Это помогает избежать конфликтов по порту БД и разделять схемы сервисов по базам.

## Запуск
```bash
./gradlew :abstract-service:bootRun
```

## Как создать отдельный сервис из шаблона
1. Скопируй модуль `abstract-service` в новый каталог (например, `location-service`).
2. Переименуй пакет и main-класс (например, `ru.underfish.locationservice.LocationServiceApplication`).
3. Обнови `spring.application.name` и `DB_NAME` в `application*.yml`.
4. Укажи новый HTTP-порт сервиса через `SERVER_PORT` (чтобы не делить `8080` с другими сервисами).
5. Добавь модуль в корневой `settings.gradle.kts` через `include(":location-service")`.
6. В новом `build.gradle.kts` оставь нужные зависимости, лишнее удаляй по назначению сервиса.
7. Замени `db/migration/V1__init.sql` на реальные миграции схемы сервиса.
8. Подними сервис локально и проверь smoke-тест.
