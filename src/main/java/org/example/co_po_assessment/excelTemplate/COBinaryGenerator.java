package org.example.co_po_assessment.excelTemplate;

import java.util.Map;

import org.apache.poi.ss.usermodel.*;

public class COBinaryGenerator {
    private final Sheet aggSheet;
    private final Sheet contribSheet;
    private final Sheet outputSheet;
    private final Map<String, Double> coThresholds;

    public COBinaryGenerator(Workbook wb,
                             Sheet aggregateCO,
                             Sheet contributionSheet,
                             Map<String, Double> coThresholds) {
        this.aggSheet      = aggregateCO;
        this.contribSheet  = contributionSheet;
        this.coThresholds  = coThresholds;
        this.outputSheet   = wb.createSheet("CO_Binary");
    }

    public Sheet getSheet() {
        return outputSheet;
    }

    public void generate() {
        // 1) copy header from contributionSheet
        Row hdrCon = contribSheet.getRow(0);
        Row hdrOut = outputSheet.createRow(0);
        for (int c = 0; c < hdrCon.getLastCellNum(); c++) {
            hdrOut.createCell(c)
                    .setCellValue(hdrCon.getCell(c).getStringCellValue());
        }

        // 2) per‐student, compare against threshold
        int rowCount = contribSheet.getLastRowNum();
        for (int r = 1; r <= rowCount; r++) {
            Row inPct   = contribSheet.getRow(r);
            if (inPct == null) continue;

            Row outRow = outputSheet.createRow(r);
            // copy student ID
            outRow.createCell(0)
                    .setCellValue(inPct.getCell(0).getStringCellValue());

            // each CO column
            for (int c = 1; c < inPct.getLastCellNum(); c++) {
                String coName = hdrCon.getCell(c).getStringCellValue();
                double pct    = inPct.getCell(c).getNumericCellValue();
                double thresh = coThresholds.get(coName);

                int passFail  = pct >= thresh ? 1 : 0;
                outRow.createCell(c).setCellValue(passFail);
            }
        }

        // 3) auto‐size
        for (int c = 0; c < hdrCon.getLastCellNum(); c++) {
            outputSheet.autoSizeColumn(c);
        }

        outputSheet.protectSheet("");
    }
}
