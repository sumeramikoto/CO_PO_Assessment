package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class QuizEntryGenerator {

    public void generateSheet(String path) throws IOException {
        XSSFWorkbook wb;
        try (FileInputStream fis = new FileInputStream(path)) {
            wb = new XSSFWorkbook(fis);
        }

        // 1. Read student IDs
        Sheet studentSheet = wb.getSheet("StudentInfo");
        List<String> studentIds = new ArrayList<>();
        for (int r = 11; r <= studentSheet.getLastRowNum(); r++) {
            Row row = studentSheet.getRow(r);
            if (row == null) continue;
            Cell c = row.getCell(0);
            if (c == null) continue;
            switch (c.getCellType()) {
                case STRING -> studentIds.add(c.getStringCellValue());
                case NUMERIC -> studentIds.add(String.valueOf((long)c.getNumericCellValue()));
            }
        }

        // 2. Read QuestionInfoQuiz
        Sheet qSheet = wb.getSheet("QuestionInfoQuiz");
        // 4 blocks: Quiz1 (A,B), Quiz2 (D,E), Quiz3 (G,H), Quiz4 (J,K)
        Map<String, List<Question>> quizMap = new LinkedHashMap<>();
        quizMap.put("Quiz1Entry", readQuizBlock(qSheet, 0, 1));
        quizMap.put("Quiz2Entry", readQuizBlock(qSheet, 4, 5));
        quizMap.put("Quiz3Entry", readQuizBlock(qSheet, 8, 9));
        quizMap.put("Quiz4Entry", readQuizBlock(qSheet, 12, 13));

        // 3. Generate each entry sheet
        for (Map.Entry<String, List<Question>> entry : quizMap.entrySet()) {
            createEntrySheet(wb, entry.getKey(), entry.getValue(), studentIds);
        }

        // 4. Write back to the same file
        try (FileOutputStream fos = new FileOutputStream(path)) {
            wb.write(fos);
        }

        System.out.println("Successfully updated quiz entry sheets in: " + path);
    }

    private List<Question> readQuizBlock(Sheet sheet, int colQNum, int colMaxMark) {
        List<Question> questions = new ArrayList<>();
        for (int r = 6; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell qCell = row.getCell(colQNum);
            Cell mCell = row.getCell(colMaxMark);
            if (qCell != null && mCell != null) {
                String qNum = null;

                if (qCell.getCellType() == CellType.STRING) {
                    qNum = qCell.getStringCellValue().trim();
                } else if (qCell.getCellType() == CellType.NUMERIC) {
                    qNum = String.valueOf((long) qCell.getNumericCellValue());
                }

                if (qNum != null && !qNum.isBlank()) {
                    int max = (int) mCell.getNumericCellValue();
                    questions.add(new Question(qNum, max));
                }
            }
        }
        return questions;
    }

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
