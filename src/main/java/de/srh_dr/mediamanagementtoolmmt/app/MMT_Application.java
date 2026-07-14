package de.srh_dr.mediamanagementtoolmmt.app;

import com.zaxxer.hikari.HikariConfig;
import de.srh_dr.mediamanagementtoolmmt.controller.MainController;
import de.srh_dr.mediamanagementtoolmmt.services.DBConnection;
import de.srh_dr.mediamanagementtoolmmt.util.LanguageManager;
import de.srh_dr.mediamanagementtoolmmt.util.WindowPositionManager;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class MMT_Application extends Application {
    private static final Logger LOGGER = Logger.getLogger(MMT_Application.class.getName());
    private static final Dotenv dotenv = Dotenv.load();
    private ScheduledExecutorService connectionScheduler;
    private static MainController mainController;

    @Override
    public void start(Stage stage) throws IOException {
        //save window size and position
        Preferences prefs = Preferences.userNodeForPackage(MMT_Application.class);
        boolean isFirstLaunch = prefs.getBoolean("first_launch", true);

        FXMLLoader fxmlLoader = new FXMLLoader(MMT_Application.class.getResource("/de/srh_dr/mediamanagementtoolmmt/view/Main_Shell.fxml"));
        fxmlLoader.setResources(LanguageManager.getBundle());
        Parent root = fxmlLoader.load();
        mainController = fxmlLoader.getController();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle(LanguageManager.getString("app.title"));

        //load window size and position
        if (isFirstLaunch) {
            stage.sizeToScene();
            stage.centerOnScreen();
        }else{
            double savedX = prefs.getDouble("x", 0);
            double savedY = prefs.getDouble("y", 0);
            double savedWidth = prefs.getDouble("width", 1050);
            double savedHeight = prefs.getDouble("height", 800);

            WindowPositionManager.restoreWindowBounds(stage, savedX, savedY, savedWidth, savedHeight);

            String defaultImagePath = "/de/srh_dr/mediamanagementtoolmmt/Images/MMT_icon.png";
            URL iconURL = getClass().getResource(defaultImagePath);
            if(iconURL != null){
            Image icon = new Image(iconURL.toExternalForm());
            stage.getIcons().add(icon);
            }
        }

        //save window size and position
        stage.setOnCloseRequest(event -> {
            prefs.putDouble("x", stage.getX());
            prefs.putDouble("y", stage.getY());
            prefs.putDouble("width", stage.getWidth());
            prefs.putDouble("height", stage.getHeight());
            prefs.putBoolean("first_launch", false);
        });

        startConnectionMonitor();
        stage.show();
    }

    private void setUpDatabase(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + dotenv.get("DB_HOST") + ":" + dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME"));
        hikariConfig.setUsername(dotenv.get("DB_USER"));
        hikariConfig.setPassword(dotenv.get("DB_PASSWORD"));
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setInitializationFailTimeout(3);

        DBConnection.initPool(hikariConfig);
    }

    private void startConnectionMonitor() {
        this.connectionScheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicBoolean live = new AtomicBoolean(false);
        connectionScheduler.scheduleAtFixedRate(() -> {
            //initialize connection
            if(!DBConnection.isInitialized()){
                try {
                    setUpDatabase();
                    LOGGER.info("Database pool initialized.");

                    if(DBConnection.isConnected()){
                        if(mainController != null) {
                            Platform.runLater(() -> mainController.showDefaultView());
                        }else {
                            live.set(false);
                            LOGGER.fine("MainController not initialized yet, waiting...");
                        }
                    }else {
                        live.set(false);
                        LOGGER.warning("Database offline, retrying connection...");
                    }
                }catch (Exception e){
                    live.set(false);
                    LOGGER.warning("Database connection attempt failed: " + e.getMessage());
                }
            }
            if(DBConnection.isConnected() && !live.get()){
                LOGGER.info("Database connection established.");
                live.set(true);
                Platform.runLater(() -> mainController.showDefaultView());
            }
        }, 1, 15, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        connectionScheduler.shutdown();
        DBConnection.closePool();
    }
}
