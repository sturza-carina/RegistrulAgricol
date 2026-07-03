package com.multitenant.repository;

import com.multitenant.model.audit.GdprAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GdprAuditLogRepository extends JpaRepository<GdprAuditLog, Long> {
}
