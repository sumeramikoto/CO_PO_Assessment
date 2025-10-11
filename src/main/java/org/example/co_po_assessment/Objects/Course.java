package org.example.co_po_assessment.Objects;

public class Course {
    private String code;
    private String title;
    private String instructor;
    private String academicYear;
    private double credit;
    private String programme; // renamed from program
    private String department;

    public Course(String code, String title, String instructor, String academicYear,
                  double credit, String programme, String department) {
        this.code = code;
        this.title = title;
        this.instructor = instructor;
        this.academicYear = academicYear;
        this.credit = credit;
        this.programme = programme;
        this.department = department;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getInstructor() { return instructor; }
    public String getAcademicYear() { return academicYear; }
    public double getCredit() { return credit; }
    public String getProgramme() { return programme; }
    // Backward compatibility
    public String getProgram() { return programme; }
    public String getDepartment() { return department; }

    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public void setCredit(double credit) { this.credit = credit; }
    public void setProgramme(String programme) { this.programme = programme; }
    public void setProgram(String programme) { this.programme = programme; }
    public void setDepartment(String department) { this.department = department; }
}
