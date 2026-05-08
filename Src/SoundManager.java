import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;

/**
 * Manages playing sound effects and background music.
 * Supports WAV files.
 * Extends AbstractApplicationManager to participate in application lifecycle management.
 */
public class SoundManager extends ApplicationManager {
    private Clip currentMusicClip; // Clip for background music
    private Random random = new Random();
    private volatile boolean continuePlayingMusic = false; // Control flag for recursive playback

    // Define the path to the sounds folder
    private static final String SOUNDS_FOLDER = "Sounds"; 

    /**
     * Constructor for SoundManager.
     * Note: Initialization of resources is now handled by the initialize() method.
     */
    public SoundManager() {
        // No specific constructor logic needed here, as setup is in initialize()
    }

    /**
     * Initializes resources for the SoundManager.
     * Ensures the sounds folder exists.
     */
    @Override
    public void initialize() {
        File folder = new File(SOUNDS_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs(); // Create the folder if it doesn't exist
            System.out.println("Created sounds folder: " + SOUNDS_FOLDER);
        }
        System.out.println("SoundManager initialized. Looking for sounds in: " + folder.getAbsolutePath());
    }

    /**
     * Disposes of resources held by the SoundManager, specifically stopping any playing music.
     * This method should be called on application shutdown to release audio resources.
     */
    @Override
    public void dispose() {
        stop(); // Ensure any playing music is stopped and resources are released
        System.out.println("SoundManager disposed.");
    }

    /**
     * Plays a single sound effect (e.g., an alarm).
     * The method now looks for the sound file within the predefined SOUNDS_FOLDER.
     * @param soundFileName The name of the sound file (e.g., "alarm.wav").
     */
    public void playSound(String soundFileName) {
        // Construct the full path to the sound file
        String soundFilePath = SOUNDS_FOLDER + File.separator + soundFileName;
        try {
            File soundFile = new File(soundFilePath);
            if (soundFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                // Optional: add a LineListener to close the clip when finished playing
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } else {
                System.err.println("Sound file not found: " + soundFilePath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Plays a list of songs as background music in a loop, shuffled randomly.
     * Stops any currently playing music before starting new.
     * @param songFilePaths A list of paths to WAV song files.
     */
    public void playSongs(List<String> songFilePaths) {
        stop(); // Stop any current music immediately
        
        if (songFilePaths == null || songFilePaths.isEmpty()) {
            System.out.println("No songs provided to play.");
            return;
        }

        continuePlayingMusic = true; // Set flag to true to start playback loop

        // Shuffle the playlist
        List<String> shuffledSongs = new ArrayList<>(songFilePaths);
        Collections.shuffle(shuffledSongs);

        playNextSong(shuffledSongs, 0); // Start playing from the first song
    }

    /**
     * Helper method to play the next song in the shuffled playlist recursively.
     * @param playlist The shuffled list of song file paths.
     * @param currentIndex The current index in the playlist to play.
     */
    private void playNextSong(List<String> playlist, int currentIndex) {
        // If the stop flag is set, immediately return to halt further playback
        if (!continuePlayingMusic) {
            System.out.println("Stopping music loop as requested (recursive call exit).");
            return;
        }

        if (playlist.isEmpty()) {
            System.out.println("Playlist is empty, cannot play next song.");
            return;
        }

        // Calculate the actual index for the current playback, handling loop-around
        final int nextIndexToPlay = currentIndex % playlist.size();

        String songPath = playlist.get(nextIndexToPlay);
        Clip newClip = null; // Declare newClip here

        try {
            File songFile = new File(songPath);
            if (songFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(songFile);
                newClip = AudioSystem.getClip(); // Get new clip
                newClip.open(audioIn);

                // Assign to currentMusicClip only if successfully opened
                // This ensures currentMusicClip always points to a valid, open clip or null
                currentMusicClip = newClip;

                currentMusicClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        // Crucial: capture continuePlayingMusic flag state before closing and playing next
                        boolean shouldContinue = continuePlayingMusic; 
                        
                        // Close this clip. Use the local currentMusicClip for this specific event listener
                        // to ensure it closes the clip that triggered this event.
                        if (currentMusicClip != null && currentMusicClip.isOpen()) {
                             currentMusicClip.close(); 
                        }

                        if (shouldContinue) {
                            playNextSong(playlist, nextIndexToPlay + 1);
                        } else {
                            System.out.println("Song finished, but music loop was stopped externally.");
                        }
                    }
                });
                currentMusicClip.start();
                System.out.println("Playing: " + songPath + " (Index: " + nextIndexToPlay + ")");
            } else {
                System.err.println("Music file not found: " + songPath + ". Skipping to next song.");
                // Ensure the problematic path doesn't stop the loop
                playNextSong(playlist, nextIndexToPlay + 1); 
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing music: " + e.getMessage() + ". Skipping to next song. Song: " + songPath);
            e.printStackTrace();
            if (newClip != null && newClip.isOpen()) { // Ensure resource is closed on error
                newClip.close();
            }
            // Ensure the error doesn't stop the loop
            playNextSong(playlist, nextIndexToPlay + 1); 
        }
    }

    /**
     * Stops any currently playing background music and halts the playback loop.
     */
    public void stop() {
        continuePlayingMusic = false; // Immediately signal termination to any ongoing recursive calls

        // Ensure we operate on the current clip if it exists
        if (currentMusicClip != null) {
            System.out.println("Attempting to stop and close current music clip.");
            if (currentMusicClip.isRunning()) {
                currentMusicClip.stop(); // Stop playback if running
                System.out.println("Clip stopped.");
            }
            if (currentMusicClip.isOpen()) {
                currentMusicClip.close(); // Close the clip to release resources
                System.out.println("Clip closed.");
            }
            currentMusicClip = null; // Clear the reference to signal no active clip
        } else {
            System.out.println("No active music clip found to stop.");
        }
    }
}
