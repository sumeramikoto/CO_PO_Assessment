package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class FacultyDashboardController {
    @FXML Button logoutButton;
    @FXML Label facultyLabel;
    @FXML TableView<DatabaseService.FacultyCourseAssignment> assignedCoursesTableView;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> courseCodeColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> courseNameColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> academicYearColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> departmentColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> programmeColumn;

    private final DatabaseService db = DatabaseService.getInstance();
    private final ObservableList<DatabaseService.FacultyCourseAssignment> assignments = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Configure columns
        if (courseCodeColumn != null) courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        if (courseNameColumn != null) courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        if (academicYearColumn != null) academicYearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        if (departmentColumn != null) departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        if (programmeColumn != null) programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));

        if (assignedCoursesTableView != null) {
            assignedCoursesTableView.setItems(assignments);
        }
        loadFacultyData();
    }

    private void loadFacultyData() {
        DatabaseService.FacultyInfo info = UserSession.getCurrentFaculty();
        if (info == null) {
            if (facultyLabel != null) facultyLabel.setText("Faculty (session not found)");
            return;
        }
        if (facultyLabel != null) facultyLabel.setText("Welcome, " + info.fullName + " (" + info.shortname + ")");
        try {
            assignments.clear();
            assignments.addAll(db.getAssignmentsForFaculty(info.id));
        } catch (Exception e) {
            // Optionally show an alert
            e.printStackTrace();
        }
    }

    public void onLogoutButton(ActionEvent actionEvent) {
        try {
            UserSession.clear();
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
            Platform.runLater(() -> {
                try {
                    new AssessmentSystem().start(new Stage());
                } catch (Exception e) {
                    // simple fallback: print stack trace until alert system added here
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onQuestionsButton(ActionEvent actionEvent) {
        // opens a window that'll show the questions for the selected course and each respective exam
        DatabaseService.FacultyCourseAssignment selected = assignedCoursesTableView != null ? assignedCoursesTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course first.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manageCourseQuestions-view.fxml"));
            Parent root = loader.load();
            ManageCourseQuestionsController controller = loader.getController();
            controller.setCourseAssignment(selected); // pass context (future use)
            Stage stage = new Stage();
            stage.setTitle("Manage Questions - " + selected.getCourseCode());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Manage Course Questions window: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void onMarksButton(ActionEvent actionEvent) {
        // opens a window that'll show the students marks for the selected course and each respective exam
        // also grade marks etc.
    }

    public void onReportButton(ActionEvent actionEvent) {
        // checks if all the students have been graded yet or not
        // if graded then generates the co/po assessment report for the course
    }
}
