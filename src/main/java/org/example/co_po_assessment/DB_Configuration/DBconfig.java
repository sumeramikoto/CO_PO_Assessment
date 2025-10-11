package org.example.co_po_assessment.DB_Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBconfig {
    private static final Properties prop = new Properties();
    static {
        boolean loaded = false;
        // 1) Try classpath root: src/main/resources/dbcreds.properties
        try (InputStream is = DBconfig.class.getClassLoader().getResourceAsStream("dbcreds.properties")) {
            if (is != null) {
                prop.load(is);
                loaded = true;
            }
        } catch (IOException ignored) {}
        // 2) Try same package on classpath (in case it's kept alongside the class)
        if (!loaded) {
            try (InputStream is = DBconfig.class.getResourceAsStream("/org/example/co_po_assessment/DB_Configuration/dbcreds.properties")) {
                if (is != null) {
                    prop.load(is);
                    loaded = true;
                }
            } catch (IOException ignored) {}
        }
        // 3) Fallback to working directory file (for dev overrides)
        if (!loaded) {
            try (FileInputStream fis = new FileInputStream("dbcreds.properties")) {
                prop.load(fis);
                loaded = true;
            } catch (IOException ignored) {}
        }
        if (!loaded) {
            System.err.println("DBconfig: Could not load dbcreds.properties from classpath or working directory. Database operations will fail.");
        }
    }
    public static String getUrl() {
        return prop.getProperty("db.url");
    }
    public static String getUserName() {
        return prop.getProperty("db.username");
    }
    public static String getPassword() {
        return prop.getProperty("db.password");
    }
}
