package org.example.co_po_assessment;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

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
    Button removeFacultyButton;
    @FXML
    Button backButton;

    private ObservableList<Faculty> facultyList;
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

        // Initialize faculty list
        facultyList = FXCollections.observableArrayList();
        facultyTableView.setItems(facultyList);

        // Load existing faculty data
        loadFacultyData();
    }

    public void onAddFacultyButton(ActionEvent event) {
        try {
            // Open the Faculty Info Input window
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("facultyInfoInput-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 345, 420);

            // Get the controller to handle data return
            FacultyInfoInputController controller = fxmlLoader.getController();
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Add Faculty Information");
            stage.setScene(scene);
            stage.showAndWait(); // Wait for the window to close before continuing

        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to open Add Faculty window: " + e.getMessage());
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

    /**
     * Method to be called by FacultyInfoInputController when new faculty is added
     */
    public void addNewFaculty(String id, String name, String shortname, String email, String password) {
        try {
            // Add to database
            databaseService.insertFaculty(Integer.parseInt(id), shortname, name, email, password);

            // Add to table
            Faculty newFaculty = new Faculty(id, name, shortname, email);
            facultyList.add(newFaculty);

            showInfoAlert("Success", "Faculty member added successfully.");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to add faculty member: " + e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Invalid Input", "Faculty ID must be a valid number.");
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

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Failed to load faculty data: " + e.getMessage());
        }
    }

    /**
     * Remove faculty from database
     */
    private void removeFacultyFromDatabase(Faculty faculty) throws SQLException {
        facultyDatabaseHelper.removeFaculty(Integer.parseInt(faculty.getId()));
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
}
