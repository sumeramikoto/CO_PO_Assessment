INSERT INTO Faculty (id, shortname, full_name, email, password) VALUES
(1001, 'Dr. Rahman', 'Dr. Mohammad Rahman', 'rahman@iut-dhaka.edu', 'sha256$XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg='),
(1002, 'Dr. Ahmed', 'Dr. Fatima Ahmed', 'ahmed@iut-dhaka.edu', 'sha256$XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg='),
(1003, 'Dr. Hassan', 'Dr. Ali Hassan', 'hassan@iut-dhaka.edu', 'sha256$XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg='),
(1004, 'Dr. Khan', 'Dr. Shariar Khan', 'khan@iut-dhaka.edu', 'sha256$XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg='),
(1005, 'Dr. Islam', 'Dr. Nazmul Islam', 'islam@iut-dhaka.edu', 'sha256$XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=');

-- Insert sample course data
-- Updated to include department and programme columns
INSERT INTO Course (course_code, course_name, credits, department, programme) VALUES
('4431', 'Software Engineering', 3.0, 'CSE', 'SWE'),
('4432', 'Database Systems', 3.0, 'CSE', 'SWE'),
('4433', 'Computer Networks', 3.0, 'CSE', 'SWE'),
('4434', 'Operating Systems', 3.0, 'CSE', 'SWE'),
('4435', 'Data Structures and Algorithms', 3.0, 'CSE', 'SWE');

-- Insert course assignments (linking faculty to courses)
-- Updated to include department and programme columns
INSERT INTO CourseAssignment (faculty_id, course_code, academic_year, department, programme) VALUES
(1001, '4431', '2024-2025', 'CSE', 'SWE'),
(1002, '4432', '2024-2025', 'CSE', 'SWE'),
(1003, '4433', '2024-2025', 'CSE', 'SWE'),
(1004, '4434', '2024-2025', 'CSE', 'SWE'),
(1005, '4435', '2024-2025', 'CSE', 'SWE');

-- Insert CO and PO data (idempotent suggestion: wrap with INSERT IGNORE if rerunning)
INSERT INTO CO (co_number) VALUES ('CO1'), ('CO2'), ('CO3'), ('CO4'), ('CO5'), ('CO6'), ('CO7'), ('CO8'), ('CO9'), ('CO10'), ('CO11'), ('CO12'), ('CO13'), ('CO14'), ('CO15'), ('CO16'), ('CO17'), ('CO18'), ('CO19'), ('CO20');
INSERT INTO PO (po_number) VALUES ('PO1'), ('PO2'), ('PO3'), ('PO4'), ('PO5'), ('PO6'), ('PO7'), ('PO8'), ('PO9'), ('PO10'), ('PO11'), ('PO12');

-- Insert admin data (hashed password for 'admin123')
INSERT INTO Admin (email, password) VALUES ('admin@iut-dhaka.edu', 'sha256$XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=');

-- =============================================
-- Student master data (must exist before Enrollment)
-- =============================================
INSERT INTO Student (id, batch, name, email, department, programme) VALUES
('220042101', 22, 'Aamir Rahman', 'aamir.rahman.220042101@iut-dhaka.edu', 'CSE', 'SWE'),
('220042102', 22, 'Fatima Khatun', 'fatima.khatun.220042102@iut-dhaka.edu', 'CSE', 'SWE'),
('220042103', 22, 'Rashid Ahmed', 'rashid.ahmed.220042103@iut-dhaka.edu', 'CSE', 'SWE'),
('220042104', 22, 'Naima Sultana', 'naima.sultana.220042104@iut-dhaka.edu', 'CSE', 'SWE'),
('220042105', 22, 'Tariq Hassan', 'tariq.hassan.220042105@iut-dhaka.edu', 'CSE', 'SWE'),
('220042106', 22, 'Sabrina Akter', 'sabrina.akter.220042106@iut-dhaka.edu', 'CSE', 'SWE'),
('220042107', 22, 'Karim Sheikh', 'karim.sheikh.220042107@iut-dhaka.edu', 'CSE', 'SWE'),
('220042108', 22, 'Ruma Begum', 'ruma.begum.220042108@iut-dhaka.edu', 'CSE', 'SWE'),
('220042109', 22, 'Imran Khan', 'imran.khan.220042109@iut-dhaka.edu', 'CSE', 'SWE'),
('220042110', 22, 'Shireen Rahman', 'shireen.rahman.220042110@iut-dhaka.edu', 'CSE', 'SWE'),
('220042111', 22, 'Fahim Ahmed', 'fahim.ahmed.220042111@iut-dhaka.edu', 'CSE', 'SWE'),
('220042112', 22, 'Nasreen Akhtar', 'nasreen.akhtar.220042112@iut-dhaka.edu', 'CSE', 'SWE'),
('220042113', 22, 'Salman Ali', 'salman.ali.220042113@iut-dhaka.edu', 'CSE', 'SWE'),
('220042114', 22, 'Taslima Begum', 'taslima.begum.220042114@iut-dhaka.edu', 'CSE', 'SWE'),
('220042115', 22, 'Mustafa Hossain', 'mustafa.hossain.220042115@iut-dhaka.edu', 'CSE', 'SWE'),
('220042116', 22, 'Shahana Parvin', 'shahana.parvin.220042116@iut-dhaka.edu', 'CSE', 'SWE'),
('220042117', 22, 'Arif Rahman', 'arif.rahman.220042117@iut-dhaka.edu', 'CSE', 'SWE'),
('220042118', 22, 'Rashida Khatun', 'rashida.khatun.220042118@iut-dhaka.edu', 'CSE', 'SWE'),
('220042119', 22, 'Mahbub Ahmed', 'mahbub.ahmed.220042119@iut-dhaka.edu', 'CSE', 'SWE'),
('220042120', 22, 'Salma Akter', 'salma.akter.220042120@iut-dhaka.edu', 'CSE', 'SWE'),
('220042121', 22, 'Jahangir Khan', 'jahangir.khan.220042121@iut-dhaka.edu', 'CSE', 'SWE'),
('220042122', 22, 'Rubina Sultana', 'rubina.sultana.220042122@iut-dhaka.edu', 'CSE', 'SWE'),
('220042123', 22, 'Zakir Hossain', 'zakir.hossain.220042123@iut-dhaka.edu', 'CSE', 'SWE'),
('220042124', 22, 'Rahima Begum', 'rahima.begum.220042124@iut-dhaka.edu', 'CSE', 'SWE'),
('220042125', 22, 'Shahid Rahman', 'shahid.rahman.220042125@iut-dhaka.edu', 'CSE', 'SWE'),
('220042126', 22, 'Nasir Ahmed', 'nasir.ahmed.220042126@iut-dhaka.edu', 'CSE', 'SWE'),
('220042127', 22, 'Dilara Khatun', 'dilara.khatun.220042127@iut-dhaka.edu', 'CSE', 'SWE'),
('220042128', 22, 'Rafiq Hassan', 'rafiq.hassan.220042128@iut-dhaka.edu', 'CSE', 'SWE'),
('220042129', 22, 'Sultana Akter', 'sultana.akter.220042129@iut-dhaka.edu', 'CSE', 'SWE'),
('220042130', 22, 'Belal Sheikh', 'belal.sheikh.220042130@iut-dhaka.edu', 'CSE', 'SWE'),
('220042131', 22, 'Kamrun Nahar', 'kamrun.nahar.220042131@iut-dhaka.edu', 'CSE', 'SWE'),
('220042132', 22, 'Shafiq Ahmed', 'shafiq.ahmed.220042132@iut-dhaka.edu', 'CSE', 'SWE'),
('220042133', 22, 'Monira Begum', 'monira.begum.220042133@iut-dhaka.edu', 'CSE', 'SWE'),
('220042134', 22, 'Hanif Khan', 'hanif.khan.220042134@iut-dhaka.edu', 'CSE', 'SWE'),
('220042135', 22, 'Rehana Parvin', 'rehana.parvin.220042135@iut-dhaka.edu', 'CSE', 'SWE'),
('220042136', 22, 'Salam Rahman', 'salam.rahman.220042136@iut-dhaka.edu', 'CSE', 'SWE'),
('220042137', 22, 'Farida Khatun', 'farida.khatun.220042137@iut-dhaka.edu', 'CSE', 'SWE'),
('220042138', 22, 'Mizanur Rahman', 'mizanur.rahman.220042138@iut-dhaka.edu', 'CSE', 'SWE'),
('220042139', 22, 'Shamima Akter', 'shamima.akter.220042139@iut-dhaka.edu', 'CSE', 'SWE'),
('220042140', 22, 'Abdul Karim', 'abdul.karim.220042140@iut-dhaka.edu', 'CSE', 'SWE'),
('220042141', 22, 'Morsheda Begum', 'morsheda.begum.220042141@iut-dhaka.edu', 'CSE', 'SWE'),
('220042142', 22, 'Shahjahan Ahmed', 'shahjahan.ahmed.220042142@iut-dhaka.edu', 'CSE', 'SWE'),
('220042143', 22, 'Hosne Ara', 'hosne.ara.220042143@iut-dhaka.edu', 'CSE', 'SWE'),
('220042144', 22, 'Mofazzal Hossain', 'mofazzal.hossain.220042144@iut-dhaka.edu', 'CSE', 'SWE'),
('220042145', 22, 'Laila Begum', 'laila.begum.220042145@iut-dhaka.edu', 'CSE', 'SWE'),
('220042146', 22, 'Golam Rahman', 'golam.rahman.220042146@iut-dhaka.edu', 'CSE', 'SWE'),
('220042147', 22, 'Sabina Khatun', 'sabina.khatun.220042147@iut-dhaka.edu', 'CSE', 'SWE'),
('220042148', 22, 'Nurul Islam', 'nurul.islam.220042148@iut-dhaka.edu', 'CSE', 'SWE'),
('220042149', 22, 'Rokeya Sultana', 'rokeya.sultana.220042149@iut-dhaka.edu', 'CSE', 'SWE'),
('220042150', 22, 'Jamal Ahmed', 'jamal.ahmed.220042150@iut-dhaka.edu', 'CSE', 'SWE'),
('220042151', 22, 'Kulsum Begum', 'kulsum.begum.220042151@iut-dhaka.edu', 'CSE', 'SWE'),
('220042152', 22, 'Siraj Khan', 'siraj.khan.220042152@iut-dhaka.edu', 'CSE', 'SWE'),
('220042153', 22, 'Aleya Khatun', 'aleya.khatun.220042153@iut-dhaka.edu', 'CSE', 'SWE'),
('220042154', 22, 'Mainul Hossain', 'mainul.hossain.220042154@iut-dhaka.edu', 'CSE', 'SWE'),
('220042155', 22, 'Asma Akter', 'asma.akter.220042155@iut-dhaka.edu', 'CSE', 'SWE'),
('220042156', 22, 'Abdur Rahman', 'abdur.rahman.220042156@iut-dhaka.edu', 'CSE', 'SWE'),
('220042157', 22, 'Rahela Begum', 'rahela.begum.220042157@iut-dhaka.edu', 'CSE', 'SWE'),
('220042158', 22, 'Hafizur Rahman', 'hafizur.rahman.220042158@iut-dhaka.edu', 'CSE', 'SWE'),
('220042159', 22, 'Jesmin Akter', 'jesmin.akter.220042159@iut-dhaka.edu', 'CSE', 'SWE'),
('220042160', 22, 'Shamsul Haque', 'shamsul.haque.220042160@iut-dhaka.edu', 'CSE', 'SWE');

-- Verify the insertion
SELECT COUNT(*) as total_students FROM Student WHERE batch = 22 AND department = 'CSE' AND programme = 'SWE';

-- =============================================
-- Enrollment AFTER Course & Student exist (previous earlier block removed)
-- =============================================
INSERT INTO Enrollment (student_id, course_id, academic_year) VALUES
('220042101', '4431', '2024-2025'),
('220042102', '4431', '2024-2025'),
('220042103', '4431', '2024-2025'),
('220042104', '4431', '2024-2025'),
('220042105', '4431', '2024-2025'),
('220042106', '4431', '2024-2025'),
('220042107', '4431', '2024-2025'),
('220042108', '4431', '2024-2025'),
('220042109', '4431', '2024-2025'),
('220042110', '4431', '2024-2025'),
('220042111', '4431', '2024-2025'),
('220042112', '4431', '2024-2025'),
('220042113', '4431', '2024-2025'),
('220042114', '4431', '2024-2025'),
('220042115', '4431', '2024-2025'),
('220042116', '4431', '2024-2025'),
('220042117', '4431', '2024-2025'),
('220042118', '4431', '2024-2025'),
('220042119', '4431', '2024-2025'),
('220042120', '4431', '2024-2025'),
('220042121', '4431', '2024-2025'),
('220042122', '4431', '2024-2025'),
('220042123', '4431', '2024-2025'),
('220042124', '4431', '2024-2025'),
('220042125', '4431', '2024-2025'),
('220042126', '4431', '2024-2025'),
('220042127', '4431', '2024-2025'),
('220042128', '4431', '2024-2025'),
('220042129', '4431', '2024-2025'),
('220042130', '4431', '2024-2025'),
('220042131', '4431', '2024-2025'),
('220042132', '4431', '2024-2025'),
('220042133', '4431', '2024-2025'),
('220042134', '4431', '2024-2025'),
('220042135', '4431', '2024-2025'),
('220042136', '4431', '2024-2025'),
('220042137', '4431', '2024-2025'),
('220042138', '4431', '2024-2025'),
('220042139', '4431', '2024-2025'),
('220042140', '4431', '2024-2025'),
('220042141', '4431', '2024-2025'),
('220042142', '4431', '2024-2025'),
('220042143', '4431', '2024-2025'),
('220042144', '4431', '2024-2025'),
('220042145', '4431', '2024-2025'),
('220042146', '4431', '2024-2025'),
('220042147', '4431', '2024-2025'),
('220042148', '4431', '2024-2025'),
('220042149', '4431', '2024-2025'),
('220042150', '4431', '2024-2025'),
('220042151', '4431', '2024-2025'),
('220042152', '4431', '2024-2025'),
('220042153', '4431', '2024-2025'),
('220042154', '4431', '2024-2025'),
('220042155', '4431', '2024-2025'),
('220042156', '4431', '2024-2025'),
('220042157', '4431', '2024-2025'),
('220042158', '4431', '2024-2025'),
('220042159', '4431', '2024-2025'),
('220042160', '4431', '2024-2025');

-- Verify the enrollment
SELECT COUNT(*) as total_enrollments FROM Enrollment WHERE course_id = '4431';
