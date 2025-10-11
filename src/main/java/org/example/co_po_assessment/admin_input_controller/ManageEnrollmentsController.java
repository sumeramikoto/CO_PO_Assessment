package org.example.co_po_assessment.admin_input_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.example.co_po_assessment.DB_helper.DatabaseService;
import org.example.co_po_assessment.DB_helper.StudentDatabaseHelper;
import org.example.co_po_assessment.utilities.ExcelImportUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
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
    @FXML private Button unenrollButton; // new button for unenrollment

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
            // Courses (detailed: code - name - dept - programme)
            List<String> courses = db.getCoursesDetailed();
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
        String courseDisplay = courseCombo.getValue();
        String year = academicYearCombo.getValue();
        if (courseDisplay == null || courseDisplay.isBlank()) { showWarn("Validation", "Select a course"); return; }
        if (year == null || year.isBlank()) { showWarn("Validation", "Select an academic year"); return; }
        String[] parts = courseDisplay.split(" - ");
        String courseCode = parts[0];
        String courseDept = parts.length >= 3 ? parts[2] : null;
        String courseProg = parts.length >= 4 ? parts[3] : null;
        List<StudentDatabaseHelper.StudentData> selected = studentTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) { showWarn("Validation", "Select at least one student"); return; }
        List<StudentDatabaseHelper.StudentData> eligible = new ArrayList<>();
        List<StudentDatabaseHelper.StudentData> mismatched = new ArrayList<>();
        for (StudentDatabaseHelper.StudentData s : selected) {
            if (courseDept != null && courseProg != null && s.department != null && s.programme != null &&
                    courseDept.equalsIgnoreCase(s.department) && courseProg.equalsIgnoreCase(s.programme)) {
                eligible.add(s);
            } else {
                mismatched.add(s);
            }
        }
        if (eligible.isEmpty()) {
            showWarn("Enrollment", "No selected students match the course's department/programme (" + courseDept + "/" + courseProg + ").");
            return;
        }
        List<String> ids = eligible.stream().map(s -> s.id).collect(Collectors.toList());
        try {
            db.enrollStudents(courseCode, courseProg, year, ids);
            StringBuilder msg = new StringBuilder();
            msg.append("Enrolled/updated ").append(ids.size()).append(" students for course ").append(courseCode).append(" (" + year + ")");
            if (!mismatched.isEmpty()) {
                msg.append(". Skipped ").append(mismatched.size()).append(" mismatched: ");
                msg.append(mismatched.stream().map(m -> m.id).collect(Collectors.joining(", ")));
            }
            showInfo("Enrollment", msg.toString());
        } catch (SQLException e) {
            showError("Enrollment Failed", e.getMessage());
        }
    }

    @FXML private void onUnenrollSelected() { // new handler
        String courseDisplay = courseCombo.getValue();
        String year = academicYearCombo.getValue();
        if (courseDisplay == null || courseDisplay.isBlank()) { showWarn("Validation", "Select a course"); return; }
        if (year == null || year.isBlank()) { showWarn("Validation", "Select an academic year"); return; }
        String[] parts = courseDisplay.split(" - ");
        String courseCode = parts[0];
        String courseProg = parts.length >= 4 ? parts[3] : null;
        List<StudentDatabaseHelper.StudentData> selected = studentTableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) { showWarn("Validation", "Select at least one student"); return; }
        List<String> ids = selected.stream().map(s -> s.id).collect(Collectors.toList());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Unenroll");
        confirm.setHeaderText(null);
        confirm.setContentText("Unenroll " + ids.size() + " students from course " + courseCode + " (" + year + ")? This will remove their enrollment and any associated marks for this course-year may become orphaned.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            int removed = db.unenrollStudents(courseCode, courseProg, year, ids);
            showInfo("Unenrollment", "Removed " + removed + " enrollments for course " + courseCode + " (" + year + ")");
        } catch (SQLException e) {
            showError("Unenrollment Failed", e.getMessage());
        }
    }

    // Bulk import enrollments from Excel
    @FXML private void onBulkImportEnrollments() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Enrollments Excel File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(studentTableView.getScene().getWindow());
        if (file == null) return;
        List<Map<String, String>> rows;
        try { rows = ExcelImportUtils.readSheetAsMaps(file); } catch (Exception ex) { showError("Import Failed", ex.getMessage()); return; }
        if (rows.isEmpty()) { showWarn("Import", "No data rows found in the sheet."); return; }
        // Determine defaults from UI if present
        String selectedCourse = courseCombo.getValue();
        String selectedYear = academicYearCombo.getValue();
        String defaultCourseCode = null, defaultProgramme = null;
        if (selectedCourse != null && !selectedCourse.isBlank()) {
            String[] parts = selectedCourse.split(" - ");
            defaultCourseCode = parts[0];
            defaultProgramme = parts.length >= 4 ? parts[3] : null;
        }
        // Group by (course, programme, year)
        Map<String, List<String>> group = new LinkedHashMap<>(); // key: code|prog|year -> studentIds
        List<String> issues = new ArrayList<>();
        int rowNum = 1;
        for (Map<String, String> row : rows) {
            rowNum++;
            String sid = ExcelImportUtils.get(row, "student_id", "id");
            String code = orElse(ExcelImportUtils.get(row, "course_code", "course"), defaultCourseCode);
            String prog = orElse(ExcelImportUtils.get(row, "programme", "program"), defaultProgramme);
            String year = orElse(ExcelImportUtils.get(row, "academic_year", "year", "ay"), selectedYear);
            if (sid == null || code == null || prog == null || year == null) {
                issues.add("Row " + rowNum + ": missing student_id/course_code/programme/academic_year");
                continue;
            }
            String key = code + "|" + prog + "|" + year;
            group.computeIfAbsent(key, k -> new ArrayList<>()).add(sid);
        }
        int groups = 0; int total = 0;
        for (Map.Entry<String, List<String>> e : group.entrySet()) {
            String[] parts = e.getKey().split("\\|");
            String code = parts[0]; String prog = parts[1]; String year = parts[2];
            List<String> ids = e.getValue();
            try { db.enrollStudents(code, prog, year, ids); total += ids.size(); groups++; }
            catch (SQLException ex) { issues.add("Group (" + code + "," + prog + "," + year + "): " + ex.getMessage()); }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Processed ").append(groups).append(" course-year groups. Total rows: ").append(total).append(".");
        if (!issues.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, issues.size()); i++) sb.append("- ").append(issues.get(i)).append('\n');
            if (issues.size() > 10) sb.append("... and ").append(issues.size()-10).append(" more");
        }
        showInfo("Enrollment Import", sb.toString());
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

    private static String orElse(String v, String def) { return v == null || v.isBlank() ? def : v; }
}
