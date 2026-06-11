package com.multitenant.controller;

import com.multitenant.model.core.Uat;
import com.multitenant.service.UatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/uats")
public class UatController {

    private final UatService uatService;

    public UatController(UatService uatService) {
        this.uatService = uatService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public Uat createUat(@RequestBody Uat uat) {
        return uatService.createUat(uat);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public List<Uat> getAllUats() {
        return uatService.getAllUats();
    }

    @GetMapping("/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public Uat getUat(@PathVariable String codSiruta) {
        return uatService.getUatByCodSiruta(codSiruta);
    }

    @PutMapping("/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public Uat updateUat(@PathVariable String codSiruta, @RequestBody Uat uat) {
        return uatService.updateUat(codSiruta, uat);
    }

    @DeleteMapping("/{codSiruta}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN')")
    public void deleteUat(@PathVariable String codSiruta) {
        uatService.deleteUat(codSiruta);
    }
}

