package org.example.co_po_assessment;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class StudentMarks {
    private final SimpleStringProperty studentId;
    private final SimpleDoubleProperty marks;

    public StudentMarks(String studentId, double marks) {
        this.studentId = new SimpleStringProperty(studentId);
        this.marks = new SimpleDoubleProperty(marks);
    }

    public String getStudentId() {
        return studentId.get();
    }

    public SimpleStringProperty studentIdProperty() {
        return studentId;
    }

    public double getMarks() {
        return marks.get();
    }

    public SimpleDoubleProperty marksProperty() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks.set(marks);
    }
}
