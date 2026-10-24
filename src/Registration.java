/**
 * Registration.java
 * Model class representing an event registration by a student.
 * Connects student information with the chosen event.
 */
public class Registration {
    // Private attributes (Encapsulation)
    private int regId;
    private int studentId;
    private String studentName;
    private String studentEmail;
    private int eventId;
    private String eventTitle;
    private String registrationDate;
    private String eventDate;
    private String timeSlot;
    private String venue;
    private String status;

    // Default Constructor
    public Registration() {
        this.status = "CONFIRMED";
    }

    // Extended Parameterized Constructor
    public Registration(int regId, int studentId, String studentName, 
                        String studentEmail, int eventId, String eventTitle, 
                        String registrationDate, String eventDate, String timeSlot,
                        String venue, String status) {
        this.regId = regId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.registrationDate = registrationDate;
        this.eventDate = eventDate;
        this.timeSlot = timeSlot;
        this.venue = venue;
        this.status = (status != null && !status.isEmpty()) ? status : "CONFIRMED";
    }

    // Backward-Compatible Parameterized Constructor
    public Registration(int regId, int studentId, String studentName, 
                        String studentEmail, int eventId, String eventTitle, 
                        String registrationDate) {
        this(regId, studentId, studentName, studentEmail, eventId, eventTitle, registrationDate, "", "", "", "CONFIRMED");
    }

    // Getters and Setters
    public int getRegId() {
        return regId;
    }

    public void setRegId(int regId) {
        this.regId = regId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
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

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Method to display registration details
    public void displayRegistration() {
        System.out.println("----------------------------------------");
        System.out.println("Registration ID: " + regId);
        System.out.println("Student Name   : " + studentName + " (ID: " + studentId + ")");
        System.out.println("Student Email  : " + studentEmail);
        System.out.println("Event Title    : " + eventTitle + " (ID: " + eventId + ")");
        System.out.println("Registered On  : " + registrationDate);
        System.out.println("Schedule       : " + eventDate + " (" + timeSlot + ") at " + venue);
        System.out.println("Status         : " + status);
        System.out.println("----------------------------------------");
    }
}
