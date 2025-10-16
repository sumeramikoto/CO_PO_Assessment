package org.example.co_po_assessment.DashboardPanels;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.co_po_assessment.utilities.WindowUtils;

import java.io.IOException;

public class AdminDashboardView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AdminDashboardView.class.getResource("/org/example/co_po_assessment/adminDashboard-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Admin Dashboard");
        WindowUtils.setSceneAndMaximize(stage, scene);
        stage.show();
    }
}
