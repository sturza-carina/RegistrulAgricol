-- Table: persons
-- Using SINGLE_TABLE inheritance strategy for Person hierarchy (PhysicalPerson / LegalEntity)
CREATE TABLE persons (
    id SERIAL PRIMARY KEY,
    person_type VARCHAR(50) NOT NULL, -- PHYSICAL_PERSON or LEGAL_ENTITY
    
    -- Address Details (Embedded)
    county VARCHAR(255),
    localitate VARCHAR(255),
    street VARCHAR(255),
    street_number VARCHAR(50),
    building VARCHAR(50),
    staircase VARCHAR(50),
    floor INTEGER,
    apartment_number INTEGER,
    postal_code VARCHAR(50),
    
    -- Contact Information
    phone_number VARCHAR(100),
    email VARCHAR(255),
    
    -- Agriculture Register details
    notes TEXT,
    
    -- Multi-Tenancy flag
    tenant_id VARCHAR(255),
    
    -- PhysicalPerson specific attributes
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    cnp VARCHAR(13) UNIQUE,
    date_of_birth DATE,
    is_head_of_household BOOLEAN,
    
    -- LegalEntity specific attributes
    company_name VARCHAR(255),
    cui VARCHAR(50) UNIQUE,
    registration_number VARCHAR(100),
    legal_representative VARCHAR(255)
);

-- Table: identity_documents (Child of PhysicalPerson)
CREATE TABLE identity_documents (
    id SERIAL PRIMARY KEY,
    document_type VARCHAR(50) NOT NULL, -- IDENTITY_CARD, BIRTH_CERTIFICATE, PASSPORT, DRIVER_LICENSE
    series VARCHAR(50),
    document_number VARCHAR(50) NOT NULL,
    issued_by VARCHAR(255),
    issue_date DATE,
    expiration_date DATE,
    tenant_id VARCHAR(255),
    person_id INTEGER REFERENCES persons(id) ON DELETE CASCADE
);

-- Table: person_relations (Mapping relations between two persons)
CREATE TABLE person_relations (
    id SERIAL PRIMARY KEY,
    person_id INTEGER NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    related_person_id INTEGER NOT NULL REFERENCES persons(id) ON DELETE CASCADE,
    relation_type VARCHAR(50) NOT NULL
);

