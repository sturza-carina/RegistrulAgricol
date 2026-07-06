package com.multitenant.service;

import com.multitenant.model.audit.GdprAuditLog;
import com.multitenant.repository.GdprAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GdprAuditService {

    private static final Logger logger = LoggerFactory.getLogger(GdprAuditService.class);
    private final GdprAuditLogRepository gdprAuditLogRepository;

    public GdprAuditService(GdprAuditLogRepository gdprAuditLogRepository) {
        this.gdprAuditLogRepository = gdprAuditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(GdprAuditLog log) {
        try {
            gdprAuditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to save GDPR Audit Log for user: {}, action: {}, entity: {}", 
                    log.getUtilizator(), log.getTipActiune(), log.getEntitateVizata(), e);
        }
    }
}
