# BytecodeTrackerWeb Modernized

BytecodeTrackerWeb is now a production-grade full-stack platform for Java bytecode security analysis with a complete CMS for user and scan management.

## Modernized Architecture

- Backend: Spring Boot 3, Spring Security (JWT), Spring Data JPA, ASM bytecode parser
- Frontend: Next.js 14 App Router, TypeScript strict mode, Tailwind CSS, React Query, Recharts, React Dropzone
- Database: MySQL 8 with enhanced risk and audit columns
- Deployment: Dockerized backend + frontend + MySQL via docker-compose

## Repository Layout

- backend/: Spring Boot API and ASM analyzer
- frontend/: Next.js dashboard and CMS
- docker-compose.yml: Multi-container orchestration

## Backend API

Authentication:
- POST /api/auth/register
- POST /api/auth/login

User:
- GET /api/dashboard/stats
- POST /api/scans/upload
- GET /api/scans?page=0&size=10
- GET /api/scans/{id}
- DELETE /api/scans/{id}

Admin (role ADMIN required):
- GET /api/admin/users
- DELETE /api/admin/users/{id}
- GET /api/admin/scans
- DELETE /api/admin/scans/{id}
- DELETE /api/admin/scans (bulk body: { ids: [] })
- GET /api/admin/reports/csv

## CMS Features

Users page:
- Search users by username
- Filter by role
- Delete user with cascade impact on scans

Scans page:
- Filter by username
- Filter by risk level
- Select rows and bulk delete
- View full report

Reports page:
- Export all scans as CSV
- Export summary as PDF
- View KPI cards for total scans, dangerous scans, and users

## Bytecode Detection Engine (ASM)

The backend now analyzes uploaded .class files with ASM visitors and opcode stream checks for:
- Runtime.exec, ProcessBuilder, System.exit
- Dynamic class loading and native loading calls
- Reflection abuse patterns (Method.invoke)
- File I/O and socket/network usage

Risk assignment:
- HIGH: system execution, class loading tricks, native behavior
- MEDIUM: reflection, file I/O, network
- LOW: no suspicious instructions detected

## Configuration

Copy .env.example to .env and update values:
- DB_URL
- DB_USER
- DB_PASS
- JWT_SECRET
- UPLOAD_DIR
- CORS_ORIGIN
- SERVER_PORT
- NEXT_PUBLIC_API_BASE_URL

## Run With Docker

```bash
docker compose --env-file .env up --build
```

Endpoints:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- MySQL: localhost:3306

## Local Development

Backend:
```bash
cd backend
mvn spring-boot:run
```

Frontend:
```bash
cd frontend
npm install
npm run dev
```

## Notes

- All list endpoints support pagination through page and size query params.
- Backend validates file upload type and rejects invalid/corrupt .class payloads.
- Admin access is enforced in both backend and frontend route guards.
- Ensure MySQL is running before starting the backend (use docker-compose for simplicity).
