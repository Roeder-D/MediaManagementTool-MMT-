package de.srh_dr.mediamanagementtoolmmt.controller;

import javafx.stage.Window;

// Allows centralized view management by the main controller
public interface MainControllerAware {
    void setMainController(MainController mainController);

    MainController getMainController();

    default Window getWindow() {
        MainController mc = getMainController();
        return (mc != null) ? mc.getWindow() : null;
    }
}
