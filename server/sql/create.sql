CREATE TABLE board(
    id int NOT NULL,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY(id)
);

CREATE TABLE user(
    id int NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE post(
    id int NOT NULL,
    board_id int NOT NULL,
    poster_id int NOT NULL,
    title VARCHAR(50) NOT NULL,
    body VARCHAR(255),
    link VARCHAR(255),
    flag CHAR(1),
    PRIMARY KEY (id),
    FOREIGN KEY (board_id) REFERENCES board(id),
    FOREIGN KEY (poster_id) REFERENCES user(id)
);
CREATE INDEX idx_board_id on post (board_id);
CREATE INDEX idx_poster_id on post (poster_id);

CREATE TABLE comment(
    id int NOT NULL,
    post_parent_id int NOT NULL,
    comment_parent_id int,
    flag CHAR(1),
    PRIMARY KEY (id),
    FOREIGN KEY (post_parent_id) REFERENCES post(id),
    FOREIGN KEY (comment_parent_id) REFERENCES comment(id)
);
CREATE INDEX idx_post_parent_id ON comment (post_parent_id);
CREATE INDEX idx_comment_parent_id ON comment (comment_parent_id);


CREATE INDEX idx_username on user (username);
CREATE INDEX idx_email on user (email);

CREATE TABLE message(
    id int NOT NULL,
    sender_id int NOT NULL,
    receiver_id int NOT NULL,
    message VARCHAR(1000),
    PRIMARY KEY (id),
    FOREIGN KEY (sender_id) REFERENCES user(id),
    FOREIGN KEY (receiver_id) REFERENCES user(id)
);
CREATE INDEX idx_sender_id on message (sender_id);
CREATE INDEX idx_receiver_id on message (receiver_id);

CREATE TABLE subscription(
    id int NOT NULL,
    user_id int NOT NULL,
    board_id int NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (board_id) REFERENCES board(id)
);
CREATE INDEX idx_user_id on subscription (user_id);
CREATE INDEX idx_board_id on subscription (board_id);



