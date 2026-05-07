package de.srh_dr.mediamanagementtoolmmt.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LanguageManager {
    private static final String BUNDLE_PATH = "de.srh_2551.mediamanagementtoolmmt.messages";
    private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PATH);

    //default language
    static{
        setLanguage("de");
    }

    public static void setLanguage(String languageCode){
        bundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.of(languageCode));
    }

    public static String getString(String key){
        try {
            return bundle.getString(key);
        }catch (MissingResourceException e){
            System.err.println("Warning: Missing translation for key: " + key);
            return "!" + key + "!";
        }
    }
}
