module org.example.co_po_assessment {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires org.apache.poi.ooxml;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires layout;
    requires kernel;
    requires io;
    requires javafx.graphics;
    requires java.sql;
    requires org.jfree.jfreechart;
    requires jbcrypt;

    opens org.example.co_po_assessment to javafx.fxml;
    exports org.example.co_po_assessment;
    exports depricatedClasses;
    opens depricatedClasses to javafx.fxml;
}

//    opens org.example.co_po_assessment to javafx.fxml;
//    //exports org.example.co_po_assessment;
////    exports frontend;
////    opens frontend to javafx.fxml;
////    exports;
////    opens to
//}