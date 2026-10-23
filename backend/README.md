# Gulabi Rides API

Spring Boot 3 / Java 17 backend for the cab booking app: JWT auth with roles, ride lifecycle, fare quotes with promo codes,
STOMP WebSockets, Stripe PaymentIntents with a verified webhook, ratings, and an admin summary. PostgreSQL via JPA.

## Run it
From the repository root: `docker compose up --build` (see the root README). To run just the API:
`JWT_SECRET=$(openssl rand -base64 48) mvn spring-boot:run` with a Postgres reachable at `DB_URL` (defaults to localhost:5432/cab).
Set `STRIPE_SECRET` and `STRIPE_WEBHOOK_SECRET` for payments (`stripe listen --forward-to localhost:8080/api/payments/webhook`).
Seeded: promo `WELCOME20` and admin `admin@gulabi.dev` (password from `ADMIN_PASSWORD`; change it).

## REST API
| Method | Path | Who |
|---|---|---|
| POST | /api/auth/register, /api/auth/login, /api/auth/google | public |
| POST | /api/auth/forgot, /api/auth/reset | public |
| GET | /api/rides/quote?pLat&pLng&dLat&dLng&type&promo | any user |
| POST | /api/rides, /api/rides/{id}/cancel, /api/rides/{id}/rating | rider |
| GET | /api/rides/mine | rider |
| POST | /api/driver/online?value=true | driver |
| GET | /api/driver/requests (only those near the driver), /api/driver/earnings, /api/driver/history | driver |
| POST | /api/driver/location `{lat,lng}` | driver |
| GET | /api/drivers/ranking | any user |
| POST | /api/driver/rides/{id}/accept, /arrive, /start?otp=, /complete | driver |
| POST | /api/payments/intent/{rideId} | rider |
| POST | /api/payments/webhook | Stripe |
| GET | /api/rides/{id}/receipt | rider (PDF, after payment) |
| POST/GET | /api/support, /api/support/mine | any user |
| GET | /api/admin/stats, /api/admin/rides, /api/admin/tickets | admin |
| POST | /api/admin/tickets/{id}/close | admin |
| GET/POST | /api/admin/promos, /api/admin/promos/{code}/toggle | admin |

Ride states: REQUESTED → ACCEPTED → ARRIVED → IN_TRIP → COMPLETED → PAID (or CANCELLED).

## WebSockets (STOMP at `/ws`, send `Authorization: Bearer <jwt>` on CONNECT)
- Subscribe `/user/queue/requests` (drivers: new rides near you), `/topic/ride/{id}` (status changes), `/topic/ride/{id}/location`, `/topic/admin/sos`
- Send `/app/ride/{id}/location` `{lat,lng}` (driver), `/app/sos` `{rideId,message,lat,lng}` (rider)

## Try it
```bash
curl -s localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"name":"Asha","email":"asha@example.com","password":"password123","role":"RIDER"}'
```

## Dispatch and ranking
Drivers post their position to `POST /api/driver/location` every ~10 s while online. A new request is pushed only to online drivers within
`DISPATCH_RADIUS_KM` (default 8) of the pickup; a driver with no fresh position (older than 2 minutes) still sees all requests, so nothing is lost
when GPS is off. Requests nobody accepts within `REQUEST_EXPIRY_MINUTES` (default 10) are cancelled automatically.
`GET /api/drivers/ranking` returns the top 20 drivers by weighted rating: (avg x n + 4.5 x 5) / (n + 5).

## Hardening
- **Rate limiting:** at most 10 POSTs a minute per client address to `/api/auth/**` (`AUTH_RATE_LIMIT`), answered with 429.
- **Production guard:** with `APP_PRODUCTION=true` the API refuses to start on the default JWT secret, the default or short admin password, or a missing Stripe key.
- **Schema:** Flyway owns the database (`src/main/resources/db/migration`). Hibernate only validates it on startup (`DDL_AUTO=validate`).
  Change the schema by adding a new `V2__...sql` file, never by editing V1. If startup reports a schema validation error, the entity and SQL disagree: fix the SQL in a new migration.

## Password reset
`POST /api/auth/forgot {email}` always answers the same way (no account discovery), and emails a link `PUBLIC_URL/reset?token=...` valid for 30 minutes.
Only a SHA-256 hash of the token is stored, and each link works once. `POST /api/auth/reset {token, password}` sets the new password.
Without a mail server, the link is written to the API log when `APP_PRODUCTION` is false, so you can try it locally.

## Kafka ride events (optional)
Off by default. Set `KAFKA_ENABLED=true` and start Kafka with `docker compose --profile kafka up`: every ride change (`RIDE_REQUESTED`, `RIDE_ACCEPTED`,
`RIDE_ARRIVED`, `RIDE_IN_TRIP`, `RIDE_COMPLETED`, `RIDE_PAID`, `RIDE_CANCELLED`) is published as JSON to the `ride-events` topic, keyed by ride id.
`RideEventAuditListener` is a sample consumer that logs them. Publishing is asynchronous and never blocks or fails a ride if Kafka is down.

## Caching
Road distance lookups use a Spring cache. By default it is in memory; the root `docker-compose.yml` runs Redis and sets `CACHE_TYPE=redis`.
If Redis is unreachable the API logs it and continues without the cache.

## Road distance and ETA
Set `GOOGLE_MAPS_API_KEY` (enable the Distance Matrix API) to price rides on real driving distance and show an ETA.
Without a key it falls back to a straight-line estimate x 1.3 at about 25 km/h. Results are cached in memory.

## Google sign-in
Create an OAuth client (type: Web) in Google Cloud Console, add your frontend origin (e.g. http://localhost:5173) as an authorised JavaScript origin,
and set the same client ID as `GOOGLE_CLIENT_ID` (API) and `VITE_GOOGLE_CLIENT_ID` (web). The browser gets a Google ID token, `POST /api/auth/google`
verifies it and returns our JWT. New Google accounts become riders unless they choose driver and enter vehicle details.

## Receipts
On payment success the webhook emails a PDF receipt (iText) if a mail server is configured
(`SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`); without one it only logs.
Riders can also download it from `GET /api/rides/{id}/receipt`.

## Security notes
STOMP subscriptions and sends are checked per destination (`TopicGuard`): ride topics only for that ride's rider, assigned driver or an admin;
the request feed for drivers; admin topics for admins; only the assigned driver can publish a ride's location.

## Tests
`mvn test` runs unit tests for fare/promo logic, ride state transitions (incl. OTP), topic security and PDF generation.

## Known gaps (next steps)
- Not included: deployment configuration, social login other than Google, push notifications.
- Not compiled in the environment where it was generated (no Maven Central access), so run `mvn verify` first and expect small fixes.
