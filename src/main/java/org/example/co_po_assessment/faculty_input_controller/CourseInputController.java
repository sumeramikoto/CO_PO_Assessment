package org.example.co_po_assessment.faculty_input_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.CoursesDatabaseHelper;
import org.example.co_po_assessment.admin_input_controller.ManageCoursesController;

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
        // Normalize course code input (collapse multiple spaces, uppercase) before validation
        if (courseCodeTextField.getText() != null) {
            String normalized = courseCodeTextField.getText().trim().replaceAll("\\s+", " ").toUpperCase();
            courseCodeTextField.setText(normalized);
        }

        // NOTE: We intentionally do NOT auto-normalize programme beyond trimming, to enforce explicit user entry format.

        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Get input values (course code already normalized above)
        String courseCode = courseCodeTextField.getText().trim();
        String courseName = courseNameTextField.getText().trim();
        double credits = Double.parseDouble(creditsTextField.getText().trim());
        String department = departmentTextField.getText().trim().toUpperCase();
        String programme = programmeTextField.getText().trim();

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

        // Validate course code (normalize for validation)
        String courseCode = courseCodeTextField.getText() == null ? "" : courseCodeTextField.getText().trim().replaceAll("\\s+", " ");
        if (courseCode.isEmpty()) {
            errors.append("Course code is required.\n");
        } else if (!isValidCourseCode(courseCode)) {
            errors.append("Course code must be in the format ABC 1234 or ABCD 1234 (three or four letters, space, four digits).\n");
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

        // Validate programme (strict format e.g., BSc in SWE, MSc in CSE, PhD in CS, BBA in FIN)
        String programme = programmeTextField.getText().trim();
        if (programme.isEmpty()) {
            errors.append("Programme is required.\n");
        } else if (!isValidProgramme(programme)) {
            errors.append("Programme must match the format: BSc in XX, BSc in XXX, MSc in XX/XXX, PhD in XX/XXX, or BBA in XX/XXX (where the last part is 2 or 3 uppercase letters).\n");
        }

        // Check if course (code + programme) already exists
        try {
            if (!courseCode.isEmpty() && !programme.isEmpty() && databaseHelper.courseExists(courseCode.toUpperCase(), programme)) {
                errors.append("A course with this code already exists for the selected programme.\n");
            }
        } catch (Exception e) {
            errors.append("Database error while checking course code/programme combination.\n");
        }

        if (errors.length() > 0) {
            showErrorAlert("Validation Errors", errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Validate course code format.
     * Expected format: Three OR four letters, a single space, four digits (e.g., SWE 4402 or MATH 4141).
     */
    private boolean isValidCourseCode(String courseCode) {
        if (courseCode == null) return false;
        String normalized = courseCode.trim().replaceAll("\\s+", " ").toUpperCase();
        return normalized.matches("[A-Z]{3,4} [0-9]{4}");
    }

    /**
     * Validate programme format.
     * Expected format: (BSc|MSc|PhD|BBA) in XX or XXX where X are uppercase letters.
     * Examples: BSc in SWE, MSc in CSE, PhD in CS, BBA in FIN
     */
    private boolean isValidProgramme(String programme) {
        if (programme == null) return false;
        String trimmed = programme.trim().replaceAll("\\s+", " ");
        return trimmed.matches("(BSc|MSc|PhD|BBA) in [A-Z]{2,3}");
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
