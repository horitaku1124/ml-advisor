create table projects
(
    id int auto_increment
        primary key,
    name varchar(255) not null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null
);

create table table_result
(
    id int auto_increment
        primary key,
    label_id int null,
    vectors mediumtext null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null
);

create table train_data
(
    id bigint auto_increment
        primary key,
    project_id int not null,
    result_id int null,
    data mediumtext null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null
);

create table train_labels
(
    id int auto_increment
        primary key,
    project_id int not null,
    slug varchar(255) not null,
    result text null,
    created_at timestamp default current_timestamp() null,
    updated_at timestamp default current_timestamp() null
);

