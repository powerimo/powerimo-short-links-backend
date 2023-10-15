create table link_hit (
    id              bigserial not null,
    code            varchar(16) not null,
    host            varchar(32),
    agent_string    varchar(1024),
    extracted_browser           varchar(64),
    extracted_browser_version   varchar(64),
    extracted_os_name           varchar(64),
    extracted_os_version        varchar(64),
    created_at      timestamp,
    updated_at      timestamp
);

alter table link_hit
    add constraint link_hit_pk primary key (id);

create index link_hit_idx01 on link_hit (code);
create index link_hit_idx02 on link_hit (created_at desc);