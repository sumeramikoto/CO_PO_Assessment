package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacultyDatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "pass";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Faculty management operations
    public List<FacultyData> getAllFaculty() throws SQLException {
        String sql = "SELECT id, shortname, full_name, email FROM Faculty ORDER BY id";
        List<FacultyData> facultyList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                facultyList.add(new FacultyData(
                    rs.getString("id"),
                    rs.getString("shortname"),
                    rs.getString("full_name"),
                    rs.getString("email")
                ));
            }
        }
        return facultyList;
    }

    public void removeFaculty(String facultyId) throws SQLException {
        String sql = "DELETE FROM Faculty WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, facultyId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No faculty found with ID: " + facultyId);
            }
        }
    }

    // Faculty data class
    public static class FacultyData {
        public final String id;
        public final String shortname;
        public final String fullName;
        public final String email;

        public FacultyData(String id, String shortname, String fullName, String email) {
            this.id = id;
            this.shortname = shortname;
            this.fullName = fullName;
            this.email = email;
        }

        public String getId() { return id; }
        public String getShortname() { return shortname; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
    }
}
