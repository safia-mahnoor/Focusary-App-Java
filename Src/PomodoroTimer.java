import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages the Pomodoro timer logic, including start, pause, reset,
 * and notifying observers of time-related events.
 */
public class PomodoroTimer {
    private Timer timer;
    private int remainingTime; // in seconds
    private boolean isRunning;

    private TimerObserver observer; // The single observer for timer events

    /**
     * Defines the different states of the Pomodoro timer.
     */
    public enum TimerState {
        FOCUS_TIME,
        SHORT_BREAK,
        LONG_BREAK
    }

    private TimerState currentState;

    /**
     * Constructor for PomodoroTimer.
     * @param durationInMinutes The initial duration of the timer in minutes.
     */
    public PomodoroTimer(int durationInMinutes) {
        this.remainingTime = durationInMinutes * 60;
        this.isRunning = false;
        this.currentState = TimerState.FOCUS_TIME; // Start with focus time by default
    }

    /**
     * Sets the observer that will receive notifications about timer events.
     * @param obs An instance of AbstractTimerObserver.
     */
    public void setObserver(TimerObserver obs) {
        this.observer = obs;
    }

    /**
     * Starts the timer. If already running, does nothing.
     * Notifies the observer when the timer starts.
     */
    public void start() {
        if (isRunning) return; // Prevent starting if already running

        isRunning = true;
        timer = new Timer(); // Create a new Timer instance
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (remainingTime > 0) {
                    remainingTime--;
                    if (observer != null) observer.onTimerTick(remainingTime);
                } else {
                    stop(); // Stop the current timer when time runs out
                    if (observer != null) {
                        // Notify the observer based on the current state that time has finished
                        if (currentState == TimerState.FOCUS_TIME) {
                            observer.onFocusTimeFinished();
                        } else if (currentState == TimerState.SHORT_BREAK) {
                            observer.onShortBreakFinished();
                        } else if (currentState == TimerState.LONG_BREAK) {
                            observer.onLongBreakFinished();
                        }
                    }
                }
            }
        }, 0, 1000); // Delay 0, repeat every 1000ms (1 second)

        if (observer != null) observer.onTimerStarted(); // Notify observer that timer has started
    }

    /**
     * Pauses the timer.
     * Notifies the observer when the timer is paused.
     */
    public void pause() {
        if (timer != null) {
            timer.cancel(); // Stop the timer task
            isRunning = false;
        }
        if (observer != null) observer.onTimerPaused(); // Notify observer that timer has paused
    }

    /**
     * Resets the timer to a new duration and state.
     * This method also implicitly pauses the timer before resetting.
     * @param durationInMinutes The new duration in minutes.
     * @param newState The new state (FOCUS_TIME, SHORT_BREAK, LONG_BREAK).
     */
    public void reset(int durationInMinutes, TimerState newState) {
        pause(); // Always pause before resetting to ensure timer is stopped
        this.remainingTime = durationInMinutes * 60; // Convert minutes to seconds
        this.currentState = newState;
        if (observer != null) observer.onTimerReset(); // Notify observer that timer has reset
    }

    /**
     * Gets the remaining time in the current session.
     * @return The remaining time in seconds.
     */
    public int getRemainingTime() {
        return remainingTime;
    }

    /**
     * Checks if the timer is currently running.
     * @return true if the timer is running, false otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Gets the current state of the Pomodoro timer (Focus, Short Break, or Long Break).
     * @return The current TimerState.
     */
    public TimerState getCurrentState() {
        return currentState;
    }

    /**
     * Stops the timer completely and cleans up the timer object.
     * This is typically called when the timer is reset or the application is closing.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel(); // Stop any currently scheduled tasks
            timer = null; // Clear the timer object to ensure a fresh one is created on next start
            isRunning = false;
        }
    }
}