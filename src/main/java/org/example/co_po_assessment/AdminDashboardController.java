package org.example.co_po_assessment;

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

    private void openWindow(String fxml, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = (w > 0 && h > 0) ? new Scene(loader.load(), w, h) : new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
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