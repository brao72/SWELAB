# Deployment & CI/CD — LibraTrack

This document explains how LibraTrack is containerized, deployed to AWS EC2, and automatically updated via a CI/CD pipeline using GitHub Actions.

---

## Table of Contents

1. [Deployment Architecture](#1-deployment-architecture)
2. [Docker — Containerization](#2-docker--containerization)
3. [Docker Compose — Orchestration](#3-docker-compose--orchestration)
4. [Nginx — Reverse Proxy](#4-nginx--reverse-proxy)
5. [AWS EC2 — Cloud Hosting](#5-aws-ec2--cloud-hosting)
6. [CI/CD Pipeline — GitHub Actions](#6-cicd-pipeline--github-actions)
7. [Environment Variables & Secrets](#7-environment-variables--secrets)
8. [Data Persistence](#8-data-persistence)

---

## 1. Deployment Architecture

```
                        +---------------------------+
                        |      Developer Machine     |
                        |   git push origin main     |
                        +-------------+-------------+
                                      |
                                      v
                        +---------------------------+
                        |       GitHub Actions       |
                        |  1. Checkout code          |
                        |  2. Setup Java 17          |
                        |  3. Run mvn test (82 tests)|
                        |  4. If tests pass:         |
                        |     SSH into EC2 & deploy  |
                        +-------------+-------------+
                                      |
                                      v
               +----------------------------------------------+
               |              AWS EC2 Instance                 |
               |              (Amazon Linux 2)                 |
               |                                               |
               |   +----------+  +---------+  +------------+  |
               |   | Frontend |  | Backend |  | PostgreSQL |  |
               |   | (Nginx)  |  | (Java)  |  |   (DB)     |  |
               |   | :3000    |  | :7070   |  |  :5432     |  |
               |   +----+-----+  +----+----+  +------+-----+  |
               |        |             |               |        |
               |        +------+------+               |        |
               |               |                      |        |
               |          Docker Network              |        |
               |          (internal DNS)              |        |
               +----------------------------------------------+
                                      |
                                      v
                        +---------------------------+
                        |      End User (Browser)    |
                        |  http://<EC2-IP>:3000      |
                        +---------------------------+
```

### Request Flow

```
Browser                    Nginx (:3000)                Backend (:7070)         PostgreSQL (:5432)
  |                            |                             |                        |
  |--- GET /books ----------->|                             |                        |
  |                            |-- serves React HTML/JS --> |                        |
  |                            |                             |                        |
  |--- GET /api/books ------->|                             |                        |
  |                            |-- proxy_pass ------------->|                        |
  |                            |                             |--- JPQL via ORM ----->|
  |                            |                             |<-- ResultSet ----------|
  |                            |<-- JSON response ----------|                        |
  |<-- JSON to browser -------|                             |                        |
```

- **Static requests** (`/`, `/books`, `/members`): Nginx serves the built React files directly
- **API requests** (`/api/*`): Nginx forwards them to the Java backend, which queries PostgreSQL via Hibernate

---

## 2. Docker — Containerization

Docker packages each component of the application into isolated **containers** — lightweight environments that include everything needed to run the application. This eliminates "works on my machine" problems.

### 2.1 Backend Dockerfile (`/Dockerfile`)

Uses a **multi-stage build** to keep the final image small.

```dockerfile
# Stage 1: Build the JAR (Maven + JDK image — ~800MB)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B        # Cache dependencies
COPY src ./src
RUN mvn clean package -DskipTests -B    # Build JAR (tests run in CI, not here)

# Stage 2: Runtime (JRE-only image — ~200MB)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/libratrack-1.0-SNAPSHOT.jar app.jar
COPY src/main/resources/schema.sql schema.sql
EXPOSE 7070
ENTRYPOINT ["java", "-cp", "app.jar", "com.libratrack.api.ApiServer"]
```

**Why multi-stage?**
- Stage 1 has Maven, JDK, source code — only needed for building
- Stage 2 has just the JRE + built JAR — everything else is discarded
- Final image is ~200MB instead of ~800MB

**Why `-DskipTests`?**
- Tests require a running PostgreSQL database
- During Docker build, the database container hasn't started yet
- Tests are already run in the CI/CD pipeline before deployment

**Dependency caching:**
- `COPY pom.xml` and `RUN mvn dependency:go-offline` are separate steps
- Docker caches each step — if pom.xml hasn't changed, dependencies aren't re-downloaded
- Only when source code changes does the build step re-run

### 2.2 Frontend Dockerfile (`/frontend/Dockerfile`)

Same multi-stage concept for the React app.

```dockerfile
# Stage 1: Build React app (Node.js image)
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install                          # Cache node_modules
COPY . .
RUN npm run build                        # Vite produces static files in /app/dist

# Stage 2: Serve with Nginx (~40MB)
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**Why Nginx instead of Node?**
- After `npm run build`, the React app is just static HTML/JS/CSS files
- No Node.js runtime needed to serve static files
- Nginx is purpose-built for serving static content — faster and uses less memory
- Final image is ~40MB instead of ~300MB with Node

---

## 3. Docker Compose — Orchestration

Docker Compose defines and runs **multiple containers** as a single application. Without it, you'd need to manually run 3 separate `docker run` commands with networking and volume flags.

### `docker-compose.yml`

```yaml
services:
  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: libratrack
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: .
    environment:
      DB_URL: jdbc:postgresql://db:5432/libratrack
      DB_USER: postgres
      DB_PASSWORD: postgres
    ports:
      - "7070:7070"
    depends_on:
      db:
        condition: service_healthy

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  pgdata:
```

### Startup Order

```
1. db (PostgreSQL)
   |-- healthcheck: pg_isready -U postgres (every 5s, up to 5 retries)
   |-- waits until PostgreSQL is accepting connections
   v
2. backend (Java/Javalin)
   |-- depends_on: db (condition: service_healthy)
   |-- only starts after db healthcheck passes
   |-- connects to db using DB_URL: jdbc:postgresql://db:5432/libratrack
   v
3. frontend (Nginx)
   |-- depends_on: backend
   |-- starts after backend is running
   |-- proxies /api/ requests to http://backend:7070
```

### Internal Networking

Docker Compose creates a **private network** where containers find each other by service name:

```
+----------------------------------------------------------+
|  Docker Network (swe_default)                            |
|                                                          |
|  "db"       → resolves to PostgreSQL container IP        |
|  "backend"  → resolves to Java backend container IP      |
|  "frontend" → resolves to Nginx container IP             |
|                                                          |
|  Backend connects to:  jdbc:postgresql://db:5432/...     |
|  Nginx proxies to:     http://backend:7070               |
+----------------------------------------------------------+
```

### Port Mapping

| Service | Container Port | Host Port | Why |
|---------|---------------|-----------|-----|
| db | 5432 | 5433 | 5433 avoids conflict with local PostgreSQL |
| backend | 7070 | 7070 | Javalin API server |
| frontend | 80 | 3000 | Nginx serves on 80 internally, mapped to 3000 externally |

---

## 4. Nginx — Reverse Proxy

### `frontend/nginx.conf`

```nginx
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # Serve React SPA
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API requests to backend
    location /api/ {
        proxy_pass http://backend:7070;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

**`location /` — SPA routing:**
- `try_files $uri $uri/ /index.html` handles client-side routing
- When user visits `/books`, there is no `books.html` file
- Nginx falls back to `index.html`, React loads, React Router renders the Books page
- Without this, refreshing on any route other than `/` would give a 404

**`location /api/` — Reverse proxy:**
- All `/api/*` requests are forwarded to the Java backend
- `http://backend:7070` uses Docker's internal DNS
- `proxy_set_header` lines pass the original client's IP and hostname to the backend
- This avoids CORS issues — browser only talks to port 3000, never directly to 7070

---

## 5. AWS EC2 — Cloud Hosting

### Instance Details

| Property | Value |
|----------|-------|
| Instance type | t3.micro (1 vCPU, 1 GB RAM) |
| OS | Amazon Linux 2 |
| Region | ap-southeast-2 (Sydney) |
| Access | SSH via key pair (.pem file) |
| Security Group | Ports 22 (SSH), 3000 (frontend), 7070 (backend) open |

### Memory Management

t3.micro has only 1 GB RAM. Running three Docker containers requires optimization:

- **Swap space:** 512 MB swap file added to prevent out-of-memory crashes
- **JVM cap:** `JAVA_TOOL_OPTIONS=-Xmx256m` limits Java heap to 256 MB
- Without these, the JVM could consume all available RAM and crash the instance

### Initial Server Setup

```bash
# Install Docker and Docker Compose on EC2
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo usermod -aG docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
  -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Clone repository and deploy
git clone https://github.com/<repo-url> ~/swe
cd ~/swe
docker-compose up -d --build
```

---

## 6. CI/CD Pipeline — GitHub Actions

CI/CD (Continuous Integration / Continuous Deployment) automates testing and deployment. Every push to `main` triggers the pipeline.

### Pipeline Flow

```
git push origin main
        |
        v
+-------------------+
|   GitHub Actions   |
+-------------------+
        |
        v
+-------------------+     FAIL
|   TEST JOB        |-------------> Pipeline stops.
|                   |               Code is NOT deployed.
|  1. Checkout code |
|  2. Setup Java 17 |
|  3. mvn test      |
|     (82 tests)    |
+--------+----------+
         | PASS
         v
+-------------------+
|   DEPLOY JOB      |
|                   |
|  1. SSH into EC2  |
|  2. git pull      |
|  3. docker-compose|
|     up -d --build |
+-------------------+
         |
         v
   App is live with
   latest changes
```

### Workflow File (`.github/workflows/deploy.yml`)

```yaml
name: Deploy to EC2

on:
  push:
    branches: [main]       # Triggers only on push to main branch

jobs:
  test:                     # Job 1: Run tests
    runs-on: ubuntu-latest  # GitHub provides a fresh Ubuntu VM
    steps:
      - uses: actions/checkout@v4        # Clone the repo
      - uses: actions/setup-java@v4      # Install Java 17
        with:
          distribution: temurin
          java-version: 17
      - run: mvn test                    # Run all 82 tests

  deploy:                   # Job 2: Deploy to EC2
    needs: test             # Only runs if test job PASSES
    runs-on: ubuntu-latest
    steps:
      - name: Deploy via SSH
        uses: appleboy/ssh-action@v1     # SSH into EC2
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd ~/swe
            git pull                           # Pull latest code
            docker-compose up -d --build       # Rebuild and restart containers
```

### Key Concepts

- **`needs: test`** — The deploy job is **gated** behind the test job. If any of the 82 tests fail, deployment is skipped entirely. This prevents broken code from reaching production.

- **`on: push: branches: [main]`** — Only pushes to the `main` branch trigger the pipeline. Feature branch pushes are ignored.

- **`${{ secrets.EC2_HOST }}`** — GitHub encrypted secrets store the EC2 IP address and SSH private key. These are never exposed in logs or code.

- **`appleboy/ssh-action`** — A GitHub Action that SSHs into the EC2 instance and runs commands remotely.

- **`docker-compose up -d --build`** — Rebuilds Docker images (picks up code changes), restarts containers in detached mode (`-d`).

---

## 7. Environment Variables & Secrets

### GitHub Secrets (configured in repo Settings > Secrets)

| Secret | Purpose |
|--------|---------|
| `EC2_HOST` | EC2 instance public IP address |
| `EC2_SSH_KEY` | Private SSH key (.pem) for EC2 access |

### Docker Compose Environment Variables

| Variable | Service | Purpose |
|----------|---------|---------|
| `POSTGRES_DB` | db | Database name created on first startup |
| `POSTGRES_USER` | db | PostgreSQL superuser username |
| `POSTGRES_PASSWORD` | db | PostgreSQL superuser password |
| `DB_URL` | backend | JDBC connection string (uses Docker DNS `db:5432`) |
| `DB_USER` | backend | Database username for the Java app |
| `DB_PASSWORD` | backend | Database password for the Java app |

### How Backend Reads Config

The `ApiServer.java` checks environment variables first, then falls back to `application.properties`:

```java
String url = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url"));
```

- **Locally:** No env vars set → uses `application.properties` (localhost:5432)
- **In Docker:** Env vars set by docker-compose → uses `db:5432` (Docker network)

---

## 8. Data Persistence

### Docker Volumes

```yaml
volumes:
  - pgdata:/var/lib/postgresql/data
```

The `pgdata` **named volume** stores PostgreSQL data files on the host machine. This means:

- `docker-compose down` → containers stop, **data survives**
- `docker-compose up` → containers restart, **data is still there**
- `docker-compose down -v` → containers stop, **data is DELETED** (the `-v` flag removes volumes)

### Schema Initialization

```yaml
volumes:
  - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql
```

PostgreSQL automatically runs SQL files in `/docker-entrypoint-initdb.d/` on **first startup only** (when the database doesn't exist yet). On subsequent startups, this is skipped.

Additionally, Hibernate is configured with `hibernate.hbm2ddl.auto = update`, which automatically creates or updates tables based on entity annotations. This provides a safety net — even without the schema.sql, Hibernate will create the necessary tables.

---

## Summary

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Containerization | Docker | Package app + dependencies into portable images |
| Orchestration | Docker Compose | Run 3 containers (DB, backend, frontend) together |
| Web Server | Nginx | Serve React SPA + reverse proxy API requests |
| Cloud Hosting | AWS EC2 | Run containers on a remote server |
| CI/CD | GitHub Actions | Auto-test and auto-deploy on every push to main |
| Version Control | Git + GitHub | Source code management and collaboration |
