import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * StudentDAO.java
 * Data Access Object for persisting and retrieving Student profiles in MySQL.
 */
public class StudentDAO {

    /**
     * Creates the students table if it doesn't already exist.
     */
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS students ("
                   + "student_id INT AUTO_INCREMENT PRIMARY KEY, "
                   + "name VARCHAR(100) NOT NULL, "
                   + "email VARCHAR(100) NOT NULL UNIQUE, "
                   + "department VARCHAR(50) NOT NULL, "
                   + "academic_year INT NOT NULL, "
                   + "budget DOUBLE NOT NULL DEFAULT 0.0, "
                   + "interests VARCHAR(255), "
                   + "skills VARCHAR(255), "
                   + "availability VARCHAR(50), "
                   + "skill_level VARCHAR(20) DEFAULT 'Beginner', "
                   + "preferred_days VARCHAR(50) DEFAULT 'Both', "
                   + "career_goals VARCHAR(255) DEFAULT 'Improve programming skills', "
                   + "preferred_format VARCHAR(20) DEFAULT 'Either', "
                   + "free_only TINYINT(1) DEFAULT 0"
                   + ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Safe ALTER TABLE migration to add columns if they don't exist yet
            try { stmt.execute("ALTER TABLE students ADD COLUMN skill_level VARCHAR(20) DEFAULT 'Beginner'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN preferred_days VARCHAR(50) DEFAULT 'Both'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN career_goals VARCHAR(255) DEFAULT 'Improve programming skills'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN preferred_format VARCHAR(20) DEFAULT 'Either'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN free_only TINYINT(1) DEFAULT 0"); } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.err.println("Error creating/migrating students table: " + e.getMessage());
        }
    }

    /**
     * Saves or updates a student profile.
     */
    public boolean saveOrUpdateStudent(Student s) {
        String checkSql = "SELECT student_id FROM students WHERE email=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, s.getEmail());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    // Update existing
                    String updateSql = "UPDATE students SET name=?, department=?, academic_year=?, budget=?, "
                                     + "interests=?, skills=?, availability=?, skill_level=?, preferred_days=?, "
                                     + "career_goals=?, preferred_format=?, free_only=? WHERE email=?";
                    try (PreparedStatement upPs = conn.prepareStatement(updateSql)) {
                        upPs.setString(1, s.getName());
                        upPs.setString(2, s.getDepartment());
                        upPs.setInt(3, s.getAcademicYear());
                        upPs.setDouble(4, s.getMaxBudget());
                        upPs.setString(5, s.getInterests());
                        upPs.setString(6, s.getSkills());
                        upPs.setString(7, s.getAvailableSlot());
                        upPs.setString(8, s.getSkillLevel());
                        upPs.setString(9, s.getPreferredDays());
                        upPs.setString(10, s.getCareerGoals());
                        upPs.setString(11, s.getPreferredFormat());
                        upPs.setInt(12, s.isFreeOnly() ? 1 : 0);
                        upPs.setString(13, s.getEmail());
                        return upPs.executeUpdate() > 0;
                    }
                } else {
                    // Insert new
                    String insertSql = "INSERT INTO students (name, email, department, academic_year, budget, "
                                     + "interests, skills, availability, skill_level, preferred_days, career_goals, preferred_format, free_only) "
                                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement inPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        inPs.setString(1, s.getName());
                        inPs.setString(2, s.getEmail());
                        inPs.setString(3, s.getDepartment());
                        inPs.setInt(4, s.getAcademicYear());
                        inPs.setDouble(5, s.getMaxBudget());
                        inPs.setString(6, s.getInterests());
                        inPs.setString(7, s.getSkills());
                        inPs.setString(8, s.getAvailableSlot());
                        inPs.setString(9, s.getSkillLevel());
                        inPs.setString(10, s.getPreferredDays());
                        inPs.setString(11, s.getCareerGoals());
                        inPs.setString(12, s.getPreferredFormat());
                        inPs.setInt(13, s.isFreeOnly() ? 1 : 0);
                        int rows = inPs.executeUpdate();
                        if (rows > 0) {
                            try (ResultSet keys = inPs.getGeneratedKeys()) {
                                if (keys.next()) {
                                    s.setStudentId(keys.getInt(1));
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving student profile: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds a student profile by email.
     */
    public Student getStudentByEmail(String email) {
        String sql = "SELECT * FROM students WHERE email=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String skillLevel = "Beginner";
                    String preferredDays = "Both";
                    String careerGoals = "Improve programming skills";
                    String preferredFormat = "Either";
                    boolean freeOnly = false;

                    try { skillLevel = rs.getString("skill_level"); } catch (SQLException ignored) {}
                    try { preferredDays = rs.getString("preferred_days"); } catch (SQLException ignored) {}
                    try { careerGoals = rs.getString("career_goals"); } catch (SQLException ignored) {}
                    try { preferredFormat = rs.getString("preferred_format"); } catch (SQLException ignored) {}
                    try { freeOnly = rs.getInt("free_only") == 1; } catch (SQLException ignored) {}

                    return new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getInt("academic_year"),
                        rs.getDouble("budget"),
                        rs.getString("interests"),
                        rs.getString("skills"),
                        skillLevel != null ? skillLevel : "Beginner",
                        rs.getString("availability"),
                        preferredDays != null ? preferredDays : "Both",
                        careerGoals != null ? careerGoals : "Improve programming skills",
                        preferredFormat != null ? preferredFormat : "Either",
                        freeOnly
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving student: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all student profiles for Event Audience Insights.
     */
    public ArrayList<Student> getAllStudents() {
        ArrayList<Student> list = new ArrayList<Student>();
        String sql = "SELECT * FROM students ORDER BY student_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String skillLevel = "Beginner";
                String preferredDays = "Both";
                String careerGoals = "Improve programming skills";
                String preferredFormat = "Either";
                boolean freeOnly = false;

                try { skillLevel = rs.getString("skill_level"); } catch (SQLException ignored) {}
                try { preferredDays = rs.getString("preferred_days"); } catch (SQLException ignored) {}
                try { careerGoals = rs.getString("career_goals"); } catch (SQLException ignored) {}
                try { preferredFormat = rs.getString("preferred_format"); } catch (SQLException ignored) {}
                try { freeOnly = rs.getInt("free_only") == 1; } catch (SQLException ignored) {}

                list.add(new Student(
                    rs.getInt("student_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("department"),
                    rs.getInt("academic_year"),
                    rs.getDouble("budget"),
                    rs.getString("interests"),
                    rs.getString("skills"),
                    skillLevel != null ? skillLevel : "Beginner",
                    rs.getString("availability"),
                    preferredDays != null ? preferredDays : "Both",
                    careerGoals != null ? careerGoals : "Improve programming skills",
                    preferredFormat != null ? preferredFormat : "Either",
                    freeOnly
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all students: " + e.getMessage());
        }
        return list;
    }
}
