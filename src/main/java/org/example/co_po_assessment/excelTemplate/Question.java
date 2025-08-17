package org.example.co_po_assessment.excelTemplate;

public class Question {
    String title;
    double maxMark;
    String co;
    String po;

    Question(String title, double maxMark) {
        this.title = title;
        this.maxMark = maxMark;
    }

    public Question(String title, double maxMark, String co, String po) {
        this.title = title;
        this.maxMark = maxMark;
        this.co = co;
        this.po = po;
    }
}
