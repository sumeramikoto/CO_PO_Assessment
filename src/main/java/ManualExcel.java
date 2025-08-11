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


private MenuBar createMenuBar() {
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");
    MenuItem newItem = new MenuItem("New");
    MenuItem saveItem = new MenuItem("Save");
    MenuItem exitItem = new MenuItem("Exit");
    fileMenu.getItems().addAll(newItem, saveItem, new SeparatorMenuItem(), exitItem);

    Menu editMenu = new Menu("Edit");

    Menu reportsMenu = new Menu("Reports");

    menuBar.getMenus().addAll(fileMenu, editMenu, reportsMenu);
    return menuBar;
}

private Tab createStudentInfoTab() {
    Tab tab = new Tab("Student Information");
    tab.setClosable(false);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 10, 10, 10));

    grid.add(new Label("Course Information:"), 0, 0, 2, 1);

    grid.add(new Label("Course Code:"), 0, 1);
    grid.add(new TextField(), 1, 1);

    grid.add(new Label("Course Title:"), 0, 2);
    grid.add(new TextField(), 1, 2);

    grid.add(new Label("Instructor:"), 0, 3);
    grid.add(new TextField(), 1, 3);

    grid.add(new Label("Academic Year:"), 0, 4);
    grid.add(new TextField(), 1, 4);

    TableView<String> studentTable = new TableView<>();
    TableColumn<String, String> idCol = new TableColumn<>("Student ID");
    TableColumn<String, String> nameCol = new TableColumn<>("Name");
    TableColumn<String, String> emailCol = new TableColumn<>("Email");
    studentTable.getColumns().addAll(idCol, nameCol, emailCol);

    HBox buttonBox = new HBox(10);
    Button addBtn = new Button("Add Student");
    Button removeBtn = new Button("Remove Student");
    buttonBox.getChildren().addAll(addBtn, removeBtn);

    VBox vbox = new VBox(10);
    vbox.getChildren().addAll(grid, new Separator(), studentTable, buttonBox);

    tab.setContent(vbox);
    return tab;
}


public void main() {
}