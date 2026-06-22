package de.srh_dr.mediamanagementtoolmmt.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigManager {
    private static final String USER_HOME = System.getProperty("user.home");
    private static final Path EXTERNAL_CONFIG_PATH = Paths.get(USER_HOME, ".mmt", "config", "app.properties");
    private static final Properties properties = new Properties();

    static {
        boolean foundConfig = false;
        if(Files.exists(EXTERNAL_CONFIG_PATH)) {
            try(InputStream in = Files.newInputStream(EXTERNAL_CONFIG_PATH)){
                properties.load(in);
                foundConfig = true;
            }catch(Exception ignored){
                System.err.println("External config found but failed to load.");
            }
        }

        if(!foundConfig) {
            try (InputStream in = ConfigManager.class.getResourceAsStream("/de/srh_dr/mediamanagementtoolmmt/config/app.properties")) {
                if (in == null) {
                    throw new ExceptionInInitializerError();
                } else {
                    properties.load(in);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    //READ
    public static List<String> getISBNEnabledMediaTypes(){
        String rawValue = properties.getProperty("APP_ISBN_MEDIA_TYPES");
        return Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .toList();
    }

    public static String getAppLanguage(){
        String rawValue = properties.getProperty("APP_LANGUAGE", "default");
        return rawValue.trim();
    }

    //WRITE
    public static void setProperty(String key, String value){
        properties.setProperty(key, value);

        try{
            Files.createDirectories(EXTERNAL_CONFIG_PATH.getParent());

            try(OutputStream out = Files.newOutputStream(EXTERNAL_CONFIG_PATH)){
                properties.store(out, "MMT User Settings");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
