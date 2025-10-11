package org.example.co_po_assessment.utilities;

import org.example.co_po_assessment.DB_Services.DatabaseService;

/** Simple in-memory session holder for currently logged-in user (faculty). */
public final class UserSession {
    private static DatabaseService.FacultyInfo currentFaculty;
    private UserSession() {}

    public static void setCurrentFaculty(DatabaseService.FacultyInfo info) { currentFaculty = info; }
    public static DatabaseService.FacultyInfo getCurrentFaculty() { return currentFaculty; }
    public static void clear() { currentFaculty = null; }
}

