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

    // Default Constructor
    public Registration() {
    }

    // Parameterized Constructor
    public Registration(int regId, int studentId, String studentName, 
                        String studentEmail, int eventId, String eventTitle, 
                        String registrationDate) {
        this.regId = regId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.registrationDate = registrationDate;
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

    // Method to display registration details
    public void displayRegistration() {
        System.out.println("----------------------------------------");
        System.out.println("Registration ID: " + regId);
        System.out.println("Student Name   : " + studentName + " (ID: " + studentId + ")");
        System.out.println("Student Email  : " + studentEmail);
        System.out.println("Event Title    : " + eventTitle + " (ID: " + eventId + ")");
        System.out.println("Registered On  : " + registrationDate);
        System.out.println("----------------------------------------");
    }
}
