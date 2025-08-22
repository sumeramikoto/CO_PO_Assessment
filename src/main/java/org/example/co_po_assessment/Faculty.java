package org.example.co_po_assessment;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Faculty {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty shortname;
    private final StringProperty email;

    public Faculty(String id, String name, String shortname, String email) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.shortname = new SimpleStringProperty(shortname);
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

    public String getShortname() {
        return shortname.get();
    }

    public StringProperty shortnameProperty() {
        return shortname;
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }
}
