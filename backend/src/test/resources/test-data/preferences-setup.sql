TRUNCATE TABLE user_types, user_locations, refresh_tokens, food_items, users CASCADE;

INSERT INTO users (userid, username, password, enabled)
VALUES ('00000000-0000-0000-0000-000000000001', 'testuser', 'password', true),
       ('00000000-0000-0000-0000-000000000002', 'otheruser', 'password', true);

INSERT INTO user_types (id, userid, name)
VALUES (20, '00000000-0000-0000-0000-000000000001', 'DAIRY'),
       (21, '00000000-0000-0000-0000-000000000002', 'MEAT');

INSERT INTO user_locations (id, userid, name)
VALUES (20, '00000000-0000-0000-0000-000000000001', 'FRIDGE'),
       (21, '00000000-0000-0000-0000-000000000002', 'FREEZER');