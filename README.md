\# Gulabi Rides 🚗



\*\*A smarter way to get from one place to another.\*\*



Gulabi Rides is a full-stack cab booking application built to bring riders, drivers, and administrators together in one place. The goal is simple: make booking a ride straightforward, give drivers the tools they need to manage their work, and provide administrators with a clear view of what's happening across the platform.



This project is a hands-on exploration of building a complete ride-booking system — from authentication and ride requests to driver dispatch, real-time updates, and payments.



\## ✨ What you can do



\### 🙋 For riders

\- Create an account and sign in.

\- Explore available ride categories and fare estimates.

\- Request a ride and follow its status.

\- View ride history.

\- Access payment and receipt features.

\- Get support when something goes wrong.



\### 🚘 For drivers

\- Register for the driver experience.

\- Manage driver availability and receive ride requests.

\- Accept eligible ride requests.

\- Follow ride progress through the application.



\### 🛡️ For administrators

\- Access a dedicated administration dashboard.

\- Review platform users, drivers, and rides.

\- Monitor operational activity and platform statistics.

\- Manage supported administrative functions.



\### ⚡ Behind the scenes

\- JWT-based authentication and role-based access control.

\- REST APIs connecting the frontend and backend.

\- WebSocket support for real-time communication.

\- PostgreSQL for persistent application data.

\- Redis for caching and supporting application services.

\- Optional Kafka integration for ride-event publishing.

\- Docker Compose for running the local development environment.

\- Automated checks through GitHub Actions.



\*Some capabilities depend on configuration and the current implementation. External services such as payment processing, Google authentication, and mapping APIs require their own credentials.\*



\## 🧰 Built with



| Area | Technology |

|---|---|

| Frontend | React, Vite, Tailwind CSS |

| Backend | Java, Spring Boot, Maven |

| Database | PostgreSQL |

| Caching | Redis |

| Real-time communication | WebSockets |

| Event publishing | Optional Apache Kafka |

| Infrastructure | Docker, Docker Compose |

| Testing | JUnit and Spring testing tools |

| CI | GitHub Actions |



\## 🏗️ How the project is organized



```text

cab-booking-app/

├── .github/

│   └── workflows/       # CI workflow

├── backend/

│   ├── src/

│   │   ├── main/        # Application code and configuration

│   │   └── test/        # Automated tests

│   ├── Dockerfile

│   └── pom.xml

├── frontend/

│   ├── src/

│   │   ├── components/  # Rider, driver, admin and other screens

│   │   ├── api.js       # API communication

│   │   └── ws.js        # WebSocket connection

│   ├── Dockerfile

│   └── package.json

├── prototype/           # Standalone UI prototype

├── docker-compose.yml

├── .env.example

└── README.md

```



\## 🚀 Run it locally



The easiest way to start is with Docker Compose.



\### 1. Get the code



```bash

git clone https://github.com/Imprawin7/Gulabi-Rides.git

cd Gulabi-Rides

```



\### 2. Configure the environment



Copy the example environment file:



\*\*Windows PowerShell\*\*

```powershell

Copy-Item .env.example .env

notepad .env

```



\*\*macOS / Linux\*\*

```bash

cp .env.example .env

```



Open `.env` and configure the required values. At minimum, set a strong, randomly generated `JWT\_SECRET`. Review the other settings and configure external services if you intend to use them.



Keep your actual `.env` file private. Never commit API keys, passwords, tokens, or other credentials to GitHub.



\### 3. Start the application



Make sure Docker Desktop is running, then execute:



```bash

docker compose up -d --build

```



Check the container status:



```bash

docker compose ps

```



To inspect application logs:



```bash

docker compose logs -f

```



The exact local URLs and ports depend on the Docker Compose configuration. Check `docker-compose.yml` and the service logs if you need to confirm them.



\### 4. Stop the application



```bash

docker compose down

```



This stops and removes the Compose containers and network while normally preserving named volumes.



\*\*Avoid `docker compose down -v` unless you deliberately intend to delete the associated volumes and their stored data.\*\*



\## 🔑 Configuration and integrations



Gulabi Rides supports configuration for several optional integrations:



\- \*\*Payments:\*\* Stripe API credentials and webhook configuration.

\- \*\*Google sign-in:\*\* OAuth client configuration.

\- \*\*Maps and distance calculations:\*\* Google Maps API key, where required.

\- \*\*Real-time ride events:\*\* Kafka can be enabled when the corresponding service is started.

\- \*\*Ride dispatch:\*\* Configurable dispatch radius and request-expiry duration.



You do not need to configure every external integration just to explore the source code. Some features may remain unavailable until their required credentials and services are configured.



\## 🧪 Run the tests



From the project root, run the backend tests with Docker or a local Maven installation, depending on your setup:



```bash

cd backend

mvn test

```



Return to the root directory afterward:



```bash

cd ..

```



For the frontend, install dependencies and run the configured scripts:



```bash

cd frontend

npm ci

npm run

```



The final command lists the available npm scripts defined by the project. Use the relevant script from `frontend/package.json` to start the development server or run any configured checks.



\## 🔒 Security notes



A few things matter when running or extending this project:



\- Use strong secrets and passwords.

\- Keep `.env` and private credentials out of version control.

\- Configure production CORS, OAuth redirects, and webhook endpoints appropriately.

\- Use HTTPS and production-grade secret management when deploying.

\- Test authentication and role-based access before exposing the application publicly.



The local development configuration is not automatically suitable for production.



\## 🛣️ Where the project can go next



There is plenty of room to make the experience better as the application evolves:



\- Show ride availability based on registered, eligible, online drivers.

\- Match requests to the correct vehicle category.

\- Improve the driver onboarding and vehicle-registration experience.

\- Expand ride tracking and notifications.

\- Strengthen administrative reporting and operational monitoring.

\- Improve automated tests and deployment workflows.



These are potential areas for continued development, not a claim that every item is already implemented.



\## 👨‍💻 About the project



Gulabi Rides is a learning and development project focused on bringing frontend development, backend engineering, databases, authentication, and containerization together in a single application.



Building a ride-booking platform involves more than displaying a map and a booking button. It also means thinking about who can access each feature, how rides are assigned, what happens when a driver is unavailable, and how the system behaves when something fails.



That's what makes this project interesting: learning how all those pieces fit together.



\## 📄 License



No license has been specified here. Add a license file if you intend to define how others may use, modify, and distribute the project.



