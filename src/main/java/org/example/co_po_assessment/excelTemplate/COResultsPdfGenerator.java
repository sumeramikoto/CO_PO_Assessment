package org.example.co_po_assessment.excelTemplate;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Reads the COResults sheet from an Excel file and generates a PDF report
 * listing each CO attainment percentage and a line chart.
 * Dependencies: Apache POI, iText 7, JFreeChart
 */
public class COResultsPdfGenerator {

    private final String SHEET_NAME = "COResults";

    public void generatePDFReport(File excelFile, File pdfFile) throws IOException {
        // 1. Read CO results from Excel
        Map<String, Double> coData = readCOResults(excelFile);

        // 2. Create PDF
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            // Add text lines
            for (Map.Entry<String, Double> entry : coData.entrySet()) {
                String co = entry.getKey();
                double pct = entry.getValue();
                String line = String.format("%s was attained by %.2f%% of students", co, pct);
                document.add(new Paragraph(line));
            }

            // Add space before chart
            document.add(new Paragraph("\n"));

            // Create chart image
            BufferedImage chartImage = createLineChart(coData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", baos);
            baos.flush();
            ImageData imgData = ImageDataFactory.create(baos.toByteArray());
            Image pdfImage = new Image(imgData);
            document.add(pdfImage);
        }
    }

    private Map<String, Double> readCOResults(File excelFile) throws IOException {
        Map<String, Double> results = new LinkedHashMap<>(); // preserve order
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + SHEET_NAME + "' not found in " + excelFile);
            }

            // Assume first row is header: [CO, Percentage]
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell coCell = row.getCell(0);
                Cell pctCell = row.getCell(1);
                if (coCell == null || pctCell == null) continue;
                String co = coCell.getStringCellValue().trim();
                double pct = pctCell.getNumericCellValue();
                results.put(co, pct);
            }
        }
        return results;
    }

    private BufferedImage createLineChart(Map<String, Double> coData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Double> entry : coData.entrySet()) {
            dataset.addValue(entry.getValue(), "Attainment", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Course Outcome Attainment",  // chart title
                "CO",                          // domain axis label
                "% Achieved",                 // range axis label
                dataset,                        // data
                PlotOrientation.VERTICAL,
                false,                          // include legend
                true,
                false
        );

        // Render chart to image
        return chart.createBufferedImage(600, 400);
    }

//    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.err.println("Usage: java COResultsPdfGenerator <input.xlsx> <output.pdf>");
//            System.exit(1);
//        }
//        String excelPath = args[0];
//        String pdfPath = args[1];
//        try {
//            generatePDFReport(excelPath, pdfPath);
//            System.out.println("PDF report generated: " + pdfPath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
