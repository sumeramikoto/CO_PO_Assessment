package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "sinhawiz123";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Get all students for management
    public List<StudentData> getAllStudents() throws SQLException {
        String sql = "SELECT id, name, email, batch, programme, department FROM Student ORDER BY id";
        List<StudentData> students = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(new StudentData(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getInt("batch"),
                    rs.getString("programme"),
                    rs.getString("department")
                ));
            }
        }
        return students;
    }

    public void removeStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM Student WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("No student found with ID: " + studentId);
            }
        }
    }

    // Student data class
    public static class StudentData {
        public final String id;
        public final String name;
        public final String email;
        public final int batch;
        public final String programme;
        public final String department;

        public StudentData(String id, String name, String email, int batch, String programme, String department) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.batch = batch;
            this.programme = programme;
            this.department = department;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public int getBatch() { return batch; }
        public String getProgramme() { return programme; }
        public String getDepartment() { return department; }
    }
}
