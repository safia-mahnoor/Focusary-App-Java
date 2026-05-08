import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * The main panel for the Pomodoro timer functionality.
 * It displays the timer, motivational quotes, allows task selection,
 * and controls the Pomodoro cycle (Focus, Short Break, Long Break).
 * This class acts as a concrete observer for the PomodoroTimer.
 */
public class PomodoroPanel extends JPanel {
    private JLabel timerLabel;
    private JTextArea quoteArea;
    private JTextField taskDisplayField; // Displays the currently selected task
    private JButton chooseTaskButton;     // Button to select tasks from saved list
    // Removed addTaskButton as per user request

    private JButton startButton;
    private JButton pauseButton;
    private JButton resetButton;

    private JButton focusButton, shortBreakButton, longBreakButton; // Mode selection buttons

    private PomodoroTimer pomodoroTimer;
    private QuoteManager quoteManager;
    private SoundManager soundManager;

    private List<Task> currentTasks; // List to hold loaded tasks
    private Task selectedTask;       // To hold the currently selected task

    private static final int FOCUS_DURATION = 25; // 25 minutes for focus session
    private static final int SHORT_BREAK_DURATION = 5; // 5 minutes for short break
    private static final int LONG_BREAK_DURATION = 15; // 15 minutes for long break

    /**
     * Constructor for the PomodoroPanel.
     * Initializes UI components, sets up layouts, and connects to managers.
     * @param quoteManager Manages and provides random quotes.
     * @param soundManager Manages sound playback (for alarms and music).
     */
    public PomodoroPanel(QuoteManager quoteManager, SoundManager soundManager) {
        this.quoteManager = quoteManager;
        this.soundManager = soundManager;
        this.pomodoroTimer = new PomodoroTimer(FOCUS_DURATION); // Initialize with default focus time

        setOpaque(false); // Make transparent to see background
        setLayout(null); // Use null layout for absolute positioning

        // Load tasks at startup and select the first one if available
        this.currentTasks = TaskManager.loadTasks();
        if (currentTasks.isEmpty()) {
            Task defaultTask = new Task("No task selected. Add or choose one!");
            currentTasks.add(defaultTask);
            TaskManager.saveTasks(currentTasks); // Save this default task
            selectedTask = defaultTask;
        } else {
            selectedTask = currentTasks.get(0); // Select the first task by default
        }


        // === TASK DISPLAY FIELD (TOP CENTER) ===
        taskDisplayField = new JTextField(selectedTask.getDescription());
        taskDisplayField.setFont(new Font("SansSerif", Font.PLAIN, 20));
        taskDisplayField.setForeground(Color.WHITE);
        taskDisplayField.setBackground(new Color(0, 0, 0, 0));
        taskDisplayField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        taskDisplayField.setHorizontalAlignment(SwingConstants.CENTER);
        taskDisplayField.setOpaque(false);
        taskDisplayField.setCaretColor(Color.WHITE);
        taskDisplayField.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        taskDisplayField.setEditable(false); // Not editable directly
        add(taskDisplayField);

        // === CHOOSE TASK BUTTON ===
        chooseTaskButton = new JButton("Choose Task");
        styleModeButton(chooseTaskButton); // Use similar styling
        chooseTaskButton.setFont(new Font("SansSerif", Font.BOLD, 14)); // Adjust font size
        add(chooseTaskButton);

        // Action listener for Choose Task Button
        chooseTaskButton.addActionListener(e -> {
            currentTasks = TaskManager.loadTasks(); // Reload tasks to ensure up-to-date list
            if (currentTasks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No tasks available. Please add a task first from the 'Tasks' menu!", "No Tasks", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] taskDescriptions = currentTasks.stream()
                                                    .map(Task::getDescription)
                                                    .toArray(String[]::new);

            String selectedTaskDesc = (String) JOptionPane.showInputDialog(
                this,
                "Select a task:",
                "Choose Task",
                JOptionPane.PLAIN_MESSAGE,
                null, // Icon
                taskDescriptions,
                selectedTask != null ? selectedTask.getDescription() : null // Initial selection
            );

            if (selectedTaskDesc != null) {
                // Find the selected Task object
                for (Task task : currentTasks) {
                    if (task.getDescription().equals(selectedTaskDesc)) {
                        selectedTask = task;
                        break;
                    }
                }
                taskDisplayField.setText(selectedTask.getDescription());
            }
        });


        // === MODE SELECTION BUTTONS (Below Task Field) ===
        JPanel modeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        modeButtonsPanel.setOpaque(false);

        focusButton = new JButton("Focus");
        shortBreakButton = new JButton("Short Break");
        longBreakButton = new JButton("Long Break");

        styleModeButton(focusButton);
        styleModeButton(shortBreakButton);
        styleModeButton(longBreakButton);

        modeButtonsPanel.add(focusButton);
        modeButtonsPanel.add(shortBreakButton);
        modeButtonsPanel.add(longBreakButton);
        add(modeButtonsPanel);

        // === TIMER LABEL ===
        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 150));
        timerLabel.setForeground(Color.WHITE);
        add(timerLabel);

        // === CONTROL BUTTONS ===
        startButton = new JButton("Start");
        pauseButton = new JButton("Pause");
        resetButton = new JButton("Reset");

        styleControlButton(startButton);
        styleControlButton(pauseButton);
        styleControlButton(resetButton);

        add(startButton);
        add(pauseButton);
        add(resetButton);

        // Initially, the pause button is disabled until timer starts
        pauseButton.setEnabled(false);

        // === QUOTE AREA (TOP-RIGHT) ===
        quoteArea = new JTextArea(quoteManager.getRandomQuote());
        quoteArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        quoteArea.setForeground(Color.WHITE);
        quoteArea.setOpaque(false);
        quoteArea.setEditable(false);
        quoteArea.setWrapStyleWord(true);
        quoteArea.setLineWrap(true);
        quoteArea.setFocusable(false);
        add(quoteArea);

        // === Observer Implementation (Anonymous Inner Class for Polymorphism/Inheritance) ===
        // This is where PomodoroPanel acts as a concrete observer to PomodoroTimer events.
       TimerObserver panelObserver = new TimerObserver() {
            @Override
            public void onTimerTick(int remaining) {
                SwingUtilities.invokeLater(() -> {
                    String timeText = String.format("%02d:%02d", remaining / 60, remaining % 60);
                    timerLabel.setText(timeText);
                });
            }

            @Override
            public void onFocusTimeFinished() {
                SwingUtilities.invokeLater(() -> {
                    soundManager.playSound("alarm.wav");
                    JOptionPane.showMessageDialog(PomodoroPanel.this, "Time's up! Take a short break.");
                    pomodoroTimer.reset(SHORT_BREAK_DURATION, PomodoroTimer.TimerState.SHORT_BREAK);
                    pomodoroTimer.start();
                    // UI updates handled by onTimerStarted/onTimerTick for consistency
                });
            }

            @Override
            public void onShortBreakFinished() {
                SwingUtilities.invokeLater(() -> {
                    soundManager.playSound("alarm.wav");
                    int choice = JOptionPane.showConfirmDialog(PomodoroPanel.this,
                            "Break is over! Do you want to go back to focus?", "Pomodoro Break",
                            JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        pomodoroTimer.reset(FOCUS_DURATION, PomodoroTimer.TimerState.FOCUS_TIME);
                        pomodoroTimer.start();
                    } else {
                        pomodoroTimer.stop();
                        pomodoroTimer.reset(FOCUS_DURATION, PomodoroTimer.TimerState.FOCUS_TIME);
                        timerLabel.setText("25:00"); // Explicitly reset display
                        updateModeButtonColors(null); // No mode selected visually
                    }
                    // UI updates handled by onTimerStarted/onTimerReset for consistency
                });
            }

            @Override
            public void onLongBreakFinished() {
                 SwingUtilities.invokeLater(() -> {
                    soundManager.playSound("alarm.wav");
                    int choice = JOptionPane.showConfirmDialog(PomodoroPanel.this,
                            "Long Break is over! Do you want to go back to focus?", "Pomodoro Long Break",
                            JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        pomodoroTimer.reset(FOCUS_DURATION, PomodoroTimer.TimerState.FOCUS_TIME);
                        pomodoroTimer.start();
                    } else {
                        pomodoroTimer.stop();
                        pomodoroTimer.reset(FOCUS_DURATION, PomodoroTimer.TimerState.FOCUS_TIME);
                        timerLabel.setText("25:00"); // Explicitly reset display
                        updateModeButtonColors(null); // No mode selected visually
                    }
                    // UI updates handled by onTimerStarted/onTimerReset for consistency
                });
            }

            @Override
            public void onTimerStarted() {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(false);
                    pauseButton.setEnabled(true);
                    // Update mode button colors based on timer's current state
                    if (pomodoroTimer.getCurrentState() == PomodoroTimer.TimerState.FOCUS_TIME) {
                        updateModeButtonColors(focusButton);
                    } else if (pomodoroTimer.getCurrentState() == PomodoroTimer.TimerState.SHORT_BREAK) {
                        updateModeButtonColors(shortBreakButton);
                    } else if (pomodoroTimer.getCurrentState() == PomodoroTimer.TimerState.LONG_BREAK) {
                        updateModeButtonColors(longBreakButton);
                    }
                });
            }

            @Override
            public void onTimerPaused() {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                });
            }

            @Override
            public void onTimerReset() {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    updateModeButtonColors(focusButton); // Default to focus after reset
                });
            }
        };

        // Register the observer with the timer
        this.pomodoroTimer.setObserver(panelObserver);

        // === Component Listener for Dynamic Positioning ===
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();

                // Position task-related elements
                int taskFieldWidth = 600;
                int taskFieldX = (panelWidth - taskFieldWidth) / 2;
                taskDisplayField.setBounds(taskFieldX, 50, taskFieldWidth, 30);
                chooseTaskButton.setBounds(taskFieldX - 110 - 10, 50, 110, 30); // Position choose button to the left

                // Removed addTaskButton positioning here

                // Center modeButtonsPanel
                int modePanelWidth = modeButtonsPanel.getPreferredSize().width;
                modeButtonsPanel.setBounds((panelWidth - modePanelWidth) / 2, 100, modePanelWidth, 40);

                // Recalculate and set bounds for the timer label to center it
                int timerLabelCalculatedWidth = Math.max(timerLabel.getPreferredSize().width, 700);
                int timerLabelCalculatedHeight = Math.max(timerLabel.getPreferredSize().height, 150);
                
                int timerX = (panelWidth - timerLabelCalculatedWidth) / 2;
                int timerY = panelHeight / 2 - 50;
                timerLabel.setBounds(timerX, timerY, timerLabelCalculatedWidth, timerLabelCalculatedHeight);

                // Recalculate and set bounds for the control buttons below the timer
                int controlButtonHeight = 60; // Fixed height for control buttons
                int startButtonCalculatedWidth = startButton.getPreferredSize().width;
                int pauseButtonCalculatedWidth = pauseButton.getPreferredSize().width;
                int resetButtonCalculatedWidth = resetButton.getPreferredSize().width;

                int totalControlButtonsWidth = startButtonCalculatedWidth + pauseButtonCalculatedWidth + resetButtonCalculatedWidth + (20 * 2); // Sum of widths + 2 gaps
                int controlButtonY = timerY + timerLabelCalculatedHeight + 40;
                int controlButtonsStartX = (panelWidth - totalControlButtonsWidth) / 2;

                startButton.setBounds(controlButtonsStartX, controlButtonY, startButtonCalculatedWidth, controlButtonHeight);
                pauseButton.setBounds(controlButtonsStartX + startButtonCalculatedWidth + 20, controlButtonY, pauseButtonCalculatedWidth, controlButtonHeight);
                resetButton.setBounds(pauseButton.getX() + pauseButtonCalculatedWidth + 20, controlButtonY, resetButtonCalculatedWidth, controlButtonHeight);
            
                // Keep quote area top-right
                quoteArea.setBounds(panelWidth - 290, 10, 280, 60);
            }
        });

        // === Button Action Listeners ===
        // These listeners now primarily interact with the PomodoroTimer,
        // and the UI updates are delegated to the observer (`panelObserver`).
        startButton.addActionListener(e -> {
            if (!pomodoroTimer.isRunning()) {
                // If timer is at 0, reset based on current mode before starting
                if (pomodoroTimer.getRemainingTime() == 0) {
                    if (pomodoroTimer.getCurrentState() == PomodoroTimer.TimerState.FOCUS_TIME) {
                        pomodoroTimer.reset(FOCUS_DURATION, PomodoroTimer.TimerState.FOCUS_TIME);
                    } else if (pomodoroTimer.getCurrentState() == PomodoroTimer.TimerState.SHORT_BREAK) {
                        pomodoroTimer.reset(SHORT_BREAK_DURATION, PomodoroTimer.TimerState.SHORT_BREAK);
                    } else if (pomodoroTimer.getCurrentState() == PomodoroTimer.TimerState.LONG_BREAK) {
                        pomodoroTimer.reset(LONG_BREAK_DURATION, PomodoroTimer.TimerState.LONG_BREAK);
                    }
                }
                pomodoroTimer.start();
            }
        });

        pauseButton.addActionListener(e -> {
            if (pomodoroTimer.isRunning()) {
                pomodoroTimer.pause();
            }
        });

        resetButton.addActionListener(e -> {
            pomodoroTimer.stop(); // Stop completely before resetting
            pomodoroTimer.reset(FOCUS_DURATION, PomodoroTimer.TimerState.FOCUS_TIME); // Reset to Focus mode
            timerLabel.setText("25:00"); // Explicitly set text for immediate visual feedback
        });

        // Mode selection button actions (these implicitly stop the timer and reset state)
        focusButton.addActionListener(e -> {
            pomodoroTimer.stop();
            pomodoroTimer.reset(FOCUS_DURATION, PomodoroTimer.TimerState.FOCUS_TIME);
            timerLabel.setText("25:00");
        });

        shortBreakButton.addActionListener(e -> {
            pomodoroTimer.stop();
            pomodoroTimer.reset(SHORT_BREAK_DURATION, PomodoroTimer.TimerState.SHORT_BREAK);
            timerLabel.setText("05:00");
        });

        longBreakButton.addActionListener(e -> {
            pomodoroTimer.stop();
            pomodoroTimer.reset(LONG_BREAK_DURATION, PomodoroTimer.TimerState.LONG_BREAK);
            timerLabel.setText("15:00");
        });

        // Initialize button colors and state (initial state for buttons and colors)
        updateModeButtonColors(focusButton);
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        // Trigger resize event manually once after everything is added and initialized
        // to correctly set initial bounds based on the actual panel size.
        SwingUtilities.invokeLater(() -> this.dispatchEvent(new ComponentEvent(this, ComponentEvent.COMPONENT_RESIZED)));
    }

    /**
     * Styles the mode selection buttons (Focus, Short Break, Long Break),
     * and now also applies to the "Choose Task" button.
     * @param button The JButton to style.
     */
    private void styleModeButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(100, 100, 100, 150));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                JButton btn = (JButton) c;
                ButtonModel model = btn.getModel();

                Color background = btn.getBackground();
                if (model.isRollover() || model.isPressed()) {
                    background = background.brighter();
                }
                
                g2.setColor(background);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);

                super.paint(g2, c);

                g2.dispose();
            }
        });
    }

    /**
     * Styles the main control buttons (Start, Pause, Reset).
     * @param button The JButton to style.
     */
    private void styleControlButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(138, 43, 226, 150));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                JButton btn = (JButton) c;
                ButtonModel model = btn.getModel();

                Color background = btn.getBackground();
                if (model.isRollover() || model.isPressed()) {
                    background = background.brighter();
                }

                g2.setColor(background);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 30, 30);
                
                super.paint(g2, c);

                g2.dispose();
            }
        });
    }

    /**
     * Updates the background color of mode selection buttons to highlight the active one.
     * @param activeButton The button to highlight, or null to unhighlight all.
     */
    private void updateModeButtonColors(JButton activeButton) {
        JButton[] buttons = {focusButton, shortBreakButton, longBreakButton};
        for (JButton btn : buttons) {
            if (btn == activeButton) {
                btn.setBackground(new Color(138, 43, 226, 200));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 2),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            } else {
                btn.setBackground(new Color(100, 100, 100, 150));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
            btn.repaint();
        }
    }

    /**
     * Sets a random quote in the quote area.
     */
    public void setRandomQuote() {
        quoteArea.setText(quoteManager.getRandomQuote());
    }
}
