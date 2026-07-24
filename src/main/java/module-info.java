module com.cvt.conveapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires org.apache.pdfbox;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.apache.logging.log4j;
    requires java.desktop;
    requires tess4j;
    requires spire.office.free;
    requires java.net.http;

    opens com.cvt.conveapp to javafx.fxml;
    exports com.cvt.conveapp;
}