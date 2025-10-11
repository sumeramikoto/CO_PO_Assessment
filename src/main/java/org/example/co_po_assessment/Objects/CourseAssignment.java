package org.example.co_po_assessment.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CourseAssignment {
    private final StringProperty courseCode;
    private final StringProperty courseName;
    private final StringProperty facultyName;
    private final StringProperty academicYear;
    private final StringProperty department;
    private final StringProperty programme;

    public CourseAssignment(String courseCode, String courseName, String facultyName, String academicYear,
                            String department, String programme) {
        this.courseCode = new SimpleStringProperty(courseCode);
        this.courseName = new SimpleStringProperty(courseName);
        this.facultyName = new SimpleStringProperty(facultyName);
        this.academicYear = new SimpleStringProperty(academicYear);
        this.department = new SimpleStringProperty(department);
        this.programme = new SimpleStringProperty(programme);
    }

    public String getCourseCode() {
        return courseCode.get();
    }

    public StringProperty courseCodeProperty() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName.get();
    }

    public StringProperty courseNameProperty() {
        return courseName;
    }

    public String getFacultyName() {
        return facultyName.get();
    }

    public StringProperty facultyNameProperty() {
        return facultyName;
    }

    public String getAcademicYear() {
        return academicYear.get();
    }

    public StringProperty academicYearProperty() {
        return academicYear;
    }

    public String getDepartment() {
        return department.get();
    }

    public StringProperty departmentProperty() {
        return department;
    }

    public String getProgramme() {
        return programme.get();
    }

    public StringProperty programmeProperty() {
        return programme;
    }
}
