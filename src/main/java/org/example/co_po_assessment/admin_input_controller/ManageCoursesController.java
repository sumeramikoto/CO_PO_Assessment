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
import org.example.co_po_assessment.Objects.Course;
import org.example.co_po_assessment.DB_helper.CoursesDatabaseHelper;
import org.example.co_po_assessment.faculty_input_controller.CourseInputController;
import org.example.co_po_assessment.utilities.ExcelImportUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class ManageCoursesController implements Initializable {
    @FXML
    TableView<Course> courseTableView;
    @FXML
    TableColumn<Course, String> courseCodeColumn;
    @FXML
    TableColumn<Course, String> courseNameColumn;
    @FXML
    TableColumn<Course, Double> creditsColumn;
    @FXML
    TableColumn<Course, String> departmentColumn;
    @FXML
    TableColumn<Course, String> programmeColumn;
    @FXML
    Button addCourseButton;
    @FXML
    Button removeCourseButton;
    @FXML
    Button backButton;

    private ObservableList<Course> courseList;
    private CoursesDatabaseHelper databaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseHelper = new CoursesDatabaseHelper();

        // Set up table columns
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        if (departmentColumn != null) {
            departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        }
        if (programmeColumn != null) {
            programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        }

        // Initialize course list
        courseList = FXCollections.observableArrayList();
        courseTableView.setItems(courseList);

        // Load existing course data
        loadCourseData();
    }

    public void onAddCourseButton(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/courseInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 420);
            CourseInputController controller = fxmlLoader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Add New Course");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Add Course window: " + e.getMessage());
        }
    }

    public void onRemoveCourseButton(ActionEvent actionEvent) {
        Course selectedCourse = courseTableView.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showWarningAlert("No Selection", "Please select a course to remove.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Course");
        confirmAlert.setContentText("Are you sure you want to remove the course:\n" +
                "Code: " + selectedCourse.getCode() + "\n" +
                "Name: " + selectedCourse.getTitle() + "\n" +
                "Credits: " + selectedCourse.getCredit() + "\n" +
                "Department: " + selectedCourse.getDepartment() + "\n" +
                "Programme: " + selectedCourse.getProgramme() + "\n\n" +
                "Note: This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseHelper.removeCourse(selectedCourse.getCode(), selectedCourse.getProgramme());
                courseList.remove(selectedCourse);
                showInfoAlert("Success", "Course removed successfully.");
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove course: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    // Bulk import handler
    @FXML
    public void onBulkImportCourses(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Courses Excel File");
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
                String code = ExcelImportUtils.get(row, "course_code", "code");
                String name = ExcelImportUtils.get(row, "course_name", "name", "title");
                String creditsStr = ExcelImportUtils.get(row, "credits", "credit");
                String dept = ExcelImportUtils.get(row, "department", "dept");
                String prog = ExcelImportUtils.get(row, "programme", "program", "program_name");
                if (code == null || name == null || creditsStr == null || dept == null || prog == null) {
                    errors.add("Row " + rowNum + ": missing required fields (course_code, course_name, credits, department, programme)");
                    skipped++;
                    continue;
                }
                double credits;
                try { credits = Double.parseDouble(creditsStr); } catch (NumberFormatException nfe) {
                    errors.add("Row " + rowNum + ": invalid credits '" + creditsStr + "'");
                    skipped++; continue;
                }
                try {
                    databaseHelper.addCourse(code, name, credits, dept, prog);
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("duplicate")) errors.add("Row " + rowNum + ": duplicate (" + code + ", " + prog + ")");
                    else errors.add("Row " + rowNum + ": " + msg);
                    skipped++;
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        loadCourseData();
        StringBuilder sb = new StringBuilder("Imported ").append(inserted).append(" rows. Skipped ").append(skipped).append(".");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, errors.size());i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size() - 10).append(" more");
        }
        showInfoAlert("Course Import", sb.toString());
    }

    // Updated to include department & programme
    public void addNewCourse(String courseCode, String courseName, double credits, String department, String programme) {
        try {
            databaseHelper.addCourse(courseCode, courseName, credits, department, programme);
            Course newCourse = new Course(courseCode, courseName, "", "", credits, programme, department);
            courseList.add(newCourse);
            showInfoAlert("Success", "Course added successfully.");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showErrorAlert("Course Error", "A course with this code already exists for the selected programme.");
            } else {
                showErrorAlert("Database Error", "Failed to add course: " + e.getMessage());
            }
        }
    }

    private void loadCourseData() {
        try {
            var courseData = databaseHelper.getAllCourses();
            courseList.clear();
            for (var course : courseData) {
                courseList.add(new Course(
                    course.getCourseCode(),
                    course.getCourseName(),
                    "", // instructor
                    "", // academic year
                    course.getCredits(),
                    course.getProgramme(),
                    course.getDepartment()
                ));
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load course data: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
