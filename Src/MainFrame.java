import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List; // For list of managers
import javax.swing.*;

/**
 * The main application window for Focusary.
 * Manages the overall UI layout, navigation between different panels (Pomodoro, Tasks, Notes),
 * and initializes various managers (Sound, Quote, Background).
 */
public class MainFrame extends JFrame {
    // UI Components
    private ToDoPanel toDoPanel;
    private JButton todoButton;
    
    private JButton pomodoroButton;
    private JLabel timeLabel;
    private JLabel greetingLabel;

    // Managers and Timers (now explicitly typed as concrete classes)
    private SoundManager soundManager;
    private QuoteManager quoteManager;
    private PomodoroPanel pomodoroPanel;
    private Timer timer; // For updating the clock

    // List to hold all managers for easy lifecycle management (Polymorphism)
    private List<ApplicationManager> managers;

    // CardLayout components for panel switching
    private JPanel cardPanel;
    private CardLayout cardLayout;

    /**
     * Constructor for the MainFrame.
     * Initializes the UI, sets up layouts, and connects all the different panels and functionalities.
     */
    public MainFrame() {
        // Basic JFrame setup
        setTitle("Focusary");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Exit application on close
        setLocationRelativeTo(null); // Center the window on the screen

        // Initialize managers
        quoteManager = new QuoteManager();
        soundManager = new SoundManager();

        // Populate list of managers for polymorphic lifecycle management
        managers = new ArrayList<>();
        managers.add(quoteManager);
        managers.add(soundManager);
        // Add other managers here if they were to extend AbstractApplicationManager
        // For now, TaskManager is static, so it's not added here.

        // Initialize all managers
        for (ApplicationManager manager : managers) {
            manager.initialize();
        }

        // Add a WindowListener to dispose managers when the frame closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (ApplicationManager manager : managers) {
                    manager.dispose();
                }
                // Also stop the Swing timer that updates time
                if (timer != null && timer.isRunning()) {
                    timer.stop();
                }
                // Any other final cleanup before exiting
                super.windowClosing(e); // Calls default close operation
            }
        });


        // Create the main background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout()); // Use BorderLayout for NORTH and CENTER regions

        // Initialize CardLayout for switching between different content views (Main, TODO, Notes, Pomodoro)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false); // Make it transparent so the background image is visible

        // === Center Panel (Main View: Clock + Greeting) ===
        // This panel will display the current time and a dynamic greeting.
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false); // Make it transparent
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS)); // Stack elements vertically

        greetingLabel = new JLabel("Rest easy. You've done well today!");
        greetingLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        greetingLabel.setForeground(Color.WHITE);
        greetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align horizontally
        greetingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        timeLabel = new JLabel("HH:MM", SwingConstants.CENTER); // Initial text, will be updated by timer
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 120));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateTime(); // Call once immediately to set initial time

        // Add components to the center panel with vertical glue for spacing
        centerPanel.add(Box.createVerticalGlue()); // Pushes content to the center
        centerPanel.add(greetingLabel);
        centerPanel.add(Box.createVerticalStrut(20)); // 20-pixel vertical space
        centerPanel.add(timeLabel);
        centerPanel.add(Box.createVerticalGlue()); // Pushes content to the center

        // Add the main center panel to the cardPanel
        cardPanel.add(centerPanel, "MAIN");

        // === Initialize other content panels ===
        toDoPanel = new ToDoPanel();
        toDoPanel.setOpaque(false); // Ensure ToDoPanel background is transparent to match theme

       
        pomodoroPanel = new PomodoroPanel(quoteManager, soundManager);
        pomodoroPanel.setOpaque(false); // Ensure PomodoroPanel background is transparent

        // Add other functional panels to the cardPanel
        cardPanel.add(toDoPanel, "TODO");
       
        cardPanel.add(pomodoroPanel, "POMODORO");

        // === Selector Panel (for navigation buttons at the top) ===
        JPanel selectorPanel = new BackgroundSelectorPanel(backgroundPanel);
        selectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        selectorPanel.setOpaque(false);

        // Initialize navigation buttons
        pomodoroButton = new JButton("Pomodoro");
        todoButton = new JButton("Tasks");
        
        JButton playMusicButton = new JButton("Play Music");
        JButton stopMusicButton = new JButton("Stop Music");

        // Add action listener for the "Play Music" button
        playMusicButton.addActionListener(e -> {
            System.out.println("Play Music button clicked."); // Debug print

            // Load songs directly from the predefined "Sounds" folder
            List<String> songs = SongSelectorPanel.loadSongsFromDefaultFolder();
            System.out.println("Songs loaded from default folder (" + SongSelectorPanel.DEFAULT_SOUNDS_FOLDER + "): " + songs.size() + " songs found."); // Debug print
            for (String songPath : songs) {
                System.out.println("  - " + songPath); // List found songs
            }

            if (songs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No .wav songs found in the 'Sounds' folder! Please ensure the folder exists and contains .wav files.", "No Songs Found", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("No songs found in 'Sounds' folder."); // Debug print
            } else {
                // Extract just the file names for display in the dialog
                String[] songNames = songs.stream()
                                         .map(path -> new File(path).getName())
                                         .toArray(String[]::new);

                String selectedSongName = (String) JOptionPane.showInputDialog(
                    this,
                    "Select a song to play:",
                    "Choose Song",
                    JOptionPane.PLAIN_MESSAGE,
                    null, // Icon
                    songNames,
                    songNames.length > 0 ? songNames[0] : null // Default selection if songs exist
                );

                if (selectedSongName != null) {
                    // Find the full path of the selected song
                    String fullPathToPlay = songs.stream()
                                                 .filter(path -> new File(path).getName().equals(selectedSongName))
                                                 .findFirst()
                                                 .orElse(null);

                    if (fullPathToPlay != null) {
                        System.out.println("User selected: " + selectedSongName + " (Path: " + fullPathToPlay + ")"); // Debug print
                        soundManager.playSongs(List.of(fullPathToPlay)); // Play just the selected song
                    } else {
                        System.err.println("Error: Full path not found for selected song name: " + selectedSongName); // Debug
                    }
                } else {
                    System.out.println("User cancelled song selection."); // Debug print
                }
            }
        });


        stopMusicButton.addActionListener(e -> {
            soundManager.stop();
            System.out.println("Music stopped."); // Debug print
        });

        // Add all navigation buttons to the selector panel
        selectorPanel.add(todoButton);
        
        selectorPanel.add(pomodoroButton);
        selectorPanel.add(playMusicButton);
        selectorPanel.add(stopMusicButton);

        // Add action listeners for navigation buttons to switch cardPanel views
        todoButton.addActionListener(e -> cardLayout.show(cardPanel, "TODO"));
     

        // Action listener for the Pomodoro button
        pomodoroButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "POMODORO");
            pomodoroPanel.setRandomQuote();
            revalidate();
            repaint();
        });

        backgroundPanel.add(selectorPanel, BorderLayout.NORTH);
        backgroundPanel.add(cardPanel, BorderLayout.CENTER);

        // Show MAIN view on startup
        cardLayout.show(cardPanel, "MAIN");

        // Initialize and start a Swing Timer to update the time every second
        timer = new Timer(1000, e -> updateTime());
        timer.start();

        // Set the background panel as the content pane of the JFrame
        setContentPane(backgroundPanel);
        setVisible(true);
    }

    /**
     * Updates the time label and changes the greeting based on the time of day.
     */
    private void updateTime() {
        String timeStr = new SimpleDateFormat("HH:mm").format(new Date());
        timeLabel.setText(timeStr);

        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(new Date()));
        if (hour >= 5 && hour < 12) {
            greetingLabel.setText("Good Morning! Stay focused!");
        } else if (hour >= 12 && hour < 18) {
            greetingLabel.setText("Good Afternoon! Keep going!");
        } else {
            greetingLabel.setText("Good Evening! Relax and recharge!");
        }
    }

    /**
     * Main method to start the application.
     * Initializes background resources and runs the MainFrame on the Event Dispatch Thread (EDT).
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        BackgroundManager.initializeBackgrounds();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
