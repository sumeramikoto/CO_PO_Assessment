package org.example.co_po_assessment;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

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

        // Event handler for login
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
                
                // Launch appropriate dashboard based on user type
                if (isAdmin) {
                    launchAdminDashboard();
                } else {
                    // Launch faculty dashboard
                    launchFacultyDashboard();
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

    private void launchFacultyDashboard() {
        try {
            // Create a new stage for the faculty dashboard
            Stage facultyStage = new Stage();
            FacultyDashboard facultyDashboard = new FacultyDashboard();
            facultyDashboard.start(facultyStage);
            
            // Close the login window
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error message if dashboard fails to load
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Faculty Dashboard");
            alert.setContentText("Please try again or contact support.");
            alert.showAndWait();
        }
    }

    private void launchAdminDashboard() {
        try {
            Stage adminStage = new Stage();
            AdminDashboard adminDashboard = new AdminDashboard();
            adminDashboard.start(adminStage);
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Admin Dashboard");
            alert.setContentText("Please try again or contact support.");
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
