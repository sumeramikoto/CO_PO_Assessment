# CO-PO Assessment System

## Overview

The **CO-PO Assessment System** is a comprehensive educational management platform designed to track, measure, and analyze **Course Outcomes (CO)** against **Program Outcomes (PO)** in academic institutions. This JavaFX-based desktop application provides a robust solution for educational institutions to monitor student performance, assess curriculum effectiveness, and ensure alignment with program objectives according to accreditation standards.

Developed for the **Islamic University of Technology (IUT)**, this system facilitates outcome-based education (OBE) assessment and reporting for faculty and administrators.

---

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Requirements](#system-requirements)
- [Installation & Setup](#installation--setup)
- [Database Configuration](#database-configuration)
- [Running the Application](#running-the-application)
- [User Guide](#user-guide)
- [Project Structure](#project-structure)
- [Building for Production](#building-for-production)
- [Default Credentials](#default-credentials)
- [Contributing](#contributing)
- [License](#license)

---

## Features

### 🔐 Authentication & User Management
- **Multi-role Login System**: Separate authentication for administrators and faculty members
- **Secure Password Storage**: SHA-256 hashing for enhanced security
- **Session Management**: Maintains active user sessions throughout the application lifecycle

### 👨‍💼 Administrator Dashboard
- **Course Management**: Create, edit, and delete courses with details including:
  - Course code, name, and credits
  - Department and program affiliation
  - Academic year tracking
- **Faculty Management**: 
  - Register new faculty members
  - Manage faculty profiles and credentials
  - View faculty course assignments
- **Student Management**:
  - Add and manage student records
  - Track batch, department, and program information
  - Manage student enrollment across multiple courses
- **Course Assignment**: Assign faculty members to specific courses for each academic year
- **Enrollment Management**: Track and manage student enrollment in courses with program and year specificity

### 👨‍🏫 Faculty Dashboard
- **Assigned Courses View**: Access all courses assigned to the logged-in faculty member
- **Assessment Management**:
  - Create and manage Quiz questions (Quiz 1-4)
  - Create and manage Mid-term examination questions
  - Create and manage Final examination questions
  - Define CO-PO mappings for each question
- **Student Performance Tracking**:
  - Record marks for individual students across all assessments
  - View detailed performance metrics
  - Track CO and PO attainment levels
- **Reports Generation**: Generate comprehensive CO-PO attainment reports

### 📊 Assessment & Reporting
- **CO-PO Mapping**: Map each assessment question to specific course outcomes and program outcomes
- **Performance Analytics**: 
  - Visualize student performance trends
  - Analyze CO attainment levels
  - Calculate PO attainment across courses
- **PDF Report Generation**: Create detailed, professional reports using iText library
- **Excel Integration**: 
  - Export assessment data to Excel format
  - Import bulk student data from Excel files
- **Data Visualization**: Generate charts and graphs using JFreeChart for visual analysis

### 📈 Advanced Features
- **Multi-assessment Support**: Quizzes (1-4), Mid-term, and Final examinations
- **Flexible Question Naming**: Support for sub-questions (1a, 1b, 2a, etc.)
- **Academic Year Tracking**: Maintain historical data across multiple academic years
- **Program-specific Courses**: Handle different programs (SWE, CSE, etc.) with separate course codes
- **Comprehensive Views**: Pre-built database views for performance analysis

---

## Technology Stack

### Frontend
- **JavaFX 17**: UI framework for desktop application
- **FXML**: Declarative UI layouts
- **FormsFX**: Enhanced form controls
- **ValidatorFX**: Input validation framework
- **Ikonli**: Icon library for JavaFX

### Backend
- **Java 17**: Core programming language
- **Maven**: Dependency management and build automation
- **MySQL Connector**: Database connectivity (version 8.0.33)
- **jBCrypt**: Password hashing library

### Database
- **MySQL 8.0+**: Relational database management system
- **JDBC**: Java Database Connectivity

### Libraries & Tools
- **Apache POI 5.4.1**: Excel file handling (XLSX format)
- **iText 9.2.0**: PDF generation for reports
- **JFreeChart 1.5.4**: Charts and data visualization
- **JUnit 5**: Unit testing framework

---

## System Requirements

### Software Requirements
- **Java Development Kit (JDK)**: Version 17 or higher
- **Maven**: Version 3.6 or higher
- **MySQL Server**: Version 8.0 or higher
- **Operating System**: Windows, macOS, or Linux

### Hardware Requirements (Recommended)
- **Processor**: Intel Core i3 or equivalent
- **RAM**: 4 GB minimum, 8 GB recommended
- **Disk Space**: 500 MB for application and dependencies
- **Display**: 1280x720 minimum resolution (Full HD recommended)

---

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd CO_PO_As
```

### 2. Install MySQL

Download and install MySQL Server 8.0+ from the [official website](https://dev.mysql.com/downloads/mysql/).

### 3. Create Database

Open MySQL command line or MySQL Workbench and run:

```sql
CREATE DATABASE SPL2;
USE SPL2;
```

### 4. Initialize Database Schema

Run the schema creation script:

```bash
mysql -u root -p SPL2 < src/main/resources/schema.sql
```

### 5. Insert Sample Data (Optional)

To populate the database with sample data:

```bash
mysql -u root -p SPL2 < insert.sql
```

This will create:
- Sample admin account
- 5 faculty members
- 14 students
- 6 courses
- Course assignments
- CO and PO master data (CO1-CO20, PO1-PO12)

---

## Database Configuration

### Update Database Credentials

Edit the file `src/main/resources/dbcreds.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/SPL2
db.username=your_mysql_username
db.password=your_mysql_password
```

**⚠️ Important**: Ensure this file is not committed to version control with real credentials. Add it to `.gitignore` if needed.

### Database Schema Overview

The system uses the following main tables:
- **Admin**: Administrator credentials
- **Faculty**: Faculty member information
- **Student**: Student records
- **Course**: Course information with program/department
- **CourseAssignment**: Faculty-to-course mappings
- **Enrollment**: Student-to-course enrollments
- **CO/PO**: Course and Program outcome master tables
- **Quiz/Mid/Final**: Assessment structures
- **QuizQuestion/MidQuestion/FinalQuestion**: Question details with CO-PO mappings
- **StudentQuizMarks/StudentMidMarks/StudentFinalMarks**: Student performance data

---

## Running the Application

### Using Maven

```bash
# Compile the project
mvn clean compile

# Run the application
mvn javafx:run
```

### Using Maven Wrapper (Windows)

```cmd
mvnw.cmd clean compile
mvnw.cmd javafx:run
```

### Using Maven Wrapper (Linux/Mac)

```bash
./mvnw clean compile
./mvnw javafx:run
```

### Using IDE (IntelliJ IDEA / Eclipse)

1. Import the project as a Maven project
2. Wait for dependencies to download
3. Run the main class: `org.example.co_po_assessment.Launcher`

---

## User Guide

### First Time Login

#### Administrator Login
- **Email**: `admin@iut-dhaka.edu`
- **Password**: `admin123`

#### Faculty Login (Sample)
- **Email**: `rahman@iut-dhaka.edu`
- **Password**: `admin123`

### Administrator Workflow

1. **Login** using admin credentials
2. **Add Faculty**: Navigate to Faculty Management → Add new faculty members
3. **Add Courses**: Navigate to Course Management → Create courses with details
4. **Assign Courses**: Navigate to Course Assignments → Assign faculty to courses
5. **Add Students**: Navigate to Student Management → Register students
6. **Manage Enrollments**: Navigate to Enrollment Management → Enroll students in courses

### Faculty Workflow

1. **Login** using faculty credentials
2. **View Assigned Courses**: See all courses assigned for the current academic year
3. **Manage Questions**:
   - Select a course
   - Add questions for Quizzes, Mid-term, or Final exams
   - Assign CO and PO to each question
4. **Enter Student Marks**:
   - Select assessment type
   - Enter marks for enrolled students
5. **Generate Reports**:
   - Navigate to Reports section
   - Select course and report type (CO or PO)
   - Generate and export PDF reports

### CO-PO Mapping

Each assessment question must be mapped to:
- **One Course Outcome (CO)**: Learning objective specific to the course
- **One Program Outcome (PO)**: Graduate attribute aligned with program goals

Example:
- Question 1a (10 marks) → CO1, PO2
- Question 1b (15 marks) → CO2, PO3

---

## Project Structure

```
CO_PO_As/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/co_po_assessment/
│   │   │       ├── Launcher.java                    # Application entry point
│   │   │       ├── admin_input_controller/          # Admin UI controllers
│   │   │       ├── dashboard_controller/            # Dashboard logic
│   │   │       ├── DashboardPanels/                 # Main dashboard views
│   │   │       │   ├── AssessmentSystem.java        # Login & main window
│   │   │       │   ├── AdminDashboardView.java      # Admin dashboard
│   │   │       │   └── FacultyDashboardView.java    # Faculty dashboard
│   │   │       ├── DB_Configuration/                # Database config
│   │   │       ├── DB_helper/                       # Database operations
│   │   │       ├── faculty_input_controller/        # Faculty UI controllers
│   │   │       ├── Objects/                         # Data models/POJOs
│   │   │       ├── Report_controller/               # Report generation
│   │   │       └── utilities/                       # Helper utilities
│   │   └── resources/
│   │       ├── dbcreds.properties                   # DB credentials (configure this)
│   │       ├── schema.sql                           # Database schema
│   │       └── org/example/co_po_assessment/        # FXML views
│   └── test/
│       └── java/                                    # Unit tests
├── lib/                                             # External JAR dependencies
├── target/                                          # Compiled classes
├── pom.xml                                          # Maven configuration
├── insert.sql                                       # Sample data
└── README.md                                        # This file
```

---

## Building for Production

### Create Executable JAR

```bash
mvn clean package
```

This creates an executable JAR with all dependencies:
- `target/CO_PO_Assessment-1.0.0-jar-with-dependencies.jar`

### Run the JAR

```bash
java -jar target/CO_PO_Assessment-1.0.0-jar-with-dependencies.jar
```

### Create Windows Installer (MSI)

```bash
mvn clean package
mvn jpackage:jpackage
```

This creates a Windows MSI installer in the project root directory.

**Requirements for installer creation**:
- WiX Toolset (Windows)
- Java 17+ with jpackage tool

---

## Default Credentials

### Admin Account
- **Email**: `admin@iut-dhaka.edu`
- **Password**: `admin123`

### Sample Faculty Accounts
All faculty accounts use password: `admin123`

| Faculty ID | Name | Email |
|------------|------|-------|
| 1001 | Dr. Mohammad Rahman | rahman@iut-dhaka.edu |
| 1002 | Dr. Fatima Ahmed | ahmed@iut-dhaka.edu |
| 1003 | Dr. Ali Hassan | hassan@iut-dhaka.edu |
| 1004 | Dr. Shariar Khan | khan@iut-dhaka.edu |
| 1005 | Dr. Nazmul Islam | islam@iut-dhaka.edu |

**⚠️ Security Notice**: Change all default passwords after first login in production environments.

---

## Development & Testing

### Running Tests

```bash
mvn test
```

### Test Coverage

The project includes unit tests for:
- Database configuration loading
- Connection management
- Data validation

### Adding New Tests

Create test files in `src/test/java/org/example/co_po_assessment/`

---

## Troubleshooting

### Common Issues

#### Database Connection Failed
- Verify MySQL service is running
- Check credentials in `dbcreds.properties`
- Ensure database `SPL2` exists
- Verify MySQL port (default: 3306)

#### JavaFX Runtime Components Missing
- Ensure JavaFX SDK is properly configured
- Use Java 17 or higher with bundled JavaFX support
- Run with Maven: `mvn javafx:run`

#### Module Not Found Errors
- Clean and rebuild: `mvn clean install`
- Check `module-info.java` for correct module declarations

#### Excel Import/Export Issues
- Ensure Apache POI libraries are in classpath
- Check Excel file format (XLSX supported)
- Verify file permissions

---

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Comment complex logic
- Write unit tests for new features
- Update documentation as needed

---

## License

This project is licensed under the terms specified in the [LICENSE](LICENSE) file.

**Copyright © 2025 Islamic University of Technology**

---

## Acknowledgments

- **Islamic University of Technology (IUT)** - Project sponsor and requirements provider
- **Apache POI Team** - Excel processing library
- **iText Team** - PDF generation library
- **OpenJFX Community** - JavaFX framework

---

## Contact & Support

For issues, questions, or contributions:
- Create an issue in the repository
- Contact the development team at IUT

---

## Version History

### Version 1.0.0 (Current)
- Initial release
- Complete CO-PO assessment functionality
- Admin and Faculty dashboards
- Report generation (PDF)
- Excel import/export
- Multi-assessment support (Quiz, Mid, Final)

---

## Future Enhancements

Planned features for upcoming versions:
- [ ] Student portal for viewing own performance
- [ ] Bulk mark upload via Excel
- [ ] Advanced analytics dashboard with charts
- [ ] Email notifications for faculty and students
- [ ] Backup and restore functionality
- [ ] Multi-language support
- [ ] Mobile-responsive web version
- [ ] Cloud database support
- [ ] Role-based access control refinement
- [ ] Automated CO-PO attainment calculation

---

## Technical Documentation

### Database Views

The system includes pre-built views for easy data retrieval:
- `StudentQuizPerformance`: Student performance on quizzes with CO-PO mapping
- `StudentMidPerformance`: Mid-term exam performance analysis
- `StudentFinalPerformance`: Final exam performance analysis

### Password Security

Passwords are hashed using SHA-256 algorithm before storage. The format in database:
```
sha256$<hash_value>
```

### Academic Year Format

Academic years are stored in format: `YYYY-YYYY` (e.g., `2024-2025`)

---

**Built with ❤️ for Academic Excellence**
