create table camera_api_key (
  id         bigint generated always as identity primary key,
  camera_id  bigint not null references camera (id),
  key_hash   varchar(64) not null,
  key_prefix varchar(20) not null,
  active     boolean not null default true,
  created_at timestamptz not null default now(),
  revoked_at timestamptz,
  constraint uq_camera_api_key_hash unique (key_hash)
);

create index ix_camera_api_key_camera on camera_api_key (camera_id);
