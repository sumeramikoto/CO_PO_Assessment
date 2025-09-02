package org.example.co_po_assessment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageEnrollmentsController {
    @FXML private TableView<StudentDatabaseHelper.StudentData> studentTableView;
    @FXML private TableColumn<StudentDatabaseHelper.StudentData, String> idColumn;
    @FXML private TableColumn<StudentDatabaseHelper.StudentData, String> nameColumn;
    @FXML private TableColumn<StudentDatabaseHelper.StudentData, Integer> batchColumn;
    @FXML private TableColumn<StudentDatabaseHelper.StudentData, String> departmentColumn;
    @FXML private TableColumn<StudentDatabaseHelper.StudentData, String> programmeColumn;

    @FXML private ComboBox<Integer> batchFilterCombo;
    @FXML private ComboBox<String> departmentFilterCombo;
    @FXML private ComboBox<String> programmeFilterCombo;
    @FXML private ComboBox<String> courseCombo;
    @FXML private ComboBox<String> academicYearCombo;

    @FXML private Button refreshButton;
    @FXML private Button clearFiltersButton;
    @FXML private Button enrollButton;
    @FXML private Button closeButton;

    private final StudentDatabaseHelper studentDb = new StudentDatabaseHelper();
    private final DatabaseService db = DatabaseService.getInstance();

    @FXML
    public void initialize() {
        setupTable();
        loadStaticCombos();
        loadStudents();
    }

    private void setupTable() {
        if (idColumn != null) idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (nameColumn != null) nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (batchColumn != null) batchColumn.setCellValueFactory(new PropertyValueFactory<>("batch"));
        if (departmentColumn != null) departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        if (programmeColumn != null) programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        studentTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadStaticCombos() {
        try {
            // Batches
            List<Integer> batches = new ArrayList<>(studentDb.getDistinctBatches());
            batchFilterCombo.setItems(FXCollections.observableArrayList(batches));
            batchFilterCombo.setPromptText("All Batches");
            // Departments
            List<String> depts = new ArrayList<>(studentDb.getDistinctDepartments());
            departmentFilterCombo.setItems(FXCollections.observableArrayList(depts));
            departmentFilterCombo.setPromptText("All Departments");
            // Programmes
            List<String> progs = new ArrayList<>(studentDb.getDistinctProgrammes());
            programmeFilterCombo.setItems(FXCollections.observableArrayList(progs));
            programmeFilterCombo.setPromptText("All Programmes");
            // Courses
            List<String> courses = db.getCourseCodes();
            courseCombo.setItems(FXCollections.observableArrayList(courses));
            courseCombo.setPromptText("Select Course");
            // Academic Years
            List<String> years = db.getAcademicYears();
            academicYearCombo.setItems(FXCollections.observableArrayList(years));
            academicYearCombo.setPromptText("Academic Year");
        } catch (SQLException e) {
            showError("Load Error", e.getMessage());
        }

        // Auto reload when filters change
        batchFilterCombo.valueProperty().addListener((obs, o, n) -> loadStudents());
        departmentFilterCombo.valueProperty().addListener((obs, o, n) -> loadStudents());
        programmeFilterCombo.valueProperty().addListener((obs, o, n) -> loadStudents());
    }

    private void loadStudents() {
        try {
            Integer batch = batchFilterCombo.getValue();
            String dept = departmentFilterCombo.getValue();
            String prog = programmeFilterCombo.getValue();
            List<StudentDatabaseHelper.StudentData> list = studentDb.getStudents(batch, dept, prog);
            ObservableList<StudentDatabaseHelper.StudentData> data = FXCollections.observableArrayList(list);
            studentTableView.setItems(data);
        } catch (SQLException e) {
            showError("Load Students", e.getMessage());
        }
    }

    @FXML private void onRefresh() { loadStudents(); }

    @FXML private void onClearFilters() {
        batchFilterCombo.getSelectionModel().clearSelection();
        departmentFilterCombo.getSelectionModel().clearSelection();
        programmeFilterCombo.getSelectionModel().clearSelection();
        loadStudents();
    }

    @FXML private void onEnrollSelected() {
        String course = courseCombo.getValue();
        String year = academicYearCombo.getValue();
        if (course == null || course.isBlank()) { showWarn("Validation", "Select a course"); return; }
        if (year == null || year.isBlank()) { showWarn("Validation", "Select an academic year"); return; }
        List<StudentDatabaseHelper.StudentData> selected = studentTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) { showWarn("Validation", "Select at least one student"); return; }
        List<String> ids = selected.stream().map(s -> s.id).collect(Collectors.toList());
        try {
            db.enrollStudents(course, year, ids);
            showInfo("Enrollment", "Enrolled/updated " + ids.size() + " students for course " + course + " (" + year + ")");
        } catch (SQLException e) {
            showError("Enrollment Failed", e.getMessage());
        }
    }

    @FXML private void onClose() { closeButton.getScene().getWindow().hide(); }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showWarn(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}

