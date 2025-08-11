import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public void start(Stage primaryStage) {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));

    MenuBar menuBar = createMenuBar();
    root.setTop(menuBar);

    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

    tabPane.getTabs().addAll(
            createStudentInfoTab(),
            createQuestionInfoTab(),
            createMarksEntryTab(),
            createResultsTab()
    );

    root.setCenter(tabPane);

    Scene scene = new Scene(root, 1000, 700);
    primaryStage.setTitle("CO/PO Assessment System");
    primaryStage.setScene(scene);
    primaryStage.show();
}

public void main() {
}