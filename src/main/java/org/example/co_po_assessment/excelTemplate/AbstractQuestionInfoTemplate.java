package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

abstract class AbstractQuestionInfoTemplate {
    protected final Object[][] courseInfo = {
            {"Course Code", "=StudentInfo!$C$2", null, null, "Instructor", "=StudentInfo!$C$1", null, null, "Program", "=StudentInfo!$C$8", null, null, "Credit", "=StudentInfo!$C$4", null, null},
            {"Course Title", "=StudentInfo!$C$3", null, null, "Academic Year", "=StudentInfo!$C$7", null, null, "Department", "=StudentInfo!$C$8", null, null, "Total Students", "=StudentInfo!$C$5", null, null}
    };

    protected String[] coOptions = {"C01", "C02", "C03", "C04", "C05", "C06", "C07", "C08", "C09", "C010", "C011", "C012", "C013", "C014", "C015", "C016", "C017", "C018", "C019", "C020"};
    protected String[] poOptions = {"P01", "P02", "P03", "P04", "P05", "P06", "P07", "P08", "P09", "P010", "P011", "P012"};

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

    abstract void generateSheet(XSSFSheet sheet);
}
