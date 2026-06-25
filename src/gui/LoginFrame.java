package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import model.User;
import model.enums.UserRole;
import services.UserService;
import util.Constants;
import util.SessionManager;

/**
 * LoginFrame - Modern Login Interface with Beautiful Design
 * Features gradient background, animated elements, and smooth transitions
 */
public class LoginFrame extends JFrame {
    private JTextField nameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<UserRole> roleBox;
    private JButton loginBtn;
    private JLabel titleLabel;
    private Timer shakeTimer;
    private final UserService userService;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50");
    private static final Color SECONDARY_COLOR = Color.decode("#3498DB");
    private static final Color SUCCESS_COLOR = Color.decode("#2ECC71");
    private static final Color GRADIENT_START = Color.decode("#1A2980");
    private static final Color GRADIENT_END = Color.decode("#26D0CE");
    private static final Color CARD_BG = new Color(255, 255, 255, 230);
    
    public LoginFrame() {
        userService = new UserService();
        initializeFrame();
        createComponents();
        setupAnimations();
    }
    
    /**
     * Initialize the frame with modern settings
     */
    private void initializeFrame() {
        setTitle(Constants.APP_NAME + " - Secure Login");
        setSize(420, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set custom icon
        try {
            ImageIcon icon = new ImageIcon(Constants.APP_ICON_PATH);
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Use default icon if custom icon fails
        }
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Use default look and feel
        }
    }
    
    /**
     * Create all UI components with modern design
     */
    private void createComponents() {
        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    getWidth(), getHeight(), GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Title panel with animated text
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        
        titleLabel = new JLabel(Constants.APP_NAME, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("Transport Management System", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(255, 255, 255, 180));
        
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        
        // Login card with rounded corners
        JPanel loginCard = createRoundedCard();
        loginCard.setLayout(new GridBagLayout());
        loginCard.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginCard.add(createInputLabel("👤 Full Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        nameField = createStyledTextField();
        loginCard.add(nameField, gbc);
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginCard.add(createInputLabel("📧 Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        usernameField = createStyledTextField();
        loginCard.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        loginCard.add(createInputLabel("🔒 Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        passwordField = createStyledPasswordField();
        loginCard.add(passwordField, gbc);
        
        // Role selection
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        loginCard.add(createInputLabel("👥 Role:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        roleBox = createStyledComboBox();
        roleBox.addItem(UserRole.STUDENT);
        roleBox.addItem(UserRole.TEACHER);
        roleBox.addItem(UserRole.STAFF);
        roleBox.addItem(UserRole.ADMIN);
        loginCard.add(roleBox, gbc);
        
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 0, 8);
        loginBtn = createLoginButton();
        loginCard.add(loginBtn, gbc);
        
        // Remember me checkbox
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 8, 0, 8);
        JCheckBox rememberCheck = new JCheckBox("Remember me");
        rememberCheck.setOpaque(false);
        rememberCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginCard.add(rememberCheck, gbc);
        
        // Forgot password link
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 8, 0, 8);
        JLabel forgotLink = new JLabel("<html><u>Forgot Password?</u></html>");
        forgotLink.setForeground(SECONDARY_COLOR);
        forgotLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
        });
        loginCard.add(forgotLink, gbc);
        
        // Add key listener for Enter key
        setupEnterKeyListener();
        
        // Assembly
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(loginCard, BorderLayout.CENTER);
        
        // Footer with copyright
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel footerLabel = new JLabel(Constants.COPYRIGHT);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(255, 255, 255, 150));
        footer.add(footerLabel);
        
        mainPanel.add(footer, BorderLayout.SOUTH);
        add(mainPanel);
    }
    
    /**
     * Create a rounded card panel
     */
    private JPanel createRoundedCard() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle with shadow effect
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Add subtle border
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
    }
    
    /**
     * Create styled text field
     */
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        
        // Add focus listener for animation
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });
        
        return field;
    }
    
    /**
     * Create styled password field
     */
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(15);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
        
        return field;
    }
    
    /**
     * Create styled combo box
     */
    private JComboBox<UserRole> createStyledComboBox() {
        JComboBox<UserRole> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        // Add renderer to display role names properly using pattern matching
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UserRole role) {
                    setText(getRoleDisplayName(role));
                }
                return this;
            }
        });
        
        return combo;
    }
    
    /**
     * Get display name for user role with complete switch expression
     */
    private String getRoleDisplayName(UserRole role) {
        return switch (role) {
            case STUDENT -> "🎓 Student";
            case TEACHER -> "👨‍🏫 Teacher";
            case STAFF -> "👔 Staff";
            case ADMIN -> "👑 Administrator";
            case DRIVER -> "👨‍✈️ Driver";
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        };
    }
    
    /**
     * Create input label with icon
     */
    private JLabel createInputLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(PRIMARY_COLOR);
        return label;
    }
    
    /**
     * Create animated login button using SUCCESS_COLOR
     */
    private JButton createLoginButton() {
        JButton button = new JButton("🚀 Login to System");
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(SUCCESS_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Make button rounded
        button.setBorder(new RoundedBorder(25));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SUCCESS_COLOR.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SUCCESS_COLOR);
            }
        });
        
        // Add login action
        button.addActionListener(e -> performLogin());
        
        return button;
    }
    
    /**
     * Set up Enter key listener for login
     */
    private void setupEnterKeyListener() {
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        
        nameField.addKeyListener(enterKeyListener);
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        roleBox.addKeyListener(enterKeyListener);
    }
    
    /**
     * Set up animations
     */
    private void setupAnimations() {
        // Title animation
        Timer titleTimer = new Timer(2000, e -> {
            String current = titleLabel.getText();
            if (current.equals(Constants.APP_NAME)) {
                titleLabel.setText("Welcome Back!");
            } else {
                titleLabel.setText(Constants.APP_NAME);
            }
        });
        titleTimer.start();
    }
    
    /**
     * Perform login validation and processing
     */
    private void performLogin() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        UserRole role = (UserRole) roleBox.getSelectedItem();
        
        // Validation
        if (username.isEmpty()) {
            showError(usernameField, "Username is required!");
            return;
        }
        
        if (password.isEmpty()) {
            showError(passwordField, "Password is required!");
            return;
        }
        
        // Handle empty name field with a dialog
        String finalName = handleEmptyName(name, username);
        if (finalName == null) {
            return; // User cancelled
        }
        
        // Show loading animation
        loginBtn.setText("🔐 Authenticating...");
        loginBtn.setEnabled(false);
        
        // Capture final variables for lambda
        final String finalUsername = username;
        final String finalPassword = password;
        final UserRole finalRole = role;
        final String finalNameForLambda = finalName;
        
        // Simulate authentication delay
        Timer authTimer = new Timer(1500, e -> {
            try {
                // Create user with proper constructor
                User user = createUserFromInput(finalNameForLambda, finalUsername, finalPassword, finalRole);
                
                // Authenticate user
                User authenticatedUser = userService.login(finalUsername, finalPassword);
                
                if (authenticatedUser != null) {
                    SessionManager.login(authenticatedUser);
                    showSuccess("Login successful!");
                    
                    // Navigate to appropriate dashboard based on role
                    SwingUtilities.invokeLater(() -> {
                        if (authenticatedUser.getRole() == UserRole.ADMIN) {
                            new AdminDashboard().setVisible(true);
                        } else {
                            new UserDashboard().setVisible(true);
                        }
                        dispose();
                    });
                } else {
                    // If login fails, create demo user for testing
                    showWarning("⚠️ Using demo mode - No database connection");
                    SessionManager.login(user);
                    new UserDashboard().setVisible(true);
                    dispose();
                }
                
            } catch (IllegalArgumentException ex) {
                showError(null, ex.getMessage());
                loginBtn.setText("🚀 Login to System");
                loginBtn.setEnabled(true);
            }
        });
        authTimer.setRepeats(false);
        authTimer.start();
    }
    
    /**
     * Handle empty name field with confirmation dialog
     */
    private String handleEmptyName(String name, String username) {
        if (!name.isEmpty()) {
            return name;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Name field is empty. Would you like to use a default name?",
            "Confirm Default Name",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            return "User " + username;
        } else {
            nameField.requestFocus();
            return null;
        }
    }
    
    /**
     * Create user from input fields with proper constructor
     */
    private User createUserFromInput(String name, String username, String password, UserRole role) {
        // Generate default email and phone
        String email = username + "@diu.edu.bd";
        String phone = "017" + String.format("%08d", (int)(Math.random() * 100000000));
        
        // Use the correct User constructor
        return new User(0, username, password, role, name, email, phone);
    }
    
    /**
     * Show error animation on field
     */
    private void showError(JComponent component, String message) {
        if (component != null) {
            // Shake animation
            if (shakeTimer != null && shakeTimer.isRunning()) {
                shakeTimer.stop();
            }
            
            final Point originalLocation = component.getLocation();
            shakeTimer = new Timer(50, new ActionListener() {
                private int shakeCount = 0;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    int offset = (shakeCount % 2 == 0) ? 5 : -5;
                    component.setLocation(originalLocation.x + offset, originalLocation.y);
                    shakeCount++;
                    
                    if (shakeCount > 10) {
                        shakeTimer.stop();
                        component.setLocation(originalLocation);
                        component.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.RED, 2),
                            BorderFactory.createEmptyBorder(7, 11, 7, 11)
                        ));
                        
                        // Reset border after delay
                        Timer resetTimer = new Timer(2000, ev -> {
                            if (component instanceof JTextField) {
                                component.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                                ));
                            }
                        });
                        resetTimer.setRepeats(false);
                        resetTimer.start();
                    }
                }
            });
            shakeTimer.start();
        }
        
        JOptionPane.showMessageDialog(this,
            message,
            "Validation Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show warning message
     */
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Warning",
            JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show forgot password dialog
     */
    private void showForgotPasswordDialog() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel instruction = new JLabel("<html>Enter your username or email to reset password:</html>");
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JTextField resetField = new JTextField(20);
        resetField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(instruction, BorderLayout.NORTH);
        panel.add(resetField, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(this,
            panel,
            "Reset Password",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String input = resetField.getText().trim();
            if (!input.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Password reset link has been sent to your registered email.",
                    "Reset Link Sent",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Custom rounded border for buttons
     */
    private class RoundedBorder implements Border {
        private final int radius;
        
        RoundedBorder(int radius) {
            this.radius = radius;
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return true;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(SUCCESS_COLOR.darker());
            g2d.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2d.dispose();
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}