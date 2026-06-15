package de.srh_dr.mediamanagementtoolmmt.util;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class WindowPositionManager {
    public static void restoreWindowBounds(Stage stage, double savedX, double savedY, double savedWidth, double savedHeight) {
        stage.setWidth(savedWidth > 100 ? savedWidth : 1050);
        stage.setHeight(savedHeight > 100 ? savedHeight : 800);

        boolean positionValid = false;
        ObservableList<Screen> screens = Screen.getScreens();

        for(Screen screen : screens) {
            Rectangle2D bounds = screen.getVisualBounds();

            if(savedX >= bounds.getMinX() && savedX <= bounds.getMaxX() && savedY >= bounds.getMinY() && savedY <= bounds.getMaxY()) {
                positionValid = true;
                break;
            }
        }

        if(positionValid) {
            stage.setX(savedX);
            stage.setY(savedY);
        }else {
            Screen primaryScreen = Screen.getPrimary();
            Rectangle2D bounds = primaryScreen.getVisualBounds();

            stage.setX(bounds.getWidth() - stage.getWidth() / 2);
            stage.setY(bounds.getHeight() - stage.getHeight() / 2);
        }
    }
}
