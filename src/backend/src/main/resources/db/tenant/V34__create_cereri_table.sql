CREATE TABLE cereri (
    id BIGSERIAL PRIMARY KEY,
    nume VARCHAR(255) NOT NULL,
    domiciliu TEXT NOT NULL,
    telefon VARCHAR(20),
    email VARCHAR(255),
    numar_carte_funciara VARCHAR(50),
    numar_cadastral VARCHAR(50),
    cod_cerere VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    user_id BIGINT,
    uat_id BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
