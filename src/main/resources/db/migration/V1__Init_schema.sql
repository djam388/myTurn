CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT UNIQUE,
    username VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone_number VARCHAR(255) UNIQUE,
    registration_state VARCHAR(50),
    created_at TIMESTAMP,
    last_active TIMESTAMP
);

CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    doctor_id BIGINT REFERENCES doctors(id),
    appointment_time TIMESTAMP,
    duration INTEGER,
    status VARCHAR(50),
    last_rescheduled_at TIMESTAMP,
    reschedule_count INTEGER DEFAULT 0
);

CREATE TABLE clinic_employees (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE doctor_working_hours (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT REFERENCES doctors(id),
    day_of_week VARCHAR(20),
    start_time TIMESTAMP,
    end_time TIMESTAMP
);