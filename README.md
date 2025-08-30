# CO / PO Assessment System

A JavaFX + MySQL desktop application for managing university course outcome (CO) and program outcome (PO) assessment workflows: course & faculty management, student enrollment, question definition, marks entry, and attainment reporting.

## Key Features
- Admin dashboard: manage faculties, students, courses, course assignments.
- Assessment panel (Manual window): select course & academic year, load enrolled students, define quiz/mid/final questions, enter marks, view CO/PO attainment.
- Persistence via MySQL (schema + seed data provided).
- Dynamic question definition with CO/PO tagging.
- Export/report groundwork (PDF / Excel libs present for future use).

## Technology Stack
| Layer | Tech |
|-------|------|
| UI | JavaFX 17 (FXML, ControlsFX, TilesFX) |
| Charts/Reports | JFreeChart, Apache POI, iText Core (PDF) |
| Database | MySQL 8 (JDBC) |
| Build | Maven, Java 24 (preview enabled) |
| Misc | Ikonli (icons), ValidatorFX (input validation) |

## Module & Packaging
Java module: `org.example.co_po_assessment` (see `module-info.java`). Preview features enabled (source/target 24). Ensure you run with a matching JDK (>=24) and add `--enable-preview`.

## Project Structure (simplified)
```
src/main/java/
  module-info.java
  org/example/co_po_assessment/
    AdminDashboardWindow / Controller & FXML
    Manual (CO/PO assessment panel)
    DatabaseService (core DB access)
    *DatabaseHelper classes (entity-specific queries)
    Faculty / Course / AssessmentQuestion / Student models
    FXML view controllers
  depricatedClasses/ (legacy screens)
resources/
  schema.sql (DDL)
insert.sql (seed data: faculty, courses, assignments, CO/PO, students, enrollments)
```

## Database Schema Overview
Tables: Admin, Faculty, Course, CourseAssignment, Student, Enrollment, Quiz, Mid, Final, QuizQuestion, CO, PO (see `schema.sql`).
Relationships:
- `CourseAssignment(faculty_id, course_code)` links instructors to courses & academic years.
- `Enrollment(student_id, course_id)` maps students to courses.
- Assessment tables (Quiz / Mid / Final) reference `Course`.
- Question tables reference both assessment instance and CO/PO master tables.

## Setup
### Prerequisites
- JDK 24 (or newer) with preview features
- Maven 3.9+
- MySQL Server 8+

### 1. Create Database & Apply Schema
```sql
CREATE DATABASE SPL2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE SPL2;
SOURCE schema.sql;   -- or copy contents manually
SOURCE insert.sql;   -- seed data
```

### 2. Adjust Connection Settings (optional)
Edit `DatabaseService.java` if your MySQL credentials differ:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/SPL2";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "yourPassword";
```

### 3. Build
```bash
mvn clean package -DskipTests
```

### 4. Run (Examples)
Run Admin dashboard main window (adjust if class changes):
```bash
java --enable-preview -p target/classes;path/to/javafx-sdk/lib \
     -m org.example.co_po_assessment/org.example.co_po_assessment.AdminDashboardWindow
```
Run assessment panel directly:
```bash
java --enable-preview -p target/classes;path/to/javafx-sdk/lib \
     -m org.example.co_po_assessment/org.example.co_po_assessment.Manual
```
(If using javafx-maven-plugin, update `<mainClass>` to a valid launcher then `mvn clean javafx:run`).

## Usage Flow (Typical)
1. Launch Admin dashboard → add/verify faculty, courses, assign faculty to courses, manage students.
2. Launch CO/PO Assessment (Manual):
   - Click "Select Course" → choose course code + instructor + academic year.
   - Enrolled students load (via `Enrollment`).
   - Add quiz / mid / final questions (CO & PO mapping) → they persist to DB.
   - Enter marks per student → totals recompute.
   - Calculate results → view preliminary CO/PO attainment (simple averaging placeholder).
3. Export (future work) to PDF / Excel.

## CO/PO Attainment Logic (Current State)
- Present implementation: average percentage of marks earned per CO/PO across relevant questions.
- Placeholder only; refine mapping & weighting to institutional rules.

## Extending
### Add a New Assessment Question Type
1. Create new assessment table (DDL) similar to Quiz.
2. Add persistence methods in `DatabaseService`.
3. Extend UI creation in `Manual#createMarksEntryTab` + question dialogs.
4. Update attainment aggregation.

### Add Configuration Externalization
Replace hard-coded DB credentials with a `config.properties` loaded at startup.

## Troubleshooting
| Issue | Cause | Fix |
|-------|-------|-----|
| Empty student table after selecting course | Missing enrollments | Verify `Enrollment` rows and course code match | 
| ClassNotFound for controller | Wrong module export/open | Ensure `module-info.java` opens package to `javafx.fxml` |
| JavaFX version warning (23 vs 17) | FXML saved with newer SceneBuilder | Align SceneBuilder to 17 or upgrade dependencies | 
| Questions not saving | Course not selected or CO/PO rows absent | Select course first; seed CO/PO tables | 
| Duplicate key on question insert | Reusing title per assessment | Use unique `title` per quiz/mid/final |

## Known Limitations / TODO
- Authentication & role-based access minimal.
- Attainment formulas simplistic.
- No input validation for many dialogs yet.
- Export/report generation not finalized.
- Hard-coded DB credentials.
- Lack of automated tests.

## Possible Next Steps
- Add DAO abstraction & connection pooling.
- Introduce Flyway/Liquibase migrations.
- Implement service layer unit tests (JUnit + Testcontainers).
- Add user authentication (password hashing, sessions).
- Implement configurable attainment thresholds & weighting.
- Dark mode / responsive layout improvements.

## Testing (Suggested Pattern)
Add JUnit tests under `src/test/java` e.g.:
- `DatabaseServiceTest`: CRUD operations (with test DB).
- Attainment calculation tests with synthetic data.
Run:
```bash
mvn test
```

## Security Notes
- Replace plain-text passwords with hashed storage (BCrypt / Argon2).
- Lock DB user to least privileges (SELECT/INSERT/UPDATE only as needed).

## License
See [LICENSE](LICENSE).

## Attribution / Credits
- JavaFX & ControlsFX for UI.
- Apache POI & iText for planned reporting.
- JFreeChart for potential attainment visualizations.

## Support
Open an issue describing:
- Environment (OS, JDK, MySQL version)
- Steps to reproduce
- Expected vs actual behavior
- Relevant logs/stack traces

---
Maintained as a learning-oriented academic assessment tooling prototype. Contributions welcome.

