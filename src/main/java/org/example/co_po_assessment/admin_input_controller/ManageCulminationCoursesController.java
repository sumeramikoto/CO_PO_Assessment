package org.example.co_po_assessment.admin_input_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.DatabaseService;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ManageCulminationCoursesController {
    @FXML private ComboBox<String> programmeCombo;
    @FXML private ListView<String> availableCoursesList;
    @FXML private ListView<String> selectedCoursesList;
    @FXML private Label statusLabel;

    private final DatabaseService db = DatabaseService.getInstance();

    @FXML
    public void initialize() {
        // Configure list selection modes
        if (availableCoursesList != null)
            availableCoursesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (selectedCoursesList != null)
            selectedCoursesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        loadProgrammes();
        if (programmeCombo != null) {
            programmeCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadCoursesForProgramme(newVal));
        }
    }

    @FXML private void onRefreshProgrammes() { loadProgrammes(); }

    private void loadProgrammes() {
        try {
            List<String> progs = db.getDistinctProgrammesFromCourses();
            programmeCombo.setItems(FXCollections.observableArrayList(progs));
            if (!progs.isEmpty()) {
                String cur = programmeCombo.getValue();
                if (cur == null || !progs.contains(cur)) programmeCombo.getSelectionModel().selectFirst();
                else loadCoursesForProgramme(cur);
            } else {
                availableCoursesList.getItems().clear();
                selectedCoursesList.getItems().clear();
            }
            setStatus("Loaded " + progs.size() + " programme(s)");
        } catch (SQLException e) {
            showError("Load Programmes", e.getMessage());
        }
    }

    private void loadCoursesForProgramme(String programme) {
        if (programme == null || programme.isBlank()) return;
        try {
            // All courses under programme (display as CODE - NAME)
            List<String> all = db.getCoursesForProgramme(programme);
            // Already saved culminating course codes for this programme
            Set<String> savedCodes = new LinkedHashSet<>(db.getCulminationCourses(programme));

            // Split into selected (by code) and available
            List<String> selected = new ArrayList<>();
            for (String disp : all) {
                String code = parseCode(disp);
                if (savedCodes.contains(code)) selected.add(disp);
            }
            List<String> available = new ArrayList<>(all);
            available.removeAll(selected);

            availableCoursesList.setItems(FXCollections.observableArrayList(available));
            selectedCoursesList.setItems(FXCollections.observableArrayList(selected));
            setStatus("Programme " + programme + ": " + all.size() + " course(s), " + selected.size() + " selected");
        } catch (SQLException e) {
            showError("Load Courses", e.getMessage());
        }
    }

    private String parseCode(String display) {
        if (display == null) return null;
        int idx = display.indexOf(" - ");
        return idx > 0 ? display.substring(0, idx) : display;
    }

    @FXML private void onAddSelected() {
        ObservableList<String> toMove = FXCollections.observableArrayList(availableCoursesList.getSelectionModel().getSelectedItems());
        if (toMove.isEmpty()) return;
        ObservableList<String> avail = availableCoursesList.getItems();
        ObservableList<String> sel = selectedCoursesList.getItems();
        for (String s : toMove) if (!sel.contains(s)) sel.add(s);
        avail.removeAll(toMove);
    }

    @FXML private void onRemoveSelected() {
        ObservableList<String> toMove = FXCollections.observableArrayList(selectedCoursesList.getSelectionModel().getSelectedItems());
        if (toMove.isEmpty()) return;
        ObservableList<String> avail = availableCoursesList.getItems();
        ObservableList<String> sel = selectedCoursesList.getItems();
        sel.removeAll(toMove);
        for (String s : toMove) if (!avail.contains(s)) avail.add(s);
        FXCollections.sort(avail);
    }

    @FXML private void onSave() {
        String programme = programmeCombo.getValue();
        if (programme == null || programme.isBlank()) { showWarn("Validation", "Select a programme first."); return; }
        List<String> selectedDisplay = new ArrayList<>(selectedCoursesList.getItems());
        if (selectedDisplay.isEmpty()) { showWarn("Validation", "Add at least one course to Culmination Courses."); return; }
        List<String> codes = selectedDisplay.stream().map(this::parseCode).collect(Collectors.toList());
        try {
            // Validate PO1..PO12 coverage across chosen courses
            List<String> missing = db.getMissingPOsForCourses(programme, codes, 12);
            if (!missing.isEmpty()) {
                showError("PO Coverage Incomplete", "The selected courses do not cover all POs (PO1..PO12). Missing: " + String.join(", ", missing));
                return;
            }
            db.saveCulminationCourses(programme, codes);
            showInfo("Saved", "Culmination courses updated for " + programme + ".");
            setStatus("Saved " + codes.size() + " course(s) for " + programme);
        } catch (SQLException e) {
            showError("Save Failed", e.getMessage());
        }
    }

    @FXML private void onClose() {
        Stage stage = (Stage) programmeCombo.getScene().getWindow();
        stage.close();
    }

    private void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    private void showError(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private void showWarn(String title, String msg) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private void showInfo(String title, String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
}

