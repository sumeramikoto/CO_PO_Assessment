package org.example.co_po_assessment.Report_controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.DatabaseService;
import org.example.co_po_assessment.utilities.WindowUtils;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AdminPOReportSelectorController implements Initializable {
    @FXML private ComboBox<String> programmeCombo;
    @FXML private ComboBox<Integer> batchCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private ComboBox<String> course1Combo;
    @FXML private ComboBox<String> course2Combo;
    @FXML private ComboBox<String> course3Combo;

    @FXML private TableView<StudentRow> studentsTable;
    @FXML private TableColumn<StudentRow, String> colId;
    @FXML private TableColumn<StudentRow, String> colName;
    @FXML private TableColumn<StudentRow, String> colEmail;
    @FXML private TableColumn<StudentRow, Integer> colBatch;

    @FXML private Label statusLabel;
    @FXML private Button generateButton;

    private final DatabaseService db = DatabaseService.getInstance();
    private final ObservableList<StudentRow> students = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Table setup
        if (studentsTable != null) studentsTable.setItems(students);
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colName != null) colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colBatch != null) colBatch.setCellValueFactory(new PropertyValueFactory<>("batch"));

        // Load filters
        try {
            List<String> programmes = db.getProgrammes();
            programmeCombo.setItems(FXCollections.observableArrayList(programmes));
            List<Integer> batches = db.getBatches();
            batchCombo.setItems(FXCollections.observableArrayList(batches));
            List<String> years = db.getAcademicYears();
            yearCombo.setItems(FXCollections.observableArrayList(years));
            if (!years.isEmpty()) yearCombo.setValue(years.get(years.size()-1));
        } catch (SQLException ex) {
            setStatus("Failed to load filters: " + ex.getMessage());
        }

        // Listeners to reload courses/students
        programmeCombo.valueProperty().addListener((o, old, val) -> onCohortChanged());
        batchCombo.valueProperty().addListener((o, old, val) -> onCohortChanged());
        yearCombo.valueProperty().addListener((o, old, val) -> onCohortChanged());
        course1Combo.valueProperty().addListener((o, old, val) -> onCoursesChanged());
        course2Combo.valueProperty().addListener((o, old, val) -> onCoursesChanged());
        course3Combo.valueProperty().addListener((o, old, val) -> onCoursesChanged());

        updateGenerateButtonState();
    }

    private void onCohortChanged() {
        // Clear course selections and reload available courses for the cohort
        course1Combo.getItems().clear(); course1Combo.setValue(null);
        course2Combo.getItems().clear(); course2Combo.setValue(null);
        course3Combo.getItems().clear(); course3Combo.setValue(null);
        students.clear();
        updateGenerateButtonState();
        String programme = programmeCombo.getValue();
        Integer batch = batchCombo.getValue();
        String year = yearCombo.getValue();
        if (programme == null || batch == null || year == null) return;
        try {
            List<String> courseItems = db.getCoursesForCohort(batch, programme, year);
            course1Combo.setItems(FXCollections.observableArrayList(courseItems));
            course2Combo.setItems(FXCollections.observableArrayList(courseItems));
            course3Combo.setItems(FXCollections.observableArrayList(courseItems));
            setStatus(courseItems.isEmpty()? "No courses found for selected cohort." : "Select up to 3 courses.");
        } catch (SQLException ex) {
            setStatus("Failed to load courses: " + ex.getMessage());
        }
    }

    private void onCoursesChanged() {
        updateGenerateButtonState();
        refreshStudents();
    }

    private void refreshStudents() {
        students.clear();
        String programme = programmeCombo.getValue();
        Integer batch = batchCombo.getValue();
        String year = yearCombo.getValue();
        List<String> codes = selectedCourseCodes();
        if (programme == null || batch == null || year == null || codes.isEmpty()) return;
        try {
            List<DatabaseService.StudentData> list = db.getStudentsForCohortAndCourses(batch, programme, year, codes);
            for (DatabaseService.StudentData s : list) {
                students.add(new StudentRow(s.id, s.name, s.email, s.batch));
            }
            setStatus(list.isEmpty() ? "No students found for current selection." : ("Loaded " + list.size() + " students."));
        } catch (SQLException ex) {
            setStatus("Failed to load students: " + ex.getMessage());
        }
    }

    private List<String> selectedCourseCodes() {
        List<String> items = new ArrayList<>();
        for (ComboBox<String> cb : List.of(course1Combo, course2Combo, course3Combo)) {
            String s = cb.getValue();
            if (s != null && !s.isBlank()) {
                // Expect format: CODE - NAME
                String code = s.split(" - ", 2)[0].trim();
                if (!code.isBlank()) items.add(code);
            }
        }
        // Deduplicate
        return items.stream().distinct().collect(Collectors.toList());
    }

    private void updateGenerateButtonState() {
        boolean ready = programmeCombo.getValue() != null && batchCombo.getValue() != null && yearCombo.getValue() != null;
        List<String> codes = selectedCourseCodes();
        generateButton.setDisable(!(ready && codes.size() == 3));
    }

    @FXML
    public void onGenerate(ActionEvent e) {
        String programme = programmeCombo.getValue();
        Integer batch = batchCombo.getValue();
        String year = yearCombo.getValue();
        List<String> codes = selectedCourseCodes();
        if (programme == null || batch == null || year == null || codes.size() != 3) {
            setStatus("Please select programme, batch, year and exactly three courses.");
            return;
        }
        // For each selected course, open PO report dialog in its own window
        for (String code : codes) {
            openPOReportForCourse(code, programme, year);
        }
    }

    private void openPOReportForCourse(String courseCode, String programme, String academicYear) {
        try {
            DatabaseService.CourseData ci = db.getCourseInfo(courseCode, programme);
            if (ci == null) { setStatus("Course not found: " + courseCode + " ("+programme+")"); return; }
            DatabaseService.FacultyCourseAssignment selected = new DatabaseService.FacultyCourseAssignment(
                    ci.courseCode, ci.courseName, academicYear, ci.department, ci.programme
            );
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/poReportDialog-view.fxml"));
            Parent root = loader.load();
            POReportDialogController controller = loader.getController();
            controller.setContext(selected);
            Stage stage = new Stage();
            stage.setTitle("PO Report - " + courseCode + " (" + academicYear + ")");
            WindowUtils.setSceneAndMaximize(stage, new Scene(root));
            stage.show();
        } catch (Exception ex) {
            setStatus("Failed to open PO report: " + ex.getMessage());
        }
    }

    @FXML
    public void onClose(ActionEvent e) {
        Stage st = (Stage) generateButton.getScene().getWindow();
        st.close();
    }

    private void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    // Lightweight row model for the table
    public static class StudentRow {
        private final SimpleStringProperty id = new SimpleStringProperty();
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleStringProperty email = new SimpleStringProperty();
        private final SimpleIntegerProperty batch = new SimpleIntegerProperty();
        public StudentRow(String id, String name, String email, int batch) { this.id.set(id); this.name.set(name); this.email.set(email); this.batch.set(batch); }
        public String getId(){ return id.get(); }
        public String getName(){ return name.get(); }
        public String getEmail(){ return email.get(); }
        public Integer getBatch(){ return batch.get(); }
    }
}

