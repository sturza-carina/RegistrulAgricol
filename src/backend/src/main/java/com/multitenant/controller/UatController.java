package com.multitenant.controller;

import com.multitenant.model.Uat;
import com.multitenant.service.UatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uats")
public class UatController {

    private final UatService uatService;

    public UatController(UatService uatService) {
        this.uatService = uatService;
    }

    @PostMapping
    public Uat createUat(@RequestBody Uat uat) {
        return uatService.createUat(uat);
    }

    @GetMapping
    public List<Uat> getAllUats() {
        return uatService.getAllUats();
    }

    @GetMapping("/{codSiruta}")
    public Uat getUat(@PathVariable String codSiruta) {
        return uatService.getUatByCodSiruta(codSiruta);
    }

    @PutMapping("/{codSiruta}")
    public Uat updateUat(@PathVariable String codSiruta, @RequestBody Uat uat) {
        return uatService.updateUat(codSiruta, uat);
    }

    @DeleteMapping("/{codSiruta}")
    public void deleteUat(@PathVariable String codSiruta) {
        uatService.deleteUat(codSiruta);
    }
}
