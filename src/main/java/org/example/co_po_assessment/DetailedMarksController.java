package org.example.co_po_assessment;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.*;

public class DetailedMarksController {
    @FXML private TableView<Map<String, Object>> quiz1TableView;
    @FXML private TableView<Map<String, Object>> quiz2TableView;
    @FXML private TableView<Map<String, Object>> quiz3TableView;
    @FXML private TableView<Map<String, Object>> quiz4TableView;
    @FXML private TableView<Map<String, Object>> midTableView;
    @FXML private TableView<Map<String, Object>> finalTableView;

    private String courseId;
    private final DatabaseService dbService = DatabaseService.getInstance();
    private boolean hasUnsavedChanges = false;

    @FXML
    public void initialize() {
        setupBasicColumns(quiz1TableView);
        setupBasicColumns(quiz2TableView);
        setupBasicColumns(quiz3TableView);
        setupBasicColumns(quiz4TableView);
        setupBasicColumns(midTableView);
        setupBasicColumns(finalTableView);
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
        try {
            dbService.ensureAssessmentsExist(courseId, getCurrentAcademicYear());
            loadAllMarks();
        } catch (SQLException e) {
            showError("Error Initializing", e.getMessage());
        }
    }

    private String getCurrentAcademicYear() {
        return "2025-2026";
    }

    private void setupBasicColumns(TableView<Map<String, Object>> tableView) {
        TableColumn<Map<String, Object>, String> studentIdCol = new TableColumn<>("Student ID");
        studentIdCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("studentId")));
        studentIdCol.setPrefWidth(100);

        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("studentName")));
        nameCol.setPrefWidth(200);

        tableView.getColumns().clear();
        tableView.getColumns().add(studentIdCol);
        tableView.getColumns().add(nameCol);
        tableView.setEditable(true);
    }

    private void loadAllMarks() {
        try {
            loadQuizMarks(1, quiz1TableView);
            loadQuizMarks(2, quiz2TableView);
            loadQuizMarks(3, quiz3TableView);
            loadQuizMarks(4, quiz4TableView);
            loadMidMarks();
            loadFinalMarks();
        } catch (SQLException e) {
            showError("Error Loading Marks", e.getMessage());
        }
    }

    private void loadQuizMarks(int quizNumber, TableView<Map<String, Object>> tableView) throws SQLException {
        List<DatabaseService.StudentMarksData> marksData = dbService.getStudentQuizMarks(courseId, quizNumber);
        if (marksData.isEmpty()) {
            List<DatabaseService.QuestionData> questions = dbService.getQuizQuestions(courseId, quizNumber);
            if (!questions.isEmpty()) {
                setupEmptyQuestionColumns(tableView, questions);
            }
        } else {
            setupQuestionColumns(tableView, marksData);
        }
    }

    private void loadMidMarks() throws SQLException {
        List<DatabaseService.StudentMarksData> marksData = dbService.getStudentMidMarks(courseId);
        if (marksData.isEmpty()) {
            List<DatabaseService.QuestionData> questions = dbService.getMidQuestions(courseId);
            if (!questions.isEmpty()) {
                setupEmptyQuestionColumns(midTableView, questions);
            }
        } else {
            setupQuestionColumns(midTableView, marksData);
        }
    }

    private void loadFinalMarks() throws SQLException {
        List<DatabaseService.StudentMarksData> marksData = dbService.getStudentFinalMarks(courseId);
        if (marksData.isEmpty()) {
            List<DatabaseService.QuestionData> questions = dbService.getFinalQuestions(courseId);
            if (!questions.isEmpty()) {
                setupEmptyQuestionColumns(finalTableView, questions);
            }
        } else {
            setupQuestionColumns(finalTableView, marksData);
        }
    }

    private void setupEmptyQuestionColumns(TableView<Map<String, Object>> tableView,
                                         List<DatabaseService.QuestionData> questions) {
        // Keep existing student ID and name columns
        tableView.getColumns().removeIf(col ->
            !col.getText().equals("Student ID") && !col.getText().equals("Name"));

        // Create columns for each question
        for (DatabaseService.QuestionData question : questions) {
            String columnTitle = String.format("%s (%.1f)", question.title, question.marks);
            TableColumn<Map<String, Object>, Double> col = new TableColumn<>(columnTitle);
            col.setCellValueFactory(data -> {
                Double value = (Double) data.getValue().get(question.title);
                return new SimpleDoubleProperty(value != null ? value : 0.0).asObject();
            });
            setupEditableColumn(col, question.id, question.marks);
            tableView.getColumns().add(col);

            // Store max marks in the row data for validation
            tableView.getItems().forEach(row -> row.put("max_" + question.title, question.marks));
        }

        // Load enrolled students with empty marks
        try {
            List<DatabaseService.StudentData> students = dbService.getEnrolledStudents(courseId);
            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();

            for (DatabaseService.StudentData student : students) {
                Map<String, Object> row = new HashMap<>();
                row.put("studentId", student.id);
                row.put("studentName", student.name);
                for (DatabaseService.QuestionData question : questions) {
                    row.put(question.title, 0.0);
                    row.put("qid_" + question.title, question.id);
                    row.put("max_" + question.title, question.marks);
                }
                data.add(row);
            }

            tableView.setItems(data);
        } catch (SQLException e) {
            showError("Error Loading Students", e.getMessage());
        }
    }

    private void setupQuestionColumns(TableView<Map<String, Object>> tableView,
                                    List<DatabaseService.StudentMarksData> marksData) {
        // Keep existing student ID and name columns
        tableView.getColumns().removeIf(col ->
            !col.getText().equals("Student ID") && !col.getText().equals("Name"));

        // Get unique questions and their max marks
        Map<String, Double> maxMarks = new HashMap<>();
        marksData.forEach(mark -> maxMarks.put(mark.questionTitle, mark.maxMarks));

        // Group marks by student
        Map<String, Map<String, Object>> studentRows = new HashMap<>();

        for (DatabaseService.StudentMarksData mark : marksData) {
            studentRows.computeIfAbsent(mark.studentId, k -> {
                Map<String, Object> row = new HashMap<>();
                row.put("studentId", mark.studentId);
                row.put("studentName", mark.studentName);
                return row;
            });

            Map<String, Object> row = studentRows.get(mark.studentId);
            row.put(mark.questionTitle, mark.marksObtained);
            row.put("qid_" + mark.questionTitle, mark.questionId);
            row.put("max_" + mark.questionTitle, mark.maxMarks);
        }

        // Create columns for questions
        for (String title : new TreeSet<>(maxMarks.keySet())) {
            double maxMark = maxMarks.get(title);
            String columnTitle = String.format("%s (%.1f)", title, maxMark);
            TableColumn<Map<String, Object>, Double> col = new TableColumn<>(columnTitle);
            col.setCellValueFactory(data -> {
                Double value = (Double) data.getValue().get(title);
                return new SimpleDoubleProperty(value != null ? value : 0.0).asObject();
            });
            col.setPrefWidth(80);
            setupEditableColumn(col, -1, maxMark); // questionId will be retrieved from the row data
            tableView.getColumns().add(col);
        }

        // Set the table data
        tableView.setItems(FXCollections.observableArrayList(studentRows.values()));
    }

    private void setupEditableColumn(TableColumn<Map<String, Object>, Double> column, int questionId, double maxMarks) {
        column.setCellFactory(tc -> new TableCell<>() {
            private final TextField textField = new TextField();

            {
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        commitEdit();
                    }
                });
                textField.setOnAction(event -> commitEdit());
            }

            private void commitEdit() {
                try {
                    double value = Double.parseDouble(textField.getText());
                    Map<String, Object> rowData = getTableRow().getItem();

                    if (rowData != null) {
                        String questionTitle = column.getText().split(" \\(")[0]; // Remove the marks part from title

                        // Get the actual question ID and validate the input value
                        int actualQuestionId = questionId != -1 ? questionId :
                            ((Integer) rowData.get("qid_" + questionTitle));

                        if (value < 0 || value > maxMarks) {
                            showError("Invalid Input",
                                String.format("Marks must be between 0 and %.1f", maxMarks));
                            updateItem(getItem(), false);
                            return;
                        }

                        rowData.put(questionTitle, value);
                        hasUnsavedChanges = true;

                        try {
                            dbService.saveStudentQuizMarks(
                                (String) rowData.get("studentId"),
                                actualQuestionId,
                                value
                            );
                        } catch (SQLException e) {
                            showError("Error Saving Mark", e.getMessage());
                        }

                        updateItem(value, false);
                    }
                } catch (NumberFormatException e) {
                    updateItem(getItem(), false);
                }
            }

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            textField.setText(getText());
                            setGraphic(textField);
                            setText(null);
                            textField.requestFocus();
                        }
                    });
                }
            }
        });
    }

    @FXML
    private void onCloseButton() {
        if (hasUnsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes");
            alert.setContentText("Would you like to save before closing?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.NO || response == ButtonType.YES) {
                    closeWindow();
                }
            });
        } else {
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) quiz1TableView.getScene().getWindow();
        stage.close();
    }

    private void showError(String header, String content) {
        showAlert(Alert.AlertType.ERROR, header, content);
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Information");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onSaveButton() {
        try {
            // Save marks for all assessment types
            saveTableMarks(quiz1TableView, 1);
            saveTableMarks(quiz2TableView, 2);
            saveTableMarks(quiz3TableView, 3);
            saveTableMarks(quiz4TableView, 4);
            saveMidMarks(midTableView);
            saveFinalMarks(finalTableView);

            hasUnsavedChanges = false;
            showAlert(Alert.AlertType.INFORMATION, "Success", "All marks have been saved successfully.");
        } catch (SQLException e) {
            showError("Error Saving Marks", e.getMessage());
        }
    }

    private void saveTableMarks(TableView<Map<String, Object>> tableView, int quizNumber) throws SQLException {
        for (Map<String, Object> row : tableView.getItems()) {
            String studentId = (String) row.get("studentId");
            for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
                String columnTitle = column.getText();
                if (!columnTitle.equals("Student ID") && !columnTitle.equals("Name")) {
                    Integer questionId = (Integer) row.get("qid_" + columnTitle);
                    if (questionId != null) {
                        Double marks = (Double) row.get(columnTitle);
                        if (marks != null) {
                            dbService.saveStudentQuizMarks(studentId, questionId, marks);
                        }
                    }
                }
            }
        }
    }

    private void saveMidMarks(TableView<Map<String, Object>> tableView) throws SQLException {
        for (Map<String, Object> row : tableView.getItems()) {
            String studentId = (String) row.get("studentId");
            for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
                String columnTitle = column.getText();
                if (!columnTitle.equals("Student ID") && !columnTitle.equals("Name")) {
                    Integer questionId = (Integer) row.get("qid_" + columnTitle);
                    if (questionId != null) {
                        Double marks = (Double) row.get(columnTitle);
                        if (marks != null) {
                            dbService.saveStudentMidMarks(studentId, questionId, marks);
                        }
                    }
                }
            }
        }
    }

    private void saveFinalMarks(TableView<Map<String, Object>> tableView) throws SQLException {
        for (Map<String, Object> row : tableView.getItems()) {
            String studentId = (String) row.get("studentId");
            for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
                String columnTitle = column.getText();
                if (!columnTitle.equals("Student ID") && !columnTitle.equals("Name")) {
                    Integer questionId = (Integer) row.get("qid_" + columnTitle);
                    if (questionId != null) {
                        Double marks = (Double) row.get(columnTitle);
                        if (marks != null) {
                            dbService.saveStudentFinalMarks(studentId, questionId, marks);
                        }
                    }
                }
            }
        }
    }
}
