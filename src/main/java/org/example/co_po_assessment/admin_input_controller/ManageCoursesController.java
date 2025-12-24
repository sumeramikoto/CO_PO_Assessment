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
import org.example.co_po_assessment.Objects.Course;
import org.example.co_po_assessment.DB_helper.CoursesDatabaseHelper;
import org.example.co_po_assessment.DB_helper.DatabaseService;
import org.example.co_po_assessment.faculty_input_controller.CourseInputController;
import org.example.co_po_assessment.utilities.ExcelImportUtils;
import org.example.co_po_assessment.utilities.WindowUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

// POI imports for Excel generation
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ManageCoursesController implements Initializable {
    @FXML
    TableView<Course> courseTableView;
    @FXML
    TableColumn<Course, String> courseCodeColumn;
    @FXML
    TableColumn<Course, String> courseNameColumn;
    @FXML
    TableColumn<Course, Double> creditsColumn;
    @FXML
    TableColumn<Course, String> departmentColumn;
    @FXML
    TableColumn<Course, String> programmeColumn;
    // New columns for COs and POs
    @FXML
    TableColumn<Course, String> coNumbersColumn;
    @FXML

    // Reference to parent dashboard controller
    private org.example.co_po_assessment.dashboard_controller.AdminDashboardController dashboardController;

    public void setDashboardController(org.example.co_po_assessment.dashboard_controller.AdminDashboardController controller) {
        this.dashboardController = controller;
    }
    TableColumn<Course, String> poNumbersColumn;
    @FXML
    Button addCourseButton;
    @FXML
    Button editCourseButton;
    @FXML
    Button removeCourseButton;
    @FXML
    Button backButton;
    // New: referenced by FXML buttons
    @FXML Button excelTemplateButton;
    @FXML Button bulkImportCoursesButton;

    private ObservableList<Course> courseList;
    private CoursesDatabaseHelper databaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseHelper = new CoursesDatabaseHelper();

        // Set up table columns
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credit"));
        if (departmentColumn != null) {
            departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        }
        if (programmeColumn != null) {
            programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        }
        if (coNumbersColumn != null) {
            coNumbersColumn.setCellValueFactory(new PropertyValueFactory<>("coNumbers"));
        }
        if (poNumbersColumn != null) {
            poNumbersColumn.setCellValueFactory(new PropertyValueFactory<>("poNumbers"));
        }

        // Initialize course list
        courseList = FXCollections.observableArrayList();
        courseTableView.setItems(courseList);

        // Load existing course data
        loadCourseData();
    }

    public void onAddCourseButton(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/courseInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 520);
            CourseInputController controller = fxmlLoader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Add New Course");
            WindowUtils.setSceneAndMaximize(stage, scene);
            stage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Add Course window: " + e.getMessage());
        }
    }

    public void onEditCourseButton(ActionEvent actionEvent) {
        Course selectedCourse = courseTableView.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showWarningAlert("No Selection", "Please select a course to edit.");
            return;
        }

        try {
            // Get the current COs and POs for the course
            List<Integer> currentCOs = getCourseCOs(selectedCourse.getCode(), selectedCourse.getProgramme());
            List<Integer> currentPOs = getCoursePOs(selectedCourse.getCode(), selectedCourse.getProgramme());

            // Open the Course Input window in edit mode
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/courseInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 520);
            
            CourseInputController controller = fxmlLoader.getController();
            controller.setParentController(this);
            controller.setEditMode(selectedCourse.getCode(), selectedCourse.getTitle(), 
                                  selectedCourse.getCredit(), selectedCourse.getDepartment(),
                                  selectedCourse.getProgramme(), currentCOs, currentPOs);

            Stage stage = new Stage();
            stage.setTitle("Edit Course");
            WindowUtils.setSceneAndMaximize(stage, scene);
            stage.showAndWait();

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load course data: " + e.getMessage());
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Edit Course window: " + e.getMessage());
        }
    }

    public void onRemoveCourseButton(ActionEvent actionEvent) {
        Course selectedCourse = courseTableView.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showWarningAlert("No Selection", "Please select a course to remove.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Course");
        confirmAlert.setContentText("Are you sure you want to remove the course:\n" +
                "Code: " + selectedCourse.getCode() + "\n" +
                "Name: " + selectedCourse.getTitle() + "\n" +
                "Credits: " + selectedCourse.getCredit() + "\n" +
                "Department: " + selectedCourse.getDepartment() + "\n" +
                "Programme: " + selectedCourse.getProgramme() + "\n\n" +
                "Note: This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseHelper.removeCourse(selectedCourse.getCode(), selectedCourse.getProgramme());
                courseList.remove(selectedCourse);
                showInfoAlert("Success", "Course removed successfully.");
            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove course: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        // Navigate back to dashboard home
        if (dashboardController != null) {
            dashboardController.loadHomeView();
        }
    }

    @FXML private void onExcelTemplateButton() {
        // Let user choose where to save the template
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Courses Import Template");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("CoursesTemplate.xlsx");
        Stage stage = (Stage) backButton.getScene().getWindow();
        File file = chooser.showSaveDialog(stage);
        if (file == null) return;
        if (!file.getName().toLowerCase().endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Courses");

            // Header row (add COs and POs)
            String[] headers = {"Course Code", "Course Name", "Credits", "Department", "Programme", "COs", "POs"};
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setWrapText(true);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                int width;
                if (i == 1) width = 35; // Course Name wider
                else if (i >= 5) width = 18; // COs/POs
                else width = 20;
                sheet.setColumnWidth(i, width * 256);
            }
            sheet.createFreezePane(0, 1);

            DataValidationHelper dvh = sheet.getDataValidationHelper();
            int firstRow = 1; // row 2 in Excel (below headers)
            int lastRow = 1000; // allow up to 1000 rows

            // Course Code: 3 uppercase letters, space, 4 digits (e.g., CSE 4107)
            String codeFormula =
                    "AND(" +
                    "LEN(A2)=8," +
                    "MID(A2,4,1)=\" \"," +
                    "ISNUMBER(VALUE(RIGHT(A2,4)))," +
                    "CODE(LEFT(A2,1))>=65, CODE(LEFT(A2,1))<=90," +
                    "CODE(MID(A2,2,1))>=65, CODE(MID(A2,2,1))<=90," +
                    "CODE(MID(A2,3,1))>=65, CODE(MID(A2,3,1))<=90" +
                    ")";
            DataValidationConstraint codeC = dvh.createCustomConstraint(codeFormula);
            CellRangeAddressList codeRange = new CellRangeAddressList(firstRow, lastRow, 0, 0);
            DataValidation codeDV = dvh.createValidation(codeC, codeRange);
            codeDV.setShowErrorBox(true);
            codeDV.createErrorBox("Invalid Course Code", "Use format ABC 1234 (3 uppercase letters, space, 4 digits). Example: CSE 4107");
            sheet.addValidationData(codeDV);

            // Course Name: non-empty (trimmed)
            String nameFormula = "LEN(TRIM(B2))>0";
            DataValidationConstraint nameC = dvh.createCustomConstraint(nameFormula);
            CellRangeAddressList nameRange = new CellRangeAddressList(firstRow, lastRow, 1, 1);
            DataValidation nameDV = dvh.createValidation(nameC, nameRange);
            nameDV.setShowErrorBox(true);
            nameDV.createErrorBox("Invalid Course Name", "Course Name cannot be empty.");
            sheet.addValidationData(nameDV);

            // Credits: positive number, only .0 or .5 steps
            String creditsFormula = "AND(ISNUMBER(C2), C2>0, MOD(C2*10,5)=0)";
            DataValidationConstraint creditsC = dvh.createCustomConstraint(creditsFormula);
            CellRangeAddressList creditsRange = new CellRangeAddressList(firstRow, lastRow, 2, 2);
            DataValidation creditsDV = dvh.createValidation(creditsC, creditsRange);
            creditsDV.setShowErrorBox(true);
            creditsDV.createErrorBox("Invalid Credits", "Enter a positive number in 0.5 steps (e.g., 3.0, 1.5).");
            sheet.addValidationData(creditsDV);

            // Department: exactly 3 uppercase letters
            String deptFormula =
                    "AND(" +
                    "LEN(D2)=3," +
                    "CODE(LEFT(D2,1))>=65, CODE(LEFT(D2,1))<=90," +
                    "CODE(MID(D2,2,1))>=65, CODE(MID(D2,2,1))<=90," +
                    "CODE(MID(D2,3,1))>=65, CODE(MID(D2,3,1))<=90" +
                    ")";
            DataValidationConstraint deptC = dvh.createCustomConstraint(deptFormula);
            CellRangeAddressList deptRange = new CellRangeAddressList(firstRow, lastRow, 3, 3);
            DataValidation deptDV = dvh.createValidation(deptC, deptRange);
            deptDV.setShowErrorBox(true);
            deptDV.createErrorBox("Invalid Department", "Department must be exactly 3 uppercase letters (e.g., CSE, MPE).");
            sheet.addValidationData(deptDV);

            // Programme: BSc/MSc/PhD in + 2 or 3 uppercase letters
            String progFormula =
                    "OR(" +
                    // BSc in XX or XXX
                    "AND(LEFT(E2,7)=\"BSc in \", OR(LEN(E2)=9, LEN(E2)=10)," +
                    "CODE(MID(E2,8,1))>=65, CODE(MID(E2,8,1))<=90," +
                    "CODE(MID(E2,9,1))>=65, CODE(MID(E2,9,1))<=90," +
                    "IF(LEN(E2)=10, AND(CODE(MID(E2,10,1))>=65, CODE(MID(E2,10,1))<=90), TRUE)" +
                    ")," +
                    // MSc in XX or XXX
                    "AND(LEFT(E2,7)=\"MSc in \", OR(LEN(E2)=9, LEN(E2)=10)," +
                    "CODE(MID(E2,8,1))>=65, CODE(MID(E2,8,1))<=90," +
                    "CODE(MID(E2,9,1))>=65, CODE(MID(E2,9,1))<=90," +
                    "IF(LEN(E2)=10, AND(CODE(MID(E2,10,1))>=65, CODE(MID(E2,10,1))<=90), TRUE)" +
                    ")," +
                    // PhD in XX or XXX
                    "AND(LEFT(E2,8)=\"PhD in \", OR(LEN(E2)=10, LEN(E2)=11)," +
                    "CODE(MID(E2,9,1))>=65, CODE(MID(E2,9,1))<=90," +
                    "CODE(MID(E2,10,1))>=65, CODE(MID(E2,10,1))<=90," +
                    "IF(LEN(E2)=11, AND(CODE(MID(E2,11,1))>=65, CODE(MID(E2,11,1))<=90), TRUE)" +
                    ")" +
                    ")";
            DataValidationConstraint progC = dvh.createCustomConstraint(progFormula);
            CellRangeAddressList progRange = new CellRangeAddressList(firstRow, lastRow, 4, 4);
            DataValidation progDV = dvh.createValidation(progC, progRange);
            progDV.setShowErrorBox(true);
            progDV.createErrorBox("Invalid Programme", "Programme must be like 'BSc in CSE' or 'MSc in EEE'.");
            sheet.addValidationData(progDV);

            // Validation for COs (column F) and POs (column G): allow only digits, commas, hyphens, and spaces; allow blank; enforce sane separators
            String allowedCOsFormula =
                    "OR(" +
                    "TRIM(F2)=\"\"," +
                    "AND(" +
                        // After stripping commas, hyphens and spaces, must be numeric
                        "ISNUMBER(VALUE(SUBSTITUTE(SUBSTITUTE(SUBSTITUTE(TRIM(F2),\",\",\"\"),\"-\",\"\"),\" \",\"\")))," +
                        // cannot start or end with comma or hyphen
                        "LEFT(TRIM(F2),1)<>\",\", LEFT(TRIM(F2),1)<>\"-\", RIGHT(TRIM(F2),1)<>\",\", RIGHT(TRIM(F2),1)<>\"-\"," +
                        // no repeated or illegal adjacent separators
                        "ISERROR(SEARCH(\",,\",F2)), ISERROR(SEARCH(\"--\",F2)), ISERROR(SEARCH(\",-\",F2)), ISERROR(SEARCH(\"-,\",F2))" +
                    ")" +
                    ")";
            DataValidationConstraint cosC = dvh.createCustomConstraint(allowedCOsFormula);
            CellRangeAddressList cosRange = new CellRangeAddressList(firstRow, lastRow, 5, 5);
            DataValidation cosDV = dvh.createValidation(cosC, cosRange);
            cosDV.setShowErrorBox(true);
            cosDV.createErrorBox("Invalid COs", "Use numbers separated by commas and optional ranges with '-' (e.g., 1-5 or 1,2,3). Only digits, commas, hyphens and spaces are allowed.");
            sheet.addValidationData(cosDV);

            String allowedPOsFormula =
                    "OR(" +
                    "TRIM(G2)=\"\"," +
                    "AND(" +
                    // After stripping commas, hyphens and spaces, must be numeric
                    "ISNUMBER(VALUE(SUBSTITUTE(SUBSTITUTE(SUBSTITUTE(TRIM(G2),\",\",\"\"),\"-\",\"\"),\" \",\"\")))," +
                    // cannot start or end with comma or hyphen
                    "LEFT(TRIM(G2),1)<>\",\", LEFT(TRIM(G2),1)<>\"-\", RIGHT(TRIM(G2),1)<>\",\", RIGHT(TRIM(G2),1)<>\"-\"," +
                    // no repeated or illegal adjacent separators
                    "ISERROR(SEARCH(\",,\",G2)), ISERROR(SEARCH(\"--\",G2)), ISERROR(SEARCH(\",-\",G2)), ISERROR(SEARCH(\"-,\",G2))" +
                    ")" +
                    ")";
            DataValidationConstraint posC = dvh.createCustomConstraint(allowedPOsFormula);
            CellRangeAddressList posRange = new CellRangeAddressList(firstRow, lastRow, 6, 6);
            DataValidation posDV = dvh.createValidation(posC, posRange);
            posDV.setShowErrorBox(true);
            posDV.createErrorBox("Invalid POs", "Use numbers separated by commas and optional ranges with '-' (e.g., 1-5 or 1,2,3). Only digits, commas, hyphens and spaces are allowed.");
            sheet.addValidationData(posDV);

//            // Example hint row - use valid examples so validation passes
            // Example hint row - use valid examples so validation passes
            Row example = sheet.createRow(1);
            example.createCell(5).setCellValue("1-5");
            example.createCell(6).setCellValue("1,2,5");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            showInfoAlert("Template Created", "Saved Excel template to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showErrorAlert("Template Error", "Failed to create template: " + e.getMessage());
        }
    }

    // Bulk import handler
    @FXML
    public void onBulkImportCourses(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Courses Excel File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(((Stage) backButton.getScene().getWindow()));
        if (file == null) return;
        List<String> errors = new ArrayList<>();
        int inserted = 0, skipped = 0, mapped = 0;
        try {
            List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(file);
            int rowNum = 1;
            for (Map<String, String> row : rows) {
                rowNum++;
                String code = ExcelImportUtils.get(row, "course_code", "code");
                String name = ExcelImportUtils.get(row, "course_name", "name", "title");
                String creditsStr = ExcelImportUtils.get(row, "credits", "credit");
                String dept = ExcelImportUtils.get(row, "department", "dept");
                String prog = ExcelImportUtils.get(row, "programme", "program", "program_name");
                String cosRaw = ExcelImportUtils.get(row, "cos", "course_outcomes");
                String posRaw = ExcelImportUtils.get(row, "pos", "program_outcomes", "po_s");
                if (code == null || name == null || creditsStr == null || dept == null || prog == null) {
                    errors.add("Row " + rowNum + ": missing required fields (course_code, course_name, credits, department, programme)");
                    skipped++;
                    continue;
                }
                double credits;
                try { credits = Double.parseDouble(creditsStr); } catch (NumberFormatException nfe) {
                    errors.add("Row " + rowNum + ": invalid credits '" + creditsStr + "'");
                    skipped++; continue;
                }
                OutcomeParseResult coParsed = parseAndValidateOutcomes(cosRaw, 20, "CO");
                OutcomeParseResult poParsed = parseAndValidateOutcomes(posRaw, 12, "PO");
                if (coParsed.error != null) errors.add("Row " + rowNum + ": " + coParsed.error);
                if (poParsed.error != null) errors.add("Row " + rowNum + ": " + poParsed.error);
                try {
                    databaseHelper.addCourse(code, name, credits, dept, prog);
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
                    if (!msg.contains("duplicate")) {
                        errors.add("Row " + rowNum + ": " + ex.getMessage());
                        skipped++;
                        continue;
                    }
                    // Duplicate -> proceed to mapping
                }
                // Map only valid outcomes
                try {
                    if (!coParsed.numbers.isEmpty()) { databaseHelper.assignCOsToCourse(code, prog, coParsed.numbers); mapped++; }
                    if (!poParsed.numbers.isEmpty()) { databaseHelper.assignPOsToCourse(code, prog, poParsed.numbers); mapped++; }
                } catch (SQLException mapEx) {
                    errors.add("Row " + rowNum + ": failed to map CO/POs - " + mapEx.getMessage());
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        loadCourseData();
        StringBuilder sb = new StringBuilder("Imported ").append(inserted).append(" course rows. Skipped ").append(skipped).append(".");
        if (mapped > 0) sb.append(" Mapped CO/POs on ").append(mapped).append(" row(s).");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, errors.size());i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size() - 10).append(" more");
        }
        showInfoAlert("Course Import", sb.toString());
    }

    // Updated to include department & programme and assign CO/POs when provided
    public void addNewCourse(String courseCode, String courseName, double credits, String department, String programme,
                             List<Integer> coNumbers, List<Integer> poNumbers) {
        try {
            databaseHelper.addCourse(courseCode, courseName, credits, department, programme);
            if (coNumbers != null && !coNumbers.isEmpty()) databaseHelper.assignCOsToCourse(courseCode, programme, coNumbers);
            if (poNumbers != null && !poNumbers.isEmpty()) databaseHelper.assignPOsToCourse(courseCode, programme, poNumbers);
            Course newCourse = new Course(courseCode, courseName, "", "", credits, programme, department);
            // Populate display values for COs/POs
            if (coNumbers != null && !coNumbers.isEmpty()) newCourse.setCoNumbers(joinInts(new ArrayList<>(new LinkedHashSet<>(coNumbers))));
            else newCourse.setCoNumbers(joinInts(getCourseCOs(courseCode, programme)));
            if (poNumbers != null && !poNumbers.isEmpty()) newCourse.setPoNumbers(joinInts(new ArrayList<>(new LinkedHashSet<>(poNumbers))));
            else newCourse.setPoNumbers(joinInts(getCoursePOs(courseCode, programme)));
            courseList.add(newCourse);
            showInfoAlert("Success", "Course added successfully.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                showErrorAlert("Course Error", "A course with this code already exists for the selected programme.");
            } else {
                showErrorAlert("Database Error", "Failed to add course: " + e.getMessage());
            }
        }
    }

    // Backward compatible existing call sites (if any)
    public void addNewCourse(String courseCode, String courseName, double credits, String department, String programme) {
        addNewCourse(courseCode, courseName, credits, department, programme, Collections.emptyList(), Collections.emptyList());
    }

    // Update existing course
    public void updateCourse(String oldCourseCode, String oldProgramme, String newCourseCode, String courseName, 
                            double credits, String department, String newProgramme,
                            List<Integer> coNumbers, List<Integer> poNumbers) {
        try {
            // Update course info using DatabaseService
            DatabaseService databaseService = DatabaseService.getInstance();
            databaseService.updateCourseInfo(oldCourseCode, oldProgramme, courseName, credits, department);
            
            // If course code or programme changed, this would require complex updates - for now we don't allow that
            // The UI should disable editing of course code and programme
            
            // Update CO and PO mappings by deleting old ones and adding new ones
            if (coNumbers != null && !coNumbers.isEmpty()) {
                databaseHelper.assignCOsToCourse(oldCourseCode, oldProgramme, coNumbers);
            }
            if (poNumbers != null && !poNumbers.isEmpty()) {
                databaseHelper.assignPOsToCourse(oldCourseCode, oldProgramme, poNumbers);
            }
            
            // Update in the table
            for (int i = 0; i < courseList.size(); i++) {
                Course c = courseList.get(i);
                if (c.getCode().equals(oldCourseCode) && c.getProgramme().equals(oldProgramme)) {
                    Course updatedCourse = new Course(oldCourseCode, courseName, "", "", credits, oldProgramme, department);
                    updatedCourse.setCoNumbers(joinInts(coNumbers));
                    updatedCourse.setPoNumbers(joinInts(poNumbers));
                    courseList.set(i, updatedCourse);
                    break;
                }
            }
            
            showInfoAlert("Success", "Course updated successfully.");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update course: " + e.getMessage());
        }
    }

    @FXML
    private void onExportCourses(ActionEvent actionEvent) {
        // Export current courses with CO/PO mappings
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Courses to Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("Courses.xlsx");
        Stage stage = (Stage) backButton.getScene().getWindow();
        File out = chooser.showSaveDialog(stage);
        if (out == null) return;
        if (!out.getName().toLowerCase().endsWith(".xlsx")) out = new File(out.getParentFile(), out.getName() + ".xlsx");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Courses");
            String[] headers = {"Course Code", "Course Name", "Credits", "Department", "Programme", "COs", "POs"};
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont(); headerFont.setBold(true); headerStyle.setFont(headerFont);
            for (int i=0;i<headers.length;i++) { Cell cell = headerRow.createCell(i); cell.setCellValue(headers[i]); cell.setCellStyle(headerStyle); sheet.setColumnWidth(i, (i==1?35:20)*256); }

            int r = 1;
            for (Course c : courseList) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(c.getCode());
                row.createCell(1).setCellValue(c.getTitle());
                row.createCell(2).setCellValue(c.getCredit());
                row.createCell(3).setCellValue(c.getDepartment());
                row.createCell(4).setCellValue(c.getProgramme());
                try {
                    List<Integer> coNums = getCourseCOs(c.getCode(), c.getProgramme());
                    List<Integer> poNums = getCoursePOs(c.getCode(), c.getProgramme());
                    row.createCell(5).setCellValue(joinInts(coNums));
                    row.createCell(6).setCellValue(joinInts(poNums));
                } catch (SQLException e) {
                    row.createCell(5).setCellValue("");
                    row.createCell(6).setCellValue("");
                }
            }
            try (FileOutputStream fos = new FileOutputStream(out)) { wb.write(fos); }
            showInfoAlert("Export Complete", "Exported to: " + out.getAbsolutePath());
        } catch (IOException e) {
            showErrorAlert("Export Failed", "Could not write Excel: " + e.getMessage());
        }
    }

    private List<Integer> getCourseCOs(String courseCode, String programme) throws SQLException {
        // query DB for CO ids mapped to this course and translate to numbers
        List<Integer> out = new ArrayList<>();
        String sql = "SELECT co.co_number FROM Course_CO cc JOIN CO co ON cc.co_id = co.id WHERE cc.course_code=? AND cc.programme=? ORDER BY co.id";
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(org.example.co_po_assessment.DB_Configuration.DBconfig.getUrl(), org.example.co_po_assessment.DB_Configuration.DBconfig.getUserName(), org.example.co_po_assessment.DB_Configuration.DBconfig.getPassword());
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setString(2, programme);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getString(1); // e.g., CO5
                    out.add(extractTrailingInt(label));
                }
            }
        }
        return out;
    }

    private List<Integer> getCoursePOs(String courseCode, String programme) throws SQLException {
        List<Integer> out = new ArrayList<>();
        String sql = "SELECT po.po_number FROM Course_PO cp JOIN PO po ON cp.po_id = po.id WHERE cp.course_code=? AND cp.programme=? ORDER BY po.id";
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(org.example.co_po_assessment.DB_Configuration.DBconfig.getUrl(), org.example.co_po_assessment.DB_Configuration.DBconfig.getUserName(), org.example.co_po_assessment.DB_Configuration.DBconfig.getPassword());
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setString(2, programme);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String label = rs.getString(1); // e.g., PO2
                    out.add(extractTrailingInt(label));
                }
            }
        }
        return out;
    }

    private static int extractTrailingInt(String label) {
        if (label == null) return -1;
        for (int i=label.length()-1;i>=0;i--) {
            if (!Character.isDigit(label.charAt(i))) {
                String num = label.substring(i+1);
                try { return Integer.parseInt(num); } catch (NumberFormatException e) { return -1; }
            }
        }
        try { return Integer.parseInt(label); } catch (Exception e) { return -1; }
    }

    private static String joinInts(List<Integer> ints) {
        if (ints == null || ints.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<ints.size();i++) {
            if (i>0) sb.append(',');
            sb.append(ints.get(i));
        }
        return sb.toString();
    }

    private static List<Integer> parseOutcomeNumbers(String raw) {
        List<Integer> nums = new ArrayList<>();
        if (raw == null) return nums;
        String s = raw.trim();
        if (s.isEmpty()) return nums;
        // Normalize separators
        s = s.replace("CO", "").replace("Po", "").replace("po", "").replace("PO", "");
        s = s.replaceAll("\\s+", " ");
        // Split by comma or space
        String[] parts = s.split("[ ,;]+");
        // Also handle ranges like 1-5
        for (String p : parts) {
            if (p.isBlank()) continue;
            if (p.contains("-")) {
                String[] ab = p.split("-");
                if (ab.length == 2) {
                    try {
                        int a = Integer.parseInt(ab[0].trim());
                        int b = Integer.parseInt(ab[1].trim());
                        if (a <= b) {
                            for (int x=a; x<=b; x++) if (!nums.contains(x)) nums.add(x);
                        }
                    } catch (NumberFormatException ignore) { /* skip */ }
                }
            } else {
                try {
                    int v = Integer.parseInt(p.trim());
                    if (!nums.contains(v)) nums.add(v);
                } catch (NumberFormatException ignore) { /* skip */ }
            }
        }
        // Sort
        Collections.sort(nums);
        return nums;
    }

    private static class OutcomeParseResult {
        final List<Integer> numbers; final String error;
        OutcomeParseResult(List<Integer> numbers, String error) { this.numbers = numbers; this.error = error; }
    }

    private static OutcomeParseResult parseAndValidateOutcomes(String raw, int maxAllowed, String label) {
        List<Integer> nums = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return new OutcomeParseResult(nums, null);
        String s = raw.trim().replaceAll("\\s+", " ");
        s = s.replaceAll("(?i)CO", "").replaceAll("(?i)PO", "");
        String[] parts = s.split("[ ,;]+");
        List<String> invalid = new ArrayList<>();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (part.contains("-")) {
                String[] ab = part.split("-");
                if (ab.length != 2) { invalid.add(part); continue; }
                try {
                    int a = Integer.parseInt(ab[0].trim());
                    int b = Integer.parseInt(ab[1].trim());
                    if (a > b) { invalid.add(part); continue; }
                    for (int x=a; x<=b; x++) {
                        if (x < 1 || x > maxAllowed) { invalid.add(String.valueOf(x)); continue; }
                        if (!nums.contains(x)) nums.add(x);
                    }
                } catch (NumberFormatException nfe) {
                    invalid.add(part);
                }
            } else {
                try {
                    int v = Integer.parseInt(part.trim());
                    if (v < 1 || v > maxAllowed) invalid.add(part);
                    else if (!nums.contains(v)) nums.add(v);
                } catch (NumberFormatException nfe) {
                    invalid.add(part);
                }
            }
        }
        Collections.sort(nums);
        String error = invalid.isEmpty() ? null : (label + " values out of range or invalid: " + String.join(", ", invalid));
        return new OutcomeParseResult(nums, error);
    }

    private void loadCourseData() {
        try {
            var courseData = databaseHelper.getAllCourses();
            courseList.clear();
            for (var course : courseData) {
                Course c = new Course(
                    course.getCourseCode(),
                    course.getCourseName(),
                    "", // instructor
                    "", // academic year
                    course.getCredits(),
                    course.getProgramme(),
                    course.getDepartment()
                );
                // Populate COs and POs for display
                try {
                    List<Integer> coNums = getCourseCOs(course.getCourseCode(), course.getProgramme());
                    List<Integer> poNums = getCoursePOs(course.getCourseCode(), course.getProgramme());
                    c.setCoNumbers(joinInts(coNums));
                    c.setPoNumbers(joinInts(poNums));
                } catch (SQLException e) {
                    c.setCoNumbers("");
                    c.setPoNumbers("");
                }
                courseList.add(c);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load course data: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
