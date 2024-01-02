create table if not exists video
(
    id          serial primary key,
    name        text not null,
    description text null,
    username    text not null
);

create table if not exists user_account
(
    id          serial primary key,
    username    text not null,
    password    text not null,
    authorities text[] not null
);