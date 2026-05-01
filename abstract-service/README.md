# abstract-service

Шаблонный модуль для микросервисов, где JWT проверяется на gateway, а downstream-сервис получает уже готовую identity-информацию в заголовках.

## Что внутри

- `SecurityConfig` - stateless security chain с обязательной аутентификацией для всех маршрутов, кроме `public-paths`.
- `GatewayHeaderAuthenticationFilter` - читает доверенные заголовки (`X-User-Id`, `X-User-Roles`), опционально проверяет `X-Internal-Token`.
- `GatewayPrincipal` - типизированный principal для сервисной логики.
- `CurrentUserProvider` - доступ к текущему пользователю из `SecurityContext`.

## Минимальный контракт заголовков от gateway

- `X-User-Id` - обязательный идентификатор пользователя.
- `X-User-Roles` - обязательные роли (через запятую).
- `X-User-Email` - опционально.
- `X-Auth-Source` - опционально.
- `X-Internal-Token` - опционально, но рекомендуется для внутреннего доверенного контура.

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

