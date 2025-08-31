package org.example.co_po_assessment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("courseInput-view.fxml"));
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
                databaseHelper.removeCourse(selectedCourse.getCode());
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

    // Updated to include department & programme
    public void addNewCourse(String courseCode, String courseName, double credits, String department, String programme) {
        try {
            databaseHelper.addCourse(courseCode, courseName, credits, department, programme);
            Course newCourse = new Course(courseCode, courseName, "", "", credits, programme, department);
            courseList.add(newCourse);
            showInfoAlert("Success", "Course added successfully.");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showErrorAlert("Course Error", "A course with this code already exists.");
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
