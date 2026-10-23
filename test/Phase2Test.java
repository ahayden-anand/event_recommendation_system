import java.util.ArrayList;

/**
 * Phase2Test.java
 * Test class located in the test/ folder.
 * Tests:
 * 1. Storing and displaying events in ArrayList<Event>.
 * 2. Creating and inspecting Student profile data.
 * 3. Adding custom events and finding events by ID.
 * 4. Storing registrations in ArrayList<Registration>.
 */
public class Phase2Test {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("              PHASE 2: STUDENT PROFILE AND EVENT DATA TEST                ");
        System.out.println("==========================================================================");

        // 1. Initialize EventManager and load sample campus events
        System.out.println("\n[1] Initializing EventManager and loading sample events...");
        EventManager manager = new EventManager();
        manager.loadSampleEvents();
        System.out.println("Total events loaded: " + manager.getEventCount());

        // 2. Display all campus events using basic loop
        System.out.println("\n[2] Displaying all events from ArrayList<Event>:");
        manager.displayAllEvents();

        // 3. Test dynamic addition of a new event
        System.out.println("\n[3] Testing dynamic event addition...");
        Event customEvent = new Event(
            8,
            "Cyber Security Capture-the-Flag (CTF)",
            "Coding",
            "2026-11-30",
            "Evening",
            80.0,
            "Network Security Lab",
            "Networking, Linux, Problem Solving",
            "CSE, IT",
            0
        );
        manager.addEvent(customEvent);
        System.out.println("Added: " + customEvent.getTitle());
        System.out.println("New total event count: " + manager.getEventCount());

        // 4. Test finding an event by ID
        System.out.println("\n[4] Testing findEventById(2)...");
        Event foundEvent = manager.findEventById(2);
        if (foundEvent != null) {
            System.out.println("Found Event: " + foundEvent.getTitle() + " | Fee: Rs. " + foundEvent.getFee());
        } else {
            System.out.println("Event not found!");
        }

        // 5. Create a sample Student Profile
        System.out.println("\n[5] Creating and displaying Student Profile...");
        Student student = new Student(
            201,
            "Priya Patel",
            "priya.patel@campus.edu",
            "CSE",
            3,
            200.0,
            "AI, Coding",
            "Python, Java, Machine Learning",
            "Morning"
        );
        student.displayStudent();

        // 6. Test storing registrations using ArrayList<Registration>
        System.out.println("\n[6] Testing in-memory registration for Priya Patel...");
        ArrayList<Registration> registrations = new ArrayList<Registration>();
        
        // Register for event 2 (AI Bootcamp)
        Registration reg1 = new Registration(
            5001,
            student.getStudentId(),
            student.getName(),
            student.getEmail(),
            foundEvent.getEventId(),
            foundEvent.getTitle(),
            "2026-10-23 11:15 AM"
        );
        registrations.add(reg1);

        System.out.println("Student successfully registered for " + registrations.size() + " event(s):");
        for (int i = 0; i < registrations.size(); i++) {
            Registration r = registrations.get(i);
            System.out.println("  - Registration #" + r.getRegId() + ": " 
                               + r.getStudentName() + " registered for \"" 
                               + r.getEventTitle() + "\" on " + r.getRegistrationDate());
        }

        System.out.println("\n>>> Phase 2: Student Profile and Event Data Logic Tested Successfully! <<<");
    }
}
