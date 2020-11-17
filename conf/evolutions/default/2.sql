# --- !Ups
INSERT INTO `task` (`id`, `name`, `description`, `is_done`)
VALUES (0, 'init data', 'init data for test', FALSE);

INSERT INTO `user` (`id`, `name`, `password`)
VALUES (1, 'user1', '7a37b85c8918eac19a9089c0fa5a2ab4dce3f90528dcdeec108b23ddf3607b99');

INSERT INTO `user_task` (`user_id`, `task_id`)
VALUES (1, 0);

# --- !Downs
