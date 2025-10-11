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
import org.example.co_po_assessment.faculty_input_controller.CourseInputController;
import org.example.co_po_assessment.utilities.ExcelImportUtils;

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
    @FXML
    Button addCourseButton;
    @FXML
    Button removeCourseButton;
    @FXML
    Button backButton;

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

        // Initialize course list
        courseList = FXCollections.observableArrayList();
        courseTableView.setItems(courseList);

        // Load existing course data
        loadCourseData();
    }

    public void onAddCourseButton(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/courseInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 500, 420);
            CourseInputController controller = fxmlLoader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setTitle("Add New Course");
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Add Course window: " + e.getMessage());
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
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
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

            // Header row
            String[] headers = {"Course Code", "Course Name", "Credits", "Department", "Programme"};
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
                sheet.setColumnWidth(i, (i == 1 ? 35 : 20) * 256); // wider for Course Name
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
                    "AND(LEFT(E2,7)=\"PhD in \", OR(LEN(E2)=9, LEN(E2)=10)," +
                    "CODE(MID(E2,8,1))>=65, CODE(MID(E2,8,1))<=90," +
                    "CODE(MID(E2,9,1))>=65, CODE(MID(E2,9,1))<=90," +
                    "IF(LEN(E2)=10, AND(CODE(MID(E2,10,1))>=65, CODE(MID(E2,10,1))<=90), TRUE)" +
                    ")" +
                    ")";
            DataValidationConstraint progC = dvh.createCustomConstraint(progFormula);
            CellRangeAddressList progRange = new CellRangeAddressList(firstRow, lastRow, 4, 4);
            DataValidation progDV = dvh.createValidation(progC, progRange);
            progDV.setShowErrorBox(true);
            progDV.createErrorBox("Invalid Programme", "Use: BSc/MSc/PhD in followed by 2 or 3 uppercase letters (e.g., BSc in CSE, BSc in CE).");
            sheet.addValidationData(progDV);

            // Save workbook
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
            showInfoAlert("Template Created", "Saved Excel template to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showErrorAlert("Save Failed", "Unable to create Excel template: " + e.getMessage());
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
        int inserted = 0, skipped = 0;
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
                try {
                    databaseHelper.addCourse(code, name, credits, dept, prog);
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("duplicate")) errors.add("Row " + rowNum + ": duplicate (" + code + ", " + prog + ")");
                    else errors.add("Row " + rowNum + ": " + msg);
                    skipped++;
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        loadCourseData();
        StringBuilder sb = new StringBuilder("Imported ").append(inserted).append(" rows. Skipped ").append(skipped).append(".");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, errors.size());i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size() - 10).append(" more");
        }
        showInfoAlert("Course Import", sb.toString());
    }

    // Updated to include department & programme
    public void addNewCourse(String courseCode, String courseName, double credits, String department, String programme) {
        try {
            databaseHelper.addCourse(courseCode, courseName, credits, department, programme);
            Course newCourse = new Course(courseCode, courseName, "", "", credits, programme, department);
            courseList.add(newCourse);
            showInfoAlert("Success", "Course added successfully.");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showErrorAlert("Course Error", "A course with this code already exists for the selected programme.");
            } else {
                showErrorAlert("Database Error", "Failed to add course: " + e.getMessage());
            }
        }
    }

    private void loadCourseData() {
        try {
            var courseData = databaseHelper.getAllCourses();
            courseList.clear();
            for (var course : courseData) {
                courseList.add(new Course(
                    course.getCourseCode(),
                    course.getCourseName(),
                    "", // instructor
                    "", // academic year
                    course.getCredits(),
                    course.getProgramme(),
                    course.getDepartment()
                ));
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
