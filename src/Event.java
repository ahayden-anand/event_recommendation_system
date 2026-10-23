/**
 * Event.java
 * Model class representing a campus event.
 * Follows core OOP encapsulation with attributes, constructors, and getters/setters.
 */
public class Event {
    // Private attributes (Encapsulation)
    private int eventId;
    private String title;
    private String category;        // e.g., "Coding", "AI", "Robotics", "Sports", "Arts", "Entrepreneurship"
    private String eventDate;       // e.g., "2026-11-15"
    private String timeSlot;        // e.g., "Morning", "Afternoon", "Evening"
    private double fee;             // Registration fee
    private String venue;           // e.g., "Main Auditorium", "Lab 3"
    private String requiredSkills;  // e.g., "Java, Problem Solving"
    private String eligibleDept;    // e.g., "CSE, IT" or "All"
    private int eligibleYear;       // e.g., 1, 2, 3, 4 (0 means open for all years)

    // Default Constructor
    public Event() {
    }

    // Parameterized Constructor
    public Event(int eventId, String title, String category, String eventDate, 
                 String timeSlot, double fee, String venue, 
                 String requiredSkills, String eligibleDept, int eligibleYear) {
        this.eventId = eventId;
        this.title = title;
        this.category = category;
        this.eventDate = eventDate;
        this.timeSlot = timeSlot;
        this.fee = fee;
        this.venue = venue;
        this.requiredSkills = requiredSkills;
        this.eligibleDept = eligibleDept;
        this.eligibleYear = eligibleYear;
    }

    // Getters and Setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getEligibleDept() {
        return eligibleDept;
    }

    public void setEligibleDept(String eligibleDept) {
        this.eligibleDept = eligibleDept;
    }

    public int getEligibleYear() {
        return eligibleYear;
    }

    public void setEligibleYear(int eligibleYear) {
        this.eligibleYear = eligibleYear;
    }

    // Method to display event details in a clear format
    public void displayEvent() {
        System.out.println("----------------------------------------");
        System.out.println("Event ID       : " + eventId);
        System.out.println("Title          : " + title);
        System.out.println("Category       : " + category);
        System.out.println("Date & Slot    : " + eventDate + " (" + timeSlot + ")");
        System.out.println("Fee            : Rs. " + fee);
        System.out.println("Venue          : " + venue);
        System.out.println("Skills Needed  : " + requiredSkills);
        System.out.println("Eligible Dept  : " + eligibleDept);
        System.out.println("Eligible Year  : " + (eligibleYear == 0 ? "All Years" : "Year " + eligibleYear));
        System.out.println("----------------------------------------");
    }
}
