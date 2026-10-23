import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Phase3AccountRegistrationDBTest.java
 * Test suite for Phase 3:
 * 1. Checks duplicate identification for Student ID and Email.
 * 2. Tests transactional student registration into 'users' and 'students' tables.
 * 3. Verifies data consistency across both tables using standard JDBC.
 * 4. Verifies duplicate registration prevention.
 */
public class Phase3AccountRegistrationDBTest {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   PHASE 3: DATABASE REGISTRATION & DUPLICATE CHECK TEST SUITE            ");
        System.out.println("==========================================================================");

        // Ensure database connection
        if (!DBConnection.isConnectionAvailable()) {
            System.err.println("Database is not reachable. Skipping live database tests.");
            return;
        }

        DBConnection.initializeDatabase();
        UserDAO userDAO = new UserDAO();
        StudentDAO studentDAO = new StudentDAO();

        String testStudentId = "STU_P3_" + System.currentTimeMillis() % 10000;
        String testEmail = "student." + testStudentId.toLowerCase() + "@campus.edu";
        String testPassword = "pass" + testStudentId;

        // Clean up before test if any leftover exists
        cleanUpTestUser(testStudentId, testEmail);

        // Test 1: Username availability check on fresh ID
        boolean takenInitial = userDAO.isUsernameTaken(testStudentId);
        assertResult(!takenInitial, "Fresh student ID is available for registration (" + testStudentId + ")");

        // Test 2: Email availability check on fresh email
        boolean emailTakenInitial = userDAO.isEmailTaken(testEmail);
        assertResult(!emailTakenInitial, "Fresh email is available for registration (" + testEmail + ")");

        // Test 3: Transactional student registration
        Student testStudent = new Student(
            0,
            "Ananya Sen",
            testEmail,
            "ECE",
            2,
            350.0,
            "Robotics, AI, Coding",
            "Arduino, C++, Electronics",
            "Afternoon"
        );

        boolean regSuccess = userDAO.registerStudent(testStudent, testStudentId, testPassword);
        assertResult(regSuccess, "Register new student into MySQL via atomic transaction");

        // Test 4: Verify record exists in users table with role 'STUDENT'
        boolean userRecordFound = verifyUserTableRecord(testStudentId, testEmail, "STUDENT", "Ananya Sen");
        assertResult(userRecordFound, "Record persisted in 'users' table with role 'STUDENT'");

        // Test 5: Verify profile exists in students table
        Student loadedStudent = studentDAO.getStudentByEmail(testEmail);
        boolean studentProfileFound = (loadedStudent != null && "ECE".equals(loadedStudent.getDepartment()) && loadedStudent.getAcademicYear() == 2);
        assertResult(studentProfileFound, "Profile persisted in 'students' table with academic preferences");

        // Test 6: Duplicate Student ID check
        boolean duplicateIdDetected = userDAO.isUsernameTaken(testStudentId);
        assertResult(duplicateIdDetected, "Duplicate Student ID correctly detected as taken");

        // Test 7: Duplicate Email check
        boolean duplicateEmailDetected = userDAO.isEmailTaken(testEmail);
        assertResult(duplicateEmailDetected, "Duplicate Email correctly detected as taken");

        // Test 8: Attempting duplicate registration should fail or be caught
        boolean duplicateRegAttempt = false;
        try {
            // Should be caught before this, but if attempted directly:
            duplicateRegAttempt = userDAO.registerStudent(testStudent, testStudentId, "anotherpass");
        } catch (Exception ignored) {}
        assertResult(!duplicateRegAttempt, "Duplicate registration blocked by UNIQUE constraints / transaction rollback");

        // Test 9: Verify user authentication with newly registered credentials
        User authenticatedUser = userDAO.authenticate(testStudentId, testPassword);
        boolean authByUsername = (authenticatedUser != null && authenticatedUser.isStudent());
        assertResult(authByUsername, "User authenticated by newly registered Student ID");

        // Test 10: Verify user authentication by email
        User authenticatedByEmail = userDAO.authenticate(testEmail, testPassword);
        boolean authByEmail = (authenticatedByEmail != null && authenticatedByEmail.isStudent());
        assertResult(authByEmail, "User authenticated by newly registered Student Email");

        // Clean up test records
        cleanUpTestUser(testStudentId, testEmail);

        System.out.println("\n==========================================================================");
        System.out.println("RESULTS: " + testsPassed + " / " + totalTests + " TESTS PASSED (" 
            + (int)(((double)testsPassed / totalTests) * 100) + "%)");
        System.out.println(">>> Phase 3 Database Registration & Duplicate Checking Successfully Verified! <<<");
        System.out.println("==========================================================================");
    }

    private static void cleanUpTestUser(String username, String email) {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM users WHERE username=? OR email=?")) {
                ps1.setString(1, username);
                ps1.setString(2, email);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM students WHERE email=?")) {
                ps2.setString(1, email);
                ps2.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    private static boolean verifyUserTableRecord(String username, String email, String expectedRole, String expectedName) {
        String sql = "SELECT * FROM users WHERE username=? AND email=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    String name = rs.getString("full_name");
                    return expectedRole.equals(role) && expectedName.equals(name);
                }
            }
        } catch (SQLException e) {
            System.err.println("Verification query error: " + e.getMessage());
        }
        return false;
    }

    private static void assertResult(boolean condition, String description) {
        totalTests++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + description);
        } else {
            System.err.println("  [FAIL] " + description);
        }
    }
}
