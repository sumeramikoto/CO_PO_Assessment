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

// Added for table output in PDF
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
// Use UnitValue for percentage width
import com.itextpdf.layout.properties.UnitValue;

public class POReportDialogController implements Initializable {
    @FXML private Label titleLabel;
    @FXML private Label contextLabel;
    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, String> colCode;
    @FXML private TableColumn<Row, Double> colPercent;
    @FXML private TableColumn<Row, String> colComment;
    // New column for Possible Steps
    @FXML private TableColumn<Row, String> colSteps;

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
        // Setup Possible Steps column
        if (colSteps != null) {
            colSteps.setCellValueFactory(new PropertyValueFactory<>("steps"));
            colSteps.setCellFactory(TextFieldTableCell.forTableColumn());
            colSteps.setOnEditCommit(evt -> evt.getRowValue().setSteps(evt.getNewValue()));
        }
        if (table != null) table.setEditable(true);
    }

    public void setContext(DatabaseService.FacultyCourseAssignment selected) {
        this.selected = selected;
        titleLabel.setText("PO Report - " + selected.getCourseCode());
        contextLabel.setText("Programme: " + selected.getProgramme() + " | Year: " + selected.getAcademicYear());
        try { computeAndPopulate(); } catch (Exception e) { showError("Failed to compute: " + e.getMessage()); }
    }

    private void computeAndPopulate() throws Exception {
        data.clear();
        String courseCode = selected.getCourseCode();
        String academicYear = selected.getAcademicYear();
        String programme = selected.getProgramme();

        List<DatabaseService.QuestionData> quiz1 = db.getQuizQuestions(courseCode, programme, 1, academicYear);
        List<DatabaseService.QuestionData> quiz2 = db.getQuizQuestions(courseCode, programme, 2, academicYear);
        List<DatabaseService.QuestionData> quiz3 = db.getQuizQuestions(courseCode, programme, 3, academicYear);
        List<DatabaseService.QuestionData> quiz4 = db.getQuizQuestions(courseCode, programme, 4, academicYear);
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
            msg.append("Add & map all assessments before viewing the PO report.");
            throw new IllegalStateException(msg.toString());
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
        if (poTotal.isEmpty()) throw new IllegalStateException("Questions exist but none have PO mappings.");

        List<DatabaseService.StudentData> students = db.getEnrolledStudents(courseCode, programme, academicYear);
        if (students.isEmpty()) throw new IllegalStateException("No students enrolled for this course in the selected academic year.");

        Map<Integer,String> quizIdToPO = new HashMap<>(); Map<Integer,String> quizIdToCO = new HashMap<>();
        for (DatabaseService.QuestionData qd : quizQuestionsAll) { if (qd.po!=null) quizIdToPO.put(qd.id, qd.po.trim().toUpperCase()); if (qd.co!=null) quizIdToCO.put(qd.id, qd.co.trim().toUpperCase()); }
        Map<Integer,String> midIdToPO = new HashMap<>(); Map<Integer,String> midIdToCO = new HashMap<>();
        for (DatabaseService.QuestionData qd : midQuestions) { if (qd.po!=null) midIdToPO.put(qd.id, qd.po.trim().toUpperCase()); if (qd.co!=null) midIdToCO.put(qd.id, qd.co.trim().toUpperCase()); }
        Map<Integer,String> finalIdToPO = new HashMap<>(); Map<Integer,String> finalIdToCO = new HashMap<>();
        for (DatabaseService.QuestionData qd : finalQuestions) { if (qd.po!=null) finalIdToPO.put(qd.id, qd.po.trim().toUpperCase()); if (qd.co!=null) finalIdToCO.put(qd.id, qd.co.trim().toUpperCase()); }

        int totalRequired = 0; int graded = 0;
        Map<String, Map<String, Double>> studentPoTotals = new HashMap<>();
        Map<String, Map<String, Map<String, Double>>> studentPoCoTotals = new HashMap<>();
        for (DatabaseService.StudentData sd : students) { studentPoTotals.put(sd.id, new HashMap<>()); studentPoCoTotals.put(sd.id, new HashMap<>()); }

        for (int quizNum=1; quizNum<=4; quizNum++) {
            for (DatabaseService.StudentMarksData smd : db.getStudentQuizMarks(courseCode, programme, quizNum, academicYear)) {
                String po = quizIdToPO.get(smd.questionId); String co = quizIdToCO.get(smd.questionId);
                if (po==null || co==null) continue;
                totalRequired++;
                if (smd.marksObtained != null) {
                    graded++; double got = smd.marksObtained;
                    studentPoTotals.get(smd.studentId).merge(po, got, Double::sum);
                    studentPoCoTotals.get(smd.studentId).computeIfAbsent(po,k-> new HashMap<>()).merge(co, got, Double::sum);
                }
            }
        }
        for (DatabaseService.StudentMarksData smd : db.getStudentMidMarks(courseCode, programme, academicYear)) {
            String po = midIdToPO.get(smd.questionId); String co = midIdToCO.get(smd.questionId);
            if (po==null || co==null) continue;
            totalRequired++;
            if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentPoTotals.get(smd.studentId).merge(po, got, Double::sum); studentPoCoTotals.get(smd.studentId).computeIfAbsent(po,k-> new HashMap<>()).merge(co, got, Double::sum); }
        }
        for (DatabaseService.StudentMarksData smd : db.getStudentFinalMarks(courseCode, programme, academicYear)) {
            String po = finalIdToPO.get(smd.questionId); String co = finalIdToCO.get(smd.questionId);
            if (po==null || co==null) continue;
            totalRequired++;
            if (smd.marksObtained != null) { graded++; double got = smd.marksObtained; studentPoTotals.get(smd.studentId).merge(po, got, Double::sum); studentPoCoTotals.get(smd.studentId).computeIfAbsent(po,k-> new HashMap<>()).merge(co, got, Double::sum); }
        }

        if (totalRequired == 0) throw new IllegalStateException("There are PO-mapped questions but no enrolled students to grade.");
        if (graded == 0) throw new IllegalStateException("No marks have been entered yet. Please enter marks before viewing the PO report.");
        if (graded < totalRequired) throw new IllegalStateException("Cannot view PO report. Some required mark entries are still ungraded.");

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

        for (Map.Entry<String,Double> e : percentPerPO.entrySet()) data.add(new Row(e.getKey(), e.getValue(), "", ""));
    }

    @FXML public void onCancel(ActionEvent e) { close(); }

    @FXML
    public void onSave(ActionEvent e) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (Row r : data) dataset.addValue(r.getPercent(), "PO Attainment", r.getCode());
            JFreeChart chart = ChartFactory.createBarChart("PO Attainment", "PO", "% Students", dataset);
            CategoryPlot plot = chart.getCategoryPlot(); if (plot.getRangeAxis() instanceof NumberAxis na) { na.setRange(0.0, 100.0); na.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); }
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream(); ChartUtils.writeChartAsPNG(chartBaos, chart, 640, 400); byte[] chartBytes = chartBaos.toByteArray();

            File reportsDir = new File("po_reports"); if (!reportsDir.exists()) reportsDir.mkdirs();
            String safeProgramme = selected.getProgramme().replaceAll("[^A-Za-z0-9_-]", "");
            File outFile = new File(reportsDir, selected.getCourseCode() + "_" + selected.getAcademicYear() + "_" + safeProgramme + ".pdf");
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(outFile)); PdfDocument pdf = new PdfDocument(writer); Document doc = new Document(pdf)) {
                Paragraph title = new Paragraph(selected.getCourseCode() + " - " + selected.getCourseName()); title.setFontSize(16).setTextAlignment(TextAlignment.CENTER); doc.add(title);
                doc.add(new Paragraph("Department: " + selected.getDepartment()));
                doc.add(new Paragraph("Programme: " + selected.getProgramme()));
                DatabaseService.FacultyInfo fi = UserSession.getCurrentFaculty(); if (fi != null) doc.add(new Paragraph("Faculty: " + fi.fullName + " (" + fi.shortname + ")"));
                doc.add(new Paragraph("Academic Year: " + selected.getAcademicYear()));
                doc.add(new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
                doc.add(new Paragraph("\n"));
                for (Row r : data) {
                    doc.add(new Paragraph(r.getCode() + " attained by " + String.format(Locale.US, "%.2f", r.getPercent()) + "% of students"));
                    String comment = Optional.ofNullable(r.getComment()).orElse("").trim();
                    if (!comment.isEmpty()) doc.add(new Paragraph(" - Comment: " + comment));
                    String stepsTxt = Optional.ofNullable(r.getSteps()).orElse("").trim();
                    if (!stepsTxt.isEmpty()) doc.add(new Paragraph(" - Possible Steps: " + stepsTxt));
                }
                doc.add(new Paragraph("\n"));
                Image chartImg = new Image(ImageDataFactory.create(chartBytes)).setAutoScale(true); doc.add(chartImg);
                // Add summary table at the bottom
                doc.add(new Paragraph("\n"));
                Table tbl = new Table(new float[]{2f, 2.5f, 5f, 5f});
                // Set table width to 100%
                tbl.setWidth(UnitValue.createPercentValue(100));
                tbl.addHeaderCell(new Cell().add(new Paragraph("PO")));
                tbl.addHeaderCell(new Cell().add(new Paragraph("Attainment %")));
                tbl.addHeaderCell(new Cell().add(new Paragraph("Comment")));
                tbl.addHeaderCell(new Cell().add(new Paragraph("Possible Steps")));
                for (Row r : data) {
                    tbl.addCell(new Cell().add(new Paragraph(r.getCode())));
                    tbl.addCell(new Cell().add(new Paragraph(String.format(Locale.US, "%.2f", r.getPercent()))));
                    tbl.addCell(new Cell().add(new Paragraph(Optional.ofNullable(r.getComment()).orElse(""))));
                    tbl.addCell(new Cell().add(new Paragraph(Optional.ofNullable(r.getSteps()).orElse(""))));
                }
                doc.add(tbl);
            }
            new Alert(Alert.AlertType.INFORMATION, "PO report saved: " + outFile.getAbsolutePath(), ButtonType.OK).showAndWait();
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
        // New property for Possible Steps
        private final SimpleStringProperty steps = new SimpleStringProperty();
        public Row(String code, double percent, String comment, String steps) { this.code.set(code); this.percent.set(percent); this.comment.set(comment); this.steps.set(steps); }
        public String getCode() { return code.get(); }
        public void setCode(String v) { code.set(v); }
        public double getPercent() { return percent.get(); }
        public void setPercent(double v) { percent.set(v); }
        public String getComment() { return comment.get(); }
        public void setComment(String v) { comment.set(v); }
        public String getSteps() { return steps.get(); }
        public void setSteps(String v) { steps.set(v); }
    }
}
