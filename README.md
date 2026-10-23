# Gulabi Rides: cab booking platform

Full-stack ride-hailing app. Riders book, drivers accept, payments run through Stripe, trips update live over WebSockets,
and an admin watches everything.

```
cab-booking-app/
  backend/      Spring Boot 3, Java 17: REST API, JWT + Google sign-in, JPA/PostgreSQL, STOMP WebSockets, Stripe, PDF receipts
  frontend/     React + Vite + Tailwind: rider, driver and admin screens, Leaflet map (served by nginx in Docker)
  prototype/    Standalone HTML demo of the ride flow (no server needed, open in a browser)
  docker-compose.yml   Postgres + API + web, one command
  .github/workflows/   CI: backend tests, frontend build, docker build
```

## Run everything
```bash
cp .env.example .env        # set JWT_SECRET at minimum (openssl rand -base64 48)
docker compose up --build   # app on http://localhost, API on http://localhost:8080
```

## Develop without Docker
```bash
docker compose up db -d                    # just Postgres (add `ports: ["5432:5432"]` to db first)
cd backend && JWT_SECRET=$(openssl rand -base64 48) mvn spring-boot:run
cd frontend && npm install && npm run dev  # http://localhost:5173, proxies /api and /ws to :8080
```

## Try the full flow
1. Open the app in three browser profiles. Register a **rider**, register a **driver** (vehicle + plate), and log in as `admin@gulabi.dev` (password = `ADMIN_PASSWORD`).
2. Driver: go online and allow location access (requests are matched by distance; with location off you still see every request). Rider: pick places, try promo `WELCOME20`, book. The demo places are in Jaipur, so if you are testing from elsewhere raise `DISPATCH_RADIUS_KM` or deny location on the driver browser.
3. Driver accepts, taps arrived, enters the rider's 4-digit code, completes the trip.
4. Rider pays (needs Stripe test keys; use card 4242 4242 4242 4242), downloads the receipt and rates the driver.
5. Admin sees live stats, rides, SOS alerts, driver ranking and support tickets, and manages promo codes. Drivers see their rank on the Ranking tab.

## Optional integrations
| Feature | Variable | Without it |
|---|---|---|
| Card payments | `STRIPE_SECRET`, `STRIPE_WEBHOOK_SECRET`, `VITE_STRIPE_PK` | Pay button explains the key is missing |
| Google sign-in | `GOOGLE_CLIENT_ID` | Button hidden |
| Road distance and ETA | `GOOGLE_MAPS_API_KEY` | Straight-line estimate |
| Kafka ride events | `KAFKA_ENABLED=true` and `docker compose --profile kafka up` | Events are not published |
| Redis cache | runs in `docker-compose.yml` (`CACHE_TYPE=redis`) | In-memory cache |
| Emailed receipts and password-reset links | `SPRING_MAIL_HOST` etc. (see backend/README.md) | Receipts skipped; reset link logged in development |

More detail: `backend/README.md` (endpoints, topics, security notes) and `frontend/README.md`.

## Before going live
Set `APP_PRODUCTION=true`. The API then refuses to start until `JWT_SECRET`, `ADMIN_PASSWORD` (12+ characters) and `STRIPE_SECRET` are real values.
Login and signup are rate limited. Serve the app over HTTPS (the nginx container speaks plain HTTP; put a TLS terminator or your host's HTTPS in front).

## Status
Generated without access to Maven Central or Docker, so the backend and the Docker builds have **not been compiled or run**.
The frontend build was verified. First step on your machine: `cd backend && mvn verify`, then `docker compose up --build`, and expect a few small fixes.
Not included: deployment config (Render/Railway/AWS), push notifications.
