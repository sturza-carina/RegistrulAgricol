package com.multitenant.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.multitenant.model.common.Adresa;
import java.util.List;

@Data
@NoArgsConstructor
public class PersoanaDTO {
    private Long id;
    private String personType;
    private String firstName;
    private String lastName;
    private String cnp;
    private String initialaTatalui;
    private String companyName;
    private String cui;
    private Adresa adresa;
    private String phoneNumber;
    private String email;
    private List<Long> gospodarieIds;
}
