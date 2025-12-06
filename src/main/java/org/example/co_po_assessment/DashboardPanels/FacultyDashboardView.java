package org.example.co_po_assessment.DashboardPanels;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.co_po_assessment.utilities.WindowUtils;

import java.io.IOException;

public class FacultyDashboardView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FacultyDashboardView.class.getResource("/org/example/co_po_assessment/facultyDashboard-shell.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Faculty Dashboard");
        WindowUtils.setSceneAndMaximize(stage, scene);
        stage.show();
    }
}
