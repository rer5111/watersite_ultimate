CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START WITH 100 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS account (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    uuid UUID,
    join_date DATE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_id BIGINT UNIQUE REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL REFERENCES users(id),
    roles VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS season (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    image VARCHAR(255),
    start_date DATE,
    end_date DATE
);

CREATE TABLE IF NOT EXISTS season_extra (
    season_id BIGINT NOT NULL REFERENCES season(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (season_id, user_id)
);

INSERT INTO account (id, username, uuid, join_date)
SELECT v.id, v.username, CAST(v.uuid AS UUID), v.join_date
FROM (VALUES
    (1, 'LeadCone_TNF', 'ccb40ba9-e0af-4939-9c80-3ce15fb417ed', CURRENT_DATE),
    (2, 'rer_5111', 'ce03a881-7be7-4cbe-940c-25b254a7a2a9', CURRENT_DATE),
    (3, 'Kotyarendj', '8a9861cd-de95-4de9-ba5d-15e5c533370b', CURRENT_DATE),
    (4, 'Tabller', 'bcab9602-1050-4aad-af23-9b4f811d8e80', CURRENT_DATE),
    (5, 'Cheshirius', NULL, CURRENT_DATE)
) AS v(id, username, uuid, join_date)
WHERE NOT EXISTS (SELECT 1 FROM account a WHERE a.id = v.id);

INSERT INTO users (id, name, password, account_id)
SELECT v.id, v.name, v.password, v.account_id
FROM (VALUES
    (1, 'LeadCone_TNF', '${adminPasswordHash}', 1),
    (2, 'rer_5111', '${adminPasswordHash}', 2),
    (3, 'Kotyarendj', '${adminPasswordHash}', 3),
    (4, 'Tabller', '${adminPasswordHash}', 4),
    (5, 'Cheshirius', '${adminPasswordHash}', 5)
) AS v(id, name, password, account_id)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.id = v.id);

INSERT INTO user_role (user_id, roles)
SELECT u.id, 'ADMIN'
FROM users u
WHERE u.id BETWEEN 1 AND 5
  AND NOT EXISTS (
      SELECT 1 FROM user_role ur WHERE ur.user_id = u.id AND ur.roles = 'ADMIN'
  );
