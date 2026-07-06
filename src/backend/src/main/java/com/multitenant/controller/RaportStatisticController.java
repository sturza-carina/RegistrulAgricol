package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.StatisticiRaportCompletDto;
import com.multitenant.service.RaportStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistici")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class RaportStatisticController {

    private final RaportStatisticService raportStatisticService;

    @Autowired
    public RaportStatisticController(RaportStatisticService raportStatisticService) {
        this.raportStatisticService = raportStatisticService;
    }

    @GetMapping("/complet")
    public ResponseEntity<StatisticiRaportCompletDto> getComplet(
            @RequestParam(required = false, defaultValue = "2026") Integer an,
            @RequestParam(required = false) String uatCode) {
        StatisticiRaportCompletDto complet = raportStatisticService.getComplet(an, uatCode);
        return ResponseEntity.ok(complet);
    }

    @GetMapping("/export/vegetal")
    public ResponseEntity<byte[]> exportVegetal(
            @RequestParam(required = false, defaultValue = "2026") Integer an,
            @RequestParam(required = false) String uatCode) {
        byte[] xlsxBytes = raportStatisticService.exportVegetalXlsx(an, uatCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "centralizator_vegetal_" + an + ".xlsx");

        return new ResponseEntity<>(xlsxBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/export/zootehnic")
    public ResponseEntity<byte[]> exportZootehnic(
            @RequestParam(required = false) String uatCode) {
        byte[] xlsxBytes = raportStatisticService.exportZootehnicXlsx(uatCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "centralizator_zootehnic.xlsx");

        return new ResponseEntity<>(xlsxBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/export/utilaje")
    public ResponseEntity<byte[]> exportUtilaje(
            @RequestParam(required = false) String uatCode) {
        byte[] xlsxBytes = raportStatisticService.exportUtilajeXlsx(uatCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "centralizator_utilaje.xlsx");

        return new ResponseEntity<>(xlsxBytes, headers, HttpStatus.OK);
    }
}
