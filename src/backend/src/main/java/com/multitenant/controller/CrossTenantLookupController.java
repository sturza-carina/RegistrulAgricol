package com.multitenant.controller;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.dto.GospodarieDTO;
import com.multitenant.dto.PersoanaDTO;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.PersoanaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transfers/destinations")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class CrossTenantLookupController {

    private final GospodarieRepository gospodarieRepository;
    private final PersoanaRepository persoanaRepository;
    private final ModelMapper modelMapper;

    public CrossTenantLookupController(GospodarieRepository gospodarieRepository,
                                       PersoanaRepository persoanaRepository,
                                       ModelMapper modelMapper) {
        this.gospodarieRepository = gospodarieRepository;
        this.persoanaRepository = persoanaRepository;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/gospodarii")
    public ResponseEntity<List<GospodarieDTO>> getGospodariiForTransfer(
            @RequestParam String targetTenantId,
            @RequestParam(required = false) String uatCode) {

        String originalTenantId = TenantContext.getCurrentTenant();
        if (targetTenantId == null || targetTenantId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target Tenant ID must not be empty");
        }

        try {
            TenantContext.setCurrentTenant(targetTenantId);
            List<Gospodarie> households;
            if (uatCode != null && !uatCode.isBlank()) {
                households = gospodarieRepository.findByUat_CodSirutaOrderByIdDesc(uatCode);
            } else {
                households = gospodarieRepository.findAll();
            }
            List<GospodarieDTO> dtos = households.stream()
                    .map(h -> modelMapper.map(h, GospodarieDTO.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } finally {
            TenantContext.setCurrentTenant(originalTenantId);
        }
    }

    @GetMapping("/persons")
    public ResponseEntity<List<PersoanaDTO>> getPersonsForTransfer(
            @RequestParam String targetTenantId) {

        String originalTenantId = TenantContext.getCurrentTenant();
        if (targetTenantId == null || targetTenantId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target Tenant ID must not be empty");
        }

        try {
            TenantContext.setCurrentTenant(targetTenantId);
            List<Persoana> persons = persoanaRepository.findAll();
            List<PersoanaDTO> dtos = persons.stream()
                    .map(p -> modelMapper.map(p, PersoanaDTO.class))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } finally {
            TenantContext.setCurrentTenant(originalTenantId);
        }
    }
}
