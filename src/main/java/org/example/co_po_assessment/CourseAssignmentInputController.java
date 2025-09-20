package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CourseAssignmentInputController implements Initializable {
    @FXML
    Label headLabel;
    @FXML
    ComboBox<String> courseComboBox;
    @FXML
    ComboBox<String> facultyComboBox;
    @FXML
    TextField academicYearTextField;
    @FXML
    Button confirmButton;
    @FXML
    Button backButton;

    private ManageCourseAssignmentsController parentController;
    private CourseAssignmentDatabaseHelper databaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseHelper = new CourseAssignmentDatabaseHelper();
        loadDropdownData();
    }

    /**
     * Set the parent controller to enable communication back to the main window
     */
    public void setParentController(ManageCourseAssignmentsController parentController) {
        this.parentController = parentController;
    }

    public void onConfirmButton(ActionEvent event) {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Get input values
        String selectedCourse = courseComboBox.getValue();
        // Robust parsing: pattern ends with " - DEPT - PROGRAMME"; programme after last delimiter; code before first delimiter
        int lastSep = selectedCourse.lastIndexOf(" - ");
        String programme = selectedCourse.substring(lastSep + 3);
        String beforeLast = selectedCourse.substring(0, lastSep);
        int firstSep = beforeLast.indexOf(" - ");
        String courseCode = beforeLast.substring(0, firstSep);
        String facultyName = facultyComboBox.getValue();
        String academicYear = academicYearTextField.getText().trim();

        try {
            // Call parent controller to add the assignment
            if (parentController != null) {
                parentController.addNewCourseAssignment(courseCode, programme, facultyName, academicYear);
            }

            // Close the current window
            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to assign course: " + e.getMessage());
        }
    }

    public void onBackButton(ActionEvent event) {
        // Close the current window without saving
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Load data for dropdown menus
     */
    private void loadDropdownData() {
        try {
            // Load courses
            var courses = databaseHelper.getAllCourses();
            courseComboBox.getItems().addAll(courses);

            // Load faculty
            var faculty = databaseHelper.getAllFaculty();
            facultyComboBox.getItems().addAll(faculty);

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load dropdown data: " + e.getMessage());
        }
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Validate course selection
        if (courseComboBox.getValue() == null || courseComboBox.getValue().isEmpty()) {
            errors.append("Please select a course.\n");
        }

        // Validate faculty selection
        if (facultyComboBox.getValue() == null || facultyComboBox.getValue().isEmpty()) {
            errors.append("Please select a faculty member.\n");
        }

        // Validate academic year
        String academicYear = academicYearTextField.getText().trim();
        if (academicYear.isEmpty()) {
            errors.append("Academic year is required.\n");
        } else if (!isValidAcademicYear(academicYear)) {
            errors.append("Academic year must be in format YYYY-YYYY (e.g., 2024-2025).\n");
        }

        if (errors.length() > 0) {
            showErrorAlert("Validation Errors", errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Validate academic year format
     */
    private boolean isValidAcademicYear(String academicYear) {
        return academicYear.matches("\\d{4}-\\d{4}");
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
}
