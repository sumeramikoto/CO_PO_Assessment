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
    }

    public void onRemoveStudentButton(ActionEvent actionEvent) {
    }

    public void onBackButton(ActionEvent actionEvent) {
    }
}
