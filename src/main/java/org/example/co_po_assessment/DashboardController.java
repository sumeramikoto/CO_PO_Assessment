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
import java.util.Map;

public class DashboardController {
    private Stage primaryStage;

    public DashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void handleUploadMarks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Excel File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                processExcelFile(selectedFile.getAbsolutePath());
                showAlert("Success", "Excel file processed successfully!");
            } catch (IOException e) {
                showAlert("Error", "Failed to process Excel file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void handleGenerateReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File for Report Generation");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                generateReports(selectedFile.getAbsolutePath());
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

    private void processExcelFile(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {

            new QuizEntryGenerator().generateSheet(path);
            new MidFinalEntryGenerator().generateSheet(path);

            try (FileOutputStream fos = new FileOutputStream(path)) {
                wb.write(fos);
            }
        }
    }

    private void generateReports(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(path);
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {

            AggregateCOGenerator.generate(path);

            new COPOThresholdGenerator().generate(path);

            Sheet aggregateCO = wb.getSheet("AggregateCO");
            Map<String, Double> coTotals = new HashMap<>(); // You'll need to populate this from your data
            new COContributionGenerator(wb, aggregateCO, coTotals).generate();

            Sheet contributionSheet = wb.getSheet("CO_Contribution");
            Map<String, Double> coThresholds = new HashMap<>(); // You'll need to populate this from your data
            new COBinaryGenerator(wb, aggregateCO, contributionSheet, coThresholds).generate();

            Sheet binarySheet = wb.getSheet("CO_Binary");
            new COResultsGenerator(wb, binarySheet).generate();

            try (FileOutputStream fos = new FileOutputStream(path)) {
                wb.write(fos);
            }
        }
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