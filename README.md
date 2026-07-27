# PlantPulse

**CMMS + real-time IoT condition monitoring for industrial plants.**

A maintenance management system (CMMS) — assets, technicians, spare parts, work
orders — with a live telemetry layer on top: simulated sensors stream
temperature/vibration data over WebSocket, and the system automatically opens
a corrective work order the moment a machine drifts out of range. No manual
data entry needed to see it work.

Built as a portfolio project to demonstrate a full Java + Spring Boot + React
stack on a domain that isn't another todo app or e-commerce clone: industrial
software / Industry 4.0.

---

## Why this project

Most junior portfolios show a CRUD app. This one shows:

- A **real business domain** (industrial maintenance) that any plant manager,
  facilities lead, or ops engineer immediately recognizes.
- A **live system**, not just a form: WebSocket telemetry, auto-generated
  alerts, auto-generated corrective work orders — cause and effect the
  reviewer can watch happen in a 20-second screen recording.
- A clean separation of concerns across a Spring Boot REST + WebSocket
  backend and a React + TypeScript frontend, the kind of split real teams
  use in production.

---

## Screenshots

<!-- TODO: add real screenshots/GIF of the running dashboard to screenshots/
     and reference them here, e.g.:
     ![Dashboard](screenshots/dashboard.png)
     Nothing has been added yet — see screenshots/README.md. -->

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

---

## Roadmap

The backend and frontend skeletons above are functional but intentionally
leave room to keep building — a portfolio project is more convincing when
the commit history shows it growing. Suggested order:

**Phase 1 — Harden what exists**
- [ ] Unit tests for `WorkOrderService` and the threshold logic in
      `TelemetrySimulatorService` (JUnit 5 + Mockito)
- [ ] `@ControllerAdvice` global exception handler (404s currently rely on
      ad-hoc `Optional` handling)
- [ ] Bean Validation (`@NotBlank`, `@Min`, etc.) on entities/DTOs and a
      request-body validation layer for the controllers

**Phase 2 — Auth**
- [ ] Spring Security + JWT, two roles: `ADMIN` (manage machines/technicians)
      and `TECHNICIAN` (view + update assigned work orders)
- [ ] Login page and protected routes on the frontend

**Phase 3 — Deepen the domain**
- [ ] Spare parts inventory page with low-stock highlighting (the entity and
      endpoint already exist — `SparePart.isBelowThreshold()` — just needs a UI)
- [ ] Preventive maintenance scheduling (recurring work orders by calendar
      or by machine run-hours)
- [ ] Historical telemetry: persist readings (or a downsampled rollup) so a
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
- [ ] OpenAPI/Swagger docs (`springdoc-openapi`)

---

## License

MIT — do whatever you want with this, it's a portfolio starter.
