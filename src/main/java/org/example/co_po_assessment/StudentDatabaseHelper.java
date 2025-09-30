package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "pass";

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

    // New helper methods for enrollment management filtering
    public List<Integer> getDistinctBatches() throws SQLException {
        String sql = "SELECT DISTINCT batch FROM Student ORDER BY batch";
        List<Integer> batches = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) batches.add(rs.getInt(1));
        }
        return batches;
    }
    public List<String> getDistinctDepartments() throws SQLException {
        String sql = "SELECT DISTINCT department FROM Student WHERE department IS NOT NULL AND department<>'' ORDER BY department";
        List<String> list = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(rs.getString(1)); }
        return list;
    }
    public List<String> getDistinctProgrammes() throws SQLException {
        String sql = "SELECT DISTINCT programme FROM Student WHERE programme IS NOT NULL AND programme<>'' ORDER BY programme";
        List<String> list = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(rs.getString(1)); }
        return list;
    }
    public List<StudentData> getStudents(Integer batch, String department, String programme) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT id, name, email, batch, programme, department FROM Student WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (batch != null) { sb.append(" AND batch = ?"); params.add(batch); }
        if (department != null && !department.isBlank()) { sb.append(" AND department = ?"); params.add(department); }
        if (programme != null && !programme.isBlank()) { sb.append(" AND programme = ?"); params.add(programme); }
        sb.append(" ORDER BY id");
        List<StudentData> students = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
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
        }
        return students;
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
