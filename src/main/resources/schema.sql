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

CREATE TABLE Course (
    id INT  PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credits DECIMAL(3,1) NOT NULL,
    instructor_id INT NOT NULL,
    FOREIGN KEY (instructor_id) REFERENCES Faculty(id)
);

CREATE TABLE Student (
    id INT AUTO_INCREMENT PRIMARY KEY,
    batch INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    year INT NOT NULL
);

CREATE TABLE Enrollment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
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

CREATE TABLE Question (
    id INT AUTO_INCREMENT PRIMARY KEY,
    assessment_id INT NOT NULL,
    text VARCHAR(255) NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL,
    co_id INT NOT NULL,
    FOREIGN KEY (assessment_id) REFERENCES Assessment(id),
    FOREIGN KEY (co_id) REFERENCES CO(id)
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

CREATE TABLE Marks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    assessment_id INT NOT NULL,
    student_id INT NOT NULL,
    question_id INT NOT NULL,
    marks_obtained DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (assessment_id) REFERENCES Assessment(id),
    FOREIGN KEY (student_id) REFERENCES Student(id),
    FOREIGN KEY (question_id) REFERENCES Question(id)
);
-- CREATE TABLE QUESTION_CO_MAP(

-- ;
-- CREATE TABLE QUESTION_MARKS_MAP();
