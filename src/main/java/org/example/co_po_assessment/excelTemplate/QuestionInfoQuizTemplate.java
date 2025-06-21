package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class QuestionInfoQuizTemplate extends AbstractQuestionInfoTemplate {
    private final String[][] quizHeader = {
            {"Quiz 1", null, null, null, "Quiz 2", null, null, null, "Quiz 3", null, null, null, "Quiz 4", null, null, null},
            {"Question", "Marks", "CO", "PO", "Question", "Marks", "CO", "PO", "Question", "Marks", "CO", "PO", "Question", "Marks", "CO", "PO"}
    };

    @Override
    public void createSheet(XSSFSheet sheet) {
        for (int r = 0; r < courseInfo.length; r++) {
            XSSFRow row = sheet.createRow(r);
            for (int c = 0; c < courseInfo[0].length; c++) {
                XSSFCell cell = row.createCell(c);
                Object courseCellValue = courseInfo[r][c];
                checkAndSetCellValue(courseCellValue, cell);
            }
            sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(r, r, 5, 7));
            sheet.addMergedRegion(new CellRangeAddress(r, r, 9, 11));
            sheet.addMergedRegion(new CellRangeAddress(r, r, 13, 15));
        }
        for (int sheetRow = 4, r = 0; r < quizHeader.length; sheetRow++, r++) {
            XSSFRow row = sheet.createRow(sheetRow);
            for (int c = 0; c < quizHeader[0].length; c++) {
                XSSFCell cell = row.createCell(c);
                Object quizHeaderValue = quizHeader[r][c];
                checkAndSetCellValue(quizHeaderValue, cell);
            }
        }

        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 3));
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 4, 7));
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 8, 11));
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 12, 15));

        addDropdown(sheet, 6, 66, 2, 2, coOptions);
        addDropdown(sheet, 6, 66, 6, 6, coOptions);
        addDropdown(sheet, 6, 66, 10, 10, coOptions);
        addDropdown(sheet, 6, 66, 14, 14, coOptions);

        addDropdown(sheet, 6, 66, 3, 3, poOptions);
        addDropdown(sheet, 6, 66, 7, 7, poOptions);
        addDropdown(sheet, 6, 66, 11, 11, poOptions);
        addDropdown(sheet, 6, 66, 15, 15, poOptions);
    }
}
