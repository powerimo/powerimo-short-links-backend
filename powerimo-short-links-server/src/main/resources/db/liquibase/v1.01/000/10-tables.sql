alter table link
    add column hit_limit bigint;

alter table link
    rename column get_count to hit_count;