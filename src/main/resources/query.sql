CREATE TABLE USERS
(

    USER_ID       BIGSERIAL PRIMARY KEY,
    FIRST_NAME    VARCHAR(100) NOT NULL,
    LAST_NAME     VARCHAR(100) NULL,
    GENDER        VARCHAR(20)  NOT NULL,
    EMAIL         VARCHAR(200) NOT NULL UNIQUE,
    USERNAME      VARCHAR(10)  NOT NULL UNIQUE,
    PHONE         VARCHAR(10)  NULL,
    PHONE_CODE    VARCHAR(10)  NULL,
    PASSWORD      VARCHAR(500) NOT NULL,
    PROFILE_PHOTO BYTEA        null,
    CREATED_DATE  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE POSTS
(
    POST_ID   BIGSERIAL PRIMARY KEY,
    BODY      TEXT  NULL,
    IMAGE     BYTEA NULL,
    POSTED_BY INT   NOT NULL,
    POST_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (POSTED_BY) REFERENCES USERS (USER_ID)
);


CREATE TABLE FRIENDS
(
    ID               BIGSERIAL PRIMARY KEY,
    FRIEND_OF        INT NOT NULL,
    FRIEND_USER_ID   INT NOT NULL unique,
    FRIEND_SHIP_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE FRIEND_REQUEST
(
    REQUEST_ID    BIGSERIAL PRIMARY KEY,
    REQUEST_TO    INT NOT NULL,
    REQUEST_BY    INT NOT NULL,
    REQUEST_NOTES VARCHAR(200),
    REQUEST_DATE  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE COMMENT
(
    COMMENT_ID   BIGSERIAL PRIMARY KEY,
    POST_ID      INT  NOT NULL,
    COMMENT_TEXT TEXT NOT NULL,
    COMMENT_BY   INT  NOT NULL,
    COMMENT_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (POST_ID) references POSTS (post_id),
    FOREIGN KEY (COMMENT_BY) references USERS (USER_ID)
);

CREATE TABLE LIKE_POST
(
    LIKE_ID   BIGSERIAL PRIMARY KEY,
    POST_ID   INT NOT NULL,
    LIKE_BY   INT NOT NULL,
    LIKE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE BOOKMARK
(
    BOOKMARK_ID SERIAL PRIMARY KEY,
    USER_ID INT NOT NULL ,
    POST_ID INT NOT NULL ,
    BOOKMARKED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);




