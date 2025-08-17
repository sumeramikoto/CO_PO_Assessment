package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class MidFinalEntryGenerator {
    private String path;

    public void generateSheet(String path) throws IOException {
        // adjust paths if needed

        XSSFWorkbook wb;
        try (FileInputStream fis = new FileInputStream(path)) {
            wb = new XSSFWorkbook(fis);
        }

        // 1) Read all Student IDs from StudentInfo (col B, starting row 2)
        Sheet studentSheet = wb.getSheet("StudentInfo");
        List<String> studentIds = new ArrayList<>();
        for (int r = 11; r <= studentSheet.getLastRowNum(); r++) {
            Row row = studentSheet.getRow(r);
            if (row == null) continue;
            Cell c = row.getCell(0);
            if (c == null) continue;
            switch (c.getCellType()) {
                case STRING:
                    studentIds.add(c.getStringCellValue());
                    break;
                case NUMERIC:
                    studentIds.add(String.valueOf((long)c.getNumericCellValue()));
                    break;
                default:
                    // skip any unexpected types
            }
        }

        // 2) Read Questions into two separate lists by block
        Sheet qSheet = wb.getSheet("QuestionInfoMidFinal");
        List<Question> midQs   = new ArrayList<>();
        List<Question> finalQs = new ArrayList<>();

        // questions start on Excel row 7 → POI index 6
        for (int r = 6; r <= qSheet.getLastRowNum(); r++) {
            Row row = qSheet.getRow(r);
            if (row == null) continue;

            // MID block cols A=0 (Q#), B=1 (max)
            Cell midQcell = row.getCell(0);
            Cell midMcell = row.getCell(1);

            if (midQcell != null && midMcell != null) {
                String midQnum = null;

                if (midQcell.getCellType() == CellType.STRING) {
                    midQnum = midQcell.getStringCellValue().trim();
                } else if (midQcell.getCellType() == CellType.NUMERIC) {
                    midQnum = String.valueOf((long) midQcell.getNumericCellValue());
                }

                if (midQnum != null && !midQnum.isBlank()) {
                    double max = midMcell.getNumericCellValue();
                    midQs.add(new Question(midQnum, max));
                }
            }

            // FINAL block cols E=4 (Q#), F=5 (max)
            Cell finQcell = row.getCell(4);
            Cell finMcell = row.getCell(5);
            if (finQcell != null && finMcell != null) {
                String finQnum = null;

                if (finQcell.getCellType() == CellType.STRING) {
                    finQnum = finQcell.getStringCellValue().trim();
                } else if (finQcell.getCellType() == CellType.NUMERIC) {
                    finQnum = String.valueOf((long) finQcell.getNumericCellValue());
                }

                if (finQnum != null && !finQnum.isBlank()) {
                    double max = finMcell.getNumericCellValue();
                    finalQs.add(new Question(finQnum, max));
                }
            }
        }

        // 3) Build the two entry sheets
        createEntrySheet(wb, "MidEntry",   midQs,   studentIds);
        createEntrySheet(wb, "FinalEntry", finalQs, studentIds);

        // 4) Save out
        try (FileOutputStream fos = new FileOutputStream(path)) {
                wb.write(fos);
        }

        System.out.println("Done—wrote: " + path);

    }

    /**
     * Creates:
     *  Row 0: SID | Q1 (max1) … Qn (maxn) | Total
     *  Rows 1…: each student’s SID, blank cells for answers, and a SUM() in Total.
     */
    private void createEntrySheet(XSSFWorkbook wb,
                                        String sheetName,
                                        List<Question> questions,
                                        List<String> studentIds) {

        Sheet sheet = wb.createSheet(sheetName);

        // 1) Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("SID");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            header.createCell(i + 1)
                    .setCellValue(q.title + " (" + q.maxMark + ")");
        }
        int totalCol = 1 + questions.size();
        header.createCell(totalCol).setCellValue("Total");

        // 2) Data rows (SID + blanks + SUM)
        for (int r = 0; r < studentIds.size(); r++) {
            Row row = sheet.createRow(r + 1);
            row.createCell(0).setCellValue(studentIds.get(r));
            for (int j = 0; j < questions.size(); j++) {
                row.createCell(1 + j);  // blank, will get validation
            }
            String start = CellReference.convertNumToColString(1) + (r + 2);
            String end   = CellReference.convertNumToColString(questions.size()) + (r + 2);
            row.createCell(totalCol)
                    .setCellFormula("SUM(" + start + ":" + end + ")");
        }

        // 3) Apply Data Validation per question column
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        for (int i = 0; i < questions.size(); i++) {
            double max = questions.get(i).maxMark;
            // decimal between 0 and max
            DataValidationConstraint dvConstraint = dvHelper.createDecimalConstraint(
                    DataValidationConstraint.OperatorType.BETWEEN,
                    "0",                     // min
                    String.valueOf(max)      // max
            );
            // apply to rows 2..(studentIds.size()+1), column = i+1
            CellRangeAddressList addressList =
                    new CellRangeAddressList(
                            1,                 // first row (0-based) => Excel row 2
                            studentIds.size(), // last row => Excel row studentCount+1
                            i + 1,             // first col
                            i + 1              // last col
                    );
            DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
            // optional: customize the error message
            validation.createErrorBox(
                    "Invalid mark",
                    "Please enter a number between 0 and " + max
            );
            sheet.addValidationData(validation);
        }

        // 4) Auto‑size columns
        for (int c = 0; c <= totalCol; c++) {
            sheet.autoSizeColumn(c);
        }
    }
}
