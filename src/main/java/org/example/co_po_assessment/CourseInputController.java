package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CourseInputController implements Initializable {
    @FXML
    Label headLabel;
    @FXML
    TextField courseCodeTextField;
    @FXML
    TextField courseNameTextField;
    @FXML
    TextField creditsTextField;
    @FXML
    TextField departmentTextField;
    @FXML
    TextField programmeTextField;
    @FXML
    Button confirmButton;
    @FXML
    Button backButton;

    private ManageCoursesController parentController;
    private CoursesDatabaseHelper databaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseHelper = new CoursesDatabaseHelper();
    }

    /**
     * Set the parent controller to enable communication back to the main window
     */
    public void setParentController(ManageCoursesController parentController) {
        this.parentController = parentController;
    }

    public void onConfirmButton(ActionEvent event) {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Get input values
        String courseCode = courseCodeTextField.getText().trim().toUpperCase();
        String courseName = courseNameTextField.getText().trim();
        double credits = Double.parseDouble(creditsTextField.getText().trim());
        String department = departmentTextField.getText().trim().toUpperCase();
        String programme = programmeTextField.getText().trim().toUpperCase();

        try {
            // Call parent controller to add the course
            if (parentController != null) {
                parentController.addNewCourse(courseCode, courseName, credits, department, programme);
            }

            // Close the current window
            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to add course: " + e.getMessage());
        }
    }

    public void onBackButton(ActionEvent event) {
        // Close the current window without saving
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Validate all input fields
     */
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Validate course code
        String courseCode = courseCodeTextField.getText().trim();
        if (courseCode.isEmpty()) {
            errors.append("Course code is required.\n");
        } else if (!isValidCourseCode(courseCode)) {
            errors.append("Course code must be alphanumeric (3-8 chars).\n");
        }

        // Validate course name
        String courseName = courseNameTextField.getText().trim();
        if (courseName.isEmpty()) {
            errors.append("Course name is required.\n");
        } else if (courseName.length() < 3) {
            errors.append("Course name must be at least 3 characters long.\n");
        }

        // Validate credits
        String creditsText = creditsTextField.getText().trim();
        if (creditsText.isEmpty()) {
            errors.append("Credits is required.\n");
        } else {
            try {
                double credits = Double.parseDouble(creditsText);
                if (credits <= 0 || credits > 10) {
                    errors.append("Credits must be between 0.1 and 10.0.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Credits must be a valid number.\n");
            }
        }

        // Validate department
        String department = departmentTextField.getText().trim();
        if (department.isEmpty()) {
            errors.append("Department is required.\n");
        } else if (!department.matches("[A-Za-z]{2,3}")) {
            errors.append("Department must be 2-3 letters.\n");
        }

        // Validate programme
        String programme = programmeTextField.getText().trim();
        if (programme.isEmpty()) {
            errors.append("Programme is required.\n");
        } else if (programme.length() > 11) {
            errors.append("Programme max length is 11.\n");
        }

        // Check if course code already exists
        try {
            if (!courseCode.isEmpty() && databaseHelper.courseExists(courseCode.toUpperCase())) {
                errors.append("Course code already exists. Please use a different code.\n");
            }
        } catch (Exception e) {
            errors.append("Database error while checking course code.\n");
        }

        if (errors.length() > 0) {
            showErrorAlert("Validation Errors", errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Validate course code format
     */
    private boolean isValidCourseCode(String courseCode) {
        // Course code should be alphanumeric, typically 3-8 characters
        return courseCode.matches("[A-Za-z0-9]{3,8}");
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
