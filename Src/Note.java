import java.io.Serializable;
import java.time.LocalDate;

public class Note implements Serializable {
    private static final long serialVersionUID = 1L;

    private String content;
    private LocalDate date;

    public Note() {
        // Default constructor
    }

    public Note(String content, LocalDate date) {
        this.content = content;
        this.date = date;
    }

    // Getters
    public String getContent() {
        return content;
    }

    public LocalDate getDate() {
        return date;
    }

    // Setters
    public void setContent(String content) {
        this.content = content;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Note{" +
                "date=" + date +
                ", content='" + content + '\'' +
                '}';
    }
}
