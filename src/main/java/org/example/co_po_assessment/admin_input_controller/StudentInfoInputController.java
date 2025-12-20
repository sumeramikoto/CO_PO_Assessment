package org.example.co_po_assessment.admin_input_controller;

import javafx.collections.FXCollections;
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
    ComboBox<String> degreeComboBox;
    @FXML
    TextField programmeTextField;
    @FXML
    Button confirmButton;
    @FXML
    Button backButton;

    private ManageStudentsController parentController;
    private boolean isEditMode = false;
    private String originalStudentId = null;

    // Regex patterns according to requirements
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{9}$");
    private static final Pattern BATCH_PATTERN = Pattern.compile("^\\d{2}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.edu$");
    private static final Pattern DEPT_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern PROGRAMME_ABBR_PATTERN = Pattern.compile("^[A-Z]{2,3}$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set up the form labels and initial state
        headLabel.setText("Add Student Information");
        
        // Initialize degree dropdown
        degreeComboBox.setItems(FXCollections.observableArrayList(
            "BSc in",
            "MSc in",
            "PhD in",
            "BBA in"
        ));
    }

    /**
     * Set the parent controller to enable communication back to the student management window
     */
    public void setParentController(ManageStudentsController parentController) {
        this.parentController = parentController;
    }

    /**
     * Set edit mode and populate fields with existing student data
     */
    public void setEditMode(String id, String name, String batch, String email, String department, String programme) {
        this.isEditMode = true;
        this.originalStudentId = id;
        
        headLabel.setText("Edit Student Information");
        
        // Populate fields with existing data
        idTextField.setText(id);
        idTextField.setDisable(true); // Don't allow editing the ID
        nameTextField.setText(name);
        batchTextField.setText(batch);
        emailTextField.setText(email);
        departmentTextField.setText(department);
        
        // Parse programme to set degree and programme abbreviation
        // Programme format: "BSc in CSE" or "MSc in EEE", etc.
        if (programme != null && programme.contains(" in ")) {
            String[] parts = programme.split(" in ", 2);
            if (parts.length == 2) {
                degreeComboBox.setValue(parts[0] + " in");
                programmeTextField.setText(parts[1]);
            }
        }
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
        String degree = degreeComboBox.getValue();
        String programmeAbbr = programmeTextField.getText().trim();
        String programme = degree + " " + programmeAbbr;

        try {
            // Call parent controller to add or update the student
            if (parentController != null) {
                if (isEditMode) {
                    parentController.updateStudent(originalStudentId, id, name, batch, email, department, programme);
                } else {
                    parentController.addNewStudent(id, name, batch, email, department, programme);
                }
            }

            // Close the current window
            Stage currentStage = (Stage) confirmButton.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            showErrorAlert("Error", "Failed to " + (isEditMode ? "update" : "add") + " student: " + e.getMessage());
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

        // Validate degree selection
        String degree = degreeComboBox.getValue();
        if (degree == null || degree.isEmpty()) {
            errors.append("Degree is required. Please select from dropdown.\n");
        }

        // Validate programme abbreviation: 2-3 uppercase letters
        String programmeAbbr = programmeTextField.getText().trim();
        if (programmeAbbr.isEmpty()) {
            errors.append("Programme abbreviation is required.\n");
        } else if (!PROGRAMME_ABBR_PATTERN.matcher(programmeAbbr).matches()) {
            errors.append("Programme must be 2-3 uppercase letters, e.g., SWE or CS.\n");
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
