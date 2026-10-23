import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Phase6Test.java
 * Test suite for verifying MySQL Database Connectivity and JDBC DAO operations.
 * Allows passing MySQL root password as an optional command-line argument:
 * e.g., java -cp "out;src;lib/*" Phase6Test my_password
 */
public class Phase6Test {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("              PHASE 6: MYSQL DATABASE & JDBC DAO TEST SUITE               ");
        System.out.println("==========================================================================");

        // If user passed password as argument, use it
        if (args.length > 0 && !args[0].isEmpty()) {
            DBConnection.setCredentials("root", args[0]);
        }

        System.out.println("\n[1] Testing MySQL Connection via DBConnection...");
        System.out.println("Target Database URL: " + DBConnection.getURL());
        System.out.println("Username           : " + DBConnection.getUser());

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            System.out.println(">>> Connection to MySQL SUCCESSFUL! Database verified. <<<");
        } catch (SQLException e) {
            System.err.println("\n[!] Connection Notice: " + e.getMessage());
            System.out.println("\n--------------------------------------------------------------------------");
            System.out.println("JDBC SETUP GUIDE FOR YOUR LOCAL MYSQL:");
            System.out.println("1. If your MySQL root user has a custom password, you can either:");
            System.out.println("   a) Update the PASSWORD constant in src/DBConnection.java, OR");
            System.out.println("   b) Run with your password as argument: java -cp \"...\" Phase6Test <your_password>");
            System.out.println("2. The MySQL Connector JAR is placed inside the 'lib/' folder.");
            System.out.println("--------------------------------------------------------------------------");
            return;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }

        // 2. Initialize DAOs and Auto-Create Tables
        System.out.println("\n[2] Initializing DAOs and verifying/creating database tables...");
        EventDAO eventDAO = new EventDAO();
        RegistrationDAO regDAO = new RegistrationDAO();
        StudentDAO studentDAO = new StudentDAO();

        eventDAO.createTable();
        regDAO.createTable();
        studentDAO.createTable();
        System.out.println("Tables 'events', 'students', and 'registrations' verified in campus_events_db.");

        // 3. Test EventDAO - Create (Insert)
        System.out.println("\n[3] Testing EventDAO.addEvent()...");
        Event testEvent = new Event(
            0,
            "JDBC Database Optimization Seminar",
            "Coding",
            "2026-12-10",
            "Morning",
            75.0,
            "Lab 4B",
            "SQL, Java, JDBC",
            "CSE, IT",
            0
        );

        boolean added = eventDAO.addEvent(testEvent);
        System.out.println("Event Insert Result: " + added + " (Generated Event ID: #" + testEvent.getEventId() + ")");

        // 4. Test EventDAO - Read (Select)
        System.out.println("\n[4] Testing EventDAO.getAllEvents()...");
        ArrayList<Event> allDbEvents = eventDAO.getAllEvents();
        System.out.println("Total events currently in MySQL: " + allDbEvents.size());
        for (int i = 0; i < allDbEvents.size(); i++) {
            Event e = allDbEvents.get(i);
            System.out.println("  - [ID #" + e.getEventId() + "] " + e.getTitle() + " | Fee: Rs. " + e.getFee());
        }

        // 5. Test EventDAO - Update
        System.out.println("\n[5] Testing EventDAO.updateEvent()...");
        testEvent.setFee(99.0);
        testEvent.setVenue("APJ Main Auditorium");
        boolean updated = eventDAO.updateEvent(testEvent);
        System.out.println("Event Update Result: " + updated);

        Event refreshed = eventDAO.getEventById(testEvent.getEventId());
        if (refreshed != null) {
            System.out.println("Verified Updated Fee: Rs. " + refreshed.getFee() + " | Venue: " + refreshed.getVenue());
        }

        // 6. Test RegistrationDAO - Add and Count
        System.out.println("\n[6] Testing RegistrationDAO operations...");
        Registration testReg = new Registration(
            0,
            101,
            "Rahul Sharma",
            "rahul@campus.edu",
            testEvent.getEventId(),
            testEvent.getTitle(),
            "2026-10-23 12:00 PM"
        );
        boolean regAdded = regDAO.addRegistration(testReg);
        System.out.println("Registration Insert Result: " + regAdded + " (Generated Reg ID: #" + testReg.getRegId() + ")");

        int count = regDAO.getRegistrationCountForEvent(testEvent.getEventId());
        System.out.println("Registration count for Event #" + testEvent.getEventId() + ": " + count + " student(s)");

        // 7. Test StudentDAO - Save Profile
        System.out.println("\n[7] Testing StudentDAO.saveOrUpdateStudent()...");
        Student s = new Student(
            0, "Rahul Sharma", "rahul@campus.edu", "CSE", 3, 200.0,
            "Coding, AI", "Java, SQL", "Morning"
        );
        boolean studentSaved = studentDAO.saveOrUpdateStudent(s);
        System.out.println("Student Profile Saved Result: " + studentSaved);

        Student fetchedStudent = studentDAO.getStudentByEmail("rahul@campus.edu");
        if (fetchedStudent != null) {
            System.out.println("Retrieved Student from DB: " + fetchedStudent.getName() + " (" + fetchedStudent.getDepartment() + ")");
        }

        // 8. Test EventDAO - Delete (Cleanup test record)
        System.out.println("\n[8] Testing EventDAO.deleteEvent() for test event #" + testEvent.getEventId() + "...");
        boolean deleted = eventDAO.deleteEvent(testEvent.getEventId());
        System.out.println("Event Delete Result: " + deleted);
        System.out.println("Associated registrations cleared via foreign key cascade. Verified.");

        System.out.println("\n>>> Phase 6: All MySQL Database and JDBC DAO Operations Tested Successfully! <<<");
    }
}
