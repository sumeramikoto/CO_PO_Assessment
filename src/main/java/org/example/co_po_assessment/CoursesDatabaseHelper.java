package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursesDatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://u2pt07.h.filess.io:3307/SPL2_stiffstiff";
    private static final String DB_USER = "SPL2_stiffstiff";
    private static final String DB_PASSWORD = "44f45683637f5c4f3cba0ad2eb7966589c4c0a2f";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Get all courses (now including department & programme)
    public List<CourseData> getAllCourses() throws SQLException {
        String sql = "SELECT course_code, course_name, credits, department, programme FROM Course ORDER BY course_code, programme";
        List<CourseData> courses = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(new CourseData(
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getDouble("credits"),
                    rs.getString("department"),
                    rs.getString("programme")
                ));
            }
        }
        return courses;
    }

    // Add a new course (with department & programme)
    public void addCourse(String courseCode, String courseName, double credits, String department, String programme) throws SQLException {
        String sql = "INSERT INTO Course (course_code, course_name, credits, department, programme) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, courseName);
            stmt.setDouble(3, credits);
            stmt.setString(4, department);
            stmt.setString(5, programme);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to add course");
            }
        }
    }

    // Remove a course (by composite key)
    public void removeCourse(String courseCode, String programme) throws SQLException {
        // First check if course is assigned to any faculty
        String checkAssignmentSql = "SELECT COUNT(*) FROM CourseAssignment WHERE course_code = ? AND programme = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkAssignmentSql)) {
            checkStmt.setString(1, courseCode);
            checkStmt.setString(2, programme);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete course. It is currently assigned to faculty members.");
            }
        }

        // Check if course has enrollments
        String checkEnrollmentSql = "SELECT COUNT(*) FROM Enrollment WHERE course_id = ? AND programme = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkEnrollmentSql)) {
            checkStmt.setString(1, courseCode);
            checkStmt.setString(2, programme);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete course. Students are enrolled in this course.");
            }
        }

        // If no dependencies, delete the course
        String deleteSql = "DELETE FROM Course WHERE course_code = ? AND programme = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, programme);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Course not found or could not be deleted");
            }
        }
    }

    // Check if course (code + programme) already exists
    public boolean courseExists(String courseCode, String programme) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Course WHERE UPPER(course_code) = UPPER(?) AND programme = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, programme);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        }
    }

    // Data class for courses
    public static class CourseData {
        public final String courseCode;
        public final String courseName;
        public final double credits;
        public final String department;
        public final String programme;

        public CourseData(String courseCode, String courseName, double credits, String department, String programme) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
            this.department = department;
            this.programme = programme;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public double getCredits() { return credits; }
        public String getDepartment() { return department; }
        public String getProgramme() { return programme; }
    }
}
