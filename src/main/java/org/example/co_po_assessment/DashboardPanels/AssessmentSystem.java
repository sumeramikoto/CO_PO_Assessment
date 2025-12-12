package org.example.co_po_assessment.DashboardPanels;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.co_po_assessment.DB_helper.DatabaseService;
import org.example.co_po_assessment.utilities.UserSession;

public class AssessmentSystem extends Application {

    private Stage primaryStage;
    private TextField emailField;
    private PasswordField passwordField;
    private DatabaseService dbService = DatabaseService.getInstance();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Academic System Login");
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.setMaximized(true);
        showLoginScreen();
    }

    private void showLoginScreen() {
        BorderPane borderPane = new BorderPane();
        
        // Clean white background
        borderPane.setStyle("-fx-background-color: #f8f9fa;");
        borderPane.setPadding(new Insets(40));

        // Header with modern styling
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 30, 0));
        
        Label title = new Label("CO-PO Assessment System");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 52));
        title.setTextFill(Color.web("#2c3e50"));
        title.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.1)));
        
        Label subtitle = new Label("Academic Outcomes Management Platform");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        headerBox.getChildren().addAll(title, subtitle);
        borderPane.setTop(headerBox);

        // Center form with card-like appearance
        VBox formContainer = new VBox(25);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(550);
        formContainer.setMaxHeight(650);
        formContainer.setPadding(new Insets(50, 60, 50, 60));
        formContainer.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 5);"
        );

        Label scenetitle = new Label("Login");
        scenetitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 38));
        scenetitle.setTextFill(Color.web("#2c3e50"));
        
        Label loginSubtitle = new Label("Please enter your credentials");
        loginSubtitle.setFont(Font.font("Segoe UI", 16));
        loginSubtitle.setTextFill(Color.web("#7f8c8d"));

        // User type selection with modern radio buttons
        ToggleGroup userTypeGroup = new ToggleGroup();
        RadioButton adminRadio = new RadioButton("Administrator");
        adminRadio.setToggleGroup(userTypeGroup);
        adminRadio.setSelected(true);
        adminRadio.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        adminRadio.setStyle("-fx-text-fill: #555555;");

        RadioButton facultyRadio = new RadioButton("Faculty Member");
        facultyRadio.setToggleGroup(userTypeGroup);
        facultyRadio.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        facultyRadio.setStyle("-fx-text-fill: #555555;");

        HBox radioBox = new HBox(40);
        radioBox.setAlignment(Pos.CENTER);
        radioBox.setPadding(new Insets(10, 0, 10, 0));
        radioBox.getChildren().addAll(adminRadio, facultyRadio);

        // Email field with styling
        VBox emailBox = new VBox(8);
        Label emailLabel = new Label("Email Address");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        emailLabel.setTextFill(Color.web("#555555"));
        
        emailField = new TextField();
        emailField.setPromptText("yourname@institution.edu");
        emailField.setFont(Font.font("Segoe UI", 16));
        emailField.setPrefHeight(45);
        emailField.setStyle(
            "-fx-background-color: #f5f5f5; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1.5; " +
            "-fx-padding: 10;"
        );
        emailField.setOnMouseEntered(e -> emailField.setStyle(
            "-fx-background-color: #f5f5f5; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #3498db; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1.5; " +
            "-fx-padding: 10;"
        ));
        emailField.setOnMouseExited(e -> {
            if (!emailField.isFocused()) {
                emailField.setStyle(
                    "-fx-background-color: #f5f5f5; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-color: #e0e0e0; " +
                    "-fx-border-radius: 8; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-padding: 10;"
                );
            }
        });
        emailBox.getChildren().addAll(emailLabel, emailField);

        // Password field with styling
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        passwordLabel.setTextFill(Color.web("#555555"));
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setFont(Font.font("Segoe UI", 16));
        passwordField.setPrefHeight(45);
        passwordField.setStyle(
            "-fx-background-color: #f5f5f5; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1.5; " +
            "-fx-padding: 10;"
        );
        passwordField.setOnMouseEntered(e -> passwordField.setStyle(
            "-fx-background-color: #f5f5f5; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #3498db; " +
            "-fx-border-radius: 8; " +
            "-fx-border-width: 1.5; " +
            "-fx-padding: 10;"
        ));
        passwordField.setOnMouseExited(e -> {
            if (!passwordField.isFocused()) {
                passwordField.setStyle(
                    "-fx-background-color: #f5f5f5; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-color: #e0e0e0; " +
                    "-fx-border-radius: 8; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-padding: 10;"
                );
            }
        });
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Interactive login button with animations
        Button loginBtn = new Button("Sign In");
        loginBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        loginBtn.setPrefWidth(450);
        loginBtn.setPrefHeight(50);
        loginBtn.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.4), 10, 0, 0, 5);"
        );

        // Hover effect
        loginBtn.setOnMouseEntered(e -> {
            loginBtn.setStyle(
                "-fx-background-color: #2980b9; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.6), 15, 0, 0, 7);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(100), loginBtn);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });

        loginBtn.setOnMouseExited(e -> {
            loginBtn.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.4), 10, 0, 0, 5);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(100), loginBtn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Press effect
        loginBtn.setOnMousePressed(e -> {
            loginBtn.setStyle(
                "-fx-background-color: #21618c; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.3), 5, 0, 0, 2);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(50), loginBtn);
            st.setToX(0.98);
            st.setToY(0.98);
            st.play();
        });

        loginBtn.setOnMouseReleased(e -> {
            loginBtn.setStyle(
                "-fx-background-color: #2980b9; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(52, 152, 219, 0.6), 15, 0, 0, 7);"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(50), loginBtn);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });

        final Label messageLabel = new Label();
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        formContainer.getChildren().addAll(
            scenetitle, 
            loginSubtitle, 
            radioBox, 
            emailBox, 
            passwordBox, 
            loginBtn, 
            messageLabel
        );
        
        borderPane.setCenter(formContainer);

        // Event handler for login with loading state
        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            boolean isAdmin = adminRadio.isSelected();

            if (email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("⚠ Email and password are required!");
                messageLabel.setTextFill(Color.web("#e74c3c"));
                
                // Shake animation for empty fields
                ScaleTransition shake = new ScaleTransition(Duration.millis(100), messageLabel);
                shake.setFromX(1.0);
                shake.setToX(1.05);
                shake.setCycleCount(4);
                shake.setAutoReverse(true);
                shake.play();
                return;
            }

            // Show loading state
            String originalText = loginBtn.getText();
            loginBtn.setText("Signing In...");
            loginBtn.setDisable(true);
            messageLabel.setText("Authenticating...");
            messageLabel.setTextFill(Color.web("#3498db"));

            // Simulate async authentication (you can make this truly async if needed)
            new Thread(() -> {
                boolean authenticated = authenticateUser(email, password, isAdmin);
                
                javafx.application.Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText(originalText);
                    
                    if (authenticated) {
                        messageLabel.setText("✓ Login successful!");
                        messageLabel.setTextFill(Color.web("#27ae60"));
                        
                        // Success animation
                        ScaleTransition successAnim = new ScaleTransition(Duration.millis(200), messageLabel);
                        successAnim.setFromX(0.8);
                        successAnim.setFromY(0.8);
                        successAnim.setToX(1.0);
                        successAnim.setToY(1.0);
                        successAnim.play();

                        // Delay before launching dashboard
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(800));
                        pause.setOnFinished(ev -> {
                            if (isAdmin) {
                                launchAdminDashboard();
                            } else {
                                try {
                                    DatabaseService.FacultyInfo info = dbService.getFacultyInfo(email);
                                    UserSession.setCurrentFaculty(info);
                                } catch (Exception ex) {
                                    // continue
                                }
                                launchFacultyDashboard();
                            }
                        });
                        pause.play();
                    } else {
                        messageLabel.setText("✗ Invalid credentials!");
                        messageLabel.setTextFill(Color.web("#e74c3c"));
                        
                        // Error shake animation
                        ScaleTransition shake = new ScaleTransition(Duration.millis(80), loginBtn);
                        shake.setFromX(1.0);
                        shake.setToX(1.02);
                        shake.setCycleCount(6);
                        shake.setAutoReverse(true);
                        shake.play();
                    }
                });
            }).start();
        });

        Scene scene = new Scene(borderPane, 1920, 1080);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean authenticateUser(String email, String password, boolean isAdmin) {
        try {
            return isAdmin ? dbService.authenticateAdmin(email, password)
                           : dbService.authenticateFaculty(email, password);
        } catch (Exception e) {
            return false; // swallow for legacy screen; could log
        }
    }

    private void launchFacultyDashboard() {
        try {
            // Create a new stage for the faculty dashboard
            Stage facultyStage = new Stage();
            FacultyDashboardView facultyDashboard = new FacultyDashboardView();
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
            AdminDashboardView adminDashboard = new AdminDashboardView();
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
