package com.multitenant.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.persoana.PersoanaJuridica;
import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.model.registru.Parcela;
import com.multitenant.repository.ContractUtilizareRepository;
import com.multitenant.service.signature.SemnaturaKeystoreProvider;
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
import java.util.Base64;

@Service
public class ContractSemnaturaService {

    private final ContractUtilizareRepository contractUtilizareRepository;
    private final SemnaturaKeystoreProvider keystoreProvider;

    @Value("${app.signature.storage-path}")
    private String storageBasePath;

    @Value("${app.signature.signer-name}")
    private String signerName;

    @Value("${app.signature.reason}")
    private String signReason;

    public ContractSemnaturaService(ContractUtilizareRepository contractUtilizareRepository,
                                     SemnaturaKeystoreProvider keystoreProvider) {
        this.contractUtilizareRepository = contractUtilizareRepository;
        this.keystoreProvider = keystoreProvider;
    }

    @Transactional
    public ContractUtilizare semneaza(Long contractId, Long currentUserId, String semnaturaImagineBase64) throws Exception {
        ContractUtilizare contract = contractUtilizareRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contractul nu a fost găsit"));

        if (contract.isSemnatElectronic()) {
            throw new IllegalStateException("Contractul este deja semnat electronic");
        }

        byte[] unsignedPdf = genereazaPdfContract(contract);
        byte[] signedPdf = semneazaPdf(unsignedPdf, semnaturaImagineBase64);
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
        contract.setSemnatDeUtilizatorId(currentUserId);
        if (contract.getDataSemnare() == null) {
            contract.setDataSemnare(LocalDate.now());
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

    private byte[] semneazaPdf(byte[] unsignedPdf, String semnaturaImagineBase64) throws Exception {
        PdfReader reader = new PdfReader(unsignedPdf);
        try (ByteArrayOutputStream signedOutput = new ByteArrayOutputStream()) {
            PdfStamper stamper = PdfStamper.createSignature(reader, signedOutput, '\0');
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason(signReason);
            appearance.setLocation(signerName);

            boolean hasDrawnSignature = semnaturaImagineBase64 != null && !semnaturaImagineBase64.isBlank();
            if (hasDrawnSignature) {
                appearance.setVisibleSignature(new Rectangle(330, 40, 560, 130), 1, "semnatura-electronica");
                String base64Data = semnaturaImagineBase64;
                int commaIdx = base64Data.indexOf(',');
                if (base64Data.startsWith("data:") && commaIdx >= 0) {
                    base64Data = base64Data.substring(commaIdx + 1);
                }
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                Image semnaturaImg = Image.getInstance(imageBytes);
                appearance.setSignatureGraphic(semnaturaImg);
                // Doar desenul, fara text suprapus peste el (GraphicAndDescription se suprapunea ilizibil pe o caseta mica).
                appearance.setRender(PdfSignatureAppearance.SignatureRenderGraphic);
            } else {
                appearance.setVisibleSignature(new Rectangle(380, 50, 560, 120), 1, "semnatura-electronica");
            }

            appearance.setCrypto(keystoreProvider.getPrivateKey(), keystoreProvider.getCertificateChain(),
                    null, PdfSignatureAppearance.WINCER_SIGNED);

            stamper.close();
            return signedOutput.toByteArray();
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
