# BytecodeTrackerWeb

A full-stack web application for uploading, analyzing, and tracking Java `.class` files. BytecodeTrackerWeb inspects compiled bytecode using ASM, detects risk-level violations, and presents results through a modern dashboard — with role-based access for users and admins.

---

## Features

- Upload `.class` files and analyze bytecode instructions via ASM
- Detect violations and classify them by risk level (LOW / MEDIUM / HIGH)
- View per-class and per-method analysis breakdowns
- Dashboard with scan history, risk distribution charts, and timelines
- JWT-based authentication with USER and ADMIN roles
- Admin panel for managing users and scans
- Dockerized for easy deployment

---

## Tech Stack

| Layer     | Technology                                       |
|-----------|--------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.3.5, Spring Security, JPA |
| Frontend  | Next.js (TypeScript), Tailwind CSS               |
| Database  | MySQL 8.4                                        |
| Bytecode  | ASM 9.7.1                                        |
| Auth      | JWT (JJWT 0.11.5)                                |
| Container | Docker, Docker Compose                           |

---

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose
- Or, for local development:
  - Java 17+
  - Maven 3.6+
  - Node.js 18+ and npm
  - MySQL 8.4

---

## Getting Started

### Option 1 — Docker Compose (Recommended)

**1. Clone the repository**

```bash
git clone https://github.com/ajaygowdah/BytecodeTrackerWeb.git
cd BytecodeTrackerWeb
```

**2. Set up environment variables**

Create a `.env` file in the project root:

```env
DB_USER=yourdbuser
DB_PASS=yourdbpassword
JWT_SECRET=your_jwt_secret_key
CORS_ORIGIN=http://localhost:3000
```

**3. Start all services**

```bash
docker compose up --build
```

This spins up:
- MySQL on port `3306`
- Spring Boot backend on port `8080`
- Next.js frontend on port `3000`

**4. Open the app**

```
http://localhost:3000
```

---

### Option 2 — Local Development

**Backend**

```bash
cd backend
# Create backend/.env with DB_URL, DB_USER, DB_PASS, JWT_SECRET, CORS_ORIGIN, UPLOAD_DIR
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`.

**Frontend**

```bash
cd frontend
# Create frontend/.env.local with: NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
npm install
npm run dev
```

The frontend starts on `http://localhost:3000`.

---

## Project Structure

```
BytecodeTrackerWeb/
├── backend/                        # Spring Boot application
│   ├── src/main/java/com/bytecodetracker/
│   │   ├── controller/             # REST controllers (Auth, Scan, Admin, Dashboard)
│   │   ├── service/                # Business logic (BytecodeAnalyzerService, ScanService, etc.)
│   │   ├── model/                  # JPA entities (Scan, User, Violation, ClassAnalysis, etc.)
│   │   ├── dto/                    # Data transfer objects
│   │   ├── repository/             # Spring Data JPA repositories
│   │   ├── security/               # JWT filter & utility
│   │   └── config/                 # Security, CORS, and data initializer config
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── schema.sql
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                       # Next.js application
│   ├── src/
│   │   ├── app/                    # Next.js App Router pages
│   │   │   ├── (auth)/             # Login & Register pages
│   │   │   ├── dashboard/          # Main dashboard
│   │   │   ├── scan/               # Upload, history, and scan detail pages
│   │   │   └── cms/                # Admin panel (users, scans, reports)
│   │   ├── components/             # Reusable UI and chart components
│   │   └── lib/                    # API client, auth helpers, types
│   ├── Dockerfile
│   └── package.json
│
├── docker-compose.yml
└── .env
```

---

## API Overview

| Method | Endpoint           | Description             | Auth  |
|--------|--------------------|-------------------------|-------|
| POST   | `/auth/login`      | Login and get JWT token | Public |
| POST   | `/auth/register`   | Register a new user     | Public |
| POST   | `/scan/upload`     | Upload `.class` file    | USER  |
| GET    | `/scan/{id}`       | Get scan result by ID   | USER  |
| GET    | `/dashboard/stats` | Dashboard statistics    | USER  |
| GET    | `/admin/users`     | List all users          | ADMIN |
| GET    | `/admin/scans`     | List all scans          | ADMIN |

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

## License

This project is open source. See [LICENSE](LICENSE) for details.