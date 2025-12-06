package org.example.co_po_assessment.dashboard_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import org.example.co_po_assessment.DashboardPanels.AssessmentSystem;
import org.example.co_po_assessment.utilities.WindowUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML Label headerLabel;
    @FXML Label breadcrumbLabel; // optional breadcrumb label from shell layout
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
    @FXML Button manageEnrollmentsButton; // missing declaration added
    // Center content container from shell to embed child views
    @FXML VBox centerContent;

    // Root container used to place toast/snackbar bottom-right; will be looked up from any child
    private StackPane rootStack;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (headerLabel != null) headerLabel.setText("Welcome, Administrator!");
        if (breadcrumbLabel != null) breadcrumbLabel.setText("Home");
        // prepare root stack for toasts (optional)
        Platform.runLater(() -> {
            if (centerContent != null) {
                Parent sceneRoot = centerContent.getScene() != null ? centerContent.getScene().getRoot() : null;
                if (sceneRoot instanceof StackPane sp) { rootStack = sp; }
            }
        });
    }

    public void onManageFacultiesButton(ActionEvent event) { setBreadcrumb("Home > Manage Faculties"); setActive(manageFacultiesButton); setCenterFromFXML("manageFaculties-view.fxml"); }
    public void onManageStudentsButton(ActionEvent event) { setBreadcrumb("Home > Manage Students"); setActive(manageStudentsButton); setCenterFromFXML("manageStudents-view.fxml"); }
    public void onManageCoursesButton(ActionEvent event) { setBreadcrumb("Home > Manage Courses"); setActive(manageCoursesButton); setCenterFromFXML("manageCourses-view.fxml"); }
    public void onManageCourseAssignmentsButton(ActionEvent event) { setBreadcrumb("Home > Manage Course Assignments"); setActive(manageCourseAssignmentsButton); setCenterFromFXML("manageCourseAssignments-view.fxml"); }
    public void onManageEnrollmentsButton(ActionEvent event) { setBreadcrumb("Home > Manage Enrollments"); setActive(manageEnrollmentsButton); setCenterFromFXML("manageEnrollments-view.fxml"); }
    public void onViewReportsButton(ActionEvent event) { setBreadcrumb("Home > Reports"); setActive(viewReportsButton); setCenterFromFXML("reports-view.fxml"); }
    public void onCulminationCoursesButton(ActionEvent actionEvent) { setBreadcrumb("Home > Culmination Courses"); setActive(culminationCoursesButton); setCenterFromFXML("manageCulminationCourses-view.fxml"); }
    public void onManageGraduatingStudentsButton(ActionEvent actionEvent) { setBreadcrumb("Home > Graduating Students"); setActive(manageGraduatingStudentsButton); setCenterFromFXML("manageGraduatingStudents-view.fxml"); }
    public void onManageThresholdsButton(ActionEvent actionEvent) { setBreadcrumb("Home > Thresholds"); setActive(manageThresholdsButton); setCenterFromFXML("manageThresholds-view.fxml"); }
    public void onGraduatingCohortPOReportButton(ActionEvent actionEvent) { setBreadcrumb("Home > Graduating Cohort PO Report"); setActive(graduatingCohortPOReportButton); setCenterFromFXML("graduatingCohortPOReport-view.fxml"); }

    private void setCenterFromFXML(String fxml) {
        try {
            String resource = fxml.startsWith("/") ? fxml : "/org/example/co_po_assessment/" + fxml;
            FXMLLoader loader = new FXMLLoader(AdminDashboardController.class.getResource(resource));
            Parent root = loader.load();
            if (centerContent != null) centerContent.getChildren().setAll(root);
        } catch (IOException e) { showErrorAlert("Navigation Error", "Failed to open view: " + e.getMessage()); }
    }

    private void setBreadcrumb(String text) {
        if (breadcrumbLabel != null) breadcrumbLabel.setText(text);
    }

    private void setActive(Button active) {
        // remove active from all, then add to the given button
        Button[] buttons = new Button[]{ manageFacultiesButton, manageStudentsButton, manageCoursesButton, manageEnrollmentsButton,
                manageCourseAssignmentsButton, culminationCoursesButton, manageThresholdsButton, manageGraduatingStudentsButton,
                graduatingCohortPOReportButton, viewReportsButton };
        for (Button b : buttons) { if (b != null) b.getStyleClass().remove("active"); }
        if (active != null && !active.getStyleClass().contains("active")) active.getStyleClass().add("active");
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
        showToast(message, true);
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        showToast(message, false);
    }

    // Lightweight toast/snackbar bottom-right
    private void showToast(String message, boolean error) {
        Platform.runLater(() -> {
            StackPane stack = rootStack;
            if (stack == null && centerContent != null && centerContent.getScene() != null && centerContent.getScene().getRoot() instanceof StackPane sp) {
                stack = sp; rootStack = sp;
            }
            if (stack == null) return;
            Label toast = new Label(message);
            toast.getStyleClass().addAll("toast-container", error ? "error" : "success");
            StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
            stack.getChildren().add(toast);
            final Label toastRef = toast; // ensure effectively final capture
            // auto-dismiss
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> stack.getChildren().remove(toastRef));
            }).start();
        });
    }
}