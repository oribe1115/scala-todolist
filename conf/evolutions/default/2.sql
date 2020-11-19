# --- !Ups
INSERT INTO `task` (`id`, `name`, `description`, `is_done`)
VALUES
(0, 'init data', 'init data for user1', FALSE),
(3, 'init data', 'init data for user2', FALSE),
(4, 'init finish data', 'init finish data for user1', TRUE);

INSERT INTO `user` (`id`, `name`, `password`)
VALUES
(1, 'user1', '7a37b85c8918eac19a9089c0fa5a2ab4dce3f90528dcdeec108b23ddf3607b99'),
(2, 'user2', '7a37b85c8918eac19a9089c0fa5a2ab4dce3f90528dcdeec108b23ddf3607b99');

INSERT INTO `user_task` (`user_id`, `task_id`)
VALUES
(1, 0),
(2, 3),
(1, 4);

# --- !Downs
