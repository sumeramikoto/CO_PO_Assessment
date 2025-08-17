package org.example.co_po_assessment;

public class Course {
    private String code;
    private String title;
    private String instructor;
    private String academicYear;
    private double credit;
    private String program;
    private String department;

    public Course(String code, String title, String instructor, String academicYear,
                  double credit, String program, String department) {
        this.code = code;
        this.title = title;
        this.instructor = instructor;
        this.academicYear = academicYear;
        this.credit = credit;
        this.program = program;
        this.department = department;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getInstructor() { return instructor; }
    public String getAcademicYear() { return academicYear; }
    public double getCredit() { return credit; }
    public String getProgram() { return program; }
    public String getDepartment() { return department; }

    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setCredit(double credit) { this.credit = credit; }
    public void setProgram(String program) { this.program = program; }
    public void setDepartment(String department) { this.department = department; }
}
