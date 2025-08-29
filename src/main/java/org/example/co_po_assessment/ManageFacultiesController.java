package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ManageFacultiesController {
    @FXML
    TableView<Faculty> facultyTableView;
    @FXML
    TableColumn<Faculty, String> idColumn;
    @FXML
    TableColumn<Faculty, String> nameColumn;
    @FXML
    TableColumn<Faculty, String> shortnameColumn;
    @FXML
    TableColumn<Faculty, String> emailColumn;
    @FXML
    Button addFacultyButton;
    @FXML
    Button removeFacultyButton;
    @FXML
    Button backButton;

    public void onAddFacultyButton(ActionEvent event) {
        // opens FacultyInfoInputView from which faculty info is added
    }

    public void onRemoveFacultyButton(ActionEvent event) {
        // removes the faculty from the system which is selected in the list
    }

    public void onBackButton(ActionEvent event) {
        // takes you back to admin dashboard
    }
}
