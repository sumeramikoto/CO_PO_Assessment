package org.example.co_po_assessment;

import org.example.co_po_assessment.DB_Configuration.DBconfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DBconfigLoadTest {
    @Test
    void propertiesLoadedFromClasspath() {
        assertNotNull(DBconfig.getUrl(), "db.url should be configured");
        assertNotNull(DBconfig.getUserName(), "db.username should be configured");
        assertNotNull(DBconfig.getPassword(), "db.password should be configured");
        assertFalse(DBconfig.getUrl().isBlank(), "db.url should not be blank");
        assertFalse(DBconfig.getUserName().isBlank(), "db.username should not be blank");
        assertFalse(DBconfig.getPassword().isBlank(), "db.password should not be blank");
    }
}

