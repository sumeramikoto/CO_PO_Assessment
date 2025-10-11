package org.example.co_po_assessment.admin_input_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class StudentInfoInputController implements Initializable {
    @FXML
    Label headLabel;
    @FXML
    TextField idTextField;
    @FXML
    TextField nameTextField;
    @FXML
    TextField batchTextField;
    @FXML
    TextField emailTextField;
    @FXML
    TextField departmentTextField;
    @FXML
    TextField programmeTextField;
    @FXML
    Button confirmButton;
    @FXML
    Button backButton;

    private ManageStudentsController parentController;

    // Regex patterns according to requirements
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{9}$");
    private static final Pattern BATCH_PATTERN = Pattern.compile("^\\d{2}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.edu$");
    private static final Pattern DEPT_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern PROGRAMME_PATTERN = Pattern.compile("^(?:BSc|MSc|PhD) in [A-Z]{2,3}$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the form labels and initial state
        headLabel.setText("Add Student Information");
    }

    /**
     * Set the parent controller to enable communication back to the student management window
     */
    public void setParentController(ManageStudentsController parentController) {
        this.parentController = parentController;
    }

    public void onConfirmButton(ActionEvent event) {
        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Get input values
        String id = idTextField.getText().trim();
        String name = nameTextField.getText().trim();
        String batch = batchTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String department = departmentTextField.getText().trim();
        String programme = programmeTextField.getText().trim();

        try {
            // Call parent controller to add the student
            if (parentController != null) {
                parentController.addNewStudent(id, name, batch, email, department, programme);
            }

            // Close the current window
            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to add student: " + e.getMessage());
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

        // Validate ID: exactly 9 digits
        String id = idTextField.getText().trim();
        if (id.isEmpty()) {
            errors.append("Student ID is required.\n");
        } else if (!ID_PATTERN.matcher(id).matches()) {
            errors.append("Student ID must be exactly 9 digits, e.g., 220042101.\n");
        }

        // Validate name: non-empty
        String name = nameTextField.getText().trim();
        if (name.isEmpty()) {
            errors.append("Student name is required.\n");
        }

        // Validate batch: exactly two digits
        String batchText = batchTextField.getText().trim();
        if (batchText.isEmpty()) {
            errors.append("Batch is required.\n");
        } else if (!BATCH_PATTERN.matcher(batchText).matches()) {
            errors.append("Batch must be exactly 2 digits, e.g., 22.\n");
        }

        // Validate email: must look like yourname@institution.edu
        String email = emailTextField.getText().trim();
        if (email.isEmpty()) {
            errors.append("Email is required.\n");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.append("Email must be like yourname@institution.edu and end with .edu.\n");
        }

        // Validate department: 3 uppercase letters
        String dept = departmentTextField.getText().trim();
        if (dept.isEmpty()) {
            errors.append("Department is required.\n");
        } else if (!DEPT_PATTERN.matcher(dept).matches()) {
            errors.append("Department must be 3 uppercase letters, e.g., CSE.\n");
        }

        // Validate programme: (BSc|MSc|PhD) in [A-Z]{2,3}
        String programme = programmeTextField.getText().trim();
        if (programme.isEmpty()) {
            errors.append("Programme is required.\n");
        } else if (!PROGRAMME_PATTERN.matcher(programme).matches()) {
            errors.append("Programme must be 'BSc in XX/XXX', 'MSc in XX/XXX', or 'PhD in XX/XXX'.\n");
        }

        if (errors.length() > 0) {
            showErrorAlert("Validation Errors", errors.toString());
            return false;
        }

        return true;
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
