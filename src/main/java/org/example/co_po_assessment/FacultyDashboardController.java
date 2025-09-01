package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;

public class FacultyDashboardController {
    @FXML
    Button logoutButton;
    @FXML
    Label facultyLabel;

    // lots of other fxml elements haven't been put here yet,
    // idk the best way to represent the object that will be displayed
    // in the assigned courses table that you'll see in the faculty dashboard

    public void onLogoutButton(ActionEvent actionEvent) {
        try {
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
        // the window will also let you add, modify or delete questions
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
