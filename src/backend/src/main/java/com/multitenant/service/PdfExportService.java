package com.multitenant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.persoana.PersoanaJuridica;
import com.multitenant.model.registru.CarteFunciara;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.registru.Parcela;
import com.multitenant.model.registru.Teren;
import com.multitenant.repository.CarteFunciaraRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.repository.TerenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfExportService {

    private final TerenRepository terenRepository;
    private final CarteFunciaraRepository carteFunciaraRepository;
    private final ParcelaRepository parcelaRepository;
    private final PersoanaRepository persoanaRepository;

    public PdfExportService(TerenRepository terenRepository,
                            CarteFunciaraRepository carteFunciaraRepository,
                            ParcelaRepository parcelaRepository,
                            PersoanaRepository persoanaRepository) {
        this.terenRepository = terenRepository;
        this.carteFunciaraRepository = carteFunciaraRepository;
        this.parcelaRepository = parcelaRepository;
        this.persoanaRepository = persoanaRepository;
    }

    public byte[] generateCarteFunciaraPdf(Long terenId) throws Exception {
        Teren teren = terenRepository.findById(terenId)
                .orElseThrow(() -> new RuntimeException("Teren negasit"));
        CarteFunciara cf = carteFunciaraRepository.findByTerenId(terenId)
                .orElseThrow(() -> new RuntimeException("Carte Funciara negasita"));
        List<Parcela> parcele = parcelaRepository.findByTerenId(terenId, org.springframework.data.domain.Pageable.unpaged()).getContent();

        Gospodarie gospodarie = teren.getGospodarie();
        String proprietar = getCapGospodarieNume(gospodarie.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // Titlu
            Paragraph title = new Paragraph("CARTE FUNCIARA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Detalii Teren & Proprietar
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            addCellToTable(infoTable, "Proprietar (Cap Gospodărie):", boldFont);
            addCellToTable(infoTable, proprietar, normalFont);

            addCellToTable(infoTable, "Denumire Teren:", boldFont);
            addCellToTable(infoTable, teren.getDenumire() != null ? teren.getDenumire() : "-", normalFont);

            addCellToTable(infoTable, "Număr CF:", boldFont);
            addCellToTable(infoTable, cf.getNumarCf() != null ? cf.getNumarCf() : "Necompletat", normalFont);

            addCellToTable(infoTable, "Număr Topografic:", boldFont);
            addCellToTable(infoTable, cf.getNumarTopografic() != null ? cf.getNumarTopografic() : "Necompletat", normalFont);

            addCellToTable(infoTable, "Suprafață Totală Intabulată (ha):", boldFont);
            addCellToTable(infoTable, cf.getSuprafataTotalaIntabulata() != null ? String.valueOf(cf.getSuprafataTotalaIntabulata()) : "0", normalFont);

            document.add(infoTable);

            // Parcele
            Paragraph parceleTitle = new Paragraph("Parcele Asociate", subtitleFont);
            parceleTitle.setSpacingAfter(10);
            document.add(parceleTitle);

            if (parcele.isEmpty()) {
                document.add(new Paragraph("Nu există parcele înregistrate pentru acest teren.", normalFont));
            } else {
                for (Parcela p : parcele) {
                    PdfPTable pTable = new PdfPTable(4);
                    pTable.setWidthPercentage(100);
                    pTable.setSpacingAfter(10);
                    
                    addCellToTable(pTable, "Denumire: " + (p.getDenumire() != null ? p.getDenumire() : "-"), normalFont);
                    addCellToTable(pTable, "Suprafață (ha): " + p.getSuprafata(), normalFont);
                    addCellToTable(pTable, "Categ. folosință: " + (p.getCategorieFolosinta() != null ? p.getCategorieFolosinta() : "-"), normalFont);
                    addCellToTable(pTable, "Nr. Cadastral: " + (p.getNumarCadastral() != null ? p.getNumarCadastral() : "-"), normalFont);
                    document.add(pTable);

                    // Coordonate
                    document.add(new Paragraph("Coordonate Parcela", boldFont));
                    PdfPTable coordTable = new PdfPTable(3);
                    coordTable.setWidthPercentage(80);
                    coordTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                    coordTable.setSpacingBefore(5);
                    coordTable.setSpacingAfter(15);
                    
                    addCellToTable(coordTable, "Nr. Crt.", boldFont);
                    addCellToTable(coordTable, "X", boldFont);
                    addCellToTable(coordTable, "Y", boldFont);

                    extractAndAddCoordinates(p, coordTable, normalFont);
                    document.add(coordTable);
                }
            }

            document.close();
            return baos.toByteArray();
        }
    }

    private void addCellToTable(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String getCapGospodarieNume(Long gospodarieId) {
        Page<Persoana> persoanaPage = persoanaRepository.findByGospodarieId(gospodarieId, PageRequest.of(0, 1));
        if (persoanaPage.hasContent()) {
            Persoana p = persoanaPage.getContent().get(0);
            if (p instanceof PersoanaFizica pf) {
                return pf.getFirstName() + " " + pf.getLastName();
            } else if (p instanceof PersoanaJuridica pj) {
                return pj.getCompanyName();
            }
        }
        return "Necunoscut";
    }

    private void extractAndAddCoordinates(Parcela p, PdfPTable coordTable, Font font) {
        JsonNode poly = p.getPolygon();
        boolean hasCoords = false;

        if (poly != null) {
            JsonNode coordsArr = null;

            if (poly.has("coordinates")) {
                coordsArr = poly.get("coordinates");
            } else if (poly.has("geometry") && poly.get("geometry").has("coordinates")) {
                coordsArr = poly.get("geometry").get("coordinates");
            }

            if (coordsArr != null && coordsArr.isArray() && coordsArr.size() > 0) {
                // GeoJSON format: "coordinates": [ [ [lng, lat], [lng, lat] ] ]
                JsonNode ring = coordsArr.get(0); 
                if (ring.isArray()) {
                    for (int i = 0; i < ring.size(); i++) {
                        JsonNode pt = ring.get(i);
                        if (pt.isArray() && pt.size() >= 2) {
                            String x = pt.get(0).asText();
                            String y = pt.get(1).asText();
                            
                            addCellToTable(coordTable, String.valueOf(i + 1), font);
                            addCellToTable(coordTable, x, font);
                            addCellToTable(coordTable, y, font);
                            hasCoords = true;
                        }
                    }
                }
            }
        }

        if (!hasCoords) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("Nu există coordonate înregistrate.", font));
            emptyCell.setColspan(3);
            emptyCell.setPadding(5);
            coordTable.addCell(emptyCell);
        }
    }
}
