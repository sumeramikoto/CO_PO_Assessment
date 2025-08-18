package org.example.co_po_assessment.excelTemplate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class COPOThresholdGenerator {
    private static final double CO_THRESHOLD = 60.0;
    private static final double PO_THRESHOLD = 40.0;

    public void generate(File path) throws IOException {
        // String path = "aggregateTemp.xlsx";
        // Load workbook
        Workbook wb;
        try (FileInputStream fis = new FileInputStream(path)) {
            wb = new XSSFWorkbook(fis);
        }

        // Parse all questions from both sheets
        List<Question> quiz1Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 0, 1, 2, 3);
        List<Question> quiz2Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 4, 5, 6, 7);
        List<Question> quiz3Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 8, 9, 10, 11);
        List<Question> quiz4Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 12, 13, 14, 15);
        List<Question> midQs = parseSheet(wb.getSheet("QuestionInfoMidFinal"), 0, 1, 2, 3);
        List<Question> finalQs = parseSheet(wb.getSheet("QuestionInfoMidFinal"), 4, 5, 6, 7);

        Map<String, Double> coTotals = new TreeMap<>();
        Map<String, Double> poTotals = new TreeMap<>();

        Map<String,Question> mapQuiz1 = quiz1Qs.stream()
                .collect(Collectors.toMap(q->q.title, q->q));
        Map<String,Question> mapQuiz2 = quiz2Qs.stream()
                .collect(Collectors.toMap(q->q.title, q->q));
        Map<String,Question> mapQuiz3 = quiz3Qs.stream()
                .collect(Collectors.toMap(q->q.title, q->q));
        Map<String,Question> mapQuiz4 = quiz4Qs.stream()
                .collect(Collectors.toMap(q->q.title, q->q));
        Map<String,Question> mapMid = midQs.stream()
                .collect(Collectors.toMap(q->q.title, q->q));
        Map<String,Question> mapFin = finalQs.stream()
                .collect(Collectors.toMap(q->q.title, q->q));

        Map<String,Map<String,Question>> sheetMap = Map.of(
                "Quiz1Entry", mapQuiz1,
                "Quiz2Entry", mapQuiz2,
                "Quiz3Entry", mapQuiz3,
                "Quiz4Entry", mapQuiz4,
                "MidEntry",   mapMid,
                "FinalEntry", mapFin
        );

        // Group totals by CO and PO

        for (Map<String, Question> qMap : sheetMap.values()) {
            for (Question q : qMap.values()) {
                coTotals.merge(q.co, q.maxMark, Double::sum);
                poTotals.merge(q.po, q.maxMark, Double::sum);
            }
        }


        // Write to CO_PO_Thresholds sheet
        Sheet out = wb.createSheet("CO_PO_Thresholds");
        int ri = 0;

        Row h1 = out.createRow(ri++);
        h1.createCell(0).setCellValue("CO");
        h1.createCell(1).setCellValue("TOTAL");
        h1.createCell(2).setCellValue("CO_THRESHOLD");

        for (String co : coTotals.keySet()) {
            Row r = out.createRow(ri++);
            r.createCell(0).setCellValue(co);
            r.createCell(1).setCellValue(coTotals.get(co));
            r.createCell(2).setCellValue(CO_THRESHOLD);
        }

        ri++;
        Row h2 = out.createRow(ri++);
        h2.createCell(0).setCellValue("PO");
        h2.createCell(1).setCellValue("TOTAL");
        h2.createCell(2).setCellValue("PO_THRESHOLD");

        for (String po : poTotals.keySet()) {
            Row r = out.createRow(ri++);
            r.createCell(0).setCellValue(po);
            r.createCell(1).setCellValue(poTotals.get(po));
            r.createCell(2).setCellValue(PO_THRESHOLD);
        }

        for (int c = 0; c < 3; c++) out.autoSizeColumn(c);
        out.protectSheet("");

        try (FileOutputStream fos = new FileOutputStream(path)) {
            wb.write(fos);
        }
        wb.close();
        System.out.println("CO_PO_Thresholds generated.");
    }

    /**
     * Parses a sheet into a List<Question>.
     * cols: qCol=question#, mCol=maxMark, coCol=CO, poCol=PO
     */
    private List<Question> parseSheet(Sheet sheet,
                                             int qCol,
                                             int mCol,
                                             int coCol,
                                             int poCol) {
        List<Question> list = new ArrayList<>();
        for (int r = 6; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Cell qc = row.getCell(qCol);
            Cell mc = row.getCell(mCol);
            Cell cc = row.getCell(coCol);
            Cell pc = row.getCell(poCol);
            if (qc == null || mc == null || cc == null || pc == null) continue;
            String qNum = qc.getCellType() == CellType.STRING
                    ? qc.getStringCellValue().trim()
                    : String.valueOf((long)qc.getNumericCellValue());
            if (qNum.isEmpty()) continue;
            double max = mc.getNumericCellValue();
            String co = cc.getStringCellValue().trim();
            String po = pc.getStringCellValue().trim();
            list.add(new Question(qNum, max, co, po));
        }
        return list;
    }

}
