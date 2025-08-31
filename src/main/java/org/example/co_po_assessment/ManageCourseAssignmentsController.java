package org.example.co_po_assessment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageCourseAssignmentsController implements Initializable {
    @FXML
    TableView<CourseAssignment> courseTableView;
    @FXML
    TableColumn<CourseAssignment, String> courseCodeColumn;
    @FXML
    TableColumn<CourseAssignment, String> courseNameColumn;
    @FXML
    TableColumn<CourseAssignment, String> facultyColumn;
    @FXML
    TableColumn<CourseAssignment, String> academicYearColumn;
    @FXML
    TableColumn<CourseAssignment, String> departmentColumn;
    @FXML
    TableColumn<CourseAssignment, String> programmeColumn;
    @FXML
    Button assignCourseButton;
    @FXML
    Button removeCourseButton;
    @FXML
    Button backButton;

    private ObservableList<CourseAssignment> courseAssignmentList;
    private CourseAssignmentDatabaseHelper databaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseHelper = new CourseAssignmentDatabaseHelper();

        // Set up table columns
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        facultyColumn.setCellValueFactory(new PropertyValueFactory<>("facultyName"));
        academicYearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        if (departmentColumn != null) {
            departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        }
        if (programmeColumn != null) {
            programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        }

        // Initialize course assignment list
        courseAssignmentList = FXCollections.observableArrayList();
        courseTableView.setItems(courseAssignmentList);

        // Load existing course assignment data
        loadCourseAssignmentData();
    }

    public void onAssignCourseButton(ActionEvent actionEvent) {
        try {
            // Open the Course Assignment Input window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("courseAssignmentInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 450, 350);

            // Get the controller to handle data return
            CourseAssignmentInputController controller = fxmlLoader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Assign Course to Faculty");
            stage.setScene(scene);
            stage.showAndWait(); // Wait for the window to close before continuing

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Assign Course window: " + e.getMessage());
        }
    }

    public void onRemoveCourseButton(ActionEvent actionEvent) {
        CourseAssignment selectedAssignment = courseTableView.getSelectionModel().getSelectedItem();

        if (selectedAssignment == null) {
            showWarningAlert("No Selection", "Please select a course assignment to remove.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Course Assignment");
        confirmAlert.setContentText("Are you sure you want to remove the assignment:\n" +
                "Course: " + selectedAssignment.getCourseCode() + " - " + selectedAssignment.getCourseName() + "\n" +
                "Faculty: " + selectedAssignment.getFacultyName() + "\n" +
                "Academic Year: " + selectedAssignment.getAcademicYear() + "\n" +
                "Department: " + selectedAssignment.getDepartment() + "\n" +
                "Programme: " + selectedAssignment.getProgramme());

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Remove from database
                databaseHelper.removeCourseAssignment(
                    selectedAssignment.getCourseCode(),
                    selectedAssignment.getFacultyName(),
                    selectedAssignment.getAcademicYear()
                );

                // Remove from table
                courseAssignmentList.remove(selectedAssignment);

                showInfoAlert("Success", "Course assignment removed successfully.");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove course assignment: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        // Close the current window
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Method to be called by CourseAssignmentInputController when new assignment is added
     */
    public void addNewCourseAssignment(String courseCode, String facultyName, String academicYear) {
        try {
            // Add to database
            databaseHelper.assignCourse(courseCode, facultyName, academicYear);

            // Reload data to get the course name
            loadCourseAssignmentData();

            showInfoAlert("Success", "Course assigned successfully.");

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showErrorAlert("Assignment Error", "This course is already assigned to the selected faculty for that academic year.");
            } else {
                showErrorAlert("Database Error", "Failed to assign course: " + e.getMessage());
            }
        }
    }

    /**
     * Load course assignment data from database
     */
    private void loadCourseAssignmentData() {
        try {
            // Get all course assignments from database
            var assignmentData = databaseHelper.getAllCourseAssignments();

            courseAssignmentList.clear();
            for (var assignment : assignmentData) {
                courseAssignmentList.add(new CourseAssignment(
                    assignment.getCourseCode(),
                    assignment.getCourseName(),
                    assignment.getFacultyName(),
                    assignment.getAcademicYear(),
                    assignment.getDepartment(),
                    assignment.getProgramme()
                ));
            }

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load course assignment data: " + e.getMessage());
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
