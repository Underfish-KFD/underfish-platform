# UNDERFISH
Инновационная гиперлокальная платформа для анонсирования и поиска мероприятий

---
## Оглавление 📑
- [Описание 📋](#описание)
- [Установка и запуск 🚧](#установка-и-запуск)
- [Мониторинг и метрики 📊](#мониторинг-и-метрики)
- [Технологии 🛠️](#технологии)
- [Авторы 🖋](#авторы)

---
## Описание 📋
“Underfish” — это инновационная гиперлокальная платформа для анонсирования и поиска мероприятий, ориентированная
на малые сообщества, андерграунд-организаторов и локальных инициатив. Платформа позволяет организаторам легко
публиковать информацию о своих мероприятиях (концерты, встречи, мастер-классы, выставки), а пользователям
— находить интересные события в своём районе с помощью удобной карты и системы тегов.


## Установка и запуск 🚧

### Микросервисы через Docker Compose

В репозитории есть отдельный набор для инфраструктуры и сервисов:

- `docker-compose-infra.yml` — отдельные БД для сервисов, Redis, MinIO, Zipkin.
- `docker-compose-services.yml` — контейнеры сервисов (gateway, auth, profile, community, event, location, file-storage).

#### Полный локальный запуск: все БД и все сервисы

> На macOS перед Gradle-командами удобно выставить Java 21:
>
> ```bash
> export JAVA_HOME=$(/usr/libexec/java_home -v21)
> ```

1. Собрать jar всех сервисов:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v21) && \
./gradlew build -x test --console=plain
```

2. Собрать Docker images всех сервисов без cache:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml build --no-cache
```

3. Поднять всю инфраструктуру и все сервисы:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d
```

То же самое одной командой:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v21) && \
./gradlew build -x test --console=plain && \
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml build --no-cache && \
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d
```

Остановка без удаления volume с данными БД:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml down
```

Остановка с удалением volume БД/Redis/MinIO, чтобы начать с чистого состояния:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml down -v
```

Проверка, что контейнеры поднялись:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml ps
```

Логи всех сервисов:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml logs -f
```

Логи только auth-service:

```bash
docker logs --tail 200 -f uf_auth
```

#### Проверки после запуска

Gateway health:

```bash
curl -i http://localhost:8080/actuator/health
```

Auth-service health:

```bash
curl -i http://localhost:8091/actuator/health
```

JWKS для RS256-токенов:

```bash
curl -i http://localhost:8091/.well-known/jwks.json
```

Если нужен красивый JSON:

```bash
curl -s http://localhost:8091/.well-known/jwks.json | jq
```

#### Все тесты

Unit/integration tests Gradle-проектов:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v21) && \
./gradlew test --console=plain
```

Полная Gradle-проверка с build:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v21) && \
./gradlew build --console=plain
```

Локальный E2E-сценарий через gateway: регистрация пользователя, проверка JWT `RS256`, создание community, создание event в этом community, проверка пользователя в auth DB:

```bash
cd /Users/alex/Projects/underfish/underfish-platform && \
DEBUG=1 ./integration-tests/run_e2e.sh
```

Явный вариант этой же E2E-команды:

```bash
cd /Users/alex/Projects/underfish/underfish-platform && \
GATEWAY_URL=http://localhost:8080 \
REGISTER_PATH=/api/v1/users/register \
EXPECT_JWT_ALG=RS256 \
DB_HOST=localhost \
DB_PORT=5432 \
DB_NAME=auth_db \
DB_USER=auth_user \
DB_PASS=auth_pass \
DEBUG=1 \
./integration-tests/run_e2e.sh
```

Если E2E идёт на `http://localhost:8080/api/auth/register` и получает `401`, значит запускается старая версия скрипта или в shell задан старый `REGISTER_PATH`. Используйте актуальный путь:

```bash
REGISTER_PATH=/api/v1/users/register DEBUG=1 ./integration-tests/run_e2e.sh
```

Если после регистрации community creation падает с `500`, сначала проверьте, что контейнеры пересобраны из актуальных jar. Самые частые причины: старый auth-service выдал токен без `roles` или со старым numeric `user_id`, либо старый community-service ещё не умеет принимать такой `user_id`. Пересоберите и пересоздайте сервисы. Вариант с `--no-deps` подходит только когда инфраструктура и соседние сервисы уже запущены; после `down -v` используйте полный цикл ниже, иначе не поднимутся PostgreSQL/profile/location зависимости:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v21) && \
./gradlew build -x test --console=plain && \
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml build --no-cache auth-service gateway community-service event-service && \
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d --no-deps --no-build auth-service gateway community-service event-service
```

Полный цикл «с нуля»: пересобрать, поднять всё и запустить тесты:

```bash
cd /Users/alex/Projects/underfish/underfish-platform && \
export JAVA_HOME=$(/usr/libexec/java_home -v21) && \
./gradlew build --console=plain && \
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml down -v && \
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml build --no-cache && \
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d && \
DEBUG=1 ./integration-tests/run_e2e.sh
```

Порты сервисов (хост -> контейнер):

- gateway: `8080`
- auth-service: `8091`
- profile-service: `8082`
- event-service: `8083`
- community-service: `8084`
- location-service: `8081`
- file-storage-service: `8085`

Порты БД (хост -> контейнер) берутся из `docker-compose-infra.yml`:

- auth: `5432`
- event: `5433`
- geo: `5434`
- notification: `5435`
- community: `5436`
- storage: `5437`
- profile: `5438`

Eureka/Service Discovery в проекте не настроен — маршрутизация идет через явные `host:port`.

### Вариант 1: Сборка из исходников

1. Клонируйте репозиторий

```
git clone https://github.com/Underfish-KFD/underfish.git
cd underfish
```

---
2. Соберите проект с помощью Maven (нужен установленный Maven)

```
mvn clean package
```
3. Запустите приложение
```
java -jar target/underfish-0.1.jar
```
4. Приложение будет доступно на `http://localhost:8080`.
---
### Вариант 2: Запуск через Docker 🐳

1. Клонируйте репозиторий

```
git clone https://github.com/Underfish-KFD/underfish.git
cd underfish
```

---
2. Соберите проект с помощью Maven (нужен установленный Maven)

```
mvn clean package -Pdocker -DskipTests
```
3. Соберите Docker-образ:
```
docker build -t kotlin-app .
```
4. Запустите контейнер с зависимостями:
```
docker compose up -d
```
5. Приложение будет доступно на `http://localhost:8080`.

---


## Мониторинг и метрики 📊

- Здоровье: `/actuator/health`
- Метрики (Prometheus): `/actuator/prometheus`

Запрос метрик:
```
curl http://localhost:8080/actuator/prometheus
```
---

## Технологии 🛠️

- Kotlin 1.9.25
- Spring Boot 3.5.8
- Spring Web
- Spring security
- Spring Boot Actuator
- Micrometer Prometheus Registry

---

## 🖋 Авторы

**Косовский Иван**\
**Труфанов Александр**\
**Томин Никита**

Проект реализован в рамках учебного задания МИФИ\
GitHub: [github.com/Underfish-KFD](https://github.com/Underfish-KFD)
