create table permissions (
    id bigserial primary key,
    name varchar(100) not null unique,
    description varchar(255)
);

create table roles (
    id bigserial primary key,
    name varchar(50) not null unique,
    description varchar(255)
);

create table role_permissions (
    role_id bigint not null references roles(id) on delete cascade,
    permission_id bigint not null references permissions(id) on delete cascade,
    primary key (role_id, permission_id)
);

create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(255),
    display_name varchar(160) not null,
    provider varchar(50) not null default 'local',
    provider_id varchar(255),
    enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table user_roles (
    user_id uuid not null references users(id) on delete cascade,
    role_id bigint not null references roles(id) on delete cascade,
    primary key (user_id, role_id)
);

create index idx_users_email on users(email);
create index idx_user_roles_user_id on user_roles(user_id);
create index idx_role_permissions_role_id on role_permissions(role_id);

insert into permissions (name, description) values
    ('auth:read', 'Read authenticated account data'),
    ('auth:write', 'Manage own authentication data'),
    ('users:read', 'Read user records'),
    ('users:write', 'Manage user records')
on conflict (name) do nothing;

insert into roles (name, description) values
    ('USER', 'Default authenticated user'),
    ('ADMIN', 'Administrative operator')
on conflict (name) do nothing;

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
cross join permissions p
where r.name = 'USER' and p.name in ('auth:read', 'auth:write')
on conflict do nothing;

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
cross join permissions p
where r.name = 'ADMIN'
on conflict do nothing;
