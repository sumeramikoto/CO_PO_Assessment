-- CO/PO Assessment System MySQL Schema

CREATE TABLE Admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE Faculty (
    id INT  PRIMARY KEY,
    shortname VARCHAR(50) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);
CREATE TABLE CourseAssignment (
    faculty_id INT NOT NULL,
    course_id VARCHAR NOT NULL,
    FOREIGN KEY (faculty_id) REFERENCES Faculty(id),
    FOREIGN KEY (course_id) REFERENCES Course(id),
    UNIQUE (faculty_id, course_id)
);
CREATE TABLE Course (
    id VARCHAR PRIMARY KEY,  -- CSE 4341 etc
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credits DECIMAL(3,1) NOT NULL,
    instructor_id INT NOT NULL,
    FOREIGN KEY (instructor_id) REFERENCES Faculty(id)
);

CREATE TABLE Student (
    id INT  PRIMARY KEY,
    batch INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    year INT NOT NULL
);

CREATE TABLE Enrollment (
    student_id INT NOT NULL,
    course_id VARCHAR NOT NULL,
    FOREIGN KEY (student_id) REFERENCES Student(id),
    FOREIGN KEY (course_id) REFERENCES Course(id),
    UNIQUE (student_id, course_id)
);

CREATE TABLE Assessment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    type ENUM('quiz', 'mid', 'final') NOT NULL,
    date DATE NOT NULL,
    FOREIGN KEY (course_id) REFERENCES Course(id)
);

-- Tables for different exam types per course
-- Tables for different exam types per course

CREATE TABLE Quiz (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      course_id VARCHAR NOT NULL,
                      quiz_number INT NOT NULL CHECK (quiz_number BETWEEN 1 AND 4),
                      date DATE NOT NULL,
                      FOREIGN KEY (course_id) REFERENCES Course(id),
                      UNIQUE(course_id, quiz_number)
);

CREATE TABLE Mid (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     course_id VARCHAR NOT NULL,
                     date DATE NOT NULL,
                     FOREIGN KEY (course_id) REFERENCES Course(id),
                     UNIQUE(course_id)
);

CREATE TABLE Final (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       course_id VARCHAR NOT NULL,
                       date DATE NOT NULL,
                       FOREIGN KEY (course_id) REFERENCES Course(id),
                       UNIQUE(course_id)
);

CREATE TABLE Quiz (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR NOT NULL,
    quiz_number INT NOT NULL CHECK (quiz_number BETWEEN 1 AND 4),
    date DATE NOT NULL,
    FOREIGN KEY (course_id) REFERENCES Course(id),
    UNIQUE(course_id, quiz_number)
);
To support multiple questions per quiz/mid/final with unique titles, marks, and CO/PO assignment, replace the duplicated block with question and mapping tables in `src/main/resources/schema.sql`.


CREATE TABLE QuizQuestion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    title VARCHAR(20) NOT NULL,
    marks DECIMAL(5,2) NOT NULL,
    co_id INT NOT NULL,
    FOREIGN KEY (quiz_id) REFERENCES Quiz(id),
    FOREIGN KEY (co_id) REFERENCES CO(id),
    UNIQUE(quiz_id, title)
);

CREATE TABLE MidQuestion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    mid_id INT NOT NULL,
    title VARCHAR(20) NOT NULL,
    marks DECIMAL(5,2) NOT NULL,
    co_id INT NOT NULL,
    FOREIGN KEY (mid_id) REFERENCES Mid(id),
    FOREIGN KEY (co_id) REFERENCES CO(id),
    UNIQUE(mid_id, title)
);

CREATE TABLE FinalQuestion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    final_id INT NOT NULL,
    title VARCHAR(20) NOT NULL,
    marks DECIMAL(5,2) NOT NULL,
    co_id INT NOT NULL,
    FOREIGN KEY (final_id) REFERENCES Final(id),
    FOREIGN KEY (co_id) REFERENCES CO(id),
    UNIQUE(final_id, title)
);

-- Student marks for each question
CREATE TABLE QuizStudentMarks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES Student(id),
    FOREIGN KEY (question_id) REFERENCES QuizQuestion(id),
    UNIQUE(student_id, question_id)
);

CREATE TABLE MidStudentMarks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES Student(id),
    FOREIGN KEY (question_id) REFERENCES MidQuestion(id),
    UNIQUE(student_id, question_id)
);

CREATE TABLE FinalStudentMarks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES Student(id),
    FOREIGN KEY (question_id) REFERENCES FinalQuestion(id),
    UNIQUE(student_id, question_id)
);

CREATE TABLE CO (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    co_code VARCHAR(10) NOT NULL,
    description VARCHAR(255),
    threshold DECIMAL(5,2) NOT NULL DEFAULT 60.0,
    FOREIGN KEY (course_id) REFERENCES Course(id),
    UNIQUE(course_id, co_code)
);

CREATE TABLE PO (
    id INT AUTO_INCREMENT PRIMARY KEY,
    po_code VARCHAR(10) NOT NULL UNIQUE,
    description VARCHAR(255),
    threshold DECIMAL(5,2) NOT NULL DEFAULT 40.0
);

CREATE TABLE CO_PO_Map (
    id INT AUTO_INCREMENT PRIMARY KEY,
    co_id INT NOT NULL,
    po_id INT NOT NULL,
    FOREIGN KEY (co_id) REFERENCES CO(id),
    FOREIGN KEY (po_id) REFERENCES PO(id),
    PRIMARY KEY(co_id, po_id)
);

-- CREATE TABLE QUESTION_CO_MAP(

-- ;
-- CREATE TABLE QUESTION_MARKS_MAP();
