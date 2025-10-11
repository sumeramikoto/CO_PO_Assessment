package org.example.co_po_assessment.admin_input_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.example.co_po_assessment.Objects.CourseAssignment;
import org.example.co_po_assessment.DB_helper.CourseAssignmentDatabaseHelper;
import org.example.co_po_assessment.utilities.ExcelImportUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class ManageCourseAssignmentsController implements Initializable {
    @FXML
    TableView<CourseAssignment> courseTableView;
    @FXML
    TableColumn<CourseAssignment, String> courseCodeColumn;
    @FXML
    TableColumn<CourseAssignment, String> courseNameColumn;
    @FXML
    TableColumn<CourseAssignment, String> facultyColumn;
    @FXML
    TableColumn<CourseAssignment, String> academicYearColumn;
    @FXML
    TableColumn<CourseAssignment, String> departmentColumn;
    @FXML
    TableColumn<CourseAssignment, String> programmeColumn;
    @FXML
    Button assignCourseButton;
    @FXML
    Button removeCourseButton;
    @FXML
    Button backButton;

    private ObservableList<CourseAssignment> courseAssignmentList;
    private CourseAssignmentDatabaseHelper databaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseHelper = new CourseAssignmentDatabaseHelper();

        // Set up table columns
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        facultyColumn.setCellValueFactory(new PropertyValueFactory<>("facultyName"));
        academicYearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        if (departmentColumn != null) {
            departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        }
        if (programmeColumn != null) {
            programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        }

        // Initialize course assignment list
        courseAssignmentList = FXCollections.observableArrayList();
        courseTableView.setItems(courseAssignmentList);

        // Load existing course assignment data
        loadCourseAssignmentData();
    }

    public void onAssignCourseButton(ActionEvent actionEvent) {
        try {
            // Open the Course Assignment Input window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/courseAssignmentInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 450, 350);

            // Get the controller to handle data return
            CourseAssignmentInputController controller = fxmlLoader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Assign Course to Faculty");
            stage.setScene(scene);
            stage.showAndWait(); // Wait for the window to close before continuing

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Assign Course window: " + e.getMessage());
        }
    }

    @FXML private void onExcelTemplateButton() {
        try {
            // Save dialog
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Assignments Template");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
            chooser.setInitialFileName("AssignmentsTemplate.xlsx");
            File target = chooser.showSaveDialog(((Stage) backButton.getScene().getWindow()));
            if (target == null) return;

            // Create workbook and sheet
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                XSSFSheet sheet = wb.createSheet("Assignments");

                // Header style (bold)
                Font headerFont = wb.createFont();
                headerFont.setBold(true);
                CellStyle headerStyle = wb.createCellStyle();
                headerStyle.setFont(headerFont);

                // Create header row
                Row header = sheet.createRow(0);
                String[] headers = new String[]{
                        "Course Code", "Programme", "Faculty Shortname", "Academic Year"
                };
                for (int i = 0; i < headers.length; i++) {
                    Cell c = header.createCell(i, CellType.STRING);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                    sheet.autoSizeColumn(i);
                }

                // Prepare data validation over rows 2..501 (1-based), i.e., indices 1..500
                int firstDataRow = 1; // zero-based
                int lastDataRow = 500;
                DataValidationHelper dvh = sheet.getDataValidationHelper();

                // Column A (index 0): Course Code validation: AAA 1234
                String lettersArray = "{\"A\",\"B\",\"C\",\"D\",\"E\",\"F\",\"G\",\"H\",\"I\",\"J\",\"K\",\"L\",\"M\",\"N\",\"O\",\"P\",\"Q\",\"R\",\"S\",\"T\",\"U\",\"V\",\"W\",\"X\",\"Y\",\"Z\"}";
                String ccFormula = "AND(LEN(A2)=8, MID(A2,4,1)=\" \", ISNUMBER(VALUE(RIGHT(A2,4))), " +
                        "SUMPRODUCT(--ISNUMBER(MATCH(MID(LEFT(A2,3),ROW(INDIRECT(\"1:3\")),1)," + lettersArray + ",0)))=3)";
                DataValidationConstraint ccConstraint = dvh.createCustomConstraint(ccFormula);
                CellRangeAddressList ccRange = new CellRangeAddressList(firstDataRow, lastDataRow, 0, 0);
                DataValidation ccValidation = dvh.createValidation(ccConstraint, ccRange);
                ccValidation.setShowErrorBox(true);
                ccValidation.createErrorBox("Invalid Course Code", "Use format like CSE 4107 (3 uppercase letters, space, 4 digits).");
                sheet.addValidationData(ccValidation);

                // Column B (index 1): Programme validation
                // Accepts: BSc in XX/XXX, MSc in XX/XXX, PhD in XX/XXX (X uppercase letters)
                String progLetters = lettersArray;
                String bscFormula = "AND(LEFT(B2,7)=\"BSc in \", OR(LEN(B2)=9, LEN(B2)=10), " +
                        "SUMPRODUCT(--ISNUMBER(MATCH(MID(RIGHT(B2,LEN(B2)-7),ROW(INDIRECT(\"1:\"&LEN(B2)-7)),1)," + progLetters + ",0))) = LEN(B2)-7)";
                String mscFormula = "AND(LEFT(B2,7)=\"MSc in \", OR(LEN(B2)=9, LEN(B2)=10), " +
                        "SUMPRODUCT(--ISNUMBER(MATCH(MID(RIGHT(B2,LEN(B2)-7),ROW(INDIRECT(\"1:\"&LEN(B2)-7)),1)," + progLetters + ",0))) = LEN(B2)-7)";
                String phdFormula = "AND(LEFT(B2,8)=\"PhD in \", OR(LEN(B2)=10, LEN(B2)=11), " +
                        "SUMPRODUCT(--ISNUMBER(MATCH(MID(RIGHT(B2,LEN(B2)-8),ROW(INDIRECT(\"1:\"&LEN(B2)-8)),1)," + progLetters + ",0))) = LEN(B2)-8)";
                String progFormula = "OR(" + bscFormula + "," + mscFormula + "," + phdFormula + ")";
                DataValidationConstraint progConstraint = dvh.createCustomConstraint(progFormula);
                CellRangeAddressList progRange = new CellRangeAddressList(firstDataRow, lastDataRow, 1, 1);
                DataValidation progValidation = dvh.createValidation(progConstraint, progRange);
                progValidation.setShowErrorBox(true);
                progValidation.createErrorBox("Invalid Programme", "Use: BSc/MSc/PhD in XX or XXX (uppercase letters only).");
                sheet.addValidationData(progValidation);

                // Column C (index 2): Faculty Shortname - require non-empty
                String shortFormula = "LEN(C2)>0";
                DataValidationConstraint shortConstraint = dvh.createCustomConstraint(shortFormula);
                CellRangeAddressList shortRange = new CellRangeAddressList(firstDataRow, lastDataRow, 2, 2);
                DataValidation shortValidation = dvh.createValidation(shortConstraint, shortRange);
                shortValidation.setShowErrorBox(true);
                shortValidation.createErrorBox("Missing Shortname", "Faculty Shortname is required.");
                sheet.addValidationData(shortValidation);

                // Column D (index 3): Academic Year: YYYY-YYYY and consecutive years
                String yearFormula = "AND(LEN(D2)=9, MID(D2,5,1)=\"-\", ISNUMBER(VALUE(LEFT(D2,4))), ISNUMBER(VALUE(RIGHT(D2,4))), VALUE(RIGHT(D2,4))=VALUE(LEFT(D2,4))+1)";
                DataValidationConstraint yearConstraint = dvh.createCustomConstraint(yearFormula);
                CellRangeAddressList yearRange = new CellRangeAddressList(firstDataRow, lastDataRow, 3, 3);
                DataValidation yearValidation = dvh.createValidation(yearConstraint, yearRange);
                yearValidation.setShowErrorBox(true);
                yearValidation.createErrorBox("Invalid Academic Year", "Use consecutive years like 2023-2024 (second year must be first + 1).");
                sheet.addValidationData(yearValidation);

                // Freeze header and autosize
                sheet.createFreezePane(0, 1);
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write to file
                // Ensure parent dirs exist
                File parent = target.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                try (FileOutputStream out = new FileOutputStream(target)) {
                    wb.write(out);
                }
            }

            showInfoAlert("Template Created", "Excel template saved to:\n" + target.getAbsolutePath());
        } catch (Exception e) {
            showErrorAlert("Template Error", "Failed to create Excel template: " + e.getMessage());
        }
    }

    // Bulk import handler
    @FXML
    public void onBulkImportAssignments(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Assignments Excel File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(((Stage) backButton.getScene().getWindow()));
        if (file == null) return;
        List<String> errors = new ArrayList<>();
        int inserted = 0, skipped = 0;
        try {
            List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(file);
            int rowNum = 1;
            for (Map<String, String> row : rows) {
                rowNum++;
                String code = ExcelImportUtils.get(row, "course_code", "course", "code");
                String programme = ExcelImportUtils.get(row, "programme", "program");
                String facultyShort = ExcelImportUtils.get(row, "faculty_shortname", "shortname");
                String facultyName = ExcelImportUtils.get(row, "faculty_name", "faculty", "instructor", "teacher");
                String ay = ExcelImportUtils.get(row, "academic_year", "year", "ay");
                if (code == null || programme == null || ay == null || (facultyShort == null && facultyName == null)) {
                    errors.add("Row " + rowNum + ": missing required fields (course_code, programme, faculty_[short]name, academic_year)");
                    skipped++; continue;
                }
                // Validate academic year from Excel too
                if (!isValidAcademicYear(ay)) {
                    errors.add("Row " + rowNum + ": invalid academic year '" + ay + "' (must be consecutive like 2023-2024)");
                    skipped++; continue;
                }
                try {
                    if (facultyShort != null && !facultyShort.isBlank()) {
                        databaseHelper.assignCourseByShortname(code, programme, facultyShort, ay);
                    } else {
                        databaseHelper.assignCourse(code, programme, facultyName, ay);
                    }
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && (msg.contains("already assigned") || msg.contains("DUPLICATE_COURSE_YEAR") || msg.toLowerCase().contains("duplicate"))) {
                        errors.add("Row " + rowNum + ": duplicate for (" + code + ", " + programme + ", " + ay + ")");
                    } else {
                        errors.add("Row " + rowNum + ": " + msg);
                    }
                    skipped++;
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        loadCourseAssignmentData();
        StringBuilder sb = new StringBuilder();
        sb.append("Imported ").append(inserted).append(" rows. Skipped ").append(skipped).append(".");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, errors.size()); i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size()-10).append(" more");
        }
        showInfoAlert("Assignments Import", sb.toString());
    }

    public void onRemoveCourseButton(ActionEvent actionEvent) {
        CourseAssignment selected = courseTableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarningAlert("No Selection", "Please select a course assignment to remove.");
            return;
        }

        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Course Assignment");
        confirm.setContentText("Are you sure you want to remove the assignment:\n" +
                "Course: " + selected.getCourseCode() + " - " + selected.getCourseName() + "\n" +
                "Faculty: " + selected.getFacultyName() + "\n" +
                "Academic Year: " + selected.getAcademicYear() + "\n" +
                "Department: " + selected.getDepartment() + "\n" +
                "Programme: " + selected.getProgramme());

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Ensure we pass only the full name to the DB layer
                String facultyFullName = extractFacultyFullName(selected.getFacultyName());

                // Remove from database
                databaseHelper.removeCourseAssignment(
                        selected.getCourseCode(),
                        selected.getProgramme(),
                        facultyFullName,
                        selected.getAcademicYear()
                );

                // Remove from table
                courseAssignmentList.remove(selected);

                showInfoAlert("Success", "Course assignment removed successfully.");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove course assignment: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        // Close the current window
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Method to be called by CourseAssignmentInputController when new assignment is added
     */
    public void addNewCourseAssignment(String courseCode, String programme, String facultyName, String academicYear) {
        try {
            // Add to database
            databaseHelper.assignCourse(courseCode, programme, facultyName, academicYear);

            // Reload data to get the course name
            loadCourseAssignmentData();

            showInfoAlert("Success", "Course assigned successfully.");

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("already assigned") || msg.contains("DUPLICATE_COURSE_YEAR") || msg.contains("Duplicate entry"))) {
                showErrorAlert("Assignment Error", "This course already has an assignment for that academic year (same department & programme).");
            } else {
                showErrorAlert("Database Error", "Failed to assign course: " + msg);
            }
        }
    }

    /**
     * Load course assignment data from database
     */
    private void loadCourseAssignmentData() {
        try {
            // Get all course assignments from database
            var data = databaseHelper.getAllCourseAssignments();

            courseAssignmentList.clear();
            for (var a : data) {
                courseAssignmentList.add(new CourseAssignment(
                        a.getCourseCode(),
                        a.getCourseName(),
                        a.getFacultyName(),
                        a.getAcademicYear(),
                        a.getDepartment(),
                        a.getProgramme()
                ));
            }

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load course assignment data: " + e.getMessage());
        }
    }

    /** Extracts the faculty full name from a display value like "Full Name (Shortname)". */
    private String extractFacultyFullName(String displayValue) {
        if (displayValue == null) return null;
        String s = displayValue.trim();
        int open = s.lastIndexOf(" (");
        int close = s.endsWith(")") ? s.length() - 1 : -1;
        if (open > 0 && close > open) {
            return s.substring(0, open).trim();
        }
        return s;
    }

    /**
     * Helper method to show error alerts
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show warning alerts
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Helper method to show information alerts
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidAcademicYear(String academicYear) {
        if (academicYear == null) return false;
        String s = academicYear.trim();
        if (!s.matches("\\d{4}-\\d{4}")) return false;
        try {
            int y1 = Integer.parseInt(s.substring(0,4));
            int y2 = Integer.parseInt(s.substring(5,9));
            return y2 == y1 + 1;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
