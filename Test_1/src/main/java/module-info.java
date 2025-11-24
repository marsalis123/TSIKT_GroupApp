module com.example.test_1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example.test_1 to javafx.graphics, javafx.fxml;
    exports com.example.test_1;
}
