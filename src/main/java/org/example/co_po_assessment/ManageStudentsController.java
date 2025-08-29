package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ManageStudentsController {
    @FXML
    TableView<Student> studentTableView;
    @FXML
    TableColumn<Student, String> idColumn;
    @FXML
    TableColumn<Student, String> nameColumn;
    @FXML
    TableColumn<Student, String> batchColumn;
    @FXML
    TableColumn<Student, String> departmentColumn;
    @FXML
    TableColumn<Student, String> programmeColumn;
    @FXML
    TableColumn<Student, String> emailColumn;
    @FXML
    Button addStudentButton;
    @FXML
    Button removeStudentButton;
    @FXML
    Button backButton;

    public void onAddStudentButton(ActionEvent actionEvent) {
        // opens new window to add student info
    }

    public void onRemoveStudentButton(ActionEvent actionEvent) {
        // removes selected student
    }

    public void onBackButton(ActionEvent actionEvent) {
        // takes back to admin dashboard
    }
}
