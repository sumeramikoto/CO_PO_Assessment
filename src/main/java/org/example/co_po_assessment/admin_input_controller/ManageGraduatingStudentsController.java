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

public class ManageGraduatingStudentsController {
    @FXML private ComboBox<String> programmeCombo;
    @FXML private ComboBox<Integer> batchCombo;
    @FXML private ListView<String> availableStudentsList;
    @FXML private ListView<String> selectedStudentsList;
    @FXML private Label statusLabel;

    private final DatabaseService db = DatabaseService.getInstance();

    @FXML
    public void initialize() {
        if (availableStudentsList != null)
            availableStudentsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        if (selectedStudentsList != null)
            selectedStudentsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        loadProgrammes();

        if (programmeCombo != null) {
            programmeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                loadBatches(newVal);
            });
        }
        if (batchCombo != null) {
            batchCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                String prog = programmeCombo.getValue();
                if (prog != null && newVal != null) loadStudents(prog, newVal);
            });
        }
    }

    @FXML private void onRefreshProgrammes() { loadProgrammes(); }

    private void loadProgrammes() {
        try {
            List<String> progs = db.getDistinctProgrammesFromStudents();
            programmeCombo.setItems(FXCollections.observableArrayList(progs));
            if (!progs.isEmpty()) {
                String cur = programmeCombo.getValue();
                if (cur == null || !progs.contains(cur)) {
                    programmeCombo.getSelectionModel().selectFirst();
                    loadBatches(programmeCombo.getValue());
                } else {
                    loadBatches(cur);
                }
            } else {
                batchCombo.getItems().clear();
                availableStudentsList.getItems().clear();
                selectedStudentsList.getItems().clear();
            }
            setStatus("Loaded " + progs.size() + " programme(s)");
        } catch (SQLException e) {
            showError("Load Programmes", e.getMessage());
        }
    }

    private void loadBatches(String programme) {
        if (programme == null || programme.isBlank()) return;
        try {
            List<Integer> batches = db.getBatchesForProgramme(programme);
            batchCombo.setItems(FXCollections.observableArrayList(batches));
            if (!batches.isEmpty()) {
                Integer cur = batchCombo.getValue();
                if (cur == null || !batches.contains(cur)) {
                    batchCombo.getSelectionModel().selectFirst();
                    loadStudents(programme, batchCombo.getValue());
                } else {
                    loadStudents(programme, cur);
                }
            } else {
                availableStudentsList.getItems().clear();
                selectedStudentsList.getItems().clear();
            }
            setStatus("Programme " + programme + ": " + batches.size() + " batch(es)");
        } catch (SQLException e) {
            showError("Load Batches", e.getMessage());
        }
    }

    private String toDisplay(DatabaseService.StudentData s) { return s.id + " - " + s.name; }
    private String parseId(String display) {
        if (display == null) return null;
        int idx = display.indexOf(" - ");
        return idx > 0 ? display.substring(0, idx) : display;
    }

    private void loadStudents(String programme, int batch) {
        try {
            List<DatabaseService.StudentData> all = db.getStudentsByProgrammeAndBatch(programme, batch);
            Set<String> graduatingIds = new LinkedHashSet<>(db.getGraduatingStudentIds(programme, batch));

            List<String> selected = new ArrayList<>();
            List<String> available = new ArrayList<>();
            for (DatabaseService.StudentData s : all) {
                String disp = toDisplay(s);
                if (graduatingIds.contains(s.id)) selected.add(disp); else available.add(disp);
            }
            availableStudentsList.setItems(FXCollections.observableArrayList(available));
            selectedStudentsList.setItems(FXCollections.observableArrayList(selected));
            setStatus("Programme " + programme + ", Batch " + batch + ": " + all.size() + " student(s), " + selected.size() + " selected");
        } catch (SQLException e) {
            showError("Load Students", e.getMessage());
        }
    }

    @FXML private void onAddSelected() {
        ObservableList<String> toMove = FXCollections.observableArrayList(availableStudentsList.getSelectionModel().getSelectedItems());
        if (toMove.isEmpty()) return;
        ObservableList<String> avail = availableStudentsList.getItems();
        ObservableList<String> sel = selectedStudentsList.getItems();
        for (String s : toMove) if (!sel.contains(s)) sel.add(s);
        avail.removeAll(toMove);
    }

    @FXML private void onRemoveSelected() {
        ObservableList<String> toMove = FXCollections.observableArrayList(selectedStudentsList.getSelectionModel().getSelectedItems());
        if (toMove.isEmpty()) return;
        ObservableList<String> avail = availableStudentsList.getItems();
        ObservableList<String> sel = selectedStudentsList.getItems();
        sel.removeAll(toMove);
        for (String s : toMove) if (!avail.contains(s)) avail.add(s);
        FXCollections.sort(avail);
    }

    @FXML private void onSave() {
        String programme = programmeCombo.getValue();
        Integer batch = batchCombo.getValue();
        if (programme == null || programme.isBlank()) { showWarn("Validation", "Select a programme."); return; }
        if (batch == null) { showWarn("Validation", "Select a batch."); return; }
        List<String> selected = new ArrayList<>(selectedStudentsList.getItems());
        List<String> ids = selected.stream().map(this::parseId).collect(Collectors.toList());
        try {
            db.saveGraduatingStudents(programme, batch, ids);
            showInfo("Saved", "Graduating students updated for " + programme + " (Batch " + batch + ").");
            setStatus("Saved " + ids.size() + " student(s)");
        } catch (SQLException e) {
            showError("Save Failed", e.getMessage());
        }
    }

    @FXML private void onClose() { ((Stage) programmeCombo.getScene().getWindow()).close(); }

    private void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }
    private void showError(String title, String msg) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private void showWarn(String title, String msg) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private void showInfo(String title, String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
}

