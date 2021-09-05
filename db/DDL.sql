create table classification_result
(
    id int auto_increment
        primary key,
    project_id int not null,
    slug varchar(255) not null,
    result text null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null
);

create table projects
(
    id int auto_increment
        primary key,
    name varchar(255) not null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null
);

create table training_data
(
    id bigint auto_increment
        primary key,
    project_id int not null,
    data mediumtext null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null
);

