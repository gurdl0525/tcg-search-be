alter table app_users
    add column auth_provider text not null default 'local',
    add column provider_subject text,
    add column role text not null default 'USER',
    add column enabled boolean not null default true;

update app_users
set provider_subject = email
where provider_subject is null;

alter table app_users
    alter column provider_subject set not null,
    add constraint app_users_role_supported check (role in ('USER', 'ADMIN')),
    add constraint app_users_auth_provider_not_blank check (length(trim(auth_provider)) > 0),
    add constraint app_users_provider_subject_not_blank check (length(trim(provider_subject)) > 0);

create unique index app_users_auth_provider_subject_unique
    on app_users (auth_provider, provider_subject);

create table user_refresh_tokens (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references app_users(id) on delete cascade,
    device_id text not null,
    token_hash text not null unique,
    token_family_id uuid not null,
    expires_at timestamptz not null,
    last_used_at timestamptz,
    revoked_at timestamptz,
    rotated_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint user_refresh_tokens_device_id_not_blank check (length(trim(device_id)) > 0),
    constraint user_refresh_tokens_token_hash_not_blank check (length(trim(token_hash)) > 0),
    constraint user_refresh_tokens_expiry_after_created check (expires_at > created_at),
    constraint user_refresh_tokens_updated_after_created check (updated_at >= created_at)
);

create index user_refresh_tokens_user_id_idx
    on user_refresh_tokens (user_id);

create index user_refresh_tokens_token_family_id_idx
    on user_refresh_tokens (token_family_id);

create index user_refresh_tokens_active_user_id_idx
    on user_refresh_tokens (user_id)
    where revoked_at is null;
