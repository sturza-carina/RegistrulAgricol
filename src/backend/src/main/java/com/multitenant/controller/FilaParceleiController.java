package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.FilaParceleiDTO;
import com.multitenant.service.FilaParceleiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/parcele/{parcelaId}/fila")
@RequiredArgsConstructor
@TenantRequired
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
public class FilaParceleiController {

    private final FilaParceleiService filaParceleiService;

    @GetMapping
    public ResponseEntity<List<FilaParceleiDTO>> getFilaParcelei(
            @PathVariable Long parcelaId,
            @RequestParam(value = "anAgricol", required = false) Integer anAgricol) {
        return ResponseEntity.ok(filaParceleiService.getFilaParcelei(parcelaId, anAgricol));
    }
}
