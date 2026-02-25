CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE branches (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(120) NOT NULL,
    city VARCHAR(120) NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE cars (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    brand VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    category VARCHAR(20) NOT NULL,
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    status VARCHAR(20) NOT NULL,
    base_daily_rate NUMERIC(10,2) NOT NULL
);

CREATE TABLE maintenance_records (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    car_id BIGINT NOT NULL REFERENCES cars(id),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    code VARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    car_id BIGINT NOT NULL REFERENCES cars(id),
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_mode VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    insurance_selected BOOLEAN NOT NULL DEFAULT FALSE,
    coupon_code VARCHAR(40),
    base_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    weekend_surcharge NUMERIC(10,2) NOT NULL DEFAULT 0,
    duration_discount NUMERIC(10,2) NOT NULL DEFAULT 0,
    dynamic_surcharge NUMERIC(10,2) NOT NULL DEFAULT 0,
    insurance_fee NUMERIC(10,2) NOT NULL DEFAULT 0,
    coupon_discount NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_price NUMERIC(10,2) NOT NULL DEFAULT 0,
    cancelled_at TIMESTAMPTZ,
    cancellation_fee NUMERIC(10,2) DEFAULT 0,
    refund_amount NUMERIC(10,2) DEFAULT 0
);

CREATE TABLE waitlist_entries (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    category VARCHAR(20) NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_reference VARCHAR(80) NOT NULL
);

CREATE TABLE booking_audits (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    booking_id BIGINT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    actor VARCHAR(120) NOT NULL,
    details VARCHAR(500) NOT NULL
);

CREATE INDEX idx_bookings_car_period ON bookings(car_id, start_time, end_time);
CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_waitlist_period ON waitlist_entries(branch_id, category, start_time, end_time);
CREATE INDEX idx_maintenance_period ON maintenance_records(car_id, start_time, end_time);
