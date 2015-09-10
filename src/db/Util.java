package db;

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 * @author Lazar Vujadinovic
 */
public class Util {
    private Properties properties;
    private static Util INSTANCE;

    public static Util getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Util();
        }
        return INSTANCE;
    }

    private Util() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("db.properties"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getDBURL() {
        return properties.getProperty(properties.getProperty("current_db") + "_url");
    }

    public String getDBUser() {
        return properties.getProperty(properties.getProperty("current_db") + "_user");
    }

    public String getDBPassword() {
        return properties.getProperty(properties.getProperty("current_db") + "_password");
    }

    public String getDBDriver() {
        return properties.getProperty(properties.getProperty("current_db") + "_driver");
    }

}
