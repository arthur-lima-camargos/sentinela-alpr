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
| 3 | Domain                      | ⚪ Pending      |
| 4 | Security (JWT)              | ⚪ Pending      |
| 5 | Performance                 | ⚪ Pending      |
| 6 | Real time                   | ⚪ Pending      |

**Legend:** ✅ done · 🟡 in progress · ⚪ pending

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

**Phase 3 — Domain**
Business logic of the modules: cameras (CRUD), detections (recording + keyset query
+ event), watchlist (CRUD) and alerts (matching + status). With DTOs, validation,
error handling (`ProblemDetail`) and tests.

**Phase 4 — Security (JWT)**
Backend: user, stateless authentication, login issuing a JWT, roles and BCrypt
password hashing. Frontend: login screen, token interceptor, route guards and
expired-session handling.

**Phase 5 — Performance**
Deterministic base scenario and a 5–10M detection seed; index validation with
`EXPLAIN ANALYZE`; concurrency tests; load testing with k6 (latency and
throughput); camera simulator.

**Phase 6 — Real time**
WebSocket/STOMP: the backend publishes alerts to `/topic/alerts` and the
dashboard shows them live, with visual urgency highlighting.

## Current status

Environment and **scaffold ready**: Spring Boot 4 backend (compiles, connects to
Postgres, migration V1 applied) and Angular 22 frontend (build ok), with CI on
GitHub Actions. Next step: **Phase 3 — domain** (cameras, detections, watchlist and
alerts modules).
