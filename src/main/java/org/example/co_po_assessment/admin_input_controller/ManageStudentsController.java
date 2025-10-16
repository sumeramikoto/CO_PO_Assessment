package org.example.co_po_assessment.admin_input_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.DatabaseService;
import org.example.co_po_assessment.Objects.Student;
import org.example.co_po_assessment.DB_helper.StudentDatabaseHelper;
import org.example.co_po_assessment.utilities.ExcelImportUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.example.co_po_assessment.utilities.WindowUtils;

// added imports for Apache POI
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ManageStudentsController implements Initializable {
    @FXML
    TableView<Student> studentTableView;
    @FXML
    TableColumn<Student, String> idColumn;
    @FXML
    TableColumn<Student, String> nameColumn;
    @FXML
    TableColumn<Student, String> batchColumn;
    @FXML
    TableColumn<Student, String> departmentColumn;
    @FXML
    TableColumn<Student, String> programmeColumn;
    @FXML
    TableColumn<Student, String> emailColumn;
    @FXML
    Button addStudentButton;
    @FXML
    Button removeStudentButton;
    @FXML
    Button backButton;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> batchFilter;

    private ObservableList<Student> studentList;
    private FilteredList<Student> filteredStudents;
    private SortedList<Student> sortedStudents;
    private StudentDatabaseHelper studentDatabaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentDatabaseHelper = new StudentDatabaseHelper();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        batchColumn.setCellValueFactory(new PropertyValueFactory<>("batch"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Initialize student list and filtering
        studentList = FXCollections.observableArrayList();
        filteredStudents = new FilteredList<>(studentList, s -> true);
        sortedStudents = new SortedList<>(filteredStudents);
        sortedStudents.comparatorProperty().bind(studentTableView.comparatorProperty());
        studentTableView.setItems(sortedStudents);

        // Wire filters
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> applyStudentFilter());
        }
        if (batchFilter != null) {
            batchFilter.valueProperty().addListener((obs, old, val) -> applyStudentFilter());
        }

        // Load existing student data
        loadStudentData();
    }

    private void applyStudentFilter() {
        final String q = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        final String batchSel = batchFilter == null ? null : batchFilter.getValue();
        final boolean filterByBatch = batchSel != null && !batchSel.isBlank() && !batchSel.equalsIgnoreCase("All Batches");
        filteredStudents.setPredicate(s -> {
            // batch filter
            if (filterByBatch && (s.getBatch() == null || !s.getBatch().equals(batchSel))) return false;
            if (q.isEmpty()) return true;
            String id = Optional.ofNullable(s.getId()).orElse("").toLowerCase(Locale.ROOT);
            String name = Optional.ofNullable(s.getName()).orElse("").toLowerCase(Locale.ROOT);
            return id.contains(q) || name.contains(q);
        });
    }

    private void refreshBatchFilterOptions() {
        if (batchFilter == null) return;
        // Collect unique batches from current studentList
        Set<String> batches = studentList.stream()
                .map(Student::getBatch)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        List<String> items = new ArrayList<>();
        items.add("All Batches");
        items.addAll(batches);
        batchFilter.setItems(FXCollections.observableArrayList(items));
        if (batchFilter.getValue() == null || !items.contains(batchFilter.getValue())) {
            batchFilter.setValue("All Batches");
        }
    }

    public void onAddStudentButton(ActionEvent actionEvent) {
        try {
            // Open the Student Info Input window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/studentInfoInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 450);

            // Get the controller to handle data return
            StudentInfoInputController controller = fxmlLoader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Student Information");
            WindowUtils.setSceneAndMaximize(stage, scene);
            stage.showAndWait(); // Wait for the window to close before continuing

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Add Student window: " + e.getMessage());
        }
    }

    @FXML private void onExcelTemplateButton() {
        // Create an Excel workbook with headers and validations, then prompt user to save
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row header = sheet.createRow(0);
            String[] headers = new String[]{
                "Student ID", "Name", "Batch", "Email", "Department", "Programme"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Set reasonable column widths
            for (int i = 0; i < headers.length; i++) {
                int width;
                switch (i) {
                    case 0: width = 15; break; // ID
                    case 1: width = 25; break; // Name
                    case 2: width = 10; break; // Batch
                    case 3: width = 30; break; // Email
                    case 4: width = 12; break; // Department
                    case 5: width = 25; break; // Programme
                    default: width = 20;
                }
                sheet.setColumnWidth(i, width * 256);
            }

            // Data validations for rows starting at row index 1 (Excel row 2)
            int firstDataRowIndex = 1; // zero-based index (row 2 visually)
            int lastDataRowIndex = 1000; // generous limit
            DataValidationHelper dvHelper = sheet.getDataValidationHelper();

            // Student ID: 9-digit number (100000000..999999999)
            CellRangeAddressList idRange = new CellRangeAddressList(firstDataRowIndex, lastDataRowIndex, 0, 0);
            DataValidationConstraint idConstraint = dvHelper.createIntegerConstraint(OperatorType.BETWEEN, "100000000", "999999999");
            DataValidation idValidation = dvHelper.createValidation(idConstraint, idRange);
            idValidation.setShowErrorBox(true);
            idValidation.createErrorBox("Invalid Student ID", "Student ID must be a 9-digit number (e.g., 220042101).");
            sheet.addValidationData(idValidation);

            // Name: required (length >= 1)
            CellRangeAddressList nameRange = new CellRangeAddressList(firstDataRowIndex, lastDataRowIndex, 1, 1);
            DataValidationConstraint nameConstraint = dvHelper.createTextLengthConstraint(OperatorType.GREATER_OR_EQUAL, "1", null);
            DataValidation nameValidation = dvHelper.createValidation(nameConstraint, nameRange);
            nameValidation.setShowErrorBox(true);
            nameValidation.createErrorBox("Invalid Name", "Name is required.");
            sheet.addValidationData(nameValidation);

            // Batch: exactly 2 digits -> integer between 10 and 99
            CellRangeAddressList batchRange = new CellRangeAddressList(firstDataRowIndex, lastDataRowIndex, 2, 2);
            DataValidationConstraint batchConstraint = dvHelper.createIntegerConstraint(OperatorType.BETWEEN, "10", "99");
            DataValidation batchValidation = dvHelper.createValidation(batchConstraint, batchRange);
            batchValidation.setShowErrorBox(true);
            batchValidation.createErrorBox("Invalid Batch", "Batch must be a 2-digit number (10-99), e.g., 22.");
            sheet.addValidationData(batchValidation);

            // Email: contains '@' and ends with .edu (relative to top-left cell, D2)
            String emailFormula = "AND(ISNUMBER(SEARCH(\"@\",D2)),RIGHT(D2,4)=\".edu\")";
            CellRangeAddressList emailRange = new CellRangeAddressList(firstDataRowIndex, lastDataRowIndex, 3, 3);
            DataValidationConstraint emailConstraint = dvHelper.createCustomConstraint(emailFormula);
            DataValidation emailValidation = dvHelper.createValidation(emailConstraint, emailRange);
            emailValidation.setShowErrorBox(true);
            emailValidation.createErrorBox("Invalid Email", "Email must be like yourname@institution.edu and end with .edu");
            sheet.addValidationData(emailValidation);

            // Department: exactly 3 uppercase letters (relative to E2)

            String deptFormula =
                    "AND(" +
                            "LEN(E2)=3," +
                            "CODE(LEFT(E2,1))>=65, CODE(LEFT(E2,1))<=90," +
                            "CODE(MID(E2,2,1))>=65, CODE(MID(E2,2,1))<=90," +
                            "CODE(MID(E2,3,1))>=65, CODE(MID(E2,3,1))<=90" +
                            ")";
            CellRangeAddressList deptRange = new CellRangeAddressList(firstDataRowIndex, lastDataRowIndex, 4, 4);
            DataValidationConstraint deptConstraint = dvHelper.createCustomConstraint(deptFormula);
            DataValidation deptValidation = dvHelper.createValidation(deptConstraint, deptRange);
            deptValidation.setShowErrorBox(true);
            deptValidation.createErrorBox("Invalid Department", "Department must be 3 uppercase letters, e.g., CSE");
            sheet.addValidationData(deptValidation);

            // Programme: (BSc|MSc|PhD) in [A-Z]{2,3} (relative to F2)
            String programmeFormula =
                    "OR(" +
                            // BSc in XX or XXX
                            "AND(LEFT(F2,7)=\"BSc in \", OR(LEN(F2)=9, LEN(F2)=10)," +
                            "CODE(MID(F2,8,1))>=65, CODE(MID(F2,8,1))<=90," +
                            "CODE(MID(F2,9,1))>=65, CODE(MID(F2,9,1))<=90," +
                            "IF(LEN(F2)=10, AND(CODE(MID(F2,10,1))>=65, CODE(MID(F2,10,1))<=90), TRUE)" +
                            ")," +
                            // MSc in XX or XXX
                            "AND(LEFT(F2,7)=\"MSc in \", OR(LEN(F2)=9, LEN(F2)=10)," +
                            "CODE(MID(F2,8,1))>=65, CODE(MID(F2,8,1))<=90," +
                            "CODE(MID(F2,9,1))>=65, CODE(MID(F2,9,1))<=90," +
                            "IF(LEN(F2)=10, AND(CODE(MID(F2,10,1))>=65, CODE(MID(F2,10,1))<=90), TRUE)" +
                            ")," +
                            // PhD in XX or XXX
                            "AND(LEFT(F2,7)=\"PhD in \", OR(LEN(F2)=9, LEN(F2)=10)," +
                            "CODE(MID(F2,8,1))>=65, CODE(MID(F2,8,1))<=90," +
                            "CODE(MID(F2,9,1))>=65, CODE(MID(F2,9,1))<=90," +
                            "IF(LEN(F2)=10, AND(CODE(MID(F2,10,1))>=65, CODE(MID(F2,10,1))<=90), TRUE)" +
                            ")" +
                            ")";
            CellRangeAddressList programmeRange = new CellRangeAddressList(firstDataRowIndex, lastDataRowIndex, 5, 5);
            DataValidationConstraint programmeConstraint = dvHelper.createCustomConstraint(programmeFormula);
            DataValidation programmeValidation = dvHelper.createValidation(programmeConstraint, programmeRange);
            programmeValidation.setShowErrorBox(true);
            programmeValidation.createErrorBox("Invalid Programme", "Programme must be like 'BSc in XX/XXX', 'MSc in XX/XXX', or 'PhD in XX/XXX'");
            sheet.addValidationData(programmeValidation);

            // Freeze header row
            sheet.createFreezePane(0, 1);

            // Prompt user to save the file
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Students Excel Template");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
            chooser.setInitialFileName("StudentsTemplate.xlsx");
            File saveTo = chooser.showSaveDialog(((Stage) backButton.getScene().getWindow()));
            if (saveTo == null) return;
            try (FileOutputStream fos = new FileOutputStream(saveTo)) {
                workbook.write(fos);
            }

            showInfoAlert("Template Generated", "Excel template saved to: " + saveTo.getAbsolutePath());
        } catch (IOException ex) {
            showErrorAlert("Template Error", "Failed to generate Excel template: " + ex.getMessage());
        } catch (Exception ex) {
            showErrorAlert("Template Error", "Unexpected error: " + ex.getMessage());
        }
    }

    @FXML
    public void onBulkImportStudents(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Students Excel File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = chooser.showOpenDialog(((Stage) backButton.getScene().getWindow()));
        if (file == null) return;
        DatabaseService databaseService = DatabaseService.getInstance();
        List<String> errors = new ArrayList<>();
        int inserted = 0, skipped = 0;
        try {
            List<Map<String, String>> rows = ExcelImportUtils.readSheetAsMaps(file);
            int rowNum = 1;
            for (Map<String, String> row : rows) {
                rowNum++;
                String id = ExcelImportUtils.get(row, "id", "student_id");
                String name = ExcelImportUtils.get(row, "name", "full_name");
                String batchStr = ExcelImportUtils.get(row, "batch", "year");
                String email = ExcelImportUtils.get(row, "email");
                String dept = ExcelImportUtils.get(row, "department", "dept");
                String prog = ExcelImportUtils.get(row, "programme", "program");
                if (id == null || name == null || batchStr == null || dept == null || prog == null) {
                    errors.add("Row " + rowNum + ": missing required fields (id, name, batch, department, programme)");
                    skipped++; continue;
                }
                int batch;
                try { batch = Integer.parseInt(batchStr); } catch (NumberFormatException nfe) {
                    errors.add("Row " + rowNum + ": invalid batch '" + batchStr + "'");
                    skipped++; continue;
                }
                try {
                    databaseService.insertStudent(id, batch, name, email == null ? "" : email, dept, prog);
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("duplicate")) errors.add("Row " + rowNum + ": duplicate (" + id + ")");
                    else errors.add("Row " + rowNum + ": " + msg);
                    skipped++;
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        loadStudentData();
        StringBuilder sb = new StringBuilder("Imported ").append(inserted).append(" rows. Skipped ").append(skipped).append(".");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i=0;i<Math.min(10, errors.size());i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size() - 10).append(" more");
        }
        showInfoAlert("Student Import", sb.toString());
    }

    public void onRemoveStudentButton(ActionEvent actionEvent) {
        Student selectedStudent = studentTableView.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showWarningAlert("No Selection", "Please select a student to remove.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Remove Student");
        confirmAlert.setContentText("Are you sure you want to remove student: " + selectedStudent.getName() + " (ID: " + selectedStudent.getId() + ")?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Remove from database
                studentDatabaseHelper.removeStudent(selectedStudent.getId());

                // Remove from table
                studentList.remove(selectedStudent);

                // Update filters
                refreshBatchFilterOptions();
                applyStudentFilter();

                showInfoAlert("Success", "Student removed successfully.");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove student: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent actionEvent) {
        // Close the current window
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    /**
     * Load student data from database
     */
    private void loadStudentData() {
        try {
            // Get all students from database
            var studentData = studentDatabaseHelper.getAllStudents();

            studentList.clear();
            for (var student : studentData) {
                studentList.add(new Student(
                    student.getId(),
                    student.getName(),
                    String.valueOf(student.getBatch()),
                    student.getDepartment(),
                    student.getProgramme(),
                    student.getEmail()
                ));
            }
            refreshBatchFilterOptions();
            applyStudentFilter();

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load student data: " + e.getMessage());
        }
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

    /**
     * Method to be called by StudentInfoInputController when new student is added
     */
    public void addNewStudent(String id, String name, String batch, String email, String department, String programme) {
        try {
            // Add to database
            DatabaseService databaseService = DatabaseService.getInstance();
            databaseService.insertStudent(id, Integer.parseInt(batch), name, email, department, programme);

            // Add to table
            Student newStudent = new Student(id, name, batch, department, programme, email);
            studentList.add(newStudent);

            // Update filters
            refreshBatchFilterOptions();
            applyStudentFilter();

            showInfoAlert("Success", "Student added successfully.");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add student: " + e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Input", "Batch must be a valid number.");
        }
    }
}
