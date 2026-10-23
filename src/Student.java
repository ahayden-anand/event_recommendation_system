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
    private String skillLevel;      // e.g., "Beginner", "Intermediate", "Advanced"
    private String availableSlot;   // e.g., "Morning", "Afternoon", "Evening", "All"
    private String preferredDays;   // e.g., "Weekdays", "Weekends", "Both"
    private String careerGoals;     // e.g., "Improve programming skills", "Prepare for placements"
    private String preferredFormat; // e.g., "In-person", "Online", "Either"
    private boolean freeOnly;       // true if student prefers free events only

    // Default Constructor
    public Student() {
        this.skillLevel = "Beginner";
        this.preferredDays = "Both";
        this.careerGoals = "Improve programming skills";
        this.preferredFormat = "Either";
        this.freeOnly = false;
    }

    // Parameterized Constructor (Comprehensive)
    public Student(int studentId, String name, String email, String department, 
                   int academicYear, double maxBudget, String interests, 
                   String skills, String skillLevel, String availableSlot,
                   String preferredDays, String careerGoals, String preferredFormat,
                   boolean freeOnly) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.department = department;
        this.academicYear = academicYear;
        this.maxBudget = maxBudget;
        this.interests = interests;
        this.skills = skills;
        this.skillLevel = skillLevel;
        this.availableSlot = availableSlot;
        this.preferredDays = preferredDays;
        this.careerGoals = careerGoals;
        this.preferredFormat = preferredFormat;
        this.freeOnly = freeOnly;
    }

    // Backward-compatible constructor
    public Student(int studentId, String name, String email, String department, 
                   int academicYear, double maxBudget, String interests, 
                   String skills, String availableSlot) {
        this(studentId, name, email, department, academicYear, maxBudget, interests, 
             skills, "Beginner", availableSlot, "Both", "Improve programming skills", "Either", false);
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

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getPreferredDays() {
        return preferredDays;
    }

    public void setPreferredDays(String preferredDays) {
        this.preferredDays = preferredDays;
    }

    public String getCareerGoals() {
        return careerGoals;
    }

    public void setCareerGoals(String careerGoals) {
        this.careerGoals = careerGoals;
    }

    public String getPreferredFormat() {
        return preferredFormat;
    }

    public void setPreferredFormat(String preferredFormat) {
        this.preferredFormat = preferredFormat;
    }

    public boolean isFreeOnly() {
        return freeOnly;
    }

    public void setFreeOnly(boolean freeOnly) {
        this.freeOnly = freeOnly;
    }

    // Method to display student information in a formatted way
    public void displayStudent() {
        System.out.println("----------------------------------------");
        System.out.println("Student ID     : " + studentId);
        System.out.println("Name           : " + name);
        System.out.println("Email          : " + email);
        System.out.println("Department     : " + department);
        System.out.println("Academic Year  : Year " + academicYear);
        System.out.println("Max Budget     : Rs. " + maxBudget + (freeOnly ? " (Free Only)" : ""));
        System.out.println("Interests      : " + interests);
        System.out.println("Skills         : " + skills + " (" + skillLevel + ")");
        System.out.println("Availability   : " + availableSlot + " | " + preferredDays);
        System.out.println("Career Goals   : " + careerGoals);
        System.out.println("Preferred Form : " + preferredFormat);
        System.out.println("----------------------------------------");
    }
}
