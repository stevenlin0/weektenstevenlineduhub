module com.example.csc311_db_ui_semesterlongproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires com.azure.storage.blob;

    exports viewmodel;
    exports dao;
    exports model;

    opens viewmodel to javafx.fxml;
    opens dao;
    opens model;
}