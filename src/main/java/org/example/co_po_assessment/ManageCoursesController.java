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

        // Initialize course list
        courseList = FXCollections.observableArrayList();
        courseTableView.setItems(courseList);

        // Load existing course data
        loadCourseData();
    }

    public void onAddCourseButton(ActionEvent actionEvent) {
        try {
            // Open the Course Input window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("courseInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 450, 300);

            // Get the controller to handle data return
            CourseInputController controller = fxmlLoader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add New Course");
            stage.setScene(scene);
            stage.showAndWait(); // Wait for the window to close before continuing

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

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Course");
        confirmAlert.setContentText("Are you sure you want to remove the course:\n" +
                "Code: " + selectedCourse.getCode() + "\n" +
                "Name: " + selectedCourse.getTitle() + "\n" +
                "Credits: " + selectedCourse.getCredit() + "\n\n" +
                "Note: This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Remove from database
                databaseHelper.removeCourse(selectedCourse.getCode());

                // Remove from table
                courseList.remove(selectedCourse);

                showInfoAlert("Success", "Course removed successfully.");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove course: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        // Close the current window
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Method to be called by CourseInputController when new course is added
     */
    public void addNewCourse(String courseCode, String courseName, double credits) {
        try {
            // Add to database
            databaseHelper.addCourse(courseCode, courseName, credits);

            // Add to table view
            Course newCourse = new Course(courseCode, courseName, "", "", credits, "", "");
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

    /**
     * Load course data from database
     */
    private void loadCourseData() {
        try {
            // Get all courses from database
            var courseData = databaseHelper.getAllCourses();

            courseList.clear();
            for (var course : courseData) {
                courseList.add(new Course(
                    course.getCourseCode(),
                    course.getCourseName(),
                    "", // instructor - not needed for this view
                    "", // academic year - not needed for this view
                    course.getCredits(),
                    "", // program - not needed for this view
                    ""  // department - not needed for this view
                ));
            }

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load course data: " + e.getMessage());
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
