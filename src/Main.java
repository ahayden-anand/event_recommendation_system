import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Main.java - Clean SaaS-Style Login Screen
 * Serves as the primary desktop entry point for the Campus Event Recommendation System.
 * Features:
 * 1. Centered SaaS card layout with white background, navy typography, and subtle blue accent.
 * 2. Input validation for username/email and password.
 * 3. Automatic MySQL connection initialization without repeated credential popups.
 * 4. Silent status indicator for live database connectivity.
 */
public class Main extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatusBadge;
    private UserDAO userDAO;

    public Main() {
        // Initialize database tables & default accounts automatically
        DBConnection.initializeDatabase();
        userDAO = new UserDAO();

        // Window properties
        setTitle("Campus Event Recommendation System - Sign In");
        setSize(500, 560);
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Root container background (Light grey SaaS canvas)
        getContentPane().setBackground(new Color(244, 246, 249));
        setLayout(new GridBagLayout()); // Centers the card panel

        initUI();
    }

    private void initUI() {
        // Centered White SaaS Card
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            BorderFactory.createEmptyBorder(35, 40, 35, 40)
        ));
        card.setPreferredSize(new Dimension(400, 460));

        // 1. App Title (Navy)
        JLabel lblTitle = new JLabel("Campus Event Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(26, 37, 48));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);

        card.add(Box.createRigidArea(new Dimension(0, 6)));

        // 2. Subtitle
        JLabel lblSubtitle = new JLabel("Discover campus events that match your interests.");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(127, 140, 141));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSubtitle);

        card.add(Box.createRigidArea(new Dimension(0, 28)));

        // 3. Username / Email Label & Field
        JLabel lblUser = new JLabel("Username or Email");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(44, 62, 80));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblUser);

        card.add(Box.createRigidArea(new Dimension(0, 6)));

        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(txtUsername);

        card.add(Box.createRigidArea(new Dimension(0, 18)));

        // 4. Password Label & Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(44, 62, 80));
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblPass);

        card.add(Box.createRigidArea(new Dimension(0, 6)));

        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Enable pressing Enter key to submit
        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
        card.add(txtPassword);

        card.add(Box.createRigidArea(new Dimension(0, 24)));

        // 5. Restrained Blue SaaS Sign-In Button
        btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(41, 128, 185)); // Single professional accent blue
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        card.add(btnLogin);

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Create Student Account Button
        JButton btnRegister = new JButton("Create Student Account");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(Color.WHITE);
        btnRegister.setForeground(new Color(41, 128, 185)); // Accent blue text
        btnRegister.setFocusPainted(false);
        btnRegister.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnRegister.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new StudentRegistrationFrame().setVisible(true);
                dispose(); // Close login window
            }
        });
        card.add(btnRegister);

        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // 6. Supported Roles Notice
        JLabel lblRolesHint = new JLabel("Supports Student and Event Coordinator accounts");
        lblRolesHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRolesHint.setForeground(new Color(149, 165, 166));
        lblRolesHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblRolesHint);

        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // 7. Silent Database Status Badge
        boolean dbOk = DBConnection.isConnectionAvailable();
        lblStatusBadge = new JLabel(dbOk ? "● MySQL Database: Connected" : "● Mode: Standalone In-Memory");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatusBadge.setForeground(dbOk ? new Color(39, 174, 96) : new Color(230, 126, 34));
        lblStatusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblStatusBadge);

        add(card); // Add card to centered GridBagLayout
    }

    /**
     * Validates input fields and authenticates the user.
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // 1. Validation: Empty inputs
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both your username/email and password.",
                "Missing Credentials",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Authenticate using UserDAO
        User user = userDAO.authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password.\nPlease check your credentials and try again.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
            return;
        }

        // 3. Successful Login - Route to the appropriate dashboard
        openDashboardForUser(user);
    }

    /**
     * Opens the dashboard matching the authenticated user's role.
     */
    private void openDashboardForUser(User user) {
        if (user.isStudent()) {
            StudentDashboard studentDashboard = new StudentDashboard(
                new EventDAO(),
                new RegistrationDAO(),
                new StudentDAO(),
                user
            );
            studentDashboard.setVisible(true);
            this.dispose(); // Close login window
        } else if (user.isCoordinator()) {
            EventManagerDashboard coordinatorDashboard = new EventManagerDashboard(
                new EventDAO(),
                new RegistrationDAO(),
                user
            );
            coordinatorDashboard.setVisible(true);
            this.dispose(); // Close login window
        } else {
            JOptionPane.showMessageDialog(this,
                "Unrecognized role: " + user.getRole(),
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Main loginFrame = new Main();
                loginFrame.setVisible(true);
            }
        });
    }
}
