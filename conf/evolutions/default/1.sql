# -- Table definitions

# --- !Ups
CREATE TABLE enquete (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    gender varchar(6) NOT NULL,
    message varchar(255) NOT NULL,
    created_at timestamp default CURRENT_TIMESTAMP() NOT NULL
);

CREATE TABLE task (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    description varchar(255),
    is_done boolean default FALSE NOT NULL,
    created_at timestamp default CURRENT_TIMESTAMP() NOT NULL,
    updated_at timestamp default CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP() NOT NULL
);

CREATE TABLE user (
    id int PRIMARY KEY AUTO_INCREMENT,
    name varchar(32) NOT NULL,
    password varchar(86) NOT NULL,
    created_at timestamp default CURRENT_TIMESTAMP() NOT NULL,
    updated_at timestamp default CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP() NOT NULL
);

CREATE TABLE user_task (
    user_id int,
    task_id int,
    CONSTRAINT fk_user_id
        FOREIGN KEY (user_id)
        REFERENCES user(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_id
        FOREIGN KEY (task_id)
        REFERENCES task(id)
        ON DELETE CASCADE,
    PRIMARY KEY(user_id, task_id)
);


# --- !Downs
DROP TABLE enquete;

DROP TABLE task;
DROP TABLE user;
DROP TABLE user_task;