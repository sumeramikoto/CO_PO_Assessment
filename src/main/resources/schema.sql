-- CO/PO Assessment System MySQL Schema

CREATE TABLE Admin (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL
);

CREATE TABLE Faculty (
                         id INT PRIMARY KEY,  -- Manual ID assignment (e.g., employee ID)
                         shortname VARCHAR(50) NOT NULL,
                         full_name VARCHAR(100) NOT NULL,
                         email VARCHAR(100) NOT NULL UNIQUE,
                         password VARCHAR(100) NOT NULL
);

CREATE TABLE Course (
                        course_code VARCHAR(20) PRIMARY KEY,
                        course_name VARCHAR(100) NOT NULL,
                        credits DECIMAL(3,1) NOT NULL
);

CREATE TABLE CourseAssignment (
                                  faculty_id INT NOT NULL,
                                  course_id VARCHAR(20) NOT NULL,
                                  academic_year VARCHAR(9) NOT NULL,
                                  FOREIGN KEY (faculty_id) REFERENCES Faculty(id),
                                  FOREIGN KEY (course_id) REFERENCES Course(id),
                                  UNIQUE (faculty_id, course_id)
);

CREATE TABLE Student (
                         id VARCHAR(9) PRIMARY KEY,  -- Manual ID assignment (e.g., student ID/roll number)
                         batch INT NOT NULL,
                         name VARCHAR(100) NOT NULL,
                         email VARCHAR(100) NOT NULL UNIQUE,
                         department VARCHAR(3),
                         programme VARCHAR(3)
);

CREATE TABLE Enrollment (
                            student_id VARCHAR(9) NOT NULL,
                            course_id VARCHAR(20) NOT NULL,
                            FOREIGN KEY (student_id) REFERENCES Student(id),
                            FOREIGN KEY (course_id) REFERENCES Course(id),
                            UNIQUE (student_id, course_id)
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

-- Assessment tables
CREATE TABLE Quiz (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      course_id VARCHAR(20) NOT NULL,
                      quiz_number INT NOT NULL,
                      academic_year VARCHAR(9) NOT NULL,
                      total_marks DECIMAL(5,2) DEFAULT 0,
                      FOREIGN KEY (course_id) REFERENCES Course(id),
                      UNIQUE(course_id, quiz_number),
                      CHECK (quiz_number BETWEEN 1 AND 4)
);

CREATE TABLE `Mid` (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       course_id VARCHAR(20) NOT NULL,
                       academic_year VARCHAR(9) NOT NULL,
                       total_marks DECIMAL(5,2) DEFAULT 0,
                       FOREIGN KEY (course_id) REFERENCES Course(id),
                       UNIQUE(course_id)
);

CREATE TABLE `Final` (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         course_id VARCHAR(20) NOT NULL,
                         academic_year VARCHAR(9) NOT NULL,
                         total_marks DECIMAL(5,2) DEFAULT 0,
                         FOREIGN KEY (course_id) REFERENCES Course(id),
                         UNIQUE(course_id)
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

-- Views for easy mark retrieval and CO/PO analysis
CREATE VIEW StudentQuizPerformance AS
SELECT
    s.id as student_id,
    s.name as student_name,
    c.id as course_id,
    c.course_name,
    q.quiz_number,
    qq.title as question_title,
    qq.marks as max_marks,
    sqm.marks_obtained,
    co.co_number,
    po.po_number
FROM Student s
         JOIN Enrollment e ON s.id = e.student_id
         JOIN Course c ON e.course_id = c.id
         JOIN Quiz q ON c.id = q.course_id
         JOIN QuizQuestion qq ON q.id = qq.quiz_id
         LEFT JOIN StudentQuizMarks sqm ON s.id = sqm.student_id AND qq.id = sqm.quiz_question_id
         JOIN CO co ON qq.co_id = co.id
         JOIN PO po ON qq.po_id = po.id;

CREATE VIEW StudentMidPerformance AS
SELECT
    s.id as student_id,
    s.name as student_name,
    c.id as course_id,
    c.course_name,
    mq.title as question_title,
    mq.marks as max_marks,
    smm.marks_obtained,
    co.co_number,
    po.po_number
FROM Student s
         JOIN Enrollment e ON s.id = e.student_id
         JOIN Course c ON e.course_id = c.id
         JOIN `Mid` m ON c.id = m.course_id
         JOIN MidQuestion mq ON m.id = mq.mid_id
         LEFT JOIN StudentMidMarks smm ON s.id = smm.student_id AND mq.id = smm.mid_question_id
         JOIN CO co ON mq.co_id = co.id
         JOIN PO po ON mq.po_id = po.id;

CREATE VIEW StudentFinalPerformance AS
SELECT
    s.id as student_id,
    s.name as student_name,
    c.id as course_id,
    c.course_name,
    fq.title as question_title,
    fq.marks as max_marks,
    sfm.marks_obtained,
    co.co_number,
    po.po_number
FROM Student s
         JOIN Enrollment e ON s.id = e.student_id
         JOIN Course c ON e.course_id = c.id
         JOIN `Final` f ON c.id = f.course_id
         JOIN FinalQuestion fq ON f.id = fq.final_id
         LEFT JOIN StudentFinalMarks sfm ON s.id = sfm.student_id AND fq.id = sfm.final_question_id
         JOIN CO co ON fq.co_id = co.id
         JOIN PO po ON fq.po_id = po.id;

-- INSERT SAMPLE DATA

-- Insert Faculty (asaduzzamanherok with shortname 'ah')
INSERT INTO Faculty (id, shortname, full_name, email, password)
VALUES (101, 'azh', 'Asaduzzaman Herok', 'asaduzzamanherok@example.com', 'password123');

-- Insert Course CSE4403 with 3.0 credits
INSERT INTO Course (id, course_code, course_name, credits, instructor_id)
VALUES ('CSE4403', 'CSE4403', 'Algorithm', 3.0, 101);

-- Insert basic Program Outcomes (POs)
INSERT INTO PO (po_number, description) VALUES
('PO1', 'Engineering knowledge: Apply the knowledge of mathematics, science, engineering fundamentals'),
('PO2', 'Problem analysis: Identify, formulate, review research literature, and analyze complex engineering problems'),
('PO3', 'Design/development of solutions: Design solutions for complex engineering problems'),
('PO4', 'Conduct investigations of complex problems: Use research-based knowledge and research methods'),
('PO5', 'Modern tool usage: Create, select, and apply appropriate techniques, resources, and modern engineering tools');

-- Insert Course Outcomes (COs) for CSE4403
INSERT INTO CO (course_id, co_number, description) VALUES
('CSE4403', 'CO1', 'Apply software engineering principles to develop software systems'),
('CSE4403', 'CO2', 'Design and implement software architectures using appropriate design patterns'),
('CSE4403', 'CO3', 'Develop software projects using modern development methodologies'),
('CSE4403', 'CO4', 'Test and validate software systems using various testing techniques');

-- Insert sample students
INSERT INTO Student (id, batch, name, email, year) VALUES
(1901001, 19, 'John Doe', 'john.doe@student.example.com', 4),
(1901002, 19, 'Jane Smith', 'jane.smith@student.example.com', 4),
(1901003, 19, 'Bob Johnson', 'bob.johnson@student.example.com', 4),
(1901004, 19, 'Alice Brown', 'alice.brown@student.example.com', 4),
(1901005, 19, 'Charlie Wilson', 'charlie.wilson@student.example.com', 4);

-- Enroll students in CSE4403
INSERT INTO Enrollment (student_id, course_id) VALUES
(1901001, 'CSE4403'),
(1901002, 'CSE4403'),
(1901003, 'CSE4403'),
(1901004, 'CSE4403'),
(1901005, 'CSE4403');

-- Create assessments for CSE4403
INSERT INTO Quiz (course_id, quiz_number, date) VALUES
('CSE4403', 1, '2024-02-15'),
('CSE4403', 2, '2024-03-15'),
('CSE4403', 3, '2024-04-15'),
('CSE4403', 4, '2024-05-15');

INSERT INTO `Mid` (course_id, date) VALUES
('CSE4403', '2024-03-30');

INSERT INTO `Final` (course_id, date) VALUES
('CSE4403', '2024-06-15');
