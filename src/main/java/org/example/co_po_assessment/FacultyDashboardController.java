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
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // added for excel export
import org.apache.poi.ss.usermodel.*; // added for excel export
import org.apache.poi.ss.util.CellReference; // added for excel export
import org.apache.poi.ss.util.CellRangeAddressList; // added for data validation
import javafx.stage.FileChooser; // added for excel import

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
            controller.setContext(selected.getCourseCode(), selected.getProgramme(), selected.getAcademicYear());

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
        String programme = selected.getProgramme();
        try {
            // Fetch questions per assessment individually to validate completeness
            List<DatabaseService.QuestionData> quiz1 = db.getQuizQuestions(courseCode, programme, 1,academicYear);
            List<DatabaseService.QuestionData> quiz2 = db.getQuizQuestions(courseCode, programme, 2,academicYear);
            List<DatabaseService.QuestionData> quiz3 = db.getQuizQuestions(courseCode, programme, 3,academicYear);
            List<DatabaseService.QuestionData> quiz4 = db.getQuizQuestions(courseCode, programme, 4,academicYear);
            List<DatabaseService.QuestionData> midQuestions = db.getMidQuestions(courseCode, programme, academicYear);
            List<DatabaseService.QuestionData> finalQuestions = db.getFinalQuestions(courseCode, programme, academicYear);

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
            List<DatabaseService.StudentData> students = db.getEnrolledStudents(courseCode, programme, academicYear);
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
                for (DatabaseService.StudentMarksData smd : db.getStudentQuizMarks(courseCode, programme, quizNum, academicYear)) {
                    String co = quizIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
                }
            }
            // Process mid
            for (DatabaseService.StudentMarksData smd : db.getStudentMidMarks(courseCode, programme, academicYear)) {
                String co = midIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
            }
            // Process final
            for (DatabaseService.StudentMarksData smd : db.getStudentFinalMarks(courseCode, programme, academicYear)) {
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
        DatabaseService.FacultyCourseAssignment selected = assignedCoursesTableView != null ? assignedCoursesTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course first.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        String courseCode = selected.getCourseCode();
        String academicYear = selected.getAcademicYear();
        String programme = selected.getProgramme();
        try {
            List<DatabaseService.QuestionData> quiz1 = db.getQuizQuestions(courseCode, programme, 1,academicYear);
            List<DatabaseService.QuestionData> quiz2 = db.getQuizQuestions(courseCode, programme, 2,academicYear);
            List<DatabaseService.QuestionData> quiz3 = db.getQuizQuestions(courseCode, programme, 3,academicYear);
            List<DatabaseService.QuestionData> quiz4 = db.getQuizQuestions(courseCode, programme, 4,academicYear);
            List<DatabaseService.QuestionData> midQuestions = db.getMidQuestions(courseCode, programme, academicYear);
            List<DatabaseService.QuestionData> finalQuestions = db.getFinalQuestions(courseCode, programme, academicYear);

            List<String> missingAssessments = new ArrayList<>();
            List<String> noPOAssessments = new ArrayList<>();
            java.util.function.BiConsumer<String,List<DatabaseService.QuestionData>> classify = (label,list)-> {
                if (list.isEmpty()) missingAssessments.add(label);
                else if (list.stream().noneMatch(q-> q.po!=null && !q.po.trim().isEmpty())) noPOAssessments.add(label);
            };
            classify.accept("Quiz 1", quiz1);
            classify.accept("Quiz 2", quiz2);
            classify.accept("Quiz 3", quiz3);
            classify.accept("Quiz 4", quiz4);
            classify.accept("Mid", midQuestions);
            classify.accept("Final", finalQuestions);

            if (!missingAssessments.isEmpty() || !noPOAssessments.isEmpty()) {
                StringBuilder msg = new StringBuilder();
                if (!missingAssessments.isEmpty()) msg.append("These assessments have no questions defined: ").append(String.join(", ", missingAssessments)).append(".\n");
                if (!noPOAssessments.isEmpty()) msg.append("These assessments have questions but none mapped to POs: ").append(String.join(", ", noPOAssessments)).append(".\n");
                msg.append("Add & map all assessments before generating the PO report.");
                Alert a = new Alert(Alert.AlertType.WARNING, msg.toString(), ButtonType.OK); a.setHeaderText("Incomplete Assessment Setup"); a.showAndWait();
                return;
            }

            List<DatabaseService.QuestionData> quizQuestionsAll = new ArrayList<>();
            quizQuestionsAll.addAll(quiz1); quizQuestionsAll.addAll(quiz2); quizQuestionsAll.addAll(quiz3); quizQuestionsAll.addAll(quiz4);
            Map<String,Double> poTotal = new HashMap<>();
            Map<String, Map<String,Double>> poCoTotals = new HashMap<>();
            java.util.function.Consumer<DatabaseService.QuestionData> accumulate = q -> {
                if (q.po == null || q.po.trim().isEmpty() || q.co == null || q.co.trim().isEmpty()) return;
                String po = q.po.trim().toUpperCase();
                String co = q.co.trim().toUpperCase();
                poTotal.merge(po, q.marks, Double::sum);
                poCoTotals.computeIfAbsent(po, k-> new HashMap<>()).merge(co, q.marks, Double::sum);
            };
            quizQuestionsAll.forEach(accumulate); midQuestions.forEach(accumulate); finalQuestions.forEach(accumulate);
            if (poTotal.isEmpty()) { Alert a = new Alert(Alert.AlertType.INFORMATION, "Questions exist but none have PO mappings.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }

            List<DatabaseService.StudentData> students = db.getEnrolledStudents(courseCode, programme, academicYear);
            if (students.isEmpty()) { Alert a = new Alert(Alert.AlertType.INFORMATION, "No students enrolled for this course in the selected academic year.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }

            Map<Integer,String> quizIdToPO = new HashMap<>(); Map<Integer,String> quizIdToCO = new HashMap<>();
            for (DatabaseService.QuestionData qd : quizQuestionsAll) { if (qd.po!=null) quizIdToPO.put(qd.id, qd.po.trim().toUpperCase()); if (qd.co!=null) quizIdToCO.put(qd.id, qd.co.trim().toUpperCase()); }
            Map<Integer,String> midIdToPO = new HashMap<>(); Map<Integer,String> midIdToCO = new HashMap<>();
            for (DatabaseService.QuestionData qd : midQuestions) { if (qd.po!=null) midIdToPO.put(qd.id, qd.po.trim().toUpperCase()); if (qd.co!=null) midIdToCO.put(qd.id, qd.co.trim().toUpperCase()); }
            Map<Integer,String> finalIdToPO = new HashMap<>(); Map<Integer,String> finalIdToCO = new HashMap<>();
            for (DatabaseService.QuestionData qd : finalQuestions) { if (qd.po!=null) finalIdToPO.put(qd.id, qd.po.trim().toUpperCase()); if (qd.co!=null) finalIdToCO.put(qd.id, qd.co.trim().toUpperCase()); }

            int totalRequired = 0; int graded = 0; List<String> sampleMissing = new ArrayList<>();
            Map<String, Map<String, Double>> studentPoTotals = new HashMap<>();
            Map<String, Map<String, Map<String, Double>>> studentPoCoTotals = new HashMap<>();
            for (DatabaseService.StudentData sd : students) { studentPoTotals.put(sd.id, new HashMap<>()); studentPoCoTotals.put(sd.id, new HashMap<>()); }

            // Process quizzes
            for (int quizNum=1; quizNum<=4; quizNum++) {
                for (DatabaseService.StudentMarksData smd : db.getStudentQuizMarks(courseCode, programme, quizNum, academicYear)) {
                    String po = quizIdToPO.get(smd.questionId); String co = quizIdToCO.get(smd.questionId);
                    if (po==null || co==null) continue;
                    totalRequired++;
                    if (smd.marksObtained != null) {
                        graded++; double got = smd.marksObtained;
                        studentPoTotals.get(smd.studentId).merge(po, got, Double::sum);
                        studentPoCoTotals.get(smd.studentId).computeIfAbsent(po,k-> new HashMap<>()).merge(co, got, Double::sum);
                    } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
                }
            }
            // Mid
            for (DatabaseService.StudentMarksData smd : db.getStudentMidMarks(courseCode, programme, academicYear)) {
                String po = midIdToPO.get(smd.questionId); String co = midIdToCO.get(smd.questionId);
                if (po==null || co==null) continue;
                totalRequired++;
                if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentPoTotals.get(smd.studentId).merge(po, got, Double::sum); studentPoCoTotals.get(smd.studentId).computeIfAbsent(po,k-> new HashMap<>()).merge(co, got, Double::sum); } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
            }
            // Final
            for (DatabaseService.StudentMarksData smd : db.getStudentFinalMarks(courseCode, programme, academicYear)) {
                String po = finalIdToPO.get(smd.questionId); String co = finalIdToCO.get(smd.questionId);
                if (po==null || co==null) continue;
                totalRequired++;
                if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentPoTotals.get(smd.studentId).merge(po, got, Double::sum); studentPoCoTotals.get(smd.studentId).computeIfAbsent(po,k-> new HashMap<>()).merge(co, got, Double::sum); } else if (sampleMissing.size()<5) sampleMissing.add(smd.studentId+" -> Q"+smd.questionTitle);
            }

            int missing = totalRequired - graded;
            if (totalRequired == 0) { Alert a = new Alert(Alert.AlertType.INFORMATION, "There are PO-mapped questions but no enrolled students to grade.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }
            if (graded == 0) { Alert a = new Alert(Alert.AlertType.WARNING, "No marks have been entered yet. Please enter marks before generating the PO report.", ButtonType.OK); a.setHeaderText(null); a.showAndWait(); return; }
            if (missing > 0) { Alert a = new Alert(Alert.AlertType.WARNING, String.format("Cannot generate PO report. %d of %d required mark entries are still ungraded. Sample missing: %s", missing, totalRequired, sampleMissing), ButtonType.OK); a.setHeaderText("Incomplete Grading"); a.showAndWait(); return; }

            final double CO_THRESHOLD = 0.60; final double PO_THRESHOLD = 0.40;
            Map<String,Integer> poAttainedCounts = new HashMap<>(); for (String po : poTotal.keySet()) poAttainedCounts.put(po,0);
            for (DatabaseService.StudentData sd : students) {
                Map<String, Double> studentPOMap = studentPoTotals.get(sd.id);
                Map<String, Map<String, Double>> studentPOCOMap = studentPoCoTotals.get(sd.id);
                for (String po : poTotal.keySet()) {
                    double poDenom = poTotal.get(po); if (poDenom <= 0) continue;
                    Map<String,Double> coDenoms = poCoTotals.getOrDefault(po, Collections.emptyMap());
                    boolean anyCOAttained = false;
                    for (String co : coDenoms.keySet()) {
                        double coDenom = coDenoms.get(co); if (coDenom <= 0) continue;
                        double coGot = Optional.ofNullable(studentPOCOMap.get(po)).map(m-> m.getOrDefault(co,0.0)).orElse(0.0);
                        if (coGot / coDenom >= CO_THRESHOLD) { anyCOAttained = true; break; }
                    }
                    if (!anyCOAttained) continue;
                    double poGot = studentPOMap.getOrDefault(po, 0.0);
                    if (poGot / poDenom >= PO_THRESHOLD) poAttainedCounts.merge(po,1,Integer::sum);
                }
            }
            Map<String,Double> percentPerPO = new TreeMap<>(Comparator.comparingInt(p -> { try { return Integer.parseInt(p.replaceAll("[^0-9]", "")); } catch(Exception ex){ return Integer.MAX_VALUE; } }));
            for (String po : poTotal.keySet()) percentPerPO.put(po, poAttainedCounts.getOrDefault(po,0)*100.0 / students.size());

            // Chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Map.Entry<String,Double> e : percentPerPO.entrySet()) dataset.addValue(e.getValue(), "PO Attainment", e.getKey());
            JFreeChart chart = ChartFactory.createBarChart("PO Attainment", "PO", "% Students", dataset);
            CategoryPlot plot = chart.getCategoryPlot();
            if (plot.getRangeAxis() instanceof NumberAxis na) { na.setRange(0.0, 100.0); na.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); }
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream(); ChartUtils.writeChartAsPNG(chartBaos, chart, 640, 400); byte[] chartBytes = chartBaos.toByteArray();

            // PDF output
            File reportsDir = new File("po_reports"); if (!reportsDir.exists()) reportsDir.mkdirs();
            String safeProgramme = selected.getProgramme().replaceAll("[^A-Za-z0-9_-]", "");
            File outFile = new File(reportsDir, courseCode + "_" + academicYear + "_" + safeProgramme + ".pdf");
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(outFile)); PdfDocument pdf = new PdfDocument(writer); Document doc = new Document(pdf)) {
                Paragraph title = new Paragraph(courseCode + " - " + selected.getCourseName()); title.setFontSize(16).setTextAlignment(TextAlignment.CENTER); doc.add(title);
                doc.add(new Paragraph("Department: " + selected.getDepartment()));
                doc.add(new Paragraph("Programme: " + selected.getProgramme()));
                DatabaseService.FacultyInfo fi = UserSession.getCurrentFaculty(); if (fi != null) doc.add(new Paragraph("Faculty: " + fi.fullName + " (" + fi.shortname + ")"));
                doc.add(new Paragraph("Academic Year: " + academicYear));
                doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
                doc.add(new Paragraph("\n"));
                for (Map.Entry<String,Double> e : percentPerPO.entrySet()) doc.add(new Paragraph(e.getKey() + " was successfully attained by " + String.format(Locale.US, "%.2f", e.getValue()) + "% of students"));
                doc.add(new Paragraph("\n"));
                Image chartImg = new Image(ImageDataFactory.create(chartBytes)).setAutoScale(true); doc.add(chartImg);
            }
            Alert done = new Alert(Alert.AlertType.INFORMATION, "PO report generated: " + outFile.getAbsolutePath(), ButtonType.OK); done.setHeaderText(null); done.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to generate PO report: " + ex.getMessage(), ButtonType.OK); err.setHeaderText(null); err.showAndWait();
        }
    }

    public void onExcelExportButton(ActionEvent actionEvent) {
        DatabaseService.FacultyCourseAssignment selected = assignedCoursesTableView != null ? assignedCoursesTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course first.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        String courseCode = selected.getCourseCode();
        String academicYear = selected.getAcademicYear();
        String programme = selected.getProgramme();
        try {
            // Fetch questions for all required assessments
            List<DatabaseService.QuestionData> quiz1 = db.getQuizQuestions(courseCode, programme, 1, academicYear);
            List<DatabaseService.QuestionData> quiz2 = db.getQuizQuestions(courseCode, programme, 2, academicYear);
            List<DatabaseService.QuestionData> quiz3 = db.getQuizQuestions(courseCode, programme, 3, academicYear);
            List<DatabaseService.QuestionData> quiz4 = db.getQuizQuestions(courseCode, programme, 4, academicYear);
            List<DatabaseService.QuestionData> midQuestions = db.getMidQuestions(courseCode, programme, academicYear);
            List<DatabaseService.QuestionData> finalQuestions = db.getFinalQuestions(courseCode, programme, academicYear);

            Map<String, List<DatabaseService.QuestionData>> assessmentQuestions = new LinkedHashMap<>();
            assessmentQuestions.put("Quiz 1", quiz1);
            assessmentQuestions.put("Quiz 2", quiz2);
            assessmentQuestions.put("Quiz 3", quiz3);
            assessmentQuestions.put("Quiz 4", quiz4);
            assessmentQuestions.put("Mid", midQuestions);
            assessmentQuestions.put("Final", finalQuestions);

            List<String> missing = assessmentQuestions.entrySet().stream()
                    .filter(e -> e.getValue() == null || e.getValue().isEmpty())
                    .map(Map.Entry::getKey)
                    .toList();
            if (!missing.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Cannot export. Missing questions for: " + String.join(", ", missing), ButtonType.OK);
                a.setHeaderText("Incomplete Assessments");
                a.showAndWait();
                return;
            }

            // Enrolled students (can be empty; we'll still create template)
            List<DatabaseService.StudentData> students = db.getEnrolledStudents(courseCode, programme, academicYear);

            // Pre-fetch existing marks so export reflects current state (useful if partially graded)
            // Build maps: assessment -> studentId -> questionTitle -> marksObtained
            class MarksMapBuilder {
                Map<String, Map<String, Map<String, Double>>> data = new HashMap<>();
                void put(String assessment, String sid, String qTitle, Double marks) {
                    if (marks == null) return; // keep blank if ungraded
                    data.computeIfAbsent(assessment, k -> new HashMap<>())
                        .computeIfAbsent(sid, k -> new HashMap<>())
                        .put(qTitle, marks);
                }
                Double get(String assessment, String sid, String qTitle) {
                    return Optional.ofNullable(data.get(assessment))
                            .map(m -> m.get(sid))
                            .map(m -> m.get(qTitle)).orElse(null);
                }
            }
            MarksMapBuilder marksBuilder = new MarksMapBuilder();
            // Quizzes marks
            for (int q = 1; q <= 4; q++) {
                String key = "Quiz " + q;
                for (DatabaseService.StudentMarksData smd : db.getStudentQuizMarks(courseCode, programme, q, academicYear)) {
                    marksBuilder.put(key, smd.studentId, smd.questionTitle, smd.marksObtained);
                }
            }
            for (DatabaseService.StudentMarksData smd : db.getStudentMidMarks(courseCode, programme, academicYear)) {
                marksBuilder.put("Mid", smd.studentId, smd.questionTitle, smd.marksObtained);
            }
            for (DatabaseService.StudentMarksData smd : db.getStudentFinalMarks(courseCode, programme, academicYear)) {
                marksBuilder.put("Final", smd.studentId, smd.questionTitle, smd.marksObtained);
            }

            // Create workbook
            File outDir = new File("excel_exports");
            if (!outDir.exists()) outDir.mkdirs();
            String safeProgramme = programme.replaceAll("[^A-Za-z0-9_-]", "");
            File outFile = new File(outDir, courseCode + "_" + academicYear + "_" + safeProgramme + "_marks.xlsx");

            try (Workbook wb = new XSSFWorkbook()) {
                // Styles
                CellStyle headerStyle = wb.createCellStyle();
                Font headerFont = wb.createFont(); headerFont.setBold(true); headerStyle.setFont(headerFont);
                CellStyle numberStyle = wb.createCellStyle();
                DataFormat df = wb.createDataFormat(); numberStyle.setDataFormat(df.getFormat("0.00"));
                CellStyle totalStyle = wb.createCellStyle();
                Font totalFont = wb.createFont(); totalFont.setBold(true); totalStyle.setFont(totalFont); totalStyle.setDataFormat(df.getFormat("0.00"));

                for (Map.Entry<String, List<DatabaseService.QuestionData>> entry : assessmentQuestions.entrySet()) {
                    String assessmentName = entry.getKey();
                    List<DatabaseService.QuestionData> questions = entry.getValue();
                    Sheet sheet = wb.createSheet(assessmentName);

                    // Header row
                    Row header = sheet.createRow(0);
                    int col = 0;
                    header.createCell(col).setCellValue("Student ID"); header.getCell(col).setCellStyle(headerStyle); sheet.setColumnWidth(col++, 15 * 256);
                    header.createCell(col).setCellValue("Name"); header.getCell(col).setCellStyle(headerStyle); sheet.setColumnWidth(col++, 25 * 256);
                    int questionStartCol = col; // remember first question column
                    for (DatabaseService.QuestionData qd : questions) {
                        String title = qd.title + " (" + (qd.marks == (long) qd.marks ? String.format("%d", (long) qd.marks) : String.format(Locale.US, "%.1f", qd.marks)) + ")";
                        header.createCell(col).setCellValue(title);
                        header.getCell(col).setCellStyle(headerStyle);
                        sheet.setColumnWidth(col, Math.min(35 * 256, Math.max(12 * 256, title.length() * 180)));
                        col++;
                    }
                    int totalColIndex = col;
                    double totalMax = questions.stream().mapToDouble(q -> q.marks).sum();
                    header.createCell(totalColIndex).setCellValue("Total (" + (totalMax == (long) totalMax ? String.format("%d", (long) totalMax) : String.format(Locale.US, "%.1f", totalMax)) + ")");
                    header.getCell(totalColIndex).setCellStyle(headerStyle);
                    sheet.setColumnWidth(totalColIndex, 15 * 256);

                    // Data validation (0 .. maxMarks) for each question column
                    if (!questions.isEmpty()) {
                        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                        int lastRow = Math.max(1, students.size() + 100); // allow extra blank rows for future entries
                        for (int qi = 0; qi < questions.size(); qi++) {
                            DatabaseService.QuestionData qd = questions.get(qi);
                            int cIdx = questionStartCol + qi;
                            String upper = (qd.marks == (long) qd.marks) ? String.valueOf((long) qd.marks) : String.valueOf(qd.marks);
                            DataValidationConstraint constraint = dvHelper.createDecimalConstraint(DataValidationConstraint.OperatorType.BETWEEN, "0", upper);
                            CellRangeAddressList range = new CellRangeAddressList(1, lastRow, cIdx, cIdx);
                            DataValidation validation = dvHelper.createValidation(constraint, range);
                            validation.setShowErrorBox(true);
                            validation.createErrorBox("Invalid Marks", "Enter a value between 0 and " + upper + ".");
                            try { sheet.addValidationData(validation); } catch (Exception ignored) { }
                        }
                    }

                    // Data rows
                    int rowIdx = 1;
                    for (DatabaseService.StudentData sd : students) {
                        Row r = sheet.createRow(rowIdx);
                        int c = 0;
                        r.createCell(c).setCellValue(sd.id); c++;
                        r.createCell(c).setCellValue(sd.name); c++;
                        int firstQuestionCol = c;
                        for (DatabaseService.QuestionData qd : questions) {
                            Double val = marksBuilder.get(assessmentName, sd.id, qd.title);
                            if (val != null) {
                                org.apache.poi.ss.usermodel.Cell cell = r.createCell(c); cell.setCellValue(val); cell.setCellStyle(numberStyle);
                            } else {
                                r.createCell(c); // blank
                            }
                            c++;
                        }
                        // Total formula over question columns
                        if (!questions.isEmpty()) {
                            String startCol = CellReference.convertNumToColString(firstQuestionCol);
                            String endCol = CellReference.convertNumToColString(firstQuestionCol + questions.size() - 1);
                            String formula = "SUM(" + startCol + (rowIdx + 1) + ":" + endCol + (rowIdx + 1) + ")"; // Excel rows are 1-based
                            org.apache.poi.ss.usermodel.Cell totalCell = r.createCell(totalColIndex);
                            totalCell.setCellFormula(formula);
                            totalCell.setCellStyle(totalStyle);
                        } else {
                            r.createCell(totalColIndex); // keep blank
                        }
                        rowIdx++;
                    }
                }

                // Write to disk
                try (FileOutputStream fos = new FileOutputStream(outFile)) { wb.write(fos); }
            }
            Alert done = new Alert(Alert.AlertType.INFORMATION, "Excel exported: " + outFile.getAbsolutePath(), ButtonType.OK);
            done.setHeaderText(null);
            done.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to export Excel: " + ex.getMessage(), ButtonType.OK);
            err.setHeaderText(null);
            err.showAndWait();
        }
    }

    public void onExcelImportButton(ActionEvent actionEvent) {
        DatabaseService.FacultyCourseAssignment selected = assignedCoursesTableView != null ? assignedCoursesTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a course first.", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Marks Excel File");
        File initialDir = new File("excel_exports");
        if (initialDir.exists()) fc.setInitialDirectory(initialDir);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showOpenDialog(logoutButton.getScene().getWindow());
        if (file == null) return;
        String courseCode = selected.getCourseCode();
        String academicYear = selected.getAcademicYear();
        String programme = selected.getProgramme();
        int savedCount = 0;
        int skippedInvalid = 0;
        int skippedMissingQuestion = 0;
        int skippedStudent = 0;
        List<String> issues = new ArrayList<>();
        try (org.apache.poi.ss.usermodel.Workbook wb = new XSSFWorkbook(file)) {
            org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            for (int si = 0; si < wb.getNumberOfSheets(); si++) {
                org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(si);
                if (sheet == null) continue;
                String sheetName = sheet.getSheetName().trim();
                // Determine assessment type
                boolean isQuiz = sheetName.startsWith("Quiz ");
                boolean isMid = sheetName.equalsIgnoreCase("Mid");
                boolean isFinal = sheetName.equalsIgnoreCase("Final");
                if (!isQuiz && !isMid && !isFinal) continue; // skip unexpected sheets
                int quizNumber = -1;
                if (isQuiz) {
                    try { quizNumber = Integer.parseInt(sheetName.substring(5).trim()); } catch (NumberFormatException ignored) {}
                    if (quizNumber < 1 || quizNumber > 4) { issues.add("Skipping sheet '"+sheetName+"' (invalid quiz number)"); continue; }
                }
                // Fetch questions for mapping (title -> QuestionData)
                List<DatabaseService.QuestionData> questions = switch (sheetName) {
                    case "Mid" -> db.getMidQuestions(courseCode, programme, academicYear);
                    case "Final" -> db.getFinalQuestions(courseCode, programme, academicYear);
                    default -> db.getQuizQuestions(courseCode, programme, quizNumber, academicYear);
                };
                Map<String, DatabaseService.QuestionData> qTitleMap = new HashMap<>();
                for (DatabaseService.QuestionData qd : questions) qTitleMap.put(qd.title.trim(), qd);
                // Parse header to know columns
                org.apache.poi.ss.usermodel.Row header = sheet.getRow(0);
                if (header == null) { issues.add("Sheet '"+sheetName+"' has no header row; skipped"); continue; }
                List<Integer> questionColIndices = new ArrayList<>();
                List<DatabaseService.QuestionData> questionOrder = new ArrayList<>();
                for (int c = 0; c <= header.getLastCellNum(); c++) {
                    org.apache.poi.ss.usermodel.Cell hc = header.getCell(c);
                    if (hc == null) continue;
                    String text = hc.getStringCellValue();
                    if (text == null) continue;
                    text = text.trim();
                    if (text.equalsIgnoreCase("Student ID") || text.equalsIgnoreCase("Name") || text.startsWith("Total (")) continue;
                    int idx = text.lastIndexOf(" (");
                    String baseTitle = idx > 0 ? text.substring(0, idx) : text; // strip trailing (marks)
                    baseTitle = baseTitle.trim();
                    DatabaseService.QuestionData qd = qTitleMap.get(baseTitle);
                    if (qd == null) { skippedMissingQuestion++; issues.add("Unknown question '"+baseTitle+"' in sheet '"+sheetName+"'"); continue; }
                    questionColIndices.add(c);
                    questionOrder.add(qd);
                }
                if (questionColIndices.isEmpty()) { issues.add("No valid question columns in sheet '"+sheetName+"'"); continue; }
                // Iterate over rows
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                    if (row == null) continue;
                    org.apache.poi.ss.usermodel.Cell sidCell = row.getCell(0);
                    if (sidCell == null) continue;
                    String sid = sidCell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING ? sidCell.getStringCellValue().trim() : null;
                    if (sid == null || sid.isEmpty()) continue; // blank row
                    // Validate that student is enrolled (optional)
                    // We'll attempt to save; DB constraints will enforce presence if any foreign keys exist.
                    for (int qi = 0; qi < questionColIndices.size(); qi++) {
                        int cIdx = questionColIndices.get(qi);
                        DatabaseService.QuestionData qd = questionOrder.get(qi);
                        org.apache.poi.ss.usermodel.Cell mc = row.getCell(cIdx);
                        if (mc == null) continue; // blank => no change
                        Double val = null;
                        try {
                            switch (mc.getCellType()) {
                                case NUMERIC -> val = mc.getNumericCellValue();
                                case FORMULA -> {
                                    try { val = evaluator.evaluate(mc).getNumberValue(); } catch (Exception ignore) {}
                                }
                                case STRING -> {
                                    String s = mc.getStringCellValue().trim();
                                    if (!s.isEmpty()) val = Double.parseDouble(s);
                                }
                                default -> {}
                            }
                        } catch (NumberFormatException nfe) {
                            skippedInvalid++; issues.add("Invalid number at "+sheetName+"!"+(r+1)+" col " + (cIdx+1)); continue;
                        }
                        if (val == null) continue; // no mark entered
                        if (val < 0 || val > qd.marks + 1e-9) { // allow tiny epsilon
                            skippedInvalid++; issues.add(String.format(Locale.US, "Out of range %.2f (0-%.2f) at %s!%d question %s", val, qd.marks, sheetName, r+1, qd.title));
                            continue;
                        }
                        try {
                            if (isMid) {
                                db.saveStudentMidMarks(sid, qd.id, val);
                            } else if (isFinal) {
                                db.saveStudentFinalMarks(sid, qd.id, val);
                            } else { // quiz
                                db.saveStudentQuizMarks(sid, qd.id, val);
                            }
                            savedCount++;
                        } catch (Exception ex) {
                            skippedStudent++; issues.add("DB save failed for student "+sid+" question "+qd.title+" ("+ex.getMessage()+")");
                        }
                    }
                }
            }
            StringBuilder summary = new StringBuilder();
            summary.append("Imported marks saved: ").append(savedCount).append('\n');
            if (skippedInvalid>0) summary.append("Invalid cells skipped: ").append(skippedInvalid).append('\n');
            if (skippedMissingQuestion>0) summary.append("Headers with no matching question: ").append(skippedMissingQuestion).append('\n');
            if (skippedStudent>0) summary.append("Failed student saves: ").append(skippedStudent).append('\n');
            if (!issues.isEmpty()) {
                int max = Math.min(10, issues.size());
                summary.append("Sample issues ("+max+" of "+issues.size()+"):\n");
                for (int i=0;i<max;i++) summary.append(" - ").append(issues.get(i)).append('\n');
            }
            Alert done = new Alert(Alert.AlertType.INFORMATION, summary.toString(), ButtonType.OK);
            done.setHeaderText("Excel Import Summary");
            done.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to import Excel: " + ex.getMessage(), ButtonType.OK);
            err.setHeaderText(null);
            err.showAndWait();
        }
    }
}
