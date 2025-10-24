package org.example.co_po_assessment.admin_input_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.DatabaseService;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ManageThresholdsController implements Initializable {
    @FXML private TextField coIndividualField;    // CO_INDIVIDUAL
    @FXML private TextField poIndividualField;    // PO_INDIVIDUAL
    @FXML private TextField coCohortField;        // CO_COHORTSET
    @FXML private TextField poCohortField;        // PO_COHORTSET
    @FXML private Button saveButton;
    @FXML private Button closeButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load existing thresholds and populate fields
        try {
            Map<String, Double> t = DatabaseService.getInstance().getThresholds();
            // Fallback defaults if not present (matching insert.sql)
            setField(coIndividualField, t.getOrDefault("CO_INDIVIDUAL", 60.0));
            setField(poIndividualField, t.getOrDefault("PO_INDIVIDUAL", 40.0));
            setField(coCohortField, t.getOrDefault("CO_COHORTSET", 50.0));
            setField(poCohortField, t.getOrDefault("PO_COHORTSET", 50.0));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load thresholds: " + e.getMessage());
            // Still present sensible defaults
            setField(coIndividualField, 60.0);
            setField(poIndividualField, 40.0);
            setField(coCohortField, 50.0);
            setField(poCohortField, 50.0);
        }
    }

    private void setField(TextField tf, double val) {
        if (tf != null) tf.setText(String.valueOf(val));
    }

    @FXML
    public void onSave(ActionEvent evt) {
        try {
            double coInd = parsePercent(coIndividualField, "CO individual");
            double poInd = parsePercent(poIndividualField, "PO individual");
            double coCoh = parsePercent(coCohortField, "CO cohort");
            double poCoh = parsePercent(poCohortField, "PO cohort");

            Map<String, Double> updates = new HashMap<>();
            updates.put("CO_INDIVIDUAL", coInd);
            updates.put("PO_INDIVIDUAL", poInd);
            updates.put("CO_COHORTSET", coCoh);
            updates.put("PO_COHORTSET", poCoh);

            DatabaseService.getInstance().updateThresholds(updates);
            showInfo("Saved", "Thresholds updated successfully.");
        } catch (IllegalArgumentException ex) {
            showError("Validation Error", ex.getMessage());
        } catch (SQLException ex) {
            showError("Database Error", "Failed to save thresholds: " + ex.getMessage());
        }
    }

    @FXML
    public void onClose(ActionEvent evt) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private double parsePercent(TextField tf, String label) {
        String s = tf.getText();
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(label + " threshold cannot be empty.");
        try {
            double v = Double.parseDouble(s.trim());
            if (v < 0 || v >= 100) throw new IllegalArgumentException(label + " threshold must be between 0 and 99.99.");
            return v;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(label + " threshold must be a number.");
        }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}

