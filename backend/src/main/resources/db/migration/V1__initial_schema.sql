-- V1 — Initial schema for Sentinela ALPR
-- Conventions: snake_case, singular table names, timestamptz (UTC),
-- cross-module reference by id (no FK), enums as varchar + CHECK.

-- auth module
create table app_user (
  id            bigint generated always as identity primary key,
  login         varchar(60)  not null,
  password_hash varchar(100) not null,
  role          varchar(20)  not null,
  active        boolean not null default true,
  version       bigint not null default 0,
  created_at    timestamptz not null default now(),
  constraint uq_app_user_login unique (login),
  constraint ck_app_user_role check (role in ('OPERATOR','ADMIN'))
);

-- cameras module
create table camera (
  id         bigint generated always as identity primary key,
  name       varchar(120) not null,
  latitude   numeric(9,6),
  longitude  numeric(9,6),
  road       varchar(120),
  active     boolean not null default true,
  version    bigint not null default 0,
  created_at timestamptz not null default now()
);

-- watchlist module
create table watched_vehicle (
  id         bigint generated always as identity primary key,
  plate      varchar(7)  not null,
  reason     varchar(30) not null,
  active     boolean not null default true,
  version    bigint not null default 0,
  created_at timestamptz not null default now(),
  constraint uq_watched_vehicle_plate unique (plate),
  constraint ck_watched_vehicle_reason check (reason in ('ROBBERY','THEFT','WANTED','SUSPECT'))
);

-- detections module (append-only, high volume)
-- Composite PK (id, detected_at) to allow future RANGE partitioning.
create table detection (
  id          bigint generated always as identity,
  plate       varchar(7) not null,
  camera_id   bigint not null,
  detected_at timestamptz not null,
  created_at  timestamptz not null default now(),
  primary key (id, detected_at)
);
create index ix_detection_plate_time on detection (plate, detected_at desc);
create index ix_detection_camera     on detection (camera_id);
create index brin_detection_time     on detection using brin (detected_at);

-- alerts module
create table alert (
  id                 bigint generated always as identity primary key,
  plate              varchar(7)  not null,
  detection_id       bigint not null,
  watched_vehicle_id bigint not null,
  detected_at        timestamptz not null,
  status             varchar(20) not null default 'NEW',
  version            bigint not null default 0,
  created_at         timestamptz not null default now(),
  constraint uq_alert_detection unique (detection_id),
  constraint ck_alert_status check (status in ('NEW','SEEN'))
);
create index ix_alert_status_time on alert (status, detected_at desc);
