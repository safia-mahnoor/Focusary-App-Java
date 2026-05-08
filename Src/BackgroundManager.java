import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List; // Import Arrays for utility methods

public class BackgroundManager {
    // We no longer need DATA_FILE as we won't be serializing/deserializing
    // private static final String DATA_FILE = "backgrounds.ser"; 
    
    private static final String BG_FOLDER = "Backgrounds"; // Folder where background images are stored

    /**
     * Initializes the background management.
     * This method now primarily ensures the background folder exists.
     * It no longer saves paths to a .ser file as per new requirement.
     */
    public static void initializeBackgrounds() {
        File folder = new File(BG_FOLDER);
        if (!folder.exists()) {
            // Create the folder if it does not exist.
            // This is useful if the user doesn't create it manually.
            if (folder.mkdirs()) {
                System.out.println("BackgroundManager: Created background folder: " + BG_FOLDER);
            } else {
                System.err.println("BackgroundManager: Failed to create background folder: " + BG_FOLDER);
            }
        } else {
            System.out.println("BackgroundManager: Background folder already exists: " + BG_FOLDER);
        }
    }

    /**
     * Loads a list of absolute paths of background images directly from the
     * predefined 'Backgrounds' folder. This method no longer deserializes from a file.
     * It scans the folder every time it's called.
     * @return A List of String containing absolute paths of .png and .jpg files.
     * Returns an empty list if the folder does not exist, is not a directory,
     * or contains no supported image files.
     */
    public static List<String> loadBackgrounds() {
        List<String> paths = new ArrayList<>();
        File folder = new File(BG_FOLDER);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg")
            );

            if (files != null) {
                // Sort files by name for consistent order in selection dialogs
                Arrays.sort(files); 
                for (File file : files) {
                    paths.add(file.getAbsolutePath()); // Add absolute path
                }
            }
            System.out.println("BackgroundManager: Loaded " + paths.size() + " backgrounds directly from folder.");
        } else {
            System.err.println("BackgroundManager: Background folder not found or is not a directory: " + BG_FOLDER);
        }
        return paths;
    }
}
