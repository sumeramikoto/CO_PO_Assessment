package org.example.co_po_assessment.dashboard_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import org.example.co_po_assessment.DashboardPanels.AssessmentSystem;
import org.example.co_po_assessment.utilities.WindowUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML Label headerLabel;
    @FXML Button logoutButton;
    @FXML Button manageFacultiesButton;
    @FXML Button manageStudentsButton;
    @FXML Button manageCourseAssignmentsButton;
    @FXML Button manageCoursesButton;
    @FXML Button viewReportsButton;
    @FXML Button culminationCoursesButton;
    @FXML Button manageThresholdsButton;
    @FXML Button manageGraduatingStudentsButton;
    @FXML Button graduatingCohortPOReportButton; // New button for Graduating Cohort PO Report

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        headerLabel.setText("Welcome, Administrator!");
    }

    public void onManageFacultiesButton(ActionEvent event) { openWindow("manageFaculties-view.fxml", "Manage Faculty Information", -1, -1); }
    public void onManageStudentsButton(ActionEvent event) { openWindow("manageStudents-view.fxml", "Manage Student Info", 345, 380); }
    public void onManageCoursesButton(ActionEvent event) { openWindow("manageCourses-view.fxml", "New Course Manage", 345, 380); }
    public void onManageCourseAssignmentsButton(ActionEvent event) { openWindow("manageCourseAssignments-view.fxml", "Manage Course Assignments", 345, 380); }
    public void onManageEnrollmentsButton(ActionEvent event) { openWindow("manageEnrollments-view.fxml", "Manage Enrollments", 840, 520); }
    public void onViewReportsButton(ActionEvent event) { openWindow("reports-view.fxml", "CO / PO Reports", 500, 400); }
    public void onCulminationCoursesButton(ActionEvent actionEvent) {
        openWindow("manageCulminationCourses-view.fxml", "Manage Culmination Courses", 840, 520);
    }
    public void onManageGraduatingStudentsButton(ActionEvent actionEvent) {
        openWindow("manageGraduatingStudents-view.fxml", "Manage Graduating Students", 840, 520);
    }
    public void onManageThresholdsButton(ActionEvent actionEvent) { openWindow("manageThresholds-view.fxml", "Manage Thresholds", 420, 260); }
    public void onGraduatingCohortPOReportButton(ActionEvent actionEvent) { // Handler to open Graduating Cohort PO Report
        openWindow("graduatingCohortPOReport-view.fxml", "Graduating Cohort PO Report", 900, 600);
    }

    private void openWindow(String fxml, String title, int w, int h) {
        try {
            String resource = fxml.startsWith("/") ? fxml : "/org/example/co_po_assessment/" + fxml;
            FXMLLoader loader = new FXMLLoader(AdminDashboardController.class.getResource(resource));
            Scene scene = (w > 0 && h > 0) ? new Scene(loader.load(), w, h) : new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            WindowUtils.setSceneAndMaximize(stage, scene);
            stage.show();
        } catch (IOException e) { showErrorAlert("Navigation Error", "Failed to open " + title + ": " + e.getMessage()); }
    }

    public void onLogoutButton(ActionEvent event) {
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
            Platform.runLater(() -> {
                try { new AssessmentSystem().start(new Stage()); }
                catch (Exception ex) { showErrorAlert("Logout Error", "Failed to return to login screen: " + ex.getMessage()); }
            });
        } catch (Exception e) { showErrorAlert("Logout Error", "Unexpected error during logout: " + e.getMessage()); }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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