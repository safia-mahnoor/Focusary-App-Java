import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages saving and loading of a list of Task objects to/from a file.
 * Uses Java's object serialization for persistence.
 */
public class TaskManager {
    private static final String TASK_FILE = "tasks.ser"; // File name for saving multiple tasks

    /**
     * Saves a list of Task objects to a file.
     * @param tasks The List of Task objects to be saved.
     */
    public static void saveTasks(List<Task> tasks) {
        System.out.println("TaskManager: Attempting to save tasks to " + TASK_FILE); // Debug
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(TASK_FILE))) {
            out.writeObject(new ArrayList<>(tasks)); // Write a copy of the list
            System.out.println("TaskManager: Tasks saved successfully. Total: " + tasks.size());
        } catch (IOException e) {
            System.err.println("TaskManager: Error saving tasks to " + TASK_FILE + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a list of Task objects from a file.
     * If the file does not exist, an empty list is returned.
     * If any other error occurs during loading, an empty list is returned.
     * @return A List of loaded Task objects, or an empty list if none found/error.
     */
    @SuppressWarnings("unchecked") // Suppress warning for unchecked cast
    public static List<Task> loadTasks() {
        System.out.println("TaskManager: Attempting to load tasks from " + TASK_FILE); // Debug
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(TASK_FILE))) {
            Object obj = in.readObject();
            if (obj instanceof ArrayList) {
                // Safely cast the read object to ArrayList<Task>
                List<Task> loadedTasks = (List<Task>) obj;
                System.out.println("TaskManager: Tasks loaded successfully. Total: " + loadedTasks.size());
                return loadedTasks;
            } else {
                System.err.println("TaskManager: Loaded object is not an ArrayList of tasks. Returning empty list.");
                return new ArrayList<>(); // Return empty if not the expected type
            }
        } catch (FileNotFoundException e) {
            System.out.println("TaskManager: No tasks file found ('" + TASK_FILE + "'). Returning empty list.");
            return new ArrayList<>(); // Return empty list if no file exists
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("TaskManager: Error loading tasks from " + TASK_FILE + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on other errors
        }
    }
}

