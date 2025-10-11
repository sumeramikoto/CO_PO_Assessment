package org.example.co_po_assessment;

import org.example.co_po_assessment.utilities.ExcelImportUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ExcelFacultyImportTest {
    @Test
    void inspectFacultyExcelHeaders() throws Exception {
        File f = new File("src/Excels/Untitled spreadsheet.xlsx");
        assertTrue(f.exists(), "Test Excel file not found at " + f.getAbsolutePath());
        List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(f);
        System.out.println("Rows read: " + rows.size());
        assertTrue(rows.size() > 0, "No data rows found in the first sheet");
        Set<String> headers = rows.get(0).keySet();
        System.out.println("Detected headers: " + headers);
        // Check required headers presence in normalized form
        boolean hasId = headers.contains("id") || headers.contains("faculty_id");
        boolean hasName = headers.contains("full_name") || headers.contains("name");
        boolean hasEmail = headers.contains("email");
        System.out.printf("hasId=%s, hasName=%s, hasEmail=%s\n", hasId, hasName, hasEmail);
        assertTrue(hasId, "Missing required column: id or faculty_id");
        assertTrue(hasName, "Missing required column: full_name or name");
        assertTrue(hasEmail, "Missing required column: email");
    }
}

