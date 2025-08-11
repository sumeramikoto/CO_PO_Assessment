import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
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
private Tab createQuestionInfoTab() {
    Tab tab = new Tab("Question Information");
    tab.setClosable(false);

    String[] coOptions = {"CO1", "CO2", "CO3", "CO4", "CO5"};
    String[] poOptions = {"PO1", "PO2", "PO3", "PO4", "PO5"};

    VBox quizBox = new VBox(10);
    quizBox.setPadding(new Insets(10));
    quizBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

    Label quizLabel = new Label("Quiz Questions");
    quizLabel.setStyle("-fx-font-weight: bold;");

    TableView<String> quizTable = new TableView<>();
    TableColumn<String, String> qNoCol = new TableColumn<>("Q.No");
    TableColumn<String, String> marksCol = new TableColumn<>("Marks");
    TableColumn<String, String> coCol = new TableColumn<>("CO");
    TableColumn<String, String> poCol = new TableColumn<>("PO");
    quizTable.getColumns().addAll(qNoCol, marksCol, coCol, poCol);

    HBox quizButtonBox = new HBox(10);
    Button addQuizBtn = new Button("Add Question");
    Button removeQuizBtn = new Button("Remove Question");
    quizButtonBox.getChildren().addAll(addQuizBtn, removeQuizBtn);

    quizBox.getChildren().addAll(quizLabel, quizTable, quizButtonBox);

    VBox examBox = new VBox(10);
    examBox.setPadding(new Insets(10));
    examBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

    Label examLabel = new Label("Mid/Final Questions");
    examLabel.setStyle("-fx-font-weight: bold;");

    TableView<String> examTable = new TableView<>();
    examTable.getColumns().addAll(
            new TableColumn<>("Q.No"),
            new TableColumn<>("Marks"),
            new TableColumn<>("CO"),
            new TableColumn<>("PO")
    );

    HBox examButtonBox = new HBox(10);
    Button addExamBtn = new Button("Add Question");
    Button removeExamBtn = new Button("Remove Question");
    examButtonBox.getChildren().addAll(addExamBtn, removeExamBtn);

    examBox.getChildren().addAll(examLabel, examTable, examButtonBox);

    VBox mainBox = new VBox(20);
    mainBox.getChildren().addAll(quizBox, examBox);
    tab.setContent(mainBox);

    return tab;
}

private Tab createMarksEntryTab() {
    Tab tab = new Tab("Marks Entry");
    tab.setClosable(false);

    TabPane entryTabs = new TabPane();
    entryTabs.getTabs().addAll(
            createQuizEntryTab("Quiz 1"),
            createQuizEntryTab("Quiz 2"),
            createExamEntryTab("Mid Exam"),
            createExamEntryTab("Final Exam")
    );

    tab.setContent(entryTabs);
    return tab;
}

private Tab createQuizEntryTab(String title) {
    Tab tab = new Tab(title);

    TableView<String> marksTable = new TableView<>();
    TableColumn<String, String> sidCol = new TableColumn<>("Student ID");
    TableColumn<String, String> nameCol = new TableColumn<>("Name");
    
    for (int i = 1; i <= 5; i++) {
        TableColumn<String, String> qCol = new TableColumn<>("Q" + i);
        marksTable.getColumns().add(qCol);
    }

    TableColumn<String, String> totalCol = new TableColumn<>("Total");
    marksTable.getColumns().add(totalCol);

    VBox vbox = new VBox(10);
    vbox.getChildren().add(marksTable);
    tab.setContent(vbox);

    return tab;
}




public void main() {
}