package org.example.co_po_assessment;

public class AssessmentQuestion {
    private Integer id; // database primary key (question table row id)
    private String number;
    private double marks;
    private String co;
    private String po;
    private String assessmentType;

    // Existing constructor retained (id unknown yet)
    public AssessmentQuestion(String number, double marks, String co, String po, String assessmentType) {
        this(null, number, marks, co, po, assessmentType);
    }

    // New constructor including database id
    public AssessmentQuestion(Integer id, String number, double marks, String co, String po, String assessmentType) {
        this.id = id; // may be null if not persisted yet
        this.number = number;
        this.marks = marks;
        this.co = co;
        this.po = po;
        this.assessmentType = assessmentType;
    }

    public Integer getId() { return id; }
    public String getNumber() { return number; }
    public double getMarks() { return marks; }
    public String getCo() { return co; }
    public String getPo() { return po; }
    public String getAssessmentType() { return assessmentType; }

    public void setId(Integer id) { this.id = id; }
    public void setNumber(String number) { this.number = number; }
    public void setMarks(double marks) { this.marks = marks; }
    public void setCo(String co) { this.co = co; }
    public void setPo(String po) { this.po = po; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }
}