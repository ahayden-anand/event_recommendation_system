import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * StudentDashboard.java - Modern SaaS Desktop Redesign
 * Professional, clean SaaS-style desktop UI with white cards, navy typography,
 * single blue accent color, refined table styles, and comfortable padding.
 */
public class StudentDashboard extends JFrame {

    // Authenticated User Session
    private User currentUser;

    // DAOs and In-Memory Fallback
    private EventDAO eventDAO;
    private RegistrationDAO registrationDAO;
    private StudentDAO studentDAO;
    private EventManager fallbackEventManager;
    private boolean isDatabaseConnected = false;

    // Recommendation Engine & Data Lists
    private RecommendationEngine recommendationEngine;
    private ArrayList<EventRecommendation> currentRecommendations;
    private ArrayList<Registration> fallbackRegistrations;

    // Form Controls
    private JTextField txtName;
    private JTextField txtEmail;
    private JComboBox<String> cmbDept;
    private JComboBox<Integer> cmbYear;
    private JTextField txtBudget;
    private JTextField txtSkills;
    private JComboBox<String> cmbTimeSlot;

    // Checkboxes
    private JCheckBox cbCoding;
    private JCheckBox cbAI;
    private JCheckBox cbRobotics;
    private JCheckBox cbSports;
    private JCheckBox cbArts;
    private JCheckBox cbEntrepreneurship;

    // Action Buttons
    private JButton btnGetRecommendations;
    private JButton btnRegister;
    private JButton btnRefreshAllEvents;
    private JButton btnLogout;

    // JTables & TableModels
    private DefaultTableModel recTableModel;
    private JTable recTable;

    private DefaultTableModel allEventsTableModel;
    private JTable allEventsTable;

    private DefaultTableModel myRegTableModel;
    private JTable myRegTable;

    private JLabel lblStatus;
    private JLabel lblDbStatus;

    // SaaS Color Palette Constants
    private static final Color BG_CANVAS = new Color(248, 250, 252);     // Light grey background
    private static final Color BG_CARD = Color.WHITE;                     // Pure white card
    private static final Color BORDER_CARD = new Color(226, 232, 240);    // Subtle slate border
    private static final Color TEXT_NAVY = new Color(30, 41, 59);         // Slate navy text
    private static final Color TEXT_MUTED = new Color(100, 116, 139);     // Muted slate text
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);      // Single restrained SaaS blue
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);     // Success green
    private static final Color ACCENT_RED = new Color(220, 38, 38);       // Logout red

    public StudentDashboard(EventDAO eDao, RegistrationDAO rDao, StudentDAO sDao, User user) {
        this.eventDAO = eDao;
        this.registrationDAO = rDao;
        this.studentDAO = sDao;
        this.currentUser = (user != null) ? user : new User(1, "student", "", "STUDENT", "Rahul Sharma", "rahul@campus.edu");
        this.recommendationEngine = new RecommendationEngine();
        this.currentRecommendations = new ArrayList<EventRecommendation>();
        this.fallbackRegistrations = new ArrayList<Registration>();

        checkDatabaseConnection();

        setTitle("Campus Event Recommendation System - Student Portal (" + currentUser.getFullName() + ")");
        setSize(1080, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_CANVAS);

        initComponents();
        loadStudentProfileFromDatabase();
        loadAllEvents();
    }

    public StudentDashboard(EventDAO eDao, RegistrationDAO rDao, StudentDAO sDao) {
        this(eDao, rDao, sDao, null);
    }

    public StudentDashboard(EventManager manager) {
        this(new EventDAO(), new RegistrationDAO(), new StudentDAO(), null);
        this.fallbackEventManager = manager;
    }

    private void checkDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            isDatabaseConnected = true;
            eventDAO.createTable();
            registrationDAO.createTable();
            studentDAO.createTable();

            if (eventDAO.getAllEvents().isEmpty()) {
                EventManager seed = new EventManager();
                seed.loadSampleEvents();
                for (Event ev : seed.getAllEvents()) {
                    eventDAO.addEvent(ev);
                }
            }
        } catch (Exception e) {
            isDatabaseConnected = false;
            if (fallbackEventManager == null) {
                fallbackEventManager = new EventManager();
                fallbackEventManager.loadSampleEvents();
            }
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // 1. SaaS Navigation Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Modern Deep Slate Navy
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        // Left Branding
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        brandPanel.setOpaque(false);
        JLabel lblApp = new JLabel("Campus Event Recommendation System");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblApp.setForeground(Color.WHITE);
        brandPanel.add(lblApp);

        JLabel lblUserBadge = new JLabel("Student Portal  •  " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        lblUserBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserBadge.setForeground(new Color(148, 163, 184));
        brandPanel.add(lblUserBadge);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Right Nav: Connection indicator & Logout
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightNav.setOpaque(false);

        lblDbStatus = new JLabel(isDatabaseConnected ? "● MySQL Online" : "● Standalone Mode");
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDbStatus.setForeground(isDatabaseConnected ? new Color(74, 222, 128) : new Color(251, 146, 60));
        rightNav.add(lblDbStatus);

        btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(ACCENT_RED);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { performLogout(); }
        });
        rightNav.add(btnLogout);

        headerPanel.add(rightNav, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Content Tabs (SaaS Tabbed Navigation)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(BG_CANVAS);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        tabbedPane.addTab("Recommended Events", buildRecommendationsTab());
        tabbedPane.addTab("Find & Browse All Events", buildAllEventsTab());
        tabbedPane.addTab("My Registrations", buildMyRegistrationsTab());

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Status Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD));
        lblStatus = new JLabel("Ready. Fill your profile criteria and click 'Get Personalized Recommendations'.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_MUTED);
        statusBar.add(lblStatus);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel buildRecommendationsTab() {
        JPanel container = new JPanel(new BorderLayout(14, 14));
        container.setBackground(BG_CANVAS);
        container.setBorder(BorderFactory.createEmptyBorder(12, 4, 12, 4));

        // Top Card: Student Profile Input Form
        JPanel profileCard = createCardPanel();
        profileCard.setLayout(new BorderLayout(10, 10));

        JLabel lblFormTitle = new JLabel("Student Profile & Academic Criteria");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFormTitle.setForeground(TEXT_NAVY);
        profileCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(5, 4, 10, 8));
        grid.setOpaque(false);

        grid.add(createFormLabel("Full Name:"));
        txtName = createTextField(currentUser.getFullName());
        grid.add(txtName);

        grid.add(createFormLabel("Email Address:"));
        txtEmail = createTextField(currentUser.getEmail());
        grid.add(txtEmail);

        grid.add(createFormLabel("Department:"));
        cmbDept = new JComboBox<String>(new String[]{"CSE", "IT", "ECE", "MECH", "CIVIL"});
        cmbDept.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        grid.add(cmbDept);

        grid.add(createFormLabel("Academic Year:"));
        cmbYear = new JComboBox<Integer>(new Integer[]{1, 2, 3, 4});
        cmbYear.setSelectedItem(3);
        cmbYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        grid.add(cmbYear);

        grid.add(createFormLabel("Max Budget (Rs.):"));
        txtBudget = createTextField("200.0");
        grid.add(txtBudget);

        grid.add(createFormLabel("Available Time Slot:"));
        cmbTimeSlot = new JComboBox<String>(new String[]{"Morning", "Afternoon", "Evening", "All"});
        cmbTimeSlot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        grid.add(cmbTimeSlot);

        grid.add(createFormLabel("Your Skills:"));
        txtSkills = createTextField("Java, Python, Problem Solving");
        grid.add(txtSkills);

        grid.add(createFormLabel("Preferred Category:"));
        JPanel cbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cbPanel.setOpaque(false);
        cbCoding = new JCheckBox("Coding", true);
        cbAI = new JCheckBox("AI", true);
        cbRobotics = new JCheckBox("Robotics");
        cbSports = new JCheckBox("Sports");
        cbArts = new JCheckBox("Arts");
        cbEntrepreneurship = new JCheckBox("Entrepreneurship");
        cbPanel.add(cbCoding);
        cbPanel.add(cbAI);
        cbPanel.add(cbRobotics);
        cbPanel.add(cbSports);
        cbPanel.add(cbArts);
        cbPanel.add(cbEntrepreneurship);
        grid.add(cbPanel);

        grid.add(new JLabel(""));
        btnGetRecommendations = new JButton("Get Personalized Recommendations");
        btnGetRecommendations.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGetRecommendations.setBackground(ACCENT_BLUE);
        btnGetRecommendations.setForeground(Color.WHITE);
        btnGetRecommendations.setFocusPainted(false);
        btnGetRecommendations.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGetRecommendations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { generateRecommendations(); }
        });
        grid.add(btnGetRecommendations);
        grid.add(new JLabel(""));
        grid.add(new JLabel(""));

        profileCard.add(grid, BorderLayout.CENTER);
        container.add(profileCard, BorderLayout.NORTH);

        // Center Card: Recommendations Table
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel lblTableTitle = new JLabel("Top Recommended Events (Sorted by 100-Point Suitability Score)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(TEXT_NAVY);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Rank", "ID", "Event Title", "Category", "Date & Slot", "Fee (Rs.)", "Venue", "Score (/100)", "Why Recommended?"};
        recTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recTable = new JTable(recTableModel);
        styleTable(recTable);
        recTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        recTable.getColumnModel().getColumn(1).setPreferredWidth(35);
        recTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        recTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        recTable.getColumnModel().getColumn(8).setPreferredWidth(280);

        tableCard.add(new JScrollPane(recTable), BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bottomBar.setOpaque(false);
        btnRegister = new JButton("Register for Selected Event");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(ACCENT_GREEN);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { registerSelectedEvent(); }
        });
        bottomBar.add(btnRegister);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        container.add(tableCard, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildAllEventsTab() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setBackground(BG_CANVAS);
        container.setBorder(BorderFactory.createEmptyBorder(12, 4, 12, 4));

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("All Available Campus Events");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_NAVY);
        card.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Event ID", "Title", "Category", "Date", "Slot", "Fee (Rs.)", "Venue", "Required Skills", "Eligible Dept", "Eligible Year"};
        allEventsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allEventsTable = new JTable(allEventsTableModel);
        styleTable(allEventsTable);
        card.add(new JScrollPane(allEventsTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bottomPanel.setOpaque(false);
        btnRefreshAllEvents = new JButton("Refresh Events Catalog");
        btnRefreshAllEvents.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefreshAllEvents.setBackground(Color.WHITE);
        btnRefreshAllEvents.setForeground(TEXT_NAVY);
        btnRefreshAllEvents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadAllEvents();
                lblStatus.setText("Events catalog refreshed from database.");
            }
        });
        bottomPanel.add(btnRefreshAllEvents);
        card.add(bottomPanel, BorderLayout.SOUTH);

        container.add(card, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildMyRegistrationsTab() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setBackground(BG_CANVAS);
        container.setBorder(BorderFactory.createEmptyBorder(12, 4, 12, 4));

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("My Confirmed Event Registrations");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(TEXT_NAVY);
        card.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Reg ID", "Student Name", "Student Email", "Event ID", "Event Title", "Registered Timestamp"};
        myRegTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        myRegTable = new JTable(myRegTableModel);
        styleTable(myRegTable);
        card.add(new JScrollPane(myRegTable), BorderLayout.CENTER);

        container.add(card, BorderLayout.CENTER);
        return container;
    }

    public void generateRecommendations() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String dept = (String) cmbDept.getSelectedItem();
        int year = (Integer) cmbYear.getSelectedItem();
        String budgetStr = txtBudget.getText().trim();
        String skills = txtSkills.getText().trim();
        String slot = (String) cmbTimeSlot.getSelectedItem();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your Name and Email.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double budget = 0.0;
        try {
            budget = Double.parseDouble(budgetStr);
            if (budget < 0) {
                JOptionPane.showMessageDialog(this, "Budget cannot be negative.", "Invalid Budget", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric budget (e.g. 150.0).", "Invalid Budget Format", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String interests = "";
        if (cbCoding.isSelected()) interests += "Coding, ";
        if (cbAI.isSelected()) interests += "AI, ";
        if (cbRobotics.isSelected()) interests += "Robotics, ";
        if (cbSports.isSelected()) interests += "Sports, ";
        if (cbArts.isSelected()) interests += "Arts, ";
        if (cbEntrepreneurship.isSelected()) interests += "Entrepreneurship, ";
        if (interests.endsWith(", ")) interests = interests.substring(0, interests.length() - 2);

        Student student = new Student(0, name, email, dept, year, budget, interests, skills, slot);

        if (isDatabaseConnected) {
            studentDAO.saveOrUpdateStudent(student);
        }

        ArrayList<Event> eventCatalog = isDatabaseConnected ? eventDAO.getAllEvents() : fallbackEventManager.getAllEvents();
        currentRecommendations = recommendationEngine.recommendEvents(student, eventCatalog);

        recTableModel.setRowCount(0);
        for (int i = 0; i < currentRecommendations.size(); i++) {
            EventRecommendation rec = currentRecommendations.get(i);
            Event ev = rec.getEvent();
            recTableModel.addRow(new Object[]{
                "#" + (i + 1),
                ev.getEventId(),
                ev.getTitle(),
                ev.getCategory(),
                ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                ev.getFee(),
                ev.getVenue(),
                rec.getScore() + "/100",
                rec.getMatchReason()
            });
        }

        loadMyRegistrations(email);
        lblStatus.setText("Generated " + currentRecommendations.size() + " personalized recommendations for " + name);
    }

    private void registerSelectedEvent() {
        int selectedRow = recTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an event row from the recommendations table to register.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eventId = (Integer) recTableModel.getValueAt(selectedRow, 1);
        String eventTitle = (String) recTableModel.getValueAt(selectedRow, 2);
        String email = txtEmail.getText().trim();
        String name = txtName.getText().trim();

        ArrayList<Registration> currentRegs = isDatabaseConnected ? 
            registrationDAO.getRegistrationsByStudent(email) : fallbackRegistrations;

        for (int i = 0; i < currentRegs.size(); i++) {
            if (currentRegs.get(i).getEventId() == eventId) {
                JOptionPane.showMessageDialog(this, "You are already registered for \"" + eventTitle + "\"!", "Already Registered", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm registration for:\n\"" + eventTitle + "\"?",
            "Confirm Event Registration",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Registration reg = new Registration(0, 0, name, email, eventId, eventTitle, timeStamp);

            if (isDatabaseConnected) {
                registrationDAO.addRegistration(reg);
            } else {
                reg.setRegId(8000 + fallbackRegistrations.size() + 1);
                fallbackRegistrations.add(reg);
            }

            loadMyRegistrations(email);

            JOptionPane.showMessageDialog(this,
                "Registration Confirmed and Stored in Database!\nRegistration ID: #" + reg.getRegId() + "\nEvent: " + eventTitle,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            lblStatus.setText("Registered for " + eventTitle + " (Registration ID: #" + reg.getRegId() + ")");
        }
    }

    public void loadAllEvents() {
        allEventsTableModel.setRowCount(0);
        ArrayList<Event> list = isDatabaseConnected ? eventDAO.getAllEvents() : fallbackEventManager.getAllEvents();
        for (int i = 0; i < list.size(); i++) {
            Event e = list.get(i);
            allEventsTableModel.addRow(new Object[]{
                e.getEventId(),
                e.getTitle(),
                e.getCategory(),
                e.getEventDate(),
                e.getTimeSlot(),
                e.getFee(),
                e.getVenue(),
                e.getRequiredSkills(),
                e.getEligibleDept(),
                e.getEligibleYear() == 0 ? "All" : "Year " + e.getEligibleYear()
            });
        }
    }

    private void loadMyRegistrations(String email) {
        myRegTableModel.setRowCount(0);
        ArrayList<Registration> list = isDatabaseConnected ? 
            registrationDAO.getRegistrationsByStudent(email) : fallbackRegistrations;

        for (int i = 0; i < list.size(); i++) {
            Registration r = list.get(i);
            myRegTableModel.addRow(new Object[]{
                r.getRegId(),
                r.getStudentName(),
                r.getStudentEmail(),
                r.getEventId(),
                r.getEventTitle(),
                r.getRegistrationDate()
            });
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out of the Student Portal?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new Main().setVisible(true);
        }
    }

    // --- SaaS UI Helper Methods ---
    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        return card;
    }

    private JLabel createFormLabel(String text) {
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

    private void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(BORDER_CARD);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_NAVY);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_NAVY);
        header.setPreferredSize(new Dimension(0, 30));
    }

    /**
     * Loads the student's saved academic profile from the MySQL students table
     * and populates the dashboard criteria inputs and interest checkboxes.
     */
    private void loadStudentProfileFromDatabase() {
        if (studentDAO == null || currentUser == null || currentUser.getEmail() == null) {
            return;
        }

        Student profile = studentDAO.getStudentByEmail(currentUser.getEmail());
        if (profile != null) {
            if (profile.getName() != null && !profile.getName().trim().isEmpty()) {
                txtName.setText(profile.getName());
            }
            if (profile.getDepartment() != null) {
                cmbDept.setSelectedItem(profile.getDepartment());
            }
            if (profile.getAcademicYear() > 0) {
                cmbYear.setSelectedItem(profile.getAcademicYear());
            }
            txtBudget.setText(String.valueOf(profile.getMaxBudget()));
            if (profile.getAvailableSlot() != null) {
                cmbTimeSlot.setSelectedItem(profile.getAvailableSlot());
            }
            if (profile.getSkills() != null) {
                txtSkills.setText(profile.getSkills());
            }

            // Sync interest checkboxes
            String interests = profile.getInterests();
            if (interests != null) {
                cbCoding.setSelected(interests.contains("Coding"));
                cbAI.setSelected(interests.contains("AI"));
                cbRobotics.setSelected(interests.contains("Robotics"));
                cbSports.setSelected(interests.contains("Sports"));
                cbArts.setSelected(interests.contains("Arts"));
                cbEntrepreneurship.setSelected(interests.contains("Entrepreneurship"));
            }
        }
    }
}
