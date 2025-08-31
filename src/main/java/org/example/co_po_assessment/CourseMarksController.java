package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;

public class CourseMarksController {
    public void onQ1BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }

    public void onQ2BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }

    public void onQ3BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }

    public void onQ4BackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }

    public void onMidBackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }

    public void onFinalBackButton(ActionEvent actionEvent) { closeWindow(actionEvent); }

    private void closeWindow(ActionEvent event) {
        if (event == null) return;
        Object src = event.getSource();
        if (src instanceof Node node) {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }
}
