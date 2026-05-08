import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * A panel for managing a To-Do list, allowing users to add and view tasks.
 * It interacts with the TaskManager to persist tasks.
 */
public class ToDoPanel extends JPanel {
    private DefaultListModel<Task> taskListModel; // Model for the JList of tasks
    private JList<Task> taskList;                 // Displays the list of tasks
    private JTextField newTaskField;              // Input field for new tasks
    private JButton addTaskButton;                // Button to add a new task
    private JButton removeTaskButton;             // Button to remove selected task

    public ToDoPanel() {
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout with gaps
        setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black background
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding around the panel

        // === Title Label ===
        JLabel titleLabel = new JLabel("Your Tasks", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        // === Task List Display ===
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("SansSerif", Font.PLAIN, 18));
        taskList.setForeground(Color.BLACK); // Make text readable against white background
        taskList.setBackground(Color.WHITE);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one task can be selected

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)); // Add a subtle border
        add(scrollPane, BorderLayout.CENTER);

        // Load existing tasks on startup
        loadTasks();

        // === Input and Control Panel for Tasks ===
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout(10, 0)); // For input field and add button
        controlPanel.setOpaque(false); // Make transparent

        newTaskField = new JTextField("Enter a new task...");
        newTaskField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        newTaskField.setForeground(Color.DARK_GRAY); // Placeholder text color
        newTaskField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (newTaskField.getText().equals("Enter a new task...")) {
                    newTaskField.setText("");
                    newTaskField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (newTaskField.getText().isEmpty()) {
                    newTaskField.setText("Enter a new task...");
                    newTaskField.setForeground(Color.DARK_GRAY);
                }
            }
        });
        controlPanel.add(newTaskField, BorderLayout.CENTER);

        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); // For Add and Remove buttons
        buttonGroupPanel.setOpaque(false);

        addTaskButton = new JButton("Add Task");
        addTaskButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        addTaskButton.setBackground(new Color(138, 43, 226, 200)); // Purple background
        addTaskButton.setForeground(Color.WHITE);
        addTaskButton.setFocusPainted(false);
        addTaskButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1)); // White border
        addTaskButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor
        buttonGroupPanel.add(addTaskButton);

        // === Remove Task Button ===
        removeTaskButton = new JButton("Remove Selected");
        removeTaskButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        removeTaskButton.setBackground(new Color(200, 50, 50, 200)); // Red background for remove
        removeTaskButton.setForeground(Color.WHITE);
        removeTaskButton.setFocusPainted(false);
        removeTaskButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        removeTaskButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonGroupPanel.add(removeTaskButton);


        controlPanel.add(buttonGroupPanel, BorderLayout.EAST);

        // Add action listener for the Add Task button
        addTaskButton.addActionListener(e -> addNewTask());

        // Allow adding task by pressing Enter in the text field
        newTaskField.addActionListener(e -> addNewTask());

        // Add action listener for the Remove Task button
        removeTaskButton.addActionListener(e -> removeSelectedTask());


        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Loads tasks from TaskManager and populates the JList.
     * This method is called upon panel initialization.
     */
    private void loadTasks() {
        taskListModel.clear(); // Clear existing tasks in the list model
        List<Task> tasks = TaskManager.loadTasks(); // Load tasks from persistence
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                taskListModel.addElement(task); // Add each task to the list model
            }
        }
        System.out.println("ToDoPanel: Tasks loaded on init. Count: " + tasks.size()); // Debug
    }

    /**
     * Adds a new task from the input field to the list and saves all tasks.
     */
    private void addNewTask() {
        String taskDesc = newTaskField.getText().trim();
        if (!taskDesc.isEmpty() && !taskDesc.equals("Enter a new task...")) {
            Task newTask = new Task(taskDesc);
            taskListModel.addElement(newTask); // Add to display
            
            saveAllTasksFromListModel(); // <<< Explicitly call save after add
            
            newTaskField.setText("Enter a new task..."); // Reset input field
            newTaskField.setForeground(Color.DARK_GRAY);
            JOptionPane.showMessageDialog(this, "Task added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("ToDoPanel: Task added: " + newTask.getDescription()); // Debug
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a task description.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Removes the selected task from the list and saves the updated list.
     */
    private void removeSelectedTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String removedTaskDesc = taskListModel.getElementAt(selectedIndex).getDescription(); // Debug
            taskListModel.remove(selectedIndex); // Remove from display
            saveAllTasksFromListModel(); // <<< Explicitly call save after remove
            JOptionPane.showMessageDialog(this, "Task removed!", "Success", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("ToDoPanel: Task removed: " + removedTaskDesc); // Debug
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to remove.", "Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Helper method to get all tasks from the current list model and save them.
     */
    private void saveAllTasksFromListModel() {
        List<Task> allTasks = new ArrayList<>();
        for (int i = 0; i < taskListModel.size(); i++) {
            allTasks.add(taskListModel.getElementAt(i));
        }
        TaskManager.saveTasks(allTasks);
        System.out.println("ToDoPanel: All tasks saved. Current count: " + allTasks.size()); // Debug
    }
}
