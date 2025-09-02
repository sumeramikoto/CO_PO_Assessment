package org.example.co_po_assessment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.properties.TextAlignment; // corrected package name (properties)

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot; // added
import org.jfree.chart.axis.NumberAxis; // added

public class FacultyDashboardController {
    @FXML Button logoutButton;
    @FXML Label facultyLabel;
    @FXML TableView<DatabaseService.FacultyCourseAssignment> assignedCoursesTableView;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> courseCodeColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> courseNameColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> academicYearColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> departmentColumn;
    @FXML TableColumn<DatabaseService.FacultyCourseAssignment, String> programmeColumn;

    private final DatabaseService db = DatabaseService.getInstance();
    private final ObservableList<DatabaseService.FacultyCourseAssignment> assignments = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Configure columns
        if (courseCodeColumn != null) courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        if (courseNameColumn != null) courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        if (academicYearColumn != null) academicYearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        if (departmentColumn != null) departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        if (programmeColumn != null) programmeColumn.setCellValueFactory(new PropertyValueFactory<>("programme"));

        if (assignedCoursesTableView != null) {
            assignedCoursesTableView.setItems(assignments);
        }
        loadFacultyData();
    }

    private void loadFacultyData() {
        DatabaseService.FacultyInfo info = UserSession.getCurrentFaculty();
        if (info == null) {
            if (facultyLabel != null) facultyLabel.setText("Faculty (session not found)");
            return;
        }
        if (facultyLabel != null) facultyLabel.setText("Welcome, " + info.fullName + " (" + info.shortname + ")");
        try {
            assignments.clear();
            assignments.addAll(db.getAssignmentsForFaculty(info.id));
        } catch (Exception e) {
            // Optionally show an alert
            e.printStackTrace();
        }
    }

    public void onLogoutButton(ActionEvent actionEvent) {
        try {
            UserSession.clear();
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
            Platform.runLater(() -> {
                try {
                    new AssessmentSystem().start(new Stage());
                } catch (Exception e) {
                    // simple fallback: print stack trace until alert system added here
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onQuestionsButton(ActionEvent actionEvent) {
        // opens a window that'll show the questions for the selected course and each respective exam
        DatabaseService.FacultyCourseAssignment selected = assignedCoursesTableView != null ? assignedCoursesTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course first.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("manageCourseQuestions-view.fxml"));
            Parent root = loader.load();
            ManageCourseQuestionsController controller = loader.getController();
            controller.setCourseAssignment(selected); // pass context (future use)
            Stage stage = new Stage();
            stage.setTitle("Manage Questions - " + selected.getCourseCode());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Manage Course Questions window: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void onMarksButton(ActionEvent actionEvent) {
        DatabaseService.FacultyCourseAssignment selected = assignedCoursesTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course first.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("detailedMarks-view.fxml"));
            Parent root = loader.load();
            DetailedMarksController controller = loader.getController();
            controller.setContext(selected.getCourseCode(), selected.getAcademicYear());

            Stage stage = new Stage();
            stage.setTitle("Student Marks - " + selected.getCourseCode() + " (" + selected.getAcademicYear() + ")");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open Marks window: " + e.getMessage(), ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void onReportButton(ActionEvent actionEvent) {
        // checks if all the students have been graded yet or not
        // if graded then generates the co/po assessment report for the course
    }

    public void onCOReportButton(ActionEvent actionEvent) {
        DatabaseService.FacultyCourseAssignment selected = assignedCoursesTableView != null ? assignedCoursesTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course first.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        String courseCode = selected.getCourseCode();
        String academicYear = selected.getAcademicYear();
        try {
            // Fetch questions per assessment individually to validate completeness
            List<DatabaseService.QuestionData> quiz1 = db.getQuizQuestions(courseCode,1,academicYear);
            List<DatabaseService.QuestionData> quiz2 = db.getQuizQuestions(courseCode,2,academicYear);
            List<DatabaseService.QuestionData> quiz3 = db.getQuizQuestions(courseCode,3,academicYear);
            List<DatabaseService.QuestionData> quiz4 = db.getQuizQuestions(courseCode,4,academicYear);
            List<DatabaseService.QuestionData> midQuestions = db.getMidQuestions(courseCode, academicYear);
            List<DatabaseService.QuestionData> finalQuestions = db.getFinalQuestions(courseCode, academicYear);

            List<String> missingAssessments = new ArrayList<>();
            List<String> noCOAssessments = new ArrayList<>();
            java.util.function.BiConsumer<String,List<DatabaseService.QuestionData>> classify = (label,list)-> {
                if (list.isEmpty()) missingAssessments.add(label);
                else if (list.stream().noneMatch(q-> q.co!=null && !q.co.trim().isEmpty())) noCOAssessments.add(label);
            };
            classify.accept("Quiz 1", quiz1);
            classify.accept("Quiz 2", quiz2);
            classify.accept("Quiz 3", quiz3);
            classify.accept("Quiz 4", quiz4);
            classify.accept("Mid", midQuestions);
            classify.accept("Final", finalQuestions);

            if (!missingAssessments.isEmpty() || !noCOAssessments.isEmpty()) {
                StringBuilder msg = new StringBuilder();
                if (!missingAssessments.isEmpty()) msg.append("These assessments have no questions defined: ").append(String.join(", ", missingAssessments)).append(".\n");
                if (!noCOAssessments.isEmpty()) msg.append("These assessments have questions but none mapped to COs: ").append(String.join(", ", noCOAssessments)).append(".\n");
                msg.append("Add & map all assessments before generating the CO report.");
                Alert a = new Alert(Alert.AlertType.WARNING, msg.toString(), ButtonType.OK); a.setHeaderText("Incomplete Assessment Setup"); a.showAndWait();
                return;
            }

            // Combine all quiz questions for existing downstream logic
            List<DatabaseService.QuestionData> quizQuestionsAll = new ArrayList<>();
            quizQuestionsAll.addAll(quiz1); quizQuestionsAll.addAll(quiz2); quizQuestionsAll.addAll(quiz3); quizQuestionsAll.addAll(quiz4);
            // Build CO totals
            Map<String,Double> coTotal = new HashMap<>();
            java.util.function.BiConsumer<List<DatabaseService.QuestionData>,String> addQuestions = (list,label)-> {
                for (DatabaseService.QuestionData qd : list) { if (qd.co==null) continue; String co = qd.co.trim().toUpperCase(); if (co.isEmpty()) continue; coTotal.merge(co, qd.marks, Double::sum);} };
            addQuestions.accept(quizQuestionsAll, "Quiz");
            addQuestions.accept(midQuestions, "Mid");
            addQuestions.accept(finalQuestions, "Final");
            if (coTotal.isEmpty()) { Alert a = new Alert(Alert.AlertType.INFORMATION, "Questions exist but none have CO mappings.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }
            List<DatabaseService.StudentData> students = db.getEnrolledStudents(courseCode, academicYear);
            if (students.isEmpty()) { Alert a = new Alert(Alert.AlertType.INFORMATION, "No students enrolled for this course in the selected academic year.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }
            Map<Integer,String> quizIdToCO = new HashMap<>(); for (DatabaseService.QuestionData qd : quizQuestionsAll) if (qd.co!=null) quizIdToCO.put(qd.id, qd.co.trim().toUpperCase());
            Map<Integer,String> midIdToCO = new HashMap<>(); for (DatabaseService.QuestionData qd : midQuestions) if (qd.co!=null) midIdToCO.put(qd.id, qd.co.trim().toUpperCase());
            Map<Integer,String> finalIdToCO = new HashMap<>(); for (DatabaseService.QuestionData qd : finalQuestions) if (qd.co!=null) finalIdToCO.put(qd.id, qd.co.trim().toUpperCase());
            // Validation counters (only for CO-mapped questions)
            int totalRequired = 0; int graded = 0; List<String> sampleMissing = new ArrayList<>();
            // Per-student accumulation structure
            Map<String, Map<String, Double>> studentCOObtained = new HashMap<>();
            for (DatabaseService.StudentData sd : students) studentCOObtained.put(sd.id, new HashMap<>());
            // Process quizzes
            for (int quizNum=1; quizNum<=4; quizNum++) {
                for (DatabaseService.StudentMarksData smd : db.getStudentQuizMarks(courseCode, quizNum, academicYear)) {
                    String co = quizIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
                }
            }
            // Process mid
            for (DatabaseService.StudentMarksData smd : db.getStudentMidMarks(courseCode, academicYear)) {
                String co = midIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
            }
            // Process final
            for (DatabaseService.StudentMarksData smd : db.getStudentFinalMarks(courseCode, academicYear)) {
                String co = finalIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
            }
            int missing = totalRequired - graded;
            if (totalRequired == 0) { Alert a = new Alert(Alert.AlertType.INFORMATION, "There are CO-mapped questions but no enrolled students to grade.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }
            if (graded == 0) { Alert a = new Alert(Alert.AlertType.WARNING, "No marks have been entered yet. Please enter marks before generating the CO report.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }
            if (missing > 0) {
                Alert a = new Alert(Alert.AlertType.WARNING, String.format("Cannot generate CO report. %d of %d required mark entries are still ungraded. Sample missing: %s", missing, totalRequired, sampleMissing), ButtonType.OK);
                a.setHeaderText("Incomplete Grading"); a.showAndWait(); return; }
            final double THRESHOLD = 0.60; Map<String,Integer> attainedCounts = new HashMap<>(); for (String co: coTotal.keySet()) attainedCounts.put(co,0);
            for (DatabaseService.StudentData sd : students) { Map<String,Double> gotMap = studentCOObtained.get(sd.id); for (String co : coTotal.keySet()) { double denom = coTotal.get(co); if (denom <= 0) continue; double got = gotMap.getOrDefault(co,0.0); if (got/denom >= THRESHOLD) attainedCounts.merge(co,1,Integer::sum); } }
            Map<String,Double> percentPerCO = new TreeMap<>(Comparator.comparingInt(c -> { try { return Integer.parseInt(c.replaceAll("[^0-9]", "")); } catch(Exception ex){ return Integer.MAX_VALUE; } })); for (String co: coTotal.keySet()) percentPerCO.put(co, attainedCounts.getOrDefault(co,0)*100.0 / students.size());
            // Chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String,Double> e : percentPerCO.entrySet()) dataset.addValue(e.getValue(), "CO Attainment", e.getKey());
            JFreeChart chart = ChartFactory.createBarChart("CO Attainment", "CO", "% Students", dataset);
            // Force y-axis 0-100%
            CategoryPlot plot = chart.getCategoryPlot();
            if (plot.getRangeAxis() instanceof NumberAxis na) {
                na.setRange(0.0, 100.0);
                na.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            }
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream(); ChartUtils.writeChartAsPNG(chartBaos, chart, 640, 400); byte[] chartBytes = chartBaos.toByteArray();
            // PDF
            File reportsDir = new File("co_reports"); if (!reportsDir.exists()) reportsDir.mkdirs();
            String safeProgramme = selected.getProgramme().replaceAll("[^A-Za-z0-9_-]", "");
            File outFile = new File(reportsDir, courseCode + "_" + academicYear + "_" + safeProgramme + ".pdf");
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(outFile)); PdfDocument pdf = new PdfDocument(writer); Document doc = new Document(pdf)) {
                Paragraph title = new Paragraph(courseCode + " - " + selected.getCourseName()); title.setFontSize(16).setTextAlignment(TextAlignment.CENTER); doc.add(title);
                doc.add(new Paragraph("Department: " + selected.getDepartment()));
                doc.add(new Paragraph("Programme: " + selected.getProgramme()));
                DatabaseService.FacultyInfo fi = UserSession.getCurrentFaculty();
                if (fi != null) doc.add(new Paragraph("Faculty: " + fi.fullName + " (" + fi.shortname + ")"));
                doc.add(new Paragraph("Academic Year: " + academicYear));
                doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
                doc.add(new Paragraph("\n"));
                for (Map.Entry<String,Double> e : percentPerCO.entrySet()) doc.add(new Paragraph(e.getKey() + " was successfully attained by " + String.format(Locale.US, "%.2f", e.getValue()) + "% of students"));
                doc.add(new Paragraph("\n"));
                Image chartImg = new Image(ImageDataFactory.create(chartBytes)).setAutoScale(true); doc.add(chartImg);
            }
            Alert done = new Alert(Alert.AlertType.INFORMATION, "CO report generated: " + outFile.getAbsolutePath(), ButtonType.OK); done.setHeaderText(null); done.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to generate CO report: " + ex.getMessage(), ButtonType.OK); err.setHeaderText(null); err.showAndWait();
        }
    }

    public void onPOReportButton(ActionEvent actionEvent) {
    }
}
