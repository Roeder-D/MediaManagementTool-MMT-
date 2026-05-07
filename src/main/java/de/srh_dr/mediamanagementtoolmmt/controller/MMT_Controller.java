package de.srh_dr.mediamanagementtoolmmt.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MMT_Controller {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
