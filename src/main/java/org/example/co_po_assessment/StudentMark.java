package org.example.co_po_assessment;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.Map;

public class StudentMark {
    private final StringProperty studentId;
    private final StringProperty assessmentType;
    private Map<String, Double> questionMarks;
    private final DoubleProperty total;

    public StudentMark(String studentId, String assessmentType) {
        this.studentId = new SimpleStringProperty(studentId);
        this.assessmentType = new SimpleStringProperty(assessmentType);
        this.questionMarks = new HashMap<>();
        this.total = new SimpleDoubleProperty(0);
    }

    public StringProperty studentIdProperty() { return studentId; }
    public StringProperty assessmentTypeProperty() { return assessmentType; }
    public DoubleProperty totalProperty() { return total; }

    public String getStudentId() { return studentId.get(); }
    public String getAssessmentType() { return assessmentType.get(); }
    public Map<String, Double> getQuestionMarks() { return questionMarks; }
    public double getTotal() { return total.get(); }

    public void setStudentId(String studentId) { this.studentId.set(studentId); }
    public void setAssessmentType(String assessmentType) { this.assessmentType.set(assessmentType); }
    public void addQuestionMark(String questionNumber, double mark) {
        questionMarks.put(questionNumber, mark);
        calculateTotal();
    }

    private void calculateTotal() {
        total.set(questionMarks.values().stream().mapToDouble(Double::doubleValue).sum());
    }
}