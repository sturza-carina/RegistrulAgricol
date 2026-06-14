package com.multitenant.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * DTO for creating a new Teren.
 * Accepts a plain gospodarieId instead of the full Gospodarie entity,
 * which avoids JPA detached-entity issues.
 */
@Data
public class TerenCreateDTO {
    private String denumire;
    private String tipTeren;
    private String stereo70Coordinates;
    private JsonNode polygon;
    private Long gospodarieId;
}
