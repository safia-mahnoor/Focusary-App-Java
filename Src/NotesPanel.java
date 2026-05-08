import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class NotesPanel extends JPanel {
    private JTextArea noteArea;
    private JSpinner dateSpinner;
    private JButton saveButton;

    // Temporary in-memory note store (Map<DateString, Note>)
    private Map<String, String> notesMap = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public NotesPanel() {
        setLayout(new BorderLayout());

        // Setup date spinner
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);

        // Top panel with date selector and save button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveButton = new JButton("Save Note");
        topPanel.add(new JLabel("Date:"));
        topPanel.add(dateSpinner);
        topPanel.add(saveButton);

        // Setup text area
        noteArea = new JTextArea(10, 30);
        JScrollPane scrollPane = new JScrollPane(noteArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Save action
        saveButton.addActionListener(e -> {
            String dateKey = dateFormat.format((Date) dateSpinner.getValue());
            String note = noteArea.getText().trim();
            if (!note.isEmpty()) {
                notesMap.put(dateKey, note);
                JOptionPane.showMessageDialog(this, "Note saved for " + dateKey);
            }
        });

        // Load note when date changes
        dateSpinner.addChangeListener(e -> {
            String dateKey = dateFormat.format((Date) dateSpinner.getValue());
            noteArea.setText(notesMap.getOrDefault(dateKey, ""));
        });
    }
}
