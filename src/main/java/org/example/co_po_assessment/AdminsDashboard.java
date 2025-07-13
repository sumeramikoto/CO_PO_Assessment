package org.example.co_po_assessment;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.util.ArrayList;
import java.util.List;
public class AdminsDashboard extends Application {
    private List<Course> courses = new ArrayList<>();
    private VBox centerContent;
    
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #dedcdc;");

        HBox topBar = createTopBar(primaryStage);
        root.setTop(topBar);

        VBox leftMenu = createLeftMenu(primaryStage);
        root.setLeft(leftMenu);

        centerContent = createCenterContent();
        root.setCenter(centerContent);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Admin Dashboard - CO/PO Assessment System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar(Stage primaryStage) {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: #343a40;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        ImageView logo = new ImageView();
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);
        try {
            logo.setImage(new Image("file:/C:/Users/User/Desktop/Induction/iut_logo.png"));
        } catch (Exception e) {
            System.out.println("Couldn't load logo image");
        }

        Label welcomeLabel = new Label("Welcome, Admin");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logout");
            alert.setHeaderText(null);
            alert.setContentText("You have been logged out successfully!");
            alert.showAndWait();
            // Close dashboard and reopen login
            primaryStage.close();
            try {
                new AssessmentSystem().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox rightMenu = new HBox(20, welcomeLabel, logoutButton);
        rightMenu.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightMenu, Priority.ALWAYS);

        topBar.getChildren().addAll(logo, rightMenu);
        return topBar;
    }

    private VBox createLeftMenu(Stage primaryStage) {
        VBox leftMenu = new VBox(10);
        leftMenu.setPadding(new Insets(20));
        leftMenu.setStyle("-fx-background-color: #f8f9fa;");
        leftMenu.setPrefWidth(200);

        Label menuTitle = new Label("Admin Menu");
        menuTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Button addCourseBtn = new Button("Add Course");
        addCourseBtn.setMaxWidth(Double.MAX_VALUE);
        addCourseBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        addCourseBtn.setOnAction(e -> showAddCourseDialog());

        Button showStudentInfoBtn = new Button("Show Student Information");
        showStudentInfoBtn.setMaxWidth(Double.MAX_VALUE);
        showStudentInfoBtn.setOnAction(e -> showStudentInformation());

        Button showCOPOBtn = new Button("Show CO PO");
        showCOPOBtn.setMaxWidth(Double.MAX_VALUE);
        showCOPOBtn.setOnAction(e -> showCOPODialog());

        Button facultyInfoBtn = new Button("Faculty Info");
        facultyInfoBtn.setMaxWidth(Double.MAX_VALUE);
        facultyInfoBtn.setOnAction(e -> showFacultyInfo());

        leftMenu.getChildren().addAll(menuTitle, addCourseBtn, showStudentInfoBtn, showCOPOBtn, facultyInfoBtn);
        return leftMenu;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);

        Label dashboardTitle = new Label("Admin Dashboard");
        dashboardTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label welcomeMessage = new Label("Welcome to the CO/PO Assessment System");
        welcomeMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

        // Course list section
        VBox courseSection = new VBox(10);
        courseSection.setAlignment(Pos.TOP_LEFT);
        courseSection.setPadding(new Insets(15));
        courseSection.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        Label courseListTitle = new Label("Current Courses:");
        courseListTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        ListView<String> courseListView = new ListView<>();
        courseListView.setPrefHeight(200);
        courseListView.setId("courseListView");

        courseSection.getChildren().addAll(courseListTitle, courseListView);

        centerContent.getChildren().addAll(
                dashboardTitle,
                welcomeMessage,
                courseSection
        );

        return centerContent;
    }

    private void showAddCourseDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add Course");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Add New Course");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField courseCodeField = new TextField();
        courseCodeField.setPromptText("Course Code");
        courseCodeField.setPrefWidth(300);

        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Course Name");
        courseNameField.setPrefWidth(300);

        TextField instructorField = new TextField();
        instructorField.setPromptText("Course Instructor");
        instructorField.setPrefWidth(300);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add Course");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        addButton.setOnAction(e -> {
            String courseCode = courseCodeField.getText().trim();
            String courseName = courseNameField.getText().trim();
            String instructor = instructorField.getText().trim();

            if (courseCode.isEmpty() || courseName.isEmpty() || instructor.isEmpty()) {
                showAlert("Error", "All fields are required!");
                return;
            }

            Course newCourse = new Course(courseCode, courseName, instructor);
            courses.add(newCourse);
            updateCourseList();
            
            showAlert("Success", "Course added successfully!");
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(addButton, cancelButton);

        dialogContent.getChildren().addAll(
                titleLabel,
                courseCodeField,
                courseNameField,
                instructorField,
                buttonBox
        );

        Scene dialogScene = new Scene(dialogContent, 400, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showStudentInformation() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Student Information");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Student Information");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        String[] studentData = {
            "Student ID: 2021001 | Name: John Doe | Course: CSE101 | Year: 2021",
            "Student ID: 2021002 | Name: Jane Smith | Course: CSE101 | Year: 2021",
            "Student ID: 2021003 | Name: Bob Johnson | Course: CSE102 | Year: 2021",
            "Student ID: 2022001 | Name: Alice Brown | Course: CSE101 | Year: 2022",
            "Student ID: 2022002 | Name: Charlie Wilson | Course: CSE102 | Year: 2022"
        };

        ListView<String> studentListView = new ListView<>();
        studentListView.getItems().addAll(studentData);
        studentListView.setPrefHeight(300);
        studentListView.setPrefWidth(500);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        dialogContent.getChildren().addAll(titleLabel, studentListView, closeButton);

        Scene dialogScene = new Scene(dialogContent, 550, 400);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showCOPODialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("CO/PO Assessment");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("CO/PO Assessment by Year");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label yearLabel = new Label("Select Year:");
        ComboBox<String> yearComboBox = new ComboBox<>();
        yearComboBox.getItems().addAll("2021", "2022", "2023", "2024");
        yearComboBox.setValue("2024");
        yearComboBox.setPrefWidth(200);

        TextArea resultArea = new TextArea();
        resultArea.setPrefHeight(300);
        resultArea.setPrefWidth(500);
        resultArea.setEditable(false);

        Button viewButton = new Button("View CO/PO Assessment");
        viewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        viewButton.setOnAction(e -> {
            String selectedYear = yearComboBox.getValue();
            if (selectedYear != null) {
                String coPoData = "CO/PO Assessment Results for Year " + selectedYear + ":\n\n" +
                    "Course: CSE101\n" +
                    "CO1: 85% | CO2: 78% | CO3: 82% | CO4: 79%\n" +
                    "PO1: 80% | PO2: 85% | PO3: 75% | PO4: 88%\n\n" +
                    "Course: CSE102\n" +
                    "CO1: 88% | CO2: 82% | CO3: 85% | CO4: 80%\n" +
                    "PO1: 85% | PO2: 88% | PO3: 82% | PO4: 90%\n\n" +
                    "Overall Assessment: Satisfactory";
                resultArea.setText(coPoData);
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        dialogContent.getChildren().addAll(
                titleLabel,
                yearLabel,
                yearComboBox,
                viewButton,
                resultArea,
                closeButton
        );

        Scene dialogScene = new Scene(dialogContent, 550, 500);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showFacultyInfo() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Faculty Information");

        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(20));
        dialogContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Faculty Information");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        String[] facultyData = {
            "Faculty ID: F001 | Name: Dr. Sarah Johnson | Department: CSE | Email: sarah.johnson@university.edu",
            "Faculty ID: F002 | Name: Prof. Michael Chen | Department: CSE | Email: michael.chen@university.edu",
            "Faculty ID: F003 | Name: Dr. Emily Davis | Department: CSE | Email: emily.davis@university.edu",
            "Faculty ID: F004 | Name: Prof. David Wilson | Department: CSE | Email: david.wilson@university.edu"
        };

        ListView<String> facultyListView = new ListView<>();
        facultyListView.getItems().addAll(facultyData);
        facultyListView.setPrefHeight(300);
        facultyListView.setPrefWidth(500);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        dialogContent.getChildren().addAll(titleLabel, facultyListView, closeButton);

        Scene dialogScene = new Scene(dialogContent, 550, 400);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void updateCourseList() {
        ListView<String> courseListView = (ListView<String>) centerContent.lookup("#courseListView");
        if (courseListView != null) {
            courseListView.getItems().clear();
            for (Course course : courses) {
                courseListView.getItems().add(
                    course.getCourseCode() + " - " + course.getCourseName() + " (Instructor: " + course.getInstructor() + ")"
                );
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private static class Course {
        private String courseCode;
        private String courseName;
        private String instructor;

        public Course(String courseCode, String courseName, String instructor) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.instructor = instructor;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getInstructor() { return instructor; }
    }
}