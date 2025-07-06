package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.ss.usermodel.*;

public class COResultsGenerator {
    private final Sheet binarySheet;
    private final Sheet resultsSheet;

    /**
     * @param wb           the workbook to write into
     * @param binarySheet  the CO_Binary sheet (with 1s and 0s)
     */
    public COResultsGenerator(Workbook wb, Sheet binarySheet) {
        this.binarySheet = binarySheet;
        this.resultsSheet = wb.createSheet("COResults");
    }

    public Sheet getSheet() {
        return resultsSheet;
    }

    /**
     * Generate the COResults sheet:
     *   Column A = CO name
     *   Column B = % of students who have a 1 in that CO
     */
    public void generate() {
        // 1) Header row
        Row header = resultsSheet.createRow(0);
        header.createCell(0).setCellValue("CO");
        header.createCell(1).setCellValue("% Achieved");

        // 2) Determine total number of students
        //    We assume each non-null row from row 1 downward is a student
        int lastRow = binarySheet.getLastRowNum();
        int studentCount = 0;
        for (int r = 1; r <= lastRow; r++) {
            Row row = binarySheet.getRow(r);
            if (row != null && row.getCell(0) != null
                    && !row.getCell(0).getStringCellValue().trim().isEmpty()) {
                studentCount++;
            }
        }

        // 3) For each CO column in the binary sheet
        Row binHeader = binarySheet.getRow(0);
        int coCount = binHeader.getLastCellNum() - 1;  // minus the "Student ID" column

        int outRowIdx = 1;
        for (int c = 1; c <= coCount; c++) {
            String coName = binHeader.getCell(c).getStringCellValue();
            // count how many students have a "1"
            int passCount = 0;
            for (int r = 1; r <= lastRow; r++) {
                Row row = binarySheet.getRow(r);
                if (row != null) {
                    Cell cell = row.getCell(c);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC
                            && cell.getNumericCellValue() == 1.0) {
                        passCount++;
                    }
                }
            }

            double pctAchieved = studentCount == 0
                    ? 0.0
                    : (passCount * 100.0) / studentCount;

            // write out
            Row out = resultsSheet.createRow(outRowIdx++);
            out.createCell(0).setCellValue(coName);
            out.createCell(1).setCellValue(pctAchieved);
        }

        // 4) Auto‐size columns
        resultsSheet.autoSizeColumn(0);
        resultsSheet.autoSizeColumn(1);

        // 5) (Optional) Protect this sheet too:
        resultsSheet.protectSheet("");
    }
}

