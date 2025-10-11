package org.example.co_po_assessment;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.co_po_assessment.utilities.ExcelImportUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BulkImportHeadersTest {

    private File createWorkbook(String[][] data) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sh = wb.createSheet("Sheet1");
        for (int r = 0; r < data.length; r++) {
            Row row = sh.createRow(r);
            String[] arr = data[r];
            for (int c = 0; c < arr.length; c++) {
                row.createCell(c).setCellValue(arr[c]);
            }
        }
        File f = File.createTempFile("bulk-", ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(f)) { wb.write(fos); }
        wb.close();
        f.deleteOnExit();
        return f;
    }

    @Test
    void studentsHeadersVariantsAreRecognized() throws Exception {
        String[][] content = {
                {"Student ID", "Full Name", "Batch", "Email", "Dept", "Program"},
                {"2020001", "Alice Smith", "2020", "alice@example.edu", "CSE", "BSc"}
        };
        File f = createWorkbook(content);
        List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(f);
        assertEquals(1, rows.size());
        Map<String, String> row = rows.get(0);
        String id = ExcelImportUtils.get(row, "id", "student_id");
        String name = ExcelImportUtils.get(row, "name", "full_name");
        String batchStr = ExcelImportUtils.get(row, "batch", "year");
        String email = ExcelImportUtils.get(row, "email");
        String dept = ExcelImportUtils.get(row, "department", "dept");
        String prog = ExcelImportUtils.get(row, "programme", "program");
        assertAll(
                () -> assertEquals("2020001", id),
                () -> assertEquals("Alice Smith", name),
                () -> assertEquals("2020", batchStr),
                () -> assertEquals("alice@example.edu", email),
                () -> assertEquals("CSE", dept),
                () -> assertEquals("BSc", prog)
        );
    }

    @Test
    void coursesHeadersVariantsAreRecognized() throws Exception {
        String[][] content = {
                {"Code", "Title", "Credit", "Dept", "Programme"},
                {"CSE101", "Intro to CSE", "3.0", "CSE", "BSc"}
        };
        File f = createWorkbook(content);
        List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(f);
        assertEquals(1, rows.size());
        Map<String, String> row = rows.get(0);
        String code = ExcelImportUtils.get(row, "course_code", "code");
        String name = ExcelImportUtils.get(row, "course_name", "name", "title");
        String creditsStr = ExcelImportUtils.get(row, "credits", "credit");
        String dept = ExcelImportUtils.get(row, "department", "dept");
        String prog = ExcelImportUtils.get(row, "programme", "program", "program_name");
        assertAll(
                () -> assertEquals("CSE101", code),
                () -> assertEquals("Intro to CSE", name),
                () -> assertEquals("3.0", creditsStr),
                () -> assertEquals("CSE", dept),
                () -> assertEquals("BSc", prog)
        );
    }

    @Test
    void enrollmentsHeadersVariantsAreRecognized() throws Exception {
        String[][] content = {
                {"student_id", "course_code", "program", "ay"},
                {"2020001", "CSE101", "BSc", "2024-2025"}
        };
        File f = createWorkbook(content);
        List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(f);
        assertEquals(1, rows.size());
        Map<String, String> row = rows.get(0);
        String sid = ExcelImportUtils.get(row, "student_id", "id");
        String code = ExcelImportUtils.get(row, "course_code", "course");
        String prog = ExcelImportUtils.get(row, "programme", "program");
        String year = ExcelImportUtils.get(row, "academic_year", "year", "ay");
        assertAll(
                () -> assertEquals("2020001", sid),
                () -> assertEquals("CSE101", code),
                () -> assertEquals("BSc", prog),
                () -> assertEquals("2024-2025", year)
        );
    }

    @Test
    void assignmentsHeadersVariantsAreRecognized() throws Exception {
        String[][] content = {
                {"course", "program", "faculty", "year"},
                {"CSE101", "BSc", "Dr. Alice", "2024-2025"}
        };
        File f = createWorkbook(content);
        List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(f);
        assertEquals(1, rows.size());
        Map<String, String> row = rows.get(0);
        String code = ExcelImportUtils.get(row, "course_code", "course");
        String programme = ExcelImportUtils.get(row, "programme", "program");
        String facultyName = ExcelImportUtils.get(row, "faculty_name", "faculty", "instructor", "teacher");
        String ay = ExcelImportUtils.get(row, "academic_year", "year", "ay");
        assertAll(
                () -> assertEquals("CSE101", code),
                () -> assertEquals("BSc", programme),
                () -> assertEquals("Dr. Alice", facultyName),
                () -> assertEquals("2024-2025", ay)
        );
    }
}

