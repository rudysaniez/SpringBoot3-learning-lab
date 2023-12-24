create table if not exists video
(
    id          serial primary key,
    name        text not null,
    description text null

);