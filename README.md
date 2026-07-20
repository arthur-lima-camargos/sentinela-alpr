# Sentinela ALPR

🇧🇷 Português &nbsp;|&nbsp; [🇺🇸 English](README.en.md)

Sistema de **monitoramento veicular por reconhecimento de placas (ALPR)** para um
cenário de segurança pública. Registra passagens de veículos por câmeras,
consulta o histórico e dispara **alertas** quando uma placa monitorada é vista.

> O reconhecimento de placa (OCR) está fora do escopo: o sistema recebe a placa
> **já reconhecida** via REST, simulando as câmeras.

## Objetivos

- Aprender, na prática, uma stack moderna de **Java 21 / Spring Boot 4**,
  **Angular** e **PostgreSQL**.
- Construir uma API bem modelada, com boas fronteiras de domínio.
- **Demonstrar desempenho** com milhões de registros (indexação, paginação
  eficiente e testes de carga).
- Exercitar concorrência e colisão em condições próximas às da vida real.

## Stack

- **Backend:** Java 21 (LTS) + Spring Boot 4.x, Maven (via Wrapper)
- **Banco:** PostgreSQL + Flyway (migrations em SQL puro)
- **Frontend:** Angular (última estável, standalone) + WebSocket/STOMP
- **Segurança:** Spring Security 7 + JWT
- **Testes:** JUnit 5 + Mockito + Testcontainers; carga com k6

## Funcionalidades (MVP)

1. Cadastro de câmeras (pontos de captura)
2. Registro de passagens (leituras de placa)
3. Consulta de passagens (por placa, câmera e período)
4. Watchlist de veículos monitorados
5. Alertas automáticos ao detectar placa monitorada
6. Dashboard com alertas em tempo real

## Progresso

| # | Fase                        | Status         |
|:-:|-----------------------------|----------------|
| 0 | Arquitetura e planejamento  | ✅ Concluído    |
| 1 | Ambiente                    | ✅ Concluído    |
| 2 | Scaffold                    | ✅ Concluído    |
| 3 | Domínio                     | ✅ Concluído    |
| 4 | Segurança (JWT)             | ✅ Concluído    |
| 5 | Desempenho                  | ⚪ Pendente     |
| 6 | Tempo real                  | ⚪ Pendente     |

**Legenda:** ✅ concluído · 🟡 em andamento · ⚪ pendente

## Detalhamento das fases

**Fase 0 — Arquitetura e planejamento**
Definição de stack, estilo arquitetural (monólito modular), escopo do MVP, modelo
de domínio e de dados, e estratégias de desempenho, concorrência e testes.

**Fase 1 — Ambiente**
Instalar/validar JDK 21, Node LTS, Docker Desktop e VS Code (com extensões); criar o
`docker-compose.yml` do PostgreSQL de desenvolvimento e validar cada ferramenta.

**Fase 2 — Scaffold**
Esqueleto dos projetos: backend Spring Boot (Maven Wrapper, estrutura por módulo,
`application.yml`), frontend Angular (`core/ shared/ features/`) e a migration
inicial do schema. Validar que backend sobe, conecta no banco e o frontend serve.
Inclui a configuração da **Integração Contínua** (GitHub Actions), validando build
e testes a cada push.

**Fase 3 — Domínio**
Regra de negócio dos módulos: câmeras (CRUD), passagens (registro + consulta por
keyset + evento), watchlist (CRUD) e alertas (matching + status). Com DTOs,
validação, tratamento de erro (`ProblemDetail`) e testes.

**Fase 4 — Segurança (JWT)**
Backend: usuário, autenticação stateless, login com emissão de JWT, perfis e senha
com BCrypt. Frontend: tela de login, interceptor de token, guards de rota e
tratamento de sessão expirada.

**Fase 5 — Desempenho**
Cenário base determinístico e seed de 5–10M passagens; validação de índices com
`EXPLAIN ANALYZE`; testes de concorrência; carga com k6 (latência e throughput);
simulador de câmeras.

**Fase 6 — Tempo real**
WebSocket/STOMP: o backend publica alertas em `/topic/alerts` e o dashboard os
exibe ao vivo, com destaque visual de urgência.

## Status atual

**Domínio implementado (Fase 3):** os quatro módulos de negócio — câmeras (CRUD),
passagens (registro + consulta por **keyset** + evento de domínio), watchlist
(CRUD, placa normalizada) e alertas (matching automático + status) — com DTOs
`record`, Bean Validation, erros `ProblemDetail` e a infra transversal
(`GlobalExceptionHandler`, `DomainEventPublisher`, STOMP `/topic/alerts`). A
geração de alerta usa `AFTER_COMMIT` + `REQUIRES_NEW` (ADR-020).

**Segurança — Fase 4a (JWT humano) concluída:** módulo `auth` com login
(`POST /api/v1/auth/login`) e renovação (`POST /api/v1/auth/refresh`) emitindo
**JWT HS256** via OAuth2 Resource Server (ADR-021); access token curto + refresh
stateless (ADR-022); senha com **BCrypt**; perfis `OPERATOR`/`ADMIN` (escrita de
câmeras/watchlist exige ADMIN); 401/403 em `ProblemDetail`; usuário admin de dev
semeado na migration `V2` (ADR-023). **27 testes de integração** (Testcontainers)
verdes.

**Segurança — Fase 4b (API Key das câmeras) concluída:** ingestão
`POST /api/v1/detections` deixou de ser pública e passou a exigir **API Key por
câmera** no header `X-API-Key` (`ROLE_CAMERA` via filtro). Chaves em tabela
dedicada `camera_api_key` (migration `V3`), guardando só o **hash SHA-256** (não
BCrypt — chave de alta entropia e busca determinística); emissão devolve o segredo
**uma única vez** (`POST /cameras/{id}/api-keys`, ADMIN), com listagem e revogação.
O `cameraId` agora é **derivado da chave** (saiu do corpo). Chave ausente/inválida/
câmera inativa → 401. Detalhes em ADR-024.

**Segurança — Fase 4c (WebSocket/STOMP) concluída:** a conexão de tempo real é
autenticada no **frame STOMP `CONNECT`** por um `ChannelInterceptor` que valida o
JWT (header `Authorization`) reusando o `JwtDecoder`; sem token válido a conexão é
recusada. Só usuário autenticado assina `/topic/alerts`. Detalhes em ADR-025. Com
isso a **Fase 4 está concluída**: **36 testes de integração** verdes (inclui STOMP
com `WebSocketStompClient`). O cliente WebSocket no Angular chega na **Fase 6
(Tempo real)**.

**Frontend — login (Fase 4) concluído:** app Angular com estrutura `core/features`,
tela de **login** (Reactive Forms, estados de carregando/erro), `AuthService`
(signals + `localStorage`), **interceptor** que anexa `Bearer` e faz **silent
refresh no 401** (single-flight), **guard** de rota e um shell protegido (`home`)
com usuário logado e logout. Tokens de design (tema escuro) em `styles.scss`;
proxy do Angular CLI (`/api → :8080`) para o dev cross-origin. Fluxo validado ponta
a ponta pelo proxy (login/refresh/401/403).

Com a Fase 4 concluída, o **próximo passo é a Fase 5 — Desempenho** (seed de alto
volume, índices, concorrência e carga com k6).
