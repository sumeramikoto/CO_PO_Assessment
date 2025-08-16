package org.example.co_po_assessment;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.awt.event.ActionEvent;
import java.net.URL;
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
    int marks;
    String co;
    String po;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        coChoiceBox.getItems().addAll(coArray);
        poChoiceBox.getItems().addAll(poArray);
        examChoiceBox.getItems().addAll(exams);
    }

    public void addQuestion(ActionEvent event) {
        try {
            questionNo = questionNoTextField.getText();
            marks = Integer.parseInt(marksTextField.getText());
            co = coChoiceBox.getValue();
            po = poChoiceBox.getValue();
            examType = examChoiceBox.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
