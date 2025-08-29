package org.example.co_po_assessment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FacultyInfoInputView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FacultyInfoInputView.class.getResource("facultyInfoInput-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 345, 420);
        stage.setTitle("Add Faculty Information");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
