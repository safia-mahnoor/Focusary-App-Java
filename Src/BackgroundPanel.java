import java.awt.*;
import java.io.File;
import javax.swing.*; // Needed for checking file existence

public class BackgroundPanel extends JPanel {
    private Image backgroundImage;
    private String currentImagePath; // Added to store the path of the current image

    // Default constructor loads default image
    public BackgroundPanel() {
        // Attempt to load 'Default.jpg' from the current directory
        // Provide a fallback if 'Default.jpg' is not found.
        String defaultPath = "Default.jpg";
        File defaultFile = new File(defaultPath);
        if (defaultFile.exists()) {
            setBackgroundImage(defaultPath);
        } else {
            // Fallback: Use a placeholder image or just set backgroundImage to null
            System.out.println("Default.jpg not found. Using a placeholder or no background.");
            // backgroundImage = new ImageIcon(getClass().getResource("/resources/default_placeholder.jpg")).getImage();
            backgroundImage = null; // No default image
            currentImagePath = null; // No default path
        }
    }

    // Constructor with custom image path
    public BackgroundPanel(String initialPath) {
        setBackgroundImage(initialPath);
    }

    public void setBackgroundImage(String path) {
        if (path == null || path.isEmpty()) {
            backgroundImage = null;
            currentImagePath = null;
            System.out.println("Cleared background image.");
        } else {
            try {
                File imageFile = new File(path);
                if (imageFile.exists() && !imageFile.isDirectory()) {
                    backgroundImage = new ImageIcon(imageFile.getAbsolutePath()).getImage();
                    currentImagePath = path; // Store the path when image is successfully set
                } else {
                    System.out.println("Background image file not found or is a directory: " + path);
                    backgroundImage = null;
                    currentImagePath = null; // Clear path if file not found
                }
            } catch (Exception e) {
                System.err.println("Failed to load background: " + path + ". Error: " + e.getMessage());
                backgroundImage = null;
                currentImagePath = null; // Clear path on error
            }
        }
        repaint();
    }

    /**
     * Returns the path of the currently set background image.
     * @return The file path of the current background image, or null if none is set.
     */
    public String getCurrentImagePath() {
        return currentImagePath;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}