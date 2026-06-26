package com.multitenant.controller;

import com.multitenant.dto.ContractUtilizareDTO;
import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.service.ContractUtilizareService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracte")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class ContractUtilizareController {

    private final ContractUtilizareService contractUtilizareService;

    public ContractUtilizareController(ContractUtilizareService contractUtilizareService) {
        this.contractUtilizareService = contractUtilizareService;
    }

    private boolean isPublicContext() {
        return "public".equals(com.multitenant.config.tenant.TenantContext.getCurrentTenant());
    }

    @GetMapping
    public ResponseEntity<?> getAllContracts() {
        if (isPublicContext()) {
            return ResponseEntity.badRequest().body("Trebuie să selectați un context de UAT/Tenant.");
        }
        return ResponseEntity.ok(contractUtilizareService.getAllContracts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContractById(@PathVariable Long id) {
        if (isPublicContext()) {
            return ResponseEntity.badRequest().body("Trebuie să selectați un context de UAT/Tenant.");
        }
        return ResponseEntity.ok(contractUtilizareService.getContractById(id));
    }

    @PostMapping
    public ResponseEntity<?> createContract(@RequestBody ContractUtilizareDTO contract) {
        if (isPublicContext()) {
            return ResponseEntity.badRequest().body("Nu se poate crea un contract în afara unui context de UAT/Tenant.");
        }
        try {
            return ResponseEntity.ok(contractUtilizareService.createContract(contract));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Eroare la crearea contractului: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContract(@PathVariable Long id, @RequestBody ContractUtilizareDTO contract) {
        if (isPublicContext()) {
            return ResponseEntity.badRequest().body("Nu se poate edita un contract în afara unui context de UAT/Tenant.");
        }
        try {
            return ResponseEntity.ok(contractUtilizareService.updateContract(id, contract));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la actualizarea contractului: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContract(@PathVariable Long id) {
        if (isPublicContext()) {
            return ResponseEntity.badRequest().body("Nu se poate șterge un contract în afara unui context de UAT/Tenant.");
        }
        try {
            contractUtilizareService.deleteContract(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la ștergerea contractului: " + e.getMessage());
        }
    }
}
