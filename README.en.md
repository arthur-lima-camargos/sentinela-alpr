# Sentinela ALPR

[🇧🇷 Português](README.md) &nbsp;|&nbsp; 🇺🇸 English

**Vehicle monitoring system based on Automatic License Plate Recognition (ALPR)**
for a public-safety scenario. It records vehicle detections captured by cameras,
queries the history, and raises **alerts** when a monitored plate is seen.

> Plate recognition (OCR) is out of scope: the system receives the **already
> recognized** plate via REST, simulating the cameras.

## Goals

- Learn a modern **Java 21 / Spring Boot 4**, **Angular** and **PostgreSQL**
  stack in practice.
- Build a well-modeled API with clear domain boundaries.
- **Demonstrate performance** with millions of records (indexing, efficient
  pagination and load testing).
- Exercise concurrency and collisions under near real-world conditions.

## Stack

- **Backend:** Java 21 (LTS) + Spring Boot 4.x, Maven (via Wrapper)
- **Database:** PostgreSQL + Flyway (plain SQL migrations)
- **Frontend:** Angular (latest stable, standalone) + WebSocket/STOMP
- **Security:** Spring Security 7 + JWT
- **Testing:** JUnit 5 + Mockito + Testcontainers; load testing with k6

## Features (MVP)

1. Camera registration (capture points)
2. Detection recording (plate reads)
3. Detection querying (by plate, camera and time range)
4. Watchlist of monitored vehicles
5. Automatic alerts when a monitored plate is detected
6. Dashboard with real-time alerts

## Progress

| # | Phase                       | Status          |
|:-:|-----------------------------|-----------------|
| 0 | Architecture & planning     | ✅ Done          |
| 1 | Environment                 | ✅ Done          |
| 2 | Scaffold                    | ✅ Done          |
| 3 | Domain (REST API)           | ✅ Done          |
| 4 | Security (JWT)              | ✅ Done          |
| 5 | Performance (benchmark)     | 🟡 Partial      |
| 6 | Frontend — API screens      | ✅ Done         |

**Legend:** ✅ done · 🟡 in progress/partial · ⚪ pending

## Phase details

**Phase 0 — Architecture & planning**
Definition of the stack, architectural style (modular monolith), MVP scope,
domain and data models, and the performance, concurrency and testing strategies.

**Phase 1 — Environment**
Install/validate JDK 21, Node LTS, Docker Desktop and VS Code (with extensions); create the
`docker-compose.yml` for the development PostgreSQL and validate each tool.

**Phase 2 — Scaffold**
Project skeletons: Spring Boot backend (Maven Wrapper, per-module structure,
`application.yml`), Angular frontend (`core/ shared/ features/`) and the initial
schema migration. Verify the backend boots, connects to the database and the
frontend serves. Includes **Continuous Integration** setup (GitHub Actions),
validating build and tests on every push.

**Phase 3 — Domain (REST API)**
Business logic of the modules: cameras (CRUD), detections (recording + keyset query
+ event), watchlist (CRUD) and alerts (matching + status). With DTOs, validation,
error handling (`ProblemDetail`) and tests. It produced the **complete MVP REST
API** that Phase 6 now consumes.

**Phase 4 — Security (JWT)**
Backend: user, stateless authentication, login issuing a JWT, roles and BCrypt
password hashing. Frontend: login screen, token interceptor, route guards and
expired-session handling.

**Phase 5 — Performance (benchmark)**
An A/B benchmark **with vs without indexes** over 10M detections (reads, writes and
disk) in a fixed-resource Docker environment — material for a technical write-up.
**Done for that scope (database only).** **Deferred** (off the MVP critical path):
k6 load testing, the camera simulator and concurrency tests.

**Phase 6 — Frontend: API consumption screens**
Build the screens that consume the API resources: **cameras** and **watchlist**
(done), **detections** (keyset query with filters), **alerts** (status triage) and
the **real-time panel** (WebSocket/STOMP client subscribing to `/topic/alerts`, with
visual urgency highlighting). Real time is the final deliverable of this phase.

## Current status

**Domain implemented (Phase 3):** the four business modules — cameras (CRUD),
detections (record + **keyset** query + domain event), watchlist (CRUD, normalized
plate) and alerts (automatic matching + status) — with `record` DTOs, Bean
Validation, `ProblemDetail` errors and the cross-cutting infra
(`GlobalExceptionHandler`, `DomainEventPublisher`, STOMP `/topic/alerts`). Alert
generation runs on `AFTER_COMMIT` + `REQUIRES_NEW` (ADR-020).

**Security — Phase 4a (human JWT) done:** the `auth` module with login
(`POST /api/v1/auth/login`) and refresh (`POST /api/v1/auth/refresh`) issuing
**HS256 JWTs** via OAuth2 Resource Server (ADR-021); short access token + stateless
refresh (ADR-022); passwords with **BCrypt**; `OPERATOR`/`ADMIN` roles (writing
cameras/watchlist requires ADMIN); 401/403 as `ProblemDetail`; a dev admin user
seeded in migration `V2` (ADR-023). **27 integration tests** (Testcontainers)
green.

**Security — Phase 4b (camera API keys) done:** ingestion
`POST /api/v1/detections` is no longer public and now requires a **per-camera API
key** in the `X-API-Key` header (`ROLE_CAMERA` via a filter). Keys live in a
dedicated `camera_api_key` table (migration `V3`) storing only the **SHA-256 hash**
(not BCrypt — a high-entropy key with deterministic lookup); minting returns the
secret **once** (`POST /cameras/{id}/api-keys`, ADMIN), with listing and revocation.
The `cameraId` is now **derived from the key** (dropped from the body). Missing/
invalid key or inactive camera → 401. See ADR-024.

**Security — Phase 4c (WebSocket/STOMP) done:** the real-time connection is
authenticated on the **STOMP `CONNECT` frame** by a `ChannelInterceptor` that
validates the JWT (`Authorization` header) reusing the `JwtDecoder`; without a valid
token the connection is refused. Only authenticated users subscribe to
`/topic/alerts`. See ADR-025. With this, **Phase 4 is complete**: **36 integration
tests** green (including STOMP via `WebSocketStompClient`). The Angular WebSocket
client arrives in **Phase 6 (Real time)**.

**Frontend — login (Phase 4) done:** Angular app with a `core/features` layout, a
**login** screen (Reactive Forms, loading/error states), an `AuthService` (signals
+ `localStorage`), an **interceptor** that attaches `Bearer` and performs **silent
refresh on 401** (single-flight), a route **guard** and a protected shell (`home`)
with the logged-in user and logout. Design tokens (dark theme) in `styles.scss`;
Angular CLI proxy (`/api → :8080`) for dev cross-origin. Flow validated end to end
through the proxy (login/refresh/401/403).

**Frontend — camera management (done):** the first data screen. An authenticated
navigation shell (topbar + Panel/Cameras menu), a **cameras** table with pagination,
create/edit in a **modal** (Reactive Forms + validation mirroring the backend) and
actions restricted to **ADMIN** (OPERATOR sees read-only). On the backend, camera
**reactivation** was added (`POST /api/v1/cameras/{id}/activate`), making the
soft-delete reversible (ADR-026).

**Frontend — watchlist management (done):** the second data screen, following the
cameras pattern (paginated table + modal). To align it, the watchlist backend gained
a **reversible soft-delete** (`DELETE` now sets `active=false`), **reactivation**
(`POST /api/v1/watchlist/{id}/activate`) and **reason reclassification**
(`PUT /api/v1/watchlist/{id}`, `reason` only — the plate is immutable), all restricted
to ADMIN (ADR-028). The screen validates the plate on the client (same Mercosur/old
format as the backend), shows the reason (Robbery/Theft/Wanted/Suspect) and status,
with plates in a monospace font. Common data-screen styles were extracted into a
shared SCSS partial.

**Frontend — detections (done):** the central read screen. It consumes
`GET /api/v1/detections` with **cursor (keyset) pagination** — a **"Load more"**
button that appends the next batch (the cursor is forward-only). Filters for **plate**
(exact match, normalized on the client), **camera** (dropdown), and **time range**
(from/to via `datetime-local`, converted to a UTC `Instant`). Each detection's
`cameraId` is resolved to the **camera name** by fetching the camera list once.
Read-only (any role).

**Frontend — alerts and real time (done):** the flagship screen. An alert list
(`GET /api/v1/alerts`) with a **status filter** (All/New/Seen), triage via `PATCH`
(**"Mark as seen"/"Reopen"**) and an urgency badge (NEW in critical color + icon +
text). **Real time** uses a **STOMP** client (`@stomp/stompjs`) that connects to `/ws`
authenticating with the **access token in the `CONNECT` frame** (pulled on each
attempt, surviving refresh), auto-reconnects and subscribes to `/topic/alerts`. The
`AlertRealtimeService` lives in the **shell** (app-wide): a **bell with a counter** in
the topbar lights up when an alert arrives (always visible), and the screen itself
**prepends the alert live**, with a fading highlight. The alert reason
(Robbery/Theft…) is resolved from the watchlist. Dev proxy `/ws` (`ws: true`);
ADR-029.

**Frontend — API key management (done):** inside the cameras screen, a **"Keys"
modal** per camera (ADMIN) that **lists** the keys (prefix, status, dates), **issues**
a new one showing the **secret once** (with a warning and copy button) and **revokes**
active ones — consuming `POST/GET/DELETE /api/v1/cameras/{id}/api-keys`. It closes the
usage loop: a registered camera now gets its key through the UI itself. Coverage
expanded across backend and frontend (Vitest); the STOMP connection itself is verified
at runtime.

**Frontend — home dashboard (done):** the phase's last screen. The `home` is no longer
a placeholder and became a **metrics panel** with four cards: **pending alerts** (NEW,
highlighted as urgent when > 0), **detections** (last 24h / last hour), **cameras**
(active / inactive) and **watched vehicles** (active). Each card links to its screen,
and the alerts card **increments live** when an alert arrives over STOMP. The metrics
come from **per-module aggregation endpoints**
(`GET /api/v1/{alerts,detections,cameras,watchlist}/summary`), loaded in parallel;
detection counts use **rolling windows** (24h/1h) over `detected_at`. With this,
**Phase 6 is done**: **61** backend integration tests and **67** on the frontend.

**Performance — Phase 5 (benchmark done; load testing deferred):** an **A/B
benchmark with vs without indexes** over 10M detections, measuring reads, writes and
disk in a fixed-resource Docker environment (ADR-027). Results: plate lookup
~11,000× faster; camera+time range with a **composite** index
`(camera_id, detected_at)` ~83×; BRIN (40 kB) ~60× on a recent window; writes ~2.75×
slower and ~602 MB of indexes. The harness (not versioned) lives in `bench/`.
**Deferred** (off the MVP critical path): concurrency tests, k6 load testing and the
camera simulator.

## Current moment

The backend exposes the **complete MVP REST API** (auth, cameras + API keys,
detections, watchlist, alerts) and the **real-time alert broadcast** (STOMP
`/topic/alerts`), all covered by **61 integration tests**. The **frontend consumes all
those resources** — login, cameras (with API key management), watchlist, detections,
alerts (with real time) and the **home dashboard with metrics** — closing Phase 6.

**API coverage by the frontend:**

| Resource                                       | Backend | Screen |
|------------------------------------------------|:-------:|:------:|
| Authentication (login / refresh)               |   ✅    |   ✅   |
| Cameras (CRUD)                                  |   ✅    |   ✅   |
| Per-camera API keys (issue / list / revoke)     |  ✅    |   ✅   |
| Detections (keyset query + filters)            |   ✅    |   ✅   |
| Watchlist (CRUD + reclassification)            |   ✅    |   ✅   |
| Alerts (list / filter + status change)         |   ✅    |   ✅   |
| Real-time alerts (`/topic/alerts`)             |   ✅    |   ✅   |
| Home dashboard (metrics via `/summary`)        |   ✅    |   ✅   |

> Detection ingestion (`POST /api/v1/detections`) is consumed by the **camera**
> (via API key), not by the UI — hence no screen.

**Phase 6 done.** Optional next steps: pick up the deferred Phase 5 items (k6 load
testing, camera simulator, concurrency tests).
