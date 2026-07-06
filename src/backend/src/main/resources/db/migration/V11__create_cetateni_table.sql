CREATE TABLE public.cetateni (
    id BIGSERIAL PRIMARY KEY,
    nume VARCHAR(255) NOT NULL,
    prenume VARCHAR(255) NOT NULL,
    cnp VARCHAR(13) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    parola VARCHAR(255) NOT NULL,
    telefon VARCHAR(50) NOT NULL,
    judet VARCHAR(255) NOT NULL,
    localitate VARCHAR(255) NOT NULL,
    strada VARCHAR(255) NOT NULL,
    numar VARCHAR(50) NOT NULL,
    bloc VARCHAR(50),
    scara VARCHAR(50),
    etaj VARCHAR(50),
    apartament VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
