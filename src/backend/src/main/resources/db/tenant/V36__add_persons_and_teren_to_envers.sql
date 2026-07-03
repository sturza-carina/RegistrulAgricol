-- Table: persons_aud (Envers audit table for Persons / Physical Persons / Legal Entities)
CREATE TABLE IF NOT EXISTS persons_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    person_type VARCHAR(50),
    
    -- Address details
    county VARCHAR(255),
    localitate VARCHAR(255),
    street VARCHAR(255),
    street_number VARCHAR(50),
    building VARCHAR(50),
    staircase VARCHAR(50),
    floor INTEGER,
    apartment_number INTEGER,
    postal_code VARCHAR(50),
    
    -- Contact details
    phone_number VARCHAR(100),
    email VARCHAR(255),
    
    -- Agricultural register details
    notes TEXT,
    register_volume VARCHAR(255),
    register_position VARCHAR(255),
    
    -- Multi-tenancy
    tenant_id VARCHAR(255),
    
    -- PhysicalPerson attributes
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    cnp VARCHAR(13),
    date_of_birth DATE,
    is_head_of_household BOOLEAN,
    
    -- LegalEntity attributes
    company_name VARCHAR(255),
    cui VARCHAR(50),
    registration_number VARCHAR(100),
    legal_representative VARCHAR(255),
    
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

-- Table: terenuri_aud (Envers audit table for Terens)
CREATE TABLE IF NOT EXISTS terenuri_aud (
    id BIGINT NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    denumire VARCHAR(255),
    gospodarie_id BIGINT,
    tip_teren VARCHAR(100),
    stereo70_coordinates TEXT,
    polygon JSONB,
    
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo(rev)
);
