package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.NotificareSuccesiuneDTO;
import com.multitenant.service.NotificareSuccesiuneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/succesiuni")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class NotificareSuccesiuneController {

    private final NotificareSuccesiuneService notificareSuccesiuneService;

    public NotificareSuccesiuneController(NotificareSuccesiuneService notificareSuccesiuneService) {
        this.notificareSuccesiuneService = notificareSuccesiuneService;
    }

    @PostMapping
    public ResponseEntity<NotificareSuccesiuneDTO> createNotificare(@RequestBody NotificareSuccesiuneDTO dto) {
        NotificareSuccesiuneDTO created = notificareSuccesiuneService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NotificareSuccesiuneDTO>> getAllNotificari() {
        List<NotificareSuccesiuneDTO> list = notificareSuccesiuneService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/defunct/{cnp}")
    public ResponseEntity<List<NotificareSuccesiuneDTO>> getNotificariByDefunctCnp(@PathVariable String cnp) {
        List<NotificareSuccesiuneDTO> list = notificareSuccesiuneService.findByDefunctCnp(cnp);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/stadiu")
    public ResponseEntity<NotificareSuccesiuneDTO> updateStadiu(@PathVariable Long id, @RequestParam String stadiu) {
        NotificareSuccesiuneDTO updated = notificareSuccesiuneService.updateStadiu(id, stadiu);
        return ResponseEntity.ok(updated);
    }
}
