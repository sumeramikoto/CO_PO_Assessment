package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursesDatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "sinhawiz123";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Get all courses
    public List<CourseData> getAllCourses() throws SQLException {
        String sql = "SELECT course_code, course_name, credits FROM Course ORDER BY course_code";
        List<CourseData> courses = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(new CourseData(
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getDouble("credits")
                ));
            }
        }
        return courses;
    }

    // Add a new course
    public void addCourse(String courseCode, String courseName, double credits) throws SQLException {
        String sql = "INSERT INTO Course (course_code, course_name, credits) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, courseName);
            stmt.setDouble(3, credits);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to add course");
            }
        }
    }

    // Remove a course
    public void removeCourse(String courseCode) throws SQLException {
        // First check if course is assigned to any faculty
        String checkAssignmentSql = "SELECT COUNT(*) FROM CourseAssignment WHERE course_code = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkAssignmentSql)) {
            checkStmt.setString(1, courseCode);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete course. It is currently assigned to faculty members.");
            }
        }

        // Check if course has enrollments
        String checkEnrollmentSql = "SELECT COUNT(*) FROM Enrollment WHERE course_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkEnrollmentSql)) {
            checkStmt.setString(1, courseCode);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete course. Students are enrolled in this course.");
            }
        }

        // If no dependencies, delete the course
        String deleteSql = "DELETE FROM Course WHERE course_code = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, courseCode);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Course not found or could not be deleted");
            }
        }
    }

    // Check if course code already exists
    public boolean courseExists(String courseCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Course WHERE course_code = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Data class for courses
    public static class CourseData {
        public final String courseCode;
        public final String courseName;
        public final double credits;

        public CourseData(String courseCode, String courseName, double credits) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public double getCredits() { return credits; }
    }
}
