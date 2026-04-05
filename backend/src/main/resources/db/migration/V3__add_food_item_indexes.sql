-- Composite indexes for the two filtered list queries.
-- Partial index (WHERE deleted_at IS NULL) keeps the index small by excluding
-- soft-deleted rows, which are never included in active-item queries.

CREATE INDEX idx_food_items_userid_food_type
    ON food_items(userid, food_type)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_food_items_userid_location
    ON food_items(userid, location)
    WHERE deleted_at IS NULL;
