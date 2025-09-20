package org.example.co_po_assessment;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty; // added
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
    private String academicYear;
    private String programme;
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

    // New context setter
    public void setContext(String courseId, String programme, String academicYear) {
        this.courseId = courseId;
        this.academicYear = academicYear;
        this.programme = programme;
        try {
            dbService.ensureAssessmentsExist(courseId, programme, academicYear);
            loadAllMarks();
        } catch (SQLException e) {
            showError("Error Initializing", e.getMessage());
        }
    }

    // Backward compatibility (if only courseId provided, fall back to latest year assumption via existing hard-coded method)
    public void setCourseId(String courseId) {
        setContext(courseId, programme, getCurrentAcademicYear());
    }

    private String getCurrentAcademicYear() {
        return academicYear != null ? academicYear : "2025-2026";
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
        if (academicYear == null) academicYear = getCurrentAcademicYear();
        List<DatabaseService.StudentMarksData> marksData = dbService.getStudentQuizMarks(courseId, programme, quizNumber, academicYear);
        if (marksData.isEmpty()) {
            List<DatabaseService.QuestionData> questions = dbService.getQuizQuestions(courseId, programme, quizNumber, academicYear);
            if (!questions.isEmpty()) {
                setupEmptyQuestionColumns(tableView, questions);
            }
        } else {
            setupQuestionColumns(tableView, marksData);
        }
    }

    private void loadMidMarks() throws SQLException {
        if (academicYear == null) academicYear = getCurrentAcademicYear();
        List<DatabaseService.StudentMarksData> marksData = dbService.getStudentMidMarks(courseId, programme, academicYear);
        if (marksData.isEmpty()) {
            List<DatabaseService.QuestionData> questions = dbService.getMidQuestions(courseId, programme, academicYear);
            if (!questions.isEmpty()) {
                setupEmptyQuestionColumns(midTableView, questions);
            }
        } else {
            setupQuestionColumns(midTableView, marksData);
        }
    }

    private void loadFinalMarks() throws SQLException {
        if (academicYear == null) academicYear = getCurrentAcademicYear();
        List<DatabaseService.StudentMarksData> marksData = dbService.getStudentFinalMarks(courseId, programme, academicYear);
        if (marksData.isEmpty()) {
            List<DatabaseService.QuestionData> questions = dbService.getFinalQuestions(courseId, programme, academicYear);
            if (!questions.isEmpty()) {
                setupEmptyQuestionColumns(finalTableView, questions);
            }
        } else {
            setupQuestionColumns(finalTableView, marksData);
        }
    }

    private void setupEmptyQuestionColumns(TableView<Map<String, Object>> tableView,
                                         List<DatabaseService.QuestionData> questions) {
        // Keep only student ID and name columns; remove any previous question/Total columns
        tableView.getColumns().removeIf(col ->
            !col.getText().equals("Student ID") && !col.getText().equals("Name"));

        // Create columns for each question
        for (DatabaseService.QuestionData question : questions) {
            String columnTitle = String.format("%s (%.1f)", question.title, question.marks);
            TableColumn<Map<String, Object>, Double> col = new TableColumn<>(columnTitle);
            col.setCellValueFactory(data -> {
                Double value = (Double) data.getValue().get(question.title);
                return new SimpleObjectProperty<>(value); // keep nulls as null (blank cell)
            });
            setupEditableColumn(col, question.id, question.marks);
            tableView.getColumns().add(col);

            // Store max marks in the row data for validation
            tableView.getItems().forEach(row -> row.put("max_" + question.title, question.marks));
        }

        // Load enrolled students with empty marks
        try {
            List<DatabaseService.StudentData> students = dbService.getEnrolledStudents(courseId, programme, getCurrentAcademicYear());
            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();

            for (DatabaseService.StudentData student : students) {
                Map<String, Object> row = new HashMap<>();
                row.put("studentId", student.id);
                row.put("studentName", student.name);
                for (DatabaseService.QuestionData question : questions) {
                    row.put(question.title, null); // do NOT default to 0.0
                    row.put("qid_" + question.title, question.id);
                    row.put("max_" + question.title, question.marks);
                }
                data.add(row);
            }

            tableView.setItems(data);
        } catch (SQLException e) {
            showError("Error Loading Students", e.getMessage());
        }

        // Add total column after questions
        double totalMax = questions.stream().mapToDouble(q -> q.marks).sum();
        addTotalColumn(tableView, totalMax, questions.stream().map(q -> q.title).toList());
    }

    private void setupQuestionColumns(TableView<Map<String, Object>> tableView,
                                    List<DatabaseService.StudentMarksData> marksData) {
        // Keep only student ID and name columns; remove any previous question/Total columns
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
                Double value = (Double) data.getValue().get(title); // may be null if ungraded
                return new SimpleObjectProperty<>(value);
            });
            col.setPrefWidth(80);
            setupEditableColumn(col, -1, maxMark);
            tableView.getColumns().add(col);
        }

        // Set the table data
        tableView.setItems(FXCollections.observableArrayList(studentRows.values()));

        // Add total column
        double totalMax = maxMarks.values().stream().mapToDouble(Double::doubleValue).sum();
        addTotalColumn(tableView, totalMax, maxMarks.keySet());
    }

    private void addTotalColumn(TableView<Map<String, Object>> tableView, double totalMaxMarks, Collection<String> questionTitles) {
        // Ensure we don't add duplicate
        boolean exists = tableView.getColumns().stream().anyMatch(c -> c.getText().startsWith("Total"));
        if (exists) return;
        TableColumn<Map<String, Object>, Double> totalCol = new TableColumn<>(String.format("Total (%.1f)", totalMaxMarks));
        totalCol.setCellValueFactory(data -> {
            Map<String, Object> row = data.getValue();
            if (row == null) return new SimpleObjectProperty<>(null);
            double sum = 0.0;
            boolean any = false;
            for (String q : questionTitles) {
                Object obj = row.get(q);
                if (obj instanceof Double d) { sum += d; any = true; }
            }
            return any ? new SimpleObjectProperty<>(sum) : new SimpleObjectProperty<>(null);
        });
        totalCol.setEditable(false);
        totalCol.setPrefWidth(100);
        totalCol.setStyle("-fx-font-weight: bold;");
        // Custom cell to show blank when null
        totalCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                if (item == null) { setText(""); }
                else setText(String.format("%.2f", item));
            }
        });
        tableView.getColumns().add(totalCol);
    }

    private void setupEditableColumn(TableColumn<Map<String, Object>, Double> column, int questionId, double maxMarks) {
        column.setCellFactory(tc -> new TableCell<>() {
            private final TextField textField = new TextField();
            {
                textField.focusedProperty().addListener((obs, o, n) -> { if (!n) commitEdit(); });
                textField.setOnAction(e -> commitEdit());
            }
            private void commitEdit() {
                try {
                    double value = Double.parseDouble(textField.getText());
                    Map<String, Object> rowData = getTableRow().getItem();
                    if (rowData == null) return;
                    String questionTitle = column.getText();
                    int parenIndex = questionTitle.indexOf(" (");
                    if (parenIndex >= 0) questionTitle = questionTitle.substring(0, parenIndex);
                    int actualQuestionId = questionId != -1 ? questionId : (Integer) rowData.get("qid_" + questionTitle);
                    if (value < 0 || value > maxMarks) { showError("Invalid Input", String.format("Marks must be between 0 and %.1f", maxMarks)); updateItem(getItem(), false); return; }
                    rowData.put(questionTitle, value);
                    hasUnsavedChanges = true;
                    try {
                        TableView<Map<String,Object>> tv = column.getTableView();
                        String sid = (String) rowData.get("studentId");
                        if (tv == midTableView) {
                            dbService.saveStudentMidMarks(sid, actualQuestionId, value);
                        } else if (tv == finalTableView) {
                            dbService.saveStudentFinalMarks(sid, actualQuestionId, value);
                        } else {
                            dbService.saveStudentQuizMarks(sid, actualQuestionId, value);
                        }
                    } catch (SQLException ex) { showError("Error Saving Mark", ex.getMessage()); }
                    updateItem(value, false);
                    // Refresh table to update Total column
                    column.getTableView().refresh();
                } catch (NumberFormatException ignore) { updateItem(getItem(), false); }
            }
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                if (item == null) { // show blank for ungraded
                    setGraphic(null); setText("");
                    setOnMouseClicked(evt -> { if (evt.getClickCount() == 2) { textField.setText(""); setGraphic(textField); setText(null); textField.requestFocus(); } });
                } else {
                    setText(String.format("%.2f", item));
                    setOnMouseClicked(evt -> { if (evt.getClickCount() == 2) { textField.setText(getText()); setGraphic(textField); setText(null); textField.requestFocus(); } });
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
