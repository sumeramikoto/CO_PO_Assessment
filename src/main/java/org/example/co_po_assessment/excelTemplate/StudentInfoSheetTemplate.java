package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class StudentInfoSheetTemplate {
    private final String instructor;
    private final String courseCode;
    private final String courseTitle;
    private final double credit;
    private final int totalStudents;
    private final String academicYear;
    private final String program;
    private final String department;
    private final Object[][] courseInfo;
    private final String[][] studentInfo;

    public StudentInfoSheetTemplate(String instructor, String courseCode, String courseTitle, double credit, int totalStudents, double POThreshold, String academicYear, String program, String department, Object[][] courseInfo, String[][] studentInfo) {
        this.instructor = instructor;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credit = credit;
        this.totalStudents = totalStudents;
        this.academicYear = academicYear;
        this.program = program;
        this.department = department;
        this.courseInfo = courseInfo;
        this.studentInfo = studentInfo;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public double getCredit() {
        return credit;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public String getProgram() {
        return program;
    }

    public String getDepartment() {
        return department;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    private void checkAndSetCellValue(Object value, XSSFCell cell) {
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

    public void generateSheet(XSSFSheet sheet) {
        int courseInfoRows = courseInfo.length;
        int courseInfoCols = courseInfo[0].length;
        for (int r = 0; r < courseInfoRows; r++) {
            XSSFRow row = sheet.createRow(r);
            for (int c = 0; c < courseInfoCols; c++) {
                XSSFCell cell = row.createCell(c);
                Object courseCellValue = courseInfo[r][c];
                checkAndSetCellValue(courseCellValue, cell);
            }
            sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(r, r, 2, 3));
        }

        int studentInfoRowsIndex = courseInfoRows + 2;
        XSSFRow studentInfoHeaderRow = sheet.createRow(studentInfoRowsIndex);
        String[] studentInfoHeader = {"Student ID", "Name", "Email", "Contact No."};
        int studentInfoCols = studentInfoHeader.length;
        for (int c = 0; c < studentInfoCols; c++) {
            XSSFCell cell = studentInfoHeaderRow.createCell(c);
            String headerValue = studentInfoHeader[c];
            checkAndSetCellValue(headerValue, cell);
        }
        studentInfoRowsIndex++;

        for (int r = 0; studentInfoRowsIndex < studentInfoRowsIndex + totalStudents && r < totalStudents; studentInfoRowsIndex++, r++) {
            XSSFRow row = sheet.createRow(studentInfoRowsIndex);
            for (int c = 0; c < studentInfoCols; c++) {
                row.createCell(c).setCellValue(studentInfo[r][c]);
            }
        }
    }
}
