import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * StudentRegistrationFrame.java
 * Clean SaaS-style registration form for students.
 * Collects student details, academic criteria, and login credentials with comprehensive client validation.
 * Supports all 6 required preference profile aspects:
 * 1. Interests (Checkboxes: Coding, AI, Robotics, Entrepreneurship, Music, Dance, Photography, Sports, Public Speaking, Design)
 * 2. Skills & Skill Level (Text field + Beginner/Intermediate/Advanced)
 * 3. Budget & "Free events only" toggle
 * 4. Availability (Time slot + Preferred Days)
 * 5. Career & Learning Goals
 * 6. Location / Event Format (In-person, Online, Either)
 */
public class StudentRegistrationFrame extends JFrame {

    // Input fields
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtStudentId;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<String> cmbDept;
    private JComboBox<Integer> cmbYear;
    private JTextField txtBudget;
    private JCheckBox cbFreeOnly;
    private JComboBox<String> cmbTimeSlot;
    private JComboBox<String> cmbPreferredDays;
    private JTextField txtSkills;
    private JComboBox<String> cmbSkillLevel;
    private JComboBox<String> cmbCareerGoals;
    private JComboBox<String> cmbPreferredFormat;

    // 10 Specific Course-Aligned Interest Checkboxes
    private JCheckBox cbCoding;
    private JCheckBox cbAI;
    private JCheckBox cbRobotics;
    private JCheckBox cbEntrepreneurship;
    private JCheckBox cbMusic;
    private JCheckBox cbDance;
    private JCheckBox cbPhotography;
    private JCheckBox cbSports;
    private JCheckBox cbPublicSpeaking;
    private JCheckBox cbDesign;

    // Action buttons
    private JButton btnCreateAccount;
    private JButton btnBackToLogin;

    // SaaS Color Palette Constants
    private static final Color BG_CANVAS = new Color(244, 246, 249);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_CARD = new Color(220, 224, 230);
    private static final Color TEXT_NAVY = new Color(26, 37, 48);
    private static final Color TEXT_MUTED = new Color(127, 140, 141);
    private static final Color ACCENT_BLUE = new Color(41, 128, 185);

    public StudentRegistrationFrame() {
        setTitle("Campus Event Recommendation System - Create Student Account");
        setSize(720, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        getContentPane().setBackground(BG_CANVAS);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(BG_CANVAS);
        outerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        card.setPreferredSize(new Dimension(650, 780));

        // 1. Header (Title & Subtitle)
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Create Student Account & Preference Profile", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_NAVY);
        headerPanel.add(lblTitle);

        JLabel lblSubtitle = new JLabel("Configure your academic and personal preferences for transparent event recommendations.", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);
        headerPanel.add(lblSubtitle);

        card.add(headerPanel, BorderLayout.NORTH);

        // 2. Form Grid (Two-column layout)
        JPanel formGrid = new JPanel(new GridLayout(13, 2, 10, 8));
        formGrid.setOpaque(false);

        // Row 1: Full Name
        formGrid.add(createLabel("Full Name: *"));
        txtFullName = createTextField("");
        formGrid.add(txtFullName);

        // Row 2: Student Email
        formGrid.add(createLabel("Student Email: *"));
        txtEmail = createTextField("");
        formGrid.add(txtEmail);

        // Row 3: Student ID / Reg No
        formGrid.add(createLabel("Student ID / Reg No: *"));
        txtStudentId = createTextField("");
        formGrid.add(txtStudentId);

        // Row 4: Password
        formGrid.add(createLabel("Password: *"));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(txtPassword);

        // Row 5: Confirm Password
        formGrid.add(createLabel("Confirm Password: *"));
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formGrid.add(txtConfirmPassword);

        // Row 6: Department & Academic Year
        formGrid.add(createLabel("Department & Year: *"));
        JPanel deptYearPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        deptYearPanel.setOpaque(false);
        cmbDept = new JComboBox<String>(new String[]{"CSE", "IT", "ECE", "MECH", "CIVIL"});
        cmbDept.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbYear = new JComboBox<Integer>(new Integer[]{1, 2, 3, 4});
        cmbYear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deptYearPanel.add(cmbDept);
        deptYearPanel.add(cmbYear);
        formGrid.add(deptYearPanel);

        // Row 7: Max Budget & Free Only
        formGrid.add(createLabel("Budget & Affordability: *"));
        JPanel budgetPanel = new JPanel(new BorderLayout(8, 0));
        budgetPanel.setOpaque(false);
        txtBudget = createTextField("200.0");
        cbFreeOnly = new JCheckBox("Free events only");
        cbFreeOnly.setOpaque(false);
        cbFreeOnly.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbFreeOnly.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cbFreeOnly.isSelected()) {
                    txtBudget.setText("0.0");
                    txtBudget.setEnabled(false);
                } else {
                    txtBudget.setEnabled(true);
                    txtBudget.setText("200.0");
                }
            }
        });
        budgetPanel.add(txtBudget, BorderLayout.CENTER);
        budgetPanel.add(cbFreeOnly, BorderLayout.EAST);
        formGrid.add(budgetPanel);

        // Row 8: Skills & Skill Level
        formGrid.add(createLabel("Skills & Skill Level:"));
        JPanel skillPanel = new JPanel(new BorderLayout(8, 0));
        skillPanel.setOpaque(false);
        txtSkills = createTextField("Java, Python, Public Speaking");
        cmbSkillLevel = new JComboBox<String>(new String[]{"Beginner", "Intermediate", "Advanced"});
        cmbSkillLevel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        skillPanel.add(txtSkills, BorderLayout.CENTER);
        skillPanel.add(cmbSkillLevel, BorderLayout.EAST);
        formGrid.add(skillPanel);

        // Row 9: Availability: Preferred Days & Time Slot
        formGrid.add(createLabel("Availability (Days & Slot): *"));
        JPanel availPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        availPanel.setOpaque(false);
        cmbPreferredDays = new JComboBox<String>(new String[]{"Both", "Weekdays", "Weekends"});
        cmbPreferredDays.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTimeSlot = new JComboBox<String>(new String[]{"All", "Morning", "Afternoon", "Evening"});
        cmbTimeSlot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        availPanel.add(cmbPreferredDays);
        availPanel.add(cmbTimeSlot);
        formGrid.add(availPanel);

        // Row 10: Career & Learning Goals
        formGrid.add(createLabel("Career & Learning Goal:"));
        cmbCareerGoals = new JComboBox<String>(new String[]{
            "Improve programming skills",
            "Prepare for placements",
            "Develop leadership",
            "Build a portfolio",
            "Learn AI",
            "Improve communication",
            "Explore new technologies"
        });
        cmbCareerGoals.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGrid.add(cmbCareerGoals);

        // Row 11: Preferred Format
        formGrid.add(createLabel("Event Format Preference:"));
        cmbPreferredFormat = new JComboBox<String>(new String[]{"Either", "In-person", "Online"});
        cmbPreferredFormat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formGrid.add(cmbPreferredFormat);

        // Row 12: Interests (Grid of Checkboxes)
        formGrid.add(createLabel("Interests (select 1+): *"));
        JPanel cbPanel = new JPanel(new GridLayout(2, 5, 4, 4));
        cbPanel.setOpaque(false);
        cbCoding = new JCheckBox("Coding", true);
        cbAI = new JCheckBox("AI", true);
        cbRobotics = new JCheckBox("Robotics");
        cbEntrepreneurship = new JCheckBox("Entrepreneurship");
        cbMusic = new JCheckBox("Music");
        cbDance = new JCheckBox("Dance");
        cbPhotography = new JCheckBox("Photography");
        cbSports = new JCheckBox("Sports");
        cbPublicSpeaking = new JCheckBox("Public Speaking");
        cbDesign = new JCheckBox("Design");

        cbPanel.add(cbCoding);
        cbPanel.add(cbAI);
        cbPanel.add(cbRobotics);
        cbPanel.add(cbEntrepreneurship);
        cbPanel.add(cbMusic);
        cbPanel.add(cbDance);
        cbPanel.add(cbPhotography);
        cbPanel.add(cbSports);
        cbPanel.add(cbPublicSpeaking);
        cbPanel.add(cbDesign);
        formGrid.add(cbPanel);

        // Row 13: Empty spacer
        formGrid.add(new JLabel(""));
        formGrid.add(new JLabel(""));

        card.add(formGrid, BorderLayout.CENTER);

        // 3. Action Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        buttonPanel.setOpaque(false);

        btnCreateAccount = new JButton("Create Student Account");
        btnCreateAccount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCreateAccount.setBackground(ACCENT_BLUE);
        btnCreateAccount.setForeground(Color.WHITE);
        btnCreateAccount.setFocusPainted(false);
        btnCreateAccount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreateAccount.setPreferredSize(new Dimension(0, 38));
        btnCreateAccount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleRegistration();
            }
        });
        buttonPanel.add(btnCreateAccount);

        btnBackToLogin = new JButton("Back to Login");
        btnBackToLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBackToLogin.setBackground(Color.WHITE);
        btnBackToLogin.setForeground(TEXT_NAVY);
        btnBackToLogin.setFocusPainted(false);
        btnBackToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBackToLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new Main().setVisible(true);
            }
        });
        buttonPanel.add(btnBackToLogin);

        card.add(buttonPanel, BorderLayout.SOUTH);

        outerPanel.add(card);
        JScrollPane scrollPane = new JScrollPane(outerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Validates form inputs.
     * @return true if valid, false otherwise.
     */
    public boolean validateFormInputs() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String studentId = txtStudentId.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();
        String budgetStr = txtBudget.getText().trim();

        // 1. Check required fields
        if (fullName.isEmpty() || email.isEmpty() || studentId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all required fields marked with an asterisk (*).",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 2. Validate Email Format
        if (!email.contains("@") || !email.contains(".") || email.indexOf('@') > email.lastIndexOf('.')) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid student email address (e.g., student@campus.edu).",
                "Invalid Email",
                JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }

        // 3. Validate Password Length
        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this,
                "Password must be at least 4 characters long.",
                "Weak Password",
                JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return false;
        }

        // 4. Validate Password Match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match. Please verify your password entry.",
                "Password Mismatch",
                JOptionPane.ERROR_MESSAGE);
            txtConfirmPassword.setText("");
            txtConfirmPassword.requestFocus();
            return false;
        }

        // 5. Validate Budget Format
        try {
            double budget = Double.parseDouble(budgetStr);
            if (budget < 0) {
                JOptionPane.showMessageDialog(this,
                    "Maximum event budget cannot be negative.",
                    "Invalid Budget",
                    JOptionPane.ERROR_MESSAGE);
                txtBudget.requestFocus();
                return false;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid numeric budget amount (e.g., 200.0).",
                "Invalid Budget Format",
                JOptionPane.ERROR_MESSAGE);
            txtBudget.requestFocus();
            return false;
        }

        // 6. Check at least one interest selected
        if (getSelectedInterests().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one preferred category interest.",
                "Interest Required",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Triggered when Create Account button is clicked.
     * Performs form validation, duplicate checking, and database transaction.
     */
    private void handleRegistration() {
        if (!validateFormInputs()) {
            return;
        }

        String fullName = getFullName();
        String email = getEmail();
        String studentId = getStudentId();
        String password = getEnteredPassword();
        String dept = getDepartment();
        int year = getAcademicYear();
        double budget = getBudget();
        String slot = getTimeSlot();
        String skills = getSkills();
        String skillLevel = getSkillLevel();
        String preferredDays = getPreferredDays();
        String careerGoals = getCareerGoals();
        String preferredFormat = getPreferredFormat();
        boolean freeOnly = cbFreeOnly.isSelected();
        String interests = getSelectedInterests();

        Student student = new Student(0, fullName, email, dept, year, budget, interests,
                                      skills, skillLevel, slot, preferredDays, careerGoals, preferredFormat, freeOnly);
        UserDAO userDAO = new UserDAO();

        // 1. Check if student ID / username already taken
        if (userDAO.isUsernameTaken(studentId)) {
            JOptionPane.showMessageDialog(this,
                "An account with Student ID / Registration No '" + studentId + "' already exists.\nPlease enter a unique Student ID or log in.",
                "Duplicate Student ID",
                JOptionPane.WARNING_MESSAGE);
            txtStudentId.requestFocus();
            return;
        }

        // 2. Check if email already registered
        if (userDAO.isEmailTaken(email)) {
            JOptionPane.showMessageDialog(this,
                "An account with Email '" + email + "' is already registered.\nPlease use a different email or log in.",
                "Duplicate Email",
                JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        // 3. Register student into MySQL database (Transaction across users & students tables)
        boolean success = userDAO.registerStudent(student, studentId, password);

        if (success) {
            JOptionPane.showMessageDialog(this,
                "Student account successfully created for " + fullName + "!\n\n"
                + "Student ID    : " + studentId + "\n"
                + "Email         : " + email + "\n"
                + "Department    : " + dept + " (Year " + year + ")\n"
                + "Career Goal   : " + careerGoals + "\n\n"
                + "You can now log in using your Student ID or Email.",
                "Registration Successful",
                JOptionPane.INFORMATION_MESSAGE);

            dispose(); // Close registration frame
            new Main().setVisible(true); // Return to login page
        } else {
            JOptionPane.showMessageDialog(this,
                "Unable to complete registration.\nPlease ensure MySQL database is running and try again.",
                "Database Registration Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Getters for form data
    public String getFullName() { return txtFullName.getText().trim(); }
    public String getEmail() { return txtEmail.getText().trim(); }
    public String getStudentId() { return txtStudentId.getText().trim(); }
    public String getEnteredPassword() { return new String(txtPassword.getPassword()).trim(); }
    public String getDepartment() { return (String) cmbDept.getSelectedItem(); }
    public int getAcademicYear() { return (Integer) cmbYear.getSelectedItem(); }
    public double getBudget() { return Double.parseDouble(txtBudget.getText().trim()); }
    public String getTimeSlot() { return (String) cmbTimeSlot.getSelectedItem(); }
    public String getPreferredDays() { return (String) cmbPreferredDays.getSelectedItem(); }
    public String getSkills() { return txtSkills.getText().trim(); }
    public String getSkillLevel() { return (String) cmbSkillLevel.getSelectedItem(); }
    public String getCareerGoals() { return (String) cmbCareerGoals.getSelectedItem(); }
    public String getPreferredFormat() { return (String) cmbPreferredFormat.getSelectedItem(); }
    public boolean isFreeOnly() { return cbFreeOnly.isSelected(); }

    public String getSelectedInterests() {
        String interests = "";
        if (cbCoding.isSelected()) interests += "Coding, ";
        if (cbAI.isSelected()) interests += "AI, ";
        if (cbRobotics.isSelected()) interests += "Robotics, ";
        if (cbEntrepreneurship.isSelected()) interests += "Entrepreneurship, ";
        if (cbMusic.isSelected()) interests += "Music, ";
        if (cbDance.isSelected()) interests += "Dance, ";
        if (cbPhotography.isSelected()) interests += "Photography, ";
        if (cbSports.isSelected()) interests += "Sports, ";
        if (cbPublicSpeaking.isSelected()) interests += "Public Speaking, ";
        if (cbDesign.isSelected()) interests += "Design, ";
        if (interests.endsWith(", ")) interests = interests.substring(0, interests.length() - 2);
        return interests;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_NAVY);
        return lbl;
    }

    private JTextField createTextField(String text) {
        JTextField tf = new JTextField(text, 15);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return tf;
    }
}
