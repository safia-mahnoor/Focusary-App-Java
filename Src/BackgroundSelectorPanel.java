import java.awt.*;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A JPanel that allows users to select different background images for the main application.
 * It now uses a JOptionPane to present a list of backgrounds for selection.
 */
public class BackgroundSelectorPanel extends JPanel {
    private BackgroundPanel targetBackgroundPanel; // The panel whose background will be changed
    private JButton backgroundSelectionButton; // The main button to trigger background selection

    /**
     * Constructor for BackgroundSelectorPanel.
     * @param backgroundPanel The BackgroundPanel instance whose image will be updated.
     */
    public BackgroundSelectorPanel(BackgroundPanel backgroundPanel) {
        this.targetBackgroundPanel = backgroundPanel;
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // Align to top-left with some spacing
        setOpaque(false); // Make transparent

        // === Backgrounds Selection Button ===
        backgroundSelectionButton = new JButton("Backgrounds");
        // Apply consistent styling for the button
        styleBackgroundButton(backgroundSelectionButton);
        
        backgroundSelectionButton.addActionListener(e -> {
            System.out.println("Backgrounds button clicked."); // Debug print

            List<String> backgroundPaths = BackgroundManager.loadBackgrounds();
            
            System.out.println("Loaded backgrounds: " + backgroundPaths.size() + " images found."); // Debug print
            for (String path : backgroundPaths) {
                System.out.println("  - " + path); // List found paths
            }

            if (backgroundPaths.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No background images found in 'Backgrounds' folder. Please add .png or .jpg files.", "No Backgrounds", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("No backgrounds found in 'Backgrounds' folder."); // Debug print
                return;
            }

            // Extract just the file names for display in the dialog
            String[] backgroundNames = backgroundPaths.stream()
                                                    .map(path -> new File(path).getName())
                                                    .toArray(String[]::new);

            // Use JOptionPane.showInputDialog for selection
            String selectedBackgroundName = (String) JOptionPane.showInputDialog(
                this,
                "Select a background image:",
                "Choose Background",
                JOptionPane.PLAIN_MESSAGE,
                null, // Icon
                backgroundNames,
                targetBackgroundPanel.getCurrentImagePath() != null ? new File(targetBackgroundPanel.getCurrentImagePath()).getName() : (backgroundNames.length > 0 ? backgroundNames[0] : null) // Default selection
            );

            if (selectedBackgroundName != null) {
                // Find the full path of the selected background
                String fullPathToSet = backgroundPaths.stream()
                                                     .filter(path -> new File(path).getName().equals(selectedBackgroundName))
                                                     .findFirst()
                                                     .orElse(null);
                if (fullPathToSet != null) {
                    System.out.println("User selected background: " + selectedBackgroundName + " (Path: " + fullPathToSet + ")"); // Debug print
                    targetBackgroundPanel.setBackgroundImage(fullPathToSet);
                } else {
                    System.err.println("Error: Full path not found for selected background name: " + selectedBackgroundName); // Debug
                }
            } else {
                System.out.println("User cancelled background selection."); // Debug print
            }
        });

        add(backgroundSelectionButton); // Add the main "Backgrounds" selection button
    }

    /**
     * Styles the background selection button to match the aesthetic of other navigation buttons.
     * @param button The JButton to style.
     */
    private void styleBackgroundButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.BLACK); // Changed to black foreground
        button.setBackground(new Color(220, 220, 220, 200)); // Lighter, slightly transparent background
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150, 150), 1), // Softer border
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setOpaque(true);
        button.setContentAreaFilled(false); // Still custom painted
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
                    background = background.darker(); // Darken on hover/press
                }
                
                g2.setColor(background);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8); // Slightly less rounded corners (adjust as needed)

                super.paint(g2, c);

                g2.dispose();
            }
        });
    }
}
