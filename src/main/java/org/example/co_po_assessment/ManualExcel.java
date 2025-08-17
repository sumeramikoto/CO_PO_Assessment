package org.example.co_po_assessment;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManualExcel extends Application {

    private String currentCourseId = "CSE4341"; // Default course ID - can be set dynamically
    private DatabaseService dbService = DatabaseService.getInstance();

    // Tables for questions
    private TableView<DatabaseService.QuestionData> quizTable;
    private TableView<DatabaseService.QuestionData> examTable;
    private TableView<DatabaseService.StudentData> studentTable;

    // Course info fields
    private TextField courseCodeField;
    private TextField courseTitleField;
    private TextField instructorField;
    private TextField academicYearField;

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

        // Initialize data
        loadCourseData();
        loadStudentData();
        refreshQuestionTables();
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
        courseCodeField = new TextField();
        grid.add(courseCodeField, 1, 1);

        grid.add(new Label("Course Title:"), 0, 2);
        courseTitleField = new TextField();
        grid.add(courseTitleField, 1, 2);

        grid.add(new Label("Instructor:"), 0, 3);
        instructorField = new TextField();
        grid.add(instructorField, 1, 3);

        grid.add(new Label("Academic Year:"), 0, 4);
        academicYearField = new TextField("2024-2025");
        grid.add(academicYearField, 1, 4);

        // Add save button for course information
        Button saveCourseBtn = new Button("Save Course Info");
        saveCourseBtn.setOnAction(e -> saveCourseInformation());
        grid.add(saveCourseBtn, 0, 5, 2, 1);

        // Student table
        studentTable = new TableView<>();
        TableColumn<DatabaseService.StudentData, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().id)));

        TableColumn<DatabaseService.StudentData, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));

        TableColumn<DatabaseService.StudentData, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().email));

        TableColumn<DatabaseService.StudentData, String> batchCol = new TableColumn<>("Batch");
        batchCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().batch)));

        studentTable.getColumns().addAll(idCol, nameCol, emailCol, batchCol);

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

        VBox quizBox = new VBox(10);
        quizBox.setPadding(new Insets(10));
        quizBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label quizLabel = new Label("Quiz Questions");
        quizLabel.setStyle("-fx-font-weight: bold;");

        // Quiz questions table
        quizTable = new TableView<>();
        TableColumn<DatabaseService.QuestionData, String> qNoCol = new TableColumn<>("Q.No");
        qNoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().title));

        TableColumn<DatabaseService.QuestionData, String> marksCol = new TableColumn<>("Marks");
        marksCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().marks)));

        TableColumn<DatabaseService.QuestionData, String> coCol = new TableColumn<>("CO");
        coCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().co));

        TableColumn<DatabaseService.QuestionData, String> poCol = new TableColumn<>("PO");
        poCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().po));

        quizTable.getColumns().addAll(qNoCol, marksCol, coCol, poCol);

        HBox quizButtonBox = new HBox(10);
        Button addQuizBtn = new Button("Add Question");
        addQuizBtn.setOnAction(e -> openQuestionInputWindow());
        Button removeQuizBtn = new Button("Remove Question");
        quizButtonBox.getChildren().addAll(addQuizBtn, removeQuizBtn);

        quizBox.getChildren().addAll(quizLabel, quizTable, quizButtonBox);

        // Mid/Final questions
        VBox examBox = new VBox(10);
        examBox.setPadding(new Insets(10));
        examBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-padding: 10;");

        Label examLabel = new Label("Mid/Final Questions");
        examLabel.setStyle("-fx-font-weight: bold;");

        examTable = new TableView<>();
        TableColumn<DatabaseService.QuestionData, String> examQNoCol = new TableColumn<>("Q.No");
        examQNoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().title));

        TableColumn<DatabaseService.QuestionData, String> examMarksCol = new TableColumn<>("Marks");
        examMarksCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().marks)));

        TableColumn<DatabaseService.QuestionData, String> examCoCol = new TableColumn<>("CO");
        examCoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().co));

        TableColumn<DatabaseService.QuestionData, String> examPoCol = new TableColumn<>("PO");
        examPoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().po));

        examTable.getColumns().addAll(examQNoCol, examMarksCol, examCoCol, examPoCol);

        HBox examButtonBox = new HBox(10);
        Button addExamBtn = new Button("Add Question");
        addExamBtn.setOnAction(e -> openQuestionInputWindow());
        Button removeExamBtn = new Button("Remove Question");
        examButtonBox.getChildren().addAll(addExamBtn, removeExamBtn);

        examBox.getChildren().addAll(examLabel, examTable, examButtonBox);

        VBox mainBox = new VBox(20);
        mainBox.getChildren().addAll(quizBox, examBox);
        tab.setContent(mainBox);

        return tab;
    }

    private void openQuestionInputWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("questionInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 600);

            Stage stage = new Stage();
            stage.setTitle("Input Question Info");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            // Get the controller and set the course context
            QuestionInputController controller = fxmlLoader.getController();
            controller.setCourseContext(currentCourseId, this);

            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to open question input window: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void refreshQuestionTables() {
        try {
            // Load quiz questions (all quizzes combined)
            ObservableList<DatabaseService.QuestionData> allQuizQuestions = FXCollections.observableArrayList();
            for (int i = 1; i <= 4; i++) {
                List<DatabaseService.QuestionData> quizQuestions = dbService.getQuizQuestions(currentCourseId, i);
                allQuizQuestions.addAll(quizQuestions);
            }
            quizTable.setItems(allQuizQuestions);

            // Load mid and final questions
            ObservableList<DatabaseService.QuestionData> allExamQuestions = FXCollections.observableArrayList();
            List<DatabaseService.QuestionData> midQuestions = dbService.getMidQuestions(currentCourseId);
            List<DatabaseService.QuestionData> finalQuestions = dbService.getFinalQuestions(currentCourseId);
            allExamQuestions.addAll(midQuestions);
            allExamQuestions.addAll(finalQuestions);
            examTable.setItems(allExamQuestions);

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load questions: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadCourseData() {
        try {
            DatabaseService.CourseData course = dbService.getCourseInfo(currentCourseId);
            if (course != null) {
                courseCodeField.setText(course.courseCode);
                courseTitleField.setText(course.courseName);
                instructorField.setText(course.instructorName);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load course data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadStudentData() {
        try {
            List<DatabaseService.StudentData> students = dbService.getEnrolledStudents(currentCourseId);
            ObservableList<DatabaseService.StudentData> studentList = FXCollections.observableArrayList(students);
            studentTable.setItems(studentList);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load student data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void saveCourseInformation() {
        try {
            // Validate input
            String courseCode = courseCodeField.getText().trim();
            String courseName = courseTitleField.getText().trim();
            String instructor = instructorField.getText().trim();

            if (courseCode.isEmpty() || courseName.isEmpty() || instructor.isEmpty()) {
                showAlert("Error", "Please fill in all course information fields", Alert.AlertType.ERROR);
                return;
            }

            // Save course information to database
            dbService.updateCourseInfo(currentCourseId, courseCode, courseName, 3.0); // Default credits
            dbService.updateCourseInstructor(currentCourseId, instructor);

            showAlert("Success", "Course information saved successfully!", Alert.AlertType.INFORMATION);

            // Reload data to reflect changes
            loadCourseData();

        } catch (SQLException e) {
            if (e.getMessage().contains("Instructor not found")) {
                showAlert("Error", "Instructor '" + instructorField.getText().trim() + "' not found in database. Please check the instructor name.", Alert.AlertType.ERROR);
            } else {
                showAlert("Database Error", "Failed to save course information: " + e.getMessage(), Alert.AlertType.ERROR);
            }
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private Tab createMarksEntryTab() {
        Tab tab = new Tab("Marks Entry");
        tab.setClosable(false);

        TabPane entryTabs = new TabPane();
        entryTabs.getTabs().addAll(
                createQuizEntryTab("Quiz 1"),
                createQuizEntryTab("Quiz 2"),
                createQuizEntryTab("Quiz 3"),
                createQuizEntryTab("Quiz 4"),
                createExamEntryTab("Mid Exam"),
                createExamEntryTab("Final Exam")
        );

        tab.setContent(entryTabs);
        return tab;
    }

    private Tab createQuizEntryTab(String title) {
        Tab tab = new Tab(title);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        try {
            int quizNumber = Integer.parseInt(title.split(" ")[1]);
            List<DatabaseService.StudentMarksData> marksData = dbService.getStudentQuizMarks(currentCourseId, quizNumber);

            if (marksData.isEmpty()) {
                Label noDataLabel = new Label("No questions found for " + title + ". Please add questions first.");
                vbox.getChildren().add(noDataLabel);
                tab.setContent(vbox);
                return tab;
            }

            // Create table for marks entry
            TableView<StudentQuizMarksRow> marksTable = new TableView<>();

            // Student info columns
            TableColumn<StudentQuizMarksRow, String> idCol = new TableColumn<>("Student ID");
            idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().studentId)));
            idCol.setPrefWidth(100);

            TableColumn<StudentQuizMarksRow, String> nameCol = new TableColumn<>("Student Name");
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentName));
            nameCol.setPrefWidth(150);

            marksTable.getColumns().addAll(idCol, nameCol);

            // Group marks data by student
            Map<Integer, StudentQuizMarksRow> studentRows = new HashMap<>();
            Map<String, Integer> questionColumns = new HashMap<>();

            for (DatabaseService.StudentMarksData mark : marksData) {
                if (!studentRows.containsKey(mark.studentId)) {
                    studentRows.put(mark.studentId, new StudentQuizMarksRow(mark.studentId, mark.studentName, quizNumber));
                }
                studentRows.get(mark.studentId).addQuestionMark(mark.questionTitle, mark.marksObtained, mark.questionId, mark.maxMarks);
                questionColumns.put(mark.questionTitle, mark.questionId);
            }

            // Create question columns dynamically
            List<String> sortedQuestions = questionColumns.keySet().stream().sorted().toList();
            for (String questionTitle : sortedQuestions) {
                TableColumn<StudentQuizMarksRow, String> qCol = new TableColumn<>(questionTitle);
                qCol.setCellValueFactory(data -> {
                    Double marks = data.getValue().getQuestionMark(questionTitle);
                    return new SimpleStringProperty(marks != null ? String.valueOf(marks) : "0");
                });
                qCol.setPrefWidth(80);

                // Make column editable
                qCol.setCellFactory(TextFieldTableCell.forTableColumn());
                qCol.setOnEditCommit(event -> {
                    try {
                        double newMarks = Double.parseDouble(event.getNewValue());
                        StudentQuizMarksRow row = event.getRowValue();
                        int questionId = questionColumns.get(questionTitle);

                        // Save to database
                        dbService.saveStudentQuizMarks(row.studentId, questionId, newMarks);
                        row.setQuestionMark(questionTitle, newMarks);

                        // Refresh total
                        marksTable.refresh();

                    } catch (NumberFormatException e) {
                        showAlert("Error", "Please enter a valid number", Alert.AlertType.ERROR);
                        marksTable.refresh();
                    } catch (SQLException e) {
                        showAlert("Database Error", "Failed to save marks: " + e.getMessage(), Alert.AlertType.ERROR);
                        marksTable.refresh();
                    }
                });

                marksTable.getColumns().add(qCol);
            }

            // Total column
            TableColumn<StudentQuizMarksRow, String> totalCol = new TableColumn<>("Total");
            totalCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTotalMarks())));
            totalCol.setPrefWidth(80);
            marksTable.getColumns().add(totalCol);

            // Add data to table
            marksTable.setItems(FXCollections.observableArrayList(studentRows.values()));
            marksTable.setEditable(true);

            // Add save button
            Button saveAllBtn = new Button("Save All Changes");
            saveAllBtn.setOnAction(e -> {
                showAlert("Info", "Changes are saved automatically when you edit cells", Alert.AlertType.INFORMATION);
            });

            vbox.getChildren().addAll(marksTable, saveAllBtn);

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load quiz marks: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        tab.setContent(vbox);
        return tab;
    }

    private Tab createExamEntryTab(String title) {
        Tab tab = new Tab(title);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        try {
            List<DatabaseService.StudentMarksData> marksData;
            if (title.contains("Mid")) {
                marksData = dbService.getStudentMidMarks(currentCourseId);
            } else {
                marksData = dbService.getStudentFinalMarks(currentCourseId);
            }

            if (marksData.isEmpty()) {
                Label noDataLabel = new Label("No questions found for " + title + ". Please add questions first.");
                vbox.getChildren().add(noDataLabel);
                tab.setContent(vbox);
                return tab;
            }

            // Create table for marks entry
            TableView<StudentExamMarksRow> marksTable = new TableView<>();

            // Student info columns
            TableColumn<StudentExamMarksRow, String> idCol = new TableColumn<>("Student ID");
            idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().studentId)));
            idCol.setPrefWidth(100);

            TableColumn<StudentExamMarksRow, String> nameCol = new TableColumn<>("Student Name");
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentName));
            nameCol.setPrefWidth(150);

            marksTable.getColumns().addAll(idCol, nameCol);

            // Group marks data by student
            Map<Integer, StudentExamMarksRow> studentRows = new HashMap<>();
            Map<String, Integer> questionColumns = new HashMap<>();

            for (DatabaseService.StudentMarksData mark : marksData) {
                if (!studentRows.containsKey(mark.studentId)) {
                    studentRows.put(mark.studentId, new StudentExamMarksRow(mark.studentId, mark.studentName, title));
                }
                studentRows.get(mark.studentId).addQuestionMark(mark.questionTitle, mark.marksObtained, mark.questionId, mark.maxMarks);
                questionColumns.put(mark.questionTitle, mark.questionId);
            }

            // Create question columns dynamically
            List<String> sortedQuestions = questionColumns.keySet().stream().sorted().toList();
            for (String questionTitle : sortedQuestions) {
                TableColumn<StudentExamMarksRow, String> qCol = new TableColumn<>(questionTitle);
                qCol.setCellValueFactory(data -> {
                    Double marks = data.getValue().getQuestionMark(questionTitle);
                    return new SimpleStringProperty(marks != null ? String.valueOf(marks) : "0");
                });
                qCol.setPrefWidth(80);

                // Make column editable
                qCol.setCellFactory(TextFieldTableCell.forTableColumn());
                qCol.setOnEditCommit(event -> {
                    try {
                        double newMarks = Double.parseDouble(event.getNewValue());
                        StudentExamMarksRow row = event.getRowValue();
                        int questionId = questionColumns.get(questionTitle);

                        // Save to database based on exam type
                        if (title.contains("Mid")) {
                            dbService.saveStudentMidMarks(row.studentId, questionId, newMarks);
                        } else {
                            dbService.saveStudentFinalMarks(row.studentId, questionId, newMarks);
                        }
                        row.setQuestionMark(questionTitle, newMarks);

                        // Refresh total
                        marksTable.refresh();

                    } catch (NumberFormatException e) {
                        showAlert("Error", "Please enter a valid number", Alert.AlertType.ERROR);
                        marksTable.refresh();
                    } catch (SQLException e) {
                        showAlert("Database Error", "Failed to save marks: " + e.getMessage(), Alert.AlertType.ERROR);
                        marksTable.refresh();
                    }
                });

                marksTable.getColumns().add(qCol);
            }

            // Total column
            TableColumn<StudentExamMarksRow, String> totalCol = new TableColumn<>("Total");
            totalCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTotalMarks())));
            totalCol.setPrefWidth(80);
            marksTable.getColumns().add(totalCol);

            // Add data to table
            marksTable.setItems(FXCollections.observableArrayList(studentRows.values()));
            marksTable.setEditable(true);

            // Add save button
            Button saveAllBtn = new Button("Save All Changes");
            saveAllBtn.setOnAction(e -> {
                showAlert("Info", "Changes are saved automatically when you edit cells", Alert.AlertType.INFORMATION);
            });

            vbox.getChildren().addAll(marksTable, saveAllBtn);

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load exam marks: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }

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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to set course context dynamically
    public void setCourseContext(String courseId) {
        this.currentCourseId = courseId;
        try {
            dbService.ensureAssessmentsExist(courseId);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to ensure assessments exist: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        loadCourseData();
        loadStudentData();
        refreshQuestionTables();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Helper classes for marks tables
    public static class StudentQuizMarksRow {
        public final int studentId;
        public final String studentName;
        public final int quizNumber;
        private Map<String, Double> questionMarks;
        private Map<String, Integer> questionIds;
        private Map<String, Double> maxMarks;

        public StudentQuizMarksRow(int studentId, String studentName, int quizNumber) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.quizNumber = quizNumber;
            this.questionMarks = new HashMap<>();
            this.questionIds = new HashMap<>();
            this.maxMarks = new HashMap<>();
        }

        public void addQuestionMark(String questionTitle, double marks, int questionId, double maxMark) {
            questionMarks.put(questionTitle, marks);
            questionIds.put(questionTitle, questionId);
            maxMarks.put(questionTitle, maxMark);
        }

        public Double getQuestionMark(String questionTitle) {
            return questionMarks.get(questionTitle);
        }

        public void setQuestionMark(String questionTitle, double marks) {
            questionMarks.put(questionTitle, marks);
        }

        public double getTotalMarks() {
            return questionMarks.values().stream().mapToDouble(Double::doubleValue).sum();
        }
    }

    public static class StudentExamMarksRow {
        public final int studentId;
        public final String studentName;
        public final String examType;
        private Map<String, Double> questionMarks;
        private Map<String, Integer> questionIds;
        private Map<String, Double> maxMarks;

        public StudentExamMarksRow(int studentId, String studentName, String examType) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.examType = examType;
            this.questionMarks = new HashMap<>();
            this.questionIds = new HashMap<>();
            this.maxMarks = new HashMap<>();
        }

        public void addQuestionMark(String questionTitle, double marks, int questionId, double maxMark) {
            questionMarks.put(questionTitle, marks);
            questionIds.put(questionTitle, questionId);
            maxMarks.put(questionTitle, maxMark);
        }

        public Double getQuestionMark(String questionTitle) {
            return questionMarks.get(questionTitle);
        }

        public void setQuestionMark(String questionTitle, double marks) {
            questionMarks.put(questionTitle, marks);
        }

        public double getTotalMarks() {
            return questionMarks.values().stream().mapToDouble(Double::doubleValue).sum();
        }
    }
}
