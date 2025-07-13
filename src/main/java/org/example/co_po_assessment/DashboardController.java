package org.example.co_po_assessment;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.co_po_assessment.excelTemplate.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardController {
    private Stage primaryStage;

    public DashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void handleMarksTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Excel File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                generateEntrySheets(selectedFile.getAbsolutePath());
                showAlert("Success", "Excel file processed successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to process Excel file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void handleMarksProcessing() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File for Report Generation");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                parseMarks(selectedFile.getAbsolutePath());
                showAlert("Success", "Reports generated successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to generate reports: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void handleGetTemplate() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Template As");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File selectedFile = fileChooser.showSaveDialog(primaryStage);

        if (selectedFile != null) {
            try {
                createTemplate(selectedFile.getAbsolutePath());
                showAlert("Success", "Template created successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to create template: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void generateEntrySheets(String path) throws IOException {
        QuizEntryGenerator quizEntryGenerator = new QuizEntryGenerator();
        MidFinalEntryGenerator midFinalEntryGenerator = new MidFinalEntryGenerator();
        try {
            quizEntryGenerator.generateSheet(path);
            midFinalEntryGenerator.generateSheet(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        try (FileInputStream fis = new FileInputStream(path);
//             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
//
//            new QuizEntryGenerator().generateSheet(path);
//            new MidFinalEntryGenerator().generateSheet(path);
//
//            try (FileOutputStream fos = new FileOutputStream(path)) {
//                wb.write(fos);
//            }
//        }
    }

    private void parseMarks(String path) throws IOException {
        COPOThresholdGenerator copoThresholdGenerator = new COPOThresholdGenerator();
        AggregateCOGenerator aggregateCOGenerator = new AggregateCOGenerator();
        copoThresholdGenerator.generate(path);
        aggregateCOGenerator.generate(path);
        try (FileInputStream in = new FileInputStream(path);
             XSSFWorkbook wb = new XSSFWorkbook(in)) {
            Sheet aggSheet  = wb.getSheet("AggregateCO");
            Sheet thresh    = wb.getSheet("CO_PO_Thresholds");

            // Build total‐marks & threshold maps
            Map<String, Double> coTotals    = new LinkedHashMap<>();
            Map<String, Double> coThresholds= new LinkedHashMap<>();
            boolean start = false;
            for (Row r : thresh) {
                Cell c0 = r.getCell(0);
                if (c0 == null) continue;
                String v = c0.getStringCellValue();
                if ("CO".equals(v)) {
                    start = true;
                    continue;
                }
                if (start && v.startsWith("C")) {
                    String name = v.trim();
                    coTotals.put(     name, r.getCell(1).getNumericCellValue());
                    coThresholds.put(name, r.getCell(2).getNumericCellValue());
                }
                if (start && v.startsWith("PO")) break;
            }

            // Generate Contribution sheet
            COContributionGenerator contribGen =
                    new COContributionGenerator(wb, aggSheet, coTotals);
            contribGen.generate();

            // Generate Binary sheet
            COBinaryGenerator binaryGen =
                    new COBinaryGenerator(wb, aggSheet,
                            contribGen.getSheet(),
                            coThresholds);
            binaryGen.generate();

            COResultsGenerator resultsGen =
                    new COResultsGenerator(wb, binaryGen.getSheet());
            resultsGen.generate();

            // Save
            try (FileOutputStream out = new FileOutputStream(path)) {
                wb.write(out);
            }
        }

        System.out.println("Done.");
//        try (FileInputStream fis = new FileInputStream(path);
//             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
//
//            AggregateCOGenerator.generate(path);
//
//            new COPOThresholdGenerator().generate(path);
//
//            Sheet aggregateCO = wb.getSheet("AggregateCO");
//            Map<String, Double> coTotals = new HashMap<>(); // You'll need to populate this from your data
//            Map<String, Double> coThresholds = new HashMap<>(); // You'll need to populate this from your data
//            Sheet thresh    = wb.getSheet("CO_PO_Thresholds");
//            boolean start = false;
//            for (Row r : thresh) {
//                Cell c0 = r.getCell(0);
//                if (c0 == null) continue;
//                String v = c0.getStringCellValue();
//                if ("CO".equals(v)) {
//                    start = true;
//                    continue;
//                }
//                if (start && v.startsWith("C")) {
//                    String name = v.trim();
//                    coTotals.put(     name, r.getCell(1).getNumericCellValue());
//                    coThresholds.put(name, r.getCell(2).getNumericCellValue());
//                }
//                if (start && v.startsWith("PO")) break;
//            }
//            new COContributionGenerator(wb, aggregateCO, coTotals).generate();
//
//            Sheet contributionSheet = wb.getSheet("CO_Contribution");
//
//            new COBinaryGenerator(wb, aggregateCO, contributionSheet, coThresholds).generate();
//
//            Sheet binarySheet = wb.getSheet("CO_Binary");
//            new COResultsGenerator(wb, binarySheet).generate();
//
//            try (FileOutputStream fos = new FileOutputStream(path)) {
//                wb.write(fos);
//            }
//        }
    }

    private void createTemplate(String path) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // Create StudentInfo sheet
            Object[][] courseInfo = {
                    {"Instructor", null},
                    {"Course Code", null},
                    {"Course Title", null},
                    {"Credit", null},
                    {"Total Students", null},
                    {"Academic Year", null},
                    {"Program", null},
                    {"Department", null}
            };

            String[][] studentInfo = new String[0][4]; // Empty student data
            StudentInfoSheetTemplate studentSheet = new StudentInfoSheetTemplate(
                    "", "", "", 0.0, 0, "", "", "", courseInfo, studentInfo
            );
            studentSheet.generateSheet(wb.createSheet("StudentInfo"));

            new QuestionInfoQuizTemplate().generateSheet(wb.createSheet("QuestionInfoQuiz"));

            new QuestionInfoMidFinalTemplate().generateSheet(wb.createSheet("QuestionInfoMidFinal"));
            try (FileOutputStream fos = new FileOutputStream(path)) {
                wb.write(fos);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}