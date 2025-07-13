module org.example.co_po_assessment {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;

    requires org.apache.poi.poi;

    requires org.apache.poi.ooxml;

    opens org.example.co_po_assessment to javafx.fxml;
    exports org.example.co_po_assessment;
}