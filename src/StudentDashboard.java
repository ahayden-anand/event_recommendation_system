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
    private java.util.Map<String, JButton> navButtons = new java.util.LinkedHashMap<String, JButton>();
    private String currentActiveTab = "REC";

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
    private JLabel lblBudgetMatchCount;
    private ArrayList<Event> currentBudgetEvents;

    // Tab 3: Skill Growth
    private JPanel pnlSkillCardsContainer;

    // Tab 4: Discover Something New
    private DefaultTableModel discoverTableModel;
    private JTable discoverTable;
    private JTextArea txtDiscoverExplanation;
    private JButton btnRegisterDiscoverEvent;
    private ArrayList<Event> currentDiscoverEvents;

    // Tab 5: Browse All Events
    private DefaultTableModel allEventsTableModel;
    private JTable allEventsTable;
    private JTextField txtCatalogSearch;
    private JComboBox<String> cmbCatalogCategory;
    private JComboBox<String> cmbCatalogFormat;
    private JLabel lblCatalogCount;
    private JTextArea txtCatalogDetails;
    private ArrayList<Event> currentCatalogEvents;

    // Tab 6: My Registrations
    private DefaultTableModel myRegTableModel;
    private JTable myRegTable;
    private JLabel lblMyRegStatus;

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

        JButton btnNavRec = createNavButton("Recommended Events", "REC");
        JButton btnNavBudget = createNavButton("Budget Finder", "BUDGET");
        JButton btnNavSkill = createNavButton("Skill Growth", "SKILL");
        JButton btnNavDiscover = createNavButton("Discover New", "DISCOVER");
        JButton btnNavBrowse = createNavButton("Browse Events", "BROWSE");
        JButton btnNavMyReg = createNavButton("My Registrations", "MYREG");
        JButton btnNavPref = createNavButton("My Preferences", "PREF");

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

        updateActiveNavButtons();
        add(root);
    }

    private void showTab(String key) {
        currentActiveTab = key;
        cardLayout.show(mainContentPanel, key);
        updateActiveNavButtons();
        refreshAllData();
    }

    private void updateActiveNavButtons() {
        for (java.util.Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            String tabKey = entry.getKey();
            JButton btn = entry.getValue();
            if (tabKey.equals(currentActiveTab)) {
                btn.setBackground(new Color(29, 78, 216)); // Vivid Royal Blue (#1D4ED8)
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(96, 165, 250)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 15)
                ));
            } else {
                btn.setBackground(SIDEBAR_BG);
                btn.setForeground(new Color(203, 213, 225));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 15));
            }
        }
    }

    private JButton createNavButton(String title, final String tabKey) {
        final JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!tabKey.equals(currentActiveTab)) {
                    btn.setBackground(new Color(30, 41, 59));
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!tabKey.equals(currentActiveTab)) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(new Color(203, 213, 225));
                }
            }
        });

        navButtons.put(tabKey, btn);
        return btn;
    }

    // --- TAB 1: RECOMMENDED EVENTS ---
    private JPanel createRecommendationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = createHeaderPanel("Recommended For You", "100-Point Match Scoring tailored to your academic profile & preferences.");
        JButton btnRegAction = new JButton("Register Selected Event");
        stylePrimaryButton(btnRegAction);
        btnRegAction.addActionListener(e -> handleRegisterSelectedRecommendation());
        header.add(btnRegAction, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Match Score", "Quality Tier", "Event Title", "Category", "Date & Slot", "Fee", "Format"};
        recTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recTable = createStyledTable(recTableModel);

        // Visual color renderer for Score & Tier
        DefaultTableCellRenderer matchRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && row >= 0 && currentRecommendations != null && row < currentRecommendations.size()) {
                    int score = currentRecommendations.get(row).getScore();
                    if (score >= 90) {
                        c.setForeground(new Color(39, 174, 96));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (score >= 70) {
                        c.setForeground(new Color(41, 128, 185));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(new Color(211, 84, 0));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    }
                }
                return c;
            }
        };
        recTable.getColumnModel().getColumn(0).setCellRenderer(matchRenderer);
        recTable.getColumnModel().getColumn(1).setCellRenderer(matchRenderer);

        JScrollPane scrollTable = new JScrollPane(recTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(CARD_BORDER));

        // Rationale & details card at bottom
        JPanel rationaleCard = new JPanel(new BorderLayout(8, 8));
        rationaleCard.setBackground(CARD_BG);
        rationaleCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        JLabel lblRat = new JLabel("Transparent 100-Point Match Score & Event Overview:");
        lblRat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRat.setForeground(TEXT_NAVY);

        txtRecExplanation = new JTextArea("Select any recommended event from the table above to view its transparent matching explanation.", 4, 40);
        txtRecExplanation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtRecExplanation.setForeground(TEXT_NAVY);
        txtRecExplanation.setLineWrap(true);
        txtRecExplanation.setWrapStyleWord(true);
        txtRecExplanation.setEditable(false);
        txtRecExplanation.setBackground(new Color(248, 250, 253));
        txtRecExplanation.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JButton btnRegBottom = new JButton("Register for Event");
        stylePrimaryButton(btnRegBottom);
        btnRegBottom.addActionListener(e -> handleRegisterSelectedRecommendation());

        rationaleCard.add(lblRat, BorderLayout.NORTH);
        rationaleCard.add(txtRecExplanation, BorderLayout.CENTER);
        rationaleCard.add(btnRegBottom, BorderLayout.EAST);

        recTable.getSelectionModel().addListSelectionListener(e -> {
            int row = recTable.getSelectedRow();
            if (row >= 0 && currentRecommendations != null && row < currentRecommendations.size()) {
                updateRecommendationDetailsCard(currentRecommendations.get(row));
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollTable, rationaleCard);
        splitPane.setResizeWeight(0.68);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private void updateRecommendationDetailsCard(EventRecommendation rec) {
        if (rec == null) return;
        Event ev = rec.getEvent();
        int count = registrationDAO.getRegistrationCountForEvent(ev.getEventId());
        int remaining = Math.max(0, ev.getMaxCapacity() - count);
        String feeStr = ev.getFee() == 0.0 ? "FREE" : "Rs. " + ev.getFee();

        txtRecExplanation.setText(
            "Event: " + ev.getTitle() + "  |  Category: " + ev.getCategory() + " [" + ev.getEventFormat() + "]  |  Fee: " + feeStr + "\n"
            + "Schedule: " + ev.getEventDate() + " (" + ev.getTimeSlot() + ") " + ev.getStartTime() + " - " + ev.getEndTime() + "  |  Venue: " + ev.getVenue() + "  |  Seats: " + count + "/" + ev.getMaxCapacity() + " (" + remaining + " remaining)\n\n"
            + "100-POINT SCORING BREAKDOWN:\n"
            + "  * Interests Match: +" + rec.getInterestScore() + "/30 pts\n"
            + "  * Skill Progression: +" + rec.getSkillScore() + "/25 pts\n"
            + "  * Budget Affordability: +" + rec.getBudgetScore() + "/20 pts\n"
            + "  * Time Slot & Day Match: +" + rec.getAvailabilityScore() + "/15 pts\n"
            + "  * Career Goal Alignment: +" + rec.getCareerGoalScore() + "/10 pts\n\n"
            + "MATCH ANALYSIS: " + rec.getMatchReason()
        );
    }

    // --- TAB 2: BUDGET-FRIENDLY EVENT FINDER ---
    private JPanel createBudgetFinderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Budget-Friendly Event Finder", "Filter campus events strictly within your financial affordability.");

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterControls.setOpaque(false);

        lblBudgetMatchCount = new JLabel("Showing affordable events");
        lblBudgetMatchCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBudgetMatchCount.setForeground(TEXT_NAVY);

        JLabel lblMax = new JLabel("Max Budget (Rs):");
        lblMax.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtMaxBudgetFilter = new JTextField(String.valueOf(currentStudent.getMaxBudget()), 6);
        cbFreeOnlyFilter = new JCheckBox("Free Only", currentStudent.isFreeOnly());
        cbFreeOnlyFilter.setOpaque(false);

        JButton btnApplyBudget = new JButton("Filter & Sort by Fee");
        stylePrimaryButton(btnApplyBudget);
        btnApplyBudget.addActionListener(e -> refreshBudgetEvents());

        filterControls.add(lblBudgetMatchCount);
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

        // Details preview card below table
        JPanel detailsCard = new JPanel(new BorderLayout(8, 8));
        detailsCard.setBackground(CARD_BG);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        JLabel lblDetailsTitle = new JLabel("Event Details & One-Click Registration:");
        lblDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDetailsTitle.setForeground(TEXT_NAVY);

        JTextArea txtBudgetDetails = new JTextArea("Select an event above to view full venue, time, and fee details.", 3, 40);
        txtBudgetDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtBudgetDetails.setForeground(TEXT_NAVY);
        txtBudgetDetails.setEditable(false);
        txtBudgetDetails.setLineWrap(true);
        txtBudgetDetails.setWrapStyleWord(true);

        JButton btnRegisterBudgetEvent = new JButton("Register Selected Event");
        stylePrimaryButton(btnRegisterBudgetEvent);
        btnRegisterBudgetEvent.addActionListener(e -> {
            int row = budgetTable.getSelectedRow();
            if (row >= 0 && currentBudgetEvents != null && row < currentBudgetEvents.size()) {
                executeRegistration(currentBudgetEvents.get(row));
            } else {
                JOptionPane.showMessageDialog(this, "Please select an event from the table to register.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        detailsCard.add(lblDetailsTitle, BorderLayout.NORTH);
        detailsCard.add(txtBudgetDetails, BorderLayout.CENTER);
        detailsCard.add(btnRegisterBudgetEvent, BorderLayout.EAST);

        budgetTable.getSelectionModel().addListSelectionListener(e -> {
            int row = budgetTable.getSelectedRow();
            if (row >= 0 && currentBudgetEvents != null && row < currentBudgetEvents.size()) {
                Event ev = currentBudgetEvents.get(row);
                txtBudgetDetails.setText(
                    "Event: " + ev.getTitle() + " | Category: " + ev.getCategory() + "\n"
                    + "Schedule: " + ev.getEventDate() + " (" + ev.getTimeSlot() + ") " + ev.getStartTime() + " - " + ev.getEndTime() + "\n"
                    + "Venue: " + ev.getVenue() + " [" + ev.getEventFormat() + "] | Fee: " + (ev.getFee() == 0 ? "FREE" : "Rs. " + ev.getFee()) + " | Capacity: " + ev.getMaxCapacity()
                );
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(budgetTable), detailsCard);
        split.setResizeWeight(0.72);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 3: SKILL GROWTH PATHWAYS ---
    private JPanel createSkillGrowthPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Skill Growth Pathways", "Step-by-step learning progression aligned with your career goals.");
        panel.add(header, BorderLayout.NORTH);

        pnlSkillCardsContainer = new JPanel();
        pnlSkillCardsContainer.setLayout(new BoxLayout(pnlSkillCardsContainer, BoxLayout.Y_AXIS));
        pnlSkillCardsContainer.setBackground(BG_CANVAS);

        JScrollPane scroll = new JScrollPane(pnlSkillCardsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStageSection(String stageTitle, String stageSubtitle, Color accentColor, ArrayList<Event> stageEvents) {
        JPanel stageCard = new JPanel(new BorderLayout(0, 10));
        stageCard.setBackground(CARD_BG);
        stageCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
            )
        ));

        // Header of this stage
        JPanel stageHeader = new JPanel(new BorderLayout(10, 2));
        stageHeader.setOpaque(false);

        JLabel lblTitle = new JLabel(stageTitle);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(accentColor);

        JLabel lblSub = new JLabel(stageSubtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);

        JLabel lblCount = new JLabel(stageEvents.size() + (stageEvents.size() == 1 ? " Event Available" : " Events Available"));
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCount.setForeground(TEXT_NAVY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblSub);

        stageHeader.add(titleBlock, BorderLayout.CENTER);
        stageHeader.add(lblCount, BorderLayout.EAST);
        stageCard.add(stageHeader, BorderLayout.NORTH);

        // Body with list of event cards
        JPanel eventsListPanel = new JPanel();
        eventsListPanel.setLayout(new BoxLayout(eventsListPanel, BoxLayout.Y_AXIS));
        eventsListPanel.setOpaque(false);

        if (stageEvents.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
            emptyPanel.setOpaque(false);
            JLabel lblEmpty = new JLabel("No scheduled events in this stage yet. Check back soon as coordinators publish sessions!");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblEmpty.setForeground(TEXT_MUTED);
            emptyPanel.add(lblEmpty);
            eventsListPanel.add(emptyPanel);
        } else {
            for (int i = 0; i < stageEvents.size(); i++) {
                final Event ev = stageEvents.get(i);
                JPanel eventItem = new JPanel(new BorderLayout(12, 6));
                eventItem.setBackground(new Color(250, 252, 255));
                eventItem.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 235, 245), 1),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
                ));

                // Left info
                JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 4));
                infoPanel.setOpaque(false);

                String feeText = ev.getFee() == 0.0 ? "FREE" : "Rs. " + ev.getFee();
                JLabel lblEventTitle = new JLabel(ev.getTitle() + "  [" + ev.getCategory() + " | " + feeText + "]");
                lblEventTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblEventTitle.setForeground(TEXT_NAVY);

                String skillsReq = (ev.getRequiredSkills() != null && !ev.getRequiredSkills().trim().isEmpty())
                    ? ev.getRequiredSkills() : "Open to all";
                JLabel lblEventMeta = new JLabel("Date: " + ev.getEventDate() + " (" + ev.getTimeSlot() + ")  |  Venue: " + ev.getVenue() + " [" + ev.getEventFormat() + "]  |  Skills: " + skillsReq);
                lblEventMeta.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblEventMeta.setForeground(TEXT_MUTED);

                infoPanel.add(lblEventTitle);
                infoPanel.add(lblEventMeta);
                eventItem.add(infoPanel, BorderLayout.CENTER);

                // Right button
                JButton btnReg = new JButton("Register");
                stylePrimaryButton(btnReg);
                btnReg.setPreferredSize(new Dimension(95, 28));
                btnReg.addActionListener(e -> executeRegistration(ev));
                eventItem.add(btnReg, BorderLayout.EAST);

                eventsListPanel.add(eventItem);
                if (i < stageEvents.size() - 1) {
                    eventsListPanel.add(Box.createVerticalStrut(6));
                }
            }
        }

        stageCard.add(eventsListPanel, BorderLayout.CENTER);
        return stageCard;
    }

    private boolean isRelevantToSkillPathway(Event e, Student s) {
        String cat = e.getCategory() != null ? e.getCategory().toLowerCase() : "";
        String title = e.getTitle() != null ? e.getTitle().toLowerCase() : "";
        String reqSkills = e.getRequiredSkills() != null ? e.getRequiredSkills().toLowerCase() : "";
        String goal = s.getCareerGoals() != null ? s.getCareerGoals().toLowerCase() : "";
        String interests = s.getInterests() != null ? s.getInterests().toLowerCase() : "";
        String skills = s.getSkills() != null ? s.getSkills().toLowerCase() : "";
        String dept = s.getDepartment() != null ? s.getDepartment().toLowerCase() : "";

        if (!interests.isEmpty() && interests.contains(cat)) return true;

        if (!skills.isEmpty()) {
            String[] skillTokens = skills.split("[, /]+");
            for (int i = 0; i < skillTokens.length; i++) {
                String sk = skillTokens[i].trim();
                if (sk.length() > 1 && (reqSkills.contains(sk) || title.contains(sk) || cat.contains(sk))) {
                    return true;
                }
            }
        }

        if (!goal.isEmpty()) {
            String[] goalTokens = goal.split("[, /]+");
            for (int i = 0; i < goalTokens.length; i++) {
                String gt = goalTokens[i].trim();
                if (gt.length() > 2 && (cat.contains(gt) || title.contains(gt) || reqSkills.contains(gt))) {
                    return true;
                }
            }
        }

        if (dept.contains("cse") || dept.contains("it") || goal.contains("engineer") || goal.contains("developer") || goal.contains("program")) {
            if (cat.contains("coding") || cat.contains("ai") || cat.contains("web") || cat.contains("tech") ||
                cat.contains("robotics") || title.contains("python") || title.contains("java") || title.contains("hackathon") ||
                title.contains("data") || title.contains("cloud") || title.contains("security") || title.contains("dev")) {
                return true;
            }
        }

        if (cat.contains("workshop") || cat.contains("seminar") || cat.contains("entrepreneurship")) {
            return true;
        }

        return false;
    }

    private int determineEventProgressionStage(Event e) {
        String targetLvl = e.getTargetSkillLevel() != null ? e.getTargetSkillLevel().toLowerCase() : "";
        String title = e.getTitle() != null ? e.getTitle().toLowerCase() : "";
        String cat = e.getCategory() != null ? e.getCategory().toLowerCase() : "";

        if (targetLvl.contains("advanced")) return 3;
        if (targetLvl.contains("intermediate")) return 2;
        if (targetLvl.contains("beginner")) return 1;

        if (title.contains("hackathon") || title.contains("competition") || title.contains("championship") ||
            title.contains("conclave") || title.contains("summit") || title.contains("symposium") ||
            title.contains("capstone") || title.contains("paper") || cat.contains("competition")) {
            return 3;
        }

        if (title.contains("bootcamp") || title.contains("challenge") || title.contains("project") ||
            title.contains("hands-on") || title.contains("deep dive") || title.contains("masterclass") ||
            title.contains("build") || title.contains("sprint")) {
            return 2;
        }

        if (title.contains("intro") || title.contains("fundamental") || title.contains("basics") ||
            title.contains("workshop") || title.contains("101") || title.contains("seminar") ||
            title.contains("overview") || e.getFee() == 0.0) {
            return 1;
        }

        return 2;
    }

    // --- TAB 4: DISCOVER SOMETHING NEW ---
    private JPanel createDiscoverPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Discover Something New", "Goal-aligned interdisciplinary events outside your usual primary interests.");
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Event Title", "Category", "Date & Slot", "Fee", "Venue"};
        discoverTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        discoverTable = createStyledTable(discoverTableModel);

        // Preview & Dynamic Explanation Card at Bottom
        JPanel detailsCard = new JPanel(new BorderLayout(10, 10));
        detailsCard.setBackground(CARD_BG);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblDetailsTitle = new JLabel("Strategic Cross-Disciplinary Value & One-Click Registration:");
        lblDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDetailsTitle.setForeground(TEXT_NAVY);

        txtDiscoverExplanation = new JTextArea("Select an exploration event to read why it will broaden your horizons.", 4, 40);
        txtDiscoverExplanation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiscoverExplanation.setForeground(TEXT_NAVY);
        txtDiscoverExplanation.setEditable(false);
        txtDiscoverExplanation.setLineWrap(true);
        txtDiscoverExplanation.setWrapStyleWord(true);
        txtDiscoverExplanation.setBackground(new Color(245, 248, 255));
        txtDiscoverExplanation.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 225, 245), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        btnRegisterDiscoverEvent = new JButton("Register for Event");
        stylePrimaryButton(btnRegisterDiscoverEvent);
        btnRegisterDiscoverEvent.addActionListener(e -> {
            int row = discoverTable.getSelectedRow();
            if (row >= 0 && currentDiscoverEvents != null && row < currentDiscoverEvents.size()) {
                executeRegistration(currentDiscoverEvents.get(row));
            } else {
                JOptionPane.showMessageDialog(this, "Please select an event from the table to register.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        detailsCard.add(lblDetailsTitle, BorderLayout.NORTH);
        detailsCard.add(txtDiscoverExplanation, BorderLayout.CENTER);
        detailsCard.add(btnRegisterDiscoverEvent, BorderLayout.EAST);

        discoverTable.getSelectionModel().addListSelectionListener(e -> {
            int row = discoverTable.getSelectedRow();
            if (row >= 0 && currentDiscoverEvents != null && row < currentDiscoverEvents.size()) {
                updateDiscoverDetailsCard(currentDiscoverEvents.get(row));
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(discoverTable), detailsCard);
        split.setResizeWeight(0.68);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void updateDiscoverDetailsCard(Event ev) {
        if (ev == null) return;
        String rationale = generateDiscoverRationale(currentStudent, ev);
        String feeStr = ev.getFee() == 0.0 ? "FREE" : "Rs. " + ev.getFee();
        txtDiscoverExplanation.setText(
            "Event: " + ev.getTitle() + "  |  Category: " + ev.getCategory() + " [" + ev.getEventFormat() + "]\n"
            + "Schedule: " + ev.getEventDate() + " (" + ev.getTimeSlot() + ") " + ev.getStartTime() + " - " + ev.getEndTime() + "  |  Venue: " + ev.getVenue() + "  |  Fee: " + feeStr + "\n\n"
            + "STRATEGIC EXPLORATION VALUE:\n" + rationale
        );
    }

    private String generateDiscoverRationale(Student s, Event e) {
        String cat = e.getCategory() != null ? e.getCategory().toLowerCase() : "";
        String title = e.getTitle() != null ? e.getTitle().toLowerCase() : "";
        String goal = (s.getCareerGoals() != null && !s.getCareerGoals().trim().isEmpty())
            ? s.getCareerGoals() : "academic & career development";
        String dept = s.getDepartment() != null ? s.getDepartment() : "Engineering";

        if (cat.contains("entrepreneur") || title.contains("startup") || title.contains("business") || title.contains("pitch")) {
            return "As a " + dept + " student aspiring toward " + goal + ", learning venture creation, product monetization, and tech pitching gives you the commercial acumen needed to turn technical prototypes into viable market products.";
        } else if (cat.contains("public speaking") || cat.contains("debate") || title.contains("communication") || title.contains("toastmaster") || cat.contains("literary")) {
            return "Mastering public speaking and persuasive argumentation prepares you for technical leadership, high-stakes demos, and research defenses—crucial soft skills that distinguish senior engineers and leaders in " + goal + ".";
        } else if (cat.contains("robotics") || cat.contains("iot") || title.contains("hardware") || title.contains("arduino")) {
            return "Venturing into physical computing and embedded systems bridges the gap between pure software algorithms and real-world hardware automation, expanding your versatility as an engineer.";
        } else if (cat.contains("design") || title.contains("ui/ux") || title.contains("design") || title.contains("user experience") || title.contains("creative")) {
            return "Gaining proficiency in user-centric design and visual architecture enhances your engineering workflow, enabling you to design intuitive, aesthetically polished applications that users love.";
        } else if (cat.contains("finance") || title.contains("fintech") || title.contains("investment") || title.contains("crypto")) {
            return "Understanding financial systems and algorithmic economics opens doors to quantitative engineering and FinTech roles, offering a valuable analytical complement to your " + goal + " roadmap.";
        } else if (cat.contains("sports") || cat.contains("fitness") || cat.contains("wellness")) {
            return "Physical endurance, teamwork, and tactical gameplay cultivate mental stamina and stress management, helping you maintain peak cognitive performance through demanding academic sprints.";
        } else if (cat.contains("photo") || cat.contains("media") || cat.contains("film") || cat.contains("art")) {
            return "Creative visual storytelling and media production strengthen your personal branding, project documentation, and digital marketing skills in today's tech ecosystem.";
        } else if (cat.contains("music") || cat.contains("cultural")) {
            return "Engaging in creative arts stimulates cognitive neuroplasticity and lateral thinking, providing mental refreshment and fresh perspectives for complex algorithmic problem solving.";
        } else {
            return "Participating in " + e.getCategory() + " introduces interdisciplinary perspectives outside traditional coursework, cultivating creative problem-solving and well-rounded leadership for your path in " + goal + ".";
        }
    }

    // --- TAB 5: BROWSE ALL EVENTS ---
    private JPanel createBrowseEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Browse Campus Events", "Search, filter, and inspect all active events across campus departments.");
        panel.add(header, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(CARD_BG);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtCatalogSearch = new JTextField(12);

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbCatalogCategory = new JComboBox<String>(new String[]{
            "All Categories", "Coding", "AI", "Robotics", "Design", "Entrepreneurship", "Public Speaking", "Sports", "Arts", "Tech"
        });

        JLabel lblFmt = new JLabel("Format:");
        lblFmt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbCatalogFormat = new JComboBox<String>(new String[]{
            "All Formats", "In-person", "Online", "Hybrid"
        });

        JButton btnFilterCatalog = new JButton("Filter");
        stylePrimaryButton(btnFilterCatalog);
        btnFilterCatalog.setPreferredSize(new Dimension(80, 28));
        btnFilterCatalog.addActionListener(e -> refreshAllEventsCatalog());

        JButton btnResetCatalog = new JButton("Reset");
        btnResetCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnResetCatalog.setPreferredSize(new Dimension(80, 28));
        btnResetCatalog.setFocusPainted(false);
        btnResetCatalog.addActionListener(e -> {
            txtCatalogSearch.setText("");
            cmbCatalogCategory.setSelectedIndex(0);
            cmbCatalogFormat.setSelectedIndex(0);
            refreshAllEventsCatalog();
        });

        lblCatalogCount = new JLabel("Showing events");
        lblCatalogCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCatalogCount.setForeground(TEXT_NAVY);

        filterBar.add(lblSearch);
        filterBar.add(txtCatalogSearch);
        filterBar.add(lblCat);
        filterBar.add(cmbCatalogCategory);
        filterBar.add(lblFmt);
        filterBar.add(cmbCatalogFormat);
        filterBar.add(btnFilterCatalog);
        filterBar.add(btnResetCatalog);
        filterBar.add(lblCatalogCount);

        // Table
        String[] cols = {"ID", "Event Title", "Category", "Date & Slot", "Fee", "Format", "Venue", "Capacity & Seats"};
        allEventsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allEventsTable = createStyledTable(allEventsTableModel);

        // Details & Registration card
        JPanel detailsCard = new JPanel(new BorderLayout(10, 10));
        detailsCard.setBackground(CARD_BG);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel lblDetailsTitle = new JLabel("Selected Event Overview & One-Click Registration:");
        lblDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDetailsTitle.setForeground(TEXT_NAVY);

        txtCatalogDetails = new JTextArea("Select an event from the catalog above to view full details and register.", 3, 40);
        txtCatalogDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCatalogDetails.setForeground(TEXT_NAVY);
        txtCatalogDetails.setEditable(false);
        txtCatalogDetails.setLineWrap(true);
        txtCatalogDetails.setWrapStyleWord(true);
        txtCatalogDetails.setBackground(new Color(248, 250, 253));
        txtCatalogDetails.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JButton btnRegisterCatalogEvent = new JButton("Register for Event");
        stylePrimaryButton(btnRegisterCatalogEvent);
        btnRegisterCatalogEvent.addActionListener(e -> handleRegisterFromCatalog());

        detailsCard.add(lblDetailsTitle, BorderLayout.NORTH);
        detailsCard.add(txtCatalogDetails, BorderLayout.CENTER);
        detailsCard.add(btnRegisterCatalogEvent, BorderLayout.EAST);

        allEventsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = allEventsTable.getSelectedRow();
            if (row >= 0 && currentCatalogEvents != null && row < currentCatalogEvents.size()) {
                updateCatalogDetailsCard(currentCatalogEvents.get(row));
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(allEventsTable), detailsCard);
        split.setResizeWeight(0.72);
        split.setBorder(null);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(filterBar, BorderLayout.NORTH);
        centerPanel.add(split, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private void updateCatalogDetailsCard(Event ev) {
        if (ev == null) return;
        int count = registrationDAO.getRegistrationCountForEvent(ev.getEventId());
        int remaining = Math.max(0, ev.getMaxCapacity() - count);
        String feeStr = ev.getFee() == 0.0 ? "FREE" : "Rs. " + ev.getFee();
        String skills = (ev.getRequiredSkills() != null && !ev.getRequiredSkills().trim().isEmpty()) ? ev.getRequiredSkills() : "Open to all";

        txtCatalogDetails.setText(
            "Event: " + ev.getTitle() + "  |  Category: " + ev.getCategory() + " [" + ev.getEventFormat() + "]  |  Fee: " + feeStr + "\n"
            + "Schedule: " + ev.getEventDate() + " (" + ev.getTimeSlot() + ") " + ev.getStartTime() + " - " + ev.getEndTime() + "  |  Venue: " + ev.getVenue() + "\n"
            + "Prerequisites: " + skills + "  |  Capacity: " + count + " / " + ev.getMaxCapacity() + " (" + remaining + " seats available)"
        );
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

        String[] cols = {"Reg ID", "Event Title", "Schedule (Date & Slot)", "Venue", "Registered On", "Status"};
        myRegTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        myRegTable = createStyledTable(myRegTableModel);

        lblMyRegStatus = new JLabel("Loading your registrations...");
        lblMyRegStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMyRegStatus.setForeground(TEXT_MUTED);
        lblMyRegStatus.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(new JScrollPane(myRegTable), BorderLayout.CENTER);
        centerPanel.add(lblMyRegStatus, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 7: PREFERENCES PROFILE ---
    private JPanel createPreferencesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Configure Preference Profile", "Update your academic, financial, interest, and scheduling preferences at any time.");
        JButton btnSavePrefTop = new JButton("Save Profile Preferences");
        stylePrimaryButton(btnSavePrefTop);
        btnSavePrefTop.addActionListener(e -> handleSavePreferences());
        header.add(btnSavePrefTop, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel pnlPrefsContainer = new JPanel();
        pnlPrefsContainer.setLayout(new BoxLayout(pnlPrefsContainer, BoxLayout.Y_AXIS));
        pnlPrefsContainer.setBackground(BG_CANVAS);

        // --- CARD 1: ACADEMIC & FINANCIAL PROFILE ---
        JPanel card1 = new JPanel(new BorderLayout(0, 10));
        card1.setBackground(CARD_BG);
        card1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(41, 128, 185)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
            )
        ));

        JPanel c1Header = new JPanel(new GridLayout(2, 1, 0, 2));
        c1Header.setOpaque(false);
        JLabel lblC1Title = new JLabel("1. Academic & Financial Profile");
        lblC1Title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblC1Title.setForeground(TEXT_NAVY);
        JLabel lblC1Sub = new JLabel("Your department, year, and budget directly filter event eligibility and recommendation relevance.");
        lblC1Sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblC1Sub.setForeground(TEXT_MUTED);
        c1Header.add(lblC1Title);
        c1Header.add(lblC1Sub);
        card1.add(c1Header, BorderLayout.NORTH);

        JPanel c1Grid = new JPanel(new GridLayout(3, 2, 15, 10));
        c1Grid.setOpaque(false);

        // Full Name
        JPanel pName = new JPanel(new BorderLayout(4, 2));
        pName.setOpaque(false);
        pName.add(new JLabel("Full Name:"), BorderLayout.NORTH);
        prefName = new JTextField(currentStudent.getName());
        pName.add(prefName, BorderLayout.CENTER);
        c1Grid.add(pName);

        // Department
        JPanel pDept = new JPanel(new BorderLayout(4, 2));
        pDept.setOpaque(false);
        pDept.add(new JLabel("Department:"), BorderLayout.NORTH);
        prefDept = new JComboBox<String>(new String[]{"CSE", "IT", "ECE", "MECH", "CIVIL"});
        prefDept.setSelectedItem(currentStudent.getDepartment());
        pDept.add(prefDept, BorderLayout.CENTER);
        c1Grid.add(pDept);

        // Academic Year
        JPanel pYear = new JPanel(new BorderLayout(4, 2));
        pYear.setOpaque(false);
        pYear.add(new JLabel("Academic Year:"), BorderLayout.NORTH);
        prefYear = new JComboBox<Integer>(new Integer[]{1, 2, 3, 4});
        prefYear.setSelectedItem(currentStudent.getAcademicYear());
        pYear.add(prefYear, BorderLayout.CENTER);
        c1Grid.add(pYear);

        // Max Budget
        JPanel pBudget = new JPanel(new BorderLayout(4, 2));
        pBudget.setOpaque(false);
        pBudget.add(new JLabel("Max Event Fee Budget (Rs):"), BorderLayout.NORTH);
        prefBudget = new JTextField(String.valueOf(currentStudent.getMaxBudget()));
        pBudget.add(prefBudget, BorderLayout.CENTER);
        c1Grid.add(pBudget);

        // Free Only Checkbox
        JPanel pFree = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        pFree.setOpaque(false);
        prefFreeOnly = new JCheckBox("Strictly Restrict to 100% Free Events", currentStudent.isFreeOnly());
        prefFreeOnly.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prefFreeOnly.setForeground(TEXT_NAVY);
        prefFreeOnly.setOpaque(false);
        pFree.add(prefFreeOnly);
        c1Grid.add(pFree);

        card1.add(c1Grid, BorderLayout.CENTER);
        pnlPrefsContainer.add(card1);
        pnlPrefsContainer.add(Box.createVerticalStrut(14));

        // --- CARD 2: PRIMARY AREAS OF INTEREST (CHECKBOX MATRIX) ---
        JPanel card2 = new JPanel(new BorderLayout(0, 10));
        card2.setBackground(CARD_BG);
        card2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(39, 174, 96)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
            )
        ));

        JPanel c2Header = new JPanel(new GridLayout(2, 1, 0, 2));
        c2Header.setOpaque(false);
        JLabel lblC2Title = new JLabel("2. Primary Areas of Interest");
        lblC2Title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblC2Title.setForeground(TEXT_NAVY);
        JLabel lblC2Sub = new JLabel("Events in checked categories receive up to +30 match points in the transparent recommendation scoring algorithm.");
        lblC2Sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblC2Sub.setForeground(TEXT_MUTED);
        c2Header.add(lblC2Title);
        c2Header.add(lblC2Sub);
        card2.add(c2Header, BorderLayout.NORTH);

        JPanel c2Grid = new JPanel(new GridLayout(5, 2, 15, 6));
        c2Grid.setOpaque(false);

        String interestsLower = currentStudent.getInterests() != null ? currentStudent.getInterests().toLowerCase() : "";
        cbCoding = new JCheckBox("Coding & Software Development", interestsLower.contains("coding"));
        cbAI = new JCheckBox("AI & Machine Learning", interestsLower.contains("ai") || interestsLower.contains("machine"));
        cbRobotics = new JCheckBox("Robotics & IoT Automation", interestsLower.contains("robotics") || interestsLower.contains("iot"));
        cbEntrepreneurship = new JCheckBox("Entrepreneurship & Startups", interestsLower.contains("entrepreneur") || interestsLower.contains("startup"));
        cbDesign = new JCheckBox("UI/UX & Design Systems", interestsLower.contains("design") || interestsLower.contains("ui"));
        cbPublicSpeaking = new JCheckBox("Public Speaking & Debate", interestsLower.contains("public") || interestsLower.contains("speaking") || interestsLower.contains("debate"));
        cbPhotography = new JCheckBox("Photography & Digital Media", interestsLower.contains("photo") || interestsLower.contains("media"));
        cbSports = new JCheckBox("Sports & Athletics", interestsLower.contains("sports") || interestsLower.contains("fitness"));
        cbMusic = new JCheckBox("Music & Performing Arts", interestsLower.contains("music") || interestsLower.contains("cultural"));
        cbDance = new JCheckBox("Dance & Cultural Festivals", interestsLower.contains("dance"));

        JCheckBox[] cbs = {cbCoding, cbAI, cbRobotics, cbEntrepreneurship, cbDesign, cbPublicSpeaking, cbPhotography, cbSports, cbMusic, cbDance};
        for (int i = 0; i < cbs.length; i++) {
            cbs[i].setOpaque(false);
            cbs[i].setFont(new Font("Segoe UI", Font.PLAIN, 12));
            c2Grid.add(cbs[i]);
        }

        card2.add(c2Grid, BorderLayout.CENTER);
        pnlPrefsContainer.add(card2);
        pnlPrefsContainer.add(Box.createVerticalStrut(14));

        // --- CARD 3: SKILL GROWTH & SCHEDULING PREFERENCES ---
        JPanel card3 = new JPanel(new BorderLayout(0, 10));
        card3.setBackground(CARD_BG);
        card3.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(142, 68, 173)),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
            )
        ));

        JPanel c3Header = new JPanel(new GridLayout(2, 1, 0, 2));
        c3Header.setOpaque(false);
        JLabel lblC3Title = new JLabel("3. Skill Growth & Scheduling Preferences");
        lblC3Title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblC3Title.setForeground(TEXT_NAVY);
        JLabel lblC3Sub = new JLabel("Defines your progression stage, preferred time slots, and target career direction.");
        lblC3Sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblC3Sub.setForeground(TEXT_MUTED);
        c3Header.add(lblC3Title);
        c3Header.add(lblC3Sub);
        card3.add(c3Header, BorderLayout.NORTH);

        JPanel c3Grid = new JPanel(new GridLayout(3, 2, 15, 10));
        c3Grid.setOpaque(false);

        // Skills
        JPanel pSkills = new JPanel(new BorderLayout(4, 2));
        pSkills.setOpaque(false);
        pSkills.add(new JLabel("Known Skills (Comma-separated):"), BorderLayout.NORTH);
        prefSkills = new JTextField(currentStudent.getSkills());
        pSkills.add(prefSkills, BorderLayout.CENTER);
        c3Grid.add(pSkills);

        // Skill Level
        JPanel pLvl = new JPanel(new BorderLayout(4, 2));
        pLvl.setOpaque(false);
        pLvl.add(new JLabel("Current Skill Level:"), BorderLayout.NORTH);
        prefSkillLevel = new JComboBox<String>(new String[]{"Beginner", "Intermediate", "Advanced"});
        prefSkillLevel.setSelectedItem(currentStudent.getSkillLevel());
        pLvl.add(prefSkillLevel, BorderLayout.CENTER);
        c3Grid.add(pLvl);

        // Time Slot
        JPanel pSlot = new JPanel(new BorderLayout(4, 2));
        pSlot.setOpaque(false);
        pSlot.add(new JLabel("Preferred Time Slot:"), BorderLayout.NORTH);
        prefSlot = new JComboBox<String>(new String[]{"All", "Morning", "Afternoon", "Evening"});
        prefSlot.setSelectedItem(currentStudent.getAvailableSlot());
        pSlot.add(prefSlot, BorderLayout.CENTER);
        c3Grid.add(pSlot);

        // Preferred Days
        JPanel pDays = new JPanel(new BorderLayout(4, 2));
        pDays.setOpaque(false);
        pDays.add(new JLabel("Preferred Days:"), BorderLayout.NORTH);
        prefDays = new JComboBox<String>(new String[]{"Both", "Weekdays", "Weekends"});
        prefDays.setSelectedItem(currentStudent.getPreferredDays());
        pDays.add(prefDays, BorderLayout.CENTER);
        c3Grid.add(pDays);

        // Career Goal
        JPanel pGoal = new JPanel(new BorderLayout(4, 2));
        pGoal.setOpaque(false);
        pGoal.add(new JLabel("Target Career Goal:"), BorderLayout.NORTH);
        prefGoal = new JComboBox<String>(new String[]{
            "Full Stack Developer", "AI / ML Specialist", "Data Scientist",
            "Cloud & DevOps Engineer", "Cybersecurity Analyst", "Tech Entrepreneur",
            "Improve programming skills", "Prepare for placements", "Develop leadership", "Build a portfolio"
        });
        prefGoal.setSelectedItem(currentStudent.getCareerGoals());
        pGoal.add(prefGoal, BorderLayout.CENTER);
        c3Grid.add(pGoal);

        // Event Format
        JPanel pFmt = new JPanel(new BorderLayout(4, 2));
        pFmt.setOpaque(false);
        pFmt.add(new JLabel("Preferred Event Format:"), BorderLayout.NORTH);
        prefFormat = new JComboBox<String>(new String[]{"Either", "In-person", "Online"});
        prefFormat.setSelectedItem(currentStudent.getPreferredFormat());
        pFmt.add(prefFormat, BorderLayout.CENTER);
        c3Grid.add(pFmt);

        card3.add(c3Grid, BorderLayout.CENTER);
        pnlPrefsContainer.add(card3);
        pnlPrefsContainer.add(Box.createVerticalStrut(16));

        // Bottom Save Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomBar.setOpaque(false);
        JButton btnSaveBottom = new JButton("Save Profile Preferences");
        stylePrimaryButton(btnSaveBottom);
        btnSaveBottom.setPreferredSize(new Dimension(200, 34));
        btnSaveBottom.addActionListener(e -> handleSavePreferences());
        bottomBar.add(btnSaveBottom);
        pnlPrefsContainer.add(bottomBar);

        JScrollPane scroll = new JScrollPane(pnlPrefsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void loadPreferencesIntoUI() {
        if (currentStudent == null) return;
        if (prefName != null) prefName.setText(currentStudent.getName());
        if (prefDept != null) prefDept.setSelectedItem(currentStudent.getDepartment());
        if (prefYear != null) prefYear.setSelectedItem(currentStudent.getAcademicYear());
        if (prefBudget != null) prefBudget.setText(String.valueOf(currentStudent.getMaxBudget()));
        if (prefFreeOnly != null) prefFreeOnly.setSelected(currentStudent.isFreeOnly());
        if (prefSkills != null) prefSkills.setText(currentStudent.getSkills());
        if (prefSkillLevel != null) prefSkillLevel.setSelectedItem(currentStudent.getSkillLevel());
        if (prefSlot != null) prefSlot.setSelectedItem(currentStudent.getAvailableSlot());
        if (prefDays != null) prefDays.setSelectedItem(currentStudent.getPreferredDays());
        if (prefGoal != null) prefGoal.setSelectedItem(currentStudent.getCareerGoals());
        if (prefFormat != null) prefFormat.setSelectedItem(currentStudent.getPreferredFormat());

        String interests = currentStudent.getInterests() != null ? currentStudent.getInterests().toLowerCase() : "";
        if (cbCoding != null) cbCoding.setSelected(interests.contains("coding"));
        if (cbAI != null) cbAI.setSelected(interests.contains("ai") || interests.contains("machine"));
        if (cbRobotics != null) cbRobotics.setSelected(interests.contains("robotics") || interests.contains("iot"));
        if (cbEntrepreneurship != null) cbEntrepreneurship.setSelected(interests.contains("entrepreneur") || interests.contains("startup"));
        if (cbDesign != null) cbDesign.setSelected(interests.contains("design") || interests.contains("ui"));
        if (cbPublicSpeaking != null) cbPublicSpeaking.setSelected(interests.contains("public") || interests.contains("speaking") || interests.contains("debate"));
        if (cbPhotography != null) cbPhotography.setSelected(interests.contains("photo") || interests.contains("media"));
        if (cbSports != null) cbSports.setSelected(interests.contains("sports") || interests.contains("fitness"));
        if (cbMusic != null) cbMusic.setSelected(interests.contains("music") || interests.contains("cultural"));
        if (cbDance != null) cbDance.setSelected(interests.contains("dance"));
    }

    // --- REFRESH & DATA LOGIC ---
    private void refreshAllData() {
        loadStudentProfile();
        loadPreferencesIntoUI();
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

            String tier;
            if (rec.getScore() >= 90) {
                tier = "★ Strong Match";
            } else if (rec.getScore() >= 70) {
                tier = "● Good Match";
            } else {
                tier = "▲ Moderate Match";
            }

            recTableModel.addRow(new Object[]{
                rec.getScore() + "/100",
                tier,
                ev.getTitle(),
                ev.getCategory(),
                ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                ev.getFee() == 0 ? "FREE" : "Rs. " + ev.getFee(),
                ev.getEventFormat()
            });
        }

        if (currentRecommendations != null && !currentRecommendations.isEmpty()) {
            recTable.setRowSelectionInterval(0, 0);
            updateRecommendationDetailsCard(currentRecommendations.get(0));
        } else if (txtRecExplanation != null) {
            txtRecExplanation.setText("No recommended events found matching your department or budget. Try updating your preferences.");
        }
    }

    private void refreshBudgetEvents() {
        budgetTableModel.setRowCount(0);
        double maxBudget = currentStudent.getMaxBudget();
        try {
            if (txtMaxBudgetFilter != null) {
                String bText = txtMaxBudgetFilter.getText().trim();
                if (!bText.isEmpty()) {
                    double parsed = Double.parseDouble(bText);
                    if (parsed < 0) {
                        JOptionPane.showMessageDialog(this, "Maximum budget cannot be negative. Resetting to 0.0.", "Invalid Budget", JOptionPane.WARNING_MESSAGE);
                        parsed = 0.0;
                        txtMaxBudgetFilter.setText("0.0");
                    }
                    maxBudget = parsed;
                }
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric budget amount.", "Invalid Format", JOptionPane.WARNING_MESSAGE);
            txtMaxBudgetFilter.setText(String.valueOf(currentStudent.getMaxBudget()));
        }

        boolean freeOnly = cbFreeOnlyFilter != null ? cbFreeOnlyFilter.isSelected() : currentStudent.isFreeOnly();

        ArrayList<Event> all = eventDAO.getAllEvents();
        currentBudgetEvents = new ArrayList<Event>();

        // Strictly filter eligible events within budget (exclude over-budget by default)
        for (int i = 0; i < all.size(); i++) {
            Event ev = all.get(i);
            if (freeOnly && ev.getFee() > 0.0) continue;
            if (ev.getFee() > maxBudget) continue; // Exclude over-budget events!
            currentBudgetEvents.add(ev);
        }

        // Bubble sort eligible by fee ascending (O(n^2))
        for (int i = 0; i < currentBudgetEvents.size() - 1; i++) {
            for (int j = 0; j < currentBudgetEvents.size() - i - 1; j++) {
                if (currentBudgetEvents.get(j).getFee() > currentBudgetEvents.get(j + 1).getFee()) {
                    Event temp = currentBudgetEvents.get(j);
                    currentBudgetEvents.set(j, currentBudgetEvents.get(j + 1));
                    currentBudgetEvents.set(j + 1, temp);
                }
            }
        }

        if (lblBudgetMatchCount != null) {
            lblBudgetMatchCount.setText("Matching: " + currentBudgetEvents.size() + " event(s)");
        }

        if (currentBudgetEvents.isEmpty()) {
            budgetTableModel.addRow(new Object[]{
                "No events found matching your budget criteria.", "-", "-", "-", "-", "Try increasing budget or unchecking Free Only"
            });
            return;
        }

        for (int i = 0; i < currentBudgetEvents.size(); i++) {
            Event ev = currentBudgetEvents.get(i);
            String feeStr = ev.getFee() == 0.0 ? "FREE" : "Rs. " + ev.getFee();
            String status = ev.getFee() == 0.0 ? "100% Free" : "Affordable (Within Rs. " + maxBudget + ")";
            budgetTableModel.addRow(new Object[]{
                ev.getTitle(),
                feeStr,
                ev.getCategory(),
                ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                ev.getVenue(),
                status
            });
        }
    }

    private void refreshSkillGrowth() {
        if (pnlSkillCardsContainer == null) return;
        pnlSkillCardsContainer.removeAll();

        ArrayList<Event> allEvents = eventDAO.getAllEvents();
        String studentGoal = (currentStudent.getCareerGoals() != null && !currentStudent.getCareerGoals().trim().isEmpty())
            ? currentStudent.getCareerGoals() : "Software Development & Tech Engineering";

        // 1. Top Roadmap Summary Banner Card
        JPanel summaryCard = new JPanel(new BorderLayout(10, 6));
        summaryCard.setBackground(CARD_BG);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel lblGoalTitle = new JLabel("Target Learning Pathway: " + studentGoal);
        lblGoalTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblGoalTitle.setForeground(TEXT_NAVY);

        JLabel lblStudentMeta = new JLabel("Student: " + currentStudent.getName()
            + "  |  Department: " + currentStudent.getDepartment() + " (Year " + currentStudent.getAcademicYear() + ")"
            + "  |  Current Skill Level: " + currentStudent.getSkillLevel()
            + "  |  Known Skills: " + (currentStudent.getSkills() != null ? currentStudent.getSkills() : "None listed"));
        lblStudentMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStudentMeta.setForeground(TEXT_MUTED);

        JPanel summaryText = new JPanel(new GridLayout(2, 1, 0, 4));
        summaryText.setOpaque(false);
        summaryText.add(lblGoalTitle);
        summaryText.add(lblStudentMeta);
        summaryCard.add(summaryText, BorderLayout.CENTER);

        pnlSkillCardsContainer.add(summaryCard);
        pnlSkillCardsContainer.add(Box.createVerticalStrut(12));

        // 2. Classify events by stage with relevance filtering
        ArrayList<Event> stage1Events = new ArrayList<Event>();
        ArrayList<Event> stage2Events = new ArrayList<Event>();
        ArrayList<Event> stage3Events = new ArrayList<Event>();

        for (int i = 0; i < allEvents.size(); i++) {
            Event ev = allEvents.get(i);
            if (isRelevantToSkillPathway(ev, currentStudent)) {
                int stage = determineEventProgressionStage(ev);
                if (stage == 1) stage1Events.add(ev);
                else if (stage == 2) stage2Events.add(ev);
                else stage3Events.add(ev);
            }
        }

        // Fallback: If no events matched strict filter, populate all available events across stages
        if (stage1Events.isEmpty() && stage2Events.isEmpty() && stage3Events.isEmpty()) {
            for (int i = 0; i < allEvents.size(); i++) {
                Event ev = allEvents.get(i);
                int stage = determineEventProgressionStage(ev);
                if (stage == 1) stage1Events.add(ev);
                else if (stage == 2) stage2Events.add(ev);
                else stage3Events.add(ev);
            }
        }

        // 3. Stage 1: Foundation (Beginner)
        pnlSkillCardsContainer.add(createStageSection(
            "STAGE 1: FOUNDATION & CORE CONCEPTS (BEGINNER)",
            "Introductory workshops, fundamental bootcamps, and conceptual grounding to build your base.",
            new Color(41, 128, 185),
            stage1Events
        ));
        pnlSkillCardsContainer.add(Box.createVerticalStrut(12));

        // 4. Stage 2: Practical Application (Intermediate)
        pnlSkillCardsContainer.add(createStageSection(
            "STAGE 2: PRACTICAL APPLICATION & MINI-PROJECTS (INTERMEDIATE)",
            "Hands-on project sprints, coding challenges, and intermediate tech implementation.",
            new Color(39, 174, 96),
            stage2Events
        ));
        pnlSkillCardsContainer.add(Box.createVerticalStrut(12));

        // 5. Stage 3: Advanced Mastery & Competitions (Advanced)
        pnlSkillCardsContainer.add(createStageSection(
            "STAGE 3: ADVANCED MASTERY & PORTFOLIO (ADVANCED)",
            "High-impact hackathons, symposiums, research conclaves, and portfolio-building competitions.",
            new Color(142, 68, 173),
            stage3Events
        ));

        pnlSkillCardsContainer.revalidate();
        pnlSkillCardsContainer.repaint();
    }

    private void refreshDiscoverNew() {
        discoverTableModel.setRowCount(0);
        if (currentDiscoverEvents == null) {
            currentDiscoverEvents = new ArrayList<Event>();
        }
        currentDiscoverEvents.clear();

        ArrayList<Event> allEvents = eventDAO.getAllEvents();
        String studentInterests = currentStudent.getInterests() != null ? currentStudent.getInterests().toLowerCase() : "";

        // Collect top recommendation IDs (top 3)
        ArrayList<Integer> topRecIds = new ArrayList<Integer>();
        if (currentRecommendations != null) {
            int limit = Math.min(3, currentRecommendations.size());
            for (int i = 0; i < limit; i++) {
                topRecIds.add(currentRecommendations.get(i).getEvent().getEventId());
            }
        }

        // Collect registered IDs
        ArrayList<Integer> registeredIds = new ArrayList<Integer>();
        String userEmail = currentUser != null ? currentUser.getEmail() : "";
        ArrayList<Registration> myRegs = registrationDAO.getRegistrationsByStudent(userEmail);
        for (int i = 0; i < myRegs.size(); i++) {
            registeredIds.add(myRegs.get(i).getEventId());
        }

        for (int i = 0; i < allEvents.size(); i++) {
            Event e = allEvents.get(i);

            // 1. Exclude already registered
            if (registeredIds.contains(e.getEventId())) continue;

            // 2. Exclude top recommendations
            if (topRecIds.contains(e.getEventId())) continue;

            // 3. Exclude events matching current student's primary interests/categories
            String cat = e.getCategory() != null ? e.getCategory().toLowerCase() : "";
            if (!studentInterests.isEmpty() && studentInterests.contains(cat)) continue;

            // 4. Must be within student's budget
            if (e.getFee() > currentStudent.getMaxBudget()) continue;
            if (currentStudent.isFreeOnly() && e.getFee() > 0) continue;

            currentDiscoverEvents.add(e);
            discoverTableModel.addRow(new Object[]{
                e.getTitle(),
                e.getCategory(),
                e.getEventDate() + " (" + e.getTimeSlot() + ")",
                e.getFee() == 0 ? "FREE" : "Rs. " + e.getFee(),
                e.getVenue()
            });
        }

        if (!currentDiscoverEvents.isEmpty()) {
            discoverTable.setRowSelectionInterval(0, 0);
            updateDiscoverDetailsCard(currentDiscoverEvents.get(0));
        } else {
            txtDiscoverExplanation.setText("No cross-disciplinary events currently found matching your budget and free-only preferences.");
        }
    }

    private void refreshAllEventsCatalog() {
        allEventsTableModel.setRowCount(0);
        if (currentCatalogEvents == null) {
            currentCatalogEvents = new ArrayList<Event>();
        }
        currentCatalogEvents.clear();

        ArrayList<Event> list = eventDAO.getAllEvents();
        String query = txtCatalogSearch != null ? txtCatalogSearch.getText().trim().toLowerCase() : "";
        String catFilter = cmbCatalogCategory != null ? (String) cmbCatalogCategory.getSelectedItem() : "All Categories";
        String formatFilter = cmbCatalogFormat != null ? (String) cmbCatalogFormat.getSelectedItem() : "All Formats";

        for (int i = 0; i < list.size(); i++) {
            Event ev = list.get(i);

            // Filter by search query
            if (!query.isEmpty()) {
                String title = ev.getTitle().toLowerCase();
                String cat = ev.getCategory().toLowerCase();
                String venue = ev.getVenue().toLowerCase();
                String skills = ev.getRequiredSkills() != null ? ev.getRequiredSkills().toLowerCase() : "";
                if (!title.contains(query) && !cat.contains(query) && !venue.contains(query) && !skills.contains(query)) {
                    continue;
                }
            }

            // Filter by category
            if (catFilter != null && !catFilter.startsWith("All")) {
                if (!ev.getCategory().equalsIgnoreCase(catFilter)) {
                    continue;
                }
            }

            // Filter by format
            if (formatFilter != null && !formatFilter.startsWith("All")) {
                if (!ev.getEventFormat().equalsIgnoreCase(formatFilter)) {
                    continue;
                }
            }

            currentCatalogEvents.add(ev);
            int count = registrationDAO.getRegistrationCountForEvent(ev.getEventId());
            int remaining = Math.max(0, ev.getMaxCapacity() - count);
            String capacityStr = count + " / " + ev.getMaxCapacity() + " (" + remaining + " left)";
            if (remaining == 0) {
                capacityStr = count + " / " + ev.getMaxCapacity() + " [FULL]";
            }

            allEventsTableModel.addRow(new Object[]{
                ev.getEventId(),
                ev.getTitle(),
                ev.getCategory(),
                ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                ev.getFee() == 0 ? "FREE" : "Rs. " + ev.getFee(),
                ev.getEventFormat(),
                ev.getVenue(),
                capacityStr
            });
        }

        if (lblCatalogCount != null) {
            lblCatalogCount.setText(currentCatalogEvents.size() + " events displayed");
        }

        if (!currentCatalogEvents.isEmpty()) {
            allEventsTable.setRowSelectionInterval(0, 0);
            updateCatalogDetailsCard(currentCatalogEvents.get(0));
        } else if (txtCatalogDetails != null) {
            txtCatalogDetails.setText("No campus events found matching your filter criteria.");
        }
    }

    private void refreshMyRegistrations() {
        myRegTableModel.setRowCount(0);
        String email = currentUser != null ? currentUser.getEmail() : "";
        ArrayList<Registration> list = registrationDAO.getRegistrationsByStudent(email);
        for (int i = 0; i < list.size(); i++) {
            Registration r = list.get(i);
            String schedule = r.getEventDate() + " (" + r.getTimeSlot() + ")";
            myRegTableModel.addRow(new Object[]{
                r.getRegId(),
                r.getEventTitle(),
                schedule,
                r.getVenue(),
                r.getRegistrationDate(),
                r.getStatus()
            });
        }

        if (lblMyRegStatus != null) {
            if (list.isEmpty()) {
                lblMyRegStatus.setText("You have not enrolled in any campus events yet. Explore 'Recommended Events' or 'Browse Events' to register.");
            } else {
                lblMyRegStatus.setText("Total Enrolled Events: " + list.size() + "  |  Select any row and click 'Cancel Registration' to withdraw.");
            }
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
        String eventTitle = (String) myRegTableModel.getValueAt(row, 1);

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
            String name = prefName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Full name cannot be blank.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double budget;
            try {
                budget = Double.parseDouble(prefBudget.getText().trim());
                if (budget < 0) {
                    JOptionPane.showMessageDialog(this, "Event fee budget cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric budget.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Assemble interests from checkboxes
            ArrayList<String> interestsList = new ArrayList<String>();
            if (cbCoding != null && cbCoding.isSelected()) interestsList.add("Coding");
            if (cbAI != null && cbAI.isSelected()) interestsList.add("AI");
            if (cbRobotics != null && cbRobotics.isSelected()) interestsList.add("Robotics");
            if (cbEntrepreneurship != null && cbEntrepreneurship.isSelected()) interestsList.add("Entrepreneurship");
            if (cbDesign != null && cbDesign.isSelected()) interestsList.add("Design");
            if (cbPublicSpeaking != null && cbPublicSpeaking.isSelected()) interestsList.add("Public Speaking");
            if (cbPhotography != null && cbPhotography.isSelected()) interestsList.add("Photography");
            if (cbSports != null && cbSports.isSelected()) interestsList.add("Sports");
            if (cbMusic != null && cbMusic.isSelected()) interestsList.add("Music");
            if (cbDance != null && cbDance.isSelected()) interestsList.add("Dance");

            String interestsStr = String.join(", ", interestsList);
            if (interestsStr.isEmpty()) {
                interestsStr = "Coding";
            }

            currentStudent.setName(name);
            currentStudent.setDepartment((String) prefDept.getSelectedItem());
            currentStudent.setAcademicYear((Integer) prefYear.getSelectedItem());
            currentStudent.setMaxBudget(budget);
            currentStudent.setFreeOnly(prefFreeOnly.isSelected());
            currentStudent.setInterests(interestsStr);
            currentStudent.setSkills(prefSkills.getText().trim());
            currentStudent.setSkillLevel((String) prefSkillLevel.getSelectedItem());
            currentStudent.setAvailableSlot((String) prefSlot.getSelectedItem());
            currentStudent.setPreferredDays((String) prefDays.getSelectedItem());
            currentStudent.setCareerGoals((String) prefGoal.getSelectedItem());
            currentStudent.setPreferredFormat((String) prefFormat.getSelectedItem());

            studentDAO.saveOrUpdateStudent(currentStudent);
            JOptionPane.showMessageDialog(this,
                "Preference Profile Updated Successfully!\n\n"
                + "Selected Interests: " + interestsStr + "\n"
                + "Career Goal: " + currentStudent.getCareerGoals() + "\n"
                + "Max Budget: Rs. " + budget + " (Free Only: " + (currentStudent.isFreeOnly() ? "Yes" : "No") + ")\n\n"
                + "All recommendations and progression pathways have been dynamically recalculated.",
                "Preferences Saved",
                JOptionPane.INFORMATION_MESSAGE);

            refreshAllData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving preferences: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(30, 58, 138));

        JTableHeader th = table.getTableHeader();
        th.setPreferredSize(new Dimension(0, 34));
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(241, 245, 249));
        th.setForeground(new Color(15, 23, 42));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
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
