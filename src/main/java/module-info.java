module com.example.ocrdemo.ocrdemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires org.bytedeco.javacv;
    requires tess4j;
    requires java.desktop;


    opens com.example.ocrdemo.ocrdemo to javafx.fxml;
    exports com.example.ocrdemo.ocrdemo;
    exports Controller;
    opens Controller to javafx.fxml;
}