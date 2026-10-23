/**
 * Student.java
 * Model class representing a student in the campus event system.
 * Demonstrates Encapsulation: private attributes with public getters and setters.
 */
public class Student {
    // Private attributes (Encapsulation)
    private int studentId;
    private String name;
    private String email;
    private String department;
    private int academicYear;       // e.g., 1, 2, 3, or 4
    private double maxBudget;       // Maximum registration fee student is willing to pay
    private String interests;       // e.g., "Coding, AI, Web Development"
    private String skills;          // e.g., "Java, Python, Communication"
    private String availableSlot;   // e.g., "Morning", "Afternoon", "Weekend"

    // Default Constructor
    public Student() {
    }

    // Parameterized Constructor
    public Student(int studentId, String name, String email, String department, 
                   int academicYear, double maxBudget, String interests, 
                   String skills, String availableSlot) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.academicYear = academicYear;
        this.maxBudget = maxBudget;
        this.interests = interests;
        this.skills = skills;
        this.availableSlot = availableSlot;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getAvailableSlot() {
        return availableSlot;
    }

    public void setAvailableSlot(String availableSlot) {
        this.availableSlot = availableSlot;
    }

    // Method to display student information in a formatted way
    public void displayStudent() {
        System.out.println("----------------------------------------");
        System.out.println("Student ID     : " + studentId);
        System.out.println("Name           : " + name);
        System.out.println("Email          : " + email);
        System.out.println("Department     : " + department);
        System.out.println("Academic Year  : Year " + academicYear);
        System.out.println("Max Budget     : Rs. " + maxBudget);
        System.out.println("Interests      : " + interests);
        System.out.println("Skills         : " + skills);
        System.out.println("Availability   : " + availableSlot);
        System.out.println("----------------------------------------");
    }
}
