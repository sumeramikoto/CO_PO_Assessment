package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseAssignmentDatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "sinhawiz123";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Get all course assignments
    public List<CourseAssignmentData> getAllCourseAssignments() throws SQLException {
        String sql = """
            SELECT ca.course_code, c.course_name, f.full_name as faculty_name, ca.academic_year
            FROM CourseAssignment ca
            JOIN Course c ON ca.course_code = c.course_code
            JOIN Faculty f ON ca.faculty_id = f.id
            ORDER BY ca.course_code, ca.academic_year
            """;

        List<CourseAssignmentData> assignments = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                assignments.add(new CourseAssignmentData(
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getString("faculty_name"),
                    rs.getString("academic_year")
                ));
            }
        }
        return assignments;
    }

    // Get all available courses
    public List<String> getAllCourses() throws SQLException {
        String sql = "SELECT course_code, course_name FROM Course ORDER BY course_code";
        List<String> courses = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(rs.getString("course_code") + " - " + rs.getString("course_name"));
            }
        }
        return courses;
    }

    // Get all faculty members
    public List<String> getAllFaculty() throws SQLException {
        String sql = "SELECT id, full_name FROM Faculty ORDER BY full_name";
        List<String> faculty = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                faculty.add(rs.getString("full_name"));
            }
        }
        return faculty;
    }

    // Assign course to faculty
    public void assignCourse(String courseCode, String facultyName, String academicYear) throws SQLException {
        String sql = """
            INSERT INTO CourseAssignment (faculty_id, course_code, academic_year)
            SELECT f.id, ?, ?
            FROM Faculty f
            WHERE f.full_name = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, academicYear);
            stmt.setString(3, facultyName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Faculty not found: " + facultyName);
            }
        }
    }

    // Remove course assignment
    public void removeCourseAssignment(String courseCode, String facultyName, String academicYear) throws SQLException {
        String sql = """
            DELETE ca FROM CourseAssignment ca
            JOIN Faculty f ON ca.faculty_id = f.id
            WHERE ca.course_code = ? AND f.full_name = ? AND ca.academic_year = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, facultyName);
            stmt.setString(3, academicYear);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Course assignment not found");
            }
        }
    }

    // Data class for course assignments
    public static class CourseAssignmentData {
        public final String courseCode;
        public final String courseName;
        public final String facultyName;
        public final String academicYear;

        public CourseAssignmentData(String courseCode, String courseName, String facultyName, String academicYear) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.facultyName = facultyName;
            this.academicYear = academicYear;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getFacultyName() { return facultyName; }
        public String getAcademicYear() { return academicYear; }
    }
}
