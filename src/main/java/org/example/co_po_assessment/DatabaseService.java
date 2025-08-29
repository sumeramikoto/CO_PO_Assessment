package org.example.co_po_assessment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class DatabaseService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "sinhawiz123";
//Amar credentials diyas MySQL workbench e connection create koira try korte paro
    private static DatabaseService instance;

    private DatabaseService() {}

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Question related operations
    public void saveQuizQuestion(String courseId, int quizNumber, String title, double marks, String co, String po) throws SQLException {
        String sql = """
            INSERT INTO QuizQuestion (quiz_id, title, marks, co_id, po_id) 
            SELECT q.id, ?, ?, c.id, p.id 
            FROM Quiz q 
            JOIN CO c ON c.course_id = ? AND c.co_number = ?
            JOIN PO p ON p.po_number = ?
            WHERE q.course_id = ? AND q.quiz_number = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setDouble(2, marks);
            stmt.setString(3, courseId);
            stmt.setString(4, co);
            stmt.setString(5, po);
            stmt.setString(6, courseId);
            stmt.setInt(7, quizNumber);
            stmt.executeUpdate();
        }
    }

    public void saveMidQuestion(String courseId, String title, double marks, String co, String po) throws SQLException {
        String sql = """
            INSERT INTO MidQuestion (mid_id, title, marks, co_id, po_id) 
            SELECT m.id, ?, ?, c.id, p.id 
            FROM Mid m 
            JOIN CO c ON c.course_id = ? AND c.co_number = ?
            JOIN PO p ON p.po_number = ?
            WHERE m.course_id = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setDouble(2, marks);
            stmt.setString(3, courseId);
            stmt.setString(4, co);
            stmt.setString(5, po);
            stmt.setString(6, courseId);
            stmt.executeUpdate();
        }
    }

    public void saveFinalQuestion(String courseId, String title, double marks, String co, String po) throws SQLException {
        String sql = """
            INSERT INTO FinalQuestion (final_id, title, marks, co_id, po_id) 
            SELECT f.id, ?, ?, c.id, p.id 
            FROM Final f 
            JOIN CO c ON c.course_id = ? AND c.co_number = ?
            JOIN PO p ON p.po_number = ?
            WHERE f.course_id = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setDouble(2, marks);
            stmt.setString(3, courseId);
            stmt.setString(4, co);
            stmt.setString(5, po);
            stmt.setString(6, courseId);
            stmt.executeUpdate();
        }
    }

    // Retrieve questions
    public List<QuestionData> getQuizQuestions(String courseId, int quizNumber) throws SQLException {
        String sql = """
            SELECT qq.title, qq.marks, c.co_number, p.po_number 
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
            SELECT mq.title, mq.marks, c.co_number, p.po_number 
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
            SELECT fq.title, fq.marks, c.co_number, p.po_number 
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
            SELECT s.id, s.name, s.email, s.batch, s.year
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
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getInt("batch"),
                    rs.getInt("year")
                ));
            }
        }
        return students;
    }

    // Course operations
    public CourseData getCourseInfo(String courseId) throws SQLException {
        String sql = """
            SELECT c.course_code, c.course_name, c.credits, f.full_name as instructor_name
            FROM Course c
            JOIN Faculty f ON c.instructor_id = f.id
            WHERE c.id = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new CourseData(
                    courseId,
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getDouble("credits"),
                    rs.getString("instructor_name")
                );
            }
        }
        return null;
    }

    // Update course information
    public void updateCourseInfo(String courseId, String courseCode, String courseName, double credits) throws SQLException {
        String sql = "UPDATE Course SET course_code = ?, course_name = ?, credits = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, courseName);
            stmt.setDouble(3, credits);
            stmt.setString(4, courseId);
            stmt.executeUpdate();
        }
    }

    // Update instructor for a course
    public void updateCourseInstructor(String courseId, String instructorName) throws SQLException {
        String sql = """
            UPDATE Course c 
            JOIN Faculty f ON f.full_name = ?
            SET c.instructor_id = f.id 
            WHERE c.id = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, instructorName);
            stmt.setString(2, courseId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("Instructor not found: " + instructorName);
            }
        }
    }

    // Create assessments if they don't exist
    public void ensureAssessmentsExist(String courseId) throws SQLException {
        try (Connection conn = getConnection()) {
            // Create quizzes
            for (int i = 1; i <= 4; i++) {
                String sql = "INSERT IGNORE INTO Quiz (course_id, quiz_number, date) VALUES (?, ?, CURDATE())";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, courseId);
                    stmt.setInt(2, i);
                    stmt.executeUpdate();
                }
            }

            // Create mid exam
            String midSql = "INSERT IGNORE INTO Mid (course_id, date) VALUES (?, CURDATE())";
            try (PreparedStatement stmt = conn.prepareStatement(midSql)) {
                stmt.setString(1, courseId);
                stmt.executeUpdate();
            }

            // Create final exam
            String finalSql = "INSERT IGNORE INTO Final (course_id, date) VALUES (?, CURDATE())";
            try (PreparedStatement stmt = conn.prepareStatement(finalSql)) {
                stmt.setString(1, courseId);
                stmt.executeUpdate();
            }
        }
    }

    // Student marks operations
    public void saveStudentQuizMarks(int studentId, int quizQuestionId, double marksObtained) throws SQLException {
        String sql = """
            INSERT INTO StudentQuizMarks (student_id, quiz_question_id, marks_obtained) 
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, quizQuestionId);
            stmt.setDouble(3, marksObtained);
            stmt.executeUpdate();
        }
    }

    public void saveStudentMidMarks(int studentId, int midQuestionId, double marksObtained) throws SQLException {
        String sql = """
            INSERT INTO StudentMidMarks (student_id, mid_question_id, marks_obtained) 
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, midQuestionId);
            stmt.setDouble(3, marksObtained);
            stmt.executeUpdate();
        }
    }

    public void saveStudentFinalMarks(int studentId, int finalQuestionId, double marksObtained) throws SQLException {
        String sql = """
            INSERT INTO StudentFinalMarks (student_id, final_question_id, marks_obtained) 
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
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
            JOIN Course c ON e.course_id = c.id
            JOIN Quiz q ON c.id = q.course_id AND q.quiz_number = ?
            JOIN QuizQuestion qq ON q.id = qq.quiz_id
            LEFT JOIN StudentQuizMarks sqm ON s.id = sqm.student_id AND qq.id = sqm.quiz_question_id
            WHERE c.id = ?
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
                    rs.getInt("student_id"),
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
            JOIN Course c ON e.course_id = c.id
            JOIN Mid m ON c.id = m.course_id
            JOIN MidQuestion mq ON m.id = mq.mid_id
            LEFT JOIN StudentMidMarks smm ON s.id = smm.student_id AND mq.id = smm.mid_question_id
            WHERE c.id = ?
            ORDER BY s.id, mq.title
            """;

        List<StudentMarksData> marks = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                marks.add(new StudentMarksData(
                    rs.getInt("student_id"),
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
                    rs.getInt("student_id"),
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
            SELECT s.id, s.name, s.batch, s.year,
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
            
            SELECT s.id, s.name, s.batch, s.year,
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
            WHERE c.course_code= ?
            
            UNION ALL
            
            SELECT s.id, s.name, s.batch, s.year,
                   'Final' as assessment_type, 0 as assessment_number,
                   fq.title as question_title, fq.marks as max_marks,
                   COALESCE(sfm.marks_obtained, 0) as marks_obtained,
                   co.co_number, po.po_number
            FROM Student s
            JOIN Enrollment e ON s.id = e.student_id
            JOIN Course c ON e.course_id = c.id
            JOIN Final f ON c.id = f.course_id
            JOIN FinalQuestion fq ON f.id = fq.final_id
            LEFT JOIN StudentFinalMarks sfm ON s.id = sfm.student_id AND fq.id = sfm.final_question_id
            JOIN CO co ON fq.co_id = co.id
            JOIN PO po ON fq.po_id = po.id
            WHERE c.id = ?
            
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
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("batch"),
                    rs.getInt("year"),
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


    public void insertCourse(String courseId, String courseCode, String courseName, double credits, int instructorId) throws SQLException {
        String sql = "INSERT INTO Course (id, course_code, course_name, credits, instructor_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setString(2, courseCode);
            stmt.setString(3, courseName);
            stmt.setDouble(4, credits);
            stmt.setInt(5, instructorId);
            stmt.executeUpdate();
        }
    }

    public void insertStudent(int id, int batch, String name, String email, int year) throws SQLException {
        String sql = "INSERT INTO Student (id, batch, name, email, year) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, batch);
            stmt.setString(3, name);
            stmt.setString(4, email);
            stmt.setInt(5, year);
            stmt.executeUpdate();
        }
    }

    public void enrollStudent(int studentId, String courseId) throws SQLException {
        String sql = "INSERT INTO Enrollment (student_id, course_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, courseId);
            stmt.executeUpdate();
        }
    }

    public void insertCO(String courseId, String coNumber, String description) throws SQLException {
        String sql = "INSERT INTO CO (course_id, co_number, description) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            stmt.setString(2, coNumber);
            stmt.setString(3, description);
            stmt.executeUpdate();
        }
    }

    public void insertPO(String poNumber, String description) throws SQLException {
        String sql = "INSERT INTO PO (po_number, description) VALUES (?, ?) ON DUPLICATE KEY UPDATE description = VALUES(description)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, poNumber);
            stmt.setString(2, description);
            stmt.executeUpdate();
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
            JOIN Faculty f ON c.instructor_id = f.id
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
        // Generate academic years - you can modify this logic based on your needs
        List<String> years = new ArrayList<>();
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear - 5; i <= currentYear + 2; i++) {
            years.add(i + "-" + (i + 1));
        }
        return years;
    }

    public CourseData getCourseByCodeAndInstructor(String courseCode, String instructorName) throws SQLException {
        String sql = """
            SELECT c.id, c.course_code, c.course_name, c.credits, f.full_name as instructor_name
            FROM Course c
            JOIN Faculty f ON c.instructor_id = f.id
            WHERE c.course_code = ? AND f.full_name = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseCode);
            stmt.setString(2, instructorName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new CourseData(
                    rs.getString("id"),
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getDouble("credits"),
                    rs.getString("instructor_name")
                );
            }
        }
        return null;
    }

    // Data classes
    public static class QuestionData {
        public final String title;
        public final double marks;
        public final String co;
        public final String po;

        public QuestionData(String title, double marks, String co, String po) {
            this.title = title;
            this.marks = marks;
            this.co = co;
            this.po = po;
        }
    }

    public static class StudentData {
        public final int id;
        public final String name;
        public final String email;
        public final int batch;
        public final int year;

        public StudentData(int id, String name, String email, int batch, int year) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.batch = batch;
            this.year = year;
        }
    }

    public static class CourseData {
        public final String id;
        public final String courseCode;
        public final String courseName;
        public final double credits;
        public final String instructorName;

        public CourseData(String id, String courseCode, String courseName, double credits, String instructorName) {
            this.id = id;
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
            this.instructorName = instructorName;
        }
    }

    public static class StudentMarksData {
        public final int studentId;
        public final String studentName;
        public final int questionId;
        public final String questionTitle;
        public final double maxMarks;
        public final double marksObtained;

        public StudentMarksData(int studentId, String studentName, int questionId, String questionTitle, double maxMarks, double marksObtained) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.questionId = questionId;
            this.questionTitle = questionTitle;
            this.maxMarks = maxMarks;
            this.marksObtained = marksObtained;
        }
    }

    public static class StudentPerformanceData {
        public final int studentId;
        public final String studentName;
        public final int batch;
        public final int year;
        public final String assessmentType;
        public final int assessmentNumber;
        public final String questionTitle;
        public final double maxMarks;
        public final double marksObtained;
        public final String coNumber;
        public final String poNumber;

        public StudentPerformanceData(int studentId, String studentName, int batch, int year, String assessmentType, int assessmentNumber, String questionTitle, double maxMarks, double marksObtained, String coNumber, String poNumber) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.batch = batch;
            this.year = year;
            this.assessmentType = assessmentType;
            this.assessmentNumber = assessmentNumber;
            this.questionTitle = questionTitle;
            this.maxMarks = maxMarks;
            this.marksObtained = marksObtained;
            this.coNumber = coNumber;
            this.poNumber = poNumber;
        }
    }
}
