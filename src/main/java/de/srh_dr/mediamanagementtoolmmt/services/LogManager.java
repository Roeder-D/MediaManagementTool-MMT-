package de.srh_dr.mediamanagementtoolmmt.services;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
    private static final Logger rootLogger = Logger.getLogger("");

    public static void setup(){
        try {
            String userHome = System.getProperty("user.home");
            FileHandler fileHandler = getFileHandler(userHome);

            rootLogger.addHandler(fileHandler);

            //setup minimum level for logging
            rootLogger.setLevel(Level.INFO);

            rootLogger.info("LogManager successfully initialized. Application starting.");
        }catch (Exception e){
            System.err.println("LogManager initialization failed. " +  e.getMessage());
        }

    }

    private static FileHandler getFileHandler(String userHome) throws IOException {
        String logDirPath = userHome + File.separator + "mediaManagementtool" +  File.separator + "logs";
        File logDir = new File(logDirPath);

        if (!logDir.exists()) {
            if(!logDir.mkdirs()){
                throw new IOException("Could not create log directory.");
            }

        }

        String logFilePath = logDirPath + File.separator + "app.log";

        // Parameters: path, size limit (5MB), file count (keep 3 backups), append mode (true)
        FileHandler fileHandler = new FileHandler(logFilePath, 1024 * 1024 * 5, 3, true);

        //human-readable
        fileHandler.setFormatter(new SimpleFormatter());
        return fileHandler;
    }


}
