package org.example.co_po_assessment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt; // backward compatibility

public final class PasswordUtils {
    private static final String PREFIX = "sha256$"; // new unsalted format: sha256$<Base64Digest>

    private PasswordUtils() {}

    // Hash raw password with plain SHA-256 (no salt) producing deterministic digest
    public static String hash(String raw) {
        if (raw == null) throw new IllegalArgumentException("Password cannot be null");
        byte[] digest = sha256(raw.getBytes(StandardCharsets.UTF_8));
        return PREFIX + Base64.getEncoder().encodeToString(digest);
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean isHashed(String value) {
        if (value == null) return false;
        if (value.startsWith(PREFIX)) {
            // Accept either new 2-part form or legacy 3-part salted form
            int parts = value.split("\\$").length;
            return parts == 2 || parts == 3;
        }
        return value.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }

    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) return false;
        if (stored.startsWith(PREFIX)) {
            String[] parts = stored.split("\\$");
            // New unsalted format: sha256$<digest>
            if (parts.length == 2) {
                try {
                    byte[] expected = Base64.getDecoder().decode(parts[1]);
                    byte[] actual = sha256(raw.getBytes(StandardCharsets.UTF_8));
                    return constantTimeEquals(expected, actual);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
            // Legacy salted format: sha256$<salt>$<digest>
            if (parts.length == 3) {
                try {
                    byte[] salt = Base64.getDecoder().decode(parts[1]);
                    byte[] expected = Base64.getDecoder().decode(parts[2]);
                    // Reconstruct salted digest (salt || raw)
                    byte[] combined = new byte[salt.length + raw.length()];
                    System.arraycopy(salt, 0, combined, 0, salt.length);
                    System.arraycopy(raw.getBytes(StandardCharsets.UTF_8), 0, combined, salt.length, raw.length());
                    byte[] actual = sha256(combined);
                    return constantTimeEquals(expected, actual);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
            return false; // malformed
        }
        if (stored.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$")) {
            try { return BCrypt.checkpw(raw, stored); } catch (Exception ignored) { return false; }
        }
        return raw.equals(stored);
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) result |= a[i] ^ b[i];
        return result == 0;
    }

    public static void main(String[] args) {
        String raw = "password";
        String hashed = hash(raw);
        System.out.println("Raw: " + raw);
        System.out.println("Hashed: " + hashed);
        System.out.println("Matches: " + matches(raw, hashed));
        System.out.println("Matches wrong: " + matches("wrongpassword", hashed));
    }
}
