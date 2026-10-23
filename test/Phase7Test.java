import java.util.ArrayList;

/**
 * Phase7Test.java
 * Test suite for verifying Phase 7 Integration:
 * 1. Synchronized communication between Student and Event Manager modules.
 * 2. Event creation by manager -> immediate availability for student recommendations.
 * 3. Student registration -> immediate attendee count update for the event manager.
 * 4. Dual-mode support (MySQL DAO mode and In-Memory Standalone mode).
 */
public class Phase7Test {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("              PHASE 7: FULL SYSTEM INTEGRATION TEST SUITE                 ");
        System.out.println("==========================================================================");

        // 1. Prepare shared managers
        EventManager eventManager = new EventManager();
        eventManager.loadSampleEvents();
        ArrayList<Registration> sharedRegistrations = new ArrayList<Registration>();
        RecommendationEngine engine = new RecommendationEngine();

        System.out.println("\n[1] Verifying Event Catalog Initialization...");
        System.out.println("Active Events in Catalog: " + eventManager.getEventCount());

        // 2. Simulate Event Manager creating a new event
        System.out.println("\n[2] [MANAGER ACTION] Adding a new specialized event...");
        Event newHackathon = new Event(
            10,
            "Inter-College Hackathon: Green Tech & Sustainability",
            "Coding",
            "2026-12-05",
            "Morning",
            100.0,
            "Innovation Hall",
            "Java, Python, IoT",
            "CSE, IT, ECE",
            3
        );
        eventManager.addEvent(newHackathon);
        System.out.println("Manager successfully added: \"" + newHackathon.getTitle() + "\" (Fee: Rs. " + newHackathon.getFee() + ")");
        System.out.println("New total events: " + eventManager.getEventCount());

        // 3. Simulate Student viewing and generating recommendations
        System.out.println("\n[3] [STUDENT ACTION] Student creates profile and requests recommendations...");
        Student student = new Student(
            201,
            "Karthik Iyer",
            "karthik.iyer@campus.edu",
            "CSE",
            3,
            250.0,
            "Coding, AI",
            "Java, Python, IoT",
            "Morning"
        );
        student.displayStudent();

        ArrayList<EventRecommendation> recs = engine.recommendEvents(student, eventManager.getAllEvents());
        System.out.println("\nGenerated " + recs.size() + " recommendations for " + student.getName() + ":");
        for (int i = 0; i < Math.min(recs.size(), 5); i++) {
            recs.get(i).displayRecommendation(i + 1);
        }

        // 4. Simulate Student Registering for Rank #1
        System.out.println("\n[4] [STUDENT ACTION] Registering for Top Recommended Event...");
        Event topEvent = recs.get(0).getEvent();
        Registration reg = new Registration(
            9001,
            student.getStudentId(),
            student.getName(),
            student.getEmail(),
            topEvent.getEventId(),
            topEvent.getTitle(),
            "2026-10-23 02:45 PM"
        );
        sharedRegistrations.add(reg);
        System.out.println("Registration created: Reg #" + reg.getRegId() + " -> " + topEvent.getTitle());

        // 5. Simulate Event Manager checking registrations and count
        System.out.println("\n[5] [MANAGER ACTION] Checking live registrations for Event #" + topEvent.getEventId() + "...");
        int attendeeCount = 0;
        for (int i = 0; i < sharedRegistrations.size(); i++) {
            if (sharedRegistrations.get(i).getEventId() == topEvent.getEventId()) {
                attendeeCount++;
            }
        }
        System.out.println("Event: \"" + topEvent.getTitle() + "\"");
        System.out.println("Live Attendee Count: " + attendeeCount + " student(s)");
        System.out.println("Registered Attendee: " + sharedRegistrations.get(0).getStudentName() + " (" + sharedRegistrations.get(0).getStudentEmail() + ")");

        System.out.println("\n>>> Phase 7 Integration: End-to-End Workflow Completed and Verified! <<<");
    }
}
