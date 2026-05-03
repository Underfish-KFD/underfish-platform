# EventService

Микросервис событий для платформы Underfish.

## Что реализовано

- `/api/v1/events` CRUD + фильтрация + пагинация.
- `/api/v1/tags` CRUD.
- `/api/v1/events/{event_id}/tags` управление тегами события.
- `/api/v1/events/{event_id}/attendance` управление посещениями.
- `/api/v1/events/{event_id}/reviews` и `/api/v1/reviews/{review_id}`.
- Security через gateway headers (`X-User-Id`, `X-User-Roles`, `X-Internal-Token`).
- Интеграционные вызовы в другие МК через `InternalLookupClient` (profile/location), включаются флагом `integration.validation-enabled=true`.

## Run

```bash
./gradlew :eventService:bootRun
```

## Test

```bash
./gradlew :eventService:test
```

