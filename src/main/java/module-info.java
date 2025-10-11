module org.example.co_po_assessment {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.apache.poi.ooxml;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires layout;
    requires kernel;
    requires io;
    requires javafx.graphics;
    requires java.sql;
    requires org.jfree.jfreechart;

    opens org.example.co_po_assessment to javafx.fxml;
    exports org.example.co_po_assessment;
    exports org.example.co_po_assessment.faculty_input_controller;
    opens org.example.co_po_assessment.faculty_input_controller to javafx.fxml;
    exports org.example.co_po_assessment.DashboardPanels;
    opens org.example.co_po_assessment.DashboardPanels to javafx.fxml;
    exports org.example.co_po_assessment.DB_helper;
    opens org.example.co_po_assessment.DB_helper to javafx.fxml;
    exports org.example.co_po_assessment.dashboard_controller;
    opens org.example.co_po_assessment.dashboard_controller to javafx.fxml;
    exports org.example.co_po_assessment.admin_input_controller;
    opens org.example.co_po_assessment.admin_input_controller to javafx.fxml;
    exports org.example.co_po_assessment.Report_controller;
    opens org.example.co_po_assessment.Report_controller to javafx.fxml;
    exports org.example.co_po_assessment.utilities;
    opens org.example.co_po_assessment.utilities to javafx.fxml;
    exports org.example.co_po_assessment.DB_Services;
    opens org.example.co_po_assessment.DB_Services to javafx.fxml;
    exports org.example.co_po_assessment.Objects;
    opens org.example.co_po_assessment.Objects to javafx.fxml;
}

//    opens org.example.co_po_assessment to javafx.fxml;
//    //exports org.example.co_po_assessment;
////    exports frontend;
////    opens frontend to javafx.fxml;
////    exports;
////    opens to
//}