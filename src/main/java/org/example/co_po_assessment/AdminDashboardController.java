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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML
    Label headerLabel;
    @FXML
    Button logoutButton;
    @FXML
    Button manageFacultiesButton;
    @FXML
    Button manageStudentsButton;
    @FXML
    Button manageCourseAssignmentsButton;
    @FXML
    Button manageCoursesButton;
    @FXML
    Button viewReportsButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set welcome message for admin
        headerLabel.setText("Welcome, Administrator!");
    }

    public void onManageFacultiesButton(ActionEvent event) {
        try {
            // Load the Manage Faculties view
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("manageFaculties-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            Stage stage = new Stage();
            stage.setTitle("Manage Faculty Information");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Manage Faculties window: " + e.getMessage());
        }
    }

    public void onManageStudentsButton(ActionEvent event) {
        try {
            // Load the Manage Students view
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("manageStudents-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 345, 380);

            Stage stage = new Stage();
            stage.setTitle("Manage Student Info");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Manage Students window: " + e.getMessage());
        }
    }

    public void onManageCoursesButton(ActionEvent event) {
        try {
            // Load the Manage Courses view
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("manageCourses-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 345, 380);

            Stage stage = new Stage();
            stage.setTitle("New Course Manage");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Manage Courses window: " + e.getMessage());
        }
    }

    public void onManageCourseAssignmentsButton(ActionEvent event) {
        try {
            // Load the Manage Course Assignments view
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("manageCourseAssignments-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 345, 380);

            Stage stage = new Stage();
            stage.setTitle("Manage Course Assignments");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Manage Course Assignments window: " + e.getMessage());
        }
    }

    public void onViewReportsButton(ActionEvent event) {
        // TODO: Implement reports functionality when reports view is available
        showInfoAlert("Reports", "Reports functionality will be implemented soon.");
    }

    public void onLogoutButton(ActionEvent event) {
        try {
            // Navigate back to login/main screen
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 320, 240);

            // Get current stage and replace scene
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setTitle("CO/PO Assessment System");
            currentStage.setScene(scene);

        } catch (IOException e) {
            showErrorAlert("Logout Error", "Failed to return to login screen: " + e.getMessage());
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
