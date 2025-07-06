package org.example.co_po_assessment.excelTemplate;

public class Question {
    String number;
    double maxMark;
    String co;
    String po;

    Question(String number, double maxMark) {
        this.number = number;
        this.maxMark = maxMark;
    }

    public Question(String number, double maxMark, String co, String po) {
        this.number = number;
        this.maxMark = maxMark;
        this.co = co;
        this.po = po;
    }
}
