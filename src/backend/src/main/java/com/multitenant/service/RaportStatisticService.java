package com.multitenant.service;

import com.multitenant.dto.*;
import com.multitenant.model.core.Uat;
import com.multitenant.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RaportStatisticService {

    private final CulturaParcelaRepository culturaParcelaRepository;
    private final ParcelaRepository parcelaRepository;
    private final AnimalIndividualRepository animalIndividualRepository;
    private final EfectivGrupRepository efectivGrupRepository;
    private final MachineryRepository machineryRepository;
    private final UatRepository uatRepository;

    @Autowired
    public RaportStatisticService(CulturaParcelaRepository culturaParcelaRepository,
                                  ParcelaRepository parcelaRepository,
                                  AnimalIndividualRepository animalIndividualRepository,
                                  EfectivGrupRepository efectivGrupRepository,
                                  MachineryRepository machineryRepository,
                                  UatRepository uatRepository) {
        this.culturaParcelaRepository = culturaParcelaRepository;
        this.parcelaRepository = parcelaRepository;
        this.animalIndividualRepository = animalIndividualRepository;
        this.efectivGrupRepository = efectivGrupRepository;
        this.machineryRepository = machineryRepository;
        this.uatRepository = uatRepository;
    }

    public StatisticiRaportCompletDto getComplet(Integer an, String uatCode) {
        List<StatisticaCulturaDto> culturi = culturaParcelaRepository.getStatisticiCulturi(an, uatCode);
        List<StatisticaCategorieFolosintaDto> categoriiFolosinta = parcelaRepository.getStatisticiCategoriiFolosinta(uatCode);
        List<StatisticaAnimalDto> animaleIndividuale = animalIndividualRepository.getStatisticiAnimaleIndividuale(uatCode);
        List<StatisticaEfectivGrupDto> efectiveGrup = efectivGrupRepository.getStatisticiEfectiveGrup(uatCode);
        List<StatisticaUtilajDto> utilaje = machineryRepository.getStatisticiUtilaje(uatCode);

        return new StatisticiRaportCompletDto(culturi, categoriiFolosinta, animaleIndividuale, efectiveGrup, utilaje);
    }

    private String getUatName(String uatCode) {
        if (uatCode != null && !uatCode.trim().isEmpty()) {
            return uatRepository.findByCodSiruta(uatCode)
                    .map(u -> u.getDenumire() + (u.getJudet() != null ? " (Jud. " + u.getJudet() + ")" : ""))
                    .orElse("Registrul Agricol Local");
        }
        return uatRepository.findAll().stream()
                .findFirst()
                .map(u -> u.getDenumire() + (u.getJudet() != null ? " (Jud. " + u.getJudet() + ")" : ""))
                .orElse("Registrul Agricol Local");
    }

    public byte[] exportVegetalXlsx(Integer an, String uatCode) {
        List<StatisticaCulturaDto> culturi = culturaParcelaRepository.getStatisticiCulturi(an, uatCode);
        List<StatisticaCategorieFolosintaDto> categoriiFolosinta = parcelaRepository.getStatisticiCategoriiFolosinta(uatCode);
        String uatName = getUatName(uatCode);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Fonturi și Stiluri
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 11);

            Font metaFont = workbook.createFont();
            metaFont.setItalic(true);
            metaFont.setFontHeightInPoints((short) 10);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setFont(subtitleFont);

            CellStyle metaStyle = workbook.createCellStyle();
            metaStyle.setFont(metaFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Sheet 1: Culturi Agricole
            Sheet sheet1 = workbook.createSheet("Culturi Agricole " + an);
            
            // Antet administrativ
            Row titleRow = sheet1.createRow(0);
            titleRow.createCell(0).setCellValue("REGISTRUL AGRICOL LOCAL");
            titleRow.getCell(0).setCellStyle(titleStyle);

            Row subtitleRow1 = sheet1.createRow(1);
            subtitleRow1.createCell(0).setCellValue("CENTRALIZATOR STATISTIC - SECTOR VEGETAL (CULTURI)");
            subtitleRow1.getCell(0).setCellStyle(subtitleStyle);

            Row metaRow1 = sheet1.createRow(2);
            metaRow1.createCell(0).setCellValue("UAT: " + uatName + " | An raportare: " + an);
            metaRow1.getCell(0).setCellStyle(metaStyle);

            // Rând liber
            sheet1.createRow(3);

            // Tabel
            Row headerRow1 = sheet1.createRow(4);
            headerRow1.createCell(0).setCellValue("Specie Cultură / Plantă");
            headerRow1.createCell(1).setCellValue("Suprafață Totală (ha)");
            headerRow1.createCell(2).setCellValue("Producție Totală Estimată (tone)");
            for (int i = 0; i < 3; i++) {
                headerRow1.getCell(i).setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            for (StatisticaCulturaDto c : culturi) {
                Row row = sheet1.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getSpecieCultura() != null ? c.getSpecieCultura() : "-");
                row.createCell(1).setCellValue(c.getSuprafataTotalaHa() != null ? c.getSuprafataTotalaHa() : 0.0);
                row.createCell(2).setCellValue(c.getProductieTotalaTone() != null ? c.getProductieTotalaTone() : 0.0);
            }
            sheet1.autoSizeColumn(0);
            sheet1.autoSizeColumn(1);
            sheet1.autoSizeColumn(2);

            // Sheet 2: Categorii de Folosință
            Sheet sheet2 = workbook.createSheet("Categorii de Folosință");
            
            // Antet administrativ
            Row titleRow2 = sheet2.createRow(0);
            titleRow2.createCell(0).setCellValue("REGISTRUL AGRICOL LOCAL");
            titleRow2.getCell(0).setCellStyle(titleStyle);

            Row subtitleRow2 = sheet2.createRow(1);
            subtitleRow2.createCell(0).setCellValue("CENTRALIZATOR STATISTIC - CATEGORII DE FOLOSINȚĂ TERENURI");
            subtitleRow2.getCell(0).setCellStyle(subtitleStyle);

            Row metaRow2 = sheet2.createRow(2);
            metaRow2.createCell(0).setCellValue("UAT: " + uatName + " | Situație consolidată");
            metaRow2.getCell(0).setCellStyle(metaStyle);

            // Rând liber
            sheet2.createRow(3);

            // Tabel
            Row headerRow2 = sheet2.createRow(4);
            headerRow2.createCell(0).setCellValue("Categorie de Folosință");
            headerRow2.createCell(1).setCellValue("Suprafață Totală (ha)");
            for (int i = 0; i < 2; i++) {
                headerRow2.getCell(i).setCellStyle(headerStyle);
            }

            rowIdx = 5;
            for (StatisticaCategorieFolosintaDto f : categoriiFolosinta) {
                Row row = sheet2.createRow(rowIdx++);
                row.createCell(0).setCellValue(f.getCategorieFolosinta() != null ? f.getCategorieFolosinta() : "-");
                row.createCell(1).setCellValue(f.getSuprafataTotalaHa() != null ? f.getSuprafataTotalaHa() : 0.0);
            }
            sheet2.autoSizeColumn(0);
            sheet2.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Eroare la generarea fișierului Excel", e);
        }
    }

    public byte[] exportZootehnicXlsx(String uatCode) {
        List<StatisticaAnimalDto> animaleIndividuale = animalIndividualRepository.getStatisticiAnimaleIndividuale(uatCode);
        List<StatisticaEfectivGrupDto> efectiveGrup = efectivGrupRepository.getStatisticiEfectiveGrup(uatCode);
        String uatName = getUatName(uatCode);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Fonturi și Stiluri
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 11);

            Font metaFont = workbook.createFont();
            metaFont.setItalic(true);
            metaFont.setFontHeightInPoints((short) 10);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setFont(subtitleFont);

            CellStyle metaStyle = workbook.createCellStyle();
            metaStyle.setFont(metaFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Sheet 1: Animale Individuale
            Sheet sheet1 = workbook.createSheet("Animale Individuale");
            
            // Antet administrativ
            Row titleRow = sheet1.createRow(0);
            titleRow.createCell(0).setCellValue("REGISTRUL AGRICOL LOCAL");
            titleRow.getCell(0).setCellStyle(titleStyle);

            Row subtitleRow1 = sheet1.createRow(1);
            subtitleRow1.createCell(0).setCellValue("CENTRALIZATOR STATISTIC - SECTOR ZOOTEHNIC (ANIMALE INDIVIDUALE)");
            subtitleRow1.getCell(0).setCellStyle(subtitleStyle);

            Row metaRow1 = sheet1.createRow(2);
            metaRow1.createCell(0).setCellValue("UAT: " + uatName + " | Situație consolidată crotalate");
            metaRow1.getCell(0).setCellStyle(metaStyle);

            // Rând liber
            sheet1.createRow(3);

            // Tabel
            Row headerRow1 = sheet1.createRow(4);
            headerRow1.createCell(0).setCellValue("Specie Animal");
            headerRow1.createCell(1).setCellValue("Total Capete");
            headerRow1.createCell(2).setCellValue("Masculi");
            headerRow1.createCell(3).setCellValue("Femele");
            for (int i = 0; i < 4; i++) {
                headerRow1.getCell(i).setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            for (StatisticaAnimalDto a : animaleIndividuale) {
                Row row = sheet1.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getSpecie() != null ? a.getSpecie().toString() : "-");
                row.createCell(1).setCellValue(a.getTotalCapete() != null ? a.getTotalCapete() : 0L);
                row.createCell(2).setCellValue(a.getMasculi() != null ? a.getMasculi() : 0L);
                row.createCell(3).setCellValue(a.getFemele() != null ? a.getFemele() : 0L);
            }
            for (int i = 0; i < 4; i++) {
                sheet1.autoSizeColumn(i);
            }

            // Sheet 2: Efective de Grup
            Sheet sheet2 = workbook.createSheet("Efective de Grup");
            
            // Antet administrativ
            Row titleRow2 = sheet2.createRow(0);
            titleRow2.createCell(0).setCellValue("REGISTRUL AGRICOL LOCAL");
            titleRow2.getCell(0).setCellStyle(titleStyle);

            Row subtitleRow2 = sheet2.createRow(1);
            subtitleRow2.createCell(0).setCellValue("CENTRALIZATOR STATISTIC - SECTOR ZOOTEHNIC (EFECTIVE DE GRUP)");
            subtitleRow2.getCell(0).setCellStyle(subtitleStyle);

            Row metaRow2 = sheet2.createRow(2);
            metaRow2.createCell(0).setCellValue("UAT: " + uatName + " | Situație consolidată păsări/albine");
            metaRow2.getCell(0).setCellStyle(metaStyle);

            // Rând liber
            sheet2.createRow(3);

            // Tabel
            Row headerRow2 = sheet2.createRow(4);
            headerRow2.createCell(0).setCellValue("Specie / Categorie Grup");
            headerRow2.createCell(1).setCellValue("Total Capete / Familii");
            for (int i = 0; i < 2; i++) {
                headerRow2.getCell(i).setCellStyle(headerStyle);
            }

            rowIdx = 5;
            for (StatisticaEfectivGrupDto eg : efectiveGrup) {
                Row row = sheet2.createRow(rowIdx++);
                row.createCell(0).setCellValue(eg.getSpecie() != null ? eg.getSpecie().toString() : "-");
                row.createCell(1).setCellValue(eg.getTotalCapete() != null ? eg.getTotalCapete() : 0L);
            }
            sheet2.autoSizeColumn(0);
            sheet2.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Eroare la generarea fișierului Excel", e);
        }
    }

    public byte[] exportUtilajeXlsx(String uatCode) {
        List<StatisticaUtilajDto> utilaje = machineryRepository.getStatisticiUtilaje(uatCode);
        String uatName = getUatName(uatCode);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Fonturi și Stiluri
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 11);

            Font metaFont = workbook.createFont();
            metaFont.setItalic(true);
            metaFont.setFontHeightInPoints((short) 10);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);

            CellStyle subtitleStyle = workbook.createCellStyle();
            subtitleStyle.setFont(subtitleFont);

            CellStyle metaStyle = workbook.createCellStyle();
            metaStyle.setFont(metaFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Sheet sheet = workbook.createSheet("Utilaje Agricole");
            
            // Antet administrativ
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("REGISTRUL AGRICOL LOCAL");
            titleRow.getCell(0).setCellStyle(titleStyle);

            Row subtitleRow = sheet.createRow(1);
            subtitleRow.createCell(0).setCellValue("CENTRALIZATOR STATISTIC - MAȘINI ȘI UTILAJE AGRICOLE");
            subtitleRow.getCell(0).setCellStyle(subtitleStyle);

            Row metaRow = sheet.createRow(2);
            metaRow.createCell(0).setCellValue("UAT: " + uatName + " | Situație consolidată parc auto/utilaje");
            metaRow.getCell(0).setCellStyle(metaStyle);

            // Rând liber
            sheet.createRow(3);

            // Tabel
            Row headerRow = sheet.createRow(4);
            headerRow.createCell(0).setCellValue("Denumire / Tip Utilaj sau Mașină Agricolă");
            headerRow.createCell(1).setCellValue("Total Unități Înregistrate");
            for (int i = 0; i < 2; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            int rowIdx = 5;
            for (StatisticaUtilajDto u : utilaje) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(u.getTipUtilaj() != null ? u.getTipUtilaj() : "-");
                row.createCell(1).setCellValue(u.getTotalUnitati() != null ? u.getTotalUnitati() : 0L);
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Eroare la generarea fișierului Excel", e);
        }
    }
}
