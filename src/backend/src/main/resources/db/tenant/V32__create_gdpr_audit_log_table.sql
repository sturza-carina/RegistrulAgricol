CREATE TABLE gdpr_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    utilizator VARCHAR(255) NOT NULL,
    tip_actiune VARCHAR(50) NOT NULL,
    entitate_vizata VARCHAR(255) NOT NULL,
    id_persoana_vizata VARCHAR(1024),
    endpoint VARCHAR(512),
    tenant_id VARCHAR(255)
);
