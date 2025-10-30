-- Insert CO and PO data (idempotent suggestion: wrap with INSERT IGNORE if rerunning)
INSERT INTO CO (co_number) VALUES ('CO1'), ('CO2'), ('CO3'), ('CO4'), ('CO5'), ('CO6'), ('CO7'), ('CO8'), ('CO9'), ('CO10'), ('CO11'), ('CO12'), ('CO13'), ('CO14'), ('CO15'), ('CO16'), ('CO17'), ('CO18'), ('CO19'), ('CO20');
INSERT INTO PO (po_number) VALUES ('PO1'), ('PO2'), ('PO3'), ('PO4'), ('PO5'), ('PO6'), ('PO7'), ('PO8'), ('PO9'), ('PO10'), ('PO11'), ('PO12');

-- Insert admin data (hashed password for 'admin123')
INSERT INTO Admin (email, password) VALUES ('admin@iut-dhaka.edu', 'sha256$JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=');

INSERT INTO Thresholds (type, percentage) VALUES ('CO_INDIVIDUAL', 60), ('PO_INDIVIDUAL', 40), ('CO_COHORTSET', 50), ('PO_COHORTSET', 50);