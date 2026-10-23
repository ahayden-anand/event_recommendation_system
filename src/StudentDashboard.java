import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * StudentDashboard.java - Complete Campus SaaS Redesign
 * Professional, clean SaaS-style desktop UI with dark navy sidebar navigation,
 * crisp white cards, refined table styling, and complete feature coverage:
 * 
 * Tabs:
 * 1. Recommended Events (100-pt scoring breakdown, "Why This Event?" rationale, Register)
 * 2. Budget-Friendly Finder (Free events only toggle, budget filter, fee sorting)
 * 3. Skill Growth Pathways (Beginner -> Intermediate -> Advanced career roadmaps)
 * 4. Discover Something New (Serendipity recommendations matching career goals)
 * 5. Browse All Events (Full catalog inspection)
 * 6. My Registrations (With Schedule Clash Detector & Cancellation)
 * 7. My Preferences (Update 6-aspect profile live)
 */
public class StudentDashboard extends JFrame {

    private User currentUser;
    private Student currentStudent;

    private EventDAO eventDAO;
    private RegistrationDAO registrationDAO;
    private StudentDAO studentDAO;
    private RecommendationEngine recommendationEngine;

    // Sidebar navigation buttons
    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    // Tab 1: Recommended Events
    private DefaultTableModel recTableModel;
    private JTable recTable;
    private JTextArea txtRecExplanation;
    private ArrayList<EventRecommendation> currentRecommendations;

    // Tab 2: Budget Finder
    private DefaultTableModel budgetTableModel;
    private JTable budgetTable;
    private JTextField txtMaxBudgetFilter;
    private JCheckBox cbFreeOnlyFilter;

    // Tab 3: Skill Growth
    private JTextArea txtSkillPathways;

    // Tab 4: Discover Something New
    private DefaultTableModel discoverTableModel;
    private JTable discoverTable;
    private JTextArea txtDiscoverExplanation;

    // Tab 5: Browse All Events
    private DefaultTableModel allEventsTableModel;
    private JTable allEventsTable;

    // Tab 6: My Registrations
    private DefaultTableModel myRegTableModel;
    private JTable myRegTable;

    // Tab 7: Preferences Form Fields
    private JTextField prefName;
    private JComboBox<String> prefDept;
    private JComboBox<Integer> prefYear;
    private JTextField prefBudget;
    private JCheckBox prefFreeOnly;
    private JTextField prefSkills;
    private JComboBox<String> prefSkillLevel;
    private JComboBox<String> prefSlot;
    private JComboBox<String> prefDays;
    private JComboBox<String> prefGoal;
    private JComboBox<String> prefFormat;
    private JCheckBox cbCoding, cbAI, cbRobotics, cbEntrepreneurship, cbMusic, cbDance, cbPhotography, cbSports, cbPublicSpeaking, cbDesign;

    // Visual Palette
    private static final Color BG_CANVAS = new Color(245, 247, 250);
    private static final Color SIDEBAR_BG = new Color(26, 37, 48);
    private static final Color SIDEBAR_ACTIVE = new Color(41, 128, 185);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(220, 224, 230);
    private static final Color TEXT_NAVY = new Color(26, 37, 48);
    private static final Color TEXT_MUTED = new Color(110, 120, 135);
    private static final Color ACCENT_BLUE = new Color(41, 128, 185);

    public StudentDashboard(User user) {
        this.currentUser = user;
        this.eventDAO = new EventDAO();
        this.registrationDAO = new RegistrationDAO();
        this.studentDAO = new StudentDAO();
        this.recommendationEngine = new RecommendationEngine();

        // Load or create student profile
        loadStudentProfile();

        setTitle("Campus Event Recommendation System - Student Portal (" + currentUser.getFullName() + ")");
        setSize(1180, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        refreshAllData();
    }

    private void loadStudentProfile() {
        if (currentUser != null && currentUser.getEmail() != null) {
            this.currentStudent = studentDAO.getStudentByEmail(currentUser.getEmail());
        }
        if (this.currentStudent == null) {
            this.currentStudent = new Student(
                0, currentUser.getFullName(), currentUser.getEmail(), "CSE", 2, 200.0,
                "Coding, AI", "Java, Python", "Beginner", "Morning", "Both",
                "Improve programming skills", "Either", false
            );
        }
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_CANVAS);

        // 1. Sidebar Navigation (Left)
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Sidebar Header
        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 20, 20));

        JLabel lblBrand = new JLabel("Campus Events");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel(currentUser.getFullName() + " (" + currentStudent.getDepartment() + " Y" + currentStudent.getAcademicYear() + ")");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUser.setForeground(new Color(180, 190, 205));

        brandPanel.add(lblBrand);
        brandPanel.add(lblUser);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Sidebar Navigation Links
        JPanel navPanel = new JPanel(new GridLayout(7, 1, 0, 4));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnNavRec = createNavButton("Recommended Events");
        JButton btnNavBudget = createNavButton("Budget Finder");
        JButton btnNavSkill = createNavButton("Skill Growth");
        JButton btnNavDiscover = createNavButton("Discover New");
        JButton btnNavBrowse = createNavButton("Browse Events");
        JButton btnNavMyReg = createNavButton("My Registrations");
        JButton btnNavPref = createNavButton("My Preferences");

        navPanel.add(btnNavRec);
        navPanel.add(btnNavBudget);
        navPanel.add(btnNavSkill);
        navPanel.add(btnNavDiscover);
        navPanel.add(btnNavBrowse);
        navPanel.add(btnNavMyReg);
        navPanel.add(btnNavPref);

        sidebar.add(navPanel, BorderLayout.CENTER);

        // Sidebar Footer (Logout)
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new Main().setVisible(true);
            }
        });
        footerPanel.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // 2. Main Content Panels (CardLayout)
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_CANVAS);

        mainContentPanel.add(createRecommendationsPanel(), "REC");
        mainContentPanel.add(createBudgetFinderPanel(), "BUDGET");
        mainContentPanel.add(createSkillGrowthPanel(), "SKILL");
        mainContentPanel.add(createDiscoverPanel(), "DISCOVER");
        mainContentPanel.add(createBrowseEventsPanel(), "BROWSE");
        mainContentPanel.add(createMyRegistrationsPanel(), "MYREG");
        mainContentPanel.add(createPreferencesPanel(), "PREF");

        root.add(mainContentPanel, BorderLayout.CENTER);

        // Navigation Action Listeners
        btnNavRec.addActionListener(e -> showTab("REC"));
        btnNavBudget.addActionListener(e -> showTab("BUDGET"));
        btnNavSkill.addActionListener(e -> showTab("SKILL"));
        btnNavDiscover.addActionListener(e -> showTab("DISCOVER"));
        btnNavBrowse.addActionListener(e -> showTab("BROWSE"));
        btnNavMyReg.addActionListener(e -> showTab("MYREG"));
        btnNavPref.addActionListener(e -> showTab("PREF"));

        add(root);
    }

    private void showTab(String key) {
        cardLayout.show(mainContentPanel, key);
        refreshAllData();
    }

    private JButton createNavButton(String title) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(230, 235, 245));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- TAB 1: RECOMMENDED EVENTS ---
    private JPanel createRecommendationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = createHeaderPanel("Recommended For You", "100-Point Rule-Based Match Scoring tailored to your preferences.");
        JButton btnRegAction = new JButton("Register Selected Event");
        stylePrimaryButton(btnRegAction);
        btnRegAction.addActionListener(e -> handleRegisterSelectedRecommendation());
        header.add(btnRegAction, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Score", "Event Title", "Category", "Date & Slot", "Fee (Rs)", "Format", "Score Breakdown"};
        recTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recTable = createStyledTable(recTableModel);

        JScrollPane scrollTable = new JScrollPane(recTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        // Rationale card at bottom
        JPanel rationaleCard = new JPanel(new BorderLayout(8, 8));
        rationaleCard.setBackground(CARD_BG);
        rationaleCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        JLabel lblRat = new JLabel("Why This Event? (Selected Match Rationale):");
        lblRat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRat.setForeground(TEXT_NAVY);

        txtRecExplanation = new JTextArea("Select any recommended event from the table above to view its transparent matching explanation.", 3, 40);
        txtRecExplanation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtRecExplanation.setForeground(TEXT_NAVY);
        txtRecExplanation.setLineWrap(true);
        txtRecExplanation.setWrapStyleWord(true);
        txtRecExplanation.setEditable(false);
        txtRecExplanation.setBackground(CARD_BG);

        rationaleCard.add(lblRat, BorderLayout.NORTH);
        rationaleCard.add(txtRecExplanation, BorderLayout.CENTER);

        recTable.getSelectionModel().addListSelectionListener(e -> {
            int row = recTable.getSelectedRow();
            if (row >= 0 && currentRecommendations != null && row < currentRecommendations.size()) {
                EventRecommendation rec = currentRecommendations.get(row);
                txtRecExplanation.setText(rec.getMatchReason() + "\n\nBreakdown: " + rec.getBreakdownText());
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, rationaleCard);
        splitPane.setResizeWeight(0.75);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 2: BUDGET-FRIENDLY EVENT FINDER ---
    private JPanel createBudgetFinderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Budget-Friendly Event Finder", "Filter campus events strictly within your financial affordability.");

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterControls.setOpaque(false);

        JLabel lblMax = new JLabel("Max Budget (Rs):");
        lblMax.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtMaxBudgetFilter = new JTextField(String.valueOf(currentStudent.getMaxBudget()), 6);
        cbFreeOnlyFilter = new JCheckBox("Free Only", currentStudent.isFreeOnly());
        cbFreeOnlyFilter.setOpaque(false);

        JButton btnApplyBudget = new JButton("Filter & Sort by Fee");
        stylePrimaryButton(btnApplyBudget);
        btnApplyBudget.addActionListener(e -> refreshBudgetEvents());

        filterControls.add(lblMax);
        filterControls.add(txtMaxBudgetFilter);
        filterControls.add(cbFreeOnlyFilter);
        filterControls.add(btnApplyBudget);

        header.add(filterControls, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Event Title", "Fee (Rs)", "Category", "Date & Slot", "Venue", "Affordability Status"};
        budgetTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        budgetTable = createStyledTable(budgetTableModel);

        panel.add(new JScrollPane(budgetTable), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 3: SKILL GROWTH PATHWAYS ---
    private JPanel createSkillGrowthPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Skill Growth Pathways", "Step-by-step learning progression aligned with your career goals.");
        panel.add(header, BorderLayout.NORTH);

        txtSkillPathways = new JTextArea();
        txtSkillPathways.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtSkillPathways.setEditable(false);
        txtSkillPathways.setLineWrap(true);
        txtSkillPathways.setWrapStyleWord(true);
        txtSkillPathways.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        panel.add(new JScrollPane(txtSkillPathways), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 4: DISCOVER SOMETHING NEW ---
    private JPanel createDiscoverPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Discover Something New", "Goal-aligned events outside your usual primary interests.");
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Event Title", "Category", "Date & Slot", "Fee", "Venue", "Exploration Reason"};
        discoverTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        discoverTable = createStyledTable(discoverTableModel);

        txtDiscoverExplanation = new JTextArea("Select an exploration event to read why it will broaden your horizons.", 3, 40);
        txtDiscoverExplanation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiscoverExplanation.setEditable(false);
        txtDiscoverExplanation.setLineWrap(true);
        txtDiscoverExplanation.setWrapStyleWord(true);
        txtDiscoverExplanation.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        discoverTable.getSelectionModel().addListSelectionListener(e -> {
            int row = discoverTable.getSelectedRow();
            if (row >= 0 && row < discoverTableModel.getRowCount()) {
                txtDiscoverExplanation.setText((String) discoverTableModel.getValueAt(row, 5));
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(discoverTable), txtDiscoverExplanation);
        split.setResizeWeight(0.8);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 5: BROWSE ALL EVENTS ---
    private JPanel createBrowseEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Browse Campus Events", "Full catalog of available activities across all departments.");
        JButton btnRegisterCatalog = new JButton("Register for Event");
        stylePrimaryButton(btnRegisterCatalog);
        btnRegisterCatalog.addActionListener(e -> handleRegisterFromCatalog());
        header.add(btnRegisterCatalog, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Event Title", "Category", "Date & Slot", "Fee (Rs)", "Format", "Skills Needed", "Capacity"};
        allEventsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allEventsTable = createStyledTable(allEventsTableModel);

        panel.add(new JScrollPane(allEventsTable), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 6: MY REGISTRATIONS ---
    private JPanel createMyRegistrationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("My Registered Events", "Events you are enrolled in. Schedule clashes are strictly prevented.");
        JButton btnCancelReg = new JButton("Cancel Registration");
        btnCancelReg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelReg.setBackground(new Color(192, 57, 43));
        btnCancelReg.setForeground(Color.WHITE);
        btnCancelReg.setFocusPainted(false);
        btnCancelReg.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelReg.addActionListener(e -> handleCancelRegistration());
        header.add(btnCancelReg, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Reg ID", "Event ID", "Event Title", "Registered At"};
        myRegTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        myRegTable = createStyledTable(myRegTableModel);

        panel.add(new JScrollPane(myRegTable), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 7: PREFERENCES PROFILE ---
    private JPanel createPreferencesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Configure Preference Profile", "Update your six personal and academic criteria at any time.");
        JButton btnSavePref = new JButton("Save Profile Preferences");
        stylePrimaryButton(btnSavePref);
        btnSavePref.addActionListener(e -> handleSavePreferences());
        header.add(btnSavePref, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridLayout(10, 2, 10, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // 1. Name & Dept
        card.add(new JLabel("Full Name:"));
        prefName = new JTextField(currentStudent.getName());
        card.add(prefName);

        card.add(new JLabel("Department:"));
        prefDept = new JComboBox<String>(new String[]{"CSE", "IT", "ECE", "MECH", "CIVIL"});
        prefDept.setSelectedItem(currentStudent.getDepartment());
        card.add(prefDept);

        // 2. Year & Budget
        card.add(new JLabel("Academic Year:"));
        prefYear = new JComboBox<Integer>(new Integer[]{1, 2, 3, 4});
        prefYear.setSelectedItem(currentStudent.getAcademicYear());
        card.add(prefYear);

        card.add(new JLabel("Max Budget (Rs) & Free Only:"));
        JPanel bPanel = new JPanel(new BorderLayout(6, 0));
        bPanel.setOpaque(false);
        prefBudget = new JTextField(String.valueOf(currentStudent.getMaxBudget()));
        prefFreeOnly = new JCheckBox("Free Only", currentStudent.isFreeOnly());
        prefFreeOnly.setOpaque(false);
        bPanel.add(prefBudget, BorderLayout.CENTER);
        bPanel.add(prefFreeOnly, BorderLayout.EAST);
        card.add(bPanel);

        // 3. Skills & Level
        card.add(new JLabel("Skills:"));
        prefSkills = new JTextField(currentStudent.getSkills());
        card.add(prefSkills);

        card.add(new JLabel("Skill Level:"));
        prefSkillLevel = new JComboBox<String>(new String[]{"Beginner", "Intermediate", "Advanced"});
        prefSkillLevel.setSelectedItem(currentStudent.getSkillLevel());
        card.add(prefSkillLevel);

        // 4. Availability & Preferred Days
        card.add(new JLabel("Time Slot:"));
        prefSlot = new JComboBox<String>(new String[]{"All", "Morning", "Afternoon", "Evening"});
        prefSlot.setSelectedItem(currentStudent.getAvailableSlot());
        card.add(prefSlot);

        card.add(new JLabel("Preferred Days:"));
        prefDays = new JComboBox<String>(new String[]{"Both", "Weekdays", "Weekends"});
        prefDays.setSelectedItem(currentStudent.getPreferredDays());
        card.add(prefDays);

        // 5. Goals & Format
        card.add(new JLabel("Career Goal:"));
        prefGoal = new JComboBox<String>(new String[]{
            "Improve programming skills", "Prepare for placements", "Develop leadership",
            "Build a portfolio", "Learn AI", "Improve communication", "Explore new technologies"
        });
        prefGoal.setSelectedItem(currentStudent.getCareerGoals());
        card.add(prefGoal);

        card.add(new JLabel("Event Format:"));
        prefFormat = new JComboBox<String>(new String[]{"Either", "In-person", "Online"});
        prefFormat.setSelectedItem(currentStudent.getPreferredFormat());
        card.add(prefFormat);

        panel.add(new JScrollPane(card), BorderLayout.CENTER);
        return panel;
    }

    // --- REFRESH & DATA LOGIC ---
    private void refreshAllData() {
        loadStudentProfile();
        refreshRecommendations();
        refreshBudgetEvents();
        refreshSkillGrowth();
        refreshDiscoverNew();
        refreshAllEventsCatalog();
        refreshMyRegistrations();
    }

    private void refreshRecommendations() {
        recTableModel.setRowCount(0);
        ArrayList<Event> allEvents = eventDAO.getAllEvents();
        currentRecommendations = recommendationEngine.recommendEvents(currentStudent, allEvents);

        for (int i = 0; i < currentRecommendations.size(); i++) {
            EventRecommendation rec = currentRecommendations.get(i);
            Event ev = rec.getEvent();
            recTableModel.addRow(new Object[]{
                rec.getScore() + "/100",
                ev.getTitle(),
                ev.getCategory(),
                ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                ev.getFee() == 0 ? "FREE" : "Rs. " + ev.getFee(),
                ev.getEventFormat(),
                rec.getBreakdownText()
            });
        }
    }

    private void refreshBudgetEvents() {
        budgetTableModel.setRowCount(0);
        double maxBudget = currentStudent.getMaxBudget();
        try {
            if (txtMaxBudgetFilter != null) maxBudget = Double.parseDouble(txtMaxBudgetFilter.getText().trim());
        } catch (Exception ignored) {}

        boolean freeOnly = cbFreeOnlyFilter != null ? cbFreeOnlyFilter.isSelected() : currentStudent.isFreeOnly();

        ArrayList<Event> list = eventDAO.getAllEvents();
        // Bubble sort by fee ascending (O(n^2))
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = 0; j < list.size() - i - 1; j++) {
                if (list.get(j).getFee() > list.get(j + 1).getFee()) {
                    Event temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }

        for (int i = 0; i < list.size(); i++) {
            Event ev = list.get(i);
            boolean isAffordable = ev.getFee() <= maxBudget;
            if (freeOnly && ev.getFee() > 0) continue;

            String status = isAffordable ? (ev.getFee() == 0 ? "100% Free" : "Affordable") : "Over-Budget (Exceeds Rs. " + maxBudget + ")";
            budgetTableModel.addRow(new Object[]{
                ev.getTitle(),
                ev.getFee() == 0 ? "FREE" : "Rs. " + ev.getFee(),
                ev.getCategory(),
                ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                ev.getVenue(),
                status
            });
        }
    }

    private void refreshSkillGrowth() {
        ArrayList<Event> events = eventDAO.getAllEvents();
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                       YOUR TAILORED SKILL GROWTH LEARNING PATHWAY                      \n");
        sb.append("========================================================================================\n\n");
        sb.append("Student: ").append(currentStudent.getName()).append(" | Current Level: ").append(currentStudent.getSkillLevel()).append("\n");
        sb.append("Target Career Goal: ").append(currentStudent.getCareerGoals()).append("\n\n");

        ArrayList<Event> beginners = new ArrayList<Event>();
        ArrayList<Event> intermediates = new ArrayList<Event>();
        ArrayList<Event> advanced = new ArrayList<Event>();

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            String lvl = e.getTargetSkillLevel() != null ? e.getTargetSkillLevel().toLowerCase() : "all";
            if (lvl.contains("beginner")) beginners.add(e);
            else if (lvl.contains("intermediate")) intermediates.add(e);
            else if (lvl.contains("advanced")) advanced.add(e);
            else {
                // Open level
                beginners.add(e);
            }
        }

        sb.append("[STAGE 1: FOUNDATION - BEGINNER]\n");
        if (beginners.isEmpty()) sb.append("  * No beginner sessions currently listed in database.\n");
        for (int i = 0; i < beginners.size(); i++) {
            Event e = beginners.get(i);
            sb.append("  -> ").append(e.getTitle()).append(" (Fee: Rs. ").append(e.getFee()).append(", ").append(e.getEventDate()).append(")\n");
        }

        sb.append("\n[STAGE 2: APPLICATION - INTERMEDIATE]\n");
        if (intermediates.isEmpty()) sb.append("  * No intermediate sessions currently listed in database.\n");
        for (int i = 0; i < intermediates.size(); i++) {
            Event e = intermediates.get(i);
            sb.append("  -> ").append(e.getTitle()).append(" (Fee: Rs. ").append(e.getFee()).append(", ").append(e.getEventDate()).append(")\n");
        }

        sb.append("\n[STAGE 3: MASTERY & PORTFOLIO - ADVANCED]\n");
        if (advanced.isEmpty()) sb.append("  * No advanced competitive sessions currently listed in database.\n");
        for (int i = 0; i < advanced.size(); i++) {
            Event e = advanced.get(i);
            sb.append("  -> ").append(e.getTitle()).append(" (Fee: Rs. ").append(e.getFee()).append(", ").append(e.getEventDate()).append(")\n");
        }

        txtSkillPathways.setText(sb.toString());
    }

    private void refreshDiscoverNew() {
        discoverTableModel.setRowCount(0);
        ArrayList<Event> events = eventDAO.getAllEvents();
        String studentInterests = currentStudent.getInterests() != null ? currentStudent.getInterests().toLowerCase() : "";

        for (int i = 0; i < events.size(); i++) {
            Event e = events.get(i);
            // Must be within budget
            if (e.getFee() > currentStudent.getMaxBudget()) continue;
            if (currentStudent.isFreeOnly() && e.getFee() > 0) continue;

            // Outside usual primary interests
            if (!studentInterests.contains(e.getCategory().toLowerCase())) {
                String reason = "Suggested to broaden your horizon beyond your usual categories while supporting your career goal: " + currentStudent.getCareerGoals();
                discoverTableModel.addRow(new Object[]{
                    e.getTitle(),
                    e.getCategory(),
                    e.getEventDate() + " (" + e.getTimeSlot() + ")",
                    e.getFee() == 0 ? "FREE" : "Rs. " + e.getFee(),
                    e.getVenue(),
                    reason
                });
            }
        }
    }

    private void refreshAllEventsCatalog() {
        allEventsTableModel.setRowCount(0);
        ArrayList<Event> list = eventDAO.getAllEvents();
        for (int i = 0; i < list.size(); i++) {
            Event ev = list.get(i);
            int count = registrationDAO.getRegistrationCountForEvent(ev.getEventId());
            allEventsTableModel.addRow(new Object[]{
                ev.getEventId(),
                ev.getTitle(),
                ev.getCategory(),
                ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                ev.getFee() == 0 ? "FREE" : "Rs. " + ev.getFee(),
                ev.getEventFormat(),
                ev.getRequiredSkills(),
                count + " / " + ev.getMaxCapacity()
            });
        }
    }

    private void refreshMyRegistrations() {
        myRegTableModel.setRowCount(0);
        ArrayList<Registration> list = registrationDAO.getRegistrationsByStudent(currentUser.getEmail());
        for (int i = 0; i < list.size(); i++) {
            Registration r = list.get(i);
            myRegTableModel.addRow(new Object[]{
                r.getRegId(),
                r.getEventId(),
                r.getEventTitle(),
                r.getRegistrationDate()
            });
        }
    }

    // --- REGISTRATION & SCHEDULE CLASH HANDLERS ---
    private void handleRegisterSelectedRecommendation() {
        int row = recTable.getSelectedRow();
        if (row < 0 || currentRecommendations == null || row >= currentRecommendations.size()) {
            JOptionPane.showMessageDialog(this, "Please select an event from the recommendations table to register.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Event targetEvent = currentRecommendations.get(row).getEvent();
        executeRegistration(targetEvent);
    }

    private void handleRegisterFromCatalog() {
        int row = allEventsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the catalog to register.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int eventId = (Integer) allEventsTableModel.getValueAt(row, 0);
        Event targetEvent = eventDAO.getEventById(eventId);
        executeRegistration(targetEvent);
    }

    private void executeRegistration(Event targetEvent) {
        if (targetEvent == null) return;

        // 1. Check duplicate registration
        if (registrationDAO.isAlreadyRegistered(currentUser.getEmail(), targetEvent.getEventId())) {
            JOptionPane.showMessageDialog(this, "You are already registered for '" + targetEvent.getTitle() + "'!", "Already Registered", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. SCHEDULE CLASH DETECTOR
        String clashInfo = registrationDAO.checkScheduleClash(currentUser.getEmail(), targetEvent, eventDAO);
        if (clashInfo != null) {
            JOptionPane.showMessageDialog(this,
                "Schedule Conflict Detected!\n\n"
                + "You cannot register for '" + targetEvent.getTitle() + "' because it overlaps with:\n"
                + clashInfo + "\n\nPlease select another event or time slot.",
                "Schedule Clash Detected",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Check Capacity & Smart Waitlist
        int regCount = registrationDAO.getRegistrationCountForEvent(targetEvent.getEventId());
        if (regCount >= targetEvent.getMaxCapacity()) {
            int opt = JOptionPane.showConfirmDialog(this,
                "This event has reached full capacity (" + regCount + "/" + targetEvent.getMaxCapacity() + ").\n"
                + "Would you like to join the Smart Waitlist queue?",
                "Event Full - Join Waitlist?",
                JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                registrationDAO.addToWaitlist(currentStudent.getStudentId(), currentStudent.getName(), currentStudent.getEmail(), targetEvent.getEventId(), targetEvent.getTitle());
                JOptionPane.showMessageDialog(this, "You have been placed on the Waitlist queue!", "Waitlist Enrolled", JOptionPane.INFORMATION_MESSAGE);
            }
            return;
        }

        // 4. Enroll Student
        Registration reg = new Registration(
            0, currentStudent.getStudentId(), currentStudent.getName(), currentStudent.getEmail(),
            targetEvent.getEventId(), targetEvent.getTitle(), java.time.LocalDate.now().toString()
        );

        if (registrationDAO.addRegistration(reg)) {
            JOptionPane.showMessageDialog(this,
                "Successfully registered for '" + targetEvent.getTitle() + "'!\n\n"
                + "Venue: " + targetEvent.getVenue() + " (" + targetEvent.getEventFormat() + ")\n"
                + "Date : " + targetEvent.getEventDate() + " (" + targetEvent.getTimeSlot() + ")",
                "Registration Confirmed",
                JOptionPane.INFORMATION_MESSAGE);
            refreshAllData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to complete registration. Check database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancelRegistration() {
        int row = myRegTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a registration record to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int regId = (Integer) myRegTableModel.getValueAt(row, 0);
        String eventTitle = (String) myRegTableModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel registration for '" + eventTitle + "'?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (registrationDAO.cancelRegistration(regId)) {
                JOptionPane.showMessageDialog(this, "Registration cancelled successfully.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                refreshAllData();
            }
        }
    }

    private void handleSavePreferences() {
        try {
            currentStudent.setName(prefName.getText().trim());
            currentStudent.setDepartment((String) prefDept.getSelectedItem());
            currentStudent.setAcademicYear((Integer) prefYear.getSelectedItem());
            currentStudent.setMaxBudget(Double.parseDouble(prefBudget.getText().trim()));
            currentStudent.setFreeOnly(prefFreeOnly.isSelected());
            currentStudent.setSkills(prefSkills.getText().trim());
            currentStudent.setSkillLevel((String) prefSkillLevel.getSelectedItem());
            currentStudent.setAvailableSlot((String) prefSlot.getSelectedItem());
            currentStudent.setPreferredDays((String) prefDays.getSelectedItem());
            currentStudent.setCareerGoals((String) prefGoal.getSelectedItem());
            currentStudent.setPreferredFormat((String) prefFormat.getSelectedItem());

            studentDAO.saveOrUpdateStudent(currentStudent);
            JOptionPane.showMessageDialog(this, "Your preference profile has been updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            refreshAllData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- UI HELPERS ---
    private JPanel createHeaderPanel(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel l1 = new JLabel(title);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l1.setForeground(TEXT_NAVY);

        JLabel l2 = new JLabel(subtitle);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l2.setForeground(TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 2, 2));
        titles.setOpaque(false);
        titles.add(l1);
        titles.add(l2);

        p.add(titles, BorderLayout.WEST);
        return p;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(235, 238, 242));

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(240, 243, 248));
        th.setForeground(TEXT_NAVY);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, center);

        return table;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
