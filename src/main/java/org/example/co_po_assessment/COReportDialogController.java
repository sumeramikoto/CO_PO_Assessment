package org.example.co_po_assessment;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public class COReportDialogController implements Initializable {
    @FXML private Label titleLabel;
    @FXML private Label contextLabel;
    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, String> colCode;
    @FXML private TableColumn<Row, Double> colPercent;
    @FXML private TableColumn<Row, String> colComment;

    private final ObservableList<Row> data = FXCollections.observableArrayList();
    private final DatabaseService db = DatabaseService.getInstance();
    private DatabaseService.FacultyCourseAssignment selected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (table != null) table.setItems(data);
        if (colCode != null) colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        if (colPercent != null) colPercent.setCellValueFactory(new PropertyValueFactory<>("percent"));
        if (colComment != null) {
            colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
            colComment.setCellFactory(TextFieldTableCell.forTableColumn());
            colComment.setOnEditCommit(evt -> evt.getRowValue().setComment(evt.getNewValue()));
        }
        if (table != null) table.setEditable(true);
    }

    public void setContext(DatabaseService.FacultyCourseAssignment selected) {
        this.selected = selected;
        titleLabel.setText("CO Report - " + selected.getCourseCode());
        contextLabel.setText("Programme: " + selected.getProgramme() + " | Year: " + selected.getAcademicYear());
        try { computeAndPopulate(); } catch (Exception e) { showError("Failed to compute: " + e.getMessage()); }
    }

    private void computeAndPopulate() throws Exception {
        data.clear();
        String courseCode = selected.getCourseCode();
        String academicYear = selected.getAcademicYear();
        String programme = selected.getProgramme();

        // Fetch questions
        List<DatabaseService.QuestionData> quiz1 = db.getQuizQuestions(courseCode, programme, 1, academicYear);
        List<DatabaseService.QuestionData> quiz2 = db.getQuizQuestions(courseCode, programme, 2, academicYear);
        List<DatabaseService.QuestionData> quiz3 = db.getQuizQuestions(courseCode, programme, 3, academicYear);
        List<DatabaseService.QuestionData> quiz4 = db.getQuizQuestions(courseCode, programme, 4, academicYear);
        List<DatabaseService.QuestionData> midQuestions = db.getMidQuestions(courseCode, programme, academicYear);
        List<DatabaseService.QuestionData> finalQuestions = db.getFinalQuestions(courseCode, programme, academicYear);

        List<String> missingAssessments = new ArrayList<>();
        List<String> noCOAssessments = new ArrayList<>();
        java.util.function.BiConsumer<String, List<DatabaseService.QuestionData>> classify = (label, list) -> {
            if (list.isEmpty()) missingAssessments.add(label);
            else if (list.stream().noneMatch(q -> q.co != null && !q.co.trim().isEmpty())) noCOAssessments.add(label);
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
            msg.append("Add & map all assessments before viewing the CO report.");
            throw new IllegalStateException(msg.toString());
        }

        List<DatabaseService.QuestionData> quizQuestionsAll = new ArrayList<>();
        quizQuestionsAll.addAll(quiz1); quizQuestionsAll.addAll(quiz2); quizQuestionsAll.addAll(quiz3); quizQuestionsAll.addAll(quiz4);

        Map<String, Double> coTotal = new HashMap<>();
        java.util.function.Consumer<DatabaseService.QuestionData> addQ = q -> {
            if (q.co == null) return; String co = q.co.trim().toUpperCase(); if (co.isEmpty()) return; coTotal.merge(co, q.marks, Double::sum);
        };
        quizQuestionsAll.forEach(addQ); midQuestions.forEach(addQ); finalQuestions.forEach(addQ);
        if (coTotal.isEmpty()) throw new IllegalStateException("Questions exist but none have CO mappings.");

        List<DatabaseService.StudentData> students = db.getEnrolledStudents(courseCode, programme, academicYear);
        if (students.isEmpty()) throw new IllegalStateException("No students enrolled for this course in the selected academic year.");

        Map<Integer,String> quizIdToCO = new HashMap<>(); for (DatabaseService.QuestionData qd : quizQuestionsAll) if (qd.co!=null) quizIdToCO.put(qd.id, qd.co.trim().toUpperCase());
        Map<Integer,String> midIdToCO = new HashMap<>(); for (DatabaseService.QuestionData qd : midQuestions) if (qd.co!=null) midIdToCO.put(qd.id, qd.co.trim().toUpperCase());
        Map<Integer,String> finalIdToCO = new HashMap<>(); for (DatabaseService.QuestionData qd : finalQuestions) if (qd.co!=null) finalIdToCO.put(qd.id, qd.co.trim().toUpperCase());

        int totalRequired = 0; int graded = 0;
        Map<String, Map<String, Double>> studentCOObtained = new HashMap<>();
        for (DatabaseService.StudentData sd : students) studentCOObtained.put(sd.id, new HashMap<>());

        for (int quizNum=1; quizNum<=4; quizNum++) {
            for (DatabaseService.StudentMarksData smd : db.getStudentQuizMarks(courseCode, programme, quizNum, academicYear)) {
                String co = quizIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); }
            }
        }
        for (DatabaseService.StudentMarksData smd : db.getStudentMidMarks(courseCode, programme, academicYear)) {
            String co = midIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); }
        }
        for (DatabaseService.StudentMarksData smd : db.getStudentFinalMarks(courseCode, programme, academicYear)) {
            String co = finalIdToCO.get(smd.questionId); if (co==null) continue; totalRequired++; if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentCOObtained.get(smd.studentId).merge(co, got, Double::sum); }
        }
        if (totalRequired == 0) throw new IllegalStateException("There are CO-mapped questions but no enrolled students to grade.");
        if (graded == 0) throw new IllegalStateException("No marks have been entered yet. Please enter marks before viewing the CO report.");
        if (graded < totalRequired) throw new IllegalStateException("Cannot view CO report. Some required mark entries are still ungraded.");

        final double THRESHOLD = 0.60;
        Map<String,Integer> attainedCounts = new HashMap<>(); for (String co: coTotal.keySet()) attainedCounts.put(co,0);
        for (DatabaseService.StudentData sd : students) {
            Map<String,Double> gotMap = studentCOObtained.get(sd.id);
            for (String co : coTotal.keySet()) { double denom = coTotal.get(co); if (denom <= 0) continue; double got = gotMap.getOrDefault(co,0.0); if (got/denom >= THRESHOLD) attainedCounts.merge(co,1,Integer::sum); }
        }
        Map<String,Double> percentPerCO = new TreeMap<>(Comparator.comparingInt(c -> { try { return Integer.parseInt(c.replaceAll("[^0-9]", "")); } catch(Exception ex){ return Integer.MAX_VALUE; } }));
        for (String co: coTotal.keySet()) percentPerCO.put(co, attainedCounts.getOrDefault(co,0)*100.0 / students.size());

        // Populate table rows, keep sorted by CO code
        for (Map.Entry<String,Double> e : percentPerCO.entrySet()) data.add(new Row(e.getKey(), e.getValue(), ""));
    }

    @FXML
    public void onCancel(ActionEvent e) { close(); }

    @FXML
    public void onSave(ActionEvent e) {
        try {
            // Build dataset and chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Row r : data) dataset.addValue(r.getPercent(), "CO Attainment", r.getCode());
            JFreeChart chart = ChartFactory.createBarChart("CO Attainment", "CO", "% Students", dataset);
            CategoryPlot plot = chart.getCategoryPlot();
            if (plot.getRangeAxis() instanceof NumberAxis na) { na.setRange(0.0, 100.0); na.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); }
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(chartBaos, chart, 640, 400); byte[] chartBytes = chartBaos.toByteArray();

            // Write PDF including comments
            File reportsDir = new File("co_reports"); if (!reportsDir.exists()) reportsDir.mkdirs();
            String safeProgramme = selected.getProgramme().replaceAll("[^A-Za-z0-9_-]", "");
            File outFile = new File(reportsDir, selected.getCourseCode() + "_" + selected.getAcademicYear() + "_" + safeProgramme + ".pdf");
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(outFile)); PdfDocument pdf = new PdfDocument(writer); Document doc = new Document(pdf)) {
                Paragraph title = new Paragraph(selected.getCourseCode() + " - " + selected.getCourseName());
                title.setFontSize(16).setTextAlignment(TextAlignment.CENTER); doc.add(title);
                doc.add(new Paragraph("Department: " + selected.getDepartment()));
                doc.add(new Paragraph("Programme: " + selected.getProgramme()));
                DatabaseService.FacultyInfo fi = UserSession.getCurrentFaculty(); if (fi != null) doc.add(new Paragraph("Faculty: " + fi.fullName + " (" + fi.shortname + ")"));
                doc.add(new Paragraph("Academic Year: " + selected.getAcademicYear()));
                doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
                doc.add(new Paragraph("\n"));
                for (Row r : data) {
                    String line = r.getCode() + " attained by " + String.format(Locale.US, "%.2f", r.getPercent()) + "% of students";
                    doc.add(new Paragraph(line));
                    String comment = Optional.ofNullable(r.getComment()).orElse("").trim();
                    if (!comment.isEmpty()) doc.add(new Paragraph(" - Comment: " + comment));
                }
                doc.add(new Paragraph("\n"));
                Image chartImg = new Image(ImageDataFactory.create(chartBytes)).setAutoScale(true); doc.add(chartImg);
            }
            Alert done = new Alert(Alert.AlertType.INFORMATION, "CO report saved: " + outFile.getAbsolutePath(), ButtonType.OK);
            done.setHeaderText(null); done.showAndWait();
            close();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to save PDF: " + ex.getMessage());
        }
    }

    private void close() { ((Stage) table.getScene().getWindow()).close(); }
    private void showError(String msg) { new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait(); }

    public static class Row {
        private final SimpleStringProperty code = new SimpleStringProperty();
        private final SimpleDoubleProperty percent = new SimpleDoubleProperty();
        private final SimpleStringProperty comment = new SimpleStringProperty();
        public Row(String code, double percent, String comment) { this.code.set(code); this.percent.set(percent); this.comment.set(comment); }
        public String getCode() { return code.get(); }
        public void setCode(String v) { code.set(v); }
        public double getPercent() { return percent.get(); }
        public void setPercent(double v) { percent.set(v); }
        public String getComment() { return comment.get(); }
        public void setComment(String v) { comment.set(v); }
    }
}

