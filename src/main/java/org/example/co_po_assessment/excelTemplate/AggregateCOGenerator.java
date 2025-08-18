package org.example.co_po_assessment.excelTemplate;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AggregateCOGenerator {
    public void generate(File path) throws IOException {
        // Load workbook
        Workbook wb;
        try (FileInputStream fis = new FileInputStream(path)) {
            wb = new XSSFWorkbook(fis);
        }

        // Master question list
        List<Question> allQs = new ArrayList<>();
        List<Question> quiz1Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 0, 1, 2, 3);
        List<Question> quiz2Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 4, 5, 6, 7);
        List<Question> quiz3Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 8, 9, 10, 11);
        List<Question> quiz4Qs = parseSheet(wb.getSheet("QuestionInfoQuiz"), 12, 13, 14, 15);
        List<Question> midQs = parseSheet(wb.getSheet("QuestionInfoMidFinal"), 0, 1, 2, 3);
        List<Question> finalQs = parseSheet(wb.getSheet("QuestionInfoMidFinal"), 4, 5, 6, 7);

        // Map question# to Question
        Map<String, Question> mapQ = allQs.stream()
                .collect(Collectors.toMap(q -> q.title, q -> q));

        // Prepare CO list


        // Read SIDs
        Sheet sinfo = wb.getSheet("StudentInfo");
        List<String> sids = new ArrayList<>();
        for (int r = 11; r <= sinfo.getLastRowNum(); r++) {
            Row row = sinfo.getRow(r);
            if (row == null) continue;
            Cell c = row.getCell(0);
            if (c == null) continue;
            String sid = c.getCellType() == CellType.STRING
                    ? c.getStringCellValue()
                    : String.valueOf((long)c.getNumericCellValue());
            sids.add(sid);
        }

        // Initialize aggregate map


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

        Set<String> allCOs = new TreeSet<>();  // TreeSet keeps it sorted

// Iterate through all sheets and collect COs
        for (Map<String, Question> qMap : sheetMap.values()) {
            for (Question q : qMap.values()) {
                allCOs.add(q.co);
            }
        }

        List<String> coList = new ArrayList<>(allCOs);

        Map<String, Map<String, Double>> agg = new LinkedHashMap<>();
        for (String sid : sids) {
            Map<String, Double> m = new HashMap<>();
            for (String co : coList) m.put(co, 0.0);
            agg.put(sid, m);
        }

        /*
        String[] sheets = {"Quiz1Entry","Quiz2Entry","Quiz3Entry","Quiz4Entry","MidEntry","FinalEntry"};
        for (String name : sheets) {
            Sheet sh = wb.getSheet(name);
            if (sh == null) continue;
            Row hdr = sh.getRow(0);
            int lastCol = hdr.getLastCellNum();
            Map<Integer,String> colToQ = new HashMap<>();
            for (int c = 1; c < lastCol - 1; c++) {
                String label = hdr.getCell(c).getStringCellValue();
                colToQ.put(c, label.split(" ")[0]);
            }
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null) continue;
                String sid = row.getCell(0).getStringCellValue();
                var coMap = agg.get(sid);
                if (coMap == null) continue;
                for (int c = 1; c < lastCol - 1; c++) {
                    Cell mc = row.getCell(c);
                    if (mc != null && mc.getCellType() == CellType.NUMERIC) {
                        double v = mc.getNumericCellValue();
                        Question q = mapQ.get(colToQ.get(c));
                        coMap.merge(q.co, v, Double::sum);
                    }
                }
            }
        }

        */

        for (String sheetName : sheetMap.keySet()) {
            Sheet sh = wb.getSheet(sheetName);
            Map<String,Question> thisMap = sheetMap.get(sheetName);
            if (sh == null) continue;

            // 1) Build a header map: columnIndex → questionNumber ("1", "2b", etc.)
            Row header = sh.getRow(0);
            int lastCol = header.getLastCellNum();  // one beyond the last busy cell
            Map<Integer,String> colToQ = new HashMap<>();
            for (int c = 1; c < lastCol - 1; c++) {
                String label = header.getCell(c).getStringCellValue();
                // label is e.g. "1 (10)" or "2b (5.0)" → split on space
                String qnum = label.split(" ")[0];
                colToQ.put(c, qnum);
            }

            // 2) Iterate each student row
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null) continue;

                // Read the SID from column 0
                Cell sidCell = row.getCell(0);
                if (sidCell == null || sidCell.getCellType() != CellType.STRING) continue;
                String sid = sidCell.getStringCellValue();

                // Grab that student’s CO‐map
                Map<String,Double> coMap = agg.get(sid);
                if (coMap == null) continue;

                // 3) For each question cell in this row
                for (int c = 1; c < lastCol - 1; c++) {
                    Cell markCell = row.getCell(c);
                    if (markCell == null || markCell.getCellType() != CellType.NUMERIC)
                        continue;  // skip blanks or text

                    double mark = markCell.getNumericCellValue();

                    // Lookup which Question this column is
                    String qnum = colToQ.get(c);
                    Question q = thisMap.get(qnum);
                    if (q == null) continue;  // no question metadata? skip

                    // Add this student’s mark into that CO’s total
                    coMap.merge(q.co, mark, Double::sum);
                }
            }
        }


        // Write AggregateCO
        Sheet out = wb.createSheet("AggregateCO");

// Write header row: SID | CO1 | CO2 | CO3 | ...
        Row header = out.createRow(0);
        header.createCell(0).setCellValue("SID");
        for (int i = 0; i < coList.size(); i++) {
            header.createCell(i + 1).setCellValue(coList.get(i));
        }

// Write student rows
        int rowIndex = 1;
        for (String sid : sids) {
            Row row = out.createRow(rowIndex++);
            row.createCell(0).setCellValue(sid);

            Map<String, Double> studentCOMap = agg.get(sid);
            for (int i = 0; i < coList.size(); i++) {
                String co = coList.get(i);
                double value = studentCOMap.getOrDefault(co, 0.0);
                row.createCell(i + 1).setCellValue(value);
            }
        }

// Auto-size columns
        for (int c = 0; c <= coList.size(); c++) {
            out.autoSizeColumn(c);
        }

        out.protectSheet("");

        // Save
        try (FileOutputStream fos = new FileOutputStream(path)) {
            wb.write(fos);
        }
        System.out.println("AggregateCO generated.");
    }

    private static List<Question> parseSheet(Sheet sheet,
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
