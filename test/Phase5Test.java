import javax.swing.SwingUtilities;
import java.util.ArrayList;

/**
 * Phase5Test.java
 * Test runner for the Event Management Dashboard Swing GUI.
 * Tests:
 * 1. Initializing EventManager and sample events.
 * 2. Pre-loading sample registrations to test attendee counting.
 * 3. Launching EventManagerDashboard on the Event Dispatch Thread (EDT).
 * 4. Verifying CRUD controls, row selection, and registration views.
 */
public class Phase5Test {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("              PHASE 5: EVENT MANAGEMENT DASHBOARD SWING TEST              ");
        System.out.println("==========================================================================");

        // 1. Prepare EventManager with sample events
        EventManager eventManager = new EventManager();
        eventManager.loadSampleEvents();
        System.out.println("Loaded " + eventManager.getEventCount() + " sample events into EventManager.");

        // 2. Prepare sample registrations
        ArrayList<Registration> sampleRegs = new ArrayList<Registration>();
        sampleRegs.add(new Registration(
            8001, 101, "Rahul Sharma", "rahul@campus.edu", 1, "National Hackathon 2026", "2026-10-23 09:00 AM"
        ));
        sampleRegs.add(new Registration(
            8002, 102, "Ananya Sen", "ananya@campus.edu", 3, "RoboWars: Combat Robotics", "2026-10-23 09:15 AM"
        ));
        sampleRegs.add(new Registration(
            8003, 103, "Vikram Rao", "vikram@campus.edu", 4, "Campus Badminton Championship", "2026-10-23 10:00 AM"
        ));
        sampleRegs.add(new Registration(
            8004, 104, "Priya Patel", "priya@campus.edu", 2, "AI & Machine Learning Bootcamp", "2026-10-23 10:30 AM"
        ));
        sampleRegs.add(new Registration(
            8005, 105, "Amit Verma", "amit@campus.edu", 1, "National Hackathon 2026", "2026-10-23 11:00 AM"
        ));
        System.out.println("Pre-loaded " + sampleRegs.size() + " sample registrations to test count display.");

        // 3. Launch EventManagerDashboard on the EDT (Slide 31)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    System.out.println("\n[1] Instantiating EventManagerDashboard...");
                    EventManagerDashboard dashboard = new EventManagerDashboard(eventManager, sampleRegs);
                    dashboard.setVisible(true);

                    System.out.println("\n>>> Event Management Dashboard launched and visible! <<<");
                    System.out.println(">>> You can test: Add Event, Update Event, Delete Event, and Registration Counts! <<<");
                } catch (Exception ex) {
                    System.err.println("Error launching EventManagerDashboard: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
}
