package de.srh_dr.mediamanagementtoolmmt.app;

import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.prefs.Preferences;

public class MMT_Application extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //save window size and position
        Preferences prefs = Preferences.userNodeForPackage(MMT_Application.class);
        boolean isFirstLaunch = prefs.getBoolean("first_launch", true);


        FXMLLoader fxmlLoader = new FXMLLoader(MMT_Application.class.getResource("/de/srh_dr/mediamanagementtoolmmt/view/Main_Shell.fxml"));
        fxmlLoader.setResources(LanguageManager.getBundle());
        Scene scene = new Scene(fxmlLoader.load());

        stage.setScene(scene);
        stage.setTitle(LanguageManager.getString("app.title"));

        //load window size and position
        if (isFirstLaunch) {
            stage.sizeToScene();
            stage.centerOnScreen();
        }else{
            stage.setX(prefs.getDouble("x", 0));
            stage.setY(prefs.getDouble("y", 0));
            stage.setWidth(prefs.getDouble("width", 1050));
            stage.setHeight(prefs.getDouble("height", 800));
        }

        //save window size and position
        stage.setOnCloseRequest(event -> {
            prefs.putDouble("x", stage.getX());
            prefs.putDouble("y", stage.getY());
            prefs.putDouble("width", stage.getWidth());
            prefs.putDouble("height", stage.getHeight());
            prefs.putBoolean("first_launch", false);
        });

        stage.show();
    }
}
