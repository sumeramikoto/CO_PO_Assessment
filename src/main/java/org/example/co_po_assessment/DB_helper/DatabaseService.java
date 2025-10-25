package org.example.co_po_assessment.DB_helper;

import org.example.co_po_assessment.DB_Configuration.DBconfig;
import org.example.co_po_assessment.utilities.PasswordUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class DatabaseService {
    // ------------------------------------------------------------------
    // Basic singleton + connection
    // ------------------------------------------------------------------
    private static final String DB_URL = DBconfig.getUrl();
    private static final String DB_USER = DBconfig.getUserName();
    private static final String DB_PASSWORD = DBconfig.getPassword();
    private static DatabaseService instance;

    private DatabaseService() {
        try { upgradeLegacyPasswords(); } catch (Exception ignored) {}
    }
    public static DatabaseService getInstance() { if (instance == null) instance = new DatabaseService(); return instance; }
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ------------------------------------------------------------------
    // Password upgrade (legacy plain -> hashed)
    // ------------------------------------------------------------------
    private void upgradeLegacyPasswords() throws SQLException {
        try (Connection conn = getConnection()) {
            // Faculty
            try (PreparedStatement ps = conn.prepareStatement("SELECT email, password FROM Faculty"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString(1); String pwd = rs.getString(2);
                    if (pwd != null && !PasswordUtils.isHashed(pwd)) {
                        try (PreparedStatement up = conn.prepareStatement("UPDATE Faculty SET password=? WHERE email=?")) {
                            up.setString(1, PasswordUtils.hash(pwd)); up.setString(2, email); up.executeUpdate();
                        }
                    }
                }
            }
            // Admin
            try (PreparedStatement ps = conn.prepareStatement("SELECT email, password FROM Admin"); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString(1); String pwd = rs.getString(2);
                    if (pwd != null && !PasswordUtils.isHashed(pwd)) {
                        try (PreparedStatement up = conn.prepareStatement("UPDATE Admin SET password=? WHERE email=?")) {
                            up.setString(1, PasswordUtils.hash(pwd)); up.setString(2, email); up.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Academic year helpers & legacy wrappers
    // ------------------------------------------------------------------
    public List<String> getAcademicYears() throws SQLException {
        List<String> years = new ArrayList<>();
        int current = java.time.Year.now().getValue();
        for (int y = current - 5; y <= current + 2; y++) years.add(y + "-" + (y + 1));
        return years;
    }
    private String latestAcademicYear() throws SQLException { List<String> y = getAcademicYears(); return y.get(y.size() - 1); }

    // ------------------------------------------------------------------
    // Course / instructor listing
    // ------------------------------------------------------------------
    public List<String> getCourseCodes() throws SQLException {
        String sql = "SELECT DISTINCT course_code FROM Course ORDER BY course_code"; List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(rs.getString(1)); }
        return list;
    }
    public List<String> getInstructorNames() throws SQLException {
        String sql = "SELECT DISTINCT full_name FROM Faculty ORDER BY full_name"; List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(rs.getString(1)); }
        return list;
    }
    public List<String> getCoursesByInstructor(String instructorName) throws SQLException {
        String sql = "SELECT DISTINCT c.course_code, c.programme, c.course_name FROM Course c JOIN CourseAssignment ca ON c.course_code=ca.course_code AND c.programme=ca.programme JOIN Faculty f ON ca.faculty_id=f.id WHERE f.full_name=? ORDER BY c.course_code,c.programme";
        List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1, instructorName); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(rs.getString(1)+" / "+rs.getString(2)+" - "+rs.getString(3)); }}
        return list;
    }

    public CourseData getCourseByCodeAndInstructor(String courseCode, String programme, String instructorName) throws SQLException {
        String sql = "SELECT c.course_code,c.course_name,c.credits,c.department,c.programme,f.full_name FROM Course c JOIN CourseAssignment ca ON c.course_code=ca.course_code AND c.programme=ca.programme JOIN Faculty f ON ca.faculty_id=f.id WHERE c.course_code=? AND c.programme=? AND f.full_name=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1, courseCode); ps.setString(2, programme); ps.setString(3, instructorName); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new CourseData(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getString(5), rs.getString(6)); }}
        return null;
    }

    // ------------------------------------------------------------------
    // Insert / assignment helpers
    // ------------------------------------------------------------------
    public void insertCourse(String courseCode, String courseName, double credits, String department, String programme) throws SQLException {
        String sql = "INSERT INTO Course (course_code, course_name, credits, department, programme) VALUES (?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1, courseCode); ps.setString(2, courseName); ps.setDouble(3, credits); ps.setString(4, department); ps.setString(5, programme); ps.executeUpdate(); }
    }
    public void insertCourse(String courseCode, String courseName, double credits) throws SQLException { insertCourse(courseCode, courseName, credits, "CSE", "BSc"); }
    public void insertFaculty(String id, String shortname, String fullName, String email, String password) throws SQLException {
        if (!PasswordUtils.isHashed(password)) password = PasswordUtils.hash(password);
        String sql = "INSERT INTO Faculty (id, shortname, full_name, email, password) VALUES (?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1, id); ps.setString(2, shortname); ps.setString(3, fullName); ps.setString(4, email); ps.setString(5, password); ps.executeUpdate(); }
    }
    public void insertStudent(String id, int batch, String name, String email, String department, String programme) throws SQLException {
        String sql = "INSERT INTO Student (id,batch,name,email,department,programme) VALUES (?,?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,id); ps.setInt(2,batch); ps.setString(3,name); ps.setString(4,email); ps.setString(5,department); ps.setString(6,programme); ps.executeUpdate(); }
    }
    public void insertAdmin(String email, String password) throws SQLException {
        if (!PasswordUtils.isHashed(password)) password = PasswordUtils.hash(password);
        String sql = "INSERT INTO Admin (email,password) VALUES (?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,email); ps.setString(2,password); ps.executeUpdate(); }
    }
    public void assignCourseToFaculty(String facultyId, String courseCode, String academicYear) throws SQLException {
        String fetch = "SELECT department, programme FROM Course WHERE course_code=?";
        String ins = "INSERT INTO CourseAssignment (faculty_id, course_code, academic_year, department, programme) VALUES (?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(fetch)) {
            ps.setString(1, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Course not found");
                String dept = rs.getString(1); String prog = rs.getString(2);
                try (PreparedStatement insPs = c.prepareStatement(ins)) {
                    insPs.setString(1, facultyId); insPs.setString(2, courseCode); insPs.setString(3, academicYear);
                    insPs.setString(4, dept); insPs.setString(5, prog); insPs.executeUpdate();
                }
            }
        }
    }
    public void assignCourseToFaculty(String facultyId, String courseCode, String academicYear, String department, String programme) throws SQLException {
        String ins = "INSERT INTO CourseAssignment (faculty_id, course_code, academic_year, department, programme) VALUES (?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(ins)) { ps.setString(1,facultyId); ps.setString(2,courseCode); ps.setString(3,academicYear); ps.setString(4,department); ps.setString(5,programme); ps.executeUpdate(); }
    }

    // ------------------------------------------------------------------
    // Authentication
    // ------------------------------------------------------------------
    public boolean authenticateAdmin(String email, String rawPassword) throws SQLException {
        String sql = "SELECT password FROM Admin WHERE email=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) return false; String stored = rs.getString(1); boolean ok = PasswordUtils.matches(rawPassword, stored); if (ok && !PasswordUtils.isHashed(stored)) { try (PreparedStatement up = c.prepareStatement("UPDATE Admin SET password=? WHERE email=?")) { up.setString(1, PasswordUtils.hash(rawPassword)); up.setString(2, email); up.executeUpdate(); } } return ok; }
        }
    }
    public boolean authenticateFaculty(String email, String rawPassword) throws SQLException {
        String sql = "SELECT password FROM Faculty WHERE email=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) return false; String stored = rs.getString(1); boolean ok = PasswordUtils.matches(rawPassword, stored); if (ok && !PasswordUtils.isHashed(stored)) { try (PreparedStatement up = c.prepareStatement("UPDATE Faculty SET password=? WHERE email=?")) { up.setString(1, PasswordUtils.hash(rawPassword)); up.setString(2, email); up.executeUpdate(); } } return ok; }
        }
    }

    // ------------------------------------------------------------------
    // Course info / update
    // ------------------------------------------------------------------
    public CourseData getCourseInfo(String courseCode, String programme) throws SQLException {
        String sql = "SELECT c.course_code,c.course_name,c.credits,c.department,c.programme,f.full_name FROM Course c LEFT JOIN CourseAssignment ca ON c.course_code=ca.course_code AND c.programme=ca.programme LEFT JOIN Faculty f ON ca.faculty_id=f.id WHERE c.course_code=? AND c.programme=? LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1, courseCode); ps.setString(2, programme); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new CourseData(rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getString(5), rs.getString(6)); }}
        return null;
    }

    public void updateCourseInfo(String code, String programme, String name, double credits, String dept) throws SQLException {
        String sql = "UPDATE Course SET course_name=?, credits=?, department=? WHERE course_code=? AND programme=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,name); ps.setDouble(2,credits); ps.setString(3,dept); ps.setString(4,code); ps.setString(5,programme); ps.executeUpdate(); }
    }

    // ------------------------------------------------------------------
    // Assessments (programme-aware)
    // ------------------------------------------------------------------
    public void ensureAssessmentsExist(String courseCode, String programme, String academicYear) throws SQLException {
        try (Connection c = getConnection()) {
            for (int i=1;i<=4;i++) {
                try (PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO Quiz (course_id, programme, quiz_number, academic_year) VALUES (?,?,?,?)")) {
                    ps.setString(1, courseCode); ps.setString(2, programme); ps.setInt(3, i); ps.setString(4, academicYear); ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO Mid (course_id, programme, academic_year) VALUES (?,?,?)")) { ps.setString(1, courseCode); ps.setString(2, programme); ps.setString(3, academicYear); ps.executeUpdate(); }
            try (PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO Final (course_id, programme, academic_year) VALUES (?,?,?)")) { ps.setString(1, courseCode); ps.setString(2, programme); ps.setString(3, academicYear); ps.executeUpdate(); }
        }
    }

    // New helpers: allowed COs/POs for a course
    public List<String> getAllowedCOsForCourse(String courseCode, String programme) throws SQLException {
        String sql = "SELECT co.co_number FROM Course_CO cc JOIN CO co ON cc.co_id = co.id WHERE cc.course_code=? AND cc.programme=? ORDER BY co.id";
        List<String> out = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setString(2, programme);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }
    public List<String> getAllowedPOsForCourse(String courseCode, String programme) throws SQLException {
        String sql = "SELECT po.po_number FROM Course_PO cp JOIN PO po ON cp.po_id = po.id WHERE cp.course_code=? AND cp.programme=? ORDER BY po.id";
        List<String> out = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseCode);
            ps.setString(2, programme);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString(1));
            }
        }
        return out;
    }

    public void saveQuizQuestion(String courseCode, String programme, int quizNumber, String title, double marks, String co, String po, String academicYear) throws SQLException {
        // Only allow CO/PO that are mapped to this course via Course_CO and Course_PO
        String sql = "INSERT INTO QuizQuestion (quiz_id, title, marks, co_id, po_id) " +
                "SELECT q.id, ?, ?, co.id, po.id " +
                "FROM Quiz q, CO co, PO po " +
                "WHERE q.course_id=? AND q.programme=? AND q.quiz_number=? AND q.academic_year=? " +
                "AND co.co_number=? AND po.po_number=? " +
                "AND EXISTS (SELECT 1 FROM Course_CO cc WHERE cc.course_code=q.course_id AND cc.programme=q.programme AND cc.co_id=co.id) " +
                "AND EXISTS (SELECT 1 FROM Course_PO cp WHERE cp.course_code=q.course_id AND cp.programme=q.programme AND cp.po_id=po.id)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,title); ps.setDouble(2,marks);
            ps.setString(3,courseCode); ps.setString(4,programme); ps.setInt(5,quizNumber); ps.setString(6,academicYear);
            ps.setString(7,co); ps.setString(8,po);
            int updated = ps.executeUpdate();
            if (updated == 0) throw new SQLException("Selected CO/PO not allowed for this course");
        }
    }
    public void saveMidQuestion(String courseCode, String programme, String title, double marks, String co, String po, String academicYear) throws SQLException {
        String sql = "INSERT INTO MidQuestion (mid_id, title, marks, co_id, po_id) " +
                "SELECT m.id, ?, ?, co.id, po.id " +
                "FROM Mid m, CO co, PO po " +
                "WHERE m.course_id=? AND m.programme=? AND m.academic_year=? " +
                "AND co.co_number=? AND po.po_number=? " +
                "AND EXISTS (SELECT 1 FROM Course_CO cc WHERE cc.course_code=m.course_id AND cc.programme=m.programme AND cc.co_id=co.id) " +
                "AND EXISTS (SELECT 1 FROM Course_PO cp WHERE cp.course_code=m.course_id AND cp.programme=m.programme AND cp.po_id=po.id)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,title); ps.setDouble(2,marks);
            ps.setString(3,courseCode); ps.setString(4,programme); ps.setString(5,academicYear);
            ps.setString(6,co); ps.setString(7,po);
            int updated = ps.executeUpdate();
            if (updated == 0) throw new SQLException("Selected CO/PO not allowed for this course");
        }
    }
    public void saveFinalQuestion(String courseCode, String programme, String title, double marks, String co, String po, String academicYear) throws SQLException {
        String sql = "INSERT INTO FinalQuestion (final_id, title, marks, co_id, po_id) " +
                "SELECT f.id, ?, ?, co.id, po.id " +
                "FROM Final f, CO co, PO po " +
                "WHERE f.course_id=? AND f.programme=? AND f.academic_year=? " +
                "AND co.co_number=? AND po.po_number=? " +
                "AND EXISTS (SELECT 1 FROM Course_CO cc WHERE cc.course_code=f.course_id AND cc.programme=f.programme AND cc.co_id=co.id) " +
                "AND EXISTS (SELECT 1 FROM Course_PO cp WHERE cp.course_code=f.course_id AND cp.programme=f.programme AND cp.po_id=po.id)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1,title); ps.setDouble(2,marks);
            ps.setString(3,courseCode); ps.setString(4,programme); ps.setString(5,academicYear);
            ps.setString(6,co); ps.setString(7,po);
            int updated = ps.executeUpdate();
            if (updated == 0) throw new SQLException("Selected CO/PO not allowed for this course");
        }
    }

    // ------------------------------------------------------------------
    // Retrieve questions (programme-aware)
    // ------------------------------------------------------------------
    public List<QuestionData> getQuizQuestions(String courseCode, String programme, int quizNumber, String ay) throws SQLException {
        String sql = "SELECT qq.id, qq.title, qq.marks, c.co_number, p.po_number FROM QuizQuestion qq JOIN Quiz q ON qq.quiz_id=q.id JOIN CO c ON qq.co_id=c.id JOIN PO p ON qq.po_id=p.id WHERE q.course_id=? AND q.programme=? AND q.quiz_number=? AND q.academic_year=? ORDER BY qq.title";
        List<QuestionData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); ps.setInt(3,quizNumber); ps.setString(4,ay); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new QuestionData(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getString(5))); }}
        return list;
    }
    public List<QuestionData> getMidQuestions(String courseCode, String programme, String ay) throws SQLException {
        String sql = "SELECT mq.id, mq.title, mq.marks, c.co_number, p.po_number FROM MidQuestion mq JOIN Mid m ON mq.mid_id=m.id JOIN CO c ON mq.co_id=c.id JOIN PO p ON mq.po_id=p.id WHERE m.course_id=? AND m.programme=? AND m.academic_year=? ORDER BY mq.title";
        List<QuestionData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); ps.setString(3,ay); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new QuestionData(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getString(5))); }}
        return list;
    }
    public List<QuestionData> getFinalQuestions(String courseCode, String programme, String ay) throws SQLException {
        String sql = "SELECT fq.id, fq.title, fq.marks, c.co_number, p.po_number FROM FinalQuestion fq JOIN Final f ON fq.final_id=f.id JOIN CO c ON fq.co_id=c.id JOIN PO p ON fq.po_id=p.id WHERE f.course_id=? AND f.programme=? AND f.academic_year=? ORDER BY fq.title";
        List<QuestionData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); ps.setString(3,ay); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new QuestionData(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getString(5))); }}
        return list;
    }

    // ------------------------------------------------------------------
    // Enrollment retrieval & management (programme-aware)
    // ------------------------------------------------------------------
    public List<StudentData> getEnrolledStudents(String courseCode, String programme, String ay) throws SQLException {
        String sql = "SELECT DISTINCT s.id,s.name,s.email,s.batch,s.programme,s.department FROM Student s JOIN Enrollment e ON s.id=e.student_id WHERE e.course_id=? AND e.programme=? AND e.academic_year=? ORDER BY s.id";
        List<StudentData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); ps.setString(3,ay); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new StudentData(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6))); }}
        return list;
    }
    public List<StudentData> getEnrolledStudentsAllYears(String courseCode, String programme) throws SQLException {
        String sql = "SELECT DISTINCT s.id,s.name,s.email,s.batch,s.programme,s.department FROM Student s JOIN Enrollment e ON s.id=e.student_id WHERE e.course_id=? AND e.programme=? ORDER BY s.id";
        List<StudentData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new StudentData(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6))); }}
        return list;
    }

    public void enrollStudents(String courseCode, String programme, String academicYear, List<String> studentIds) throws SQLException {
        if (studentIds==null || studentIds.isEmpty()) return;
        // Filter eligible students (dept & programme match course)
        String courseDept;
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT department FROM Course WHERE course_code=? AND programme=?")) {
            ps.setString(1, courseCode); ps.setString(2, programme); try (ResultSet rs = ps.executeQuery()) { if (!rs.next()) throw new SQLException("Course not found"); courseDept = rs.getString(1); }
        }
        String placeholders = studentIds.stream().map(s -> "?").collect(java.util.stream.Collectors.joining(","));
        List<String> eligible = new ArrayList<>();
        String fetch = "SELECT id, department, programme FROM Student WHERE id IN ("+placeholders+")";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(fetch)) {
            int idx=1; for (String sid: studentIds) ps.setString(idx++, sid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (courseDept.equalsIgnoreCase(rs.getString(2)) && programme.equalsIgnoreCase(rs.getString(3))) eligible.add(rs.getString(1));
                }
            }
        }
        if (eligible.isEmpty()) return;
        ensureEnrollmentYearColumn();
        String sql = "INSERT IGNORE INTO Enrollment (student_id, course_id, programme, academic_year) VALUES (?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (String sid : eligible) { ps.setString(1,sid); ps.setString(2,courseCode); ps.setString(3,programme); ps.setString(4,academicYear); ps.addBatch(); }
            ps.executeBatch();
        }
    }

    public int unenrollStudents(String courseCode, String programme, String academicYear, List<String> studentIds) throws SQLException {
        if (studentIds==null || studentIds.isEmpty()) return 0; ensureEnrollmentYearColumn();
        String sql = "DELETE FROM Enrollment WHERE course_id=? AND programme=? AND academic_year=? AND student_id=?";
        int total=0; try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { for (String sid: studentIds){ ps.setString(1,courseCode); ps.setString(2,programme); ps.setString(3,academicYear); ps.setString(4,sid); ps.addBatch(); } int[] res=ps.executeBatch(); for(int r:res) if(r>=0) total+=r; }
        return total;
    }

    // ------------------------------------------------------------------
    // Student marks retrieval (programme-aware)
    // ------------------------------------------------------------------
    public List<StudentMarksData> getStudentQuizMarks(String courseCode, String programme, int quizNumber, String ay) throws SQLException {
        String sql = "SELECT s.id,s.name,qq.id,qq.title,qq.marks,sqm.marks_obtained FROM Student s JOIN Enrollment e ON s.id=e.student_id AND e.course_id=? AND e.programme=? AND e.academic_year=? JOIN Quiz q ON q.course_id=e.course_id AND q.programme=e.programme AND q.quiz_number=? AND q.academic_year=e.academic_year JOIN QuizQuestion qq ON qq.quiz_id=q.id LEFT JOIN StudentQuizMarks sqm ON sqm.student_id=s.id AND sqm.quiz_question_id=qq.id ORDER BY s.id,qq.title";
        List<StudentMarksData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); ps.setString(3,ay); ps.setInt(4,quizNumber); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { Object val = rs.getObject(6); Double obtained = val==null? null : ((Number)val).doubleValue(); list.add(new StudentMarksData(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getDouble(5), obtained)); } }}
        return list;
    }
    public List<StudentMarksData> getStudentMidMarks(String courseCode, String programme, String ay) throws SQLException {
        String sql = "SELECT s.id,s.name,mq.id,mq.title,mq.marks,smm.marks_obtained FROM Student s JOIN Enrollment e ON s.id=e.student_id AND e.course_id=? AND e.programme=? AND e.academic_year=? JOIN Mid m ON m.course_id=e.course_id AND m.programme=e.programme AND m.academic_year=e.academic_year JOIN MidQuestion mq ON mq.mid_id=m.id LEFT JOIN StudentMidMarks smm ON smm.student_id=s.id AND smm.mid_question_id=mq.id ORDER BY s.id,mq.title";
        List<StudentMarksData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); ps.setString(3,ay); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { Object val = rs.getObject(6); Double obtained = val==null? null : ((Number)val).doubleValue(); list.add(new StudentMarksData(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getDouble(5), obtained)); } }}
        return list;
    }
    public List<StudentMarksData> getStudentFinalMarks(String courseCode, String programme, String ay) throws SQLException {
        String sql = "SELECT s.id,s.name,fq.id,fq.title,fq.marks,sfm.marks_obtained FROM Student s JOIN Enrollment e ON s.id=e.student_id AND e.course_id=? AND e.programme=? AND e.academic_year=? JOIN Final f ON f.course_id=e.course_id AND f.programme=e.programme AND f.academic_year=e.academic_year JOIN FinalQuestion fq ON fq.final_id=f.id LEFT JOIN StudentFinalMarks sfm ON sfm.student_id=s.id AND sfm.final_question_id=fq.id ORDER BY s.id,fq.title";
        List<StudentMarksData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseCode); ps.setString(2,programme); ps.setString(3,ay); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { Object val = rs.getObject(6); Double obtained = val==null? null : ((Number)val).doubleValue(); list.add(new StudentMarksData(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getDouble(5), obtained)); } }}
        return list;
    }

    // ------------------------------------------------------------------
    // Save marks for individual question results (already by question ID)
    // ------------------------------------------------------------------
    public void saveStudentQuizMarks(String studentId, int quizQuestionId, double marksObtained) throws SQLException {
        String sql = "INSERT INTO StudentQuizMarks (student_id, quiz_question_id, marks_obtained) VALUES (?,?,?) ON DUPLICATE KEY UPDATE marks_obtained=VALUES(marks_obtained)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,studentId); ps.setInt(2,quizQuestionId); ps.setDouble(3,marksObtained); ps.executeUpdate(); }
    }
    public void saveStudentMidMarks(String studentId, int midQuestionId, double marksObtained) throws SQLException {
        String sql = "INSERT INTO StudentMidMarks (student_id, mid_question_id, marks_obtained) VALUES (?,?,?) ON DUPLICATE KEY UPDATE marks_obtained=VALUES(marks_obtained)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,studentId); ps.setInt(2,midQuestionId); ps.setDouble(3,marksObtained); ps.executeUpdate(); }
    }
    public void saveStudentFinalMarks(String studentId, int finalQuestionId, double marksObtained) throws SQLException {
        String sql = "INSERT INTO StudentFinalMarks (student_id, final_question_id, marks_obtained) VALUES (?,?,?) ON DUPLICATE KEY UPDATE marks_obtained=VALUES(marks_obtained)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,studentId); ps.setInt(2,finalQuestionId); ps.setDouble(3,marksObtained); ps.executeUpdate(); }
    }

    // ------------------------------------------------------------------
    // Bulk update (exam totals) - legacy logic preserved
    // ------------------------------------------------------------------
    public void updateStudentMarks(String studentId, String courseId, String examType, int examNumber, double marks) throws SQLException {
        String sql = switch (examType.toLowerCase()) {
            case "quiz" -> "UPDATE StudentQuizMarks SET marks_obtained=? WHERE student_id=? AND quiz_question_id IN (SELECT qq.id FROM QuizQuestion qq JOIN Quiz q ON qq.quiz_id=q.id WHERE q.course_id=? AND q.quiz_number=?)";
            case "mid" -> "UPDATE StudentMidMarks SET marks_obtained=? WHERE student_id=? AND mid_question_id IN (SELECT mq.id FROM MidQuestion mq JOIN Mid m ON mq.mid_id=m.id WHERE m.course_id=?)";
            case "final" -> "UPDATE StudentFinalMarks SET marks_obtained=? WHERE student_id=? AND final_question_id IN (SELECT fq.id FROM FinalQuestion fq JOIN Final f ON fq.final_id=f.id WHERE f.course_id=?)";
            default -> throw new IllegalArgumentException("Invalid exam type: " + examType);
        };
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, marks); ps.setString(2, studentId); ps.setString(3, courseId); if (examType.equalsIgnoreCase("quiz")) ps.setInt(4, examNumber); ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    // Bulk enroll with schema auto-upgrade
    // ------------------------------------------------------------------
    public void enrollStudents(String courseId, String academicYear, List<String> studentIds) throws SQLException {
        if (studentIds == null || studentIds.isEmpty()) return;
        // Fetch course department & programme
        String courseDept = null, courseProg = null;
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT department, programme FROM Course WHERE course_code=?")) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { courseDept = rs.getString(1); courseProg = rs.getString(2); }
                else throw new SQLException("Course not found: " + courseId);
            }
        }
        // Build placeholders for IN clause
        String placeholders = studentIds.stream().map(s -> "?").collect(java.util.stream.Collectors.joining(","));
        List<String> eligible = new ArrayList<>();
        String fetchStudents = "SELECT id, department, programme FROM Student WHERE id IN (" + placeholders + ")";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(fetchStudents)) {
            int idx = 1; for (String id : studentIds) ps.setString(idx++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString(1);
                    String dept = rs.getString(2);
                    String prog = rs.getString(3);
                    if (dept != null && prog != null && dept.equalsIgnoreCase(courseDept) && prog.equalsIgnoreCase(courseProg)) {
                        eligible.add(sid);
                    }
                }
            }
        }
        if (eligible.isEmpty()) return; // no matching students
        ensureEnrollmentYearColumn();
        String sql = "INSERT IGNORE INTO Enrollment (student_id, course_id, programme, academic_year) VALUES (?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (String sid : eligible) { ps.setString(1,sid); ps.setString(2,courseId); ps.setString(3,courseProg); ps.setString(4,academicYear); ps.addBatch(); }
            ps.executeBatch();
        } catch (SQLException ex) {
            if (ex.getMessage()!=null && ex.getMessage().toLowerCase().contains("unknown column 'academic_year'")) {
                String legacy = "INSERT IGNORE INTO Enrollment (student_id, course_id) VALUES (?,?)";
                try (Connection c2 = getConnection(); PreparedStatement ps2 = c2.prepareStatement(legacy)) { for (String sid: eligible){ ps2.setString(1,sid); ps2.setString(2,courseId); ps2.addBatch(); } ps2.executeBatch(); }
            } else throw ex;
        }
    }

    // New: bulk unenroll selected students for a specific academic year
    public int unenrollStudents(String courseId, String academicYear, List<String> studentIds) throws SQLException {
        if (studentIds == null || studentIds.isEmpty()) return 0;
        ensureEnrollmentYearColumn();
        String sql = "DELETE FROM Enrollment WHERE course_id=? AND academic_year=? AND student_id=?";
        int total = 0;
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (String sid : studentIds) { ps.setString(1, courseId); ps.setString(2, academicYear); ps.setString(3, sid); ps.addBatch(); }
            int[] res = ps.executeBatch();
            for (int r : res) { if (r >= 0) total += r; }
        }
        return total;
    }
    private static volatile boolean enrollmentYearChecked = false;
    private synchronized void ensureEnrollmentYearColumn() throws SQLException {
        if (enrollmentYearChecked) return;
        try (Connection c = getConnection()) {
            boolean has;
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='Enrollment' AND COLUMN_NAME='academic_year'"); ResultSet rs = ps.executeQuery()) { rs.next(); has = rs.getInt(1) > 0; }
            if (!has) {
                try (Statement st = c.createStatement()) { st.executeUpdate("ALTER TABLE Enrollment ADD COLUMN academic_year VARCHAR(9) NOT NULL DEFAULT '2024-2025' AFTER course_id"); }
                try (PreparedStatement idx = c.prepareStatement("SHOW INDEX FROM Enrollment"); ResultSet rs = idx.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getInt("Non_unique") == 0 && "student_id".equalsIgnoreCase(rs.getString("Column_name"))) {
                            try (Statement drop = c.createStatement()) { drop.executeUpdate("ALTER TABLE Enrollment DROP INDEX `" + rs.getString("Key_name") + "`"); } catch (SQLException ignored) {}
                        }
                    }
                }
                try (Statement st = c.createStatement()) { st.executeUpdate("ALTER TABLE Enrollment ADD UNIQUE KEY uniq_enrollment (student_id, course_id, academic_year)"); } catch (SQLException ignored) {}
            }
        }
        enrollmentYearChecked = true;
    }

    // ------------------------------------------------------------------
    // Question ID helpers
    // ------------------------------------------------------------------
    public Integer getQuizQuestionId(String courseId, int quizNumber, String title, String ay) throws SQLException {
        String sql = "SELECT qq.id FROM QuizQuestion qq JOIN Quiz q ON qq.quiz_id=q.id WHERE q.course_id=? AND q.quiz_number=? AND q.academic_year=? AND qq.title=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseId); ps.setInt(2,quizNumber); ps.setString(3,ay); ps.setString(4,title); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); } }
        return null;
    }
    public Integer getQuizQuestionId(String cId, int quizNumber, String title) throws SQLException { return getQuizQuestionId(cId, quizNumber, title, latestAcademicYear()); }
    public Integer getMidQuestionId(String courseId, String title, String ay) throws SQLException {
        String sql = "SELECT mq.id FROM MidQuestion mq JOIN Mid m ON mq.mid_id=m.id WHERE m.course_id=? AND m.academic_year=? AND mq.title=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseId); ps.setString(2,ay); ps.setString(3,title); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); } }
        return null;
    }
    public Integer getMidQuestionId(String cId, String title) throws SQLException { return getMidQuestionId(cId, title, latestAcademicYear()); }
    public Integer getFinalQuestionId(String courseId, String title, String ay) throws SQLException {
        String sql = "SELECT fq.id FROM FinalQuestion fq JOIN Final f ON fq.final_id=f.id WHERE f.course_id=? AND f.academic_year=? AND fq.title=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,courseId); ps.setString(2,ay); ps.setString(3,title); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); } }
        return null;
    }
    public Integer getFinalQuestionId(String cId, String title) throws SQLException { return getFinalQuestionId(cId, title, latestAcademicYear()); }

    // ------------------------------------------------------------------
    // Delete questions (academic-year aware)
    // ------------------------------------------------------------------
    public boolean deleteQuizQuestion(String courseId, String programme, int quizNumber, String title, String ay) throws SQLException {
        String sql = "DELETE qq FROM QuizQuestion qq JOIN Quiz q ON qq.quiz_id=q.id WHERE q.course_id=? AND q.programme=? AND q.quiz_number=? AND q.academic_year=? AND qq.title=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, programme); ps.setInt(3, quizNumber); ps.setString(4, ay); ps.setString(5, title);
            return ps.executeUpdate() > 0;
        }
    }
    public boolean deleteMidQuestion(String courseId, String programme, String title, String ay) throws SQLException {
        String sql = "DELETE mq FROM MidQuestion mq JOIN Mid m ON mq.mid_id=m.id WHERE m.course_id=? AND m.programme=? AND m.academic_year=? AND mq.title=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, programme); ps.setString(3, ay); ps.setString(4, title);
            return ps.executeUpdate() > 0;
        }
    }
    public boolean deleteFinalQuestion(String courseId, String programme, String title, String ay) throws SQLException {
        String sql = "DELETE fq FROM FinalQuestion fq JOIN Final f ON fq.final_id=f.id WHERE f.course_id=? AND f.programme=? AND f.academic_year=? AND fq.title=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, programme); ps.setString(3, ay); ps.setString(4, title);
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------------------------------------------------
    // Thresholds (CO/PO) CRUD
    // ------------------------------------------------------------------
    public Map<String, Double> getThresholds() throws SQLException {
        String sql = "SELECT type, percentage FROM THRESHOLDS";
        Map<String, Double> map = new HashMap<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getDouble(2));
            }
        }
        return map;
    }

    /** Update provided thresholds within a single transaction. Values are percentages 0..100. */
    public void updateThresholds(Map<String, Double> updates) throws SQLException {
        if (updates == null || updates.isEmpty()) return;
        try (Connection c = getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                String upSql = "UPDATE THRESHOLDS SET percentage=? WHERE type=?";
                String insSql = "INSERT INTO THRESHOLDS (type, percentage) VALUES (?,?)";
                try (PreparedStatement up = c.prepareStatement(upSql); PreparedStatement ins = c.prepareStatement(insSql)) {
                    for (Map.Entry<String, Double> e : updates.entrySet()) {
                        String type = e.getKey();
                        Double pct = e.getValue();
                        if (pct == null) continue;
                        // Try update
                        up.setDouble(1, pct);
                        up.setString(2, type);
                        int n = up.executeUpdate();
                        if (n == 0) {
                            // Fallback insert (in case seed rows missing)
                            ins.setString(1, type);
                            ins.setDouble(2, pct);
                            ins.executeUpdate();
                        }
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(oldAuto);
            }
        }
    }

    // ------------------------------------------------------------------
    // Data classes
    // ------------------------------------------------------------------
    public static class QuestionData {
        public final int id;
        public final String title;
        public final double marks;
        public final String co;
        public final String po;
        public QuestionData(int id,String title,double marks,String co,String po){
            this.id=id;this.title=title;this.marks=marks;this.co=co;this.po=po;
        }
    }
    public static class StudentData { public final String id,name,email; public final int batch; public final String programme,department; public StudentData(String id,String name,String email,int batch,String programme,String department){this.id=id;this.name=name;this.email=email;this.batch=batch;this.programme=programme;this.department=department;} }
    public static class CourseData { public final String courseCode, courseName, department, programme, instructorName; public final double credits; public CourseData(String courseCode,String courseName,double credits,String department,String programme,String instructorName){this.courseCode=courseCode;this.courseName=courseName;this.credits=credits;this.department=department;this.programme=programme;this.instructorName=instructorName;} }
    public static class StudentMarksData { public final String studentId, studentName, questionTitle; public final int questionId; public final double maxMarks; public final Double marksObtained; public StudentMarksData(String sid,String sname,int qid,String qTitle,double max,Double obtained){this.studentId=sid;this.studentName=sname;this.questionId=qid;this.questionTitle=qTitle;this.maxMarks=max;this.marksObtained=obtained;} }
    public static class StudentPerformanceData { public final String studentId, studentName, assessmentType, questionTitle, coNumber, poNumber; public final int batch, assessmentNumber; public final double maxMarks, marksObtained; public StudentPerformanceData(String sid,String sname,int batch,String at,int an,String qt,double max,double got,String co,String po){this.studentId=sid;this.studentName=sname;this.batch=batch;this.assessmentType=at;this.assessmentNumber=an;this.questionTitle=qt;this.maxMarks=max;this.marksObtained=got;this.coNumber=co;this.poNumber=po;} }
    public static class FacultyInfo { public final String id; public final String shortname, fullName, email; public FacultyInfo(String id,String shortname,String fullName,String email){this.id=id;this.shortname=shortname;this.fullName=fullName;this.email=email;} public String getId(){return id;} public String getShortname(){return shortname;} public String getFullName(){return fullName;} public String getEmail(){return email;} }
    public static class FacultyCourseAssignment { public final String courseCode, courseName, academicYear, department, programme; public FacultyCourseAssignment(String courseCode,String courseName,String academicYear,String department,String programme){this.courseCode=courseCode;this.courseName=courseName;this.academicYear=academicYear;this.department=department;this.programme=programme;} public String getCourseCode(){return courseCode;} public String getCourseName(){return courseName;} public String getAcademicYear(){return academicYear;} public String getDepartment(){return department;} public String getProgramme(){return programme;} }

    public FacultyInfo getFacultyInfo(String email) throws SQLException {
        String sql = "SELECT id, shortname, full_name, email FROM Faculty WHERE email=?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,email); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return new FacultyInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)); }}
        return null;
    }
    public List<FacultyCourseAssignment> getAssignmentsForFaculty(String facultyId) throws SQLException {
        String sql = "SELECT DISTINCT ca.course_code,c.course_name,ca.academic_year,ca.department,ca.programme FROM CourseAssignment ca JOIN Course c ON ca.course_code=c.course_code WHERE ca.faculty_id=? ORDER BY ca.academic_year DESC, ca.course_code";
        List<FacultyCourseAssignment> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) { ps.setString(1,facultyId); try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new FacultyCourseAssignment(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5))); }}
        return list;
    }
    public List<String> getCoursesDetailed() throws SQLException {
        String sql = "SELECT course_code, course_name, department, programme FROM Course ORDER BY course_code";
        List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString(1) + " - " + rs.getString(2) + " - " + rs.getString(3) + " - " + rs.getString(4));
            }
        }
        return list;
    }

    // ------------------------------------------------------------------
    // Culmination Courses helpers
    // ------------------------------------------------------------------
    public List<String> getDistinctProgrammesFromCourses() throws SQLException {
        String sql = "SELECT DISTINCT programme FROM Course WHERE programme IS NOT NULL AND programme<>'' ORDER BY programme";
        List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    public List<String> getCoursesForProgramme(String programme) throws SQLException {
        String sql = "SELECT course_code, course_name FROM Course WHERE programme=? ORDER BY course_code";
        List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, programme);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1) + " - " + rs.getString(2));
            }
        }
        return list;
    }

    public List<String> getCulminationCourses(String programme) throws SQLException {
        String sql = "SELECT course_code FROM CulminationCourse WHERE programme=? ORDER BY course_code";
        List<String> codes = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, programme);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) codes.add(rs.getString(1)); }
        }
        return codes;
    }

    public List<String> getMissingPOsForCourses(String programme, List<String> courseCodes, int totalPOs) throws SQLException {
        if (courseCodes == null || courseCodes.isEmpty()) return Arrays.asList("PO1","PO2","PO3","PO4","PO5","PO6","PO7","PO8","PO9","PO10","PO11","PO12").subList(0, Math.min(totalPOs, 12));
        // Build IN clause placeholders
        String in = String.join(",", java.util.Collections.nCopies(courseCodes.size(), "?"));
        String sql = "SELECT DISTINCT po.po_number FROM Course_PO cp JOIN PO po ON cp.po_id=po.id WHERE cp.programme=? AND cp.course_code IN (" + in + ")";
        Set<String> present = new HashSet<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, programme);
            int idx = 2;
            for (String code : courseCodes) ps.setString(idx++, code);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) present.add(rs.getString(1)); }
        }
        List<String> missing = new ArrayList<>();
        for (int i = 1; i <= totalPOs; i++) {
            String label = "PO" + i;
            if (!present.contains(label)) missing.add(label);
        }
        return missing;
    }

    public void saveCulminationCourses(String programme, List<String> courseCodes) throws SQLException {
        if (programme == null || programme.isBlank()) throw new SQLException("Programme required");
        if (courseCodes == null) courseCodes = java.util.Collections.emptyList();
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement del = c.prepareStatement("DELETE FROM CulminationCourse WHERE programme=?")) {
                    del.setString(1, programme);
                    del.executeUpdate();
                }
                if (!courseCodes.isEmpty()) {
                    String insSql = "INSERT INTO CulminationCourse (course_code, programme) VALUES (?,?)";
                    try (PreparedStatement ins = c.prepareStatement(insSql)) {
                        for (String code : new LinkedHashSet<>(courseCodes)) { // dedupe
                            ins.setString(1, code);
                            ins.setString(2, programme);
                            ins.addBatch();
                        }
                        ins.executeBatch();
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    // ------------------------------------------------------------------
    // Graduating Students helpers
    // ------------------------------------------------------------------
    public List<String> getDistinctProgrammesFromStudents() throws SQLException {
        String sql = "SELECT DISTINCT programme FROM Student WHERE programme IS NOT NULL AND programme<>'' ORDER BY programme";
        List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    public List<Integer> getBatchesForProgramme(String programme) throws SQLException {
        String sql = "SELECT DISTINCT batch FROM Student WHERE programme=? ORDER BY batch";
        List<Integer> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, programme);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(rs.getInt(1)); }
        }
        return list;
    }

    public List<StudentData> getStudentsByProgrammeAndBatch(String programme, int batch) throws SQLException {
        String sql = "SELECT id,name,email,batch,programme,department FROM Student WHERE programme=? AND batch=? ORDER BY id";
        List<StudentData> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, programme); ps.setInt(2, batch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new StudentData(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6)));
            }
        }
        return list;
    }

    public List<String> getGraduatingStudentIds(String programme, int batch) throws SQLException {
        String sql = "SELECT gs.id FROM GraduatingStudent gs JOIN Student s ON s.id=gs.id WHERE s.programme=? AND s.batch=? ORDER BY gs.id";
        List<String> ids = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, programme); ps.setInt(2, batch);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ids.add(rs.getString(1)); }
        }
        return ids;
    }

    public void saveGraduatingStudents(String programme, int batch, List<String> studentIds) throws SQLException {
        if (programme == null || programme.isBlank()) throw new SQLException("Programme required");
        try (Connection c = getConnection()) {
            boolean old = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                // Clear any existing graduating flags for this programme+batch
                String delSql = "DELETE FROM GraduatingStudent WHERE id IN (SELECT id FROM Student WHERE programme=? AND batch=?)";
                try (PreparedStatement del = c.prepareStatement(delSql)) { del.setString(1, programme); del.setInt(2, batch); del.executeUpdate(); }
                if (studentIds != null && !studentIds.isEmpty()) {
                    String insSql = "INSERT IGNORE INTO GraduatingStudent (id) VALUES (?)";
                    try (PreparedStatement ins = c.prepareStatement(insSql)) {
                        for (String sid : new LinkedHashSet<>(studentIds)) { ins.setString(1, sid); ins.addBatch(); }
                        ins.executeBatch();
                    }
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(old);
            }
        }
    }
}
