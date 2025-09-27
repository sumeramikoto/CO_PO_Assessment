package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseAssignmentDatabaseHelper {
    private static final String DB_URL = "jdbc:mysql://u2pt07.h.filess.io:3307/SPL2_stiffstiff";
    private static final String DB_USER = "SPL2_stiffstiff";
    private static final String DB_PASSWORD = "44f45683637f5c4f3cba0ad2eb7966589c4c0a2f";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Get all course assignments (now with department & programme) – join includes programme
    public List<CourseAssignmentData> getAllCourseAssignments() throws SQLException {
        String sql = """
            SELECT ca.course_code, c.course_name, f.full_name AS faculty_name, ca.academic_year, ca.department, ca.programme
            FROM CourseAssignment ca
            JOIN Course c ON ca.course_code = c.course_code AND ca.programme = c.programme
            JOIN Faculty f ON ca.faculty_id = f.id
            ORDER BY ca.course_code, ca.programme, ca.academic_year
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
                    rs.getString("academic_year"),
                    rs.getString("department"),
                    rs.getString("programme")
                ));
            }
        }
        return assignments;
    }

    // Get all available courses (showing code, name, department, programme)
    public List<String> getAllCourses() throws SQLException {
        String sql = "SELECT course_code, course_name, department, programme FROM Course ORDER BY course_code, programme";
        List<String> courses = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(rs.getString("course_code") + " - " + rs.getString("course_name") + " - " + rs.getString("department") + " - " + rs.getString("programme"));
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

            while (rs.next()) { faculty.add(rs.getString("full_name")); }
        }
        return faculty;
    }

    // Assign course to faculty (copy department & programme from Course table)
    public void assignCourse(String courseCode, String programme, String facultyName, String academicYear) throws SQLException {
        try (Connection conn = getConnection()) {
            ensureNewUniqueConstraint(conn);
            // Pre-check duplicate for same composite (course_code, programme, academic_year, department)
            String dupCheck = "SELECT 1 FROM CourseAssignment ca JOIN Course c ON ca.course_code=c.course_code AND ca.programme=c.programme WHERE ca.course_code=? AND ca.programme=? AND ca.academic_year=? AND ca.department=c.department LIMIT 1";
            try (PreparedStatement dps = conn.prepareStatement(dupCheck)) {
                dps.setString(1, courseCode); dps.setString(2, programme); dps.setString(3, academicYear);
                try (ResultSet rs = dps.executeQuery()) { if (rs.next()) throw new SQLException("DUPLICATE_COURSE_YEAR"); }
            }
            String sql = """
            INSERT INTO CourseAssignment (faculty_id, course_code, programme, academic_year, department)
            SELECT f.id, c.course_code, c.programme, ?, c.department
            FROM Faculty f JOIN Course c ON c.course_code = ? AND c.programme = ?
            WHERE f.full_name = ?
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, academicYear);
                stmt.setString(2, courseCode);
                stmt.setString(3, programme);
                stmt.setString(4, facultyName);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) throw new SQLException("Faculty or Course not found for assignment");
            }
        } catch (SQLException ex) {
            if ("DUPLICATE_COURSE_YEAR".equals(ex.getMessage())) {
                throw new SQLException("This course is already assigned for the selected academic year with the same department & programme.");
            }
            throw ex;
        }
    }

    // Remove course assignment (composite key)
    public void removeCourseAssignment(String courseCode, String programme, String facultyName, String academicYear) throws SQLException {
        String sql = """
            DELETE ca FROM CourseAssignment ca
            JOIN Faculty f ON ca.faculty_id = f.id
            WHERE ca.course_code = ? AND ca.programme = ? AND f.full_name = ? AND ca.academic_year = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, programme);
            stmt.setString(3, facultyName);
            stmt.setString(4, academicYear);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) throw new SQLException("Course assignment not found");
        }
    }

    private void ensureNewUniqueConstraint(Connection conn) throws SQLException {
        // Detect if the new unique index (course_code, academic_year, department, programme) exists
        String idxSql = "SELECT INDEX_NAME, GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) cols FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='CourseAssignment' GROUP BY INDEX_NAME";
        boolean hasNew = false;
        String oldIndexToDrop = null;
        try (PreparedStatement ps = conn.prepareStatement(idxSql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String cols = rs.getString(2);
                if ("course_code,programme,academic_year,department".equalsIgnoreCase(cols) || "course_code,academic_year,department,programme".equalsIgnoreCase(cols)) { hasNew = true; }
            }
        }
        if (hasNew) return;
        conn.setAutoCommit(false);
        try (Statement st = conn.createStatement()) {
            // Remove duplicates keeping lowest faculty_id
            st.executeUpdate("DELETE ca1 FROM CourseAssignment ca1 JOIN CourseAssignment ca2 ON ca1.course_code=ca2.course_code AND ca1.programme=ca2.programme AND ca1.academic_year=ca2.academic_year AND ca1.department=ca2.department AND ca1.faculty_id>ca2.faculty_id");
            st.executeUpdate("ALTER TABLE CourseAssignment ADD UNIQUE KEY uniq_course_assignment (course_code, programme, academic_year, department)");
            conn.commit();
        } catch (SQLException ex) { conn.rollback(); throw ex; } finally { conn.setAutoCommit(true); }
    }

    private boolean existsDuplicateAssignment(Connection conn, String courseCode, String academicYear) throws SQLException {
        String sql = "SELECT 1 FROM CourseAssignment WHERE course_code=? AND academic_year=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) { ps.setString(1, courseCode); ps.setString(2, academicYear); try (ResultSet rs = ps.executeQuery()) { return rs.next(); }}
    }

    // Data class for course assignments
    public static class CourseAssignmentData {
        public final String courseCode;
        public final String courseName;
        public final String facultyName;
        public final String academicYear;
        public final String department;
        public final String programme;

        public CourseAssignmentData(String courseCode, String courseName, String facultyName, String academicYear, String department, String programme) {
            this.courseCode = courseCode; this.courseName = courseName; this.facultyName = facultyName; this.academicYear = academicYear; this.department = department; this.programme = programme; }
        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getFacultyName() { return facultyName; }
        public String getAcademicYear() { return academicYear; }
        public String getDepartment() { return department; }
        public String getProgramme() { return programme; }
    }
}
