package com.multitenant.service;

import com.multitenant.dto.*;
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

    @Autowired
    public RaportStatisticService(CulturaParcelaRepository culturaParcelaRepository,
                                  ParcelaRepository parcelaRepository,
                                  AnimalIndividualRepository animalIndividualRepository,
                                  EfectivGrupRepository efectivGrupRepository,
                                  MachineryRepository machineryRepository) {
        this.culturaParcelaRepository = culturaParcelaRepository;
        this.parcelaRepository = parcelaRepository;
        this.animalIndividualRepository = animalIndividualRepository;
        this.efectivGrupRepository = efectivGrupRepository;
        this.machineryRepository = machineryRepository;
    }

    public StatisticiRaportCompletDto getComplet(Integer an) {
        List<StatisticaCulturaDto> culturi = culturaParcelaRepository.getStatisticiCulturi(an);
        List<StatisticaCategorieFolosintaDto> categoriiFolosinta = parcelaRepository.getStatisticiCategoriiFolosinta();
        List<StatisticaAnimalDto> animaleIndividuale = animalIndividualRepository.getStatisticiAnimaleIndividuale();
        List<StatisticaEfectivGrupDto> efectiveGrup = efectivGrupRepository.getStatisticiEfectiveGrup();
        List<StatisticaUtilajDto> utilaje = machineryRepository.getStatisticiUtilaje();

        return new StatisticiRaportCompletDto(culturi, categoriiFolosinta, animaleIndividuale, efectiveGrup, utilaje);
    }

    public byte[] exportVegetalXlsx(Integer an) {
        List<StatisticaCulturaDto> culturi = culturaParcelaRepository.getStatisticiCulturi(an);
        List<StatisticaCategorieFolosintaDto> categoriiFolosinta = parcelaRepository.getStatisticiCategoriiFolosinta();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Sheet 1: Culturi Agricole
            Sheet sheet1 = workbook.createSheet("Culturi Agricole " + an);
            Row headerRow1 = sheet1.createRow(0);
            headerRow1.createCell(0).setCellValue("Specie Cultură");
            headerRow1.createCell(1).setCellValue("Suprafață Totală (ha)");
            headerRow1.createCell(2).setCellValue("Producție Totală Estimată (tone)");
            for (int i = 0; i < 3; i++) {
                headerRow1.getCell(i).setCellStyle(headerStyle);
            }

            int rowIdx = 1;
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
            Row headerRow2 = sheet2.createRow(0);
            headerRow2.createCell(0).setCellValue("Categorie Folosință");
            headerRow2.createCell(1).setCellValue("Suprafață Totală (ha)");
            for (int i = 0; i < 2; i++) {
                headerRow2.getCell(i).setCellStyle(headerStyle);
            }

            rowIdx = 1;
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

    public byte[] exportZootehnicXlsx() {
        List<StatisticaAnimalDto> animaleIndividuale = animalIndividualRepository.getStatisticiAnimaleIndividuale();
        List<StatisticaEfectivGrupDto> efectiveGrup = efectivGrupRepository.getStatisticiEfectiveGrup();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Sheet 1: Animale Individuale
            Sheet sheet1 = workbook.createSheet("Animale Individuale");
            Row headerRow1 = sheet1.createRow(0);
            headerRow1.createCell(0).setCellValue("Specie Animal");
            headerRow1.createCell(1).setCellValue("Total Capete");
            headerRow1.createCell(2).setCellValue("Masculi");
            headerRow1.createCell(3).setCellValue("Femele");
            for (int i = 0; i < 4; i++) {
                headerRow1.getCell(i).setCellStyle(headerStyle);
            }

            int rowIdx = 1;
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
            Row headerRow2 = sheet2.createRow(0);
            headerRow2.createCell(0).setCellValue("Specie Animal");
            headerRow2.createCell(1).setCellValue("Total Capete / Familii");
            for (int i = 0; i < 2; i++) {
                headerRow2.getCell(i).setCellStyle(headerStyle);
            }

            rowIdx = 1;
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

    public byte[] exportUtilajeXlsx() {
        List<StatisticaUtilajDto> utilaje = machineryRepository.getStatisticiUtilaje();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Sheet sheet = workbook.createSheet("Utilaje Agricole");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Tip Utilaj");
            headerRow.createCell(1).setCellValue("Total Unități");
            for (int i = 0; i < 2; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            int rowIdx = 1;
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
