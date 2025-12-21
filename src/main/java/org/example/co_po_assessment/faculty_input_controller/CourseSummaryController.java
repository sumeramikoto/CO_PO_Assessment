package org.example.co_po_assessment.faculty_input_controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.co_po_assessment.DB_helper.DatabaseService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CourseSummaryController implements Initializable {

    // Course Info Labels
    @FXML private Label courseNameLabel;
    @FXML private Label programmeLabel;
    @FXML private Label academicYearLabel;
    @FXML private Label enrolledStudentsLabel;

    // Quiz Labels
    @FXML private Label quiz1QuestionsLabel;
    @FXML private Label quiz1MarksLabel;
    @FXML private Label quiz1StatusLabel;
    
    @FXML private Label quiz2QuestionsLabel;
    @FXML private Label quiz2MarksLabel;
    @FXML private Label quiz2StatusLabel;
    
    @FXML private Label quiz3QuestionsLabel;
    @FXML private Label quiz3MarksLabel;
    @FXML private Label quiz3StatusLabel;
    
    @FXML private Label quiz4QuestionsLabel;
    @FXML private Label quiz4MarksLabel;
    @FXML private Label quiz4StatusLabel;

    // Exam Labels
    @FXML private Label midQuestionsLabel;
    @FXML private Label midMarksLabel;
    @FXML private Label midStatusLabel;
    
    @FXML private Label finalQuestionsLabel;
    @FXML private Label finalMarksLabel;
    @FXML private Label finalStatusLabel;

    // Overall Labels
    @FXML private Label totalQuestionsLabel;
    @FXML private Label totalMarksLabel;
    @FXML private Label completionPercentageLabel;

    private DatabaseService.FacultyCourseAssignment courseAssignment;
    private final DatabaseService db = DatabaseService.getInstance();
    private Runnable onBackAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initial setup if needed
    }

    public void setContext(String courseCode, String programme, String academicYear) {
        // Create a course assignment object with the provided data
        // We'll fetch the course name from the database, use empty string for department as fallback
        String courseName = courseCode; // Default to code if lookup fails
        String department = "";
        
        try {
            // Try to get full course details from database
            List<DatabaseService.FacultyCourseAssignment> assignments = db.getAssignmentsForFaculty(
                org.example.co_po_assessment.utilities.UserSession.getCurrentFaculty().getId()
            );
            
            // Find the matching assignment
            for (DatabaseService.FacultyCourseAssignment assignment : assignments) {
                if (assignment.courseCode.equals(courseCode) && 
                    assignment.programme.equals(programme) && 
                    assignment.academicYear.equals(academicYear)) {
                    this.courseAssignment = assignment;
                    loadCourseSummary();
                    return;
                }
            }
            
            // If not found in assignments, create a minimal one
            courseName = courseCode; // Use course code as name
            
        } catch (Exception e) {
            // Use defaults
        }
        
        // Create the assignment with available data
        this.courseAssignment = new DatabaseService.FacultyCourseAssignment(
            courseCode, courseName, academicYear, department, programme
        );
        loadCourseSummary();
    }

    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }

    public void setCourseAssignment(DatabaseService.FacultyCourseAssignment assignment) {
        this.courseAssignment = assignment;
        if (assignment != null) {
            loadCourseSummary();
        }
    }

    private void loadCourseSummary() {
        try {
            // Set course info
            courseNameLabel.setText(courseAssignment.courseCode + " - " + courseAssignment.courseName);
            programmeLabel.setText(courseAssignment.programme);
            academicYearLabel.setText(courseAssignment.academicYear);

            // Load enrolled students count
            List<DatabaseService.StudentData> students = db.getEnrolledStudents(
                courseAssignment.courseCode,
                courseAssignment.programme,
                courseAssignment.academicYear
            );
            enrolledStudentsLabel.setText(String.valueOf(students.size()));

            // Load assessment summaries
            int totalQuestions = 0;
            double totalMarks = 0.0;
            int totalMarksEntries = 0;
            int totalPossibleEntries = 0;

            // Quiz 1
            AssessmentSummary quiz1 = loadAssessmentSummary("Quiz1", 1, students.size());
            updateAssessmentLabels(quiz1, quiz1QuestionsLabel, quiz1MarksLabel, quiz1StatusLabel);
            totalQuestions += quiz1.questionCount;
            totalMarks += quiz1.totalMarks;
            totalMarksEntries += quiz1.marksEntered;
            totalPossibleEntries += quiz1.totalPossibleEntries;

            // Quiz 2
            AssessmentSummary quiz2 = loadAssessmentSummary("Quiz2", 2, students.size());
            updateAssessmentLabels(quiz2, quiz2QuestionsLabel, quiz2MarksLabel, quiz2StatusLabel);
            totalQuestions += quiz2.questionCount;
            totalMarks += quiz2.totalMarks;
            totalMarksEntries += quiz2.marksEntered;
            totalPossibleEntries += quiz2.totalPossibleEntries;

            // Quiz 3
            AssessmentSummary quiz3 = loadAssessmentSummary("Quiz3", 3, students.size());
            updateAssessmentLabels(quiz3, quiz3QuestionsLabel, quiz3MarksLabel, quiz3StatusLabel);
            totalQuestions += quiz3.questionCount;
            totalMarks += quiz3.totalMarks;
            totalMarksEntries += quiz3.marksEntered;
            totalPossibleEntries += quiz3.totalPossibleEntries;

            // Quiz 4
            AssessmentSummary quiz4 = loadAssessmentSummary("Quiz4", 4, students.size());
            updateAssessmentLabels(quiz4, quiz4QuestionsLabel, quiz4MarksLabel, quiz4StatusLabel);
            totalQuestions += quiz4.questionCount;
            totalMarks += quiz4.totalMarks;
            totalMarksEntries += quiz4.marksEntered;
            totalPossibleEntries += quiz4.totalPossibleEntries;

            // Mid
            AssessmentSummary mid = loadAssessmentSummary("Mid", 0, students.size());
            updateAssessmentLabels(mid, midQuestionsLabel, midMarksLabel, midStatusLabel);
            totalQuestions += mid.questionCount;
            totalMarks += mid.totalMarks;
            totalMarksEntries += mid.marksEntered;
            totalPossibleEntries += mid.totalPossibleEntries;

            // Final
            AssessmentSummary finalExam = loadAssessmentSummary("Final", 0, students.size());
            updateAssessmentLabels(finalExam, finalQuestionsLabel, finalMarksLabel, finalStatusLabel);
            totalQuestions += finalExam.questionCount;
            totalMarks += finalExam.totalMarks;
            totalMarksEntries += finalExam.marksEntered;
            totalPossibleEntries += finalExam.totalPossibleEntries;

            // Update overall statistics
            totalQuestionsLabel.setText(String.valueOf(totalQuestions));
            totalMarksLabel.setText(String.format("%.0f", totalMarks));
            
            double completionPercentage = totalPossibleEntries > 0 
                ? (totalMarksEntries * 100.0 / totalPossibleEntries) 
                : 0.0;
            completionPercentageLabel.setText(String.format("%.1f%%", completionPercentage));

            // Color code completion percentage
            if (completionPercentage >= 100) {
                completionPercentageLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
            } else if (completionPercentage >= 50) {
                completionPercentageLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f59e0b;");
            } else {
                completionPercentageLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to load course summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private AssessmentSummary loadAssessmentSummary(String type, int quizNumber, int studentCount) {
        try {
            List<DatabaseService.QuestionData> questions;
            
            if (type.startsWith("Quiz")) {
                questions = db.getQuizQuestions(
                    courseAssignment.courseCode,
                    courseAssignment.programme,
                    quizNumber,
                    courseAssignment.academicYear
                );
            } else if (type.equals("Mid")) {
                questions = db.getMidQuestions(
                    courseAssignment.courseCode,
                    courseAssignment.programme,
                    courseAssignment.academicYear
                );
            } else { // Final
                questions = db.getFinalQuestions(
                    courseAssignment.courseCode,
                    courseAssignment.programme,
                    courseAssignment.academicYear
                );
            }

            int questionCount = questions.size();
            double totalMarks = questions.stream().mapToDouble(q -> q.marks).sum();
            
            // Count how many marks have been entered by checking StudentMarksData
            int marksEntered = 0;
            if (questionCount > 0 && studentCount > 0) {
                List<DatabaseService.StudentMarksData> marksData;
                if (type.startsWith("Quiz")) {
                    marksData = db.getStudentQuizMarks(
                        courseAssignment.courseCode,
                        courseAssignment.programme,
                        quizNumber,
                        courseAssignment.academicYear
                    );
                } else if (type.equals("Mid")) {
                    marksData = db.getStudentMidMarks(
                        courseAssignment.courseCode,
                        courseAssignment.programme,
                        courseAssignment.academicYear
                    );
                } else { // Final
                    marksData = db.getStudentFinalMarks(
                        courseAssignment.courseCode,
                        courseAssignment.programme,
                        courseAssignment.academicYear
                    );
                }
                
                // Count non-null marks_obtained entries
                marksEntered = (int) marksData.stream()
                    .filter(m -> m.marksObtained != null)
                    .count();
            }
            
            int totalPossibleEntries = questionCount * studentCount;

            return new AssessmentSummary(questionCount, totalMarks, marksEntered, totalPossibleEntries);

        } catch (Exception e) {
            return new AssessmentSummary(0, 0, 0, 0);
        }
    }

    private void updateAssessmentLabels(AssessmentSummary summary, Label questionsLabel, 
                                       Label marksLabel, Label statusLabel) {
        questionsLabel.setText(summary.questionCount + " Question" + (summary.questionCount != 1 ? "s" : ""));
        marksLabel.setText(String.format("Total: %.0f marks", summary.totalMarks));

        if (summary.questionCount == 0) {
            statusLabel.setText("⚪ Not Setup");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        } else if (summary.marksEntered == 0) {
            statusLabel.setText("🔴 No Marks Entered");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } else if (summary.marksEntered < summary.totalPossibleEntries) {
            double percentage = (summary.marksEntered * 100.0) / summary.totalPossibleEntries;
            statusLabel.setText(String.format("🟡 %.0f%% Complete", percentage));
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        } else {
            statusLabel.setText("🟢 All Marks Entered");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        loadCourseSummary();
    }

    @FXML
    private void onBack(ActionEvent event) {
        if (onBackAction != null) {
            onBackAction.run();
        } else {
            closeWindow(event);
        }
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

    // Inner class to hold assessment summary data
    private static class AssessmentSummary {
        final int questionCount;
        final double totalMarks;
        final int marksEntered;
        final int totalPossibleEntries;

        AssessmentSummary(int questionCount, double totalMarks, int marksEntered, int totalPossibleEntries) {
            this.questionCount = questionCount;
            this.totalMarks = totalMarks;
            this.marksEntered = marksEntered;
            this.totalPossibleEntries = totalPossibleEntries;
        }
    }
}
