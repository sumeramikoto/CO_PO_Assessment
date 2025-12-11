package org.example.co_po_assessment.faculty_input_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.CoursesDatabaseHelper;
import org.example.co_po_assessment.admin_input_controller.ManageCoursesController;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
    ComboBox<String> degreeComboBox;
    @FXML
    TextField cosTextField;
    @FXML
    TextField posTextField;
    @FXML
    Button selectCOsButton;
    @FXML
    Button selectPOsButton;
    @FXML
    Button confirmButton;
    @FXML
    Button backButton;

    private ManageCoursesController parentController;
    private CoursesDatabaseHelper databaseHelper;
    
    // Store selected COs and POs
    private Set<Integer> selectedCOs = new TreeSet<>();
    private Set<Integer> selectedPOs = new TreeSet<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseHelper = new CoursesDatabaseHelper();
        
        // Initialize degree ComboBox
        if (degreeComboBox != null) {
            degreeComboBox.getItems().addAll("BSc", "MSc", "PhD", "BBA");
        }
        
        // Auto-uppercase programme code
        if (programmeTextField != null) {
            programmeTextField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals(newVal.toUpperCase())) {
                    programmeTextField.setText(newVal.toUpperCase());
                }
            });
        }
        
        // Make text fields non-editable since we use dialogs
        if (cosTextField != null) {
            cosTextField.setEditable(false);
        }
        if (posTextField != null) {
            posTextField.setEditable(false);
        }
    }

    /**
     * Set the parent controller to enable communication back to the main window
     */
    public void setParentController(ManageCoursesController parentController) {
        this.parentController = parentController;
    }

    public void onConfirmButton(ActionEvent event) {
        System.out.println("Confirm button pressed"); // Debug
        
        // Normalize course code input (collapse multiple spaces, uppercase) before validation
        if (courseCodeTextField.getText() != null) {
            String normalized = courseCodeTextField.getText().trim().replaceAll("\\s+", " ").toUpperCase();
            courseCodeTextField.setText(normalized);
        }

        // Validate input fields
        if (!validateInputs()) {
            System.out.println("Validation failed"); // Debug
            return;
        }

        System.out.println("Validation passed"); // Debug

        // Get input values (course code already normalized above)
        String courseCode = courseCodeTextField.getText().trim();
        String courseName = courseNameTextField.getText().trim();
        double credits = Double.parseDouble(creditsTextField.getText().trim());
        String department = departmentTextField.getText().trim().toUpperCase();
        String degree = degreeComboBox.getValue();
        String programmeCode = programmeTextField.getText().trim();
        String programme = (degree != null && !programmeCode.isEmpty()) ? degree + " in " + programmeCode : programmeCode;

        // Use the selected COs and POs from checkboxes
        List<Integer> coNumbers = new ArrayList<>(selectedCOs);
        List<Integer> poNumbers = new ArrayList<>(selectedPOs);

        try {
            // Call parent controller to add the course and assign CO/POs
            if (parentController != null) {
                parentController.addNewCourse(courseCode, courseName, credits, department, programme, coNumbers, poNumbers);
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

        // Validate programme
        String degree = degreeComboBox.getValue();
        String programmeCode = programmeTextField.getText().trim();
        if (degree == null || degree.isEmpty()) {
            errors.append("Degree is required.\n");
        }
        if (programmeCode.isEmpty()) {
            errors.append("Programme code is required.\n");
        } else if (!programmeCode.matches("[A-Z]{2,3}")) {
            errors.append("Programme code must be 2-3 uppercase letters.\n");
        }

        // Validate CO/PO selection
        if (selectedCOs.isEmpty()) {
            errors.append("At least one CO must be selected.\n");
        }
        if (selectedPOs.isEmpty()) {
            errors.append("At least one PO must be selected.\n");
        }

        // Check if course (code + programme) already exists
        String programme = (degree != null && !programmeCode.isEmpty()) ? degree + " in " + programmeCode : programmeCode;
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

    private static class OutcomeParseResult {
        final List<Integer> numbers; final String error;
        OutcomeParseResult(List<Integer> numbers, String error) { this.numbers = numbers; this.error = error; }
    }

    private static OutcomeParseResult parseAndValidateOutcomes(String raw, int maxAllowed, String label) {
        List<Integer> nums = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return new OutcomeParseResult(nums, null);
        String s = raw.trim().replaceAll("\\s+", " ");
        // Normalize label prefixes like CO1 or PO2 by stripping letters
        s = s.replaceAll("(?i)CO", "").replaceAll("(?i)PO", "");
        String[] parts = s.split("[ ,;]+");
        List<String> invalid = new ArrayList<>();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (part.contains("-")) {
                String[] ab = part.split("-");
                if (ab.length != 2) { invalid.add(part); continue; }
                try {
                    int a = Integer.parseInt(ab[0].trim());
                    int b = Integer.parseInt(ab[1].trim());
                    if (a > b) { invalid.add(part); continue; }
                    for (int x=a; x<=b; x++) {
                        if (x < 1 || x > maxAllowed) { invalid.add(String.valueOf(x)); continue; }
                        if (!nums.contains(x)) nums.add(x);
                    }
                } catch (NumberFormatException nfe) {
                    invalid.add(part);
                }
            } else {
                try {
                    int v = Integer.parseInt(part.trim());
                    if (v < 1 || v > maxAllowed) invalid.add(part);
                    else if (!nums.contains(v)) nums.add(v);
                } catch (NumberFormatException nfe) {
                    invalid.add(part);
                }
            }
        }
        Collections.sort(nums);
        String error = invalid.isEmpty() ? null : (label + " values out of range or invalid: " + String.join(", ", invalid));
        return new OutcomeParseResult(nums, error);
    }

    /**
     * Show dialog with checkboxes to select COs
     */
    @FXML
    public void onSelectCOs(ActionEvent event) {
        Dialog<Set<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Select Course Outcomes (COs)");
        dialog.setHeaderText("Select the COs for this course (1-12):");
        
        ButtonType selectButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButton, ButtonType.CANCEL);
        
        // Create grid with checkboxes
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        
        CheckBox[] checkBoxes = new CheckBox[12];
        for (int i = 0; i < 12; i++) {
            int coNumber = i + 1;
            checkBoxes[i] = new CheckBox("CO" + coNumber);
            checkBoxes[i].setSelected(selectedCOs.contains(coNumber));
            
            int row = i / 4;
            int col = i % 4;
            grid.add(checkBoxes[i], col, row);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == selectButton) {
                Set<Integer> selected = new TreeSet<>();
                for (int i = 0; i < 12; i++) {
                    if (checkBoxes[i].isSelected()) {
                        selected.add(i + 1);
                    }
                }
                return selected;
            }
            return null;
        });
        
        Optional<Set<Integer>> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            selectedCOs = selected;
            updateCOTextField();
        });
    }
    
    /**
     * Show dialog with checkboxes to select POs
     */
    @FXML
    public void onSelectPOs(ActionEvent event) {
        Dialog<Set<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Select Program Outcomes (POs)");
        dialog.setHeaderText("Select the POs for this course (1-12):");
        
        ButtonType selectButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButton, ButtonType.CANCEL);
        
        // Create grid with checkboxes
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        
        CheckBox[] checkBoxes = new CheckBox[12];
        for (int i = 0; i < 12; i++) {
            int poNumber = i + 1;
            checkBoxes[i] = new CheckBox("PO" + poNumber);
            checkBoxes[i].setSelected(selectedPOs.contains(poNumber));
            
            int row = i / 4;
            int col = i % 4;
            grid.add(checkBoxes[i], col, row);
        }
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == selectButton) {
                Set<Integer> selected = new TreeSet<>();
                for (int i = 0; i < 12; i++) {
                    if (checkBoxes[i].isSelected()) {
                        selected.add(i + 1);
                    }
                }
                return selected;
            }
            return null;
        });
        
        Optional<Set<Integer>> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            selectedPOs = selected;
            updatePOTextField();
        });
    }
    
    /**
     * Update CO text field display
     */
    private void updateCOTextField() {
        if (selectedCOs.isEmpty()) {
            cosTextField.setText("");
        } else {
            cosTextField.setText(selectedCOs.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));
        }
    }
    
    /**
     * Update PO text field display
     */
    private void updatePOTextField() {
        if (selectedPOs.isEmpty()) {
            posTextField.setText("");
        } else {
            posTextField.setText(selectedPOs.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ")));
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
}
