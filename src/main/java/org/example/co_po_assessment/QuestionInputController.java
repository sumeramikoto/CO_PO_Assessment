package org.example.co_po_assessment;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class QuestionInputWindow {
    @FXML
    RadioButton quiz1RB;
    @FXML    @FXML
    RadioButton quiz2RB;
    @FXML
    RadioButton quiz3RB;
    @FXML
    RadioButton quiz4RB;
    @FXML
    RadioButton midRB;
    @FXML
    RadioButton finalRB;
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

    

}
