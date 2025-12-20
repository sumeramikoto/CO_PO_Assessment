package org.example.co_po_assessment.faculty_input_controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.co_po_assessment.Objects.AssessmentQuestion;
import org.example.co_po_assessment.DB_helper.DatabaseService;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

public class ManageCourseQuestionsController implements Initializable {
    // Quiz 1
    @FXML private TableView<AssessmentQuestion> quiz1TableView;
    @FXML private TableColumn<AssessmentQuestion, String> q1QuesNoCol;
    @FXML private TableColumn<AssessmentQuestion, Double> q1MarksCol;
    @FXML private TableColumn<AssessmentQuestion, String> q1COCol;
    @FXML private TableColumn<AssessmentQuestion, String> q1POCol;

    // Quiz 2
    @FXML private TableView<AssessmentQuestion> quiz2TableView;
    @FXML private TableColumn<AssessmentQuestion, String> q2QuesNoCol;
    @FXML private TableColumn<AssessmentQuestion, Double> q2MarksCol;
    @FXML private TableColumn<AssessmentQuestion, String> q2COCol;
    @FXML private TableColumn<AssessmentQuestion, String> q2POCol;

    // Quiz 3
    @FXML private TableView<AssessmentQuestion> quiz3TableView;
    @FXML private TableColumn<AssessmentQuestion, String> q3QuesNoCol;
    @FXML private TableColumn<AssessmentQuestion, Double> q3MarksCol;
    @FXML private TableColumn<AssessmentQuestion, String> q3COCol;
    @FXML private TableColumn<AssessmentQuestion, String> q3POCol;

    // Quiz 4
    @FXML private TableView<AssessmentQuestion> quiz4TableView;
    @FXML private TableColumn<AssessmentQuestion, String> q4QuesNoCol;
    @FXML private TableColumn<AssessmentQuestion, Double> q4MarksCol;
    @FXML private TableColumn<AssessmentQuestion, String> q4COCol;
    @FXML private TableColumn<AssessmentQuestion, String> q4POCol;

    // Mid
    @FXML private TableView<AssessmentQuestion> midTableView;
    @FXML private TableColumn<AssessmentQuestion, String> midQuesNoCol;
    @FXML private TableColumn<AssessmentQuestion, Double> midMarksCol;
    @FXML private TableColumn<AssessmentQuestion, String> midCOCol;
    @FXML private TableColumn<AssessmentQuestion, String> midPOCol;

    // Final
    @FXML private TableView<AssessmentQuestion> finalTableView;
    @FXML private TableColumn<AssessmentQuestion, String> finalQuesNoCol;
    @FXML private TableColumn<AssessmentQuestion, Double> finalMarksCol;
    @FXML private TableColumn<AssessmentQuestion, String> finalCOCol;
    @FXML private TableColumn<AssessmentQuestion, String> finalPOCol;

    private ObservableList<AssessmentQuestion> quiz1Questions = FXCollections.observableArrayList();
    private ObservableList<AssessmentQuestion> quiz2Questions = FXCollections.observableArrayList();
    private ObservableList<AssessmentQuestion> quiz3Questions = FXCollections.observableArrayList();
    private ObservableList<AssessmentQuestion> quiz4Questions = FXCollections.observableArrayList();
    private ObservableList<AssessmentQuestion> midQuestions = FXCollections.observableArrayList();
    private ObservableList<AssessmentQuestion> finalQuestions = FXCollections.observableArrayList();

    // Context passed from FacultyDashboard
    private DatabaseService.FacultyCourseAssignment courseAssignment;
    private final DatabaseService db = DatabaseService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        bindTables();
    }

    // Called by FacultyDashboard after FXML load
    public void setCourseAssignment(DatabaseService.FacultyCourseAssignment assignment) {
        this.courseAssignment = assignment;
        loadExistingQuestions();
    }

    private void setupColumns() {
        if (q1QuesNoCol != null) q1QuesNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (q1MarksCol != null) q1MarksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        if (q1COCol != null) q1COCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        if (q1POCol != null) q1POCol.setCellValueFactory(new PropertyValueFactory<>("po"));

        if (q2QuesNoCol != null) q2QuesNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (q2MarksCol != null) q2MarksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        if (q2COCol != null) q2COCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        if (q2POCol != null) q2POCol.setCellValueFactory(new PropertyValueFactory<>("po"));

        if (q3QuesNoCol != null) q3QuesNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (q3MarksCol != null) q3MarksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        if (q3COCol != null) q3COCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        if (q3POCol != null) q3POCol.setCellValueFactory(new PropertyValueFactory<>("po"));

        if (q4QuesNoCol != null) q4QuesNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (q4MarksCol != null) q4MarksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        if (q4COCol != null) q4COCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        if (q4POCol != null) q4POCol.setCellValueFactory(new PropertyValueFactory<>("po"));

        if (midQuesNoCol != null) midQuesNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (midMarksCol != null) midMarksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        if (midCOCol != null) midCOCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        if (midPOCol != null) midPOCol.setCellValueFactory(new PropertyValueFactory<>("po"));

        if (finalQuesNoCol != null) finalQuesNoCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        if (finalMarksCol != null) finalMarksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));
        if (finalCOCol != null) finalCOCol.setCellValueFactory(new PropertyValueFactory<>("co"));
        if (finalPOCol != null) finalPOCol.setCellValueFactory(new PropertyValueFactory<>("po"));
    }

    private void bindTables() {
        if (quiz1TableView != null) quiz1TableView.setItems(quiz1Questions);
        if (quiz2TableView != null) quiz2TableView.setItems(quiz2Questions);
        if (quiz3TableView != null) quiz3TableView.setItems(quiz3Questions);
        if (quiz4TableView != null) quiz4TableView.setItems(quiz4Questions);
        if (midTableView != null) midTableView.setItems(midQuestions);
        if (finalTableView != null) finalTableView.setItems(finalQuestions);
    }

    private void loadExistingQuestions() {
        if (courseAssignment == null) return;
        // Ensure assessment shells exist
        try { db.ensureAssessmentsExist(courseAssignment.courseCode, courseAssignment.programme, courseAssignment.academicYear); } catch (Exception ignored) {}

        quiz1Questions.clear(); quiz2Questions.clear(); quiz3Questions.clear(); quiz4Questions.clear(); midQuestions.clear(); finalQuestions.clear();
        try {
            String year = courseAssignment.academicYear;
            for (int i = 1; i <= 4; i++) {
                List<DatabaseService.QuestionData> qd = db.getQuizQuestions(courseAssignment.courseCode, courseAssignment.programme, i, year);
                ObservableList<AssessmentQuestion> target = switch (i) { case 1 -> quiz1Questions; case 2 -> quiz2Questions; case 3 -> quiz3Questions; default -> quiz4Questions; };
                for (DatabaseService.QuestionData d : qd) {
                    target.add(new AssessmentQuestion(d.id, d.title, d.marks, d.co, d.po, "Quiz" + i));
                }
            }
            for (DatabaseService.QuestionData d : db.getMidQuestions(courseAssignment.courseCode, courseAssignment.programme, year)) {
                midQuestions.add(new AssessmentQuestion(d.id, d.title, d.marks, d.co, d.po, "Mid"));
            }
            for (DatabaseService.QuestionData d : db.getFinalQuestions(courseAssignment.courseCode, courseAssignment.programme, year)) {
                finalQuestions.add(new AssessmentQuestion(d.id, d.title, d.marks, d.co, d.po, "Final"));
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed loading questions: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    // Add Question Handlers
    public void onQuiz1AddQuestionButton(ActionEvent actionEvent) { addQuestionDialog("Quiz1"); }
    public void onQuiz2AddQuestionButton(ActionEvent actionEvent) { addQuestionDialog("Quiz2"); }
    public void onQuiz3AddQuestionButton(ActionEvent actionEvent) { addQuestionDialog("Quiz3"); }
    public void onQuiz4AddQuestionButton(ActionEvent actionEvent) { addQuestionDialog("Quiz4"); }
    public void onMidAddQuestionButton(ActionEvent actionEvent) { addQuestionDialog("Mid"); }
    public void onFinalAddQuestionButton(ActionEvent actionEvent) { addQuestionDialog("Final"); }

    // Remove Handlers
    public void onQuiz1RemoveQuestionButton(ActionEvent actionEvent) { removeSelected(quiz1TableView, quiz1Questions); }
    public void onQuiz2RemoveQuestionButton(ActionEvent actionEvent) { removeSelected(quiz2TableView, quiz2Questions); }
    public void onQuiz3RemoveQuestionButton(ActionEvent actionEvent) { removeSelected(quiz3TableView, quiz3Questions); }
    public void onQuiz4RemoveQuestionButton(ActionEvent actionEvent) { removeSelected(quiz4TableView, quiz4Questions); }
    public void onMidRemoveQuestionButton(ActionEvent actionEvent) { removeSelected(midTableView, midQuestions); }
    public void onFinalRemoveQuestionButton(ActionEvent actionEvent) { removeSelected(finalTableView, finalQuestions); }

    // Back buttons
    public void onQ1BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }
    public void onQ2BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }
    public void onQ3BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }
    public void onQ4BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }
    public void onMidBackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }
    public void onFinalBackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }

    private void addQuestionDialog(String assessmentType) {
        Dialog<AssessmentQuestion> dialog = new Dialog<>();
        dialog.setTitle("Add " + assessmentType + " Question");
        dialog.setHeaderText(null);

        ButtonType addBtnType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtnType, ButtonType.CANCEL);

        TextField numberField = new TextField();
        TextField marksField = new TextField();
        ChoiceBox<String> coChoice = new ChoiceBox<>();
        ChoiceBox<String> poChoice = new ChoiceBox<>();

        // Load allowed CO/PO for this course/programme
        List<String> allowedCOs = List.of();
        List<String> allowedPOs = List.of();
        try {
            if (courseAssignment != null) {
                allowedCOs = db.getAllowedCOsForCourse(courseAssignment.courseCode, courseAssignment.programme);
                allowedPOs = db.getAllowedPOsForCourse(courseAssignment.courseCode, courseAssignment.programme);
            }
        } catch (Exception e) {
            Alert warn = new Alert(Alert.AlertType.ERROR, "Could not fetch allowed CO/PO: " + e.getMessage(), ButtonType.OK);
            warn.setHeaderText(null);
            warn.showAndWait();
        }
        coChoice.getItems().setAll(allowedCOs);
        poChoice.getItems().setAll(allowedPOs);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Question No:"),0,0); grid.add(numberField,1,0);
        grid.add(new Label("Marks:"),0,1); grid.add(marksField,1,1);
        grid.add(new Label("CO:"),0,2); grid.add(coChoice,1,2);
        grid.add(new Label("PO:"),0,3); grid.add(poChoice,1,3);
        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addBtnType);
        addButton.setDisable(true);

        Runnable validate = () -> {
            String no = numberField.getText().trim();
            String marksTxt = marksField.getText().trim();
            boolean valid = !no.isEmpty() && coChoice.getValue() != null && poChoice.getValue() != null;
            // Also require that there are allowed CO/PO options
            if (coChoice.getItems().isEmpty() || poChoice.getItems().isEmpty()) valid = false;
            double m = -1;
            try { m = Double.parseDouble(marksTxt); } catch (Exception ignored) {}
            if (m <= 0) valid = false;
            if (valid && questionNumberExists(assessmentType, no)) valid = false; // uniqueness per assessment
            addButton.setDisable(!valid);
        };

        numberField.textProperty().addListener((obs,o,n)-> validate.run());
        marksField.textProperty().addListener((obs,o,n)-> validate.run());
        coChoice.valueProperty().addListener((obs,o,n)-> validate.run());
        poChoice.valueProperty().addListener((obs,o,n)-> validate.run());

        if (coChoice.getItems().isEmpty() || poChoice.getItems().isEmpty()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, "This course has no CO/PO mappings. Please assign COs and POs to the course first.", ButtonType.OK);
            info.setHeaderText(null);
            info.showAndWait();
        }

        dialog.setResultConverter(btn -> {
            if (btn == addBtnType) {
                String no = numberField.getText().trim();
                String marksStr = marksField.getText().trim();
                try {
                    double marks = Double.parseDouble(marksStr);
                    String co = coChoice.getValue();
                    String po = poChoice.getValue();
                    if (no.isEmpty() || co == null || po == null || marks <= 0) return null;
                    if (questionNumberExists(assessmentType, no)) {
                        Alert dup = new Alert(Alert.AlertType.WARNING, "Question number '"+ no + "' already exists in this assessment.", ButtonType.OK);
                        dup.setHeaderText(null);
                        dup.showAndWait();
                        return null;
                    }
                    AssessmentQuestion q = new AssessmentQuestion(no, marks, co, po, assessmentType);
                    if (persistQuestion(q)) {
                        return q; // only return if saved
                    } else {
                        return null; // failed to persist
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(q -> {
            // Add only if persistence succeeded earlier (persistQuestion now returns boolean)
            switch (assessmentType) {
                case "Quiz1" -> quiz1Questions.add(q);
                case "Quiz2" -> quiz2Questions.add(q);
                case "Quiz3" -> quiz3Questions.add(q);
                case "Quiz4" -> quiz4Questions.add(q);
                case "Mid" -> midQuestions.add(q);
                case "Final" -> finalQuestions.add(q);
            }
        });
    }

    private boolean questionNumberExists(String assessmentType, String number) {
        if (number == null) return false;
        String target = number.trim();
        if (target.isEmpty()) return false;
        ObservableList<AssessmentQuestion> list = switch (assessmentType) {
            case "Quiz1" -> quiz1Questions;
            case "Quiz2" -> quiz2Questions;
            case "Quiz3" -> quiz3Questions;
            case "Quiz4" -> quiz4Questions;
            case "Mid" -> midQuestions;
            case "Final" -> finalQuestions;
            default -> null;
        };
        if (list == null) return false;
        for (AssessmentQuestion q : list) {
            if (q.getNumber() != null && q.getNumber().trim().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean persistQuestion(AssessmentQuestion q) {
        if (courseAssignment == null || q == null) return false;
        try {
            String code = courseAssignment.courseCode;
            String year = courseAssignment.academicYear;
            String programme = courseAssignment.programme;
            switch (q.getAssessmentType()) {
                case "Quiz1" -> db.saveQuizQuestion(code, programme, 1,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Quiz2" -> db.saveQuizQuestion(code, programme,2,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Quiz3" -> db.saveQuizQuestion(code, programme, 3,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Quiz4" -> db.saveQuizQuestion(code, programme, 4,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Mid" -> db.saveMidQuestion(code, programme, q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Final" -> db.saveFinalQuestion(code, programme, q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
            }
            return true;
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save question: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return false;
        }
    }

    private void removeSelected(TableView<AssessmentQuestion> table, ObservableList<AssessmentQuestion> list) {
        if (table == null) return;
        AssessmentQuestion selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert warn = new Alert(Alert.AlertType.WARNING, "Select a question to remove", ButtonType.OK);
            warn.setHeaderText(null);
            warn.showAndWait();
            return;
        }
        // Attempt DB deletion first so UI stays consistent
        if (courseAssignment != null) {
            String code = courseAssignment.courseCode;
            String year = courseAssignment.academicYear;
            String programme = courseAssignment.programme;
            boolean deleted = false;
            try {
                switch (selected.getAssessmentType()) {
                    case "Quiz1" -> deleted = db.deleteQuizQuestion(code,programme,1,selected.getNumber(), year);
                    case "Quiz2" -> deleted = db.deleteQuizQuestion(code,programme,2,selected.getNumber(), year);
                    case "Quiz3" -> deleted = db.deleteQuizQuestion(code,programme,3,selected.getNumber(), year);
                    case "Quiz4" -> deleted = db.deleteQuizQuestion(code,programme,4,selected.getNumber(), year);
                    case "Mid" -> deleted = db.deleteMidQuestion(code,programme,selected.getNumber(), year);
                    case "Final" -> deleted = db.deleteFinalQuestion(code,programme,selected.getNumber(), year);
                }
            } catch (Exception ex) {
                Alert err = new Alert(Alert.AlertType.ERROR, "Failed to delete question: " + ex.getMessage(), ButtonType.OK);
                err.setHeaderText(null);
                err.showAndWait();
                return; // abort removal
            }
            if (!deleted) {
                Alert info = new Alert(Alert.AlertType.INFORMATION, "Question not found in database (it may have already been removed).", ButtonType.OK);
                info.setHeaderText(null);
                info.showAndWait();
            }
        }
        list.remove(selected);
    }

    private void closeWindow(ActionEvent event) {
        if (event == null) return;
        Object src = event.getSource();
        if (src instanceof Node node) {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    // ==================== Clone Button Handlers ====================
    @FXML
    private void onQuiz1CloneButton(ActionEvent event) {
        showCloneDialog("Quiz1", 1, quiz1Questions);
    }

    @FXML
    private void onQuiz2CloneButton(ActionEvent event) {
        showCloneDialog("Quiz2", 2, quiz2Questions);
    }

    @FXML
    private void onQuiz3CloneButton(ActionEvent event) {
        showCloneDialog("Quiz3", 3, quiz3Questions);
    }

    @FXML
    private void onQuiz4CloneButton(ActionEvent event) {
        showCloneDialog("Quiz4", 4, quiz4Questions);
    }

    @FXML
    private void onMidCloneButton(ActionEvent event) {
        showCloneDialog("Mid", 0, midQuestions);
    }

    @FXML
    private void onFinalCloneButton(ActionEvent event) {
        showCloneDialog("Final", 0, finalQuestions);
    }

    private void showCloneDialog(String targetAssessment, int targetQuizNum, ObservableList<AssessmentQuestion> targetList) {
        if (courseAssignment == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Clone Questions");
        dialog.setHeaderText("Clone questions from another assessment to " + targetAssessment + "\n⚠️ WARNING: This will DELETE all existing questions in " + targetAssessment);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Source assessment dropdown - filtered based on target type
        ComboBox<String> sourceCombo = new ComboBox<>();
        if (targetAssessment.startsWith("Quiz")) {
            // Quiz can only clone from other quizzes
            sourceCombo.getItems().addAll("Quiz 1", "Quiz 2", "Quiz 3", "Quiz 4");
            sourceCombo.setValue("Quiz 1");
        } else if (targetAssessment.equals("Mid")) {
            // Mid can only clone from Mid
            sourceCombo.getItems().add("Mid");
            sourceCombo.setValue("Mid");
        } else { // Final
            // Final can only clone from Final
            sourceCombo.getItems().add("Final");
            sourceCombo.setValue("Final");
        }
        
        // Academic year dropdown (current year and previous 3 years)
        ComboBox<String> yearCombo = new ComboBox<>();
        String currentYear = courseAssignment.academicYear;
        yearCombo.getItems().add(currentYear);
        try {
            int year = Integer.parseInt(currentYear.split("-")[0]);
            for (int i = 1; i <= 3; i++) {
                int prevYear = year - i;
                yearCombo.getItems().add(prevYear + "-" + (prevYear + 1));
            }
        } catch (Exception e) {
            // If parsing fails, just use current year
        }
        yearCombo.setValue(currentYear);

        grid.add(new Label("Source Assessment:"), 0, 0);
        grid.add(sourceCombo, 1, 0);
        grid.add(new Label("Academic Year:"), 0, 1);
        grid.add(yearCombo, 1, 1);
        grid.add(new Label("Target Assessment:"), 0, 2);
        grid.add(new Label(targetAssessment), 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String sourceText = sourceCombo.getValue();
                String sourceAssessment;
                int sourceQuizNum = 0;
                
                // Parse source assessment
                switch (sourceText) {
                    case "Quiz 1" -> { sourceAssessment = "Quiz1"; sourceQuizNum = 1; }
                    case "Quiz 2" -> { sourceAssessment = "Quiz2"; sourceQuizNum = 2; }
                    case "Quiz 3" -> { sourceAssessment = "Quiz3"; sourceQuizNum = 3; }
                    case "Quiz 4" -> { sourceAssessment = "Quiz4"; sourceQuizNum = 4; }
                    case "Mid" -> sourceAssessment = "Mid";
                    case "Final" -> sourceAssessment = "Final";
                    default -> { sourceAssessment = "Quiz1"; sourceQuizNum = 1; }
                }
                
                String sourceYear = yearCombo.getValue();
                
                // Perform clone operation
                try {
                    int count = db.cloneQuestions(
                        courseAssignment.courseCode,
                        courseAssignment.programme,
                        sourceAssessment,
                        sourceQuizNum,
                        sourceYear,
                        targetAssessment,
                        targetQuizNum,
                        courseAssignment.academicYear
                    );
                    
                    if (count > 0) {
                        Alert success = new Alert(Alert.AlertType.INFORMATION, 
                            "Successfully cloned " + count + " question(s) from " + sourceText + " (" + sourceYear + ") to " + targetAssessment, 
                            ButtonType.OK);
                        success.setHeaderText(null);
                        success.showAndWait();
                        
                        // Reload the target questions to show cloned ones
                        loadQuestionsForAssessment(targetAssessment, targetQuizNum, targetList);
                    } else {
                        Alert info = new Alert(Alert.AlertType.INFORMATION, 
                            "No questions found in " + sourceText + " (" + sourceYear + ") to clone.", 
                            ButtonType.OK);
                        info.setHeaderText(null);
                        info.showAndWait();
                    }
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR, 
                        "Failed to clone questions: " + e.getMessage(), 
                        ButtonType.OK);
                    error.setHeaderText(null);
                    error.showAndWait();
                }
            }
        });
    }

    private void loadQuestionsForAssessment(String assessment, int quizNum, ObservableList<AssessmentQuestion> list) {
        if (courseAssignment == null) return;
        try {
            List<DatabaseService.QuestionData> dbQuestions;
            switch (assessment) {
                case "Quiz1", "Quiz2", "Quiz3", "Quiz4" -> 
                    dbQuestions = db.getQuizQuestions(courseAssignment.courseCode, courseAssignment.programme, quizNum, courseAssignment.academicYear);
                case "Mid" -> 
                    dbQuestions = db.getMidQuestions(courseAssignment.courseCode, courseAssignment.programme, courseAssignment.academicYear);
                case "Final" -> 
                    dbQuestions = db.getFinalQuestions(courseAssignment.courseCode, courseAssignment.programme, courseAssignment.academicYear);
                default -> dbQuestions = java.util.Collections.emptyList();
            }
            list.clear();
            for (DatabaseService.QuestionData d : dbQuestions) {
                list.add(new AssessmentQuestion(d.id, d.title, d.marks, d.co, d.po, assessment));
            }
        } catch (Exception e) {
            // Silently fail - user already sees error if it happens
        }
    }
}
