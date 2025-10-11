package org.example.co_po_assessment.admin_input_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.DatabaseService;
import org.example.co_po_assessment.Objects.Student;
import org.example.co_po_assessment.DB_helper.StudentDatabaseHelper;
import org.example.co_po_assessment.utilities.ExcelImportUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class ManageStudentsController implements Initializable {
    @FXML
    TableView<Student> studentTableView;
    @FXML
    TableColumn<Student, String> idColumn;
    @FXML
    TableColumn<Student, String> nameColumn;
    @FXML
    TableColumn<Student, String> batchColumn;
    @FXML
    TableColumn<Student, String> departmentColumn;
    @FXML
    TableColumn<Student, String> programmeColumn;
    @FXML
    TableColumn<Student, String> emailColumn;
    @FXML
    Button addStudentButton;
    @FXML
    Button removeStudentButton;
    @FXML
    Button backButton;

    private ObservableList<Student> studentList;
    private StudentDatabaseHelper studentDatabaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentDatabaseHelper = new StudentDatabaseHelper();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        batchColumn.setCellValueFactory(new PropertyValueFactory<>("batch"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Initialize student list
        studentList = FXCollections.observableArrayList();
        studentTableView.setItems(studentList);

        // Load existing student data
        loadStudentData();
    }

    public void onAddStudentButton(ActionEvent actionEvent) {
        try {
            // Open the Student Info Input window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/studentInfoInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 450);

            // Get the controller to handle data return
            StudentInfoInputController controller = fxmlLoader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Student Information");
            stage.setScene(scene);
            stage.showAndWait(); // Wait for the window to close before continuing

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Add Student window: " + e.getMessage());
        }
    }

    @FXML
    public void onBulkImportStudents(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Students Excel File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(((Stage) backButton.getScene().getWindow()));
        if (file == null) return;
        DatabaseService databaseService = DatabaseService.getInstance();
        List<String> errors = new ArrayList<>();
        int inserted = 0, skipped = 0;
        try {
            List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(file);
            int rowNum = 1;
            for (Map<String, String> row : rows) {
                rowNum++;
                String id = ExcelImportUtils.get(row, "id", "student_id");
                String name = ExcelImportUtils.get(row, "name", "full_name");
                String batchStr = ExcelImportUtils.get(row, "batch", "year");
                String email = ExcelImportUtils.get(row, "email");
                String dept = ExcelImportUtils.get(row, "department", "dept");
                String prog = ExcelImportUtils.get(row, "programme", "program");
                if (id == null || name == null || batchStr == null || dept == null || prog == null) {
                    errors.add("Row " + rowNum + ": missing required fields (id, name, batch, department, programme)");
                    skipped++; continue;
                }
                int batch;
                try { batch = Integer.parseInt(batchStr); } catch (NumberFormatException nfe) {
                    errors.add("Row " + rowNum + ": invalid batch '" + batchStr + "'");
                    skipped++; continue;
                }
                try {
                    databaseService.insertStudent(id, batch, name, email == null ? "" : email, dept, prog);
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("duplicate")) errors.add("Row " + rowNum + ": duplicate (" + id + ")");
                    else errors.add("Row " + rowNum + ": " + msg);
                    skipped++;
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        loadStudentData();
        StringBuilder sb = new StringBuilder("Imported ").append(inserted).append(" rows. Skipped ").append(skipped).append(".");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, errors.size());i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size() - 10).append(" more");
        }
        showInfoAlert("Student Import", sb.toString());
    }

    public void onRemoveStudentButton(ActionEvent actionEvent) {
        Student selectedStudent = studentTableView.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showWarningAlert("No Selection", "Please select a student to remove.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Remove Student");
        confirmAlert.setContentText("Are you sure you want to remove student: " + selectedStudent.getName() + " (ID: " + selectedStudent.getId() + ")?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Remove from database
                studentDatabaseHelper.removeStudent(selectedStudent.getId());

                // Remove from table
                studentList.remove(selectedStudent);

                showInfoAlert("Success", "Student removed successfully.");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove student: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        // Close the current window
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Load student data from database
     */
    private void loadStudentData() {
        try {
            // Get all students from database
            var studentData = studentDatabaseHelper.getAllStudents();

            studentList.clear();
            for (var student : studentData) {
                studentList.add(new Student(
                    student.getId(),
                    student.getName(),
                    String.valueOf(student.getBatch()),
                    student.getDepartment(),
                    student.getProgramme(),
                    student.getEmail()
                ));
            }

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load student data: " + e.getMessage());
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

    /**
     * Method to be called by StudentInfoInputController when new student is added
     */
    public void addNewStudent(String id, String name, String batch, String email, String department, String programme) {
        try {
            // Add to database
            DatabaseService databaseService = DatabaseService.getInstance();
            databaseService.insertStudent(id, Integer.parseInt(batch), name, email, department, programme);

            // Add to table
            Student newStudent = new Student(id, name, batch, department, programme, email);
            studentList.add(newStudent);

            showInfoAlert("Success", "Student added successfully.");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add student: " + e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Input", "Batch must be a valid number.");
        }
    }
}
