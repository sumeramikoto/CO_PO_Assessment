# CO-PO Assessment System

## Overview

The CO-PO Assessment System is a comprehensive educational management platform designed to track, measure, and analyze Course Outcomes (CO) against Program Outcomes (PO) in an academic setting. This JavaFX-based application provides a robust solution for educational institutions to monitor student performance, assess curriculum effectiveness, and ensure alignment with program objectives.

## Features

### Authentication & User Management
- **Multi-user Login**: Separate authentication for administrators and faculty members
- **Secure Password Storage**: Implements password hashing using SHA-256 for enhanced security
- **User Session Management**: Maintains active sessions for authenticated users

### Administrator Dashboard
- **Course Management**: Add, edit, and remove courses with details like course code, name, credits, department, and program
- **Faculty Management**: Register and manage faculty information
- **Student Management**: Maintain student records and track enrollment
- **Course Assignment**: Assign faculty members to specific courses
- **Enrollment Management**: Track and manage student enrollment in courses

### Faculty Dashboard
- **Course View**: Access to assigned courses
- **Assessment Management**: Create and manage assessment questions
- **Student Performance Tracking**: Record and analyze student marks and performance
- **Reports Generation**: Generate assessment reports for courses

### Assessment & Reporting
- **CO-PO Mapping**: Map course outcomes to program outcomes
- **Performance Analytics**: Visualize student performance against course outcomes
- **PDF Report Generation**: Create detailed reports of CO-PO attainment
- **Data Export**: Export assessment data for further analysis

## Technology Stack

- **Frontend**: JavaFX (UI components, FXML for layouts)
- **Backend**: Java
- **Database**: MySQL
- **Authentication**: SHA-256 for password hashing
- **Reporting**: iText PDF for report generation
- **Data Visualization**: JFreeChart for charts and graphs
- **Excel Integration**: Apache POI for Excel file handling

## System Requirements

- Java Development Kit (JDK) 17 or higher
- JavaFX 17 or higher
- MySQL 8.0 or higher
- Maven for dependency management

## Installation & Setup

1. **Clone the repository**
   ```
   git clone [repository-url]
   cd CO_PO_Assessment2
   ```

2. **Database Setup**
   - Create a MySQL database named `SPL2`
   - Run the schema script located at `src/main/resources/schema.sql`
   - Update database credentials in:
     - `DatabaseService.java`
     - `CoursesDatabaseHelper.java`
     - And other database helper classes if necessary

3. **Build the project**
   ```
   mvn clean install
   ```

4. **Run the application**
   ```
   mvn javafx:run
   ```
   Alternatively, run the `AssessmentSystem` class directly from your IDE.

## Usage Guide

### Administrator Functions
1. Log in using administrator credentials
2. Use the dashboard to access various management modules
3. Manage courses, faculty, students, and course assignments
4. Generate and view reports

### Faculty Functions
1. Log in using faculty credentials
2. View assigned courses
3. Manage assessments and student marks
4. Generate CO-PO attainment reports for courses

## Database Structure

The application uses a MySQL database with the following main tables:
- Course: Stores course information
- Faculty: Manages faculty records
- Student: Contains student data
- CourseAssignment: Maps faculty to courses
- Enrollment: Tracks student enrollment in courses
- Assessment: Stores assessment details and questions

## Reporting

The system generates two types of reports:
- **CO Reports**: Assessment of Course Outcomes
- **PO Reports**: Analysis of Program Outcomes based on course attainments

Reports are saved in the respective folders:
- `co_reports/`
- `po_reports/`

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the terms of the license included in the repository.

## Acknowledgements

- Apache POI for Excel integration
- iText for PDF generation
- JFreeChart for data visualization
- MySQL Connector for database connectivity
- JavaFX for the user interface framework
