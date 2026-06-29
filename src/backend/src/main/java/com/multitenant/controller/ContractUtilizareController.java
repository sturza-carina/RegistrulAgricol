package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.ContractUtilizareDTO;
import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.service.ContractUtilizareService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import com.multitenant.security.UserDetailsImpl;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/contracte")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class ContractUtilizareController {

    private final ContractUtilizareService contractUtilizareService;

    public ContractUtilizareController(ContractUtilizareService contractUtilizareService) {
        this.contractUtilizareService = contractUtilizareService;
    }

    @GetMapping
    public ResponseEntity<?> getAllContracts(@RequestParam(required = false) String uatCode, Pageable pageable) {
        return ResponseEntity.ok(contractUtilizareService.getAllContracts(uatCode, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContractById(@PathVariable Long id) {
        return ResponseEntity.ok(contractUtilizareService.getContractById(id));
    }

    @PostMapping
    public ResponseEntity<?> createContract(@RequestBody ContractUtilizareDTO contract) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(contractUtilizareService.createContract(contract, userDetails.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Eroare la crearea contractului: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContract(@PathVariable Long id, @RequestBody ContractUtilizareDTO contract) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(contractUtilizareService.updateContract(id, contract, userDetails.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la actualizarea contractului: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContract(@PathVariable Long id) {
        try {
            contractUtilizareService.deleteContract(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la ștergerea contractului: " + e.getMessage());
        }
    }
}

