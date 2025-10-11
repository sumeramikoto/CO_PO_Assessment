package org.example.co_po_assessment;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FxmlPathsTest {
    @Test
    void allFxmlResourcesResolvable() {
        List<String> paths = List.of(
                "/org/example/co_po_assessment/adminDashboard-view.fxml",
                "/org/example/co_po_assessment/facultyDashboard-view.fxml",
                "/org/example/co_po_assessment/manageFaculties-view.fxml",
                "/org/example/co_po_assessment/manageStudents-view.fxml",
                "/org/example/co_po_assessment/manageCourses-view.fxml",
                "/org/example/co_po_assessment/manageCourseAssignments-view.fxml",
                "/org/example/co_po_assessment/manageEnrollments-view.fxml",
                "/org/example/co_po_assessment/reports-view.fxml",
                "/org/example/co_po_assessment/manageCourseQuestions-view.fxml",
                "/org/example/co_po_assessment/detailedMarks-view.fxml",
                "/org/example/co_po_assessment/coReportDialog-view.fxml",
                "/org/example/co_po_assessment/poReportDialog-view.fxml",
                "/org/example/co_po_assessment/courseInput-view.fxml",
                "/org/example/co_po_assessment/courseAssignmentInput-view.fxml",
                "/org/example/co_po_assessment/facultyInfoInput-view.fxml",
                "/org/example/co_po_assessment/studentInfoInput-view.fxml"
        );
        // Use an arbitrary class from the module to resolve absolute resource paths
        Class<?> anchor = org.example.co_po_assessment.dashboard_controller.AdminDashboardController.class;
        for (String p : paths) {
            URL url = anchor.getResource(p);
            assertNotNull(url, "Missing FXML resource: " + p);
        }
    }
}

