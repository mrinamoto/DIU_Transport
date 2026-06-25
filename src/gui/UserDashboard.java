package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import model.User;
import model.enums.UserRole;
import util.Constants;
import util.SessionManager;

/**
 * UserDashboard - Modern User Interface with Beautiful Design
 * Features gradient backgrounds, rounded cards, and interactive elements
 */
public class UserDashboard extends JFrame {
    private JTabbedPane tabbedPane;
    private Timer dashboardTimer;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = Color.decode("#2C3E50");
    private static final Color SECONDARY_COLOR = Color.decode("#3498DB");
    private static final Color SUCCESS_COLOR = Color.decode("#2ECC71");
    private static final Color WARNING_COLOR = Color.decode("#F39C12");
    private static final Color DANGER_COLOR = Color.decode("#E74C3C");
    private static final Color GRADIENT_START = Color.decode("#1A2980");
    private static final Color GRADIENT_END = Color.decode("#26D0CE");
    private static final Color CARD_BG = new Color(255, 255, 255, 230); // Now used
    private static final Color LIGHT_BG = Color.decode("#ECF0F1");
    private static final Color DARK_BG = Color.decode("#34495E"); // Added missing constant
    
    public UserDashboard() {
        initializeFrame();
        createComponents();
        setupAnimations();
    }
    
    private void initializeFrame() {
        setTitle(Constants.APP_NAME + " 🚍 User Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set custom icon
        try {
            ImageIcon icon = new ImageIcon(Constants.APP_ICON_PATH);
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Use default icon
        }
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Use default look and feel
        }
    }
    
    private void createComponents() {
        // Create modern menu bar
        createModernMenuBar();
        
        // Header with gradient
        JPanel headerPanel = createGradientHeader();
        
        // Create modern tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add tabs with modern panels
        tabbedPane.addTab("🚌 Schedule", createModernSchedulePanel());
        tabbedPane.addTab("💳 Transport Card", createModernTransportCardPanel());
        tabbedPane.addTab("💰 Billing", createModernBillingPanel());
        tabbedPane.addTab("🔍 Lost & Found", createModernLostFoundPanel());
        tabbedPane.addTab("📞 Contact", createModernContactPanel());
        tabbedPane.addTab("👤 Profile", createModernProfilePanel());
        
        // Custom tab renderer
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, 
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    g2d.setColor(SECONDARY_COLOR);
                } else {
                    g2d.setColor(LIGHT_BG);
                }
                
                g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 15, 15);
                g2d.dispose();
            }
        });
        
        // Set layout
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(createModernStatusBar(), BorderLayout.SOUTH);
    }
    
    private JPanel createGradientHeader() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    getWidth(), 0, GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(getWidth(), 120));
        
        // Left side: Welcome message
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        leftPanel.add(welcomeLabel);
        
        // Right side: User info and quick actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightPanel.setOpaque(false);
        
        // User info card - Now using CARD_BG
        JPanel userCard = createRoundedPanel(15, CARD_BG);
        userCard.setLayout(new BorderLayout(10, 5));
        userCard.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        User currentUser = SessionManager.getCurrentUser();
        String userName = currentUser != null ? currentUser.getName() : "User";
        UserRole userRole = currentUser != null ? currentUser.getRole() : UserRole.STUDENT;
        
        JLabel userNameLabel = new JLabel(userName);
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userNameLabel.setForeground(PRIMARY_COLOR);
        
        JLabel userRoleLabel = new JLabel(getRoleDisplayName(userRole));
        userRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userRoleLabel.setForeground(SECONDARY_COLOR);
        
        userCard.add(userNameLabel, BorderLayout.NORTH);
        userCard.add(userRoleLabel, BorderLayout.SOUTH);
        rightPanel.add(userCard);
        
        // Quick logout button
        JButton logoutBtn = createStyledButton("🚪 Logout", DANGER_COLOR);
        logoutBtn.addActionListener(e -> logout());
        rightPanel.add(logoutBtn);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private void createModernMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // File Menu
        JMenu fileMenu = createStyledMenu("📁 File", PRIMARY_COLOR);
        addStyledMenuItem(fileMenu, "🔄 Refresh", e -> refreshDashboard());
        addStyledMenuItem(fileMenu, "👤 My Profile", e -> tabbedPane.setSelectedIndex(5));
        fileMenu.addSeparator();
        addStyledMenuItem(fileMenu, "🚪 Logout", e -> logout());
        
        // Tools Menu
        JMenu toolsMenu = createStyledMenu("🛠️ Tools", SECONDARY_COLOR);
        addStyledMenuItem(toolsMenu, "🔔 Notifications", e -> showNotifications());
        addStyledMenuItem(toolsMenu, "📊 Usage Stats", e -> showUsageStats());
        addStyledMenuItem(toolsMenu, "🔍 Search", e -> showSearch());
        
        // Help Menu
        JMenu helpMenu = createStyledMenu("❓ Help", SUCCESS_COLOR);
        addStyledMenuItem(helpMenu, "📖 User Guide", e -> showUserGuide());
        addStyledMenuItem(helpMenu, "ℹ️ About", e -> showAbout());
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JPanel createModernSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(LIGHT_BG);
        
        // Title
        JLabel titleLabel = new JLabel("🚌 Daily Bus Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setOpaque(false);
        
        JComboBox<String> routeFilter = new JComboBox<>(new String[]{"All Routes", "Mirpur to DIU", "Uttara to DIU", "Dhanmondi to DIU"});
        JComboBox<String> dayFilter = new JComboBox<>(new String[]{"All Days", "Today", "Weekdays", "Weekends"});
        JButton refreshBtn = createStyledButton("🔄 Refresh", SECONDARY_COLOR);
        
        filterPanel.add(new JLabel("Route:"));
        filterPanel.add(routeFilter);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("Day:"));
        filterPanel.add(dayFilter);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(refreshBtn);
        
        panel.add(filterPanel, BorderLayout.CENTER);
        
        // Schedule table
        String[] columnNames = {"Bus No", "Route", "Departure", "Arrival", "Days", "Status"};
        Object[][] data = {
            {"DIU-101", "Mirpur to DIU", "08:00 AM", "09:00 AM", "Mon-Fri", "🟢 On Time"},
            {"DIU-102", "Uttara to DIU", "08:30 AM", "09:30 AM", "Mon-Fri", "🟢 On Time"},
            {"DIU-103", "Dhanmondi to DIU", "09:00 AM", "10:00 AM", "Mon-Fri", "🟡 Delayed"},
            {"DIU-104", "Mohakhali to DIU", "09:30 AM", "10:30 AM", "Mon-Fri", "🟢 On Time"},
            {"DIU-105", "Banani to DIU", "10:00 AM", "11:00 AM", "Mon-Fri", "🟢 On Time"},
            {"DIU-106", "Gulshan to DIU", "10:30 AM", "11:30 AM", "Mon-Fri", "🔴 Cancelled"}
        };
        
        JTable scheduleTable = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable.setRowHeight(40);
        scheduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        scheduleTable.getTableHeader().setBackground(PRIMARY_COLOR);
        scheduleTable.getTableHeader().setForeground(Color.WHITE);
        scheduleTable.setGridColor(Color.LIGHT_GRAY);
        
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Info panel - Using CARD_BG
        JPanel infoPanel = createRoundedPanel(10, CARD_BG);
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JLabel infoLabel = new JLabel("💡 Tip: Click on a bus number to view details and track location");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(SECONDARY_COLOR);
        infoPanel.add(infoLabel);
        
        // Add components
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, infoPanel);
        splitPane.setDividerLocation(400);
        splitPane.setOpaque(false);
        
        panel.add(splitPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createModernTransportCardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(LIGHT_BG);
        
        // Title
        JLabel titleLabel = new JLabel("💳 My Transport Card");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Card visualization - Using CARD_BG
        JPanel cardVisual = createRoundedPanel(20, CARD_BG);
        cardVisual.setLayout(new BorderLayout());
        cardVisual.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 3),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Card header with gradient
        JPanel cardHeader = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, GRADIENT_START,
                    getWidth(), 0, GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        cardHeader.setPreferredSize(new Dimension(0, 60));
        cardHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel universityLabel = new JLabel("DIU TRANSPORT CARD");
        universityLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        universityLabel.setForeground(Color.WHITE);
        cardHeader.add(universityLabel, BorderLayout.WEST);
        
        JLabel cardTypeLabel = new JLabel("STUDENT");
        cardTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardTypeLabel.setForeground(Color.WHITE);
        cardHeader.add(cardTypeLabel, BorderLayout.EAST);
        
        // Card body
        JPanel cardBody = new JPanel(new GridLayout(4, 2, 15, 15));
        cardBody.setOpaque(false);
        cardBody.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        
        User currentUser = SessionManager.getCurrentUser();
        String userName = currentUser != null ? currentUser.getName() : "User";
        String userId = currentUser != null ? currentUser.getUsername() : "N/A";
        
        cardBody.add(createCardField("Card ID:", "TC001"));
        cardBody.add(createCardField("Holder:", userName));
        cardBody.add(createCardField("Student ID:", userId));
        cardBody.add(createCardField("Status:", "🟢 ACTIVE"));
        cardBody.add(createCardField("Issued:", "Jan 01, 2024"));
        cardBody.add(createCardField("Expires:", "Dec 31, 2024"));
        cardBody.add(createCardField("Balance:", "5000 BDT"));
        cardBody.add(createCardField("Trips Today:", "2/4"));
        
        // Assemble card
        cardVisual.add(cardHeader, BorderLayout.NORTH);
        cardVisual.add(cardBody, BorderLayout.CENTER);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionPanel.setOpaque(false);
        
        JButton renewBtn = createStyledButton("🔄 Renew Card", SUCCESS_COLOR);
        JButton reportBtn = createStyledButton("🚨 Report Issue", WARNING_COLOR);
        JButton historyBtn = createStyledButton("📜 View History", SECONDARY_COLOR);
        
        actionPanel.add(renewBtn);
        actionPanel.add(reportBtn);
        actionPanel.add(historyBtn);
        
        cardVisual.add(actionPanel, BorderLayout.SOUTH);
        
        // QR Code placeholder - Using CARD_BG
        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setOpaque(false);
        qrPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel qrLabel = new JLabel("📱 SCAN HERE", SwingConstants.CENTER);
        qrLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        qrLabel.setForeground(PRIMARY_COLOR);
        qrLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        qrLabel.setPreferredSize(new Dimension(150, 150));
        qrLabel.setOpaque(true);
        qrLabel.setBackground(CARD_BG);
        
        qrPanel.add(qrLabel, BorderLayout.CENTER);
        
        // Main layout
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cardVisual, qrPanel);
        mainSplit.setDividerLocation(600);
        mainSplit.setOpaque(false);
        
        panel.add(mainSplit, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCardField(String label, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(false);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelComp.setForeground(Color.GRAY);
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueComp.setForeground(PRIMARY_COLOR);
        
        fieldPanel.add(labelComp, BorderLayout.NORTH);
        fieldPanel.add(valueComp, BorderLayout.CENTER);
        
        return fieldPanel;
    }
    
    private JPanel createModernBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(LIGHT_BG);
        
        // Title
        JLabel titleLabel = new JLabel("💰 Billing & Payments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Summary cards - Using CARD_BG
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        summaryPanel.add(createSummaryCard("Total Due", "5000 BDT", DANGER_COLOR, "💰"));
        summaryPanel.add(createSummaryCard("Last Payment", "5000 BDT", SUCCESS_COLOR, "✅"));
        summaryPanel.add(createSummaryCard("Next Due", "15 Jan 2024", WARNING_COLOR, "📅"));
        
        panel.add(summaryPanel, BorderLayout.CENTER);
        
        // Bills table
        String[] columnNames = {"Bill ID", "Semester", "Amount", "Due Date", "Status", "Action"};
        Object[][] data = {
            {"BILL001", "Spring 2024", "5000.00", "2024-01-15", "✅ Paid", "View"},
            {"BILL002", "Summer 2024", "5000.00", "2024-05-15", "⚠️ Due", "Pay Now"},
            {"BILL003", "Fall 2024", "5000.00", "2024-09-15", "⏳ Pending", "Remind"}
        };
        
        JTable billingTable = new JTable(data, columnNames);
        billingTable.setRowHeight(40);
        billingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        billingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        billingTable.getTableHeader().setBackground(PRIMARY_COLOR);
        billingTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(billingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Payment options - Using CARD_BG
        JPanel paymentPanel = createRoundedPanel(10, CARD_BG);
        paymentPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("💳 Payment Methods"),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        String[] paymentMethods = {"BKash", "Nagad", "Rocket", "Bank Card", "Cash"};
        for (String method : paymentMethods) {
            JButton payBtn = createStyledButton(method, SECONDARY_COLOR);
            payBtn.setPreferredSize(new Dimension(100, 40));
            paymentPanel.add(payBtn);
        }
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, paymentPanel);
        splitPane.setDividerLocation(300);
        splitPane.setOpaque(false);
        
        panel.add(splitPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, String value, Color color, String icon) {
        JPanel card = createRoundedPanel(15, CARD_BG);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.brighter(), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.WEST);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        contentPanel.add(textPanel, BorderLayout.CENTER);
        
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createModernLostFoundPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(LIGHT_BG);
        
        // Title
        JLabel titleLabel = new JLabel("🔍 Lost & Found");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Report form - Using CARD_BG
        JPanel reportPanel = createRoundedPanel(15, CARD_BG);
        reportPanel.setLayout(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("📝 Report Lost Item"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setOpaque(false);
        
        JTextField itemNameField = new JTextField();
        JTextField lostDateField = new JTextField();
        JComboBox<String> locationCombo = new JComboBox<>(new String[]{"Bus DIU-101", "Bus DIU-102", "DIU Campus", "Bus Stop"});
        JTextArea descriptionArea = new JTextArea(3, 20);
        
        formPanel.add(new JLabel("Item Name:"));
        formPanel.add(itemNameField);
        formPanel.add(new JLabel("Lost Date:"));
        formPanel.add(lostDateField);
        formPanel.add(new JLabel("Location:"));
        formPanel.add(locationCombo);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(descriptionArea));
        
        JButton submitBtn = createStyledButton("🚨 Report Lost Item", DANGER_COLOR);
        submitBtn.addActionListener(e -> {
            if (itemNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter item name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Lost item reported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            itemNameField.setText("");
            lostDateField.setText("");
            descriptionArea.setText("");
        });
        
        reportPanel.add(formPanel, BorderLayout.CENTER);
        reportPanel.add(submitBtn, BorderLayout.SOUTH);
        
        // Found items table
        String[] columnNames = {"Item", "Location", "Date Found", "Status", "Contact"};
        Object[][] data = {
            {"💧 Water Bottle", "Bus DIU-101", "2024-01-10", "🟡 Unclaimed", "017XXXXXXXX"},
            {"📓 Notebook", "Bus DIU-102", "2024-01-11", "🟡 Unclaimed", "017XXXXXXXX"},
            {"📱 Phone", "Bus DIU-103", "2024-01-12", "🟢 Claimed", "017XXXXXXXX"},
            {"🎒 Bag", "DIU Campus", "2024-01-13", "🟡 Unclaimed", "017XXXXXXXX"}
        };
        
        JTable lostFoundTable = new JTable(data, columnNames);
        lostFoundTable.setRowHeight(40);
        lostFoundTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lostFoundTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        lostFoundTable.getTableHeader().setBackground(PRIMARY_COLOR);
        lostFoundTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane tableScroll = new JScrollPane(lostFoundTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("📦 Recently Found Items"));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, reportPanel, tableScroll);
        splitPane.setDividerLocation(250);
        splitPane.setOpaque(false);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createModernContactPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(LIGHT_BG);
        
        // Title
        JLabel titleLabel = new JLabel("📞 Contact & Support");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Contact cards - Using CARD_BG
        JPanel contactsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        contactsPanel.setOpaque(false);
        
        contactsPanel.add(createContactCard("👨‍💼 Transport Manager", "Mr. Rahman", "transport@diu.edu.bd", "01912345678", "9 AM - 5 PM"));
        contactsPanel.add(createContactCard("👩‍💼 Transport Officer", "Ms. Fatima", "officer@diu.edu.bd", "01787654321", "9 AM - 5 PM"));
        contactsPanel.add(createContactCard("🚨 Emergency", "24/7 Hotline", "emergency@diu.edu.bd", "01900112233", "24/7 Available"));
        contactsPanel.add(createContactCard("💁 Help Desk", "Support Team", "help@diu.edu.bd", "01711223344", "10 AM - 6 PM"));
        
        panel.add(contactsPanel, BorderLayout.CENTER);
        
        // Feedback form - Using CARD_BG
        JPanel feedbackPanel = createRoundedPanel(15, CARD_BG);
        feedbackPanel.setLayout(new BorderLayout());
        feedbackPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("💬 Send Feedback"),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JTextArea feedbackArea = new JTextArea(4, 30);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        
        JButton sendBtn = createStyledButton("📤 Send Feedback", SUCCESS_COLOR);
        sendBtn.addActionListener(e -> {
            String feedback = feedbackArea.getText().trim();
            if (feedback.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your feedback", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Thank you for your feedback!", "Success", JOptionPane.INFORMATION_MESSAGE);
            feedbackArea.setText("");
        });
        
        feedbackPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
        feedbackPanel.add(sendBtn, BorderLayout.SOUTH);
        
        panel.add(feedbackPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createContactCard(String role, String name, String email, String phone, String hours) {
        JPanel card = createRoundedPanel(15, CARD_BG);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLabel.setForeground(Color.GRAY);
        
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(createInfoLine("📧", email));
        infoPanel.add(createInfoLine("📱", phone));
        infoPanel.add(createInfoLine("⏰", hours));
        
        JButton contactBtn = createStyledButton("📞 Contact", SECONDARY_COLOR);
        contactBtn.setPreferredSize(new Dimension(100, 30));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(roleLabel, BorderLayout.NORTH);
        topPanel.add(nameLabel, BorderLayout.SOUTH);
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(contactBtn, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createInfoLine(String icon, String text) {
        JPanel linePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        linePanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        textLabel.setForeground(Color.DARK_GRAY);
        
        linePanel.add(iconLabel);
        linePanel.add(textLabel);
        
        return linePanel;
    }
    
    private JPanel createModernProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(LIGHT_BG);
        
        // Title
        JLabel titleLabel = new JLabel("👤 My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            panel.add(new JLabel("No user is currently logged in.", SwingConstants.CENTER), BorderLayout.CENTER);
            return panel;
        }
        
        // Profile card - Using CARD_BG
        JPanel profileCard = createRoundedPanel(20, CARD_BG);
        profileCard.setLayout(new BorderLayout(20, 20));
        profileCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Avatar and basic info
        JPanel avatarPanel = new JPanel(new BorderLayout(20, 10));
        avatarPanel.setOpaque(false);
        
        JLabel avatarLabel = new JLabel("👤", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        avatarLabel.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 3));
        avatarLabel.setPreferredSize(new Dimension(100, 100));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(CARD_BG);
        
        JPanel basicInfo = new JPanel(new GridLayout(3, 1, 5, 5));
        basicInfo.setOpaque(false);
        
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(PRIMARY_COLOR);
        
        JLabel roleLabel = new JLabel(getRoleDisplayName(user.getRole()));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel idLabel = new JLabel("ID: " + user.getUsername());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        idLabel.setForeground(Color.GRAY);
        
        basicInfo.add(nameLabel);
        basicInfo.add(roleLabel);
        basicInfo.add(idLabel);
        
        avatarPanel.add(avatarLabel, BorderLayout.WEST);
        avatarPanel.add(basicInfo, BorderLayout.CENTER);
        
        // Detailed info
        JPanel detailsPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(BorderFactory.createTitledBorder("📋 Personal Information"));
        
        detailsPanel.add(createDetailField("📧 Email:", user.getEmail()));
        detailsPanel.add(createDetailField("📱 Phone:", user.getPhone()));
        detailsPanel.add(createDetailField("🎓 Department:", "Computer Science & Engineering"));
        detailsPanel.add(createDetailField("📅 Join Date:", "January 2024"));
        detailsPanel.add(createDetailField("🚌 Card Status:", "🟢 Active"));
        detailsPanel.add(createDetailField("💰 Balance:", "5000 BDT"));
        detailsPanel.add(createDetailField("📊 Total Trips:", "42"));
        detailsPanel.add(createDetailField("⭐ Rating:", "4.5/5.0"));
        
        // Edit button
        JButton editBtn = createStyledButton("✏️ Edit Profile", SECONDARY_COLOR);
        editBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Profile editing will be available soon!", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
        });
        
        profileCard.add(avatarPanel, BorderLayout.NORTH);
        profileCard.add(detailsPanel, BorderLayout.CENTER);
        profileCard.add(editBtn, BorderLayout.SOUTH);
        
        panel.add(profileCard, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDetailField(String label, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(false);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelComp.setForeground(Color.GRAY);
        
        JLabel valueComp = new JLabel(value != null ? value : "Not provided");
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueComp.setForeground(PRIMARY_COLOR);
        
        fieldPanel.add(labelComp, BorderLayout.NORTH);
        fieldPanel.add(valueComp, BorderLayout.CENTER);
        
        return fieldPanel;
    }
    
    private JPanel createModernStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(DARK_BG); // Now using DARK_BG
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        statusBar.setPreferredSize(new Dimension(getWidth(), 35));
        
        // Left side: Status
        JLabel statusLabel = new JLabel("🟢 Connected to DIU Transport System");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(Color.WHITE);
        
        // Right side: Time and info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        timeLabel.setForeground(Color.WHITE);
        
        // Update time every second
        Timer timeTimer = new Timer(1000, e -> {
            timeLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        });
        timeTimer.start();
        
        rightPanel.add(timeLabel);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    // Utility methods
    private JPanel createRoundedPanel(int radius, Color backgroundColor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(backgroundColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2d.dispose();
            }
        };
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JMenu createStyledMenu(String text, Color color) {
        JMenu menu = new JMenu(text);
        menu.setForeground(Color.WHITE);
        menu.setBackground(color);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menu.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return menu;
    }
    
    private void addStyledMenuItem(JMenu menu, String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        menu.add(item);
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
    
    private void setupAnimations() {
        // Dashboard updates timer
        dashboardTimer = new Timer(30000, e -> {
            // Update dashboard data periodically
        });
        dashboardTimer.start();
    }
    
    private void refreshDashboard() {
        // Refresh dashboard data
        JOptionPane.showMessageDialog(this, "Dashboard refreshed!", "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
    
    private void showNotifications() {
        JOptionPane.showMessageDialog(this,
            "You have no new notifications.",
            "Notifications",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showUsageStats() {
        JOptionPane.showMessageDialog(this,
            "Usage Statistics:\n• Total Trips: 42\n• This Month: 8\n• Average Rating: 4.5/5",
            "Usage Statistics",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showSearch() {
        String searchTerm = JOptionPane.showInputDialog(this,
            "Enter search term:",
            "Search",
            JOptionPane.PLAIN_MESSAGE);
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Searching for: " + searchTerm,
                "Search Results",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showUserGuide() {
        JOptionPane.showMessageDialog(this,
            "User Guide:\n\n1. Check bus schedules in Schedule tab\n2. View your transport card details\n3. Pay bills in Billing tab\n4. Report lost items\n5. Contact support for help",
            "User Guide",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            Constants.APP_NAME + " v" + Constants.APP_VERSION + "\n" +
            Constants.DEVELOPER + "\n" +
            Constants.COPYRIGHT,
            "About",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void dispose() {
        if (dashboardTimer != null) {
            dashboardTimer.stop();
        }
        super.dispose();
    }
}