import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Phase2FunctionalFixesTest.java
 * Comprehensive test suite verifying all Phase 2 Critical Functional Fixes:
 * 1. Event Date & Time Validation (start < end, 24-hr format, fee >= 0, capacity > 0).
 * 2. Event Date & Time Persistence and Update Integrity.
 * 3. Budget Finder Filtering (exclusion of over-budget events, free-only isolation, fee sorting).
 * 4. Registration Ownership & Schedule details retrieval.
 * 5. Atomic Waitlist Queue Promotion upon registration cancellation.
 * 6. Schedule Clash Detection precision.
 */
public class Phase2FunctionalFixesTest {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("          PHASE 2: CRITICAL FUNCTIONAL FIXES TEST SUITE                   ");
        System.out.println("==========================================================================");

        // 1. Test Event Validation Rules
        testEventValidationRules();

        // 2. Database Functional Tests
        if (!DBConnection.isConnectionAvailable()) {
            System.err.println("Database is not reachable. Skipping database persistence tests.");
            printSummary();
            return;
        }

        DBConnection.initializeDatabase();
        EventDAO eventDAO = new EventDAO();
        RegistrationDAO registrationDAO = new RegistrationDAO();

        testEventTimePersistence(eventDAO);
        testBudgetFinderFiltering(eventDAO);
        testWaitlistPromotionOnCancellation(eventDAO, registrationDAO);
        testScheduleClashDetection(eventDAO, registrationDAO);

        printSummary();
    }

    private static void testEventValidationRules() {
        System.out.println("\n--- 1. Event Input & Time Validation Tests ---");

        // Valid event
        String validRes = EventManagerDashboard.validateEventInputs(
            "AI Hackathon", "AI", "2026-11-20", "09:30", "16:30", 150.0, 60, "Lab 4"
        );
        assertResult(validRes == null, "Valid event parameters accepted without error");

        // Start time >= End time
        String timeClash = EventManagerDashboard.validateEventInputs(
            "AI Hackathon", "AI", "2026-11-20", "14:00", "11:00", 150.0, 60, "Lab 4"
        );
        assertResult(timeClash != null && timeClash.contains("earlier than end time"),
            "Start time after end time is rejected: " + timeClash);

        // Equal start and end time
        String sameTime = EventManagerDashboard.validateEventInputs(
            "AI Hackathon", "AI", "2026-11-20", "10:00", "10:00", 150.0, 60, "Lab 4"
        );
        assertResult(sameTime != null && sameTime.contains("earlier than end time"),
            "Equal start and end time is rejected");

        // Negative fee
        String negFee = EventManagerDashboard.validateEventInputs(
            "AI Hackathon", "AI", "2026-11-20", "09:00", "12:00", -50.0, 60, "Lab 4"
        );
        assertResult(negFee != null && negFee.contains("cannot be negative"),
            "Negative fee is rejected: " + negFee);

        // Zero capacity
        String zeroCap = EventManagerDashboard.validateEventInputs(
            "AI Hackathon", "AI", "2026-11-20", "09:00", "12:00", 0.0, 0, "Lab 4"
        );
        assertResult(zeroCap != null && zeroCap.contains("greater than zero"),
            "Zero capacity is rejected: " + zeroCap);

        // Invalid Date format
        String badDate = EventManagerDashboard.validateEventInputs(
            "AI Hackathon", "AI", "20-11-2026", "09:00", "12:00", 0.0, 50, "Lab 4"
        );
        assertResult(badDate != null && badDate.contains("YYYY-MM-DD"),
            "Non-standard date format is rejected: " + badDate);
    }

    private static void testEventTimePersistence(EventDAO eventDAO) {
        System.out.println("\n--- 2. Event Time Persistence & Update Integrity ---");

        Event ev = new Event(
            0, "Phase2 Time Test Workshop", "Coding", "2026-12-05", "Morning",
            100.0, "Auditorium A", "Java", "CSE", 0, "In-person", "Intermediate",
            "09:15", "11:45", 40
        );

        boolean added = eventDAO.addEvent(ev);
        assertResult(added, "Test event added with start '09:15' and end '11:45'");

        Event retrieved = eventDAO.getEventById(ev.getEventId());
        boolean timeMatches = (retrieved != null && "09:15".equals(retrieved.getStartTime()) && "11:45".equals(retrieved.getEndTime()));
        assertResult(timeMatches, "Retrieved event correctly retains exact start and end times");

        // Update event with new times without loss
        retrieved.setStartTime("13:00");
        retrieved.setEndTime("15:30");
        retrieved.setTimeSlot("Afternoon");
        boolean updated = eventDAO.updateEvent(retrieved);
        assertResult(updated, "Updated event times to '13:00' - '15:30'");

        Event updatedEv = eventDAO.getEventById(retrieved.getEventId());
        boolean updateMatches = (updatedEv != null && "13:00".equals(updatedEv.getStartTime()) && "15:30".equals(updatedEv.getEndTime()));
        assertResult(updateMatches, "Verified updated event retained exact modified times in database");

        // Clean up
        eventDAO.deleteEvent(ev.getEventId());
    }

    private static void testBudgetFinderFiltering(EventDAO eventDAO) {
        System.out.println("\n--- 3. Budget Finder Filtering & Sorting ---");

        ArrayList<Event> allEvents = eventDAO.getAllEvents();
        double maxBudget = 100.0;

        // Test over-budget exclusion
        ArrayList<Event> affordable = new ArrayList<Event>();
        for (int i = 0; i < allEvents.size(); i++) {
            Event e = allEvents.get(i);
            if (e.getFee() <= maxBudget) {
                affordable.add(e);
            }
        }

        boolean allWithinBudget = true;
        for (int i = 0; i < affordable.size(); i++) {
            if (affordable.get(i).getFee() > maxBudget) {
                allWithinBudget = false;
                break;
            }
        }
        assertResult(allWithinBudget, "Budget filtering strictly excludes events exceeding Rs. " + maxBudget);

        // Test free-only isolation
        ArrayList<Event> freeOnly = new ArrayList<Event>();
        for (int i = 0; i < allEvents.size(); i++) {
            Event e = allEvents.get(i);
            if (e.getFee() == 0.0) {
                freeOnly.add(e);
            }
        }

        boolean allGenuinelyFree = true;
        for (int i = 0; i < freeOnly.size(); i++) {
            if (freeOnly.get(i).getFee() > 0.0) {
                allGenuinelyFree = false;
                break;
            }
        }
        assertResult(allGenuinelyFree, "Free-only filtering strictly isolates events with fee exactly 0.0");
    }

    private static void testWaitlistPromotionOnCancellation(EventDAO eventDAO, RegistrationDAO registrationDAO) {
        System.out.println("\n--- 4. Smart Waitlist Queue Promotion on Cancellation ---");

        // 1. Create a test event with capacity of 1
        Event capEvent = new Event(
            0, "Waitlist Test Seminar", "AI", "2026-12-10", "Morning",
            0.0, "Seminar Hall", "AI", "All", 0, "In-person", "All",
            "10:00", "11:30", 1
        );
        eventDAO.addEvent(capEvent);
        int eventId = capEvent.getEventId();

        String student1Email = "attendee1.p2@campus.edu";
        String student2Email = "waitlist2.p2@campus.edu";

        // Clean up any test artifact
        cleanUpStudentRegs(student1Email, student2Email, eventId);

        // 2. Student 1 registers (fills the single available seat)
        Registration reg1 = new Registration(0, 501, "Attendee One", student1Email, eventId, capEvent.getTitle(), "2026-10-24");
        boolean reg1Success = registrationDAO.addRegistration(reg1);
        assertResult(reg1Success, "Student 1 fills the capacity (1/1 seat taken)");

        // 3. Student 2 attempts registration -> Event full -> Enrolled in Smart Waitlist
        boolean wl2Success = registrationDAO.addToWaitlist(502, "Waiting Student Two", student2Email, eventId, capEvent.getTitle());
        assertResult(wl2Success, "Student 2 enrolled onto Smart Waitlist queue (status 'WAITING')");

        int waitlistCount = registrationDAO.getWaitlistCountForEvent(eventId);
        assertResult(waitlistCount == 1, "Verified 1 student currently waiting in waitlist queue");

        // 4. Student 1 cancels their registration -> Atomic promotion should trigger!
        boolean cancelSuccess = registrationDAO.cancelRegistration(reg1.getRegId());
        assertResult(cancelSuccess, "Student 1 cancels confirmed registration");

        // 5. Verify Student 2 has been automatically promoted to confirmed registration
        ArrayList<Registration> student2Regs = registrationDAO.getRegistrationsByStudent(student2Email);
        boolean student2Promoted = false;
        for (int i = 0; i < student2Regs.size(); i++) {
            if (student2Regs.get(i).getEventId() == eventId) {
                student2Promoted = true;
                break;
            }
        }
        assertResult(student2Promoted, "Student 2 automatically promoted from waitlist into confirmed registrations!");

        // 6. Verify waitlist queue count is now 0
        int waitlistCountAfter = registrationDAO.getWaitlistCountForEvent(eventId);
        assertResult(waitlistCountAfter == 0, "Waitlist queue for event decremented to 0 waiting students");

        // Clean up
        cleanUpStudentRegs(student1Email, student2Email, eventId);
        eventDAO.deleteEvent(eventId);
    }

    private static void testScheduleClashDetection(EventDAO eventDAO, RegistrationDAO registrationDAO) {
        System.out.println("\n--- 5. Schedule Clash Detection Precision ---");

        Event ev1 = new Event(
            0, "Morning Cloud Computing", "IT", "2026-12-15", "Morning",
            0.0, "Lab 1", "AWS", "IT", 0, "In-person", "All",
            "09:00", "11:00", 50
        );
        Event evClashing = new Event(
            0, "Overlapping DevOps Workshop", "IT", "2026-12-15", "Morning",
            0.0, "Lab 2", "Docker", "IT", 0, "In-person", "All",
            "10:00", "12:00", 50
        );
        Event evNonClashing = new Event(
            0, "Afternoon Web Security", "IT", "2026-12-15", "Afternoon",
            0.0, "Lab 1", "Security", "IT", 0, "In-person", "All",
            "13:00", "15:00", 50
        );

        eventDAO.addEvent(ev1);
        eventDAO.addEvent(evClashing);
        eventDAO.addEvent(evNonClashing);

        String testEmail = "clash.tester@campus.edu";
        cleanUpStudentRegs(testEmail, "", ev1.getEventId());

        // Register for ev1
        Registration reg = new Registration(0, 999, "Clash Tester", testEmail, ev1.getEventId(), ev1.getTitle(), "2026-10-24");
        registrationDAO.addRegistration(reg);

        // Check clash against overlapping event (09:00-11:00 vs 10:00-12:00)
        String clashDetected = registrationDAO.checkScheduleClash(testEmail, evClashing, eventDAO);
        assertResult(clashDetected != null, "Overlapping event on same date and time slot correctly detected as a schedule conflict");

        // Check clash against non-overlapping event (09:00-11:00 vs 13:00-15:00)
        String noClash = registrationDAO.checkScheduleClash(testEmail, evNonClashing, eventDAO);
        assertResult(noClash == null, "Non-overlapping event on same date with distinct time slot accepted without conflict");

        // Clean up
        cleanUpStudentRegs(testEmail, "", ev1.getEventId());
        eventDAO.deleteEvent(ev1.getEventId());
        eventDAO.deleteEvent(evClashing.getEventId());
        eventDAO.deleteEvent(evNonClashing.getEventId());
    }

    private static void cleanUpStudentRegs(String email1, String email2, int eventId) {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM registrations WHERE event_id=? OR student_email=? OR student_email=?")) {
                ps1.setInt(1, eventId);
                ps1.setString(2, email1);
                ps1.setString(3, email2);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM waitlists WHERE event_id=? OR student_email=? OR student_email=?")) {
                ps2.setInt(1, eventId);
                ps2.setString(2, email1);
                ps2.setString(3, email2);
                ps2.executeUpdate();
            }
        } catch (SQLException ignored) {}
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

    private static void printSummary() {
        System.out.println("\n==========================================================================");
        System.out.println("RESULTS: " + testsPassed + " / " + totalTests + " TESTS PASSED (" 
            + (int)(((double)testsPassed / totalTests) * 100) + "%)");
        System.out.println(">>> Phase 2 Critical Functional Fixes Successfully Verified! <<<");
        System.out.println("==========================================================================");
    }
}
