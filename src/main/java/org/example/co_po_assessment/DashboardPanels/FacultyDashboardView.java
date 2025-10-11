package org.example.co_po_assessment.DashboardPanels;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FacultyDashboardView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FacultyDashboardView.class.getResource("/org/example/co_po_assessment/facultyDashboard-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Faculty Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}
