package com.multitenant.service;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.core.Tenant;
import com.multitenant.repository.TenantRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class ContractExpirationScheduler {

    private static final ZoneId BUCHAREST_ZONE = ZoneId.of("Europe/Bucharest");

    private final TenantRepository tenantRepository;
    private final ContractUtilizareService contractUtilizareService;

    public ContractExpirationScheduler(TenantRepository tenantRepository,
                                       ContractUtilizareService contractUtilizareService) {
        this.tenantRepository = tenantRepository;
        this.contractUtilizareService = contractUtilizareService;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Bucharest")
    public void expireContractsAtMidnight() {
        List<Tenant> tenants = tenantRepository.findAll();
        LocalDate today = LocalDate.now(BUCHAREST_ZONE);

        for (Tenant tenant : tenants) {
            String originalTenant = TenantContext.getCurrentTenant();
            try {
                TenantContext.setCurrentTenant(tenant.getId());
                int expiredCount = contractUtilizareService.expireActiveContracts(today);
                if (expiredCount > 0) {
                    System.out.println("[ContractExpirationScheduler] " + expiredCount
                            + " contracts expired for tenant " + tenant.getId());
                }
            } finally {
                if (originalTenant == null) {
                    TenantContext.clear();
                } else {
                    TenantContext.setCurrentTenant(originalTenant);
                }
            }
        }
    }
}
