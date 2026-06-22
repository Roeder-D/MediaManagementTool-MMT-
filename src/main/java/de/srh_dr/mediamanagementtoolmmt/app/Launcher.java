package de.srh_dr.mediamanagementtoolmmt.app;

import de.srh_dr.mediamanagementtoolmmt.services.LogManager;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        LogManager.setup();
        Application.launch(MMT_Application.class, args);
    }
}
