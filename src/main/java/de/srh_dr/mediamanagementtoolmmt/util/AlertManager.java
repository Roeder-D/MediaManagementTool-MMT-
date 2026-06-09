package de.srh_dr.mediamanagementtoolmmt.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

public class AlertManager {
    public static void showAlert(Alert.AlertType type, String title, String content, Window ownerStage){
        Platform.runLater(()->{
           Alert alert = new Alert(type);
           alert.setTitle(title);
           alert.setHeaderText(null);
           alert.setContentText(content);
           alert.initOwner(ownerStage);
           alert.showAndWait();
        });
    }

    public static boolean requestConfirmation(String title, String content, Window ownerStage){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(ownerStage);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
