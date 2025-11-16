CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    plate_number VARCHAR(50) NOT NULL,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP,
    total_price INTEGER,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tickets_plate_status
    ON tickets (plate_number, status);

