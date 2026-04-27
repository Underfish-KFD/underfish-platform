# abstract-service

Шаблонный модуль для микросервисов, где JWT проверяется на gateway, а downstream-сервис получает уже готовую identity-информацию в заголовках.

## Что внутри

- `SecurityConfig` - stateless security chain с обязательной аутентификацией для всех маршрутов, кроме `public-paths`.
- `GatewayHeaderAuthenticationFilter` - читает доверенные заголовки (`X-User-Id`, `X-User-Roles`), опционально проверяет `X-Internal-Token`.
- `GatewayPrincipal` - типизированный principal для сервисной логики.
- `CurrentUserProvider` - доступ к текущему пользователю и готовые проверки `self/admin`.

## Минимальный контракт заголовков от gateway

- `X-User-Id` - обязательный идентификатор пользователя.
- `X-User-Roles` - обязательные роли (через запятую).
- `X-User-Email` - опционально.
- `X-Auth-Source` - опционально.
- `X-Internal-Token` - рекомендуется и должен совпадать с `gateway.security.trusted-internal-token`.

## Базовые настройки

```yaml
gateway:
  security:
    enabled: true
    public-paths:
      - /actuator/health
      - /actuator/info
    roles-delimiter: ","
    trusted-internal-token: ${GATEWAY_INTERNAL_TOKEN:}
```

## Как использовать в новом сервисе

1. Добавьте `CurrentUserProvider` в контроллер или сервис через constructor injection.
2. Для операций "только для себя" используйте `requireSelfOrAdmin(pathUserId)`.
3. Для действий администратора используйте `isAdmin()` или `hasRole("ADMIN")`.
4. Не доверяйте `userId` из тела запроса, берите текущего пользователя из `CurrentUserProvider`.

Пример для endpoint `PUT /api/v1/users/{userId}`:

```kotlin
@RestController
@RequestMapping("/api/v1/users")
class ProfileController(
    private val currentUserProvider: CurrentUserProvider,
    private val profileService: ProfileService,
) {
    @PutMapping("/{userId}")
    fun updateProfile(
        @PathVariable userId: String,
        @RequestBody request: UpdateProfileRequest,
    ): UserResponse {
        currentUserProvider.requireSelfOrAdmin(userId)
        return profileService.updateProfile(userId, request)
    }
}
```

## Коды ошибок

- `401 Unauthorized` - нет валидного gateway контекста (пропущены заголовки или неверный `X-Internal-Token`).
- `403 Forbidden` - пользователь аутентифицирован, но не имеет прав на действие (например, пытается изменить чужой профиль).
