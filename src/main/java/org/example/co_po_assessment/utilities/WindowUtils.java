package org.example.co_po_assessment.utilities;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class WindowUtils {
    private WindowUtils() {}

    public static void maximize(Stage stage) {
        if (stage == null) return;
        // Prefer maximizing to occupy the full available screen area
        stage.setMaximized(true);

        // Additionally, set a sane default size (up to 1920x1080, clamped to screen bounds)
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double targetW = Math.min(1920, bounds.getWidth());
        double targetH = Math.min(1080, bounds.getHeight());
        // Only set size if not already larger
        if (stage.getWidth() <= 0 || stage.getHeight() <= 0) {
            stage.setWidth(targetW);
            stage.setHeight(targetH);
        }
        stage.setMinWidth(Math.min(1280, targetW));
        stage.setMinHeight(Math.min(720, targetH));
    }

    public static void setSceneAndMaximize(Stage stage, Scene scene) {
        if (stage == null || scene == null) return;
        stage.setScene(scene);
        maximize(stage);
    }
}

