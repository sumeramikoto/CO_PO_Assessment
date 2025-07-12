package org.example.co_po_assessment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import static javafx.application.Application.launch;
public class FacultyDashboard extends Application {
    private DashboardController controller;

    @Override
    public void start(Stage primaryStage) {
        this.controller = new DashboardController(primaryStage);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        HBox topBar = createTopBar(primaryStage);
        root.setTop(topBar);

        VBox leftMenu = createLeftMenu();
        root.setLeft(leftMenu);

        VBox centerContent = createCenterContent();
        root.setCenter(centerContent);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Faculty Dashboard - CO/PO Assessment System");
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
            logo.setImage(new Image(getClass().getResourceAsStream("/images/iut_logo.png")));
        } catch (Exception e) {
            System.out.println("Couldn't load logo image");
            logo.setImage(new Image(getClass().getResourceAsStream("/images/placeholder.png")));
        }

        Label welcomeLabel = new Label("Welcome, Faculty Member");
        welcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logout");
            alert.setHeaderText(null);
            alert.setContentText("You have been logged out successfully!");
            alert.showAndWait();
            primaryStage.close();
        });

        HBox rightMenu = new HBox(20, welcomeLabel, logoutButton);
        rightMenu.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightMenu, Priority.ALWAYS);

        topBar.getChildren().addAll(logo, rightMenu);
        return topBar;
    }

    private VBox createLeftMenu() {
        VBox leftMenu = new VBox(10);
        leftMenu.setPadding(new Insets(20));
        leftMenu.setStyle("-fx-background-color: #f8f9fa;");
        leftMenu.setPrefWidth(200);

        Label menuTitle = new Label("Menu");
        menuTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Button dashboardBtn = new Button("Dashboard");
        dashboardBtn.setMaxWidth(Double.MAX_VALUE);
        dashboardBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button coursesBtn = new Button("My Courses");
        coursesBtn.setMaxWidth(Double.MAX_VALUE);

        Button uploadBtn = new Button("Upload Data");
        uploadBtn.setMaxWidth(Double.MAX_VALUE);
        uploadBtn.setOnAction(e -> controller.handleUploadMarks());

        Button reportsBtn = new Button("Generate Reports");
        reportsBtn.setMaxWidth(Double.MAX_VALUE);
        reportsBtn.setOnAction(e -> controller.handleGenerateReport());

        Button settingsBtn = new Button("Settings");
        settingsBtn.setMaxWidth(Double.MAX_VALUE);

        leftMenu.getChildren().addAll(menuTitle, dashboardBtn, coursesBtn, uploadBtn, reportsBtn, settingsBtn);
        return leftMenu;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        centerContent.setAlignment(Pos.TOP_CENTER);

        Label dashboardTitle = new Label("Faculty Dashboard");
        dashboardTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label quickActionsLabel = new Label("Quick Actions");
        quickActionsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox quickActions = new HBox(20);
        quickActions.setAlignment(Pos.CENTER);

        VBox uploadAction = createQuickActionBox(
                "Upload Marks",
                "file:/C:/Users/User/Desktop/Induction/iut_logo.png",
                "Upload student marks and CO/PO mappings",
                () -> controller.handleUploadMarks()
        );

        VBox reportAction = createQuickActionBox(
                "Generate Report",
                "file:/C:/Users/User/Desktop/Induction/iut_logo.png",
                "Generate CO/PO attainment reports",
                () -> controller.handleGenerateReport()
        );

        VBox templateAction = createQuickActionBox(
                "Get Template",
                "file:/C:/Users/User/Desktop/Induction/iut_logo.png",
                "Download Excel template for course",
                () -> controller.handleGetTemplate()
        );

        quickActions.getChildren().addAll(uploadAction, reportAction, templateAction);
        
        return centerContent;
    }

    private VBox createQuickActionBox(String title, String imageUrl, String description, Runnable action) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        box.setPrefSize(200, 180);

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
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        descLabel.setWrapText(true);

        box.getChildren().addAll(icon, titleLabel, descLabel);
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}