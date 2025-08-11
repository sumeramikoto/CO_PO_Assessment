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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;


import static javafx.application.Application.launch;
public class FacultyDashboard extends Application {
    private DashboardController controller;


    @Override
    public void start(Stage primaryStage) {
        this.controller = new DashboardController(primaryStage);
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.setMaximized(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        HBox topBar = createTopBar(primaryStage);
        root.setTop(topBar);

        VBox leftMenu = createLeftMenu();
        root.setLeft(leftMenu);

        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);

        Scene scene = new Scene(root, 1920, 1080);
        primaryStage.setTitle("Faculty Dashboard - CO/PO Assessment System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar(Stage primaryStage) {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("-fx-background-color: #343a40;");
        topBar.setAlignment(Pos.CENTER_LEFT);

//        ImageView logo = new ImageView();
//        logo.setFitHeight(100);
//        logo.setPreserveRatio(true);
//        try {
//            logo.setImage(new Image(getClass().getResourceAsStream("/images/iut_logo.png")));
//        } catch (Exception e) {
//            System.out.println("Couldn't load logo image");
//            logo.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
//        }

        Label welcomeLabel = new Label("Welcome, Faculty Member");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        Button logoutButton = new Button("Logout");
        logoutButton.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        logoutButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 10 30 10 30; -fx-background-radius: 8;");
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

        topBar.getChildren().addAll(rightMenu);
        return topBar;
    }

    private VBox createLeftMenu() {
        VBox leftMenu = new VBox(10);
        leftMenu.setPadding(new Insets(20));
        leftMenu.setStyle("-fx-background-color: #f8f9fa;");
        leftMenu.setPrefWidth(200);

        Label menuTitle = new Label("Menu");
        menuTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 26px;");

        Button dashboardBtn = new Button("Dashboard");
        dashboardBtn.setMaxWidth(Double.MAX_VALUE);
        dashboardBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Button coursesBtn = new Button("My Courses");
        coursesBtn.setMaxWidth(Double.MAX_VALUE);
        coursesBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Button uploadBtn = new Button("Upload Data");
        uploadBtn.setMaxWidth(Double.MAX_VALUE);
        uploadBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        uploadBtn.setOnAction(e -> controller.handleMarksTemplate());

        Button reportsBtn = new Button("Generate Reports");
        reportsBtn.setMaxWidth(Double.MAX_VALUE);
        reportsBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        reportsBtn.setOnAction(e -> controller.generateReport());

        Button settingsBtn = new Button("Settings");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);
        settingsBtn.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        leftMenu.getChildren().addAll(menuTitle, dashboardBtn, coursesBtn, uploadBtn, reportsBtn, settingsBtn);
        return leftMenu;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);

        Label dashboardTitle = new Label("Faculty Dashboard");
        dashboardTitle.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        Label quickActionsLabel = new Label("Quick Actions");
        quickActionsLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        HBox quickActions = new HBox(20);
        quickActions.setAlignment(Pos.CENTER);

        VBox uploadAction = createQuickActionBox(
                "Get Marks Template",
                "file:/C:/Users/User/Desktop/Induction/iut_logo.png",
                "After filling course and student information to the plain template, input file here to get marks entry template",
                () -> controller.handleMarksTemplate()
        );

        VBox reportAction = createQuickActionBox(
                "Generate Report",
                "file:/C:/Users/User/Desktop/Induction/iut_logo.png",
                "Process marks to obtain CO/PO assessment report of the course",
                () -> controller.generateReport()
        );

        VBox templateAction = createQuickActionBox(
                "Get Plain Template",
                "file:/C:/Users/User/Desktop/Induction/iut_logo.png",
                "Get Excel template ",
                () -> controller.handleGetTemplate()
        );

        quickActions.getChildren().addAll(uploadAction, reportAction, templateAction);

        centerContent.getChildren().addAll(
                dashboardTitle,
                quickActionsLabel,
                quickActions
        );

        return centerContent;
    }

    private VBox createQuickActionBox(String title, String imageUrl, String description, Runnable action) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        box.setPrefSize(300, 200); // Increased width from 200 to 300 for more space

        box.setOnMouseClicked(e -> action.run());

        ImageView icon = new ImageView();
        try {
            icon.setImage(new Image(imageUrl));
        } catch (Exception e) {
            System.out.println("Couldn't load action icon");
        }
        icon.setFitHeight(60);
        icon.setFitWidth(60);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 22px;");
        titleLabel.setWrapText(true); // Enable wrapping for title if needed
        titleLabel.setMaxWidth(260); // Set max width to fit within padding

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 16px;"); // Reduced font size slightly
        descLabel.setWrapText(true); // Ensure text wrapping is enabled
        descLabel.setMaxWidth(260); // Set max width to prevent truncation and allow wrapping
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); // Center-align text

        box.getChildren().addAll(icon, titleLabel, descLabel);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}