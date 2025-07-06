package org.example.co_po_assessment.excelTemplate;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;

public class COContributionGenerator {
    private final Sheet aggSheet;
    private final Sheet outputSheet;
    private final Map<String, Double> coTotals;

    public COContributionGenerator(Workbook wb,
                                   Sheet aggregateCO,
                                   Map<String, Double> coTotals) {
        this.aggSheet   = aggregateCO;
        this.coTotals   = coTotals;
        this.outputSheet = wb.createSheet("CO_Contribution");
    }

    public Sheet getSheet() {
        return outputSheet;
    }

    public void generate() {
        // 1) copy header
        Row hdrAgg = aggSheet.getRow(0);
        Row hdrOut = outputSheet.createRow(0);
        hdrOut.createCell(0).setCellValue("Student ID");
        int coCount = hdrAgg.getLastCellNum() - 1;
        for (int c = 1; c <= coCount; c++) {
            String coName = hdrAgg.getCell(c).getStringCellValue();
            hdrOut.createCell(c).setCellValue(coName);
        }

        // 2) per‐student, compute percentages
        int outRowIdx = 1;
        for (int r = 1; r <= aggSheet.getLastRowNum(); r++) {
            Row inRow = aggSheet.getRow(r);
            if (inRow == null) continue;

            Row outRow = outputSheet.createRow(outRowIdx++);
            // student ID
            String sid = inRow.getCell(0).getStringCellValue();
            outRow.createCell(0).setCellValue(sid);

            // each CO
            for (int c = 1; c <= coCount; c++) {
                String coName = hdrAgg.getCell(c).getStringCellValue();
                double earned = inRow.getCell(c).getNumericCellValue();
                double total  = coTotals.get(coName);
                double pct    = (earned / total) * 100.0;

                outRow.createCell(c).setCellValue(pct);
            }
        }

        // 3) auto‐size
        for (int c = 0; c <= coCount; c++) {
            outputSheet.autoSizeColumn(c);
        }

        outputSheet.protectSheet("");
    }
}
