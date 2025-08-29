package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

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

        // Validate ID
        if (idTextField.getText().trim().isEmpty()) {
            errors.append("Student ID is required.\n");
        }

        // Validate name
        if (nameTextField.getText().trim().isEmpty()) {
            errors.append("Student name is required.\n");
        }

        // Validate batch
        String batchText = batchTextField.getText().trim();
        if (batchText.isEmpty()) {
            errors.append("Batch is required.\n");
        } else {
            try {
                int batch = Integer.parseInt(batchText);
                if (batch < 0 || batch > 99) {
                    errors.append("Batch must be a valid number between 0 and 99.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Batch must be a valid number.\n");
            }
        }

        // Validate email
        String email = emailTextField.getText().trim();
        if (email.isEmpty()) {
            errors.append("Email is required.\n");
        } else if (!isValidEmail(email)) {
            errors.append("Please enter a valid email address.\n");
        }

        // Validate department
        if (departmentTextField.getText().trim().isEmpty()) {
            errors.append("Department is required.\n");
        }

        // Validate programme
        if (programmeTextField.getText().trim().isEmpty()) {
            errors.append("Programme is required.\n");
        }

        if (errors.length() > 0) {
            showErrorAlert("Validation Errors", errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
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
