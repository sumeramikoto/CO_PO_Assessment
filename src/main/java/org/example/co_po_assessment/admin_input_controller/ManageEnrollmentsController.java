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
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddressList;

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
    @FXML private Button excelTemplateButton;
    @FXML private Button importExcelButton;

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

    @FXML private void onExcelTemplateButton() {
        // Create an Excel workbook with headers and validations
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Enrollments");

            // Header style
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);

            // Create header row
            Row header = sheet.createRow(0);
            String[] headers = new String[]{"Student ID", "Course Code", "Programme", "Academic Year"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            // Set reasonable column widths
            int[] widths = new int[]{14, 14, 18, 14}; // approx characters
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }

            // Data validation for rows 2..1001 (0-based rows 1..1000)
            int firstDataRow = 1;
            int lastDataRow = 1000;
            DataValidationHelper dvh = sheet.getDataValidationHelper();

            // A: Student ID -> 9-digit whole number between 100000000 and 999999999
            CellRangeAddressList idRange = new CellRangeAddressList(firstDataRow, lastDataRow, 0, 0);
            DataValidationConstraint idConstraint = dvh.createIntegerConstraint(DataValidationConstraint.OperatorType.BETWEEN, "100000000", "999999999");
            DataValidation idValidation = dvh.createValidation(idConstraint, idRange);
            idValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            idValidation.createErrorBox("Invalid Student ID", "Student ID must be a 9-digit number (e.g., 220042101).");
            idValidation.createPromptBox("Student ID", "Enter a 9-digit number (no spaces or letters).");
            idValidation.setShowErrorBox(true);
            idValidation.setShowPromptBox(true);
            sheet.addValidationData(idValidation);

            // B: Course Code -> custom formula: 3 uppercase letters, space, 4 digits
            String courseFormula = "AND(LEN(B2)=8, CODE(MID(B2,1,1))>=65, CODE(MID(B2,1,1))<=90, " +
                    "CODE(MID(B2,2,1))>=65, CODE(MID(B2,2,1))<=90, CODE(MID(B2,3,1))>=65, CODE(MID(B2,3,1))<=90, " +
                    "MID(B2,4,1)=\" \", ISNUMBER(--MID(B2,5,4)))";
            CellRangeAddressList courseRange = new CellRangeAddressList(firstDataRow, lastDataRow, 1, 1);
            DataValidationConstraint courseConstraint = dvh.createCustomConstraint(courseFormula);
            DataValidation courseValidation = dvh.createValidation(courseConstraint, courseRange);
            courseValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            courseValidation.createErrorBox("Invalid Course Code", "Format must be three uppercase letters, space, and four digits (e.g., CSE 4107).");
            courseValidation.createPromptBox("Course Code", "Example: CSE 4107");
            courseValidation.setShowErrorBox(true);
            courseValidation.setShowPromptBox(true);
            // Some Excel versions require this to avoid 'List of values' warning for custom validations
            courseValidation.setSuppressDropDownArrow(true);
            sheet.addValidationData(courseValidation);

            // C: Programme -> "BSc in XX/XXX" or "MSc in XX/XXX" or "PhD in XX/XXX"
            // Build custom formula checking prefixes and 2-3 trailing uppercase letters
            String prog2Letters = "AND(LEN(C2)=9, CODE(MID(C2,8,1))>=65, CODE(MID(C2,8,1))<=90, CODE(MID(C2,9,1))>=65, CODE(MID(C2,9,1))<=90)";
            String prog3Letters = "AND(LEN(C2)=10, CODE(MID(C2,8,1))>=65, CODE(MID(C2,8,1))<=90, CODE(MID(C2,9,1))>=65, CODE(MID(C2,9,1))<=90, CODE(MID(C2,10,1))>=65, CODE(MID(C2,10,1))<=90)";
            String progSuffixOK = "OR(" + prog2Letters + "," + prog3Letters + ")";
            String progFormula = "OR(" +
                    "AND(LEFT(C2,7)=\"BSc in \"," + progSuffixOK + ")," +
                    "AND(LEFT(C2,7)=\"MSc in \"," + progSuffixOK + ")," +
                    "AND(LEFT(C2,7)=\"PhD in \"," + progSuffixOK + ")" +
                    ")";
            CellRangeAddressList progRange = new CellRangeAddressList(firstDataRow, lastDataRow, 2, 2);
            DataValidationConstraint progConstraint = dvh.createCustomConstraint(progFormula);
            DataValidation progValidation = dvh.createValidation(progConstraint, progRange);
            progValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            progValidation.createErrorBox("Invalid Programme", "Must be: BSc in XX/XXX, MSc in XX/XXX, or PhD in XX/XXX (e.g., BSc in CSE, MSc in CE).");
            progValidation.createPromptBox("Programme", "Examples: BSc in CE, MSc in CSE, PhD in EE");
            progValidation.setShowErrorBox(true);
            progValidation.setShowPromptBox(true);
            progValidation.setSuppressDropDownArrow(true);
            sheet.addValidationData(progValidation);

            // D: Academic Year -> YYYY-YYYY where second = first + 1
            String yearFormula = "AND(LEN(D2)=9, MID(D2,5,1)=\"-\", ISNUMBER(--LEFT(D2,4)), ISNUMBER(--RIGHT(D2,4)), VALUE(RIGHT(D2,4)) = VALUE(LEFT(D2,4)) + 1)";
            CellRangeAddressList yearRange = new CellRangeAddressList(firstDataRow, lastDataRow, 3, 3);
            DataValidationConstraint yearConstraint = dvh.createCustomConstraint(yearFormula);
            DataValidation yearValidation = dvh.createValidation(yearConstraint, yearRange);
            yearValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            yearValidation.createErrorBox("Invalid Academic Year", "Format must be YYYY-YYYY and the second year must be the first year + 1 (e.g., 2023-2024).");
            yearValidation.createPromptBox("Academic Year", "Example: 2023-2024");
            yearValidation.setShowErrorBox(true);
            yearValidation.setShowPromptBox(true);
            yearValidation.setSuppressDropDownArrow(true);
            sheet.addValidationData(yearValidation);

            // Save dialog
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Enrollments Template");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
            chooser.setInitialFileName("EnrollmentsTemplate.xlsx");
            File out = chooser.showSaveDialog(studentTableView.getScene().getWindow());
            if (out == null) return;

            // Ensure .xlsx extension
            String path = out.getAbsolutePath();
            if (!path.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                out = new File(path + ".xlsx");
            }

            try (FileOutputStream fos = new FileOutputStream(out)) {
                wb.write(fos);
            }
            showInfo("Template Saved", "Excel template saved to: " + out.getAbsolutePath());
        } catch (Exception ex) {
            showError("Template Error", ex.getMessage());
        }
    }

    // Bulk import enrollments from Excel
    @FXML private void onExcelImport() {
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
