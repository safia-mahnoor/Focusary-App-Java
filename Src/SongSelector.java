import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to load song file paths from a given folder.
 * Currently supports only .wav files.
 */
public class SongSelector {

    // Define the default sounds folder path - CHANGED TO "Sounds" (capital S)
    private static final String DEFAULT_SOUNDS_FOLDER = "Sounds";

    /**
     * Loads absolute paths of .wav files from a specified folder.
     * @param folderPath The path to the folder to scan for songs.
     * @return An ArrayList of String containing absolute paths of .wav files.
     * Returns an empty list if the folder does not exist, is not a directory, or contains no .wav files.
     */
    public static ArrayList<String> loadSongsFromFolder(String folderPath) {
        ArrayList<String> songs = new ArrayList<>();
        File folder = new File(folderPath);

        // Check if the provided path exists and is a directory
        if (folder.exists() && folder.isDirectory()) {
            // List files that end with ".wav" (case-insensitive)
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

            if (files != null) {
                for (File file : files) {
                    // Add the absolute path of each .wav file
                    songs.add(file.getAbsolutePath());
                }
            }
        } else {
            System.err.println("Folder not found or is not a directory: " + folderPath);
        }

        return songs;
    }

    /**
     * Loads absolute paths of .wav files from the predefined default sounds folder ("Sounds").
     * This method is intended for use when the music is already organized within the project.
     * @return An ArrayList of String containing absolute paths of .wav files found in the default sounds folder.
     * Returns an empty list if the folder does not exist or contains no .wav files.
     */
    public static ArrayList<String> loadSongsFromDefaultFolder() {
        return loadSongsFromFolder(DEFAULT_SOUNDS_FOLDER);
    }
}