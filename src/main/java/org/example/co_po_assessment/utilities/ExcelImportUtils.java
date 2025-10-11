package org.example.co_po_assessment.utilities;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public final class ExcelImportUtils {
    private ExcelImportUtils() {}

    /**
     * Reads the first sheet of the workbook and returns rows as maps: header -> string value.
     * Header keys are normalized to lower_snake_case (trim, lowercase, spaces->underscore).
     */
    public static List<Map<String, String>> readSheetAsMaps(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file); Workbook wb = WorkbookFactory.create(fis)) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) return List.of();
            DataFormatter formatter = new DataFormatter();
            Iterator<Row> it = sheet.rowIterator();
            if (!it.hasNext()) return List.of();
            Row headerRow = it.next();
            int maxCell = headerRow.getLastCellNum();
            Map<Integer, String> headers = new LinkedHashMap<>();
            for (int c = 0; c < maxCell; c++) {
                Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                String key = cell == null ? null : normalizeHeader(formatter.formatCellValue(cell));
                if (key != null && !key.isBlank()) headers.put(c, key);
            }
            List<Map<String, String>> rows = new ArrayList<>();
            while (it.hasNext()) {
                Row row = it.next();
                if (row == null) continue;
                Map<String, String> map = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> e : headers.entrySet()) {
                    int c = e.getKey(); String key = e.getValue();
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String val = cell == null ? "" : formatter.formatCellValue(cell);
                    if (val != null) val = val.trim(); else val = "";
                    map.put(key, val);
                }
                boolean allBlank = map.values().stream().allMatch(String::isBlank);
                if (!allBlank) rows.add(map);
            }
            return rows;
        }
    }

    public static String normalizeHeader(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase(Locale.ROOT);
        // Safely collapse line breaks and tabs to spaces without regex pitfalls
        s = s.replace("\r", " ").replace("\n", " ").replace('\t', ' ');
        s = s.replaceAll("\\s+", " ").trim();
        s = s.replace(' ', '_');
        return s;
    }

    /**
     * Helper to get a value from row map by any of candidate keys (case-insensitive normalized).
     */
    public static String get(Map<String, String> row, String... candidates) {
        for (String c : candidates) {
            String k = normalizeHeader(c);
            if (row.containsKey(k)) return emptyToNull(row.get(k));
        }
        return null;
    }

    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}
