package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController {
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

    public void onManageFacultiesButton(ActionEvent event) {
        // opens the ManageFacultiesView window
    }

    public void onManageStudentsButton(ActionEvent event) {
        // opens the ManageStudentsView window
    }

    public void onManageCourseAssignmentsButton(ActionEvent event) {
        // opens the ManageCourseAssignmentsView window
    }

    public void onViewReportsButton(ActionEvent event) {
        // idk what to do for this one tbh, supposed to show all the reports
        // that have been generated so far
    }

    public void onManageCoursesButton(ActionEvent event) {
        // opens the ManageCoursesView window
    }

    public void onLogoutButton(ActionEvent event) {
        // takes you back to login page (AssessmentSystem)
    }
}
