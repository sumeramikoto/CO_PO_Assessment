-- CO/PO Assessment System MySQL Schema

CREATE TABLE Admin (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL
);

CREATE TABLE Faculty (
                         id VARCHAR(20) PRIMARY KEY,  -- Manual ID assignment (e.g., employee ID)
                         shortname VARCHAR(50) NOT NULL,
                         full_name VARCHAR(100) NOT NULL,
                         email VARCHAR(100) NOT NULL UNIQUE,
                         password VARCHAR(100) NOT NULL
);

CREATE TABLE Course (
                        course_code VARCHAR(20),
                        course_name VARCHAR(100) NOT NULL,
                        credits DECIMAL(3,1) NOT NULL,
                        department VARCHAR(3) NOT NULL,
                        programme VARCHAR(11) NOT NULL,
                        PRIMARY KEY (course_code, programme)
);

-- Updated: include programme in FK to Course
CREATE TABLE CourseAssignment (
                                  faculty_id INT NOT NULL,
                                  course_code VARCHAR(20) NOT NULL,
                                  programme VARCHAR(11) NOT NULL,
                                  academic_year VARCHAR(9) NOT NULL,
                                  department VARCHAR(3) NOT NULL,
                                  FOREIGN KEY (faculty_id) REFERENCES Faculty(id),
                                  FOREIGN KEY (course_code, programme) REFERENCES Course(course_code, programme),
                                  UNIQUE (course_code, programme, academic_year, department)
);

CREATE TABLE Student (
                         id VARCHAR(9) PRIMARY KEY,  -- Manual ID assignment (e.g., student ID/roll number)
                         batch INT NOT NULL,
                         name VARCHAR(100) NOT NULL,
                         email VARCHAR(100) NOT NULL UNIQUE,
                         department VARCHAR(3),
                         programme VARCHAR(11)
);

-- Added academic_year to support multi-year enrollments (UI requirement)
-- Added programme to reference composite key in Course
CREATE TABLE Enrollment (
                            student_id VARCHAR(9) NOT NULL,
                            course_id VARCHAR(20) NOT NULL,
                            programme VARCHAR(11) NOT NULL,
                            academic_year VARCHAR(9) NOT NULL,
                            FOREIGN KEY (student_id) REFERENCES Student(id),
                            FOREIGN KEY (course_id, programme) REFERENCES Course(course_code, programme),
                            UNIQUE (student_id, course_id, programme, academic_year)
);

-- CO and PO master tables
CREATE TABLE CO (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    co_number VARCHAR(10) NOT NULL -- CO1, CO2, etc.
);

CREATE TABLE PO (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    po_number VARCHAR(10) NOT NULL UNIQUE -- PO1, PO2, etc.
);

-- Assessment tables (added programme for FK to Course)
CREATE TABLE Quiz (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      course_id VARCHAR(20) NOT NULL,
                      programme VARCHAR(11) NOT NULL,
                      quiz_number INT NOT NULL,
                      academic_year VARCHAR(9) NOT NULL,
                      total_marks DECIMAL(5,2) DEFAULT 0,
                      FOREIGN KEY (course_id, programme) REFERENCES Course(course_code, programme),
                      UNIQUE(course_id, programme, quiz_number),
                      CHECK (quiz_number BETWEEN 1 AND 4)
);

CREATE TABLE `Mid` (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       course_id VARCHAR(20) NOT NULL,
                       programme VARCHAR(11) NOT NULL,
                       academic_year VARCHAR(9) NOT NULL,
                       total_marks DECIMAL(5,2) DEFAULT 0,
                       FOREIGN KEY (course_id, programme) REFERENCES Course(course_code, programme),
                       UNIQUE(course_id, programme)
);

CREATE TABLE `Final` (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         course_id VARCHAR(20) NOT NULL,
                         programme VARCHAR(11) NOT NULL,
                         academic_year VARCHAR(9) NOT NULL,
                         total_marks DECIMAL(5,2) DEFAULT 0,
                         FOREIGN KEY (course_id, programme) REFERENCES Course(course_code, programme),
                         UNIQUE(course_id, programme)
);

-- Question tables with unique titles (1a, 1b, 3a, etc.)
CREATE TABLE QuizQuestion (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              quiz_id INT NOT NULL,
                              title VARCHAR(20) NOT NULL, -- 1a, 1b, 2a, 3a, etc.
                              marks DECIMAL(5,2) NOT NULL,
                              co_id INT NOT NULL,
                              po_id INT NOT NULL,
                              FOREIGN KEY (quiz_id) REFERENCES Quiz(id) ON DELETE CASCADE,
                              FOREIGN KEY (co_id) REFERENCES CO(id),
                              FOREIGN KEY (po_id) REFERENCES PO(id),
                              UNIQUE(quiz_id, title)
);

CREATE TABLE MidQuestion (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             mid_id INT NOT NULL,
                             title VARCHAR(20) NOT NULL, -- 1a, 1b, 2a, 3a, etc.
                             marks DECIMAL(5,2) NOT NULL,
                             co_id INT NOT NULL,
                             po_id INT NOT NULL,
                             FOREIGN KEY (mid_id) REFERENCES `Mid`(id) ON DELETE CASCADE,
                             FOREIGN KEY (co_id) REFERENCES CO(id),
                             FOREIGN KEY (po_id) REFERENCES PO(id),
                             UNIQUE(mid_id, title)
);

CREATE TABLE FinalQuestion (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               final_id INT NOT NULL,
                               title VARCHAR(20) NOT NULL, -- 1a, 1b, 2a, 3a, etc.
                               marks DECIMAL(5,2) NOT NULL,
                               co_id INT NOT NULL,
                               po_id INT NOT NULL,
                               FOREIGN KEY (final_id) REFERENCES `Final`(id) ON DELETE CASCADE,
                               FOREIGN KEY (co_id) REFERENCES CO(id),
                               FOREIGN KEY (po_id) REFERENCES PO(id),
                               UNIQUE(final_id, title)
);

-- Student marks tables
CREATE TABLE StudentQuizMarks (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  student_id VARCHAR(9) NOT NULL,
                                  quiz_question_id INT NOT NULL,
                                  marks_obtained DECIMAL(5,2) NOT NULL DEFAULT 0,
                                  FOREIGN KEY (student_id) REFERENCES Student(id),
                                  FOREIGN KEY (quiz_question_id) REFERENCES QuizQuestion(id) ON DELETE CASCADE,
                                  UNIQUE(student_id, quiz_question_id)
);

CREATE TABLE StudentMidMarks (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 student_id VARCHAR(9) NOT NULL,
                                 mid_question_id INT NOT NULL,
                                 marks_obtained DECIMAL(5,2) NOT NULL DEFAULT 0,
                                 FOREIGN KEY (student_id) REFERENCES Student(id),
                                 FOREIGN KEY (mid_question_id) REFERENCES MidQuestion(id) ON DELETE CASCADE,
                                 UNIQUE(student_id, mid_question_id)
);

CREATE TABLE StudentFinalMarks (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   student_id VARCHAR(9) NOT NULL,
                                   final_question_id INT NOT NULL,
                                   marks_obtained DECIMAL(5,2) NOT NULL DEFAULT 0,
                                   FOREIGN KEY (student_id) REFERENCES Student(id),
                                   FOREIGN KEY (final_question_id) REFERENCES FinalQuestion(id) ON DELETE CASCADE,
                                   UNIQUE(student_id, final_question_id)
);

-- Views for easy mark retrieval and CO/PO analysis (updated joins to include programme)
CREATE VIEW StudentQuizPerformance AS
SELECT
    s.id as student_id,
    s.name as student_name,
    c.course_code as course_id,
    c.programme,
    c.course_name,
    q.quiz_number,
    qq.title as question_title,
    qq.marks as max_marks,
    sqm.marks_obtained,
    co.co_number,
    po.po_number
FROM Student s
         JOIN Enrollment e ON s.id = e.student_id
         JOIN Course c ON e.course_id = c.course_code AND e.programme = c.programme
         JOIN Quiz q ON c.course_code = q.course_id AND c.programme = q.programme
         JOIN QuizQuestion qq ON q.id = qq.quiz_id
         LEFT JOIN StudentQuizMarks sqm ON s.id = sqm.student_id AND qq.id = sqm.quiz_question_id
         JOIN CO co ON qq.co_id = co.id
         JOIN PO po ON qq.po_id = po.id;

CREATE VIEW StudentMidPerformance AS
SELECT
    s.id as student_id,
    s.name as student_name,
    c.course_code as course_id,
    c.programme,
    c.course_name,
    mq.title as question_title,
    mq.marks as max_marks,
    smm.marks_obtained,
    co.co_number,
    po.po_number
FROM Student s
         JOIN Enrollment e ON s.id = e.student_id
         JOIN Course c ON e.course_id = c.course_code AND e.programme = c.programme
         JOIN `Mid` m ON c.course_code = m.course_id AND c.programme = m.programme
         JOIN MidQuestion mq ON m.id = mq.mid_id
         LEFT JOIN StudentMidMarks smm ON s.id = smm.student_id AND mq.id = smm.mid_question_id
         JOIN CO co ON mq.co_id = co.id
         JOIN PO po ON mq.po_id = po.id;

CREATE VIEW StudentFinalPerformance AS
SELECT
    s.id as student_id,
    s.name as student_name,
    c.course_code as course_id,
    c.programme,
    c.course_name,
    fq.title as question_title,
    fq.marks as max_marks,
    sfm.marks_obtained,
    co.co_number,
    po.po_number
FROM Student s
         JOIN Enrollment e ON s.id = e.student_id
         JOIN Course c ON e.course_id = c.course_code AND e.programme = c.programme
         JOIN `Final` f ON c.course_code = f.course_id AND c.programme = f.programme
         JOIN FinalQuestion fq ON f.id = fq.final_id
         LEFT JOIN StudentFinalMarks sfm ON s.id = sfm.student_id AND fq.id = sfm.final_question_id
         JOIN CO co ON fq.co_id = co.id
         JOIN PO po ON fq.po_id = po.id;
