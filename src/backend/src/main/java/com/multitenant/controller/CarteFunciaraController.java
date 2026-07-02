package com.multitenant.controller;

import com.multitenant.annotation.TenantRequired;
import com.multitenant.dto.CarteFunciaraDTO;
import com.multitenant.model.registru.CarteFunciara;
import com.multitenant.repository.CarteFunciaraRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * Controller REST pentru CarteFunciara.
 *
 * Creare si actualizare automata sunt gestionate de CarteFunciaraEventListener.
 * Acest controller expune doar:
 *  - GET: citirea CF-ului asociat unui teren (pentru afisare in UI)
 *  - PUT: completarea manuala a numarCf si numarTopografic de catre operator
 */
@RestController
@RequestMapping("/api/carti-funciare")
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
@TenantRequired
public class CarteFunciaraController {

    private final CarteFunciaraRepository carteFunciaraRepository;
    private final com.multitenant.service.PdfExportService pdfExportService;

    public CarteFunciaraController(CarteFunciaraRepository carteFunciaraRepository,
                                   com.multitenant.service.PdfExportService pdfExportService) {
        this.carteFunciaraRepository = carteFunciaraRepository;
        this.pdfExportService = pdfExportService;
    }

    /**
     * Returneaza CarteFunciara asociata unui Teren.
     * Folosit de frontend pentru a afisa datele CF in fisa terenului.
     *
     * @param terenId ID-ul terenului
     * @return 200 cu CarteFunciara, sau 404 daca nu exista inca
     */
    @GetMapping("/teren/{terenId}")
    public ResponseEntity<?> getByTerenId(@PathVariable Long terenId) {
        return carteFunciaraRepository.findByTerenId(terenId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualizare manuala a numarCf si/sau numarTopografic de catre operator.
     * Celelalte campuri (suprafata, teren_id) sunt gestionate automat si nu pot fi
     * modificate prin acest endpoint.
     *
     * @param id  ID-ul CarteFunciara
     * @param dto contine numarCf si numarTopografic
     * @return 200 cu entitatea actualizata, sau 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCf(@PathVariable Long id,
                                      @Valid @RequestBody CarteFunciaraDTO dto) {
        return carteFunciaraRepository.findById(id)
                .map(cf -> {
                    if (dto.getNumarCf() != null) cf.setNumarCf(dto.getNumarCf());
                    if (dto.getNumarTopografic() != null) cf.setNumarTopografic(dto.getNumarTopografic());
                    return ResponseEntity.ok(carteFunciaraRepository.save(cf));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Genereaza si returneaza Cartea Funciara in format PDF.
     */
    @GetMapping("/teren/{terenId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long terenId) {
        try {
            byte[] pdfBytes = pdfExportService.generateCarteFunciaraPdf(terenId);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"carte_funciara_" + terenId + ".pdf\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
