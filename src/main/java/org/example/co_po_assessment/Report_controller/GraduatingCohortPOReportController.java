package org.example.co_po_assessment.Report_controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.co_po_assessment.DB_helper.DatabaseService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// Chart + PDF deps
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.*;

public class GraduatingCohortPOReportController implements Initializable {
    @FXML private ComboBox<String> programmeCombo;
    @FXML private ComboBox<Integer> batchCombo;
    @FXML private ListView<String> studentsList;
    @FXML private TableView<Row> resultsTable;
    @FXML private TableColumn<Row, String> poCol;
    @FXML private TableColumn<Row, Double> percentCol;
    @FXML private TextArea statusArea;

    private final DatabaseService db = DatabaseService.getInstance();
    private final ObservableList<Row> results = FXCollections.observableArrayList();

    private double poThreshold = 0.40; // default 40%

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (resultsTable != null) resultsTable.setItems(results);
        if (poCol != null) poCol.setCellValueFactory(new PropertyValueFactory<>("po"));
        if (percentCol != null) percentCol.setCellValueFactory(new PropertyValueFactory<>("percent"));
        if (statusArea != null) statusArea.setEditable(false);

        try {
            Map<String, Double> t = db.getThresholds();
            this.poThreshold = t.getOrDefault("PO_INDIVIDUAL", 40.0) / 100.0;
        } catch (Exception ignored) { }

        try {
            List<String> programmes = db.getDistinctProgrammesFromStudents();
            programmeCombo.getItems().setAll(programmes);
            programmeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> onProgrammeSelected(newV));
        } catch (Exception ex) {
            appendStatus("Failed to load programmes: " + ex.getMessage());
        }
    }

    private void onProgrammeSelected(String programme) {
        batchCombo.getItems().clear();
        studentsList.getItems().clear();
        results.clear();
        if (programme == null || programme.isBlank()) return;
        try {
            List<Integer> batches = db.getBatchesForProgramme(programme);
            batchCombo.getItems().setAll(batches);
        } catch (Exception ex) { appendStatus("Failed to load batches: " + ex.getMessage()); }
    }

    @FXML
    public void onLoadCohort(ActionEvent e) {
        results.clear();
        studentsList.getItems().clear();
        statusArea.clear();
        String programme = programmeCombo.getValue();
        Integer batch = batchCombo.getValue();
        if (programme == null || batch == null) { appendStatus("Select programme and batch first."); return; }
        try {
            List<String> ids = db.getGraduatingStudentIds(programme, batch);
            if (ids.isEmpty()) {
                appendStatus("No graduating students flagged for programme=" + programme + ", batch=" + batch + ". Use 'Manage Graduating Students' first.");
                return;
            }
            List<DatabaseService.StudentData> students = db.getStudentsByIds(ids);
            for (DatabaseService.StudentData s : students) {
                studentsList.getItems().add(s.id + " - " + s.name);
            }
            appendStatus("Loaded cohort: " + students.size() + " students.");
        } catch (Exception ex) {
            appendStatus("Failed to load cohort: " + ex.getMessage());
        }
    }

    @FXML
    public void onGenerate(ActionEvent e) {
        results.clear();
        statusArea.clear();
        String programme = programmeCombo.getValue();
        Integer batch = batchCombo.getValue();
        if (programme == null || batch == null) { appendStatus("Select programme and batch first."); return; }
        List<String> studentIds;
        try { studentIds = db.getGraduatingStudentIds(programme, batch); }
        catch (Exception ex) { appendStatus("Failed to fetch graduating students: " + ex.getMessage()); return; }
        if (studentIds.isEmpty()) { appendStatus("No graduating students found for the selected cohort."); return; }

        // 1) Culmination courses must exist for the programme
        List<String> culminationCourses;
        try { culminationCourses = db.getCulminationCourses(programme); }
        catch (Exception ex) { appendStatus("Failed to load culmination courses: " + ex.getMessage()); return; }
        if (culminationCourses.isEmpty()) { appendStatus("No culmination courses configured for programme '" + programme + "'. Configure them first."); return; }

        // Collect issues as we validate
        List<String> issues = new ArrayList<>();

        // Cache per course: allowed POs and per-academic-year question sets and totals
        Map<String, List<String>> courseAllowedPOs = new HashMap<>(); // course -> list of POs allowed (optional)
        Map<String, Map<String, CourseAssessmentBundle>> courseYearBundle = new HashMap<>(); // course -> ay -> bundle

        try {
            for (String course : culminationCourses) {
                courseAllowedPOs.put(course, db.getAllowedPOsForCourse(course, programme));
            }
        } catch (Exception ex) {
            appendStatus("Failed to load allowed POs for courses: " + ex.getMessage());
            return;
        }

        // For cohort aggregation
        Map<String, Integer> poAttainedCounts = new HashMap<>();
        Set<String> allPOsSeen = new TreeSet<>(Comparator.comparingInt(p -> {
            try { return Integer.parseInt(p.replaceAll("[^0-9]", "")); } catch(Exception ex){ return Integer.MAX_VALUE; }
        }));

        for (String sid : studentIds) {
            boolean studentHasBlockingIssue = false;
            // For student-level PO attained across courses (OR across courses)
            Set<String> studentAttainedPOs = new HashSet<>();

            for (String course : culminationCourses) {
                // Determine student's academic year of enrollment for this course
                String ay;
                try { ay = db.getEnrollmentYearForStudentInCourse(sid, course, programme); }
                catch (Exception ex) { issues.add("["+sid+"] Failed to check enrollment for course " + course + ": " + ex.getMessage()); studentHasBlockingIssue = true; continue; }
                if (ay == null) { issues.add("["+sid+"] Not enrolled in culmination course " + course); studentHasBlockingIssue = true; continue; }

                // Build or fetch course-year assessment bundle (questions, PO totals, questionId->PO)
                CourseAssessmentBundle bundle;
                courseYearBundle.computeIfAbsent(course, k -> new HashMap<>());
                Map<String, CourseAssessmentBundle> byYear = courseYearBundle.get(course);
                if (byYear.containsKey(ay)) {
                    bundle = byYear.get(ay);
                } else {
                    try {
                        List<DatabaseService.QuestionData> q1 = db.getQuizQuestions(course, programme, 1, ay);
                        List<DatabaseService.QuestionData> q2 = db.getQuizQuestions(course, programme, 2, ay);
                        List<DatabaseService.QuestionData> q3 = db.getQuizQuestions(course, programme, 3, ay);
                        List<DatabaseService.QuestionData> q4 = db.getQuizQuestions(course, programme, 4, ay);
                        List<DatabaseService.QuestionData> midQ = db.getMidQuestions(course, programme, ay);
                        List<DatabaseService.QuestionData> finQ = db.getFinalQuestions(course, programme, ay);

                        List<String> missingAssessments = new ArrayList<>();
                        List<String> noPOAssessments = new ArrayList<>();
                        java.util.function.BiConsumer<String,List<DatabaseService.QuestionData>> classify = (label,list)-> {
                            if (list.isEmpty()) missingAssessments.add(label);
                            else if (list.stream().noneMatch(q-> q.po!=null && !q.po.trim().isEmpty())) noPOAssessments.add(label);
                        };
                        classify.accept("Quiz 1", q1);
                        classify.accept("Quiz 2", q2);
                        classify.accept("Quiz 3", q3);
                        classify.accept("Quiz 4", q4);
                        classify.accept("Mid", midQ);
                        classify.accept("Final", finQ);
                        if (!missingAssessments.isEmpty() || !noPOAssessments.isEmpty()) {
                            if (!missingAssessments.isEmpty()) issues.add("Course "+course+" ("+ay+") has no questions for: " + String.join(", ", missingAssessments));
                            if (!noPOAssessments.isEmpty()) issues.add("Course "+course+" ("+ay+") has questions but none mapped to POs for: " + String.join(", ", noPOAssessments));
                            // Don't return; we'll still cache bundle to prevent repeat DB calls
                        }

                        List<DatabaseService.QuestionData> all = new ArrayList<>();
                        all.addAll(q1); all.addAll(q2); all.addAll(q3); all.addAll(q4);
                        all.addAll(midQ); all.addAll(finQ);
                        Map<String, Double> poTotals = new HashMap<>();
                        Map<Integer, String> idToPO = new HashMap<>();
                        for (DatabaseService.QuestionData qd : all) {
                            if (qd.po == null || qd.po.isBlank()) continue;
                            String po = qd.po.trim().toUpperCase();
                            poTotals.merge(po, qd.marks, Double::sum);
                            idToPO.put(qd.id, po);
                        }
                        bundle = new CourseAssessmentBundle(q1,q2,q3,q4,midQ,finQ,poTotals,idToPO);
                        byYear.put(ay, bundle);
                    } catch (Exception ex) {
                        issues.add("["+sid+"] Failed to load assessment structure for course "+course+" ("+ay+"): "+ex.getMessage());
                        studentHasBlockingIssue = true; continue;
                    }
                }

                // If this course-year has no PO-mapped questions, it's a blocking issue
                if (bundle.poTotals.isEmpty()) { studentHasBlockingIssue = true; continue; }

                // Now load marks and ensure all required entries are graded for this student for PO-mapped questions
                int required = 0; int graded = 0;
                Map<String, Double> studentCoursePoObtained = new HashMap<>();
                try {
                    for (int qn = 1; qn <= 4; qn++) {
                        List<DatabaseService.StudentMarksData> marks = db.getStudentQuizMarks(course, programme, qn, ay);
                        for (DatabaseService.StudentMarksData m : marks) {
                            if (!sid.equals(m.studentId)) continue;
                            String po = bundle.idToPO.get(m.questionId);
                            if (po == null || po.isBlank()) continue; // only PO mapped questions
                            required++;
                            if (m.marksObtained != null) {
                                graded++;
                                studentCoursePoObtained.merge(po, m.marksObtained, Double::sum);
                            }
                        }
                    }
                    for (DatabaseService.StudentMarksData m : db.getStudentMidMarks(course, programme, ay)) {
                        if (!sid.equals(m.studentId)) continue;
                        String po = bundle.idToPO.get(m.questionId);
                        if (po == null || po.isBlank()) continue;
                        required++;
                        if (m.marksObtained != null) {
                            graded++;
                            studentCoursePoObtained.merge(po, m.marksObtained, Double::sum);
                        }
                    }
                    for (DatabaseService.StudentMarksData m : db.getStudentFinalMarks(course, programme, ay)) {
                        if (!sid.equals(m.studentId)) continue;
                        String po = bundle.idToPO.get(m.questionId);
                        if (po == null || po.isBlank()) continue;
                        required++;
                        if (m.marksObtained != null) {
                            graded++;
                            studentCoursePoObtained.merge(po, m.marksObtained, Double::sum);
                        }
                    }
                } catch (Exception ex) {
                    issues.add("["+sid+"] Failed to load marks for course "+course+" ("+ay+"): "+ex.getMessage());
                    studentHasBlockingIssue = true; continue;
                }

                if (required == 0) { issues.add("["+sid+"] No PO-mapped questions found in course "+course+" ("+ay+")"); studentHasBlockingIssue = true; continue; }
                if (graded < required) { issues.add("["+sid+"] Not all marks graded in course "+course+" ("+ay+") [graded "+graded+" / required "+required+"]"); studentHasBlockingIssue = true; continue; }

                // Compute PO attainment for this student in this course-year, add to overall student set (OR across courses)
                for (Map.Entry<String, Double> e2 : bundle.poTotals.entrySet()) {
                    String po = e2.getKey();
                    double denom = e2.getValue();
                    if (denom <= 0) continue;
                    double got = studentCoursePoObtained.getOrDefault(po, 0.0);
                    if (got / denom >= poThreshold) studentAttainedPOs.add(po);
                }

                // Track all POs seen from questions
                allPOsSeen.addAll(bundle.poTotals.keySet());
            }

            // If student has any blocking issue, we skip counting for them entirely
            if (studentHasBlockingIssue) continue;

            // Count attained POs for this student
            for (String po : studentAttainedPOs) {
                poAttainedCounts.merge(po, 1, Integer::sum);
            }
        }

        // If any issues gathered, display and abort aggregation output
        if (!issues.isEmpty()) {
            appendStatus("Validation failed. Resolve the following before generating the cohort report:\n" + String.join("\n", issues));
            results.clear();
            return;
        }

        // Aggregate percentages across cohort
        int cohortSize = studentIds.size();
        if (cohortSize == 0) { appendStatus("Empty cohort."); return; }
        // Sort POs numerically
        List<String> sortedPOs = new ArrayList<>(allPOsSeen);
        sortedPOs.sort(Comparator.comparingInt(p -> { try { return Integer.parseInt(p.replaceAll("[^0-9]", "")); } catch(Exception ex){ return Integer.MAX_VALUE; } }));

        for (String po : sortedPOs) {
            int attained = poAttainedCounts.getOrDefault(po, 0);
            double pct = (attained * 100.0) / cohortSize;
            results.add(new Row(po, pct));
        }
        appendStatus("Generated cohort result in UI. Creating PDF...");

        try {
            File pdf = saveCohortPdf(programme, batch, results, cohortSize);
            appendStatus("PDF saved: " + pdf.getAbsolutePath());
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "Cohort PO report saved:\n" + pdf.getAbsolutePath(), ButtonType.OK);
            ok.setHeaderText("Report created");
            ok.showAndWait();
        } catch (Exception ex) {
            appendStatus("Failed to save PDF: " + ex.getMessage());
            new Alert(Alert.AlertType.ERROR, "Failed to save PDF: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private File saveCohortPdf(String programme, int batch, List<Row> rows, int cohortSize) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Row r : rows) dataset.addValue(r.getPercent(), "PO Attainment", r.getPo());
        
        JFreeChart chart = ChartFactory.createBarChart(
            "Graduating Cohort PO Attainment",
            "Program Outcomes",
            "Percentage of Students Achieved (%)",
            dataset,
            PlotOrientation.VERTICAL,
            false,  // legend
            true,   // tooltips
            false   // urls
        );
        
        // Professional styling
        chart.setBackgroundPaint(Color.WHITE);
        chart.setAntiAlias(true);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 20));
        chart.setPadding(new org.jfree.chart.ui.RectangleInsets(15, 15, 15, 15));
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 250, 252));
        plot.setDomainGridlinePaint(new Color(226, 232, 240));
        plot.setRangeGridlinePaint(new Color(226, 232, 240));
        plot.setOutlineVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(false);
        
        // Style axes
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 13));
        domainAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));
        domainAxis.setTickLabelPaint(new Color(51, 65, 85));
        domainAxis.setLabelPaint(new Color(30, 41, 59));
        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 100.0);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 13));
        rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));
        rangeAxis.setTickLabelPaint(new Color(51, 65, 85));
        rangeAxis.setLabelPaint(new Color(30, 41, 59));
        
        // Modern bar styling with gradient
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new GradientPaint(
            0.0f, 0.0f, new Color(16, 185, 129),   // Emerald 500
            0.0f, 400.0f, new Color(5, 150, 105)   // Emerald 600
        ));
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.12);  // Space between bars
        renderer.setMaximumBarWidth(0.07);  // Bar width control
        
        // Higher resolution output
        ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartBaos, chart, 1000, 600, true, 9);
        byte[] chartBytes = chartBaos.toByteArray();

        File reportsDir = new File("cohort_po_reports");
        if (!reportsDir.exists()) reportsDir.mkdirs();
        String safeProgramme = programme.replaceAll("[^A-Za-z0-9_-]", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        File outFile = new File(reportsDir, "CohortPO_" + safeProgramme + "_Batch" + batch + "_" + timestamp + ".pdf");

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(outFile));
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {
            Paragraph title = new Paragraph("Graduating Cohort PO Report");
            title.setTextAlignment(TextAlignment.CENTER).setFontSize(16);
            doc.add(title);
            doc.add(new Paragraph("Programme: " + programme));
            doc.add(new Paragraph("Batch: " + batch));
            doc.add(new Paragraph("Cohort size: " + cohortSize));
            doc.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            doc.add(new Paragraph(" "));

            Image chartImg = new Image(ImageDataFactory.create(chartBytes)).setAutoScale(true);
            doc.add(chartImg);
            doc.add(new Paragraph(" "));

            Table tbl = new Table(new float[]{2f, 3f});
            tbl.setWidth(UnitValue.createPercentValue(50));
            tbl.addHeaderCell(new Cell().add(new Paragraph("PO")));
            tbl.addHeaderCell(new Cell().add(new Paragraph("Attainment %")));
            for (Row r : rows) {
                tbl.addCell(new Cell().add(new Paragraph(r.getPo())));
                tbl.addCell(new Cell().add(new Paragraph(String.format(java.util.Locale.US, "%.2f", r.getPercent()))));
            }
            doc.add(tbl);
        }
        return outFile;
    }

    private void appendStatus(String s) { if (statusArea != null) { if (!statusArea.getText().isEmpty()) statusArea.appendText("\n"); statusArea.appendText(s); } }

    public static class Row {
        private final SimpleStringProperty po = new SimpleStringProperty();
        private final SimpleDoubleProperty percent = new SimpleDoubleProperty();
        public Row(String po, double percent) { this.po.set(po); this.percent.set(percent); }
        public String getPo() { return po.get(); }
        public double getPercent() { return percent.get(); }
    }

    // helper holder for cached course-year data
    private static class CourseAssessmentBundle {
        final List<DatabaseService.QuestionData> quiz1, quiz2, quiz3, quiz4, mid, fin;
        final Map<String, Double> poTotals; // PO -> total marks available in course-year
        final Map<Integer, String> idToPO; // questionId -> PO
        CourseAssessmentBundle(List<DatabaseService.QuestionData> q1, List<DatabaseService.QuestionData> q2, List<DatabaseService.QuestionData> q3,
                               List<DatabaseService.QuestionData> q4, List<DatabaseService.QuestionData> mid,
                               List<DatabaseService.QuestionData> fin, Map<String, Double> poTotals, Map<Integer, String> idToPO) {
            this.quiz1 = q1; this.quiz2 = q2; this.quiz3 = q3; this.quiz4 = q4; this.mid = mid; this.fin = fin; this.poTotals = poTotals; this.idToPO = idToPO;
        }
    }
}

