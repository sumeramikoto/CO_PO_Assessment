package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;

abstract class AbstractQuestionInfoTemplate {
    protected final Object[][] courseInfo = {
            {"Course Code", "=StudentInfo!$C$2", null, null, "Instructor", "=StudentInfo!$C$1", null, null, "Program", "=StudentInfo!$C$8", null, null, "Credit", "=StudentInfo!$C$4", null, null},
            {"Course Title", "=StudentInfo!$C$3", null, null, "Academic Year", "=StudentInfo!$C$7", null, null, "Department", "=StudentInfo!$C$9", null, null, "Total Students", "=StudentInfo!$C$5", null, null}
    };

    protected void checkAndSetCellValue(Object value, XSSFCell cell) {
        if (value instanceof String strVal) {
            if (strVal.startsWith("=")) {
                cell.setCellFormula(strVal.substring(1));
            } else {
                cell.setCellValue(strVal);
            }
        } else if (value instanceof Double d) {
            cell.setCellValue(d);
        } else if (value instanceof Integer i) {
            cell.setCellValue(i);
        } else if (value == null) {
            cell.setBlank();
        } else {
            cell.setCellValue(value.toString());
        }
    }

    protected void addDropdown(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol, String[] options) {
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidation validation = validationHelper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    abstract void createSheet(XSSFSheet sheet);
}
