package org.example.co_po_assessment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageStudentsController implements Initializable {
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

    private ObservableList<Student> studentList;
    private StudentDatabaseHelper studentDatabaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentDatabaseHelper = new StudentDatabaseHelper();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        batchColumn.setCellValueFactory(new PropertyValueFactory<>("batch"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Initialize student list
        studentList = FXCollections.observableArrayList();
        studentTableView.setItems(studentList);

        // Load existing student data
        loadStudentData();
    }

    public void onAddStudentButton(ActionEvent actionEvent) {
        // TODO: Implement student addition when student input form is available
        showInfoAlert("Add Student", "Student addition functionality will be implemented soon.");
    }

    public void onRemoveStudentButton(ActionEvent actionEvent) {
        Student selectedStudent = studentTableView.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showWarningAlert("No Selection", "Please select a student to remove.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Remove Student");
        confirmAlert.setContentText("Are you sure you want to remove student: " + selectedStudent.getName() + " (ID: " + selectedStudent.getId() + ")?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Remove from database
                studentDatabaseHelper.removeStudent(selectedStudent.getId());

                // Remove from table
                studentList.remove(selectedStudent);

                showInfoAlert("Success", "Student removed successfully.");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove student: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        // Close the current window
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Load student data from database
     */
    private void loadStudentData() {
        try {
            // Get all students from database
            var studentData = studentDatabaseHelper.getAllStudents();

            studentList.clear();
            for (var student : studentData) {
                studentList.add(new Student(
                    student.getId(),
                    student.getName(),
                    String.valueOf(student.getBatch()),
                    student.getDepartment(),
                    student.getProgramme(),
                    student.getEmail()
                ));
            }

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load student data: " + e.getMessage());
        }
    }

    /**
     * Helper method to show error alerts
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show warning alerts
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show information alerts
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
