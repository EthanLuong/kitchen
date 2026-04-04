CREATE TABLE item_defaults (
                               id BIGSERIAL PRIMARY KEY,
                               userid UUID NOT NULL REFERENCES users(userid) ON DELETE CASCADE,
                               name VARCHAR(255) NOT NULL,
                               food_type VARCHAR(50),
                               unit VARCHAR(20),
                               location VARCHAR(50),
                               expiration_days INTEGER,
                               UNIQUE(userid, name)
);

CREATE INDEX idx_item_defaults_userid ON item_defaults(userid);

