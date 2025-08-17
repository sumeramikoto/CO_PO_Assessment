package org.example.co_po_assessment;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManualExcel extends Application {

    private Course currentCourse;
    private ObservableList<Student> students = FXCollections.observableArrayList();
    private ObservableList<AssessmentQuestion> quizQuestions = FXCollections.observableArrayList();
    private ObservableList<AssessmentQuestion> examQuestions = FXCollections.observableArrayList();
    private Map<String, ObservableList<StudentMark>> marksData = new HashMap<>();

    private TableView<Student> studentTable;
    private TableView<AssessmentQuestion> quizTable;
    private TableView<AssessmentQuestion> examTable;
    private TableView<Map.Entry<String, Double>> coTable;
    private TableView<Map.Entry<String, Double>> poTable;

    @Override
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


        initializeSampleData();
    }
    private void initializeSampleData() {

        currentCourse = new Course("CSE 4101", "Introduction to Data Structure", "Shariar Ivan",
                "2023-2024", 3.0, "SWE", "CSE");

        students.addAll(
                new Student("220042101", "Navid Ibrahim", "navidibhrahimovic@iut-dhaka.edu", "01717655515"),
                new Student("220042128", "Naybur Rahman Sinha", "sinhawiz@iut-dhaka.edu", "0144456416"),
                new Student("220042134", "Tahir Zaman Umar", "tahirumar@iut-dhaka.edu", "01779770359")
        );

        quizQuestions.addAll(
                new AssessmentQuestion("Q1", 5, "CO1", "PO1", "Quiz1"),
                new AssessmentQuestion("Q2", 5, "CO2", "PO2", "Quiz1"),
                new AssessmentQuestion("Q1", 10, "CO3", "PO3", "Quiz2")
        );

        examQuestions.addAll(
                new AssessmentQuestion("Q1", 20, "CO1", "PO1", "Mid"),
                new AssessmentQuestion("Q2", 20, "CO2", "PO2", "Mid"),
                new AssessmentQuestion("Q1", 30, "CO4", "PO4", "Final")
        );

        marksData.put("Quiz1", FXCollections.observableArrayList());
        marksData.put("Quiz2", FXCollections.observableArrayList());
        marksData.put("Mid", FXCollections.observableArrayList());
        marksData.put("Final", FXCollections.observableArrayList());
    }





    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        newItem.setOnAction(e -> resetApplication());

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveData());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(newItem, saveItem, new SeparatorMenuItem(), exitItem);

        Menu editMenu = new Menu("Edit");
        MenuItem editCourseItem = new MenuItem("Edit Course Info");
        editCourseItem.setOnAction(e -> showCourseEditDialog());
        editMenu.getItems().add(editCourseItem);

        Menu reportsMenu = new Menu("Reports");
        MenuItem genReportItem = new MenuItem("Generate Report");
        genReportItem.setOnAction(e -> generateReport());
        reportsMenu.getItems().add(genReportItem);

        menuBar.getMenus().addAll(fileMenu, editMenu, reportsMenu);
        return menuBar;
    }

    private void resetApplication() {
        currentCourse = null;
        students.clear();
        quizQuestions.clear();
        examQuestions.clear();
        marksData.values().forEach(ObservableList::clear);
        showCourseEditDialog();
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
    private Tab createExamEntryTab(String title) {
        Tab tab = new Tab(title);

        TableView<String> marksTable = new TableView<>();
        TableColumn<String, String> sidCol = new TableColumn<>("Student ID");
        TableColumn<String, String> nameCol = new TableColumn<>("Name");

        for (int i = 1; i <= 10; i++) {
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

    private Tab createResultsTab() {
        Tab tab = new Tab("Results");
        tab.setClosable(false);

        VBox coBox = new VBox(10);
        coBox.setPadding(new Insets(10));
        coBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label coLabel = new Label("CO Attainment");
        coLabel.setStyle("-fx-font-weight: bold;");

        TableView<String> coTable = new TableView<>();
        coTable.getColumns().addAll(
                new TableColumn<>("CO"),
                new TableColumn<>("Attainment %"),
                new TableColumn<>("Status")
        );

        coBox.getChildren().addAll(coLabel, coTable);

        VBox poBox = new VBox(10);
        poBox.setPadding(new Insets(10));
        poBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label poLabel = new Label("PO Attainment");
        poLabel.setStyle("-fx-font-weight: bold;");

        TableView<String> poTable = new TableView<>();
        poTable.getColumns().addAll(
                new TableColumn<>("PO"),
                new TableColumn<>("Attainment %"),
                new TableColumn<>("Status")
        );

        poBox.getChildren().addAll(poLabel, poTable);

        HBox buttonBox = new HBox(10);
        Button calculateBtn = new Button("Calculate Results");
        Button exportBtn = new Button("Export Report");
        buttonBox.getChildren().addAll(calculateBtn, exportBtn);

        VBox mainBox = new VBox(20);
        mainBox.getChildren().addAll(coBox, poBox, buttonBox);
        tab.setContent(mainBox);

        return tab;
    }


    public static void main(String[] args) {
        launch(args);
    }
}