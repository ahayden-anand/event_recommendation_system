import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
                   + "availability VARCHAR(50)"
                   + ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating students table: " + e.getMessage());
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
                                     + "interests=?, skills=?, availability=? WHERE email=?";
                    try (PreparedStatement upPs = conn.prepareStatement(updateSql)) {
                        upPs.setString(1, s.getName());
                        upPs.setString(2, s.getDepartment());
                        upPs.setInt(3, s.getAcademicYear());
                        upPs.setDouble(4, s.getMaxBudget());
                        upPs.setString(5, s.getInterests());
                        upPs.setString(6, s.getSkills());
                        upPs.setString(7, s.getAvailableSlot());
                        upPs.setString(8, s.getEmail());
                        return upPs.executeUpdate() > 0;
                    }
                } else {
                    // Insert new
                    String insertSql = "INSERT INTO students (name, email, department, academic_year, budget, interests, skills, availability) "
                                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement inPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        inPs.setString(1, s.getName());
                        inPs.setString(2, s.getEmail());
                        inPs.setString(3, s.getDepartment());
                        inPs.setInt(4, s.getAcademicYear());
                        inPs.setDouble(5, s.getMaxBudget());
                        inPs.setString(6, s.getInterests());
                        inPs.setString(7, s.getSkills());
                        inPs.setString(8, s.getAvailableSlot());
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
                    return new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getInt("academic_year"),
                        rs.getDouble("budget"),
                        rs.getString("interests"),
                        rs.getString("skills"),
                        rs.getString("availability")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving student: " + e.getMessage());
        }
        return null;
    }
}
