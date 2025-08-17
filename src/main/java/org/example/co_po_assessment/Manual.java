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

public class Manual extends Application {
    private TabPane marksEntryTabPane;
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

    // Store reference to primary stage
    private Stage primaryStage;

    // Add this to keep track of marks entry tabs
    private Map<String, TableView<StudentMark>> marksEntryTables = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        initializeSampleData();
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

    private Tab createMarksEntryTab() {
        Tab tab = new Tab("Marks Entry");
        tab.setClosable(false);

        marksEntryTabPane = new TabPane(); // Store reference

        marksEntryTabPane.getTabs().addAll(
                createAssessmentEntryTab("Quiz1"),
                createAssessmentEntryTab("Quiz2"),
                createAssessmentEntryTab("Mid"),
                createAssessmentEntryTab("Final")
        );

        tab.setContent(marksEntryTabPane);
        return tab;
    }

    private void refreshMarksEntryTab() {
        if (marksEntryTabPane != null) {
            // Clear existing tabs
            marksEntryTabPane.getTabs().clear();

            // Recreate tabs with updated question columns
            marksEntryTabPane.getTabs().addAll(
                    createAssessmentEntryTab("Quiz1"),
                    createAssessmentEntryTab("Quiz2"),
                    createAssessmentEntryTab("Mid"),
                    createAssessmentEntryTab("Final")
            );
        }
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

        // Initialize marks data AFTER adding students
        initializeMarksData();
    }

    // Add this method to properly initialize marks data
    private void initializeMarksData() {
        String[] assessmentTypes = {"Quiz1", "Quiz2", "Quiz3", "Quiz4", "Mid", "Final"};

        marksData.clear(); // Clear existing data

        for (String assessmentType : assessmentTypes) {
            ObservableList<StudentMark> marksList = FXCollections.observableArrayList();

            // Add a StudentMark entry for each student for this assessment type
            for (Student student : students) {
                marksList.add(new StudentMark(student.getId(), assessmentType));
            }

            marksData.put(assessmentType, marksList);
        }
    }

    // Add this method to refresh marks data when students are added/removed
    private void refreshMarksData() {
        for (Map.Entry<String, ObservableList<StudentMark>> entry : marksData.entrySet()) {
            String assessmentType = entry.getKey();
            ObservableList<StudentMark> marksList = entry.getValue();

            // Clear existing marks
            marksList.clear();

            // Add marks for all current students
            for (Student student : students) {
                marksList.add(new StudentMark(student.getId(), assessmentType));
            }
        }

        // Refresh the marks entry tables
        refreshMarksEntryTables();
    }

    // Add this method to refresh marks entry tables
    private void refreshMarksEntryTables() {
        for (TableView<StudentMark> table : marksEntryTables.values()) {
            if (table != null) {
                table.refresh();
            }
        }
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

    private void saveData() {
        // Implement data saving logic
        // Could save to file or database
        System.out.println("Data saved (implementation needed)");
    }

    private void showCourseEditDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Course Information");
        dialog.setHeaderText("Enter course details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField codeField = new TextField();
        TextField titleField = new TextField();
        TextField instructorField = new TextField();
        TextField yearField = new TextField();
        TextField creditField = new TextField();
        TextField programField = new TextField();
        TextField deptField = new TextField();

        if (currentCourse != null) {
            codeField.setText(currentCourse.getCode());
            titleField.setText(currentCourse.getTitle());
            instructorField.setText(currentCourse.getInstructor());
            yearField.setText(currentCourse.getAcademicYear());
            creditField.setText(String.valueOf(currentCourse.getCredit()));
            programField.setText(currentCourse.getProgram());
            deptField.setText(currentCourse.getDepartment());
        }

        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Course Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Instructor:"), 0, 2);
        grid.add(instructorField, 1, 2);
        grid.add(new Label("Academic Year:"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("Credit:"), 0, 4);
        grid.add(creditField, 1, 4);
        grid.add(new Label("Program:"), 0, 5);
        grid.add(programField, 1, 5);
        grid.add(new Label("Department:"), 0, 6);
        grid.add(deptField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a Course object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new Course(
                            codeField.getText(),
                            titleField.getText(),
                            instructorField.getText(),
                            yearField.getText(),
                            Double.parseDouble(creditField.getText()),
                            programField.getText(),
                            deptField.getText()
                    );
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid credit value").show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            currentCourse = course;
            // Update UI
        });
    }

    private void generateReport() {
        // Implement report generation
        // Could generate PDF or Excel report
        System.out.println("Report generated (implementation needed)");
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
        TextField codeField = new TextField();
        codeField.setEditable(false);
        grid.add(codeField, 1, 1);

        grid.add(new Label("Course Title:"), 0, 2);
        TextField titleField = new TextField();
        titleField.setEditable(false);
        grid.add(titleField, 1, 2);

        grid.add(new Label("Instructor:"), 0, 3);
        TextField instructorField = new TextField();
        instructorField.setEditable(false);
        grid.add(instructorField, 1, 3);

        grid.add(new Label("Academic Year:"), 0, 4);
        TextField yearField = new TextField();
        yearField.setEditable(false);
        grid.add(yearField, 1, 4);

        if (currentCourse != null) {
            codeField.setText(currentCourse.getCode());
            titleField.setText(currentCourse.getTitle());
            instructorField.setText(currentCourse.getInstructor());
            yearField.setText(currentCourse.getAcademicYear());
        }

        studentTable = new TableView<>();
        studentTable.setEditable(true);

        TableColumn<Student, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Student, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setCellFactory(TextFieldTableCell.forTableColumn());

        studentTable.getColumns().addAll(idCol, nameCol, emailCol);
        studentTable.setItems(students);

        HBox buttonBox = new HBox(10);
        Button addBtn = new Button("Add Student");
        addBtn.setOnAction(e -> showAddStudentDialog());

        Button removeBtn = new Button("Remove Student");
        removeBtn.setOnAction(e -> {
            Student selected = studentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                students.remove(selected);
                // Refresh marks data after removing student
                refreshMarksData();
            }
        });

        buttonBox.getChildren().addAll(addBtn, removeBtn);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(grid, new Separator(), studentTable, buttonBox);

        tab.setContent(vbox);
        return tab;
    }

    private void showAddStudentDialog() {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add Student");
        dialog.setHeaderText("Enter student details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField contactField = new TextField();

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Contact No:"), 0, 3);
        grid.add(contactField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Student(
                        idField.getText(),
                        nameField.getText(),
                        emailField.getText(),
                        contactField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(student -> {
            students.add(student);
            // Refresh marks data after adding new student
            refreshMarksData();
        });
    }

    private Tab createQuestionInfoTab() {
        Tab tab = new Tab("Question Information");
        tab.setClosable(false);

        VBox quizBox = new VBox(10);
        quizBox.setPadding(new Insets(10));
        quizBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label quizLabel = new Label("Quiz Questions");
        quizLabel.setStyle("-fx-font-weight: bold;");

        quizTable = new TableView<>();
        quizTable.setEditable(true);

        TableColumn<AssessmentQuestion, String> qNoCol = new TableColumn<>("Question");
        qNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        qNoCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<AssessmentQuestion, Double> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        marksCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        TableColumn<AssessmentQuestion, String> coCol = new TableColumn<>("CO");
        coCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        coCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<AssessmentQuestion, String> poCol = new TableColumn<>("PO");
        poCol.setCellValueFactory(new PropertyValueFactory<>("po"));
        poCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<AssessmentQuestion, String> typeCol = new TableColumn<>("Quiz");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("assessmentType"));
        typeCol.setCellFactory(TextFieldTableCell.forTableColumn());

        quizTable.getColumns().addAll(qNoCol, marksCol, coCol, poCol, typeCol);
        quizTable.setItems(quizQuestions);

        HBox quizButtonBox = new HBox(10);
        Button addQuizBtn = new Button("Add Question");
        addQuizBtn.setOnAction(e -> showAddQuestionDialog("Quiz"));

        Button removeQuizBtn = new Button("Remove Question");
        removeQuizBtn.setOnAction(e -> {
            AssessmentQuestion selected = quizTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                quizQuestions.remove(selected);
                refreshMarksEntryTab();
            }
        });

        quizButtonBox.getChildren().addAll(addQuizBtn, removeQuizBtn);
        quizBox.getChildren().addAll(quizLabel, quizTable, quizButtonBox);

        VBox examBox = new VBox(10);
        examBox.setPadding(new Insets(10));
        examBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label examLabel = new Label("Mid/Final Questions");
        examLabel.setStyle("-fx-font-weight: bold;");

        examTable = new TableView<>();
        examTable.setEditable(true);

        TableColumn<AssessmentQuestion, String> eqNoCol = new TableColumn<>("Question");
        eqNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        eqNoCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<AssessmentQuestion, Double> emarksCol = new TableColumn<>("Marks");
        emarksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        emarksCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

        TableColumn<AssessmentQuestion, String> ecoCol = new TableColumn<>("CO");
        ecoCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        ecoCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<AssessmentQuestion, String> epoCol = new TableColumn<>("PO");
        epoCol.setCellValueFactory(new PropertyValueFactory<>("po"));
        epoCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<AssessmentQuestion, String> etypeCol = new TableColumn<>("Exam");
        etypeCol.setCellValueFactory(new PropertyValueFactory<>("assessmentType"));
        etypeCol.setCellFactory(TextFieldTableCell.forTableColumn());

        examTable.getColumns().addAll(eqNoCol, emarksCol, ecoCol, epoCol, etypeCol);
        examTable.setItems(examQuestions);

        HBox examButtonBox = new HBox(10);
        Button addExamBtn = new Button("Add Question");
        addExamBtn.setOnAction(e -> showAddQuestionDialog("Exam"));

        Button removeExamBtn = new Button("Remove Question");
        removeExamBtn.setOnAction(e -> {
            AssessmentQuestion selected = examTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                examQuestions.remove(selected);
                refreshMarksEntryTab();
            }
        });

        examButtonBox.getChildren().addAll(addExamBtn, removeExamBtn);
        examBox.getChildren().addAll(examLabel, examTable, examButtonBox);

        VBox mainBox = new VBox(20);
        mainBox.getChildren().addAll(quizBox, examBox);
        tab.setContent(mainBox);

        return tab;
    }

    private void showAddQuestionDialog(String type) {
        Dialog<AssessmentQuestion> dialog = new Dialog<>();
        dialog.setTitle("Add " + type + " Question");
        dialog.setHeaderText("Enter question details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField numberField = new TextField();
        TextField marksField = new TextField();

        ChoiceBox<String> coChoiceBox = new ChoiceBox<>();
        coChoiceBox.getItems().addAll("CO1", "CO2", "CO3", "CO4", "CO5", "CO6");

        ChoiceBox<String> poChoiceBox = new ChoiceBox<>();
        poChoiceBox.getItems().addAll("PO1", "PO2", "PO3", "PO4", "PO5", "PO6");

        ChoiceBox<String> assessmentChoiceBox = new ChoiceBox<>();
        if (type.equals("Quiz")) {
            assessmentChoiceBox.getItems().addAll("Quiz1", "Quiz2", "Quiz3", "Quiz4");
        } else {
            assessmentChoiceBox.getItems().addAll("Mid", "Final");
        }

        grid.add(new Label("Question Number:"), 0, 0);
        grid.add(numberField, 1, 0);
        grid.add(new Label("Marks:"), 0, 1);
        grid.add(marksField, 1, 1);
        grid.add(new Label("CO:"), 0, 2);
        grid.add(coChoiceBox, 1, 2);
        grid.add(new Label("PO:"), 0, 3);
        grid.add(poChoiceBox, 1, 3);
        grid.add(new Label("Assessment:"), 0, 4);
        grid.add(assessmentChoiceBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new AssessmentQuestion(
                            numberField.getText(),
                            Double.parseDouble(marksField.getText()),
                            coChoiceBox.getValue(),
                            poChoiceBox.getValue(),
                            assessmentChoiceBox.getValue()
                    );
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Invalid marks value").show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(question -> {
            if (type.equals("Quiz")) {
                quizQuestions.add(question);
            } else {
                examQuestions.add(question);
            }

            // Refresh the marks entry tabs to show new question columns
            refreshMarksEntryTab();
        });
    }

    private Tab createAssessmentEntryTab(String assessmentType) {
        Tab tab = new Tab(assessmentType);

        TableView<StudentMark> marksTable = new TableView<>();
        marksTable.setEditable(true);

        // Store reference to the marks table for refreshing
        marksEntryTables.put(assessmentType, marksTable);

        TableColumn<StudentMark, String> sidCol = new TableColumn<>("Student ID");
        sidCol.setCellValueFactory(cellData -> cellData.getValue().studentIdProperty());

        TableColumn<StudentMark, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData -> {
            String studentId = cellData.getValue().getStudentId();
            Student student = students.stream()
                    .filter(s -> s.getId().equals(studentId))
                    .findFirst()
                    .orElse(null);
            return student != null ? student.nameProperty() : new javafx.beans.property.SimpleStringProperty("");
        });

        marksTable.getColumns().addAll(sidCol, nameCol);

        // Get questions for this assessment type
        List<AssessmentQuestion> questions = getQuestionsForAssessment(assessmentType);

        for (AssessmentQuestion question : questions) {
            TableColumn<StudentMark, Double> qCol = new TableColumn<>(question.getNumber() + " (" + question.getMarks() + ")");

            qCol.setCellValueFactory(cellData -> {
                Double mark = cellData.getValue().getQuestionMarks().get(question.getNumber());
                return new javafx.beans.property.SimpleDoubleProperty(mark != null ? mark : 0.0).asObject();
            });

            qCol.setCellFactory(column -> new TextFieldTableCell<>(new DoubleStringConverter()));

            qCol.setOnEditCommit(event -> {
                StudentMark studentMark = event.getRowValue();
                Double newValue = event.getNewValue();
                if (newValue != null) {
                    studentMark.addQuestionMark(question.getNumber(), newValue);
                }
            });

            marksTable.getColumns().add(qCol);
        }

        TableColumn<StudentMark, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        marksTable.getColumns().add(totalCol);

        // Set the items for this assessment type
        marksTable.setItems(marksData.get(assessmentType));

        tab.setContent(marksTable);
        return tab;
    }

    // Helper method to get questions for a specific assessment type
    private List<AssessmentQuestion> getQuestionsForAssessment(String assessmentType) {
        List<AssessmentQuestion> questions = new ArrayList<>();

        if (assessmentType.startsWith("Quiz")) {
            questions = quizQuestions.stream()
                    .filter(q -> q.getAssessmentType().equals(assessmentType))
                    .toList();
        } else {
            questions = examQuestions.stream()
                    .filter(q -> q.getAssessmentType().equals(assessmentType))
                    .toList();
        }

        return questions;
    }

    private Tab createResultsTab() {
        Tab tab = new Tab("Results");
        tab.setClosable(false);

        VBox coBox = new VBox(10);
        coBox.setPadding(new Insets(10));
        coBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label coLabel = new Label("CO Attainment");
        coLabel.setStyle("-fx-font-weight: bold;");

        coTable = new TableView<>();

        TableColumn<Map.Entry<String, Double>, String> coNameCol = new TableColumn<>("CO");
        coNameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Double>, Double> coValueCol = new TableColumn<>("Attainment %");
        coValueCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getValue()).asObject());

        TableColumn<Map.Entry<String, Double>, String> coStatusCol = new TableColumn<>("Status");
        coStatusCol.setCellValueFactory(cellData -> {
            double value = cellData.getValue().getValue();
            String status = value >= 70 ? "Achieved" : value >= 50 ? "Partially Achieved" : "Not Achieved";
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        coTable.getColumns().addAll(coNameCol, coValueCol, coStatusCol);

        coBox.getChildren().addAll(coLabel, coTable);

        VBox poBox = new VBox(10);
        poBox.setPadding(new Insets(10));
        poBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label poLabel = new Label("PO Attainment");
        poLabel.setStyle("-fx-font-weight: bold;");

        poTable = new TableView<>();

        TableColumn<Map.Entry<String, Double>, String> poNameCol = new TableColumn<>("PO");
        poNameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Double>, Double> poValueCol = new TableColumn<>("Attainment %");
        poValueCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getValue()).asObject());

        TableColumn<Map.Entry<String, Double>, String> poStatusCol = new TableColumn<>("Status");
        poStatusCol.setCellValueFactory(cellData -> {
            double value = cellData.getValue().getValue();
            String status = value >= 70 ? "Achieved" : value >= 50 ? "Partially Achieved" : "Not Achieved";
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        poTable.getColumns().addAll(poNameCol, poValueCol, poStatusCol);

        poBox.getChildren().addAll(poLabel, poTable);

        HBox buttonBox = new HBox(10);
        Button calculateBtn = new Button("Calculate Results");
        calculateBtn.setOnAction(e -> calculateResults());

        Button exportBtn = new Button("Export Report");
        exportBtn.setOnAction(e -> generateReport());

        buttonBox.getChildren().addAll(calculateBtn, exportBtn);

        VBox mainBox = new VBox(20);
        mainBox.getChildren().addAll(coBox, poBox, buttonBox);
        tab.setContent(mainBox);

        return tab;
    }

    private void calculateResults() {
        Map<String, Double> coAttainment = calculateCOAttainment();
        coTable.setItems(FXCollections.observableArrayList(coAttainment.entrySet()));

        Map<String, Double> poAttainment = calculatePOAttainment();
        poTable.setItems(FXCollections.observableArrayList(poAttainment.entrySet()));
    }

    private Map<String, Double> calculateCOAttainment() {
        Map<String, Double> coAttainment = new HashMap<>();

        // average of all marks for each CO
        // should be replaced with actual CO calculation logic

        Map<String, List<AssessmentQuestion>> coQuestions = new HashMap<>();
        for (AssessmentQuestion q : quizQuestions) {
            coQuestions.computeIfAbsent(q.getCo(), k -> new ArrayList<>()).add(q);
        }
        for (AssessmentQuestion q : examQuestions) {
            coQuestions.computeIfAbsent(q.getCo(), k -> new ArrayList<>()).add(q);
        }

        for (Map.Entry<String, List<AssessmentQuestion>> entry : coQuestions.entrySet()) {
            String co = entry.getKey();
            List<AssessmentQuestion> questions = entry.getValue();

            double totalPossible = questions.stream().mapToDouble(AssessmentQuestion::getMarks).sum();
            double totalAchieved = 0;
            int studentCount = 0;

            for (Student student : students) {
                double studentTotal = 0;
                for (AssessmentQuestion q : questions) {

                    StudentMark mark = marksData.get(q.getAssessmentType()).stream()
                            .filter(m -> m.getStudentId().equals(student.getId()))
                            .findFirst()
                            .orElse(null);

                    if (mark != null) {
                        Double qMark = mark.getQuestionMarks().get(q.getNumber());
                        if (qMark != null) {
                            studentTotal += qMark;
                        }
                    }
                }
                totalAchieved += (studentTotal / totalPossible) * 100;
                studentCount++;
            }

            double averageAttainment = studentCount > 0 ? totalAchieved / studentCount : 0;
            coAttainment.put(co, averageAttainment);
        }

        return coAttainment;
    }

    private Map<String, Double> calculatePOAttainment() {
        Map<String, Double> poAttainment = new HashMap<>();

        // average of all marks for each PO
        // should be replaced with actual PO calculation logic

        Map<String, List<AssessmentQuestion>> poQuestions = new HashMap<>();
        for (AssessmentQuestion q : quizQuestions) {
            poQuestions.computeIfAbsent(q.getPo(), k -> new ArrayList<>()).add(q);
        }
        for (AssessmentQuestion q : examQuestions) {
            poQuestions.computeIfAbsent(q.getPo(), k -> new ArrayList<>()).add(q);
        }

        for (Map.Entry<String, List<AssessmentQuestion>> entry : poQuestions.entrySet()) {
            String po = entry.getKey();
            List<AssessmentQuestion> questions = entry.getValue();

            double totalPossible = questions.stream().mapToDouble(AssessmentQuestion::getMarks).sum();
            double totalAchieved = 0;
            int studentCount = 0;

            for (Student student : students) {
                double studentTotal = 0;
                for (AssessmentQuestion q : questions) {

                    StudentMark mark = marksData.get(q.getAssessmentType()).stream()
                            .filter(m -> m.getStudentId().equals(student.getId()))
                            .findFirst()
                            .orElse(null);

                    if (mark != null) {
                        Double qMark = mark.getQuestionMarks().get(q.getNumber());
                        if (qMark != null) {
                            studentTotal += qMark;
                        }
                    }
                }
                totalAchieved += (studentTotal / totalPossible) * 100;
                studentCount++;
            }

            double averageAttainment = studentCount > 0 ? totalAchieved / studentCount : 0;
            poAttainment.put(po, averageAttainment);
        }

        return poAttainment;
    }

    public void refreshQuestionTables() {
        // Refresh quiz table
        if (quizTable != null) {
            quizTable.refresh();
        }

        // Refresh exam table
        if (examTable != null) {
            examTable.refresh();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}