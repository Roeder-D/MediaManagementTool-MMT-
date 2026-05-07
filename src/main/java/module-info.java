module de.srh_dr.mediamanagementtoolmmt {
    requires javafx.controls;
    requires javafx.fxml;


    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires java.logging;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires mysql.connector.j;

    //opens de.srh_2551.mediamanagementtoolmmt to javafx.fxml;
    //exports de.srh_2551.mediamanagementtoolmmt;
    exports de.srh_dr.mediamanagementtoolmmt.app;
    opens de.srh_dr.mediamanagementtoolmmt.app to javafx.fxml;
    exports de.srh_dr.mediamanagementtoolmmt.controller;
    opens de.srh_dr.mediamanagementtoolmmt.controller to javafx.fxml;
}