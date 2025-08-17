package org.example.co_po_assessment;

import java.util.HashMap;
import java.util.Map;

public class StudentMark {
    private String studentId;
    private String assessmentType;
    private Map<String, Double> questionMarks; // questionNumber -> mark
    private double total;

    public StudentMark(String studentId, String assessmentType) {
        this.studentId = studentId;
        this.assessmentType = assessmentType;
        this.questionMarks = new HashMap<>();
    }

    // Getters and setters
    public String getStudentId() { return studentId; }
    public String getAssessmentType() { return assessmentType; }
    public Map<String, Double> getQuestionMarks() { return questionMarks; }
    public double getTotal() { return total; }

    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setAssessmentType(String assessmentType) { this.assessmentType = assessmentType; }
    public void addQuestionMark(String questionNumber, double mark) {
        questionMarks.put(questionNumber, mark);
        calculateTotal();
    }

    private void calculateTotal() {
        total = questionMarks.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}