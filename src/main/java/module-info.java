module com.example.ocrdemo.ocrdemo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ocrdemo.ocrdemo to javafx.fxml;
    exports com.example.ocrdemo.ocrdemo;
}