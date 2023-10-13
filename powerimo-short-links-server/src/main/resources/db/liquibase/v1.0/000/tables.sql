create table link (
    code            varchar(16) not null,
    url             varchar(4096) not null,
    url_hash        varchar(64),
    get_count       int not null default 0,
    ttl             int,
    expired_at      timestamp,
    host            varchar(32),
    identity_value  text,
    created_at      timestamp,
    updated_at      timestamp
);

alter table link
    add constraint link_pk primary key (code);

create index link_idx01 on link (url_hash);
create index link_idx02 on link (expired_at);