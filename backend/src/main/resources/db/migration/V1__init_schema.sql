-- V1: Initial schema — users and food_items tables

CREATE TABLE users (
    userid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE food_items (
    id BIGSERIAL PRIMARY KEY,
    userid UUID NOT NULL REFERENCES users(userid),
    name VARCHAR(255) NOT NULL,
    food_type VARCHAR(50) NOT NULL,
    quantity DOUBLE PRECISION,
    unit VARCHAR(20),
    location VARCHAR(50) NOT NULL,
    expiration_date DATE,
    purchase_date DATE,
    opened_at DATE,
    notes TEXT,
    consumed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_food_items_userid ON food_items(userid);
CREATE INDEX idx_food_items_expiration ON food_items(expiration_date);
