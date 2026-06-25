package gui;

import util.DatabaseConnection;
import util.Constants;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class DIUTransportSystem {

    private static JWindow splashWindow;

    public static void main(String[] args) {
        boolean debugMode = false;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--debug")) {
                debugMode = true;
                System.out.println("Running in DEBUG mode");
            } else if (arg.equalsIgnoreCase("--help")) {
                printHelp();
                return;
            }
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        if (debugMode) {
            showApplicationInfo();
        }

        showSplashScreen();

        boolean dbInitialized = initializeDatabase();

        if (dbInitialized) {
            SwingUtilities.invokeLater(() -> {
                closeSplashScreen();
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                closeSplashScreen();
                JOptionPane.showMessageDialog(
                    null,
                    "Failed to initialize database.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            });
        }
    }

    private static void showSplashScreen() {
        splashWindow = new JWindow();

        JPanel splashPanel = new JPanel(new BorderLayout());
        splashPanel.setBackground(Color.WHITE);
        splashPanel.setBorder(BorderFactory.createLineBorder(Color.decode(Constants.COLOR_PRIMARY), 2));

        JLabel titleLabel = new JLabel(Constants.APP_NAME, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode(Constants.COLOR_PRIMARY));

        JLabel subtitleLabel = new JLabel("Transport Schedule Automation System", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);

        JLabel versionLabel = new JLabel("Version " + Constants.APP_VERSION, JLabel.CENTER);
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        versionLabel.setForeground(Color.GRAY);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(Color.decode(Constants.COLOR_SECONDARY));

        JPanel centerPanel = new JPanel(new GridLayout(3, 1));
        centerPanel.add(titleLabel);
        centerPanel.add(subtitleLabel);
        centerPanel.add(versionLabel);

        splashPanel.add(centerPanel, BorderLayout.CENTER);
        splashPanel.add(progressBar, BorderLayout.SOUTH);

        splashWindow.getContentPane().add(splashPanel);
        splashWindow.setSize(500, 250);
        splashWindow.setLocationRelativeTo(null);
        splashWindow.setVisible(true);
    }

    private static void closeSplashScreen() {
        if (splashWindow != null) {
            splashWindow.dispose();
            splashWindow = null;
        }
    }

    private static boolean initializeDatabase() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            return connection != null;
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            return false;
        }
    }

    public static void showApplicationInfo() {
        System.out.println("=========================================");
        System.out.println(Constants.APP_NAME);
        System.out.println("Version: " + Constants.APP_VERSION);
        System.out.println("Developer: " + Constants.DEVELOPER);
        System.out.println(Constants.COPYRIGHT);
        System.out.println("Database: " + Constants.DB_URL);
        System.out.println("=========================================");
    }

    private static void printHelp() {
        System.out.println("Usage: java gui.DIUTransportSystem");
        System.out.println("Default Admin:");
        System.out.println("Username: admin");
        System.out.println("Password: admin123");
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.closeConnection();
            System.out.println("Application shutdown complete.");
        }));
    }
}