package org.example.co_po_assessment;

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
        try { db.ensureAssessmentsExist(courseAssignment.courseCode, courseAssignment.academicYear); } catch (Exception ignored) {}

        quiz1Questions.clear(); quiz2Questions.clear(); quiz3Questions.clear(); quiz4Questions.clear(); midQuestions.clear(); finalQuestions.clear();
        try {
            String year = courseAssignment.academicYear;
            for (int i = 1; i <= 4; i++) {
                List<DatabaseService.QuestionData> qd = db.getQuizQuestions(courseAssignment.courseCode, i, year);
                ObservableList<AssessmentQuestion> target = switch (i) { case 1 -> quiz1Questions; case 2 -> quiz2Questions; case 3 -> quiz3Questions; default -> quiz4Questions; };
                for (DatabaseService.QuestionData d : qd) {
                    target.add(new AssessmentQuestion(d.id, d.title, d.marks, d.co, d.po, "Quiz" + i));
                }
            }
            for (DatabaseService.QuestionData d : db.getMidQuestions(courseAssignment.courseCode, year)) {
                midQuestions.add(new AssessmentQuestion(d.id, d.title, d.marks, d.co, d.po, "Mid"));
            }
            for (DatabaseService.QuestionData d : db.getFinalQuestions(courseAssignment.courseCode, year)) {
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

        coChoice.getItems().addAll("CO1","CO2","CO3","CO4","CO5","CO6","CO7","CO8","CO9","CO10","CO11","CO12","CO13","CO14","CO15","CO16","CO17","CO18","CO19","CO20");
        poChoice.getItems().addAll("PO1","PO2","PO3","PO4","PO5","PO6","PO7","PO8","PO9","PO10","PO11","PO12");

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
            switch (q.getAssessmentType()) {
                case "Quiz1" -> db.saveQuizQuestion(code,1,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Quiz2" -> db.saveQuizQuestion(code,2,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Quiz3" -> db.saveQuizQuestion(code,3,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Quiz4" -> db.saveQuizQuestion(code,4,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Mid" -> db.saveMidQuestion(code,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
                case "Final" -> db.saveFinalQuestion(code,q.getNumber(),q.getMarks(),q.getCo(),q.getPo(), year);
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
            boolean deleted = false;
            try {
                switch (selected.getAssessmentType()) {
                    case "Quiz1" -> deleted = db.deleteQuizQuestion(code,1,selected.getNumber(), year);
                    case "Quiz2" -> deleted = db.deleteQuizQuestion(code,2,selected.getNumber(), year);
                    case "Quiz3" -> deleted = db.deleteQuizQuestion(code,3,selected.getNumber(), year);
                    case "Quiz4" -> deleted = db.deleteQuizQuestion(code,4,selected.getNumber(), year);
                    case "Mid" -> deleted = db.deleteMidQuestion(code,selected.getNumber(), year);
                    case "Final" -> deleted = db.deleteFinalQuestion(code,selected.getNumber(), year);
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
}
