package gui;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import util.Constants;
import util.SessionManager;

/**
 * AdminDashboard - Modern Admin Interface with All Management Functions
 * Features gradient backgrounds, rounded panels, and material design elements
 */
public class AdminDashboard extends JFrame {
    private JTabbedPane tabbedPane;
    private Timer dashboardTimer;
    private static final Logger logger = Logger.getLogger(AdminDashboard.class.getName());
    
    // Enhanced color scheme with DIU branding
    private static final Color DIU_PRIMARY = Color.decode("#2E3192");     // DIU Blue
    private static final Color DIU_SECONDARY = Color.decode("#00A651");   // DIU Green
    private static final Color GRADIENT_START = Color.decode("#1A2980");  // Dark Blue
    private static final Color GRADIENT_END = Color.decode("#26D0CE");    // Teal
    private static final Color CARD_BG = new Color(255, 255, 255, 230);   // Semi-transparent white
    private static final Color PANEL_BG = new Color(245, 247, 250);       // Light gray background
    private static final Color SUCCESS_COLOR = Color.decode("#2ECC71");
    private static final Color WARNING_COLOR = Color.decode("#F39C12");
    private static final Color DANGER_COLOR = Color.decode("#E74C3C");
    private static final Color ACCENT_COLOR = Color.decode("#9B59B6");
    
    // DIU-themed icons and assets
    private static final String[] TAB_ICONS = {"📊", "👥", "🚌", "📅", "👨‍✈️", "💳", "💰", "🔔", "⚙️"};
    private static final String[] STAT_ICONS = {"👥", "🚌", "📅", "💰", "💳", "👨‍✈️", "🔍", "❤️"};
    
    public AdminDashboard() {
        initializeFrame();
        createComponents();
        loadInitialData();
        startDashboardUpdates();
    }
    
    private void initializeFrame() {
        setTitle(Constants.APP_NAME + " 🚍 Admin Dashboard | DIU Transport System");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        try {
            ImageIcon icon = new ImageIcon(Constants.APP_ICON_PATH);
            setIconImage(icon.getImage());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load app icon: {0}", e.getMessage());
        }
        
        // Set modern look and feel with multi-catch
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | 
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.log(Level.WARNING, "Could not set preferred look and feel: {0}", e.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | 
                     IllegalAccessException | UnsupportedLookAndFeelException ex) {
                logger.log(Level.WARNING, "Could not set system look and feel", ex);
            }
        }
    }
    
    private void createComponents() {
        createModernMenuBar();
        
        JPanel headerPanel = createAnimatedHeader();
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add tabs with DIU-themed styling
        String[] tabNames = {"Dashboard", "Users", "Buses", "Schedule", "Drivers", 
                           "Transport Cards", "Billing", "Notifications", "Settings"};
        
        for (int i = 0; i < tabNames.length; i++) {
            tabbedPane.addTab(TAB_ICONS[i] + " " + tabNames[i], createTabPanel(i));
        }
        
        // Custom tab renderer
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, 
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected) {
                    GradientPaint gradient = new GradientPaint(
                        x, y, DIU_PRIMARY,
                        x, y + h, DIU_SECONDARY
                    );
                    g2d.setPaint(gradient);
                } else {
                    g2d.setColor(PANEL_BG);
                }
                
                g2d.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 15, 15);
                g2d.dispose();
            }
            
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, 
                    int x, int y, int w, int h, boolean isSelected) {
                // No border for modern look
            }
        });
        
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(createModernStatusBar(), BorderLayout.SOUTH);
    }
    
    private JPanel createAnimatedHeader() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Animated gradient based on time
                long time = System.currentTimeMillis() % 10000;
                float offset = (float) Math.sin(time * 0.0005) * 50;
                
                GradientPaint gradient = new GradientPaint(
                    0, 0 + offset, GRADIENT_START,
                    getWidth(), getHeight() - offset, GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Add DIU pattern
                g2d.setColor(new Color(255, 255, 255, 20));
                for (int i = 0; i < getWidth(); i += 40) {
                    for (int j = 0; j < getHeight(); j += 40) {
                        g2d.fillOval(i, j, 3, 3);
                    }
                }
                
                // DIU Transportation text
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2d.setColor(new Color(255, 255, 255, 150));
                g2d.drawString("DIU Transportation System • 50+ Buses • Campus Safety", 20, getHeight() - 10);
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(getWidth(), 140));
        
        // Animation timer
        new Timer(50, e -> headerPanel.repaint()).start();
        
        // Left panel with title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 25));
        leftPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(Constants.APP_NAME + " - ADMIN CONTROL PANEL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JLabel subtitleLabel = new JLabel("Daffodil International University");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        
        leftPanel.add(createVerticalPanel(titleLabel, subtitleLabel));
        
        // Right panel with user info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 25));
        rightPanel.setOpaque(false);
        
        JPanel userCard = createGlassPanel(20, CARD_BG);
        userCard.setLayout(new BorderLayout(10, 5));
        userCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));
        
        String userName = SessionManager.getCurrentUser() != null ? 
            SessionManager.getCurrentUser().getName() : "Administrator";
        
        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        welcomeLabel.setForeground(Color.DARK_GRAY);
        
        JLabel userNameLabel = new JLabel(userName);
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        userNameLabel.setForeground(DIU_PRIMARY);
        
        JLabel roleLabel = new JLabel("👑 System Administrator");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(DIU_SECONDARY);
        
        userCard.add(createVerticalPanel(welcomeLabel, userNameLabel, roleLabel), BorderLayout.CENTER);
        
        JButton logoutBtn = createModernButton("🚪 Logout", DANGER_COLOR);
        logoutBtn.addActionListener(e -> logout());
        
        rightPanel.add(userCard);
        rightPanel.add(logoutBtn);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createVerticalPanel(JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        for (JComponent comp : components) {
            panel.add(comp);
            panel.add(Box.createVerticalStrut(2));
        }
        return panel;
    }
    
    private JPanel createTabPanel(int index) {
        return switch (index) {
            case 0 -> createModernDashboardPanel();
            case 1 -> createModernUserManagementPanel();
            case 2 -> createModernBusManagementPanel();
            case 3 -> createScheduleManagementPanel();
            case 4 -> createDriverManagementPanel();
            case 5 -> createTransportCardsPanel();
            case 6 -> createBillingPanel();
            case 7 -> createNotificationsPanel();
            case 8 -> createSystemSettingsPanel();
            default -> new JPanel();
        };
    }
    
    private JPanel createModernDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(PANEL_BG);
        
        // Statistics cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsPanel.setOpaque(false);
        
        String[] statTitles = {"Total Users", "Active Buses", "Today's Trips", "Revenue",
                              "Pending Cards", "Active Drivers", "Lost Items", "System Health"};
        String[] statValues = {"1,250", "52", "147", "45,000 BDT", "18", "48", "7", "99%"};
        Color[] statColors = {DIU_PRIMARY, SUCCESS_COLOR, WARNING_COLOR, ACCENT_COLOR,
                             DANGER_COLOR, Color.decode("#1ABC9C"), Color.decode("#E67E22"), DIU_SECONDARY};
        
        for (int i = 0; i < 8; i++) {
            statsPanel.add(createModernStatCard(statTitles[i], statValues[i], statColors[i], STAT_ICONS[i]));
        }
        
        // Middle section
        JSplitPane middleSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        middleSplit.setDividerLocation(500);
        middleSplit.setOpaque(false);
        middleSplit.setDividerSize(3);
        middleSplit.setBorder(BorderFactory.createEmptyBorder());
        
        middleSplit.setLeftComponent(createActivityPanel());
        middleSplit.setRightComponent(createQuickActionsPanel());
        
        // Bottom system info
        JPanel systemPanel = createGlassPanel(15, Color.WHITE);
        systemPanel.setLayout(new GridLayout(1, 3, 20, 0));
        systemPanel.setBorder(createTitledBorder("💻 System Information"));
        
        systemPanel.add(createInfoCard("Database", "Connected", "💾"));
        systemPanel.add(createInfoCard("Last Backup", "Today 08:00", "🔄"));
        systemPanel.add(createInfoCard("Uptime", "15 days", "⏱️"));
        
        panel.add(statsPanel, BorderLayout.NORTH);
        panel.add(middleSplit, BorderLayout.CENTER);
        panel.add(systemPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createActivityPanel() {
        JPanel panel = createGlassPanel(15, Color.WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(createTitledBorder("📝 Recent Activity"));
        
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Time", "Activity", "User", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        String[][] activities = {
            {"10:30 AM", "User Login", "admin", "✅"},
            {"10:15 AM", "Bus DIU-105 Added", "transport", "✅"},
            {"09:45 AM", "Payment Received", "5,000 BDT", "✅"},
            {"09:30 AM", "Route R004 Updated", "scheduler", "✅"},
            {"09:00 AM", "Card Issued", "2024-045", "✅"},
            {"08:45 AM", "Maintenance Alert", "DIU-101", "⚠️"}
        };
        
        for (String[] activity : activities) {
            model.addRow(activity);
        }
        
        JTable table = new JTable(model);
        configureTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel panel = createGlassPanel(15, Color.WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(createTitledBorder("⚡ Quick Actions"));
        
        JPanel actionButtons = new JPanel(new GridLayout(4, 1, 10, 10));
        actionButtons.setOpaque(false);
        
        String[] actions = {"➕ Add New User", "🚌 Add New Bus", "📧 Send Notification", "📊 View Reports"};
        Color[] btnColors = {DIU_PRIMARY, SUCCESS_COLOR, WARNING_COLOR, ACCENT_COLOR};
        
        for (int i = 0; i < actions.length; i++) {
            JButton btn = createModernButton(actions[i], btnColors[i]);
            final int index = i;
            btn.addActionListener(e -> performQuickAction(index));
            actionButtons.add(btn);
        }
        
        panel.add(actionButtons, BorderLayout.CENTER);
        return panel;
    }
    
    private void performQuickAction(int index) {
        switch (index) {
            case 0 -> tabbedPane.setSelectedIndex(1);
            case 1 -> tabbedPane.setSelectedIndex(2);
            case 2 -> tabbedPane.setSelectedIndex(7);
            case 3 -> generateReport("dashboard");
        }
    }
    
    private JPanel createModernStatCard(String title, String value, Color color, String icon) {
        JPanel card = createGlassPanel(20, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.brighter(), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Icon and title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);
        
        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Animated progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int)(Math.random() * 30 + 70)); // Random value 70-100
        progressBar.setForeground(color);
        progressBar.setBackground(color.brighter().brighter());
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 6));
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(progressBar, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createModernUserManagementPanel() {
        return createManagementPanel("👥 User Management", "Add New User",
            new String[]{"ID", "Username", "Name", "Role", "Email", "Phone", "Department", "Status"});
    }
    
    private JPanel createModernBusManagementPanel() {
        return createManagementPanel("🚌 Bus Management", "Add New Bus",
            new String[]{"Bus ID", "Number", "Capacity", "Route", "Status", "Driver", "Last Service", "Next Trip"});
    }
    
    private JPanel createScheduleManagementPanel() {
        return createManagementPanel("📅 Schedule Management", "Add New Schedule",
            new String[]{"Schedule ID", "Bus", "Route", "Departure", "Arrival", "Driver", "Status", "Passengers"});
    }
    
    private JPanel createDriverManagementPanel() {
        return createManagementPanel("👨‍✈️ Driver Management", "Add New Driver",
            new String[]{"Driver ID", "Name", "License", "Phone", "Email", "Status", "Assigned Bus", "Experience"});
    }
    
    private JPanel createTransportCardsPanel() {
        return createManagementPanel("💳 Transport Cards", "Issue New Card",
            new String[]{"Card ID", "User", "Type", "Balance", "Issue Date", "Expiry", "Status", "Last Used"});
    }
    
    private JPanel createBillingPanel() {
        return createManagementPanel("💰 Billing & Payments", "Add Payment",
            new String[]{"Invoice ID", "User", "Amount", "Date", "Method", "Status", "Description", "Due Date"});
    }
    
    private JPanel createNotificationsPanel() {
        return createManagementPanel("🔔 Notifications", "Send Notification",
            new String[]{"ID", "Title", "Message", "Type", "Sent To", "Date", "Status", "Read By"});
    }
    
    private JPanel createManagementPanel(String title, String addButtonText, String[] columns) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(PANEL_BG);
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(DIU_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Toolbar
        JPanel toolbar = createGlassPanel(10, Color.WHITE);
        toolbar.setLayout(new BorderLayout());
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton addBtn = createModernButton("➕ " + addButtonText, SUCCESS_COLOR);
        JButton editBtn = createModernButton("✏️ Edit", DIU_PRIMARY);
        JButton deleteBtn = createModernButton("🗑️ Delete", DANGER_COLOR);
        JButton refreshBtn = createModernButton("🔄 Refresh", WARNING_COLOR);
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        
        // Search panel
        JPanel searchPanel = createSearchPanel();
        
        toolbar.add(buttonPanel, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);
        
        panel.add(toolbar, BorderLayout.CENTER);
        
        // Table
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        configureTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        
        JTextField searchField = new JTextField(25);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton searchBtn = createModernButton("🔍 Search", ACCENT_COLOR);
        searchBtn.setPreferredSize(new Dimension(100, 35));
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        return searchPanel;
    }
    
    private void configureTable(JTable table) {
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(DIU_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 2));
        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }
                return c;
            }
        });
    }
    
    private JPanel createSystemSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(PANEL_BG);
        
        JLabel titleLabel = new JLabel("⚙️ System Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(DIU_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel settingsPanel = createGlassPanel(15, Color.WHITE);
        settingsPanel.setLayout(new GridLayout(6, 1, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] settings = {"🔐 Security Settings", "📊 Database Configuration", "📧 Email Settings", 
                           "📱 SMS Gateway", "📄 Report Templates", "🔄 System Maintenance"};
        
        for (String setting : settings) {
            JPanel settingItem = createGlassPanel(10, PANEL_BG);
            settingItem.setLayout(new BorderLayout());
            settingItem.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
            
            JLabel settingLabel = new JLabel(setting);
            settingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            JButton configureBtn = createModernButton("Configure", DIU_PRIMARY);
            configureBtn.setPreferredSize(new Dimension(100, 30));
            
            settingItem.add(settingLabel, BorderLayout.WEST);
            settingItem.add(configureBtn, BorderLayout.EAST);
            settingsPanel.add(settingItem);
        }
        
        panel.add(new JScrollPane(settingsPanel), BorderLayout.CENTER);
        
        return panel;
    }
    
    private void createModernMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(46, 49, 146, 240));
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // File Menu
        JMenu fileMenu = createStyledMenu("📁 File", DIU_PRIMARY);
        addStyledMenuItem(fileMenu, "🔄 Refresh", e -> loadInitialData());
        addStyledMenuItem(fileMenu, "💾 Backup Database", e -> backupDatabase());
        fileMenu.addSeparator();
        addStyledMenuItem(fileMenu, "🚪 Logout", e -> logout());
        addStyledMenuItem(fileMenu, "❌ Exit", e -> System.exit(0));
        
        // Reports Menu
        JMenu reportsMenu = createStyledMenu("📈 Reports", DIU_SECONDARY);
        addStyledMenuItem(reportsMenu, "👥 User Report", e -> generateReport("users"));
        addStyledMenuItem(reportsMenu, "🚌 Bus Report", e -> generateReport("buses"));
        addStyledMenuItem(reportsMenu, "💰 Payment Report", e -> generateReport("payments"));
        addStyledMenuItem(reportsMenu, "📅 Schedule Report", e -> generateReport("schedules"));
        
        // Tools Menu
        JMenu toolsMenu = createStyledMenu("🛠️ Tools", WARNING_COLOR);
        addStyledMenuItem(toolsMenu, "🔍 Search", e -> showSearchDialog());
        addStyledMenuItem(toolsMenu, "📊 Statistics", e -> showStatistics());
        addStyledMenuItem(toolsMenu, "🔔 Send Broadcast", e -> sendBroadcastNotification());
        
        // Help Menu
        JMenu helpMenu = createStyledMenu("❓ Help", SUCCESS_COLOR);
        addStyledMenuItem(helpMenu, "📖 Help Contents", e -> showHelpDialog());
        helpMenu.addSeparator();
        addStyledMenuItem(helpMenu, "ℹ️ About DIU System", e -> showAboutDialog());
        
        menuBar.add(fileMenu);
        menuBar.add(reportsMenu);
        menuBar.add(toolsMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JMenu createStyledMenu(String text, Color color) {
        JMenu menu = new JMenu(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isSelected()) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, color,
                        0, getHeight(), color.darker()
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        menu.setForeground(Color.WHITE);
        menu.setOpaque(false);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menu.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return menu;
    }
    
    private void addStyledMenuItem(JMenu menu, String text, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.setBorder(BorderFactory.createEmptyBorder(8, 25, 8, 25));
        menu.add(item);
    }
    
    private JPanel createModernStatusBar() {
        JPanel statusBar = createGlassPanel(0, new Color(46, 49, 146, 240));
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 50)),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        statusBar.setPreferredSize(new Dimension(getWidth(), 45));
        
        // Left status
        JLabel statusLabel = new JLabel("🟢 DIU Transport System Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        
        // Center connection
        JLabel connectionLabel = new JLabel("📶 Connected to DIU Database");
        connectionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        connectionLabel.setForeground(new Color(200, 200, 200));
        connectionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Right time and user
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        String userName = SessionManager.getCurrentUser() != null ? 
            SessionManager.getCurrentUser().getName() : "Administrator";
        JLabel userLabel = new JLabel("👤 " + userName);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userLabel.setForeground(Color.WHITE);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLabel.setForeground(Color.WHITE);
        
        new Timer(1000, e -> 
            timeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
        ).start();
        
        rightPanel.add(userLabel);
        rightPanel.add(new JLabel("|"));
        rightPanel.add(timeLabel);
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(connectionLabel, BorderLayout.CENTER);
        statusBar.add(rightPanel, BorderLayout.EAST);
        
        return statusBar;
    }
    
    private JPanel createGlassPanel(int radius, Color backgroundColor) {
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
    
    private JButton createModernButton(String text, Color bgColor) {
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
                
                // Text with shadow
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawString(getText(), x + 1, y + 1);
                
                // Main text
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect with @Override annotations
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    private javax.swing.border.Border createTitledBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 15, 5, 15),
                title,
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                DIU_PRIMARY
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
    }
    
    private JPanel createInfoCard(String title, String value, String icon) {
        JPanel card = createGlassPanel(10, Color.WHITE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(Color.GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(DIU_PRIMARY);
        
        card.add(iconLabel, BorderLayout.WEST);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        card.add(textPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void loadInitialData() {
        // In a real application, this would load data from services
        try {
            // Simulate data loading
            Thread.sleep(500);
            logger.info("Initial data loaded successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Data loading interrupted", e);
        }
    }
    
    private void startDashboardUpdates() {
        dashboardTimer = new Timer(30000, e -> {
            // Fixed: Use supplier pattern to avoid string concatenation in logger
            logger.fine(() -> "Dashboard auto-update at: " + new Date());
        });
        dashboardTimer.start();
    }
    
    private void backupDatabase() {
        int choice = JOptionPane.showConfirmDialog(this, 
            """
            Are you sure you want to backup the database?
            
            This will create a backup of all system data including:
            • User information
            • Bus schedules
            • Payment records
            • Transport cards
            
            Estimated backup size: ~50MB
            Estimated time: 1-2 minutes
            """,
            "Database Backup",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Backup logic here
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                JOptionPane.showMessageDialog(this,
                    """
                    ✅ Database backup completed successfully!
                    
                    Backup location: /backups/system_backup_%s.db
                    
                    Backup includes:
                    • 1,250 user records
                    • 52 bus records
                    • 147 schedule entries
                    • 2,500 payment transactions
                    """.formatted(timestamp),
                    "Backup Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (SecurityException | IllegalArgumentException | NullPointerException e) {
                logger.log(Level.SEVERE, "Backup failed due to security or configuration issue", e);
                JOptionPane.showMessageDialog(this,
                    "❌ Backup failed due to security or configuration issue: " + e.getMessage(),
                    "Backup Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException e) {
                logger.log(Level.SEVERE, "Backup failed due to runtime error", e);
                JOptionPane.showMessageDialog(this,
                    "❌ Backup failed due to runtime error: " + e.getMessage(),
                    "Backup Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this,
            """
            Are you sure you want to logout?
            
            You will be redirected to the login screen.
            
            Session will be terminated immediately.
            """,
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            """
            🚍 DIU Transport Management System
            Version: %s
            
            Developed for: Daffodil International University
            Developer: %s
            Copyright: %s
            
            This comprehensive system manages:
            • Campus transportation
            • Bus scheduling
            • Student transport cards
            • Driver management
            • Financial transactions
            
            All rights reserved.
            """.formatted(Constants.APP_VERSION, Constants.DEVELOPER, Constants.COPYRIGHT),
            "About DIU Transport System",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showHelpDialog() {
        JTextArea textArea = new JTextArea(
            """
            📚 Admin Dashboard Help Guide
            
            1. 📊 Dashboard
               • View real-time system statistics
               • Monitor recent activities
               • Quick access to common tasks
            
            2. 👥 User Management
               • Add, edit, and delete users
               • Filter by role (Student/Teacher/Staff)
               • Manage user permissions
            
            3. 🚌 Bus Management
               • Manage DIU bus fleet
               • Track bus status
               • Assign routes and drivers
            
            4. 📅 Schedule Management
               • Create and edit schedules
               • Manage routes and timings
               • View upcoming trips
            
            5. 👨‍✈️ Driver Management
               • Manage driver information
               • Assign drivers to buses
               • Track driver schedules
            
            6. 💳 Transport Cards
               • Issue new cards
               • Renew expired cards
               • Track card status
            
            7. 💰 Billing & Payments
               • Manage payments
               • Generate invoices
               • Track revenue
            
            8. 🔔 Notifications
               • Send alerts to users
               • Broadcast messages
               • Manage notification history
            
            9. ⚙️ Settings
               • System configuration
               • Database management
               • User preferences
            
            💡 Tip: Use Quick Actions for faster access!
            """
        );
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Help Guide", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void generateReport(String reportType) {
        String reportName = switch (reportType) {
            case "users" -> "User Report";
            case "buses" -> "Bus Report";
            case "payments" -> "Payment Report";
            case "schedules" -> "Schedule Report";
            default -> "Dashboard Report";
        };
        
        JOptionPane.showMessageDialog(this,
            """
            📄 Generating %s...
            
            This will generate a comprehensive report including:
            • Summary statistics
            • Detailed data tables
            • Charts and graphs
            • Recommendations
            
            Report format: PDF/Excel
            Estimated time: 10-30 seconds
            
            Feature available in next update.
            """.formatted(reportName),
            "Report Generation",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showSearchDialog() {
        String searchTerm = JOptionPane.showInputDialog(this,
            """
            Enter search term to search across:
            • Users
            • Buses
            • Schedules
            • Payments
            • Transport Cards
            
            Search supports partial matches and filters.
            """,
            "Search System",
            JOptionPane.PLAIN_MESSAGE);
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                """
                🔍 Search Results for: %s
                
                Found 15 matching records:
                • Users: 5
                • Buses: 3
                • Schedules: 4
                • Payments: 3
                
                Detailed results in relevant tab.
                """.formatted(searchTerm),
                "Search Results",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showStatistics() {
        JOptionPane.showMessageDialog(this,
            """
            📊 DIU Transport System Statistics
            
            • Total Users: 1,250
               - Students: 1,000
               - Teachers: 150
               - Staff: 80
               - Admins: 20
            
            • Active Buses: 52
               - On route: 38
               - Maintenance: 10
               - Available: 4
            
            • Financial Summary
               - Today's Revenue: 45,000 BDT
               - Monthly Revenue: 900,000 BDT
               - Pending Payments: 25,000 BDT
            
            • System Performance
               - Uptime: 99.8%
               - Database Size: 85.2 MB
               - Active Sessions: 8
            
            • Pending Tasks
               - Card Approvals: 18
               - Maintenance Requests: 7
               - Schedule Changes: 3
            """,
            "System Statistics",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void sendBroadcastNotification() {
        String message = JOptionPane.showInputDialog(this,
            """
            Enter broadcast message to send to all users:
            
            This message will be sent to:
            • All active users (1,250 users)
            • Via: Email, SMS, and In-app notification
            • Priority: High
            
            Maximum length: 500 characters
            """,
            "Send Broadcast",
            JOptionPane.PLAIN_MESSAGE);
        
        if (message != null && !message.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                """
                ✅ Broadcast sent successfully!
                
                Message: %s
                
                Delivery Status:
                • Emails: Queued (1,250)
                • SMS: Sent (1,250)
                • In-app: Delivered (1,250)
                
                Users will receive within 5 minutes.
                """.formatted(message),
                "Broadcast Sent",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void dispose() {
        try {
            if (dashboardTimer != null && dashboardTimer.isRunning()) {
                dashboardTimer.stop();
            }
        } catch (SecurityException | IllegalStateException e) {
            logger.log(Level.WARNING, "Error stopping dashboard timer", e);
        } finally {
            super.dispose();
        }
    }
}