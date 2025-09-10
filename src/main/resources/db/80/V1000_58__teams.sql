CREATE TABLE chusan_team
(
    id	BIGINT AUTO_INCREMENT NOT NULL,
    team_name	VARCHAR(20)         NOT NULL,
    last_month_points	BIGINT                 NOT NULL DEFAULT 0,
    owner	BIGINT	NOT NULL,
    CONSTRAINT pk_chusan_team PRIMARY KEY (id),
    CONSTRAINT fk_chusan_team_on_chusan_user_data FOREIGN KEY (owner) REFERENCES chusan_user_data (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE chusan_user_team_points
(
    id	BIGINT AUTO_INCREMENT NOT NULL,
    user_id	BIGINT	NOT NULL,
    team_id	BIGINT NOT NULL,
    monthly_points	BIGINT	NOT NULL	DEFAULT 0,
    month_date	DATETIME	NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chusan_user_team_points_on_chusan_user_data FOREIGN KEY (user_id) REFERENCES chusan_user_data (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_chusan_user_team_points_on_chusan_team FOREIGN KEY (team_id) REFERENCES chusan_team (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT unq_chusan_user_team_month UNIQUE (user_id, month_date, team_id)
);

CREATE TABLE chusan_user_team
(
    id	BIGINT AUTO_INCREMENT NOT NULL,
    user_id	BIGINT	NOT NULL,
    team	BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chusan_user_team_on_chusan_user_data FOREIGN KEY (user_id) REFERENCES chusan_user_data (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_chusan_user_team_on_chusan_team FOREIGN KEY (team) REFERENCES chusan_team (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT unq_chusan_user_team UNIQUE (user_id)
);

 CREATE TABLE chusan_user_team_invite (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  team BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_user_team_invite_user FOREIGN KEY (user_id) REFERENCES chusan_user_data (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_user_team_invite_team FOREIGN KEY (team) REFERENCES chusan_team (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT unq_user_team_invite UNIQUE (user_id, team)
);