package org.example.co_po_assessment.DB_helper;

import org.example.co_po_assessment.DB_Configuration.DBconfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursesDatabaseHelper {
    private static final String DB_URL = DBconfig.getUrl();
    private static final String DB_USER = DBconfig.getUserName();
    private static final String DB_PASSWORD = DBconfig.getPassword();

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

    // Assign a set of CO numbers (e.g., [1,2,3]) to a course (code + programme)
    public void assignCOsToCourse(String courseCode, String programme, List<Integer> coNumbers) throws SQLException {
        if (coNumbers == null || coNumbers.isEmpty()) return;
        String selectCoIdSql = "SELECT id FROM CO WHERE co_number = ?";
        String insertSql = "INSERT INTO Course_CO (course_code, programme, co_id) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement sel = conn.prepareStatement(selectCoIdSql);
             PreparedStatement ins = conn.prepareStatement(insertSql)) {
            conn.setAutoCommit(false);
            try {
                for (Integer n : coNumbers) {
                    if (n == null) continue;
                    String label = "CO" + n;
                    Integer coId = null;
                    sel.setString(1, label);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (rs.next()) coId = rs.getInt(1);
                    }
                    if (coId == null) throw new SQLException("Unknown CO number: " + n);
                    ins.setString(1, courseCode);
                    ins.setString(2, programme);
                    ins.setInt(3, coId);
                    try {
                        ins.executeUpdate();
                    } catch (SQLIntegrityConstraintViolationException dup) {
                        // duplicate mapping -> ignore
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Assign a set of PO numbers (e.g., [1,2,5]) to a course (code + programme)
    public void assignPOsToCourse(String courseCode, String programme, List<Integer> poNumbers) throws SQLException {
        if (poNumbers == null || poNumbers.isEmpty()) return;
        String selectPoIdSql = "SELECT id FROM PO WHERE po_number = ?";
        String insertSql = "INSERT INTO Course_PO (course_code, programme, po_id) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement sel = conn.prepareStatement(selectPoIdSql);
             PreparedStatement ins = conn.prepareStatement(insertSql)) {
            conn.setAutoCommit(false);
            try {
                for (Integer n : poNumbers) {
                    if (n == null) continue;
                    String label = "PO" + n;
                    Integer poId = null;
                    sel.setString(1, label);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (rs.next()) poId = rs.getInt(1);
                    }
                    if (poId == null) throw new SQLException("Unknown PO number: " + n);
                    ins.setString(1, courseCode);
                    ins.setString(2, programme);
                    ins.setInt(3, poId);
                    try {
                        ins.executeUpdate();
                    } catch (SQLIntegrityConstraintViolationException dup) {
                        // duplicate mapping -> ignore
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
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

        // If no dependencies, delete CO/PO mappings and then the course
        String deleteCourseCo = "DELETE FROM Course_CO WHERE course_code = ? AND programme = ?";
        String deleteCoursePo = "DELETE FROM Course_PO WHERE course_code = ? AND programme = ?";
        String deleteSql = "DELETE FROM Course WHERE course_code = ? AND programme = ?";

        try (Connection conn = getConnection();
             PreparedStatement delCo = conn.prepareStatement(deleteCourseCo);
             PreparedStatement delPo = conn.prepareStatement(deleteCoursePo);
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            conn.setAutoCommit(false);
            try {
                delCo.setString(1, courseCode);
                delCo.setString(2, programme);
                delCo.executeUpdate();

                delPo.setString(1, courseCode);
                delPo.setString(2, programme);
                delPo.executeUpdate();

                stmt.setString(1, courseCode);
                stmt.setString(2, programme);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Course not found or could not be deleted");
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
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
