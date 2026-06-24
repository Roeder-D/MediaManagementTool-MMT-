package de.srh_dr.mediamanagementtoolmmt.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LanguageManager {
    private static final String BUNDLE_PATH = "de.srh_dr.mediamanagementtoolmmt.messages";
    private static ResourceBundle bundle;

    //select language
    static{
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_PATH, init());
        } catch (MissingResourceException e) {
            setLanguage("en");
        }
    }

    //get Locale/region
    private static Locale init(){
        String languageSetting;
        try {
            languageSetting = ConfigManager.getAppLanguage();
        }catch (Exception e){
            System.err.println("Failed to load language settings: " + e.getMessage());
            languageSetting = "default";
        }

        Locale locale;
        if(languageSetting.isEmpty() || languageSetting.equalsIgnoreCase("default")){
            locale = Locale.getDefault();
        }else{
            locale = new Locale.Builder().setLanguage(languageSetting).build();
        }
        return locale;
    }

    public static void setLanguage(String languageCode){
        bundle = ResourceBundle.getBundle(BUNDLE_PATH, Locale.of(languageCode));
    }

    //Provides resources to the rest of the program
    public static String getString(String key){
        try {
            return bundle.getString(key);
        }catch (MissingResourceException e){
            System.err.println("Warning: Missing translation for key: " + key);
            return "!" + key + "!";
        }
    }
    public static ResourceBundle getBundle(){
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                try{
                    return bundle.getString(key);
                }catch(MissingResourceException e){
                    System.err.println("Warning: Missing translation for key: " + key);
                    return "!" + key + "!";
                }
            }

            @Override
            public Enumeration<String> getKeys() {
                return bundle.getKeys();
            }

            @Override
            public boolean containsKey(String key) {
                return true;
            }
        };
    }
}
