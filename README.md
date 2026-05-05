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

Запускать их нужно вместе:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d --build
```

Остановка:

```bash
docker compose -f docker-compose-infra.yml -f docker-compose-services.yml down
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
