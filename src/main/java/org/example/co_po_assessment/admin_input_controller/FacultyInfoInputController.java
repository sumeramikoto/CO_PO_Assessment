package org.example.co_po_assessment.admin_input_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FacultyInfoInputController implements Initializable {
    @FXML
    Label headLabel;
    @FXML
    Button confirmButton;
    @FXML
    Button backButton;
    @FXML
    TextField idTextField;
    @FXML
    TextField nameTextField;
    @FXML
    TextField shortnameTextField;
    @FXML
    TextField emailTextField;
    @FXML
    PasswordField passwordField;

    private ManageFacultiesController parentController;
    private boolean isEditMode = false;
    private String originalFacultyId = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the form labels and initial state
        headLabel.setText("Add Faculty Information");

        // Input constraints as-you-type
        if (idTextField != null) {
            idTextField.setTextFormatter(new TextFormatter<>(change -> {
                String next = change.getControlNewText();
                if (next.matches("\\d{0,9}")) return change; // allow up to 9 digits
                return null;
            }));
        }
        if (shortnameTextField != null) {
            shortnameTextField.setTextFormatter(new TextFormatter<>(change -> {
                String text = change.getText();
                if (text != null) change.setText(text.toUpperCase());
                String next = change.getControlNewText().toUpperCase();
                if (next.matches("[A-Z]{0,4}")) return change; // allow only A-Z up to 4 chars
                return null;
            }));
        }
    }

    /**
     * Set the parent controller to enable communication back to the faculty management window
     */
    public void setParentController(ManageFacultiesController parentController) {
        this.parentController = parentController;
    }

    /**
     * Set edit mode and populate fields with existing faculty data
     */
    public void setEditMode(String id, String name, String shortname, String email) {
        this.isEditMode = true;
        this.originalFacultyId = id;
        
        headLabel.setText("Edit Faculty Information");
        
        // Populate fields with existing data
        idTextField.setText(id);
        idTextField.setDisable(true); // Don't allow editing the ID
        nameTextField.setText(name);
        shortnameTextField.setText(shortname);
        emailTextField.setText(email);
        passwordField.setText(""); // Leave password empty, will only update if changed
    }

    public void onConfirmButton(ActionEvent event) {
        // Debug: Check if idTextField is null
        if (idTextField == null) {
            showErrorAlert("Debug", "idTextField is null - FXML file may not be updated properly. Please restart the application.");
            return;
        }

        // Validate input fields
        if (!validateInputs()) {
            return;
        }

        // Get input values
        String id = idTextField.getText().trim();
        String name = nameTextField.getText().trim();
        String shortname = shortnameTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String password = passwordField.getText();

        try {
            // Call parent controller to add or update the faculty
            if (parentController != null) {
                if (isEditMode) {
                    // For edit mode, only update password if it's provided
                    if (password == null || password.isEmpty()) {
                        // If password is empty, fetch the current password or use a placeholder
                        password = null; // null means don't update password
                    }
                    parentController.updateFaculty(originalFacultyId, id, name, shortname, email, password);
                } else {
                    parentController.addNewFaculty(id, name, shortname, email, password);
                }
            }

            // Close the current window
            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to " + (isEditMode ? "update" : "add") + " faculty member: " + e.getMessage());
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
        String rawId = idTextField.getText().trim();
        if (rawId.isEmpty()) {
            errors.append("Faculty ID is required.\n");
        } else if (!rawId.matches("\\d{9}")) {
            errors.append("Faculty ID must be exactly 9 digits.\n");
        }

        // Validate name
        if (nameTextField.getText().trim().isEmpty()) {
            errors.append("Full name is required.\n");
        }

        // Validate shortname: 2-4 uppercase letters
        String shortname = shortnameTextField.getText().trim();
        if (shortname.isEmpty()) {
            errors.append("Shortname is required.\n");
        } else if (!shortname.matches("[A-Z]{2,4}")) {
            errors.append("Shortname must be 2 to 4 uppercase letters (A-Z).\n");
        }

        // Validate email: yourname@institution.edu
        String email = emailTextField.getText().trim();
        if (email.isEmpty()) {
            errors.append("Email is required.\n");
        } else if (!isValidEduEmail(email)) {
            errors.append("Email must look like yourname@institution.edu.\n");
        }

        // Validate password (only required for new entries, optional for edits)
        String pwd = passwordField.getText();
        if (!isEditMode) {
            // Password required for new faculty
            if (pwd == null || pwd.isEmpty()) {
                errors.append("Password is required.\n");
            } else if (pwd.length() < 6) {
                errors.append("Password must be at least 6 characters long.\n");
            }
        } else {
            // For edit mode, only validate if password is provided
            if (pwd != null && !pwd.isEmpty() && pwd.length() < 6) {
                errors.append("Password must be at least 6 characters long.\n");
            }
        }

        if (errors.length() > 0) {
            showErrorAlert("Validation Errors", errors.toString());
            return false;
        }

        return true;
    }

    /**
     * EDU email validation
     */
    private boolean isValidEduEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.edu$");
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
