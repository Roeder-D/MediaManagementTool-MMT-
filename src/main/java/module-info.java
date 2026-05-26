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
    requires java.net.http;
    requires jdk.jfr;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires java.management;
    requires java.prefs;

    exports de.srh_dr.mediamanagementtoolmmt.app;
    opens de.srh_dr.mediamanagementtoolmmt.app to javafx.fxml;
    exports de.srh_dr.mediamanagementtoolmmt.controller;
    opens de.srh_dr.mediamanagementtoolmmt.controller to javafx.fxml;
    exports de.srh_dr.mediamanagementtoolmmt.util;
    opens de.srh_dr.mediamanagementtoolmmt.util to javafx.fxml;
}