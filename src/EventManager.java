import java.util.ArrayList;

/**
 * EventManager.java
 * Manages the in-memory collection of campus events using an ArrayList.
 * Demonstrates basic Collection operations (add, iterate, filter) taught in class.
 */
public class EventManager {
    // List to store events dynamically in memory
    private ArrayList<Event> eventList;

    // Constructor
    public EventManager() {
        eventList = new ArrayList<Event>();
    }

    // Method to add a new event to the list
    public void addEvent(Event event) {
        eventList.add(event);
    }

    // Method to retrieve all events
    public ArrayList<Event> getAllEvents() {
        return eventList;
    }

    // Method to return the total number of events
    public int getEventCount() {
        return eventList.size();
    }

    // Method to find an event by its unique ID using a simple loop
    public Event findEventById(int eventId) {
        for (int i = 0; i < eventList.size(); i++) {
            Event current = eventList.get(i);
            if (current.getEventId() == eventId) {
                return current;
            }
        }
        return null; // Return null if not found
    }

    // Method to display all events using an index-based loop
    public void displayAllEvents() {
        System.out.println("==========================================================================");
        System.out.println("                         CAMPUS EVENTS CATALOG                            ");
        System.out.println("==========================================================================");
        
        if (eventList.isEmpty()) {
            System.out.println("No events are currently scheduled.");
            return;
        }

        for (int i = 0; i < eventList.size(); i++) {
            Event e = eventList.get(i);
            System.out.println("[" + (i + 1) + "] " + e.getTitle() + " (" + e.getCategory() + ")");
            System.out.println("    Date & Slot   : " + e.getEventDate() + " | Slot: " + e.getTimeSlot());
            System.out.println("    Venue         : " + e.getVenue());
            System.out.println("    Fee           : Rs. " + e.getFee());
            System.out.println("    Required Skills: " + e.getRequiredSkills());
            System.out.println("    Eligibility   : Dept: " + e.getEligibleDept() 
                               + " | Year: " + (e.getEligibleYear() == 0 ? "All" : "Year " + e.getEligibleYear()));
            System.out.println("--------------------------------------------------------------------------");
        }
    }

    // Helper method to populate realistic campus sample events for testing
    public void loadSampleEvents() {
        eventList.clear(); // Clear existing to prevent duplicate insertions

        eventList.add(new Event(
            1, "National Hackathon 2026", "Coding", 
            "2026-11-15", "Morning", 150.0, 
            "APJ Abdul Kalam Auditorium", "Java, Python, Problem Solving", 
            "CSE, IT", 3
        ));

        eventList.add(new Event(
            2, "AI & Machine Learning Bootcamp", "AI", 
            "2026-11-16", "Morning", 200.0, 
            "Computing Lab 1", "Python, Machine Learning", 
            "CSE, IT, ECE", 0 // 0 = All years
        ));

        eventList.add(new Event(
            3, "RoboWars: Combat Robotics", "Robotics", 
            "2026-11-18", "Afternoon", 300.0, 
            "Mechanical Engineering Workshop", "Arduino, C++, Electronics", 
            "MECH, ECE", 2
        ));

        eventList.add(new Event(
            4, "Campus Badminton Championship", "Sports", 
            "2026-11-20", "Evening", 50.0, 
            "Indoor Sports Arena", "Fitness, Agility", 
            "All", 0
        ));

        eventList.add(new Event(
            5, "Inter-College Fine Arts & Painting Fest", "Arts", 
            "2026-11-22", "Afternoon", 0.0, 
            "Open Air Amphitheatre", "Sketching, Painting, Design", 
            "All", 0
        ));

        eventList.add(new Event(
            6, "E-Cell Startup Pitch & Funding Expo", "Entrepreneurship", 
            "2026-11-25", "Morning", 100.0, 
            "Seminar Hall B", "Communication, Business Planning", 
            "All", 3
        ));

        eventList.add(new Event(
            7, "Full-Stack Web Development Workshop", "Coding", 
            "2026-11-28", "Morning", 120.0, 
            "Software Systems Lab 2", "HTML, CSS, JavaScript, Java", 
            "CSE, IT", 0
        ));

        eventList.add(new Event(
            8, "Cloud Computing & DevOps Masterclass", "Coding", 
            "2026-11-29", "Morning", 150.0, 
            "Cloud Computing Lab", "Java, Linux, Cloud", 
            "CSE, IT", 3
        ));

        eventList.add(new Event(
            9, "Drone Aerodynamics & Flight Workshop", "Robotics", 
            "2026-12-01", "Morning", 250.0, 
            "Aeronautics Center", "Electronics, C++, Problem Solving", 
            "CSE, ECE, MECH", 0
        ));
    }
}
