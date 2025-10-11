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
    Label shortnameLabel;
    @FXML
    Label emailLabel;
    @FXML
    Label passwordLabel;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the form labels and initial state
        headLabel.setText("Add Faculty Information");
    }

    /**
     * Set the parent controller to enable communication back to the faculty management window
     */
    public void setParentController(ManageFacultiesController parentController) {
        this.parentController = parentController;
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
            // Call parent controller to add the faculty
            if (parentController != null) {
                parentController.addNewFaculty(id, name, shortname, email, password);
            }

            // Close the current window
            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to add faculty member: " + e.getMessage());
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

        // Validate ID (now VARCHAR - allow alphanumeric & dashes/underscores)
        String rawId = idTextField.getText().trim();
        if (rawId.isEmpty()) {
            errors.append("Faculty ID is required.\n");
        } else if (!rawId.matches("[A-Za-z0-9_-]{1,32}")) {
            errors.append("Faculty ID may contain letters, digits, '-' or '_' (max 32 chars).\n");
        }

        // Validate name
        if (nameTextField.getText().trim().isEmpty()) {
            errors.append("Full name is required.\n");
        }

        // Validate shortname
        if (shortnameTextField.getText().trim().isEmpty()) {
            errors.append("Short name is required.\n");
        }

        // Validate email
        String email = emailTextField.getText().trim();
        if (email.isEmpty()) {
            errors.append("Email is required.\n");
        } else if (!isValidEmail(email)) {
            errors.append("Please enter a valid email address.\n");
        }

        // Validate password
        if (passwordField.getText().isEmpty()) {
            errors.append("Password is required.\n");
        } else if (passwordField.getText().length() < 6) {
            errors.append("Password must be at least 6 characters long.\n");
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
