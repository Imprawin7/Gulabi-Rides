# Gulabi Rides web (React + Vite + Tailwind)

```bash
cp .env.example .env     # add VITE_STRIPE_PK (pk_test_...) to enable card payments
npm install
npm run dev              # http://localhost:5173, proxies /api and /ws to the API on :8080
```
Start the backend first (`docker compose up --build` from the repository root, or run `backend/` directly).

Try it with three browser profiles: register a rider, a driver (go online), then log in as `admin@gulabi.dev`.
Book as the rider, accept as the driver, and watch status, OTP, location and SOS updates arrive over WebSockets.
The driver's phone or browser shares its position via the Geolocation API once a ride is accepted.

Screens: rider (book, trips with receipt download, help), driver (drive, trips, help), admin (stats, rides, SOS alerts, support tickets).
