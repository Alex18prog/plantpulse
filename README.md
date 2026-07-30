# PlantPulse

**A CMMS with live IoT condition monitoring — built to prove I can ship a real full-stack system, not just CRUD.**

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

**[▶ Live demo](https://alex18prog.github.io/plantpulse/)** · login: `admin@plantpulse.dev` / `admin123`
*(free-tier backend — first load can take up to ~1 min to wake up; the app shows a "waking up" message and retries automatically — see [Known limitations](#known-limitations))*

<!-- TODO: replace with a real screenshot or GIF of the live dashboard once captured -->
<!-- ![PlantPulse dashboard](docs/screenshot-dashboard.png) -->

## What this is

Most junior portfolios show a to-do app or an online store. PlantPulse is industrial maintenance software (a CMMS) — the kind of internal tool a real factory runs on — with a live telemetry layer on top: simulated sensors stream over WebSocket, and the system **reacts on its own**. When a reading drifts out of range, it raises an alert and opens a corrective work order automatically, with no human in the loop.

- 🔧 **A real domain** — assets, technicians, spare parts, preventive/corrective work orders: a system a plant manager recognizes immediately, not an invented CRUD.
- 📡 **Actually live** — WebSocket telemetry, auto-generated alerts and work orders. Cause and effect you can watch happen in 20 seconds, not a static screenshot.
- 🔐 **Production-shaped** — JWT auth with two roles, CORS wired correctly at the Spring Security level (not just a WebMvcConfigurer that silently does nothing), Swagger docs, Docker Compose, CI on GitHub Actions, and a real deploy across three separate free-tier services (GitHub Pages + Render + Neon) wired together end to end.
- 🧪 **Tested, not just working** — JUnit/Mockito on the backend, Playwright end-to-end on the frontend, verified cross-origin in production, not only on localhost.

## Quick links
- 🚀 [Live demo](https://alex18prog.github.io/plantpulse/)
- 📖 [Architecture & setup](#architecture) — below
- 🗺️ [Roadmap](#roadmap) — built phase by phase, with a clean commit history
- ⚠️ [Known limitations](#known-limitations) — the honest list, not hidden

---

## Architecture

```
                     ┌─────────────────────────┐
                     │   TelemetrySimulator     │  @Scheduled every 3s
                     │   (stand-in for sensors) │
                     └─────────────┬────────────┘
                                   │ perturbs baseline readings
                                   ▼
      ┌───────────────────────────────────────────────────┐
      │                  Spring Boot backend                │
      │                                                     │
      │  REST API (/api/**)         WebSocket (STOMP)        │
      │  ─ Machines                 /topic/telemetry        │
      │  ─ Technicians              /topic/alerts           │
      │  ─ Spare parts                                       │
      │  ─ Work orders        ──▶ threshold breach ──▶       │
      │  ─ Alerts                 Alert + auto WorkOrder      │
      │                                                     │
      │  H2 (dev)  /  PostgreSQL (prod)                      │
      └───────────────────────────┬─────────────────────────┘
                                   │ REST + STOMP over SockJS
                                   ▼
      ┌───────────────────────────────────────────────────┐
      │                 React + TypeScript frontend          │
      │                                                     │
      │  Dashboard: machine cards (live gauges + sparkline)  │
      │  Alert log: scrolling feed of threshold breaches     │
      │  Work orders: 3-column board (Pending/Progress/Done) │
      └───────────────────────────────────────────────────┘
```

### Backend — `backend/`

| Package | Responsibility |
|---|---|
| `domain` | JPA entities: `Machine`, `Technician`, `SparePart`, `WorkOrder`, `Alert` |
| `domain.enums` | `MachineStatus`, `WorkOrderStatus`, `WorkOrderType`, `Priority`, `AlertSeverity`, `TechnicianStatus` |
| `repository` | Spring Data JPA repositories |
| `service` | `WorkOrderService` — status transitions, auto-corrective-order creation |
| `scheduler` | `TelemetrySimulatorService` — the heartbeat of the whole app |
| `controller` | REST controllers under `/api/**` |
| `config` | `WebSocketConfig` (STOMP), `WebConfig` (CORS), `DemoDataSeeder` (startup data) |
| `dto` | `TelemetryMessage`, `AlertMessage` — WebSocket payloads |

Stack: Java 21, Spring Boot 3.3, Spring Data JPA, Spring WebSocket, Lombok,
H2 (dev) / PostgreSQL (prod), Maven.

### Frontend — `frontend/`

| Path | Responsibility |
|---|---|
| `lib/api.ts` | Typed fetch wrapper for the REST API |
| `lib/useRealtime.ts` | STOMP/SockJS hook — telemetry + alert streams |
| `components/GaugeDial.tsx` | Radial analog-style gauge (signature visual) |
| `components/MachineCard.tsx` | Per-machine panel: gauges, sparkline, status LED |
| `components/AlertsFeed.tsx` | Live alert log |
| `components/WorkOrdersBoard.tsx` | 3-column work order board |
| `components/StatusBar.tsx` | Top SCADA-style connection/health bar |

Stack: React 19, TypeScript, Vite, Tailwind CSS v4, TanStack Query, Recharts,
`@stomp/stompjs` + `sockjs-client`, lucide-react.

Design system: dark "control room" palette (graphite background, steel-blue
dividers, amber/green/red signal colors), Space Grotesk + IBM Plex Mono
typography — deliberately built to look like an industrial HMI panel rather
than a generic admin dashboard.

---

## Running it locally

### Quick start (Docker)

The recommended way to try PlantPulse — one command, Postgres included, no
local Java/Node toolchain required:

```bash
cp .env.example .env   # optional — the defaults in docker-compose.yml already work
docker compose up --build
```

- Frontend: `http://localhost:8081`
- Backend API: `http://localhost:8080`

Log in with `admin@plantpulse.dev` / `admin123` (ADMIN) or
`marta.ruiz@plantpulse.dev` / `tech123` (TECHNICIAN) — same demo accounts as
local dev, seeded automatically on first boot. See `.env.example` for the
variables you can override (Postgres credentials, JWT secret, host ports).

### Development

Faster feedback loop than rebuilding containers; useful if you're actively
working on the code. Runs the backend against an in-memory H2 database
instead of Postgres.

#### Backend
```bash
cd backend
./mvnw spring-boot:run
# or, if you don't have the wrapper jar yet: mvn spring-boot:run
```
Runs on `http://localhost:8080` with an in-memory H2 database (`dev` profile,
active by default) seeded with 4 demo machines, 2 technicians, and 3 spare
parts. H2 console at `http://localhost:8080/h2-console` (JDBC URL
`jdbc:h2:mem:plantpulse`, user `sa`, no password).

#### Frontend
```bash
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173`, proxying `/api` and `/ws` to the backend
(see `vite.config.ts`). Open it and the machine cards should start moving
within a few seconds as simulated telemetry arrives.

---

## Known limitations

- WebSocket topics (`/topic/telemetry`, `/topic/alerts`) are not authorized per user — acceptable for this single-tenant demo, but a real multi-tenant system would need per-topic authorization.
- The backend is deployed on Render's free tier, which spins down after 15 minutes of no traffic. The first request after that can take up to ~1 minute to respond (JVM boot on a 0.1 CPU instance) — the app itself handles this with a "waking up" message and automatic retry, no action needed.

---

## Roadmap

The backend and frontend skeletons above are functional but intentionally
leave room to keep building — a portfolio project is more convincing when
the commit history shows it growing. Suggested order:

**Phase 1 — Harden what exists**
- [x] Unit tests for `WorkOrderService` and the threshold logic in
      `TelemetrySimulatorService` (JUnit 5 + Mockito)
- [x] `@ControllerAdvice` global exception handler (404s currently rely on
      ad-hoc `Optional` handling)
- [x] Bean Validation (`@NotBlank`, `@Min`, etc.) on entities/DTOs and a
      request-body validation layer for the controllers

**Phase 2 — Auth**
- [x] Spring Security + JWT, two roles: `ADMIN` (manage machines/technicians)
      and `TECHNICIAN` (view + update assigned work orders)
- [x] Login page and protected routes on the frontend

**Phase 3 — Deepen the domain**
- [x] Spare parts inventory page with low-stock highlighting (the entity and
      endpoint already exist — `SparePart.isBelowThreshold()` — just needs a UI)
- [x] Preventive maintenance scheduling (recurring work orders by calendar
      or by machine run-hours)
- [x] Historical telemetry: persist readings (or a downsampled rollup) so a
      machine detail page can show trends over days/weeks, not just the last
      20 live points

**Phase 4 — Polish for reviewers**
- [x] `docker-compose.yml` (Postgres + backend + frontend) so anyone can run
      `docker compose up --build` and see it working in one command
- [x] GitHub Actions CI: build + test on push
- [ ] Screenshots/GIF of the live dashboard in this README
- [ ] Deploy a live demo (e.g. Railway/Render for the backend, Vercel for the
      frontend) and link it here

**Phase 5 — Nice-to-haves**
- [ ] Drag-and-drop on the work order board (`@dnd-kit`)
- [ ] Email/webhook notification on critical alerts
- [x] OpenAPI/Swagger docs (`springdoc-openapi`)

---

## License

MIT — do whatever you want with this, it's a portfolio starter.
