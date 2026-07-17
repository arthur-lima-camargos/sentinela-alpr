-- V1 — Schema inicial do Sentinela ALPR
-- Convenções: snake_case, tabelas no singular, timestamptz (UTC),
-- referência cross-module por id (sem FK), enums como varchar + CHECK.

-- Módulo auth
create table usuario (
  id         bigint generated always as identity primary key,
  login      varchar(60)  not null,
  senha_hash varchar(100) not null,
  perfil     varchar(20)  not null,
  ativo      boolean not null default true,
  version    bigint not null default 0,
  criado_em  timestamptz not null default now(),
  constraint uq_usuario_login unique (login),
  constraint ck_usuario_perfil check (perfil in ('OPERADOR','ADMIN'))
);

-- Módulo cameras
create table camera (
  id        bigint generated always as identity primary key,
  nome      varchar(120) not null,
  latitude  numeric(9,6),
  longitude numeric(9,6),
  via       varchar(120),
  ativa     boolean not null default true,
  version   bigint not null default 0,
  criado_em timestamptz not null default now()
);

-- Módulo watchlist
create table veiculo_monitorado (
  id        bigint generated always as identity primary key,
  placa     varchar(7)  not null,
  motivo    varchar(30) not null,
  ativo     boolean not null default true,
  version   bigint not null default 0,
  criado_em timestamptz not null default now(),
  constraint uq_veiculo_monitorado_placa unique (placa),
  constraint ck_veiculo_motivo check (motivo in ('ROUBO','FURTO','PROCURADO','SUSPEITO'))
);

-- Módulo passagens (append-only, alto volume)
-- PK composta (id, data_hora) para permitir particionamento futuro por RANGE.
create table passagem (
  id        bigint generated always as identity,
  placa     varchar(7) not null,
  camera_id bigint not null,
  data_hora timestamptz not null,
  criado_em timestamptz not null default now(),
  primary key (id, data_hora)
);
create index ix_passagem_placa_data on passagem (placa, data_hora desc);
create index ix_passagem_camera     on passagem (camera_id);
create index brin_passagem_data     on passagem using brin (data_hora);

-- Módulo alertas
create table alerta (
  id                    bigint generated always as identity primary key,
  placa                 varchar(7)  not null,
  passagem_id           bigint not null,
  veiculo_monitorado_id bigint not null,
  data_hora             timestamptz not null,
  status                varchar(20) not null default 'NOVO',
  version               bigint not null default 0,
  criado_em             timestamptz not null default now(),
  constraint uq_alerta_passagem unique (passagem_id),
  constraint ck_alerta_status check (status in ('NOVO','VISTO'))
);
create index ix_alerta_status_data on alerta (status, data_hora desc);
