CREATE TABLE IF NOT EXISTS {schema}.specialties (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_{schema}_specialties_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS {schema}.personal (
    id BIGSERIAL PRIMARY KEY,
    personal_account_id BIGINT NOT NULL REFERENCES public.accounts(id),
    specialty_id BIGINT REFERENCES {schema}.specialties(id),
    role_id BIGINT NOT NULL REFERENCES public.roles(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_{schema}_personal_account UNIQUE (personal_account_id)
);

CREATE TABLE IF NOT EXISTS {schema}.patients (
    id BIGSERIAL PRIMARY KEY,
    patient_account_id BIGINT NOT NULL REFERENCES public.accounts(id),
    role_id BIGINT NOT NULL REFERENCES public.roles(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_{schema}_patients_account UNIQUE (patient_account_id)
);

CREATE TABLE IF NOT EXISTS {schema}.schedules (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES {schema}.personal(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT
);

CREATE TABLE IF NOT EXISTS {schema}.appointments (
    id BIGSERIAL PRIMARY KEY,
    schedule_id BIGINT NOT NULL REFERENCES {schema}.schedules(id),
    patient_id BIGINT NOT NULL REFERENCES {schema}.patients(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS {schema}.doctor_patient (
    doctor_id BIGINT NOT NULL REFERENCES {schema}.personal(id),
    patient_id BIGINT NOT NULL REFERENCES {schema}.patients(id),
    PRIMARY KEY (doctor_id, patient_id)
);
