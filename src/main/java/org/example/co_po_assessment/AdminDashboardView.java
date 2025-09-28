package org.example.co_po_assessment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AdminDashboardView.class.getResource("adminDashboard-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}
