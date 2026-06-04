module com.railway {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.railway to javafx.fxml;

    exports com.railway;
    exports com.railway.config;
    exports com.railway.engine;
    exports com.railway.model;
    exports com.railway.ui;
    exports com.railway.util;
}
