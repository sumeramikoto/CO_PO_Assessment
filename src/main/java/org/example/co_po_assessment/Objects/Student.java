package org.example.co_po_assessment.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Student {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty batch;
    private final StringProperty department;
    private final StringProperty programme;
    private final StringProperty email;

    public Student(String id, String name, String batch, String department, String programme, String email) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.batch = new SimpleStringProperty(batch);
        this.department = new SimpleStringProperty(department);
        this.programme = new SimpleStringProperty(programme);
        this.email = new SimpleStringProperty(email);
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getBatch() {
        return batch.get();
    }

    public StringProperty batchProperty() {
        return batch;
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

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }
}