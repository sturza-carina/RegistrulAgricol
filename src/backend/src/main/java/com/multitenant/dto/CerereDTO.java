package com.multitenant.dto;

import com.multitenant.model.registru.StatusCerere;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CerereDTO {
    private Long id;
    private String nume;
    private String domiciliu;
    private String telefon;
    private String email;
    private String numarCarteFunciara;
    private String numarCadastral;
    private String cnpCui;
    private String codCerere;
    private StatusCerere status;
    private Long userId;
    private Long uatId;
    private LocalDateTime createdAt;
}
