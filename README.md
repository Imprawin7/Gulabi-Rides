\# Gulabi Rides



A full-stack cab booking application connecting riders, drivers, and administrators through a single platform.



Gulabi Rides is a development project focused on building a reliable ride-booking experience with authentication, ride requests, driver dispatch, real-time communication, and payment integration. It brings together frontend development, backend services, database management, and containerized deployment.



\## Features



\### Rider

\- Account registration and authentication

\- Ride categories and fare estimates

\- Ride booking and status tracking

\- Ride history

\- Payment and receipt functionality

\- Customer support



\### Driver

\- Driver registration and authentication

\- Availability management

\- Ride request notifications

\- Ride acceptance and progress tracking



\### Administration

\- Administrative dashboard

\- User, driver, and ride management

\- Platform activity and statistics

\- Administrative operations



\### Platform

\- JWT-based authentication and role-based access control

\- REST API integration

\- WebSocket communication

\- PostgreSQL database

\- Redis caching

\- Optional Apache Kafka integration for ride events

\- Docker-based development environment

\- Automated checks using GitHub Actions



Feature availability may depend on configuration and the current implementation. External integrations require their respective credentials.



\## Technology Stack



| Component | Technology |

|---|---|

| Frontend | React, Vite, Tailwind CSS |

| Backend | Java, Spring Boot, Maven |

| Database | PostgreSQL |

| Caching | Redis |

| Real-time communication | WebSockets |

| Event publishing | Apache Kafka (optional) |

| Containerization | Docker, Docker Compose |

| Testing | JUnit and Spring testing tools |

| Continuous integration | GitHub Actions |



\## Project Structure



```text

cab-booking-app/

├── .github/

│   └── workflows/

├── backend/

│   ├── src/

│   │   ├── main/

│   │   └── test/

│   ├── Dockerfile

│   └── pom.xml

├── frontend/

│   ├── src/

│   │   └── components/

│   ├── Dockerfile

│   └── package.json

├── prototype/

├── docker-compose.yml

├── .env.example

└── README.md

```



\## Getting Started



\### Prerequisites



Install the following before running the application:



\- Git

\- Docker Desktop or Docker Engine with Docker Compose

\- A terminal such as PowerShell, Command Prompt, or Bash



A local Java and Node.js installation may also be useful for running tests and developing outside Docker.



\### 1. Clone the repository



```bash

git clone https://github.com/Imprawin7/Gulabi-Rides.git

cd Gulabi-Rides

```



\### 2. Configure environment variables



On Windows PowerShell:



```powershell

Copy-Item .env.example .env

notepad .env

```



On macOS or Linux:



```bash

cp .env.example .env

```



Configure the required environment variables before starting the application. Generate a strong, unique value for `JWT\_SECRET` and supply credentials for any external services you intend to use.



Never commit your actual `.env` file or expose passwords, API keys, access tokens, or other sensitive credentials.



\### 3. Start the application



Ensure Docker is running, then execute the following command from the project root:



```bash

docker compose up -d --build

```



Check the status of the services:



```bash

docker compose ps

```



View application logs:



```bash

docker compose logs -f

```



Refer to `docker-compose.yml` and the service logs for the configured ports and local application URLs.



\### 4. Stop the application



```bash

docker compose down

```



This stops and removes the Compose containers and network. Named volumes are normally preserved.



Avoid `docker compose down -v` unless you intentionally want to remove the associated volumes and their stored data.



\## Configuration and Integrations



The application supports configuration for the following services:



\- Stripe for payment processing and webhook handling

\- Google OAuth for authentication

\- Google Maps API for distance calculations

\- Redis for caching

\- Apache Kafka for optional ride-event publishing



Ride dispatch radius and request expiry can also be configured through environment variables.



External integrations require valid credentials and appropriate service configuration. Some features may remain unavailable until their dependencies are configured.



\## Testing



To run backend tests using a local Maven installation:



```bash

cd backend

mvn test

```



To inspect the available frontend scripts:



```bash

cd ../frontend

npm ci

npm run

```



Use the relevant script defined in `frontend/package.json` to start the development server or execute configured checks.



\## Security



Before deploying the application to a production environment:



\- Use strong, unique secrets and passwords.

\- Keep credentials out of source control.

\- Configure CORS and OAuth redirect URLs for the intended environment.

\- Secure payment webhook endpoints.

\- Use HTTPS and appropriate secret management.

\- Verify authentication and role-based access controls.

\- Review database access and production configuration.



The local development configuration should not be assumed to be production-ready.



\## Future Improvements



Potential areas for further development include:



\- Showing ride availability based on eligible and available drivers

\- Matching ride requests to the correct vehicle category

\- Improving driver onboarding and vehicle registration

\- Expanding ride tracking and notifications

\- Enhancing administrative reporting

\- Increasing automated test coverage

\- Improving deployment and monitoring workflows



These items represent possible future enhancements and are not necessarily implemented features.



\## About



Gulabi Rides is a software development project exploring the engineering behind a cab-booking platform. It brings together frontend interfaces, backend APIs, persistent storage, authentication, real-time communication, and containerization in one application.



The project provides an opportunity to understand how these components work together to manage users, drivers, ride requests, and the operational requirements of a booking system.



\## License



No license has been specified. Add a license file to define the terms under which others may use, modify, or distribute this project.



