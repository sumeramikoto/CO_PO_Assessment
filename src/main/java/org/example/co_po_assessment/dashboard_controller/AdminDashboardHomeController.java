package org.example.co_po_assessment.dashboard_controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.example.co_po_assessment.DB_helper.DatabaseService;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminDashboardHomeController implements Initializable {
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    @FXML private Label dayLabel;

    private final DatabaseService db = DatabaseService.getInstance();
    private Timeline clockTimeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Start live clock
        startClock();
    }

    private void startClock() {
        // Update clock immediately
        updateClock();
        
        // Create timeline to update clock every second
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateClock()));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        
        // Format time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        if (timeLabel != null) {
            timeLabel.setText(now.format(timeFormatter));
        }
        
        // Format date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        if (dateLabel != null) {
            dateLabel.setText(now.format(dateFormatter));
        }
        
        // Format day
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        if (dayLabel != null) {
            dayLabel.setText(now.format(dayFormatter));
        }
    }
}
