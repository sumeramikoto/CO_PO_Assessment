package org.example.co_po_assessment.faculty_input_controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import org.example.co_po_assessment.DB_helper.DatabaseService;

import java.net.URL;
import java.util.*;

public class MarksEntryController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label courseInfoLabel;
    @FXML private Label statusLabel;
    @FXML private Label instructionLabel;
    @FXML private ChoiceBox<String> assessmentTypeChoice;
    @FXML private Button loadMarksButton;
    @FXML private TableView<StudentMarksRow> marksTableView;
    @FXML private TableColumn<StudentMarksRow, String> studentIdCol;
    @FXML private TableColumn<StudentMarksRow, String> studentNameCol;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    private DatabaseService.FacultyCourseAssignment courseAssignment;
    private final DatabaseService db = DatabaseService.getInstance();
    private ObservableList<StudentMarksRow> studentMarksData = FXCollections.observableArrayList();
    private List<DatabaseService.QuestionData> currentQuestions = new ArrayList<>();
    private String currentAssessmentType = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupAssessmentChoice();
        setupTable();
    }

    public void setCourseAssignment(DatabaseService.FacultyCourseAssignment assignment) {
        this.courseAssignment = assignment;
        if (assignment != null) {
            courseInfoLabel.setText(assignment.courseCode + " - " + assignment.courseName + 
                                   " (" + assignment.programme + ", " + assignment.academicYear + ")");
        }
    }

    private void setupAssessmentChoice() {
        assessmentTypeChoice.getItems().addAll("Quiz 1", "Quiz 2", "Quiz 3", "Quiz 4", "Mid", "Final");
        assessmentTypeChoice.setValue("Quiz 1");
    }

    private void setupTable() {
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        marksTableView.setItems(studentMarksData);
        marksTableView.setEditable(true);
    }

    @FXML
    private void onLoadMarks(ActionEvent event) {
        if (courseAssignment == null) {
            showAlert(Alert.AlertType.WARNING, "No course selected");
            return;
        }

        String selected = assessmentTypeChoice.getValue();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an assessment type");
            return;
        }

        currentAssessmentType = selected;
        loadAssessmentData();
    }

    private void loadAssessmentData() {
        try {
            // Clear existing dynamic columns
            marksTableView.getColumns().removeIf(col -> 
                !col.equals(studentIdCol) && !col.equals(studentNameCol));
            
            studentMarksData.clear();
            currentQuestions.clear();

            // Load questions for this assessment
            currentQuestions = loadQuestions(currentAssessmentType);
            
            if (currentQuestions.isEmpty()) {
                statusLabel.setText("No questions found for " + currentAssessmentType + ". Please add questions first.");
                saveButton.setDisable(true);
                return;
            }

            // Load enrolled students
            List<DatabaseService.StudentData> students = db.getEnrolledStudents(
                courseAssignment.courseCode, 
                courseAssignment.programme, 
                courseAssignment.academicYear
            );

            if (students.isEmpty()) {
                statusLabel.setText("No students enrolled in this course.");
                saveButton.setDisable(true);
                return;
            }

            // Create dynamic columns for each question
            for (DatabaseService.QuestionData question : currentQuestions) {
                TableColumn<StudentMarksRow, Double> col = new TableColumn<>(
                    question.title + "\n(Max: " + question.marks + ")"
                );
                col.setMinWidth(100);
                col.setCellValueFactory(cellData -> {
                    Double mark = cellData.getValue().getQuestionMark(question.id);
                    return new SimpleDoubleProperty(mark == null ? 0.0 : mark).asObject();
                });
                
                // Make column editable
                col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
                col.setOnEditCommit(event -> {
                    StudentMarksRow row = event.getRowValue();
                    Double newValue = event.getNewValue();
                    if (newValue != null) {
                        // Validate mark doesn't exceed maximum
                        if (newValue > question.marks) {
                            showAlert(Alert.AlertType.WARNING, 
                                "Marks cannot exceed maximum (" + question.marks + ")");
                            marksTableView.refresh();
                            return;
                        }
                        if (newValue < 0) {
                            showAlert(Alert.AlertType.WARNING, "Marks cannot be negative");
                            marksTableView.refresh();
                            return;
                        }
                        row.setQuestionMark(question.id, newValue);
                    }
                });
                
                marksTableView.getColumns().add(col);
            }

            // Load existing marks and create rows
            for (DatabaseService.StudentData student : students) {
                StudentMarksRow row = new StudentMarksRow(student.id, student.name);
                
                // Load existing marks for this student
                Map<Integer, Double> existingMarks = loadStudentMarks(student.id, currentAssessmentType);
                for (Integer questionId : existingMarks.keySet()) {
                    row.setQuestionMark(questionId, existingMarks.get(questionId));
                }
                
                studentMarksData.add(row);
            }

            statusLabel.setText("Loaded " + students.size() + " students and " + 
                              currentQuestions.size() + " questions for " + currentAssessmentType);
            saveButton.setDisable(false);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load marks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<DatabaseService.QuestionData> loadQuestions(String assessmentType) throws Exception {
        String code = courseAssignment.courseCode;
        String programme = courseAssignment.programme;
        String year = courseAssignment.academicYear;
        
        return switch (assessmentType) {
            case "Quiz 1" -> db.getQuizQuestions(code, programme, 1, year);
            case "Quiz 2" -> db.getQuizQuestions(code, programme, 2, year);
            case "Quiz 3" -> db.getQuizQuestions(code, programme, 3, year);
            case "Quiz 4" -> db.getQuizQuestions(code, programme, 4, year);
            case "Mid" -> db.getMidQuestions(code, programme, year);
            case "Final" -> db.getFinalQuestions(code, programme, year);
            default -> new ArrayList<>();
        };
    }

    private Map<Integer, Double> loadStudentMarks(String studentId, String assessmentType) {
        try {
            return db.getStudentMarksForAssessment(studentId, currentQuestions, assessmentType);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @FXML
    private void onSaveMarks(ActionEvent event) {
        if (studentMarksData.isEmpty() || currentQuestions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No data to save");
            return;
        }

        try {
            int savedCount = 0;
            for (StudentMarksRow row : studentMarksData) {
                for (DatabaseService.QuestionData question : currentQuestions) {
                    Double mark = row.getQuestionMark(question.id);
                    if (mark != null) {
                        db.saveStudentMark(row.getStudentId(), question.id, mark, currentAssessmentType);
                        savedCount++;
                    }
                }
            }

            showAlert(Alert.AlertType.INFORMATION, 
                "Successfully saved marks for " + studentMarksData.size() + " students!");
            statusLabel.setText("Last saved: " + savedCount + " marks entries");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to save marks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node node) {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // Inner class to represent a row in the marks table
    public static class StudentMarksRow {
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty studentName;
        private final Map<Integer, Double> questionMarks = new HashMap<>();

        public StudentMarksRow(String id, String name) {
            this.studentId = new SimpleStringProperty(id);
            this.studentName = new SimpleStringProperty(name);
        }

        public String getStudentId() { return studentId.get(); }
        public String getStudentName() { return studentName.get(); }
        
        public void setQuestionMark(int questionId, Double mark) {
            questionMarks.put(questionId, mark);
        }
        
        public Double getQuestionMark(int questionId) {
            return questionMarks.get(questionId);
        }
    }
}
