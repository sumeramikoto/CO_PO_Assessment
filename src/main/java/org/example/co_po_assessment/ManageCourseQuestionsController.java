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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        bindTables();
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

        coChoice.getItems().addAll("CO1","CO2","CO3","CO4","CO5","CO6","CO7","CO8","CO9","CO10");
        poChoice.getItems().addAll("PO1","PO2","PO3","PO4","PO5","PO6","PO7","PO8","PO9","PO10","PO11","PO12");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Question No:"),0,0); grid.add(numberField,1,0);
        grid.add(new Label("Marks:"),0,1); grid.add(marksField,1,1);
        grid.add(new Label("CO:"),0,2); grid.add(coChoice,1,2);
        grid.add(new Label("PO:"),0,3); grid.add(poChoice,1,3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == addBtnType) {
                try {
                    String no = numberField.getText().trim();
                    double marks = Double.parseDouble(marksField.getText().trim());
                    String co = coChoice.getValue();
                    String po = poChoice.getValue();
                    if (no.isEmpty() || co == null || po == null || marks <= 0) return null;
                    return new AssessmentQuestion(no, marks, co, po, assessmentType);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(q -> {
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

    private void removeSelected(TableView<AssessmentQuestion> table, ObservableList<AssessmentQuestion> list) {
        if (table == null) return;
        AssessmentQuestion selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert warn = new Alert(Alert.AlertType.WARNING, "Select a question to remove", ButtonType.OK);
            warn.setHeaderText(null);
            warn.showAndWait();
            return;
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
