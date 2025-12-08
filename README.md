## SubOne

Архитектура управления подписками. Представьте, что у вас десятки сервисов и платежей: SubOne помогает держать их под контролем, автоматически напоминает за день до оплаты, отслеживает статус подписок (пока без прямых интеграций с платёжными системами), строит статистику по расходам за месяц и год и прогнозирует среднегодовые траты.


### Архитектура
- **user_service** — хранит пользователей, группы и приглашения, кэширует права в Redis, публикует события `delete_group` в Kafka.
- **subscriptions_service** — ведёт подписки и аналитику, запускает крон‑задачи, отправляющие уведомления о платежах (`payment_subscription`).
- **notifications_service** — принимает Kafka‑сообщения (`create_user`, `notification_user`, `payment_subscription`), сохраняет уведомления и раздаёт их по REST и WebSocket.
- **Keycloak** — не стандартный образ: внутрь интегрирован Kafka‑провайдер, Keycloak сам публикует/читает события.
- **Kafka** — основной канал асинхронного обмена.
- **Redis** — кэш групповых прав и аналитических срезов.
- **PostgreSQL** — общий инстанс с выделенными схемами (`user_service`, `subscriptions_service`, `notifications_service`, `security`).
- **pgAdmin** — UI для базы, доступен на порту `8081`.

Почему микросервисы: проект задуман как полигон для отработки взаимодействия сервисов, асинхронного обмена через Kafka и изоляции схем БД. Для продуктовой задачи монолит был бы проще и дешевле в сопровождении, но текущая архитектура демонстрирует алгоритмы обмена событиями и “сервисы-владельцы” данных.

**R2DBC в user_service и subscriptions_service**: используется для изучения реактивного подхода к работе с БД. R2DBC обеспечивает неблокирующий I/O и более эффективную обработку при больших пользовательских запросах за счёт асинхронности и лучшего использования потоков. **notifications_service** использует классический JPA для простоты работы с историей уведомлений.

### Безопасность и интеграции
- Все внешние API подняты как resource server и проверяют JWT Keycloak.
- Межсервисное взаимодействие `notifications_service → user_service` реализовано по `client_credentials`, сервис получает токен в Keycloak перед REST-запросами.
- Kafka настроена с повторными попытками (retry, idempotence), но пока без dead-letter topic и тонкой настройки очередей. Для уведомлений это допустимо: если сообщение не дойдёт моментально, система всё равно отрисует историю по REST. В следующих итерациях планируется добавить DLQ и расширенные политики доставки.

### Основные технологии
`Java 21`, `Spring Boot 3.5.6`, `Spring WebFlux/Web`, `Spring Data R2DBC/JPA`, `Spring Security`, `Keycloak`, `Kafka`, `Liquibase`, `Redis`, `Docker`, `Docker Compose`.

### Сборка артефактов
1. Убедитесь, что установлен JDK 21. Maven может быть глобальным, но достаточно поставляемого с проектом wrapper’а (`mvnw` / `mvnw.cmd`).
2. Перейдите в каталог каждого сервиса и соберите его в отдельности **с запуском тестов** (пример для Unix-подобных систем):
   ```powershell
   cd user_service
   ./mvnw clean install
   cd ..
   cd subscriptions_service
   ./mvnw clean install
   cd ..
   cd notifications_service
   ./mvnw clean install
   cd ..
   ```
   В Windows используйте `mvnw.cmd`. После сборки артефакты появятся в папках `target` каждого сервиса (`*-0.0.1-SNAPSHOT.jar`).

### Построение контейнеров
Используется общий `Dockerfile`, который ожидает путь к JAR через аргумент `JAR_FILE`. Пример сборки образа для `notifications_service` (тэг `notifications-service:0.0.1` — пример, замените на нужный):
```powershell
docker build -t notifications-service:0.0.1 \
  --build-arg JAR_FILE=notifications_service/target/notifications_service-0.0.1-SNAPSHOT.jar \
  .
```
Для `user_service` и `subscriptions_service` выполните аналогичные команды с нужными путями к JAR и тегами образов.

### Переменные окружения
Ниже перечислены ключевые переменные и их дефолты из `application.yaml`:

- `SERVER_PORT` — 8000 / 8001 / 8002 для `user`, `subscriptions`, `notifications`.
- `PAGE_SIZE` — 10 (размер страниц для пагинации).
- `SPRING_DATASOURCE_URL` — `jdbc:postgresql://localhost:5432/subOne` (notifications: `jdbc:postgresql://localhost:5432/subOne?currentSchema=notifications_service`).
- `SPRING_DATASOURCE_URL_REACTIVE` — `r2dbc:postgresql://localhost:5432/subOne?schema=<schema>` (для реактивных сервисов).
- `DB_USERNAME` / `DB_PASSWORD` — `postgres` / `123456`.
- `REDIS_HOST` / `REDIS_PORT` — `localhost` / `6379` (используются `user_service`, `subscriptions_service`).
- `KEYCLOAK_URI` — `http://localhost:8080/realms/subOne`.
- `KAFKA_SERVERS` — `localhost:9092`.
- `KAFKA_NAME_TOPIC_DELETE_USER` — `delete_user` (user_service producer).
- `CLIENT_ID_CREDENTIALS` / `CLIENT_SECRET_CREDENTIALS` — **обязательные значения**, по умолчанию не заданы (notifications_service).
- `USER_SERVICE_URL` — `http://localhost:8000/api/v1/` (subscriptions и notifications).
- `USER_SERVICE_RETRIES` — `5` (количество попыток повторного вызова у subscriptions_service).

Docker Compose также ожидает переменные для Keycloak и БД: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `KEYCLOAK_ADMIN`, `KEYCLOAK_ADMIN_PASSWORD`, `ADMIN_EMAIL_PG`, `ADMIN_PASSWORD_PG`, а также порты сервисов и client credentials.

**Обязательный `.env` для запуска compose** должен содержать минимум:
```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
ADMIN_EMAIL_PG=admin@example.com
ADMIN_PASSWORD_PG=admin

SERVER_PORT_USER_SERVICE=8000
SERVER_PORT_SUBSCRIPTIONS_SERVICE=8001
SERVER_PORT_NOTIFICATIONS_SERVICE=8002

CLIENT_ID_CREDENTIALS=notifications-service
CLIENT_SECRET_CREDENTIALS=change-me
```

### Запуск через Docker Compose
1. Заполните `.env` в корне проекта нужными значениями (см. переменные выше).
2. Соберите образы сервисов (см. раздел “Построение контейнеров”).
3. Стартуйте инфраструктуру:
   ```powershell
   docker compose up -d
   ```
4. После завершения health‑check’ов:
   - Keycloak: `http://localhost:8080`
   - user_service: `http://localhost:8000`
   - subscriptions_service: `http://localhost:8001`
   - notifications_service: `http://localhost:8002`

### Разработка и диагностика
- Локальный запуск без Docker: `./<service>/mvnw spring-boot:run -Dspring.profiles.active=dev` или используйте `default` профиль.
- Миграции управляются Liquibase (`db/changelog` в каждом сервисе).
- Здоровье: `GET /actuator/health`.
- Swagger/OpenAPI: `GET /v3/api-docs` (UI отключён, но спецификация доступна), но при запуске профиля `dev` `Swagger UI` будет доступен.

### Статус проекта
Разработка фронтенд-части находится в процессе: готовятся экраны для управления подписками, аналитикой и уведомлениями.

