package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StudentInfoSheetTemplate {
    private final String instructor;
    private final String courseCode;
    private final String courseTitle;
    private final double credit;
    private final int totalStudents;
    private final double POThreshold;
    private final String academicYear;
    private final String program;
    private final String department;
    private Object[][] courseInfo;
    private String[][] studentInfo;

    public StudentInfoSheetTemplate(String instructor, String courseCode, String courseTitle, double credit, int totalStudents, double POThreshold, String academicYear, String program, String department) {
        this.instructor = instructor;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credit = credit;
        this.totalStudents = totalStudents;
        this.POThreshold = POThreshold;
        this.academicYear = academicYear;
        this.program = program;
        this.department = department;
        setCourseInfo();
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

    public double getPOThreshold() {
        return POThreshold;
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

    public void setCourseInfo() {
        this.courseInfo = new Object[][]{
                {"Instructor", getInstructor()},
                {"Course Code", getCourseCode()},
                {"Course Title", getCourseTitle()},
                {"Credit", getCredit()},
                {"Total Students", getTotalStudents()},
                {"PO Threshold", getPOThreshold()},
                {"Academic Year", getAcademicYear()},
                {"Program", getProgram()},
                {"Department", getDepartment()},
        };
    }

    public void createSheet(File excelFile) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("StudentInfo");

            int courseInfoRows = courseInfo.length;
            int courseInfoCols = courseInfo[0].length;
            for (int r = 0; r < courseInfoRows; r++) {
                XSSFRow row = sheet.createRow(r);
                for (int c = 0; c < courseInfoCols; c++) {
                    XSSFCell cell = row.createCell(c);
                    Object courseCellValue = courseInfo[r][c];
                    if (courseCellValue instanceof String) {
                        cell.setCellValue((String) courseCellValue);
                    } else if (courseCellValue instanceof Integer) {
                        cell.setCellValue((Integer) courseCellValue);
                    } else if (courseCellValue instanceof Double) {
                        cell.setCellValue((Double) courseCellValue);
                    }
                }

                // sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 1));
                // sheet.addMergedRegion(new CellRangeAddress(r, r, 2, 3));

            }
            FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
