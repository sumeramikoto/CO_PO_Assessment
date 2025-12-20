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
import org.example.co_po_assessment.DB_helper.FacultyDatabaseHelper; // added import
import org.example.co_po_assessment.Objects.Faculty;
import org.example.co_po_assessment.utilities.ExcelImportUtils;
import org.example.co_po_assessment.utilities.WindowUtils;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;

public class ManageFacultiesController implements Initializable {
    @FXML
    TableView<Faculty> facultyTableView;
    @FXML
    TableColumn<Faculty, String> idColumn;
    @FXML
    TableColumn<Faculty, String> nameColumn;
    @FXML
    TableColumn<Faculty, String> shortnameColumn;
    @FXML
    TableColumn<Faculty, String> emailColumn;
    @FXML
    Button addFacultyButton;
    @FXML
    Button editFacultyButton;
    @FXML
    Button removeFacultyButton;
    @FXML
    Button backButton;
    @FXML
    private Button excelTemplateButton; // satisfy fx:id
    @FXML
    private Button bulkImportButton; // satisfy fx:id
    @FXML
    private TextField searchField;

    private ObservableList<Faculty> facultyList;
    private FilteredList<Faculty> filteredFaculty;
    private SortedList<Faculty> sortedFaculty;
    private DatabaseService databaseService;
    private FacultyDatabaseHelper facultyDatabaseHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseService = DatabaseService.getInstance();
        facultyDatabaseHelper = new FacultyDatabaseHelper();

        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        shortnameColumn.setCellValueFactory(new PropertyValueFactory<>("shortname"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Initialize faculty list and filtering
        facultyList = FXCollections.observableArrayList();
        filteredFaculty = new FilteredList<>(facultyList, f -> true);
        sortedFaculty = new SortedList<>(filteredFaculty);
        sortedFaculty.comparatorProperty().bind(facultyTableView.comparatorProperty());
        facultyTableView.setItems(sortedFaculty);

        // Hook up search
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> applyFacultyFilter());
        }

        // Load existing faculty data
        loadFacultyData();
    }

    private void applyFacultyFilter() {
        final String query = searchField == null ? null : searchField.getText();
        final String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        filteredFaculty.setPredicate(f -> {
            if (q.isEmpty()) return true;
            String id = Optional.ofNullable(f.getId()).orElse("").toLowerCase(Locale.ROOT);
            String name = Optional.ofNullable(f.getName()).orElse("").toLowerCase(Locale.ROOT);
            return id.contains(q) || name.contains(q);
        });
    }

    public void onAddFacultyButton(ActionEvent event) {
        try {
            // Open the Faculty Info Input window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/facultyInfoInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 345, 420);

            // Get the controller to handle data return
            FacultyInfoInputController controller = fxmlLoader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Faculty Information");
            WindowUtils.setSceneAndMaximize(stage, scene);
            stage.showAndWait(); // Wait for the window to close before continuing

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Add Faculty window: " + e.getMessage());
        }
    }

    public void onEditFacultyButton(ActionEvent event) {
        Faculty selectedFaculty = facultyTableView.getSelectionModel().getSelectedItem();

        if (selectedFaculty == null) {
            showWarningAlert("No Selection", "Please select a faculty member to edit.");
            return;
        }

        try {
            // Open the Faculty Info Input window in edit mode
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/co_po_assessment/facultyInfoInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 345, 420);

            // Get the controller and set it to edit mode
            FacultyInfoInputController controller = fxmlLoader.getController();
            controller.setParentController(this);
            controller.setEditMode(selectedFaculty.getId(), selectedFaculty.getName(), 
                                  selectedFaculty.getShortname(), selectedFaculty.getEmail());

            Stage stage = new Stage();
            stage.setTitle("Edit Faculty Information");
            WindowUtils.setSceneAndMaximize(stage, scene);
            stage.showAndWait();

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Edit Faculty window: " + e.getMessage());
        }
    }

    public void onRemoveFacultyButton(ActionEvent event) {
        Faculty selectedFaculty = facultyTableView.getSelectionModel().getSelectedItem();

        if (selectedFaculty == null) {
            showWarningAlert("No Selection", "Please select a faculty member to remove.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Remove Faculty Member");
        confirmAlert.setContentText("Are you sure you want to remove faculty member: " + selectedFaculty.getName() + "?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Remove from database
                removeFacultyFromDatabase(selectedFaculty);

                // Remove from table
                facultyList.remove(selectedFaculty);

                showInfoAlert("Success", "Faculty member removed successfully.");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Failed to remove faculty member: " + e.getMessage());
            }
        }
    }

    public void onBackButton(ActionEvent event) {
        // Close the current window
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        currentStage.close();
    }

    @FXML private void onExcelTemplateButton() {
        // Let user choose where to save the template
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Faculty Excel Template");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        chooser.setInitialFileName("FacultyTemplate.xlsx");
        File file = chooser.showSaveDialog(((Stage) backButton.getScene().getWindow()));
        if (file == null) return;
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = new File(file.getParentFile(), file.getName() + ".xlsx");
        }

        // Create workbook and sheet
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Faculties");

            // Header row
            String[] headers = {"Faculty ID", "Faculty Name", "Shortname", "Email", "Password"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Freeze header
            sheet.createFreezePane(0, 1);

            // Define a reasonable input range (rows 2..1000)
            int firstDataRow = 1; // zero-based (row 2 visually)
            int lastDataRow = 999; // row 1000 visually

            // Format Faculty ID column as text to preserve leading zeros
            CellStyle textStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            textStyle.setDataFormat(df.getFormat("@"));
            for (int r = firstDataRow; r <= lastDataRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) row = sheet.createRow(r);
                Cell cell = row.createCell(0);
                cell.setCellStyle(textStyle);
            }

            // Auto-size columns after header is set
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Data validation helper
            XSSFDataValidationHelper helper = new XSSFDataValidationHelper((XSSFSheet) sheet);

            // Faculty ID: exactly 9 digits
            String idFormula = "AND(LEN(A2)=9,ISNUMBER(--A2))";
            CellRangeAddressList idRange = new CellRangeAddressList(firstDataRow, lastDataRow, 0, 0);
            DataValidationConstraint idConstraint = helper.createCustomConstraint(idFormula);
            DataValidation idValidation = helper.createValidation(idConstraint, idRange);
            idValidation.setShowErrorBox(true);
            idValidation.createErrorBox("Invalid Faculty ID", "Faculty ID must be exactly 9 digits (e.g., 012345678).");
            ((XSSFDataValidation) idValidation).setSuppressDropDownArrow(true);
            sheet.addValidationData(idValidation);

            // Name: required (length >= 1)
            DataValidationConstraint nameConstraint = helper.createTextLengthConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "1", null);
            CellRangeAddressList nameRange = new CellRangeAddressList(firstDataRow, lastDataRow, 1, 1);
            DataValidation nameValidation = helper.createValidation(nameConstraint, nameRange);
            nameValidation.setShowErrorBox(true);
            nameValidation.createErrorBox("Name Required", "Name cannot be empty.");
            sheet.addValidationData(nameValidation);

            // Shortname: 2-4 uppercase letters A-Z only
            String shortFormula = "AND(LEN(C2)>=2, LEN(C2)<=4, SUMPRODUCT(--(CODE(MID(C2,ROW(INDIRECT(\"1:\"&LEN(C2))),1))<65) + --(CODE(MID(C2,ROW(INDIRECT(\"1:\"&LEN(C2))),1))>90))=0)";
            CellRangeAddressList shortRange = new CellRangeAddressList(firstDataRow, lastDataRow, 2, 2);
            DataValidationConstraint shortConstraint = helper.createCustomConstraint(shortFormula);
            DataValidation shortValidation = helper.createValidation(shortConstraint, shortRange);
            shortValidation.setShowErrorBox(true);
            shortValidation.createErrorBox("Invalid Shortname", "Shortname must be 2 to 4 uppercase letters (A-Z).");
            ((XSSFDataValidation) shortValidation).setSuppressDropDownArrow(true);
            sheet.addValidationData(shortValidation);

            // Email: basic EDU email validation
            String emailFormula = "IFERROR(AND(ISNUMBER(SEARCH(\"@\",D2)), RIGHT(D2,4)=\".edu\", LEN(LEFT(D2,SEARCH(\"@\",D2)-1))>0, LEN(MID(D2, SEARCH(\"@\",D2)+1, LEN(D2)-SEARCH(\"@\",D2)-4))>0), FALSE)";
            CellRangeAddressList emailRange = new CellRangeAddressList(firstDataRow, lastDataRow, 3, 3);
            DataValidationConstraint emailConstraint = helper.createCustomConstraint(emailFormula);
            DataValidation emailValidation = helper.createValidation(emailConstraint, emailRange);
            emailValidation.setShowErrorBox(true);
            emailValidation.createErrorBox("Invalid Email", "Email must look like yourname@institution.edu");
            ((XSSFDataValidation) emailValidation).setSuppressDropDownArrow(true);
            sheet.addValidationData(emailValidation);

            // Password: length >= 6
            DataValidationConstraint passConstraint = helper.createTextLengthConstraint(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "6", null);
            CellRangeAddressList passRange = new CellRangeAddressList(firstDataRow, lastDataRow, 4, 4);
            DataValidation passValidation = helper.createValidation(passConstraint, passRange);
            passValidation.setShowErrorBox(true);
            passValidation.createErrorBox("Invalid Password", "Password must be at least 6 characters long.");
            sheet.addValidationData(passValidation);

            // Write file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }

            showInfoAlert("Template Saved", "Faculty Excel template saved to:\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            showErrorAlert("Save Failed", "Could not create/save Excel template: " + ex.getMessage());
        }
    }

    // Bulk import handler
    @FXML
    public void onBulkImportFaculty(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Faculty Excel File");
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
            int rowNum = 1; // header was row 1 visually
            for (Map<String, String> row : rows) {
                rowNum++;
                String id = orFirst(ExcelImportUtils.get(row, "id", "faculty_id"));
                String shortname = ExcelImportUtils.get(row, "shortname", "short_name");
                String fullName = ExcelImportUtils.get(row, "full_name", "name", "faculty_name");
                String email = ExcelImportUtils.get(row, "email");
                String password = ExcelImportUtils.get(row, "password");
                if (id == null || fullName == null || email == null) {
                    errors.add("Row " + rowNum + ": missing required fields (id, full_name/name, email)");
                    skipped++;
                    continue;
                }
                if (password == null) password = generateDefaultPassword(fullName, id);
                try {
                    databaseService.insertFaculty(id, nullToEmpty(shortname), fullName, email, password);
                    inserted++;
                } catch (SQLException ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.toLowerCase().contains("duplicate")) {
                        errors.add("Row " + rowNum + ": duplicate (" + id + ")");
                    } else {
                        errors.add("Row " + rowNum + ": " + ex.getMessage());
                    }
                    skipped++;
                }
            }
        } catch (IOException e) {
            showErrorAlert("Import Failed", "Unable to read Excel file: " + e.getMessage());
            return;
        }
        // Refresh
        loadFacultyData();
        // Summary
        StringBuilder sb = new StringBuilder();
        sb.append("Imported ").append(inserted).append(" rows. Skipped ").append(skipped).append(".");
        if (!errors.isEmpty()) {
            sb.append("\n\nIssues:\n");
            for (int i = 0; i < Math.min(10, errors.size()); i++) sb.append("- ").append(errors.get(i)).append('\n');
            if (errors.size() > 10) sb.append("... and ").append(errors.size() - 10).append(" more");
        }
        showInfoAlert("Faculty Import", sb.toString());
    }

    private String generateDefaultPassword(String fullName, String id) { return (fullName.split("\\s+")[0] + "@" + id).toLowerCase(Locale.ROOT); }
    private String nullToEmpty(String s) { return s == null ? "" : s; }

    /**
     * Method to be called by FacultyInfoInputController when new faculty is added
     */
    public void addNewFaculty(String id, String name, String shortname, String email, String password) {
        try {
            // Add to database (id is now VARCHAR)
            databaseService.insertFaculty(id, shortname, name, email, password);

            // Add to table
            Faculty newFaculty = new Faculty(id, name, shortname, email);
            facultyList.add(newFaculty);

            showInfoAlert("Success", "Faculty member added successfully.");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add faculty member: " + e.getMessage());
        }
    }

    /**
     * Method to be called by FacultyInfoInputController when faculty is updated
     */
    public void updateFaculty(String oldId, String newId, String name, String shortname, String email, String password) {
        try {
            // Update in database
            databaseService.updateFaculty(oldId, newId, shortname, name, email, password);

            // Update in table - find and update the faculty object
            for (int i = 0; i < facultyList.size(); i++) {
                Faculty f = facultyList.get(i);
                if (f.getId().equals(oldId)) {
                    facultyList.set(i, new Faculty(newId, name, shortname, email));
                    break;
                }
            }

            showInfoAlert("Success", "Faculty member updated successfully.");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to update faculty member: " + e.getMessage());
        }
    }

    /**
     * Load existing faculty data from database
     */
    private void loadFacultyData() {
        try {
            // Get all faculty from database using the helper
            var facultyData = facultyDatabaseHelper.getAllFaculty();

            facultyList.clear();
            for (var faculty : facultyData) {
                facultyList.add(new Faculty(
                    String.valueOf(faculty.getId()),
                    faculty.getFullName(),
                    faculty.getShortname(),
                    faculty.getEmail()
                ));
            }
            // Re-apply filter in case query exists
            applyFacultyFilter();

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load faculty data: " + e.getMessage());
        }
    }

    /**
     * Remove faculty from database
     */
    private void removeFacultyFromDatabase(Faculty faculty) throws SQLException {
        facultyDatabaseHelper.removeFaculty(faculty.getId());
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

    private String orFirst(String s) { return s; }
}
