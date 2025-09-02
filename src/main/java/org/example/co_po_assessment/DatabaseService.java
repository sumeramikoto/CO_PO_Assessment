package org.example.co_po_assessment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "pass";

    private static DatabaseService instance;

    private DatabaseService() {
        try {
            upgradeLegacyPasswords();
        } catch (Exception ignored) {
            // Swallow to avoid startup failure; logging could be added
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void upgradeLegacyPasswords() throws SQLException {
        try (Connection conn = getConnection()) {
            // Faculty
            try (PreparedStatement ps = conn.prepareStatement("SELECT email, password FROM Faculty");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString(1);
                    String pwd = rs.getString(2);
                    if (pwd != null && !PasswordUtils.isHashed(pwd)) {
                        String hashed = PasswordUtils.hash(pwd);
                        try (PreparedStatement up = conn.prepareStatement("UPDATE Faculty SET password=? WHERE email=?")) {
                            up.setString(1, hashed);
                            up.setString(2, email);
                            up.executeUpdate();
                        }
                    }
                }
            }
            // Admin
            try (PreparedStatement ps = conn.prepareStatement("SELECT email, password FROM Admin");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString(1);
                    String pwd = rs.getString(2);
                    if (pwd != null && !PasswordUtils.isHashed(pwd)) {
                        String hashed = PasswordUtils.hash(pwd);
                        try (PreparedStatement up = conn.prepareStatement("UPDATE Admin SET password=? WHERE email=?")) {
                            up.setString(1, hashed);
                            up.setString(2, email);
                            up.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    // Question related operations
    public void saveQuizQuestion(String courseId, int quizNumber, String title, double marks, String co, String po) throws SQLException {
        String sql = """
            INSERT INTO QuizQuestion (quiz_id, title, marks, co_id, po_id)
            SELECT q.id, ?, ?, co.id, po.id
            FROM Quiz q, CO co, PO po
            WHERE q.course_id = ? AND q.quiz_number = ?
            AND co.co_number = ?
            AND po.po_number = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setDouble(2, marks);
            stmt.setString(3, courseId);
            stmt.setInt(4, quizNumber);
            stmt.setString(5, co);
            stmt.setString(6, po);
            stmt.executeUpdate();
        }
    }

    public void saveMidQuestion(String courseId, String title, double marks, String co, String po) throws SQLException {
        String sql = """
            INSERT INTO MidQuestion (mid_id, title, marks, co_id, po_id)
            SELECT m.id, ?, ?, co.id, po.id
            FROM Mid m, CO co, PO po
            WHERE m.course_id = ?
            AND co.co_number = ?
            AND po.po_number = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setDouble(2, marks);
            stmt.setString(3, courseId);
            stmt.setString(4, co);
            stmt.setString(5, po);
            stmt.executeUpdate();
        }
    }

    public void saveFinalQuestion(String courseId, String title, double marks, String co, String po) throws SQLException {
        String sql = """
            INSERT INTO FinalQuestion (final_id, title, marks, co_id, po_id)
            SELECT f.id, ?, ?, co.id, po.id
            FROM Final f, CO co, PO po
            WHERE f.course_id = ?
            AND co.co_number = ?
            AND po.po_number = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setDouble(2, marks);
            stmt.setString(3, courseId);
            stmt.setString(4, co);
            stmt.setString(5, po);
            stmt.executeUpdate();
        }
    }

    // Retrieve questions
    public List<QuestionData> getQuizQuestions(String courseId, int quizNumber) throws SQLException {
        String sql = """
            SELECT qq.id, qq.title, qq.marks, c.co_number, p.po_number 
            FROM QuizQuestion qq
            JOIN Quiz q ON qq.quiz_id = q.id
            JOIN CO c ON qq.co_id = c.id
            JOIN PO p ON qq.po_id = p.id
            WHERE q.course_id = ? AND q.quiz_number = ?
            ORDER BY qq.title
            """;

        List<QuestionData> questions = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setInt(2, quizNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(new QuestionData(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getDouble("marks"),
                        rs.getString("co_number"),
                        rs.getString("po_number")
                ));
            }
        }
        return questions;
    }

    public List<QuestionData> getMidQuestions(String courseId) throws SQLException {
        String sql = """
            SELECT mq.id, mq.title, mq.marks, c.co_number, p.po_number 
            FROM MidQuestion mq
            JOIN Mid m ON mq.mid_id = m.id
            JOIN CO c ON mq.co_id = c.id
            JOIN PO p ON mq.po_id = p.id
            WHERE m.course_id = ?
            ORDER BY mq.title
            """;

        List<QuestionData> questions = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(new QuestionData(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getDouble("marks"),
                        rs.getString("co_number"),
                        rs.getString("po_number")
                ));
            }
        }
        return questions;
    }

    public List<QuestionData> getFinalQuestions(String courseId) throws SQLException {
        String sql = """
            SELECT fq.id, fq.title, fq.marks, c.co_number, p.po_number 
            FROM FinalQuestion fq
            JOIN Final f ON fq.final_id = f.id
            JOIN CO c ON fq.co_id = c.id
            JOIN PO p ON fq.po_id = p.id
            WHERE f.course_id = ?
            ORDER BY fq.title
            """;

        List<QuestionData> questions = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(new QuestionData(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getDouble("marks"),
                        rs.getString("co_number"),
                        rs.getString("po_number")
                ));
            }
        }
        return questions;
    }

    // Student operations
    public List<StudentData> getEnrolledStudents(String courseId) throws SQLException {
        String sql = """
            SELECT DISTINCT s.id, s.name, s.email, s.batch, s.programme, s.department
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            WHERE e.course_id = ?
            ORDER BY s.id
            """;

        List<StudentData> students = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

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

    // Course operations
    public CourseData getCourseInfo(String courseCode) throws SQLException {
        String sql = """
            SELECT c.course_code, c.course_name, c.credits, c.department, c.programme, f.full_name as instructor_name
            FROM Course c
            JOIN CourseAssignment ca ON c.course_code = ca.course_code
            JOIN Faculty f ON ca.faculty_id = f.id
            WHERE c.course_code = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new CourseData(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getDouble("credits"),
                        rs.getString("department"),
                        rs.getString("programme"),
                        rs.getString("instructor_name")
                );
            }
        }
        return null;
    }

    // Update course information
    public void updateCourseInfo(String courseCode, String courseName, double credits, String department, String programme) throws SQLException {
        String sql = "UPDATE Course SET course_name = ?, credits = ?, department = ?, programme = ? WHERE course_code = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseName);
            stmt.setDouble(2, credits);
            stmt.setString(3, department);
            stmt.setString(4, programme);
            stmt.setString(5, courseCode);
            stmt.executeUpdate();
        }
    }

    // legacy method signature retained for backward compatibility
    public void updateCourseInfo(String courseCode, String courseName, double credits) throws SQLException {
        // Fetch existing dept & programme to avoid losing them
        CourseData existing = getCourseInfo(courseCode);
        String dept = existing != null ? existing.department : ""; // fail-safe
        String prog = existing != null ? existing.programme : "";
        updateCourseInfo(courseCode, courseName, credits, dept, prog);
    }

    // Update instructor for a course
    public void updateCourseInstructor(String courseCode, String instructorName, String academicYear) throws SQLException {
        String sql = """
            UPDATE CourseAssignment ca
            JOIN Faculty f ON f.full_name = ?
            SET ca.faculty_id = f.id
            WHERE ca.course_code = ? AND ca.academic_year = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, instructorName);
            stmt.setString(2, courseCode);
            stmt.setString(3, academicYear);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("Instructor, course, or academic year not found, or assignment does not exist.");
            }
        }
    }

    // Create assessments if they don't exist
    public void ensureAssessmentsExist(String courseId, String academicYear) throws SQLException {
        try (Connection conn = getConnection()) {
            // Create quizzes
            for (int i = 1; i <= 4; i++) {
                String sql = "INSERT IGNORE INTO Quiz (course_id, quiz_number, academic_year) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, courseId);
                    stmt.setInt(2, i);
                    stmt.setString(3, academicYear);
                    stmt.executeUpdate();
                }
            }

            // Create mid exam
            String midSql = "INSERT IGNORE INTO Mid (course_id, academic_year) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(midSql)) {
                stmt.setString(1, courseId);
                stmt.setString(2, academicYear);
                stmt.executeUpdate();
            }

            // Create final exam
            String finalSql = "INSERT IGNORE INTO Final (course_id, academic_year) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(finalSql)) {
                stmt.setString(1, courseId);
                stmt.setString(2, academicYear);
                stmt.executeUpdate();
            }
        }
    }

    // Student marks operations
    public void saveStudentQuizMarks(String studentId, int quizQuestionId, double marksObtained) throws SQLException {
        String sql = """
            INSERT INTO StudentQuizMarks (student_id, quiz_question_id, marks_obtained)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setInt(2, quizQuestionId);
            stmt.setDouble(3, marksObtained);
            stmt.executeUpdate();
        }
    }

    public void saveStudentMidMarks(String studentId, int midQuestionId, double marksObtained) throws SQLException {
        String sql = """
            INSERT INTO StudentMidMarks (student_id, mid_question_id, marks_obtained)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setInt(2, midQuestionId);
            stmt.setDouble(3, marksObtained);
            stmt.executeUpdate();
        }
    }

    public void saveStudentFinalMarks(String studentId, int finalQuestionId, double marksObtained) throws SQLException {
        String sql = """
            INSERT INTO StudentFinalMarks (student_id, final_question_id, marks_obtained)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setInt(2, finalQuestionId);
            stmt.setDouble(3, marksObtained);
            stmt.executeUpdate();
        }
    }

    // Get student marks for specific assessments
    public List<StudentMarksData> getStudentQuizMarks(String courseId, int quizNumber) throws SQLException {
        String sql = """
            SELECT s.id as student_id, s.name, qq.id as question_id, qq.title, qq.marks as max_marks,
                   COALESCE(sqm.marks_obtained, 0) as marks_obtained
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Quiz q ON c.course_code = q.course_id AND q.quiz_number = ?
            JOIN QuizQuestion qq ON q.id = qq.quiz_id
            LEFT JOIN StudentQuizMarks sqm ON s.id = sqm.student_id AND qq.id = sqm.quiz_question_id
            WHERE c.course_code = ?
            ORDER BY s.id, qq.title
            """;

        List<StudentMarksData> marks = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizNumber);
            stmt.setString(2, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                marks.add(new StudentMarksData(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getInt("question_id"),
                        rs.getString("title"),
                        rs.getDouble("max_marks"),
                        rs.getDouble("marks_obtained")
                ));
            }
        }
        return marks;
    }

    public List<StudentMarksData> getStudentMidMarks(String courseId) throws SQLException {
        String sql = """
            SELECT s.id as student_id, s.name, mq.id as question_id, mq.title, mq.marks as max_marks,
                   COALESCE(smm.marks_obtained, 0) as marks_obtained
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Mid m ON c.course_code = m.course_id
            JOIN MidQuestion mq ON m.id = mq.mid_id
            LEFT JOIN StudentMidMarks smm ON s.id = smm.student_id AND mq.id = smm.mid_question_id
            WHERE c.course_code = ?
            ORDER BY s.id, mq.title
            """;

        List<StudentMarksData> marks = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                marks.add(new StudentMarksData(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getInt("question_id"),
                        rs.getString("title"),
                        rs.getDouble("max_marks"),
                        rs.getDouble("marks_obtained")
                ));
            }
        }
        return marks;
    }

    public List<StudentMarksData> getStudentFinalMarks(String courseId) throws SQLException {
        String sql = """
            SELECT s.id as student_id, s.name, fq.id as question_id, fq.title, fq.marks as max_marks,
                   COALESCE(sfm.marks_obtained, 0) as marks_obtained
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Final f ON c.course_code = f.course_id
            JOIN FinalQuestion fq ON f.id = fq.final_id
            LEFT JOIN StudentFinalMarks sfm ON s.id = sfm.student_id AND fq.id = sfm.final_question_id
            WHERE c.course_code = ?
            ORDER BY s.id, fq.title
            """;

        List<StudentMarksData> marks = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                marks.add(new StudentMarksData(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getInt("question_id"),
                        rs.getString("title"),
                        rs.getDouble("max_marks"),
                        rs.getDouble("marks_obtained")
                ));
            }
        }
        return marks;
    }

    // Get comprehensive student performance
    public List<StudentPerformanceData> getStudentPerformanceSummary(String courseId) throws SQLException {
        String sql = """
            SELECT s.id, s.name, s.batch,
                   'Quiz' as assessment_type, q.quiz_number as assessment_number,
                   qq.title as question_title, qq.marks as max_marks,
                   COALESCE(sqm.marks_obtained, 0) as marks_obtained,
                   co.co_number, po.po_number
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Quiz q ON c.course_code = q.course_id
            JOIN QuizQuestion qq ON q.id = qq.quiz_id
            LEFT JOIN StudentQuizMarks sqm ON s.id = sqm.student_id AND qq.id = sqm.quiz_question_id
            JOIN CO co ON qq.co_id = co.id
            JOIN PO po ON qq.po_id = po.id
            WHERE c.course_code = ?
            
            UNION ALL
            
            SELECT s.id, s.name, s.batch,
                   'Mid' as assessment_type, 0 as assessment_number,
                   mq.title as question_title, mq.marks as max_marks,
                   COALESCE(smm.marks_obtained, 0) as marks_obtained,
                   co.co_number, po.po_number
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Mid m ON c.course_code = m.course_id
            JOIN MidQuestion mq ON m.id = mq.mid_id
            LEFT JOIN StudentMidMarks smm ON s.id = smm.student_id AND mq.id = smm.mid_question_id
            JOIN CO co ON mq.co_id = co.id
            JOIN PO po ON mq.po_id = po.id
            WHERE c.course_code = ?
            
            UNION ALL
            
            SELECT s.id, s.name, s.batch,
                   'Final' as assessment_type, 0 as assessment_number,
                   fq.title as question_title, fq.marks as max_marks,
                   COALESCE(sfm.marks_obtained, 0) as marks_obtained,
                   co.co_number, po.po_number
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Final f ON c.course_code = f.course_id
            JOIN FinalQuestion fq ON f.id = fq.final_id
            LEFT JOIN StudentFinalMarks sfm ON s.id = sfm.student_id AND fq.id = sfm.final_question_id
            JOIN CO co ON fq.co_id = co.id
            JOIN PO po ON fq.po_id = po.id
            WHERE c.course_code = ?
            
            ORDER BY id, assessment_type, assessment_number, question_title
            """;

        List<StudentPerformanceData> performance = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setString(2, courseId);
            stmt.setString(3, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                performance.add(new StudentPerformanceData(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("batch"),
                        rs.getString("assessment_type"),
                        rs.getInt("assessment_number"),
                        rs.getString("question_title"),
                        rs.getDouble("max_marks"),
                        rs.getDouble("marks_obtained"),
                        rs.getString("co_number"),
                        rs.getString("po_number")
                ));
            }
        }
        return performance;
    }

    // Insert methods for initial data setup
    public void insertCourse(String courseCode, String courseName, double credits, String department, String programme) throws SQLException {
        String sql = "INSERT INTO Course (course_code, course_name, credits, department, programme) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, courseName);
            stmt.setDouble(3, credits);
            stmt.setString(4, department);
            stmt.setString(5, programme);
            stmt.executeUpdate();
        }
    }

    // Backward compatibility (will throw since required columns now) - attempt to infer? Use placeholders
    public void insertCourse(String courseCode, String courseName, double credits) throws SQLException {
        // Default placeholders if old code path is used
        insertCourse(courseCode, courseName, credits, "CSE", "BSc");
    }

    public void insertFaculty(int id, String shortname, String fullName, String email, String password) throws SQLException {
        String sql = "INSERT INTO Faculty (id, shortname, full_name, email, password) VALUES (?, ?, ?, ?, ?)";

        // Hash if not already hashed (BCrypt pattern)
        if (!PasswordUtils.isHashed(password)) {
            password = PasswordUtils.hash(password);
        }
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, shortname);
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setString(5, password);
            stmt.executeUpdate();
        }
    }

    // Added: insertStudent to support ManageStudentsController.addNewStudent()
    public void insertStudent(String id, int batch, String name, String email, String department, String programme) throws SQLException {
        String sql = "INSERT INTO Student (id, batch, name, email, department, programme) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setInt(2, batch);
            stmt.setString(3, name);
            stmt.setString(4, email);
            stmt.setString(5, department);
            stmt.setString(6, programme);
            stmt.executeUpdate();
        }
    }

    public void assignCourseToFaculty(int facultyId, String courseCode, String academicYear) throws SQLException {
        // Retrieve department & programme from Course table (since schema now requires them in CourseAssignment)
        String fetch = "SELECT department, programme FROM Course WHERE course_code = ?";
        String sql = "INSERT INTO CourseAssignment (faculty_id, course_code, academic_year, department, programme) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement get = conn.prepareStatement(fetch)) {
            get.setString(1, courseCode);
            try (ResultSet rs = get.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Course not found for code: " + courseCode);
                }
                String dept = rs.getString(1);
                String prog = rs.getString(2);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, facultyId);
                    stmt.setString(2, courseCode);
                    stmt.setString(3, academicYear);
                    stmt.setString(4, dept);
                    stmt.setString(5, prog);
                    stmt.executeUpdate();
                }
            }
        }
    }

    // Overload allowing explicit department/programme if needed
    public void assignCourseToFaculty(int facultyId, String courseCode, String academicYear, String department, String programme) throws SQLException {
        String sql = "INSERT INTO CourseAssignment (faculty_id, course_code, academic_year, department, programme) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, facultyId);
            stmt.setString(2, courseCode);
            stmt.setString(3, academicYear);
            stmt.setString(4, department);
            stmt.setString(5, programme);
            stmt.executeUpdate();
        }
    }


    // Admin operations
    public void insertAdmin(String email, String password) throws SQLException {
        String sql = "INSERT INTO Admin (email, password) VALUES (?, ?)";
        if (!PasswordUtils.isHashed(password)) {
            password = PasswordUtils.hash(password);
        }
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.executeUpdate();
        }
    }

    public boolean authenticateAdmin(String email, String rawPassword) throws SQLException {
        String select = "SELECT id, password FROM Admin WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String stored = rs.getString("password");
                boolean match = PasswordUtils.matches(rawPassword, stored);
                if (match && !PasswordUtils.isHashed(stored)) {
                    // upgrade legacy plain text to hash
                    String newHash = PasswordUtils.hash(rawPassword);
                    try (PreparedStatement up = conn.prepareStatement("UPDATE Admin SET password=? WHERE email=?")) {
                        up.setString(1, newHash);
                        up.setString(2, email);
                        up.executeUpdate();
                    }
                }
                return match;
            }
        }
    }

    public boolean authenticateFaculty(String email, String rawPassword) throws SQLException {
        String select = "SELECT id, password FROM Faculty WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String stored = rs.getString("password");
                boolean match = PasswordUtils.matches(rawPassword, stored);
                if (match && !PasswordUtils.isHashed(stored)) {
                    String newHash = PasswordUtils.hash(rawPassword);
                    try (PreparedStatement up = conn.prepareStatement("UPDATE Faculty SET password=? WHERE email=?")) {
                        up.setString(1, newHash);
                        up.setString(2, email);
                        up.executeUpdate();
                    }
                }
                return match;
            }
        }
    }

    // Database-related methods for dropdowns
    public List<String> getCourseCodes() throws SQLException {
        String sql = "SELECT DISTINCT course_code FROM Course ORDER BY course_code";
        List<String> courseCodes = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courseCodes.add(rs.getString("course_code"));
            }
        }
        return courseCodes;
    }

    public List<String> getInstructorNames() throws SQLException {
        String sql = "SELECT DISTINCT f.full_name FROM Faculty f ORDER BY f.full_name";
        List<String> instructors = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                instructors.add(rs.getString("full_name"));
            }
        }
        return instructors;
    }

    public List<String> getCoursesByInstructor(String instructorName) throws SQLException {
        String sql = """
            SELECT c.course_name FROM Course c
            JOIN CourseAssignment ca ON c.course_code = ca.course_code
            JOIN Faculty f ON ca.faculty_id = f.id
            WHERE f.full_name = ?
            ORDER BY c.course_name
            """;
        List<String> courses = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, instructorName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                courses.add(rs.getString("course_name"));
            }
        }
        return courses;
    }

    public List<String> getAcademicYears() throws SQLException {
        // Generate academic years based on current year
        List<String> years = new ArrayList<>();
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear - 5; i <= currentYear + 2; i++) {
            years.add(i + "-" + (i + 1));
        }
        return years;
    }

    public CourseData getCourseByCodeAndInstructor(String courseCode, String instructorName) throws SQLException {
        String sql = """
            SELECT c.course_code, c.course_name, c.credits, c.department, c.programme, f.full_name as instructor_name
            FROM Course c
            JOIN CourseAssignment ca ON c.course_code = ca.course_code
            JOIN Faculty f ON ca.faculty_id = f.id
            WHERE c.course_code = ? AND f.full_name = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, instructorName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new CourseData(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getDouble("credits"),
                        rs.getString("department"),
                        rs.getString("programme"),
                        rs.getString("instructor_name")
                );
            }
        }
        return null;
    }

    // Helper methods to fetch question IDs after insertion (when adding through UI)
    public Integer getQuizQuestionId(String courseId, int quizNumber, String title) throws SQLException {
        String sql = """
            SELECT qq.id FROM QuizQuestion qq
            JOIN Quiz q ON qq.quiz_id = q.id
            WHERE q.course_id = ? AND q.quiz_number = ? AND qq.title = ?
            """;
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setInt(2, quizNumber);
            stmt.setString(3, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    public Integer getMidQuestionId(String courseId, String title) throws SQLException {
        String sql = """
            SELECT mq.id FROM MidQuestion mq
            JOIN Mid m ON mq.mid_id = m.id
            WHERE m.course_id = ? AND mq.title = ?
            """;
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setString(2, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    public Integer getFinalQuestionId(String courseId, String title) throws SQLException {
        String sql = """
            SELECT fq.id FROM FinalQuestion fq
            JOIN Final f ON fq.final_id = f.id
            WHERE f.course_id = ? AND fq.title = ?
            """;
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setString(2, title);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    // Data classes
    public static class QuestionData {
        public final int id;
        public final String title;
        public final double marks;
        public final String co;
        public final String po;

        public QuestionData(int id, String title, double marks, String co, String po) {
            this.id = id;
            this.title = title;
            this.marks = marks;
            this.co = co;
            this.po = po;
        }
    }

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
    }

    public static class CourseData {
        public final String courseCode;
        public final String courseName;
        public final double credits;
        public final String department;
        public final String programme;
        public final String instructorName;

        public CourseData(String courseCode, String courseName, double credits, String department, String programme, String instructorName) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
            this.department = department;
            this.programme = programme;
            this.instructorName = instructorName;
        }
    }

    public static class StudentMarksData {
        public final String studentId;
        public final String studentName;
        public final int questionId;
        public final String questionTitle;
        public final double maxMarks;
        public final double marksObtained;

        public StudentMarksData(String studentId, String studentName, int questionId, String questionTitle, double maxMarks, double marksObtained) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.questionId = questionId;
            this.questionTitle = questionTitle;
            this.maxMarks = maxMarks;
            this.marksObtained = marksObtained;
        }
    }

    public static class StudentPerformanceData {
        public final String studentId;
        public final String studentName;
        public final int batch;
        public final String assessmentType;
        public final int assessmentNumber;
        public final String questionTitle;
        public final double maxMarks;
        public final double marksObtained;
        public final String coNumber;
        public final String poNumber;

        public StudentPerformanceData(String studentId, String studentName, int batch, String assessmentType, int assessmentNumber, String questionTitle, double maxMarks, double marksObtained, String coNumber, String poNumber) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.batch = batch;
            this.assessmentType = assessmentType;
            this.assessmentNumber = assessmentNumber;
            this.questionTitle = questionTitle;
            this.maxMarks = maxMarks;
            this.marksObtained = marksObtained;
            this.coNumber = coNumber;
            this.poNumber = poNumber;
        }
    }

    public static class FacultyInfo {
        public final int id;
        public final String shortname;
        public final String fullName;
        public final String email;
        FacultyInfo(int id, String shortname, String fullName, String email) {
            this.id = id;
            this.shortname = shortname;
            this.fullName = fullName;
            this.email = email;
        }
        public int getId(){return id;} public String getShortname(){return shortname;} public String getFullName(){return fullName;} public String getEmail(){return email;}
    }
    public static class FacultyCourseAssignment {
        public final String courseCode;
        public final String courseName;
        public final String academicYear;
        public final String department;
        public final String programme;
        FacultyCourseAssignment(String courseCode, String courseName, String academicYear, String department, String programme) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.academicYear = academicYear;
            this.department = department;
            this.programme = programme;
        }
        public String getCourseCode(){return courseCode;} public String getCourseName(){return courseName;} public String getAcademicYear(){return academicYear;} public String getDepartment(){return department;} public String getProgramme(){return programme;}
    }
    public FacultyInfo getFacultyInfo(String email) throws SQLException {
        String sql = "SELECT id, shortname, full_name, email FROM Faculty WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FacultyInfo(rs.getInt("id"), rs.getString("shortname"), rs.getString("full_name"), rs.getString("email"));
                }
            }
        }
        return null;
    }
    public List<FacultyCourseAssignment> getAssignmentsForFaculty(int facultyId) throws SQLException {
        String sql = """
            SELECT ca.course_code, c.course_name, ca.academic_year, ca.department, ca.programme
            FROM CourseAssignment ca
            JOIN Course c ON ca.course_code = c.course_code
            WHERE ca.faculty_id = ?
            ORDER BY ca.academic_year DESC, ca.course_code
            """;
        List<FacultyCourseAssignment> list = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FacultyCourseAssignment(
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getString("academic_year"),
                        rs.getString("department"),
                        rs.getString("programme")
                    ));
                }
            }
        }
        return list;
    }

    public List<StudentMarks> getQuizMarks(String courseId, int quizNumber) throws SQLException {
        List<StudentMarks> marks = new ArrayList<>();
        String sql = """
            SELECT s.id, COALESCE(SUM(sqm.marks_obtained), 0) as total_marks
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Quiz q ON c.course_code = q.course_id
            JOIN QuizQuestion qq ON q.id = qq.quiz_id
            LEFT JOIN StudentQuizMarks sqm ON s.id = sqm.student_id AND qq.id = sqm.quiz_question_id
            WHERE c.course_code = ? AND q.quiz_number = ?
            GROUP BY s.id
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setInt(2, quizNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    marks.add(new StudentMarks(rs.getString("id"), rs.getDouble("total_marks")));
                }
            }
        }
        return marks;
    }

    public List<StudentMarks> getMidMarks(String courseId) throws SQLException {
        List<StudentMarks> marks = new ArrayList<>();
        String sql = """
            SELECT s.id, COALESCE(SUM(smm.marks_obtained), 0) as total_marks
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Mid m ON c.course_code = m.course_id
            JOIN MidQuestion mq ON m.id = mq.mid_id
            LEFT JOIN StudentMidMarks smm ON s.id = smm.student_id AND mq.id = smm.mid_question_id
            WHERE c.course_code = ?
            GROUP BY s.id
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    marks.add(new StudentMarks(rs.getString("id"), rs.getDouble("total_marks")));
                }
            }
        }
        return marks;
    }

    public List<StudentMarks> getFinalMarks(String courseId) throws SQLException {
        List<StudentMarks> marks = new ArrayList<>();
        String sql = """
            SELECT s.id, COALESCE(SUM(sfm.marks_obtained), 0) as total_marks
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.course_code
            JOIN Final f ON c.course_code = f.course_id
            JOIN FinalQuestion fq ON f.id = fq.final_id
            LEFT JOIN StudentFinalMarks sfm ON s.id = sfm.student_id AND fq.id = sfm.final_question_id
            WHERE c.course_code = ?
            GROUP BY s.id
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    marks.add(new StudentMarks(rs.getString("id"), rs.getDouble("total_marks")));
                }
            }
        }
        return marks;
    }

    public void updateStudentMarks(String studentId, String courseId, String examType, int examNumber, double marks) throws SQLException {
        String sql = switch (examType.toLowerCase()) {
            case "quiz" -> """
                UPDATE StudentQuizMarks
                SET marks_obtained = ?
                WHERE student_id = ? AND quiz_question_id IN (
                    SELECT qq.id
                    FROM QuizQuestion qq
                    JOIN Quiz q ON qq.quiz_id = q.id
                    WHERE q.course_id = ? AND q.quiz_number = ?
                )""";
            case "mid" -> """
                UPDATE StudentMidMarks
                SET marks_obtained = ?
                WHERE student_id = ? AND mid_question_id IN (
                    SELECT mq.id
                    FROM MidQuestion mq
                    JOIN Mid m ON mq.mid_id = m.id
                    WHERE m.course_id = ?
                )""";
            case "final" -> """
                UPDATE StudentFinalMarks
                SET marks_obtained = ?
                WHERE student_id = ? AND final_question_id IN (
                    SELECT fq.id
                    FROM FinalQuestion fq
                    JOIN Final f ON fq.final_id = f.id
                    WHERE f.course_id = ?
                )""";
            default -> throw new IllegalArgumentException("Invalid exam type: " + examType);
        };

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, marks);
            stmt.setString(2, studentId);
            stmt.setString(3, courseId);
            if (examType.equalsIgnoreCase("quiz")) {
                stmt.setInt(4, examNumber);
            }
            stmt.executeUpdate();
        }
    }

    // Bulk enroll students into a course for an academic year (ignores duplicates)
    public void enrollStudents(String courseId, String academicYear, List<String> studentIds) throws SQLException {
        if (studentIds == null || studentIds.isEmpty()) return;
        // Ensure schema is upgraded to include academic_year (backward compatibility)
        ensureEnrollmentYearColumn();
        String sql = "INSERT IGNORE INTO Enrollment (student_id, course_id, academic_year) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String sid : studentIds) {
                ps.setString(1, sid);
                ps.setString(2, courseId);
                ps.setString(3, academicYear);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException ex) {
            // Fallback: legacy schema without academic_year, insert without it
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unknown column 'academic_year'")) {
                String legacySql = "INSERT IGNORE INTO Enrollment (student_id, course_id) VALUES (?, ?)";
                try (Connection conn2 = getConnection(); PreparedStatement ps2 = conn2.prepareStatement(legacySql)) {
                    for (String sid : studentIds) {
                        ps2.setString(1, sid);
                        ps2.setString(2, courseId);
                        ps2.addBatch();
                    }
                    ps2.executeBatch();
                }
            } else {
                throw ex;
            }
        }
    }

    private static volatile Boolean enrollmentYearChecked = false;
    private synchronized void ensureEnrollmentYearColumn() throws SQLException {
        if (Boolean.TRUE.equals(enrollmentYearChecked)) return;
        try (Connection conn = getConnection()) {
            boolean hasColumn;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='Enrollment' AND COLUMN_NAME='academic_year'")) {
                try (ResultSet rs = ps.executeQuery()) { rs.next(); hasColumn = rs.getInt(1) > 0; }
            }
            if (!hasColumn) {
                // Add column with a provisional default; later inserts will override.
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE Enrollment ADD COLUMN academic_year VARCHAR(9) NOT NULL DEFAULT '2024-2025' AFTER course_id");
                }
                // Adjust unique constraint: drop any existing unique on (student_id, course_id)
                try (PreparedStatement psIdx = conn.prepareStatement("SHOW INDEX FROM Enrollment")) {
                    try (ResultSet rs = psIdx.executeQuery()) {
                        // Collect index names matching unique pair
                        while (rs.next()) {
                            String keyName = rs.getString("Key_name");
                            int nonUnique = rs.getInt("Non_unique");
                            String colName = rs.getString("Column_name");
                            // Heuristic: if unique and first column student_id capture index
                            if (nonUnique == 0 && "student_id".equalsIgnoreCase(colName)) {
                                try (Statement drop = conn.createStatement()) {
                                    drop.executeUpdate("ALTER TABLE Enrollment DROP INDEX `" + keyName + "`");
                                } catch (SQLException ignored) { /* best effort */ }
                            }
                        }
                    }
                }
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE Enrollment ADD UNIQUE KEY uniq_enrollment (student_id, course_id, academic_year)");
                } catch (SQLException ignored) { /* If already exists */ }
            }
        }
        enrollmentYearChecked = true;
    }
}
