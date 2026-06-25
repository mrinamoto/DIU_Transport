package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.Pattern;
import javax.swing.*;
import model.*;
import model.enums.UserRole;
import services.UserService;
import util.Constants;

/**
 * RegisterFrame - Modern User Registration Form
 * Features gradient backgrounds, animated elements, and real-time validation
 */
public class RegisterFrame extends JFrame {
    private final UserService userService;
    private JTextField nameField, usernameField, emailField, phoneField, departmentField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<UserRole> roleComboBox;
    private JComboBox<String> semesterComboBox;
    private JCheckBox agreeCheckBox;
    private JLabel validationLabel;
    private Timer validationTimer;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50");
    private static final Color SECONDARY_COLOR = Color.decode("#3498DB");
    private static final Color SUCCESS_COLOR = Color.decode("#2ECC71");
    private static final Color WARNING_COLOR = Color.decode("#F39C12");
    private static final Color DANGER_COLOR = Color.decode("#E74C3C");
    private static final Color CARD_BG = new Color(255, 255, 255, 240);
    private static final Color GLOW_COLOR = new Color(52, 152, 219, 50);
    
    // Validation patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^01[3-9]\\d{8}$");
    
    public RegisterFrame() {
        this.userService = new UserService();
        initializeFrame();
        createComponents();
        setupValidations();
    }
    
    private void initializeFrame() {
        setTitle(Constants.APP_NAME + " 🚍 Registration");
        setSize(600, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set custom icon
        try {
            ImageIcon icon = new ImageIcon(Constants.APP_ICON_PATH);
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Could not load app icon: " + e.getMessage());
        }
        
        // Set modern look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Could not set Nimbus look and feel: " + e.getMessage());
        }
    }
    
    private void createComponents() {
        // Main panel with animated gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Animated gradient
                Color startColor = new Color(26, 41, 128);
                Color endColor = new Color(38, 208, 206);
                
                float shift = (System.currentTimeMillis() % 6283) / 1000.0f; // 2π * 1000 ≈ 6283
                
                GradientPaint gradient = new GradientPaint(
                    0, (float) (Math.sin(shift) * 100) + getHeight()/2, 
                    startColor,
                    getWidth(), (float) (Math.cos(shift) * 100) + getHeight()/2, 
                    endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add floating particles
                g2d.setColor(new Color(255, 255, 255, 20));
                for (int i = 0; i < 20; i++) {
                    int x = (int) ((Math.sin(shift + i) * 100 + i * 50) % getWidth());
                    int y = (int) ((Math.cos(shift + i) * 100 + i * 30) % getHeight());
                    int size = 2 + (i % 3);
                    g2d.fillOval(x, y, size, size);
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Animation timer for background
        Timer bgTimer = new Timer(100, e -> mainPanel.repaint());
        bgTimer.start();
        
        // Header panel with glow effect
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glow effect
                for (int i = 0; i < 5; i++) {
                    g2d.setColor(new Color(52, 152, 219, 10 - i * 2));
                    g2d.fillRoundRect(i, i, getWidth() - 2*i, getHeight() - 2*i, 20, 20);
                }
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 15, 0));
        
        JLabel headerLabel = new JLabel("✨ Create New Account", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Join DIU Transport System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // Registration card with glow
        JPanel cardPanel = createRoundedCard();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Form panel with scrolling
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Name field
        addStyledField(formPanel, gbc, row++, "👤 Full Name:", 
                      nameField = createStyledTextField(), "Enter your full name");
        
        // Username field
        addStyledField(formPanel, gbc, row++, "📝 Username:", 
                      usernameField = createStyledTextField(), "3-20 alphanumeric characters");
        
        // Password field
        JPanel passwordPanel = createPasswordPanel();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(createFieldLabel("🔒 Password:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(passwordPanel, gbc);
        row++;
        
        // Confirm Password field
        JPanel confirmPasswordPanel = createConfirmPasswordPanel();
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createFieldLabel("✅ Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(confirmPasswordPanel, gbc);
        row++;
        
        // Email field
        addStyledField(formPanel, gbc, row++, "📧 Email:", 
                      emailField = createStyledTextField(), "user@diu.edu.bd");
        
        // Phone field
        addStyledField(formPanel, gbc, row++, "📱 Phone:", 
                      phoneField = createStyledTextField(), "01XXXXXXXXX");
        
        // Role selection
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createFieldLabel("👥 User Role:"), gbc);
        
        roleComboBox = createStyledComboBox();
        roleComboBox.addItem(UserRole.STUDENT);
        roleComboBox.addItem(UserRole.TEACHER);
        roleComboBox.addItem(UserRole.STAFF);
        roleComboBox.addActionListener(e -> updateRoleSpecificFields());
        
        gbc.gridx = 1;
        formPanel.add(roleComboBox, gbc);
        row++;
        
        // Department field
        addStyledField(formPanel, gbc, row++, "🎓 Department:", 
                      departmentField = createStyledTextField(), "e.g., CSE, EEE, BBA");
        
        // Semester selection (only for students)
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(createFieldLabel("📚 Semester:"), gbc);
        
        String[] semesters = new String[12];
        for (int i = 1; i <= 12; i++) {
            semesters[i-1] = "Semester " + i;
        }
        semesterComboBox = new JComboBox<>(semesters);
        styleComboBox(semesterComboBox);
        
        gbc.gridx = 1;
        formPanel.add(semesterComboBox, gbc);
        row++;
        
        // Terms and conditions
        agreeCheckBox = new JCheckBox("<html>✅ I agree to the <font color='#3498DB'><u>terms and conditions</u></font></html>");
        agreeCheckBox.setOpaque(false);
        agreeCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        agreeCheckBox.setForeground(PRIMARY_COLOR);
        agreeCheckBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(agreeCheckBox, gbc);
        row++;
        
        // Validation label
        validationLabel = new JLabel(" ", SwingConstants.CENTER);
        validationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        validationLabel.setForeground(WARNING_COLOR);
        
        gbc.gridy = row;
        gbc.insets = new Insets(10, 10, 20, 10);
        formPanel.add(validationLabel, gbc);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        cardPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton registerButton = createStyledButton("🚀 Create Account", SUCCESS_COLOR);
        registerButton.addActionListener(e -> performRegistration());
        
        JButton cancelButton = createStyledButton("← Back to Login", SECONDARY_COLOR);
        cancelButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        cardPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Assembly
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        add(mainPanel);
        
        updateRoleSpecificFields(); // Set initial state
        
        // Add window listener to stop timers
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                bgTimer.stop();
            }
        });
    }
    
    private JPanel createRoundedCard() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Main card
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Glow effect
                g2d.setColor(GLOW_COLOR);
                for (int i = 1; i <= 3; i++) {
                    g2d.drawRoundRect(i, i, getWidth() - 2*i - 1, getHeight() - 2*i - 1, 25, 25);
                }
            }
        };
    }
    
    private void addStyledField(JPanel panel, GridBagConstraints gbc, int row, 
                               String labelText, JComponent field, String placeholder) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(createFieldLabel(labelText), gbc);
        
        gbc.gridx = 1;
        if (field instanceof JTextField textField) {
            textField.setToolTipText(placeholder);
        }
        panel.add(field, gbc);
    }
    
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(PRIMARY_COLOR);
        label.setIconTextGap(10);
        return label;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(new Color(250, 250, 250));
        
        // Add hover effect
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                field.setBackground(new Color(255, 255, 255));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                field.setBackground(new Color(250, 250, 250));
            }
        });
        
        // Add focus listener for validation
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                validateField(field);
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        return field;
    }
    
    private JPanel createPasswordPanel() {
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        passwordField.setBackground(new Color(250, 250, 250));
        
        // Add password toggle button
        JButton toggleBtn = new JButton("👁");
        toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toggleBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        toggleBtn.setBackground(new Color(240, 240, 240));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        toggleBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == '•') {
                passwordField.setEchoChar((char) 0);
                toggleBtn.setText("🙈");
            } else {
                passwordField.setEchoChar('•');
                toggleBtn.setText("👁");
            }
        });
        
        // Add focus listener
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                validatePassword();
                passwordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        // Create wrapper panel
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(250, 250, 250));
        wrapper.setBorder(passwordField.getBorder());
        wrapper.add(passwordField, BorderLayout.CENTER);
        wrapper.add(toggleBtn, BorderLayout.EAST);
        passwordField.setBorder(null);
        
        return wrapper;
    }
    
    private JPanel createConfirmPasswordPanel() {
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        confirmPasswordField.setBackground(new Color(250, 250, 250));
        
        // Add focus listener
        confirmPasswordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(SECONDARY_COLOR, 2),
                    BorderFactory.createEmptyBorder(9, 14, 9, 14)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                validatePassword();
                confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(250, 250, 250));
        wrapper.setBorder(confirmPasswordField.getBorder());
        wrapper.add(confirmPasswordField, BorderLayout.CENTER);
        confirmPasswordField.setBorder(null);
        
        return wrapper;
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(250, 250, 250));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
    }
    
    private JComboBox<UserRole> createStyledComboBox() {
        JComboBox<UserRole> combo = new JComboBox<>();
        styleComboBox(combo);
        
        // Custom renderer with icons
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof UserRole role) {
                    setText(getRoleDisplayName(role));
                    setIcon(getRoleIcon(role));
                }
                return this;
            }
        });
        
        return combo;
    }
    
    private Icon getRoleIcon(UserRole role) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color iconColor = switch (role) {
                    case STUDENT -> new Color(41, 128, 185);
                    case TEACHER -> new Color(39, 174, 96);
                    case STAFF -> new Color(142, 68, 173);
                    case ADMIN -> new Color(230, 126, 34);
                    case DRIVER -> new Color(231, 76, 60);
                    default -> new Color(127, 140, 141);
                };
                
                g2d.setColor(iconColor);
                g2d.fillOval(x, y, getIconWidth(), getIconHeight());
            }
            
            @Override
            public int getIconWidth() {
                return 12;
            }
            
            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, bgColor,
                    0, getHeight(), bgColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Border
                g2d.setColor(bgColor.darker().darker());
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);
                
                // Text
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                
                // Text shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawString(getText(), x + 1, y + 1);
                
                // Main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(15, 35, 15, 35));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    private String getRoleDisplayName(UserRole role) {
        return switch (role) {
            case STUDENT -> "🎓 Student";
            case TEACHER -> "👨‍🏫 Teacher";
            case STAFF -> "👔 Staff";
            case ADMIN -> "👑 Administrator";
            case DRIVER -> "👨‍✈️ Driver";
            default -> "User";
        };
    }
    
    private void updateRoleSpecificFields() {
        UserRole role = (UserRole) roleComboBox.getSelectedItem();
        departmentField.setEnabled(true);
        
        switch (role) {
            case STUDENT -> {
                semesterComboBox.setEnabled(true);
                departmentField.setText("Computer Science & Engineering");
            }
            case TEACHER -> {
                semesterComboBox.setEnabled(false);
                departmentField.setText("Faculty Department");
            }
            case STAFF -> {
                semesterComboBox.setEnabled(false);
                departmentField.setText("Administration");
            }
            default -> {
                semesterComboBox.setEnabled(false);
                departmentField.setText("");
            }
        }
    }
    
    private void setupValidations() {
        // Real-time validation timer
        validationTimer = new Timer(500, e -> {
            if (validateAllFields()) {
                validationLabel.setText("✅ All fields are valid!");
                validationLabel.setForeground(SUCCESS_COLOR);
            }
        });
        validationTimer.start();
    }
    
    private void performRegistration() {
        // Validate all fields
        if (!validateAllFields()) {
            JOptionPane.showMessageDialog(this,
                "Please fix all validation errors before submitting.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!agreeCheckBox.isSelected()) {
            showValidationError("You must agree to the terms and conditions!");
            return;
        }

        try {
            UserRole role = (UserRole) roleComboBox.getSelectedItem();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String department = departmentField.getText().trim();
            
            // Create final variables for lambda expression
            final String finalUsername = username;
            final String finalPassword = password;
            final String finalName = name;
            final String finalEmail = email;
            final String finalPhone = phone;
            final String finalDepartment = department;
            final UserRole finalRole = role;
            
            // Show loading animation
            JButton registerBtn = (JButton) ((JPanel) ((JPanel) getContentPane().getComponent(0))
                .getComponent(1)).getComponent(1);
            String originalText = registerBtn.getText();
            registerBtn.setText("⏳ Processing...");
            registerBtn.setEnabled(false);

            // Simulate registration delay
            Timer registrationTimer = new Timer(1500, e -> {
                try {
                    // Create user based on role using switch statement
                    User user = switch (finalRole) {
                        case STUDENT -> {
                            int semester = semesterComboBox.getSelectedIndex() + 1;
                            // Using simpler constructor - adjust parameters based on your actual model
                            yield new Student(
                                0, // id
                                finalUsername,
                                finalPassword,
                                finalName,
                                finalEmail,
                                finalPhone,
                                finalDepartment,
                                semester
                            );
                        }
                        case TEACHER -> 
                            // Using simpler constructor
                            new Teacher(
                                0, // id
                                finalUsername,
                                finalPassword,
                                finalName,
                                finalEmail,
                                finalPhone,
                                finalDepartment
                            );
                        case STAFF -> 
                            // Using simpler constructor
                            new Staff(
                                0, // id
                                finalUsername,
                                finalPassword,
                                finalName,
                                finalEmail,
                                finalPhone,
                                finalDepartment
                            );
                        default -> throw new IllegalArgumentException("Invalid role: " + finalRole);
                    };
                    
                    // The switch expression will always return a User object or throw an exception
                    user.setRole(finalRole); // Set role separately
                    
                    if (userService.register(user)) {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                            """
                            ✅ Registration successful!
                            
                            Welcome to DIU Transport System.
                            You can now login with your credentials.
                            """,
                            "Registration Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Navigate to login
                        SwingUtilities.invokeLater(() -> {
                            new LoginFrame().setVisible(true);
                            dispose();
                        });
                    } else {
                        JOptionPane.showMessageDialog(RegisterFrame.this,
                            """
                            ⚠️ Registration failed!
                            
                            Username or email might already exist.
                            Please try with different credentials.
                            """,
                            "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                        registerBtn.setText(originalText);
                        registerBtn.setEnabled(true);
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        "❌ Error: " + ex.getMessage(),
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                    registerBtn.setText(originalText);
                    registerBtn.setEnabled(true);
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(RegisterFrame.this,
                        "❌ Runtime Error: " + ex.getMessage(),
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                    registerBtn.setText(originalText);
                    registerBtn.setEnabled(true);
                }
            });
            registrationTimer.setRepeats(false);
            registrationTimer.start();

        } catch (IllegalArgumentException ex) {
            showValidationError("Error: " + ex.getMessage());
        }
    }
    
    private boolean validateAllFields() {
        return validateField(nameField, "Name is required!", true) &&
               validateField(usernameField, "Invalid username!", USERNAME_PATTERN) &&
               validatePassword() &&
               validateField(emailField, "Invalid email!", EMAIL_PATTERN) &&
               validateField(phoneField, "Invalid phone number!", PHONE_PATTERN) &&
               validateField(departmentField, "Department is required!", true);
    }
    
    private boolean validateField(JTextField field) {
        return validateField(field, "", USERNAME_PATTERN);
    }
    
    private boolean validateField(JTextField field, String errorMessage, Pattern pattern) {
        String text = field.getText().trim();
        boolean isValid = !text.isEmpty() && pattern.matcher(text).matches();
        
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isValid ? SUCCESS_COLOR : DANGER_COLOR, 2),
            BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        
        if (!isValid && !errorMessage.isEmpty() && !text.isEmpty()) {
            validationLabel.setText("⚠️ " + errorMessage);
            validationLabel.setForeground(WARNING_COLOR);
        }
        
        return isValid;
    }
    
    private boolean validateField(JTextField field, String errorMessage, boolean required) {
        String text = field.getText().trim();
        boolean isValid = !required || !text.isEmpty();
        
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isValid ? SUCCESS_COLOR : DANGER_COLOR, 2),
            BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        
        if (!isValid && !errorMessage.isEmpty()) {
            validationLabel.setText("⚠️ " + errorMessage);
            validationLabel.setForeground(WARNING_COLOR);
        }
        
        return isValid;
    }
    
    private boolean validatePassword() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        boolean passwordValid = PASSWORD_PATTERN.matcher(password).matches();
        boolean passwordsMatch = password.equals(confirmPassword);
        
        // Update password field border
        Component passwordComponent = ((JPanel) passwordField.getParent()).getComponent(0);
        if (passwordComponent instanceof JPasswordField pwdField) {
            pwdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(passwordValid ? SUCCESS_COLOR : DANGER_COLOR, 2),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
            ));
        }
        
        // Update confirm password field border
        Component confirmComponent = ((JPanel) confirmPasswordField.getParent()).getComponent(0);
        if (confirmComponent instanceof JPasswordField confirmPwdField) {
            confirmPwdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(passwordsMatch ? SUCCESS_COLOR : DANGER_COLOR, 2),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
            ));
        }
        
        if (!passwordValid) {
            validationLabel.setText("⚠️ Password must be 6-20 chars with letters & numbers!");
            validationLabel.setForeground(WARNING_COLOR);
            return false;
        }
        
        if (!passwordsMatch) {
            validationLabel.setText("⚠️ Passwords do not match!");
            validationLabel.setForeground(WARNING_COLOR);
            return false;
        }
        
        return true;
    }
    
    private void showValidationError(String message) {
        validationLabel.setText("❌ " + message);
        validationLabel.setForeground(DANGER_COLOR);
        
        // Shake animation
        Point originalLocation = validationLabel.getLocation();
        Timer shakeTimer = new Timer(50, new ActionListener() {
            private int shakeCount = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                int offset = (shakeCount % 2 == 0) ? 5 : -5;
                validationLabel.setLocation(originalLocation.x + offset, originalLocation.y);
                shakeCount++;
                
                if (shakeCount > 10) {
                    ((Timer) e.getSource()).stop();
                    validationLabel.setLocation(originalLocation);
                }
            }
        });
        shakeTimer.start();
    }
    
    @Override
    public void dispose() {
        if (validationTimer != null) {
            validationTimer.stop();
        }
        super.dispose();
    }
}