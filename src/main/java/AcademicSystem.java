import javafx.application.Application;
import javafx.stage.Stage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;

public class AcademicSystem extends Application {

    private Stage primaryStage;
    private TextField emailField;
    private PasswordField passwordField;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Academic System Login");
        showLoginScreen();
    }

    private void showLoginScreen() {
        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(20));

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER);
        Label title = new Label("ACADEMIC MANAGEMENT SYSTEM");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.DARKBLUE);
        headerBox.getChildren().add(title);
        borderPane.setTop(headerBox);

        // Center form
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label scenetitle = new Label("Login");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        // User type selection
        ToggleGroup userTypeGroup = new ToggleGroup();
        RadioButton adminRadio = new RadioButton("Admin");
        adminRadio.setToggleGroup(userTypeGroup);
        adminRadio.setSelected(true);

        RadioButton facultyRadio = new RadioButton("Faculty");
        facultyRadio.setToggleGroup(userTypeGroup);

        HBox radioBox = new HBox(10, adminRadio, facultyRadio);
        grid.add(new Label("Login as:"), 0, 1);
        grid.add(radioBox, 1, 1);

        // Email field
        emailField = new TextField();
        emailField.setPromptText("yourname@institution.edu");
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        // Password field
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);

        // Login button
        Button loginBtn = new Button("Sign In");
        loginBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(loginBtn);
        grid.add(hbBtn, 1, 4);

        final Label messageLabel = new Label();
        grid.add(messageLabel, 1, 6);

        borderPane.setCenter(grid);

        // Event handlers
        loginBtn.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            boolean isAdmin = adminRadio.isSelected();

            if (email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Email and password are required!");
                messageLabel.setTextFill(Color.FIREBRICK);
                return;
            }

            boolean authenticated = authenticateUser(email, password, isAdmin);

            if (authenticated) {
                messageLabel.setText("Login successful!");
                messageLabel.setTextFill(Color.GREEN);
                if (isAdmin) {
                    showAdminDashboard();
                } else {
                    showFacultyDashboard();
                }
            } else {
                messageLabel.setText("Invalid credentials!");
                messageLabel.setTextFill(Color.FIREBRICK);
            }
        });

        Scene scene = new Scene(borderPane, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean authenticateUser(String email, String password, boolean isAdmin) {
        // Demo authentication
        if (isAdmin) {
            return email.equalsIgnoreCase("admin@school.edu") && password.equals("admin123");
        } else {
            return email.equalsIgnoreCase("faculty@school.edu") && password.equals("faculty123");
        }
    }

    private void showAdminDashboard() {
        BorderPane adminPane = new BorderPane();

        // Menu Bar
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem logoutItem = new MenuItem("Logout");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);

        Menu dataMenu = new Menu("Data");
        MenuItem coursesItem = new MenuItem("View Courses");
        MenuItem studentsItem = new MenuItem("View Students");
        MenuItem attainmentItem = new MenuItem("View Attainment Data");
        dataMenu.getItems().addAll(coursesItem, studentsItem, attainmentItem);

        Menu reportsMenu = new Menu("Reports");
        MenuItem poReportItem = new MenuItem("PO Achievement Report");
        MenuItem trendsItem = new MenuItem("View Trends");
        reportsMenu.getItems().addAll(poReportItem, trendsItem);

        menuBar.getMenus().addAll(fileMenu, dataMenu, reportsMenu);
        adminPane.setTop(menuBar);

        // TabPane for different views
        TabPane tabPane = new TabPane();

        // Courses Tab
        Tab coursesTab = new Tab("Courses");
        coursesTab.setContent(createCoursesTable());

        // Students Tab
        Tab studentsTab = new Tab("Students");
        studentsTab.setContent(createStudentsTable());

        // Attainment Tab
        Tab attainmentTab = new Tab("Attainment Data");
        attainmentTab.setContent(createAttainmentView());

        // PO Achievement Tab
        Tab poAchievementTab = new Tab("PO Achievement");
        poAchievementTab.setContent(createPOAchievementView());

        tabPane.getTabs().addAll(coursesTab, studentsTab, attainmentTab, poAchievementTab);
        adminPane.setCenter(tabPane);

        // Status bar
        Label statusLabel = new Label("Admin Dashboard - Ready");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.setPadding(new Insets(5));
        adminPane.setBottom(statusBox);

        // Event handlers
        logoutItem.setOnAction(e -> showLoginScreen());
        exitItem.setOnAction(e -> primaryStage.close());

        coursesItem.setOnAction(e -> tabPane.getSelectionModel().select(coursesTab));
        studentsItem.setOnAction(e -> tabPane.getSelectionModel().select(studentsTab));
        attainmentItem.setOnAction(e -> tabPane.getSelectionModel().select(attainmentTab));
        poReportItem.setOnAction(e -> tabPane.getSelectionModel().select(poAchievementTab));

        trendsItem.setOnAction(e -> {
            Stage chartStage = new Stage();
            chartStage.setTitle("Performance Trends");
            chartStage.setScene(new Scene(createTrendsChart(), 800, 600));
            chartStage.show();
        });

        Scene adminScene = new Scene(adminPane, 1000, 700);
        primaryStage.setScene(adminScene);
        primaryStage.setMaximized(true);
    }

    private void showFacultyDashboard() {
        BorderPane facultyPane = new BorderPane();

        // Menu Bar
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem logoutItem = new MenuItem("Logout");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(logoutItem, new SeparatorMenuItem(), exitItem);

        Menu dataMenu = new Menu("Data");
        MenuItem inputDetailsItem = new MenuItem("Input Course/Student Details");
        MenuItem downloadTemplateItem = new MenuItem("Download Excel Template");
        MenuItem uploadTemplateItem = new MenuItem("Upload Filled Template");
        dataMenu.getItems().addAll(inputDetailsItem, downloadTemplateItem, uploadTemplateItem);

        Menu reportsMenu = new Menu("Reports");
        MenuItem coPoReportItem = new MenuItem("Generate CO/PO Report");
        MenuItem downloadReportItem = new MenuItem("Download Report");
        reportsMenu.getItems().addAll(coPoReportItem, downloadReportItem);

        menuBar.getMenus().addAll(fileMenu, dataMenu, reportsMenu);
        facultyPane.setTop(menuBar);

        // TabPane for different views
        TabPane tabPane = new TabPane();

        // Input Details Tab
        Tab inputDetailsTab = new Tab("Input Details");
        inputDetailsTab.setContent(createInputDetailsForm());

        // Template Tab
        Tab templateTab = new Tab("Templates");
        templateTab.setContent(createTemplateView());

        // Reports Tab
        Tab reportsTab = new Tab("Reports");
        reportsTab.setContent(createFacultyReportsView());

        tabPane.getTabs().addAll(inputDetailsTab, templateTab, reportsTab);
        facultyPane.setCenter(tabPane);

        // Status bar
        Label statusLabel = new Label("Faculty Dashboard - Ready");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        HBox statusBox = new HBox(statusLabel);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.setPadding(new Insets(5));
        facultyPane.setBottom(statusBox);

        // Event handlers
        logoutItem.setOnAction(e -> showLoginScreen());
        exitItem.setOnAction(e -> primaryStage.close());

        inputDetailsItem.setOnAction(e -> tabPane.getSelectionModel().select(inputDetailsTab));
        downloadTemplateItem.setOnAction(e -> downloadExcelTemplate());
        uploadTemplateItem.setOnAction(e -> uploadFilledTemplate());
        coPoReportItem.setOnAction(e -> tabPane.getSelectionModel().select(reportsTab));
        downloadReportItem.setOnAction(e -> downloadReport());

        Scene facultyScene = new Scene(facultyPane, 1000, 700);
        primaryStage.setScene(facultyScene);
        primaryStage.setMaximized(true);
    }

    // Admin Dashboard Components
    private TableView<Course> createCoursesTable() {
        TableView<Course> table = new TableView<>();

        TableColumn<Course, String> idCol = new TableColumn<>("Course ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Course, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));

        table.getColumns().addAll(idCol, nameCol, deptCol, creditsCol);

        // Sample data
        ObservableList<Course> courses = FXCollections.observableArrayList(
                new Course("CS101", "Introduction to Programming", "Computer Science", 3),
                new Course("CS201", "Data Structures", "Computer Science", 4),
                new Course("MA101", "Calculus I", "Mathematics", 4)
        );

        table.setItems(courses);
        return table;
    }

    private TableView<Student> createStudentsTable() {
        TableView<Student> table = new TableView<>();

        TableColumn<Student, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> programCol = new TableColumn<>("Program");
        programCol.setCellValueFactory(new PropertyValueFactory<>("program"));

        TableColumn<Student, Integer> batchCol = new TableColumn<>("Batch");
        batchCol.setCellValueFactory(new PropertyValueFactory<>("batch"));

        table.getColumns().addAll(idCol, nameCol, programCol, batchCol);

        // Sample data
        ObservableList<Student> students = FXCollections.observableArrayList(
                new Student("S001", "John Doe", "B.Tech CSE", 2022),
                new Student("S002", "Jane Smith", "B.Tech CSE", 2022),
                new Student("S003", "Robert Johnson", "B.Tech ECE", 2023)
        );

        table.setItems(students);
        return table;
    }

    private VBox createAttainmentView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Course Attainment Data");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Course selection
        HBox courseSelection = new HBox(10);
        courseSelection.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> courseCombo = new ComboBox<>();
        courseCombo.getItems().addAll("CS101 - Introduction to Programming",
                "CS201 - Data Structures",
                "MA101 - Calculus I");
        courseCombo.setPromptText("Select Course");

        Button viewBtn = new Button("View Attainment");
        viewBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        courseSelection.getChildren().addAll(new Label("Select Course:"), courseCombo, viewBtn);

        // Attainment table
        TableView<Attainment> attainmentTable = new TableView<>();

        TableColumn<Attainment, String> coCol = new TableColumn<>("Course Outcome");
        coCol.setCellValueFactory(new PropertyValueFactory<>("co"));

        TableColumn<Attainment, Double> attainmentCol = new TableColumn<>("Attainment Level");
        attainmentCol.setCellValueFactory(new PropertyValueFactory<>("level"));

        TableColumn<Attainment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        attainmentTable.getColumns().addAll(coCol, attainmentCol, statusCol);

        // Sample data
        ObservableList<Attainment> attainments = FXCollections.observableArrayList(
                new Attainment("CO1", 78.5, "Met"),
                new Attainment("CO2", 65.2, "Partially Met"),
                new Attainment("CO3", 82.1, "Met"),
                new Attainment("CO4", 71.8, "Met")
        );

        attainmentTable.setItems(attainments);

        viewBtn.setOnAction(e -> {
            if (courseCombo.getValue() != null) {
                // In real app, would load data for selected course
                attainmentTable.setItems(attainments);
            }
        });

        vbox.getChildren().addAll(title, courseSelection, attainmentTable);
        return vbox;
    }

    private VBox createPOAchievementView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Program Outcome Achievement by Batch");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Batch selection
        HBox batchSelection = new HBox(10);
        batchSelection.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> batchCombo = new ComboBox<>();
        batchCombo.getItems().addAll("2020", "2021", "2022", "2023");
        batchCombo.setPromptText("Select Batch");

        Button viewBtn = new Button("View PO Achievement");
        viewBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        batchSelection.getChildren().addAll(new Label("Select Batch:"), batchCombo, viewBtn);

        // PO Achievement chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Achievement %");

        BarChart<String, Number> poChart = new BarChart<>(xAxis, yAxis);
        poChart.setTitle("PO Achievement");
        poChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 1; i <= 12; i++) {
            series.getData().add(new XYChart.Data<>("PO" + i, 70 + (Math.random() * 30)));
        }
        poChart.getData().add(series);

        // PO Status table
        TableView<POStatus> statusTable = new TableView<>();

        TableColumn<POStatus, String> poCol = new TableColumn<>("Program Outcome");
        poCol.setCellValueFactory(new PropertyValueFactory<>("po"));

        TableColumn<POStatus, Double> achievementCol = new TableColumn<>("Achievement %");
        achievementCol.setCellValueFactory(new PropertyValueFactory<>("achievement"));

        TableColumn<POStatus, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusTable.getColumns().addAll(poCol, achievementCol, statusCol);

        ObservableList<POStatus> poStatusList = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            double achievement = 70 + (Math.random() * 30);
            poStatusList.add(new POStatus("PO" + i, achievement,
                    achievement >= 70 ? "Achieved" : "Not Achieved"));
        }
        statusTable.setItems(poStatusList);

        viewBtn.setOnAction(e -> {
            if (batchCombo.getValue() != null) {
                // In real app, would load data for selected batch
                poChart.getData().clear();
                poChart.getData().add(series);
                statusTable.setItems(poStatusList);
            }
        });

        vbox.getChildren().addAll(title, batchSelection, poChart, statusTable);
        return vbox;
    }

    private BarChart<String, Number> createTrendsChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Academic Year");

        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Attainment %");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Performance Trends Over Years");

        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("CO Attainment");
        series1.getData().add(new XYChart.Data<>("2019-20", 72));
        series1.getData().add(new XYChart.Data<>("2020-21", 75));
        series1.getData().add(new XYChart.Data<>("2021-22", 78));
        series1.getData().add(new XYChart.Data<>("2022-23", 82));

        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("PO Attainment");
        series2.getData().add(new XYChart.Data<>("2019-20", 68));
        series2.getData().add(new XYChart.Data<>("2020-21", 72));
        series2.getData().add(new XYChart.Data<>("2021-22", 75));
        series2.getData().add(new XYChart.Data<>("2022-23", 79));

        chart.getData().addAll(series1, series2);
        return chart;
    }

    // Faculty Dashboard Components
    private GridPane createInputDetailsForm() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label title = new Label("Input Course/Student Details");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        grid.add(title, 0, 0, 2, 1);

        // Course selection
        grid.add(new Label("Course:"), 0, 1);
        ComboBox<String> courseCombo = new ComboBox<>();
        courseCombo.getItems().addAll("CS101 - Introduction to Programming",
                "CS201 - Data Structures",
                "MA101 - Calculus I");
        grid.add(courseCombo, 1, 1);

        // Student selection
        grid.add(new Label("Student:"), 0, 2);
        ComboBox<String> studentCombo = new ComboBox<>();
        studentCombo.getItems().addAll("S001 - John Doe", "S002 - Jane Smith", "S003 - Robert Johnson");
        grid.add(studentCombo, 1, 2);

        // Marks input
        grid.add(new Label("Marks (Out of 100):"), 0, 3);
        TextField marksField = new TextField();
        marksField.setPromptText("Enter marks");
        grid.add(marksField, 1, 3);

        // CO-PO Mapping
        grid.add(new Label("CO-PO Mapping:"), 0, 4);
        TextArea mappingArea = new TextArea();
        mappingArea.setPromptText("Enter CO-PO mapping details");
        mappingArea.setPrefRowCount(3);
        grid.add(mappingArea, 1, 4);

        // Buttons
        Button saveBtn = new Button("Save Details");
        saveBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: #cccccc;");

        HBox buttonBox = new HBox(10, saveBtn, clearBtn);
        grid.add(buttonBox, 1, 5);

        // Event handlers
        saveBtn.setOnAction(e -> {
            if (courseCombo.getValue() != null && studentCombo.getValue() != null
                    && !marksField.getText().isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Details saved successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields!");
            }
        });

        clearBtn.setOnAction(e -> {
            courseCombo.getSelectionModel().clearSelection();
            studentCombo.getSelectionModel().clearSelection();
            marksField.clear();
            mappingArea.clear();
        });

        return grid;
    }

    private VBox createTemplateView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label title = new Label("Excel Templates");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label instruction = new Label("Download templates for data entry or upload filled templates:");
        instruction.setWrapText(true);

        // Template list
        ListView<String> templateList = new ListView<>();
        templateList.getItems().addAll(
                "Course Attainment Template",
                "Student Marks Template",
                "CO-PO Mapping Template",
                "Question Bank Template"
        );
        templateList.setPrefHeight(150);

        // Buttons
        Button downloadBtn = new Button("Download Selected Template");
        downloadBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        Button uploadBtn = new Button("Upload Filled Template");
        uploadBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, downloadBtn, uploadBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // Event handlers
        downloadBtn.setOnAction(e -> {
            if (templateList.getSelectionModel().getSelectedItem() != null) {
                downloadExcelTemplate();
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a template to download");
            }
        });

        uploadBtn.setOnAction(e -> uploadFilledTemplate());

        vbox.getChildren().addAll(title, instruction, templateList, buttonBox);
        return vbox;
    }

    private VBox createFacultyReportsView() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        Label title = new Label("CO/PO Reports");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Course selection
        HBox courseSelection = new HBox(10);
        courseSelection.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> courseCombo = new ComboBox<>();
        courseCombo.getItems().addAll("CS101 - Introduction to Programming",
                "CS201 - Data Structures",
                "MA101 - Calculus I");
        courseCombo.setPromptText("Select Course");

        Button generateBtn = new Button("Generate Report");
        generateBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        courseSelection.getChildren().addAll(new Label("Select Course:"), courseCombo, generateBtn);

        // Report display
        TextArea reportArea = new TextArea();
        reportArea.setPrefRowCount(10);
        reportArea.setEditable(false);
        reportArea.setText("Generated reports will appear here...");

        // Buttons
        Button downloadBtn = new Button("Download Report");
        downloadBtn.setStyle("-fx-background-color: #2a5cbd; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, downloadBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Event handlers
        generateBtn.setOnAction(e -> {
            if (courseCombo.getValue() != null) {
                reportArea.setText(generateSampleReport(courseCombo.getValue()));
            }
        });

        downloadBtn.setOnAction(e -> downloadReport());

        vbox.getChildren().addAll(title, courseSelection, reportArea, buttonBox);
        return vbox;
    }

    private void downloadExcelTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Template");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("template.xlsx");
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Template downloaded successfully to:\n" + file.getAbsolutePath());
        }
    }

    private void uploadFilledTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Filled Template");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Template uploaded successfully from:\n" + file.getAbsolutePath());
        }
    }

    private void downloadReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Word Files", "*.docx"));
        fileChooser.setInitialFileName("report.pdf");
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Report downloaded successfully to:\n" + file.getAbsolutePath());
        }
    }

    private String generateSampleReport(String course) {
        return "CO/PO ATTAINMENT REPORT\n" +
                "=======================\n" +
                "Course: " + course + "\n" +
                "Date: " + java.time.LocalDate.now() + "\n\n" +
                "Course Outcomes:\n" +
                "1. CO1: 78.5% (Met)\n" +
                "2. CO2: 65.2% (Partially Met)\n" +
                "3. CO3: 82.1% (Met)\n" +
                "4. CO4: 71.8% (Met)\n\n" +
                "Program Outcomes Mapping:\n" +
                "PO1: 75.3% (Achieved)\n" +
                "PO2: 68.9% (Achieved)\n" +
                "PO3: 72.4% (Achieved)\n" +
                "PO4: 81.2% (Achieved)\n\n" +
                "Recommendations:\n" +
                "- Focus on improving CO2 attainment\n" +
                "- Review teaching methodology for topics related to CO2";
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Data model classes
    public static class Course {
        private final String id;
        private final String name;
        private final String department;
        private final int credits;

        public Course(String id, String name, String department, int credits) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.credits = credits;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDepartment() { return department; }
        public int getCredits() { return credits; }
    }

    public static class Student {
        private final String id;
        private final String name;
        private final String program;
        private final int batch;

        public Student(String id, String name, String program, int batch) {
            this.id = id;
            this.name = name;
            this.program = program;
            this.batch = batch;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getProgram() { return program; }
        public int getBatch() { return batch; }
    }

    public static class Attainment {
        private final String co;
        private final double level;
        private final String status;

        public Attainment(String co, double level, String status) {
            this.co = co;
            this.level = level;
            this.status = status;
        }

        public String getCo() { return co; }
        public double getLevel() { return level; }
        public String getStatus() { return status; }
    }

    public static class POStatus {
        private final String po;
        private final double achievement;
        private final String status;

        public POStatus(String po, double achievement, String status) {
            this.po = po;
            this.achievement = achievement;
            this.status = status;
        }

        public String getPo() { return po; }
        public double getAchievement() { return achievement; }
        public String getStatus() { return status; }
    }
}