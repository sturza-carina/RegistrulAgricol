package com.multitenant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to read Romanian localities from siruta.csv
 * and generate the Flyway migration V12__populate_all_romanian_localities.sql.
 */
public class SirutaImporter {

    // UATs already present in V3__create_public_uat_table.sql to avoid unique key conflicts
    private static final Set<String> EXCLUDED_SIRUTA_CODES = Set.of("54975", "55311", "1017");

    public static void main(String[] args) {
        System.out.println("Starting SIRUTA Importer utility...");

        // Determine CSV file path
        File csvFile = new File("src/backend/src/main/resources/siruta.csv");
        if (!csvFile.exists()) {
            csvFile = new File("src/main/resources/siruta.csv");
        }

        if (!csvFile.exists()) {
            System.err.println("Error: siruta.csv not found at either path.");
            System.exit(1);
        }
        System.out.println("Found CSV file: " + csvFile.getAbsolutePath());

        // Determine output target directory and file
        File targetDir = new File("src/backend/src/main/resources/db/tenant");
        if (!targetDir.exists()) {
            targetDir = new File("src/main/resources/db/tenant");
        }
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            System.out.println("Created target directory: " + targetDir.getAbsolutePath() + " -> " + created);
        }

        File outputFile = new File(targetDir, "V12__populate_all_romanian_localities.sql");
        System.out.println("Output will be written to: " + outputFile.getAbsolutePath());

        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Split handling empty values
                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    continue;
                }

                String county = cleanValue(parts[2]);
                String typeAbbr = cleanValue(parts[4]);
                String sirutaStr = cleanValue(parts[5]);
                String name = cleanValue(parts[6]);

                // Skip non-data rows or header lines
                if (county.isEmpty() || typeAbbr.isEmpty() || sirutaStr.isEmpty() || name.isEmpty()) {
                    continue;
                }

                // Check if siruta code is numeric
                if (!sirutaStr.matches("\\d+")) {
                    continue;
                }

                // Skip County Councils (CJ) as they do not represent local UATs/localities
                if ("CJ".equalsIgnoreCase(typeAbbr)) {
                    continue;
                }

                // Skip pre-existing UATs (Cluj-Napoca, Floresti, Bucuresti)
                if (EXCLUDED_SIRUTA_CODES.contains(sirutaStr)) {
                    continue;
                }

                // Map abbreviations to full names matching frontend/domain types
                String mappedType;
                switch (typeAbbr.toUpperCase()) {
                    case "M":
                        mappedType = "Municipiu";
                        break;
                    case "O":
                        mappedType = "Oraș";
                        break;
                    case "C":
                        mappedType = "Comună";
                        break;
                    case "B":
                        mappedType = "Municipiu";
                        break;
                    case "S":
                        mappedType = "Sector";
                        break;
                    default:
                        mappedType = typeAbbr;
                        break;
                }

                // Format county and UAT name to Title Case
                county = toTitleCase(county);
                name = toTitleCase(name);

                // Escape single quotes for SQL safety
                name = name.replace("'", "''");
                county = county.replace("'", "''");

                // Format: INSERT INTO public.uat (cod_siruta, denumire, judet, tip_uat, is_active, tenant_id) VALUES ('...', '...', '...', '...', true, NULL);
                String sql = String.format(
                        "INSERT INTO public.uat (cod_siruta, denumire, judet, tip_uat, is_active, tenant_id) VALUES ('%s', '%s', '%s', '%s', true, NULL);",
                        sirutaStr, name, county, mappedType
                );

                writer.write(sql);
                writer.newLine();
                count++;
            }

            System.out.println("Processing finished successfully!");
            System.out.println("Wrote " + count + " records to Flyway migration file.");

        } catch (IOException e) {
            System.err.println("An error occurred during file import/export operations:");
            e.printStackTrace();
        }
    }

    private static String cleanValue(String val) {
        if (val == null) return "";
        val = val.trim();
        if (val.startsWith("\"") && val.endsWith("\"")) {
            val = val.substring(1, val.length() - 1);
        }
        return val.trim();
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Normalize multiple spaces into one
        input = input.replaceAll("\\s+", " ").trim();
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '-') {
                nextTitleCase = true;
                titleCase.append(c);
            } else if (nextTitleCase) {
                titleCase.append(Character.toUpperCase(c));
                nextTitleCase = false;
            } else {
                titleCase.append(Character.toLowerCase(c));
            }
        }
        return titleCase.toString();
    }
}
