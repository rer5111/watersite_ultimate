# Auth Service

`auth-service` verifies user credentials through the private `player-service` API and issues RS256 access and refresh JWTs. User records and password hashes remain owned by `player-service`; this service has no database.

## Gateway API

- `POST /api/auth-service/auth/login` accepts `{"username":"…","password":"…"}` and returns an access token, a refresh token, their bearer type, and the access-token lifetime.
- `POST /api/auth-service/auth/refresh` accepts `{"refreshToken":"…"}` and returns a new pair.
- `GET /api/auth-service/.well-known/jwks.json` publishes the public signing key. The gateway uses the internal service URL `http://auth-service:8090/.well-known/jwks.json`.

The OpenAPI file is [`docs/openapi/auth-service.yaml`](../../docs/openapi/auth-service.yaml).

## Local configuration

Set `AUTH_SERVICE_SHARED_SECRET` to a random value of at least 32 characters. The same value is injected into `player-service` and `auth-service`; it protects the internal credential-verification endpoint and must stay out of Git.

Set `AUTH_JWT_PRIVATE_KEY_BASE64` to a Base64-encoded RSA PKCS#8 DER private key and set `AUTH_JWT_ALLOW_EPHEMERAL_KEY=false` for deployments that need tokens to survive restarts. For local development, an ephemeral 3072-bit key is generated when no key is configured; tokens signed by that key stop validating after the service restarts.

Token lifetimes default to 15 minutes for access tokens and 7 days for refresh tokens. Refresh tokens are stateless JWTs: this first version does not persist revocations, so a refresh token remains usable until it expires. Configure the gateway's `JWT_JWK_SET_URI` with the Docker service URL and keep `GATEWAY_JWT_ENABLED=true` to validate access tokens before routing protected player-service requests.

The seed accounts use the locked password placeholder unless `ADMIN_INITIAL_PASSWORD_HASH` is configured. No default user password is supplied.
