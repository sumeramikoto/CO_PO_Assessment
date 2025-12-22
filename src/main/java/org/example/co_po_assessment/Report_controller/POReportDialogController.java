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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

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
import org.example.co_po_assessment.DB_helper.DatabaseService;
import org.example.co_po_assessment.utilities.UserSession;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

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
    private Runnable onCloseAction;

    // Thresholds (fractions 0..1). Defaults: CO 60%, PO 40%
    private double coThreshold = 0.60;
    private double poThreshold = 0.40;

    /** Allow caller to override thresholds using percentages (0..100). */
    public void setThresholds(int poPercent, int coPercent) {
        double po = Math.max(0, Math.min(100, poPercent)) / 100.0;
        double co = Math.max(0, Math.min(100, coPercent)) / 100.0;
        this.poThreshold = po;
        this.coThreshold = co;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (table != null) table.setItems(data);
        if (colCode != null) colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        if (colPercent != null) colPercent.setCellValueFactory(new PropertyValueFactory<>("percent"));
        if (colComment != null) {
            colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
            colComment.setCellFactory(createLimitedTextFieldCellFactory("comment"));
            colComment.setOnEditCommit(evt -> evt.getRowValue().setComment(evt.getNewValue()));
        }
        // Setup Possible Steps column
        if (colSteps != null) {
            colSteps.setCellValueFactory(new PropertyValueFactory<>("steps"));
            colSteps.setCellFactory(createLimitedTextFieldCellFactory("suggestions"));
            colSteps.setOnEditCommit(evt -> evt.getRowValue().setSteps(evt.getNewValue()));
        }
        if (table != null) table.setEditable(true);
    }

    private Callback<TableColumn<Row, String>, TableCell<Row, String>> createLimitedTextFieldCellFactory(String fieldName) {
        return column -> new TableCell<Row, String>() {
            private TextField textField;
            private Label charCountLabel;

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(getString());
                            updateCharCount();
                        }
                        setText(null);
                        setGraphic(createEditingGraphic());
                    } else {
                        setText(getString());
                        setGraphic(null);
                    }
                }
            }

            @Override
            public void startEdit() {
                if (!isEmpty()) {
                    super.startEdit();
                    createTextField();
                    setText(null);
                    setGraphic(createEditingGraphic());
                    textField.selectAll();
                    textField.requestFocus();
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getString());
                setGraphic(null);
            }

            private void createTextField() {
                textField = new TextField(getString());
                charCountLabel = new Label();
                updateCharCount();
                
                textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
                textField.textProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue != null && newValue.length() > 80) {
                        textField.setText(newValue.substring(0, 80));
                        showCharacterLimitAlert(fieldName);
                    }
                    updateCharCount();
                });
                
                textField.setOnAction(event -> commitEdit(textField.getText()));
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit(textField.getText());
                    }
                });
            }

            private VBox createEditingGraphic() {
                VBox vbox = new VBox(2);
                vbox.getChildren().addAll(textField, charCountLabel);
                return vbox;
            }

            private void updateCharCount() {
                if (textField != null && charCountLabel != null) {
                    int length = textField.getText() != null ? textField.getText().length() : 0;
                    charCountLabel.setText(length + "/80");
                    charCountLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (length > 80 ? "red" : "gray") + ";");
                }
            }

            private String getString() {
                return getItem() == null ? "" : getItem();
            }
        };
    }

    private void showCharacterLimitAlert(String fieldName) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Character Limit Exceeded");
        alert.setHeaderText("Maximum character limit reached");
        alert.setContentText("The " + fieldName + " field must be between 0 and 80 characters. Please shorten your input.");
        alert.showAndWait();
    }

    public void setContext(DatabaseService.FacultyCourseAssignment selected) {
        this.selected = selected;
        titleLabel.setText("PO Report - " + selected.getCourseCode());
        contextLabel.setText("Programme: " + selected.getProgramme() + " | Year: " + selected.getAcademicYear());
        try { computeAndPopulate(); } catch (Exception e) { showError("Failed to compute: " + e.getMessage()); }
    }

    public void setOnCloseAction(Runnable action) {
        this.onCloseAction = action;
    }

    private void computeAndPopulate() throws Exception {
        data.clear();
        String courseCode = selected.getCourseCode();
        String academicYear = selected.getAcademicYear();
        String programme = selected.getProgramme();

        // Load thresholds from DB (percentages -> fractions). Fallback to defaults on error/missing keys.
        try {
            Map<String, Double> t = db.getThresholds();
            this.poThreshold = t.getOrDefault("PO_INDIVIDUAL", 40.0) / 100.0;
            this.coThreshold = t.getOrDefault("CO_INDIVIDUAL", 60.0) / 100.0;
        } catch (Exception ex) {
            // keep defaults 0.40 and 0.60
        }

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

        // Total possible marks per PO across all assessments
        Map<String,Double> poTotal = new HashMap<>();
        java.util.function.Consumer<DatabaseService.QuestionData> accumulateTotals = q -> {
            if (q.po == null) return;
            String po = q.po.trim().toUpperCase();
            if (po.isEmpty()) return;
            poTotal.merge(po, q.marks, Double::sum);
        };
        quizQuestionsAll.forEach(accumulateTotals);
        midQuestions.forEach(accumulateTotals);
        finalQuestions.forEach(accumulateTotals);
        if (poTotal.isEmpty()) throw new IllegalStateException("Questions exist but none have PO mappings.");

        List<DatabaseService.StudentData> students = db.getEnrolledStudents(courseCode, programme, academicYear);
        if (students.isEmpty()) throw new IllegalStateException("No students enrolled for this course in the selected academic year.");

        // Map questionId -> PO for each assessment
        Map<Integer,String> quizIdToPO = new HashMap<>();
        for (DatabaseService.QuestionData qd : quizQuestionsAll) if (qd.po!=null) quizIdToPO.put(qd.id, qd.po.trim().toUpperCase());
        Map<Integer,String> midIdToPO = new HashMap<>();
        for (DatabaseService.QuestionData qd : midQuestions) if (qd.po!=null) midIdToPO.put(qd.id, qd.po.trim().toUpperCase());
        Map<Integer,String> finalIdToPO = new HashMap<>();
        for (DatabaseService.QuestionData qd : finalQuestions) if (qd.po!=null) finalIdToPO.put(qd.id, qd.po.trim().toUpperCase());

        int totalRequired = 0; int graded = 0;
        Map<String, Map<String, Double>> studentPoTotals = new HashMap<>();
        for (DatabaseService.StudentData sd : students) {
            studentPoTotals.put(sd.id, new HashMap<>());
        }

        // Accumulate obtained marks per student per PO, and track required/graded entries
        for (int quizNum=1; quizNum<=4; quizNum++) {
            for (DatabaseService.StudentMarksData smd : db.getStudentQuizMarks(courseCode, programme, quizNum, academicYear)) {
                String po = quizIdToPO.get(smd.questionId);
                if (po==null || po.isEmpty()) continue;
                totalRequired++;
                if (smd.marksObtained != null) {
                    graded++;
                    double got = smd.marksObtained;
                    studentPoTotals.get(smd.studentId).merge(po, got, Double::sum);
                }
            }
        }
        for (DatabaseService.StudentMarksData smd : db.getStudentMidMarks(courseCode, programme, academicYear)) {
            String po = midIdToPO.get(smd.questionId);
            if (po==null || po.isEmpty()) continue;
            totalRequired++;
            if (smd.marksObtained != null) {
                graded++;
                double got = smd.marksObtained;
                studentPoTotals.get(smd.studentId).merge(po, got, Double::sum);
            }
        }
        for (DatabaseService.StudentMarksData smd : db.getStudentFinalMarks(courseCode, programme, academicYear)) {
            String po = finalIdToPO.get(smd.questionId);
            if (po==null || po.isEmpty()) continue;
            totalRequired++;
            if (smd.marksObtained != null) {
                graded++;
                double got = smd.marksObtained;
                studentPoTotals.get(smd.studentId).merge(po, got, Double::sum);
            }
        }

        if (totalRequired == 0) throw new IllegalStateException("There are PO-mapped questions but no enrolled students to grade.");
        if (graded == 0) throw new IllegalStateException("No marks have been entered yet. Please enter marks before viewing the PO report.");
        if (graded < totalRequired) throw new IllegalStateException("Cannot view PO report. Some required mark entries are still ungraded.");

        // Count students attaining each PO using only PO threshold (no CO gate)
        Map<String,Integer> poAttainedCounts = new HashMap<>();
        for (String po : poTotal.keySet()) poAttainedCounts.put(po,0);
        for (DatabaseService.StudentData sd : students) {
            Map<String, Double> studentPOMap = studentPoTotals.get(sd.id);
            for (String po : poTotal.keySet()) {
                double poDenom = poTotal.get(po);
                if (poDenom <= 0) continue;
                double poGot = studentPOMap.getOrDefault(po, 0.0);
                if (poGot / poDenom >= poThreshold) poAttainedCounts.merge(po,1,Integer::sum);
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
            
            JFreeChart chart = ChartFactory.createBarChart(
                "Program Outcome Attainment",
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
            chart.getTitle().setFont(new Font("Arial", Font.BOLD, 18));
            chart.setPadding(new org.jfree.chart.ui.RectangleInsets(10, 10, 10, 10));
            
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(new Color(248, 250, 252));
            plot.setDomainGridlinePaint(new Color(226, 232, 240));
            plot.setRangeGridlinePaint(new Color(226, 232, 240));
            plot.setOutlineVisible(false);
            plot.setRangeGridlinesVisible(true);
            plot.setDomainGridlinesVisible(false);
            
            // Style axes
            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
            domainAxis.setLabelFont(new Font("Arial", Font.BOLD, 13));
            domainAxis.setTickLabelPaint(new Color(51, 65, 85));
            domainAxis.setLabelPaint(new Color(30, 41, 59));
            
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setRange(0.0, 100.0);
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
            rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 13));
            rangeAxis.setTickLabelPaint(new Color(51, 65, 85));
            rangeAxis.setLabelPaint(new Color(30, 41, 59));
            
            // Modern bar styling with gradient (Purple theme for PO)
            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new GradientPaint(
                0.0f, 0.0f, new Color(139, 92, 246),   // Violet 500
                0.0f, 300.0f, new Color(124, 58, 237)  // Violet 600
            ));
            renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
            renderer.setShadowVisible(false);
            renderer.setDrawBarOutline(false);
            renderer.setItemMargin(0.15);  // Space between bars
            renderer.setMaximumBarWidth(0.08);  // Bar width control
            
            // Higher resolution output
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
            ChartUtils.writeChartAsPNG(chartBaos, chart, 900, 550, true, 9);
            byte[] chartBytes = chartBaos.toByteArray();

            File reportsDir = new File("C:\\Users\\User\\Desktop\\H\\co_reports"); if (!reportsDir.exists()) reportsDir.mkdirs();
            String safeProgramme = selected.getProgramme().replaceAll("[^A-Za-z0-9_-]", "");
            File outFile = new File(reportsDir, "PO_" + selected.getCourseCode() + "_" + selected.getAcademicYear() + "_" + safeProgramme + ".pdf");
            try (PdfWriter writer = new PdfWriter(new FileOutputStream(outFile)); PdfDocument pdf = new PdfDocument(writer); Document doc = new Document(pdf)) {
                Paragraph title = new Paragraph(selected.getCourseCode() + " - " + selected.getCourseName()); title.setFontSize(16).setTextAlignment(TextAlignment.CENTER); doc.add(title);
                doc.add(new Paragraph("Department: " + selected.getDepartment()));
                doc.add(new Paragraph("Programme: " + selected.getProgramme()));
                DatabaseService.FacultyInfo fi = UserSession.getCurrentFaculty(); if (fi != null) doc.add(new Paragraph("Faculty: " + fi.fullName + " (" + fi.shortname + ")"));
                doc.add(new Paragraph("Academic Year: " + selected.getAcademicYear()));
                doc.add(new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
                doc.add(new Paragraph(" "));

                Image chartImg = new Image(ImageDataFactory.create(chartBytes)).setAutoScale(true); doc.add(chartImg);
                // Add summary table at the bottom
                doc.add(new Paragraph("\n"));
                Table tbl = new Table(new float[]{2f, 2.5f, 5f, 5f});
                // Set table width to 100%
                tbl.setWidth(UnitValue.createPercentValue(100));
                tbl.addHeaderCell(new Cell().add(new Paragraph("PO")));
                tbl.addHeaderCell(new Cell().add(new Paragraph("Attainment %")));
                tbl.addHeaderCell(new Cell().add(new Paragraph("Comment")));
                tbl.addHeaderCell(new Cell().add(new Paragraph("Suggestions")));
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

    private void close() {
        if (onCloseAction != null) {
            onCloseAction.run();
        } else {
            ((Stage) table.getScene().getWindow()).close();
        }
    }
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
        public void setComment(String v) { 
            if (v != null && v.length() > 80) {
                v = v.substring(0, 80);
            }
            comment.set(v); 
        }
        public String getSteps() { return steps.get(); }
        public void setSteps(String v) { 
            if (v != null && v.length() > 80) {
                v = v.substring(0, 80);
            }
            steps.set(v); 
        }
    }
}
