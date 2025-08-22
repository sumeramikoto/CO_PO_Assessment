package org.example.co_po_assessment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ManageCoursesView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ManageCoursesView.class.getResource("manageCourses-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 345, 380);
        stage.setTitle("Manage Course Assignment");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
