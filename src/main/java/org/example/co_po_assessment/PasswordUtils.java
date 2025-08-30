package org.example.co_po_assessment;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {
    private static final int COST = 12; // work factor
    private PasswordUtils() {}

    public static String hash(String raw) {
        if (raw == null) throw new IllegalArgumentException("Password cannot be null");
        return BCrypt.hashpw(raw, BCrypt.gensalt(COST));
    }

    public static boolean isHashed(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }

    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) return false;
        if (isHashed(stored)) {
            return BCrypt.checkpw(raw, stored);
        }
        // Legacy plain text fallback
        return raw.equals(stored);
    }
}

