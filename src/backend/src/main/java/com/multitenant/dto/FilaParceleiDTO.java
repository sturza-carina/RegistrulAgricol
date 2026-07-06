package com.multitenant.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FilaParceleiDTO {
    private LocalDate data;
    private String tipInterventie; // "Pesticid (PPP)" sau "Fertilizare"
    private String denumireProdus;
    private String tipProdus; // Fungicid, Insecticid, Organic, Chimic etc.
    private Double dozaCantitate; // Doza utilizata (L/ha or kg/ha) sau Cantitatea bruta (tone/ha or kg/ha)
    private String unitateMasura; // L/ha, kg/ha, t/ha
    private Double suprafataTratata; // aplicat doar pentru fitosanitar
    private String detaliiTinta; // Dăunătorul vizat sau Aportul de substanță activă (N-P-K)
    private String responsabil; // Persoana responsabilă de tratament/fertilizare
    private String statusAlerta; // Doza depasită sau Interdicție de iarnă
}
