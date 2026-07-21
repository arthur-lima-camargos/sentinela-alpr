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
| 3 | Domínio (API REST)          | ✅ Concluído    |
| 4 | Segurança (JWT)             | ✅ Concluído    |
| 5 | Desempenho (benchmark)      | 🟡 Parcial      |
| 6 | Frontend — telas de consumo | ✅ Concluído    |

**Legenda:** ✅ concluído · 🟡 em andamento/parcial · ⚪ pendente

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

**Fase 3 — Domínio (API REST)**
Regra de negócio dos módulos: câmeras (CRUD), passagens (registro + consulta por
keyset + evento), watchlist (CRUD) e alertas (matching + status). Com DTOs,
validação, tratamento de erro (`ProblemDetail`) e testes. Resultou na **API REST
completa do MVP**, que a Fase 6 passa a consumir.

**Fase 4 — Segurança (JWT)**
Backend: usuário, autenticação stateless, login com emissão de JWT, perfis e senha
com BCrypt. Frontend: tela de login, interceptor de token, guards de rota e
tratamento de sessão expirada.

**Fase 5 — Desempenho (benchmark)**
Benchmark comparativo **com/sem índice** sobre 10M passagens (leitura, escrita e
disco) em ambiente Docker de recursos fixos — material para publicação técnica.
**Concluído para esse escopo (só banco).** Ficam **adiados** (fora do caminho
crítico do MVP): carga com k6, simulador de câmeras e testes de concorrência.

**Fase 6 — Frontend: telas de consumo da API**
Construir as telas que consomem os recursos da API: **câmeras** e **watchlist**
(prontas), **passagens** (consulta por keyset com filtros), **alertas** (triagem por
status) e o **painel em tempo real** (cliente WebSocket/STOMP assinando
`/topic/alerts`, com destaque visual de urgência). O tempo real é a entrega final
desta fase.

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

**Frontend — gestão de câmeras (concluído):** primeira tela de dados. Shell de
navegação autenticado (topbar + menu Painel/Câmeras), tela de **câmeras** em tabela
com paginação, criação/edição em **modal** (Reactive Forms + validação espelhando o
backend) e ações restritas a **ADMIN** (OPERATOR vê somente leitura). No backend,
adicionada a **reativação** de câmera (`POST /api/v1/cameras/{id}/activate`),
tornando o soft-delete reversível (ADR-026).

**Frontend — gestão da watchlist (concluído):** segunda tela de dados, no mesmo
padrão de câmeras (tabela paginada + modal). Para alinhá-la, o backend da watchlist
ganhou **soft-delete reversível** (`DELETE` passou a marcar `active=false`),
**reativação** (`POST /api/v1/watchlist/{id}/activate`) e **reclassificação de
motivo** (`PUT /api/v1/watchlist/{id}`, só o `reason` — a placa é imutável), tudo
restrito a ADMIN (ADR-028). A tela valida a placa no cliente (mesmo formato
Mercosul/antigo do backend), exibe o motivo (Roubo/Furto/Procurado/Suspeito) e o
status, com placas em fonte monoespaçada. Estilos comuns das telas de dados
extraídos para um parcial SCSS compartilhado.

**Frontend — passagens (concluído):** a tela de leitura central. Consome
`GET /api/v1/detections` com **paginação por cursor (keyset)** — botão **"Carregar
mais"** que anexa a próxima leva (o cursor é forward-only). Filtros de **placa**
(busca exata, normalizada no cliente), **câmera** (dropdown), e **período** (de/até
via `datetime-local`, convertidos para `Instant` UTC). O `cameraId` da passagem é
resolvido para o **nome da câmera** buscando a lista de câmeras uma vez. Somente
leitura (qualquer perfil).

**Frontend — alertas e tempo real (concluído):** a tela-vitrine. Lista de alertas
(`GET /api/v1/alerts`) com **filtro por status** (Todos/Novos/Vistos), triagem via
`PATCH` (**"Marcar como visto"/"Reabrir"**) e badge de urgência (NEW em cor crítica +
ícone + texto). O **tempo real** usa um cliente **STOMP** (`@stomp/stompjs`) que
conecta ao `/ws` autenticando com o **access token no frame `CONNECT`** (puxado a cada
tentativa, sobrevivendo a refresh), reconecta sozinho e assina `/topic/alerts`. O
`AlertRealtimeService` vive no **shell** (app-wide): um **sino com contador** no
topbar acende ao chegar alerta (sempre visível), e a própria tela **insere o alerta
no topo ao vivo**, com destaque que esmaece. O motivo do alerta (Roubo/Furto…) é
resolvido pela watchlist. Proxy de dev `/ws` (`ws: true`); ADR-029.

**Frontend — gestão de API keys (concluído):** dentro da tela de câmeras, um **modal
"Chaves"** por câmera (ADMIN) que **lista** as chaves (prefixo, status, datas),
**emite** uma nova exibindo o **segredo uma única vez** (com aviso e botão copiar) e
**revoga** as ativas — consumindo `POST/GET/DELETE /api/v1/cameras/{id}/api-keys`.
Fecha o ciclo de uso: uma câmera cadastrada agora recebe sua chave pela própria UI.
Cobertura ampliada: testes de integração no backend e no frontend
(Vitest); a conexão STOMP em si é verificada em runtime.

**Frontend — painel inicial (concluído):** a última tela da fase. A `home` deixou de
ser placeholder e virou um **painel de métricas** com quatro cartões: **alertas
pendentes** (NEW, com destaque de urgência quando > 0), **passagens** (últimas 24h /
última hora), **câmeras** (ativas / inativas) e **veículos monitorados** (ativos). Cada
cartão é um atalho para a respectiva tela, e o de alertas **incrementa ao vivo** quando
chega um alerta pelo STOMP. As métricas vêm de **endpoints de agregação por módulo**
(`GET /api/v1/{alerts,detections,cameras,watchlist}/summary`), carregados em paralelo;
as contagens de passagens usam **janelas móveis** (24h/1h) sobre `detected_at`. Com isso
a **Fase 6 está concluída**: **61** testes de integração no backend e **67** no frontend.

**Desempenho — Fase 5 (benchmark concluído; carga adiada):** conduzido um
**benchmark comparativo com/sem índice** sobre 10M passagens, medindo leitura,
escrita e disco em ambiente Docker de recursos fixos (ADR-027). Resultados: busca por
placa ~11.000× mais rápida; câmera+período com índice **composto**
`(camera_id, detected_at)` ~83×; BRIN (40 kB) ~60× em janela recente; escrita ~2,75×
mais lenta e ~602 MB de índices. O harness (não versionado) fica em `bench/`.
**Adiados** (fora do caminho crítico do MVP): testes de concorrência, carga com k6 e
simulador de câmeras.

## Momento atual

O backend expõe a **API REST completa do MVP** (auth, câmeras + API keys, passagens,
watchlist, alertas) e o **broadcast de alertas em tempo real** (STOMP
`/topic/alerts`), tudo coberto por **61 testes de integração**. O **frontend consome
todos esses recursos** — login, câmeras (com gestão de API keys), watchlist, passagens,
alertas (com tempo real) e o **painel inicial com métricas** — encerrando a Fase 6.

**Cobertura da API pelo frontend:**

| Recurso                                       | Backend | Tela |
|-----------------------------------------------|:-------:|:----:|
| Autenticação (login / refresh)                |   ✅    |  ✅  |
| Câmeras (CRUD)                                 |   ✅    |  ✅  |
| API keys por câmera (emitir / listar / revogar) | ✅    |  ✅  |
| Passagens (consulta por keyset + filtros)      |   ✅    |  ✅  |
| Watchlist (CRUD + reclassificação)             |   ✅    |  ✅  |
| Alertas (listar / filtrar + mudar status)      |   ✅    |  ✅  |
| Alertas em tempo real (`/topic/alerts`)        |   ✅    |  ✅  |
| Painel inicial (métricas via `/summary`)       |   ✅    |  ✅  |

> A ingestão de passagens (`POST /api/v1/detections`) é consumida pela **câmera**
> (via API key), não pela UI — por isso não tem tela.

**Fase 6 concluída.** Próximos passos opcionais: retomar itens adiados da Fase 5
(carga com k6, simulador de câmeras, testes de concorrência).
