package com.multitenant.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.persoana.PersoanaJuridica;
import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.model.registru.Parcela;
import com.multitenant.repository.ContractUtilizareRepository;
import com.multitenant.service.signnow.SignNowClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ContractSemnaturaService {

    private final ContractUtilizareRepository contractUtilizareRepository;
    private final SignNowClient signNowClient;

    @Value("${app.signature.storage-path}")
    private String storageBasePath;

    public ContractSemnaturaService(ContractUtilizareRepository contractUtilizareRepository,
                                     SignNowClient signNowClient) {
        this.contractUtilizareRepository = contractUtilizareRepository;
        this.signNowClient = signNowClient;
    }

    /** Generează PDF-ul contractului, îl încarcă în SignNow și trimite invitația de semnare către semnatar. */
    @Transactional
    public ContractUtilizare trimiteSpreSemnare(Long contractId, String emailSemnatar) throws Exception {
        if (emailSemnatar == null || emailSemnatar.isBlank()) {
            throw new IllegalArgumentException("Adresa de email a semnatarului este obligatorie");
        }
        ContractUtilizare contract = contractUtilizareRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contractul nu a fost găsit"));

        if (contract.isSemnatElectronic()) {
            throw new IllegalStateException("Contractul este deja semnat electronic");
        }

        byte[] unsignedPdf = genereazaPdfContract(contract);
        String fileName = "contract-" + contractId + ".pdf";
        String documentId = signNowClient.uploadDocument(unsignedPdf, fileName);
        signNowClient.sendFreeFormInvite(documentId, emailSemnatar);

        contract.setSignNowDocumentId(documentId);
        contract.setSignNowStatus("trimis");
        contract.setSignNowTrimisLa(LocalDateTime.now());
        contract.setSignNowEmailSemnatar(emailSemnatar);

        return contractUtilizareRepository.save(contract);
    }

    /**
     * Verifică pe SignNow dacă documentul a fost semnat. Dacă da, descarcă versiunea finală semnată,
     * o salvează local și marchează contractul ca semnat electronic.
     */
    @Transactional
    public ContractUtilizare verificaStatusSemnare(Long contractId) throws Exception {
        ContractUtilizare contract = contractUtilizareRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contractul nu a fost găsit"));

        if (contract.getSignNowDocumentId() == null) {
            throw new IllegalStateException("Contractul nu a fost trimis spre semnare");
        }

        if (contract.isSemnatElectronic()) {
            return contract;
        }

        Map<String, Object> details = signNowClient.getDocumentDetails(contract.getSignNowDocumentId());
        boolean semnat = signNowClient.isSigned(details);
        contract.setSignNowStatus(semnat ? "semnat" : "in_asteptare");

        if (semnat) {
            byte[] signedPdf = signNowClient.downloadSignedDocument(contract.getSignNowDocumentId());
            String hash = sha256Hex(signedPdf);

            Path targetDir = Path.of(storageBasePath);
            Files.createDirectories(targetDir);
            String fileName = "contract-" + contractId + "-semnat.pdf";
            Path targetPath = targetDir.resolve(fileName).normalize();
            if (!targetPath.startsWith(targetDir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cale de fișier invalidă");
            }
            Files.write(targetPath, signedPdf);

            contract.setSemnatElectronic(true);
            contract.setDataSemnaturiiElectronice(LocalDateTime.now());
            contract.setCaleDocumentSemnat(targetPath.toString());
            contract.setHashDocumentSemnat(hash);
            if (contract.getDataSemnare() == null) {
                contract.setDataSemnare(LocalDate.now());
            }
        }

        return contractUtilizareRepository.save(contract);
    }

    public Resource descarcaDocumentSemnat(Long contractId) {
        ContractUtilizare contract = contractUtilizareRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contractul nu a fost găsit"));

        if (!contract.isSemnatElectronic() || contract.getCaleDocumentSemnat() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contractul nu este semnat electronic");
        }

        Path path = Path.of(contract.getCaleDocumentSemnat()).normalize();
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fișierul semnat nu mai există pe disc");
        }
        return new FileSystemResource(path);
    }

    private byte[] genereazaPdfContract(ContractUtilizare contract) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph(asciiSafe("CONTRACT DE " + contract.getTipContract()), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingAfter(20);

            addRow(table, "Număr Contract:", contract.getNumarContract(), boldFont, normalFont);
            addRow(table, "Parcelă Asociată:", parcelaLabel(contract.getParcela()), boldFont, normalFont);
            addRow(table, "Locator Proprietar:", persoanaLabel(contract.getLocatorProprietar()), boldFont, normalFont);
            addRow(table, "Locator Utilizator:", persoanaLabel(contract.getLocatorUtilizator()), boldFont, normalFont);
            addRow(table, "Dată Început:", String.valueOf(contract.getDataInceput()), boldFont, normalFont);
            addRow(table, "Dată Sfârșit:", String.valueOf(contract.getDataSfarsit()), boldFont, normalFont);
            addRow(table, "Arendă (RON/an):", contract.getPretArendaRonAn() != null ? String.valueOf(contract.getPretArendaRonAn()) : "-", boldFont, normalFont);
            addRow(table, "Arendă (Kg grâu/ha):", contract.getPretArendaGrauKgHa() != null ? String.valueOf(contract.getPretArendaGrauKgHa()) : "-", boldFont, normalFont);
            addRow(table, "Status:", String.valueOf(contract.getStatusContract()), boldFont, normalFont);

            document.add(table);

            Paragraph generatedAt = new Paragraph(
                    asciiSafe("Document generat la " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))),
                    normalFont);
            generatedAt.setSpacingBefore(30);
            document.add(generatedAt);

            document.close();
            return baos.toByteArray();
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new com.lowagie.text.Phrase(asciiSafe(label), labelFont));
        labelCell.setPadding(5);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new com.lowagie.text.Phrase(asciiSafe(value != null ? value : "-"), valueFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private String parcelaLabel(Parcela parcela) {
        if (parcela == null) {
            return "-";
        }
        return (parcela.getDenumire() != null ? parcela.getDenumire() : "Parcela #" + parcela.getId());
    }

    private String persoanaLabel(Persoana persoana) {
        if (persoana == null) {
            return "-";
        }
        if (persoana instanceof PersoanaFizica pf) {
            return pf.getFirstName() + " " + pf.getLastName();
        }
        if (persoana instanceof PersoanaJuridica pj) {
            return pj.getCompanyName();
        }
        return "Persoana #" + persoana.getId();
    }

    /**
     * FontFactory.HELVETICA foloseste encoding WinAnsi (Cp1252), care nu contine
     * diacriticele romanesti (a-breve, s/t cu virgula). Fara embedding de font Unicode,
     * cea mai sigura optie e sa normalizam textul la echivalentul ASCII de baza,
     * ca sa evitam caractere lipsa/garble in PDF-ul generat.
     */
    private String asciiSafe(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private String sha256Hex(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
