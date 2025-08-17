package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class QuestionInputController implements Initializable {
    @FXML
    ChoiceBox<String> examChoiceBox;
    @FXML
    Label examLabel;
    @FXML
    Label questionNoLabel;
    @FXML
    Label marksLabel;
    @FXML
    Label coLabel;
    @FXML
    Label poLabel;
    @FXML
    TextField questionNoTextField;
    @FXML
    TextField marksTextField;
    @FXML
    ChoiceBox<String> coChoiceBox;
    @FXML
    ChoiceBox<String> poChoiceBox;
    @FXML
    Button confirmButton;
    @FXML
    Button backButton;

    private final String[] coArray = {"CO1","CO2","CO3","CO4","CO4","CO5","CO6","CO7","CO8","CO8","CO9","CO10",
                            "CO11","CO12","CO13","CO14","CO15","CO16","CO17","CO18","CO19","CO20"};

    private final String[] poArray = {"PO1","PO2","PO3","PO4","PO5","PO6","PO7","PO8","PO9","PO10",
                                "PO11","PO12"};

    private final String[] exams = {"Quiz 1", "Quiz 2", "Quiz 3", "Quiz 4", "Mid", "Final"};

    String examType;
    String questionNo;
    double marks;
    String co;
    String po;

    // Course context - will be set by the calling window
    private String courseId;
    private Manual parentWindow;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        coChoiceBox.getItems().addAll(coArray);
        poChoiceBox.getItems().addAll(poArray);
        examChoiceBox.getItems().addAll(exams);
    }

    public void setCourseContext(String courseId, Manual parentWindow) {
        this.courseId = courseId;
        this.parentWindow = parentWindow;
    }

    public void addQuestion(ActionEvent event) {
        try {
            // Validate inputs
            if (questionNoTextField.getText().trim().isEmpty() ||
                marksTextField.getText().trim().isEmpty() ||
                examChoiceBox.getValue() == null ||
                coChoiceBox.getValue() == null ||
                poChoiceBox.getValue() == null) {

                showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
                return;
            }

            questionNo = questionNoTextField.getText().trim();
            marks = Double.parseDouble(marksTextField.getText().trim());
            co = coChoiceBox.getValue();
            po = poChoiceBox.getValue();
            examType = examChoiceBox.getValue();

            if (marks <= 0) {
                showAlert("Error", "Marks must be greater than 0", Alert.AlertType.ERROR);
                return;
            }

            // Save to database
            DatabaseService dbService = DatabaseService.getInstance();

            switch (examType) {
                case "Quiz 1":
                    dbService.saveQuizQuestion(courseId, 1, questionNo, marks, co, po);
                    break;
                case "Quiz 2":
                    dbService.saveQuizQuestion(courseId, 2, questionNo, marks, co, po);
                    break;
                case "Quiz 3":
                    dbService.saveQuizQuestion(courseId, 3, questionNo, marks, co, po);
                    break;
                case "Quiz 4":
                    dbService.saveQuizQuestion(courseId, 4, questionNo, marks, co, po);
                    break;
                case "Mid":
                    dbService.saveMidQuestion(courseId, questionNo, marks, co, po);
                    break;
                case "Final":
                    dbService.saveFinalQuestion(courseId, questionNo, marks, co, po);
                    break;
            }

            showAlert("Success", "Question added successfully!", Alert.AlertType.INFORMATION);

            // Refresh parent window if available
            if (parentWindow != null) {
                parentWindow.refreshQuestionTables();
            }

            // Close this window
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number for marks", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save question: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void onBackButton(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
