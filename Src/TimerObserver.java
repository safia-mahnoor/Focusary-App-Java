/**
 * An abstract observer class for the PomodoroTimer.
 * Defines the contract for classes that want to be notified of timer events.
 * Provides default empty implementations for some methods, allowing subclasses
 * to override only the events they care about.
 */
public abstract class TimerObserver {
    /**
     * Called by the PomodoroTimer on every tick (e.g., every second).
     * @param remainingTime The time remaining in the current session (in seconds).
     */
    public void onTimerTick(int remainingTime) {
        // Default empty implementation
    }

    /**
     * Called when the Focus time session has finished.
     * Subclasses MUST implement this method.
     */
    public abstract void onFocusTimeFinished();

    /**
     * Called when the Short Break session has finished.
     * Subclasses MUST implement this method.
     */
    public abstract void onShortBreakFinished();

    /**
     * Called when the Long Break session has finished.
     * Subclasses MUST implement this method.
     */
    public abstract void onLongBreakFinished();

    /**
     * Called when the timer starts.
     */
    public void onTimerStarted() {
        // Default empty implementation
    }

    /**
     * Called when the timer is paused.
     */
    public void onTimerPaused() {
        // Default empty implementation
    }

    /**
     * Called when the timer is reset.
     */
    public void onTimerReset() {
        // Default empty implementation
    }
}