package org.example.co_po_assessment.admin_input_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.co_po_assessment.Objects.CourseAssignment;
import org.example.co_po_assessment.DB_helper.CourseAssignmentDatabaseHelper;
import org.example.co_po_assessment.utilities.ExcelImportUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/courseAssignmentInput-view.fxml"));
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

    // Bulk import handler
    @FXML
    public void onBulkImportAssignments(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Assignments Excel File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(((Stage) backButton.getScene().getWindow()));
        if (file == null) return;
        List<String> errors = new ArrayList<>();
        int inserted = 0, skipped = 0;
        try {
            List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(file);
            int rowNum = 1;
            for (Map<String, String> row : rows) {
                rowNum++;
                String code = ExcelImportUtils.get(row, "course_code", "course");
                String programme = ExcelImportUtils.get(row, "programme", "program");
                String facultyName = ExcelImportUtils.get(row, "faculty_name", "faculty", "instructor", "teacher");
                String ay = ExcelImportUtils.get(row, "academic_year", "year", "ay");
                if (code == null || programme == null || facultyName == null || ay == null) {
                    errors.add("Row " + rowNum + ": missing required fields (course_code, programme, faculty_name, academic_year)");
                    skipped++; continue;
                }
                try {
                    databaseHelper.assignCourse(code, programme, facultyName, ay);
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && (msg.contains("already assigned") || msg.contains("DUPLICATE_COURSE_YEAR") || msg.toLowerCase().contains("duplicate"))) {
                        errors.add("Row " + rowNum + ": duplicate for (" + code + ", " + programme + ", " + ay + ")");
                    } else {
                        errors.add("Row " + rowNum + ": " + msg);
                    }
                    skipped++;
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        loadCourseAssignmentData();
        StringBuilder sb = new StringBuilder();
        sb.append("Imported ").append(inserted).append(" rows. Skipped ").append(skipped).append(".");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, errors.size()); i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size()-10).append(" more");
        }
        showInfoAlert("Assignments Import", sb.toString());
    }

    public void onRemoveCourseButton(ActionEvent actionEvent) {
        CourseAssignment selected = courseTableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarningAlert("No Selection", "Please select a course assignment to remove.");
            return;
        }

        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Course Assignment");
        confirm.setContentText("Are you sure you want to remove the assignment:\n" +
                "Course: " + selected.getCourseCode() + " - " + selected.getCourseName() + "\n" +
                "Faculty: " + selected.getFacultyName() + "\n" +
                "Academic Year: " + selected.getAcademicYear() + "\n" +
                "Department: " + selected.getDepartment() + "\n" +
                "Programme: " + selected.getProgramme());

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Remove from database
                databaseHelper.removeCourseAssignment(
                        selected.getCourseCode(),
                        selected.getProgramme(),
                        selected.getFacultyName(),
                        selected.getAcademicYear()
                );

                // Remove from table
                courseAssignmentList.remove(selected);

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
    public void addNewCourseAssignment(String courseCode, String programme, String facultyName, String academicYear) {
        try {
            // Add to database
            databaseHelper.assignCourse(courseCode, programme, facultyName, academicYear);

            // Reload data to get the course name
            loadCourseAssignmentData();

            showInfoAlert("Success", "Course assigned successfully.");

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("already assigned") || msg.contains("DUPLICATE_COURSE_YEAR") || msg.contains("Duplicate entry"))) {
                showErrorAlert("Assignment Error", "This course already has an assignment for that academic year (same department & programme).");
            } else {
                showErrorAlert("Database Error", "Failed to assign course: " + msg);
            }
        }
    }

    /**
     * Load course assignment data from database
     */
    private void loadCourseAssignmentData() {
        try {
            // Get all course assignments from database
            var data = databaseHelper.getAllCourseAssignments();

            courseAssignmentList.clear();
            for (var a : data) {
                courseAssignmentList.add(new CourseAssignment(
                        a.getCourseCode(),
                        a.getCourseName(),
                        a.getFacultyName(),
                        a.getAcademicYear(),
                        a.getDepartment(),
                        a.getProgramme()
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
