/**
 * Phase1Test.java
 * Test class to verify object creation, constructor invocation,
 * and getter/display methods for Student, Event, and Registration.
 */
public class Phase1Test {
    public static void main(String[] args) {
        System.out.println("===============================================");
        System.out.println("   PHASE 1: TESTING BASIC JAVA MODEL CLASSES   ");
        System.out.println("===============================================");

        // 1. Create and test a Student object
        System.out.println("\n[1] Creating a Student object...");
        Student student1 = new Student(
            101,
            "Rahul Sharma",
            "rahul.sharma@campus.edu",
            "CSE",
            3,
            250.0,
            "Coding, AI, Web Development",
            "Java, Python, HTML",
            "Morning"
        );
        student1.displayStudent();

        // 2. Create and test an Event object
        System.out.println("\n[2] Creating an Event object...");
        Event event1 = new Event(
            1,
            "National Hackathon 2026",
            "Coding",
            "2026-11-20",
            "Morning",
            150.0,
            "Main Auditorium",
            "Java, Python",
            "CSE, IT",
            3
        );
        event1.displayEvent();

        // 3. Create and test a Registration object linking student and event
        System.out.println("\n[3] Creating a Registration object...");
        Registration reg1 = new Registration(
            1001,
            student1.getStudentId(),
            student1.getName(),
            student1.getEmail(),
            event1.getEventId(),
            event1.getTitle(),
            "2026-10-23 10:30 AM"
        );
        reg1.displayRegistration();

        System.out.println(">>> Phase 1 Object Creation and Methods Tested Successfully! <<<");
    }
}
