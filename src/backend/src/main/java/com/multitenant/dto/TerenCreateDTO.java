package com.multitenant.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new Teren.
 * Accepts a plain gospodarieId instead of the full Gospodarie entity,
 * which avoids JPA detached-entity issues.
 */
@Data
public class TerenCreateDTO {
    @NotBlank(message = "Denumirea terenului este obligatorie.")
    private String denumire;
    private String tipTeren;
    private String stereo70Coordinates;
    private JsonNode polygon;
    @NotNull(message = "Gospodaria este obligatorie.")
    private Long gospodarieId;
}
