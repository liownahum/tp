package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps recent command submissions and an unfinished draft for session-only recall.
 * All access is confined to the JavaFX application thread.
 */
final class CommandHistory {
    private static final int MAX_ENTRIES = 100;

    private final List<String> entries = new ArrayList<>();
    private int position;
    private String draft = "";

    /**
     * Records a nonblank submission and starts a fresh browsing session.
     * Keeps repeated commands and their exact text, evicting the oldest at capacity.
     */
    void add(String command) {
        requireNonNull(command);
        if (command.isBlank()) {
            return;
        }
        if (entries.size() == MAX_ENTRIES) {
            entries.remove(0);
        }
        entries.add(command);
        resetNavigation();
    }

    /**
     * Returns an older submission, saving the current input when browsing starts.
     * Stops at the oldest entry; empty history leaves the input unchanged.
     */
    String previous(String currentInput) {
        requireNonNull(currentInput);
        if (entries.isEmpty()) {
            return currentInput;
        }
        if (position == entries.size()) {
            draft = currentInput;
        }
        if (position > 0) {
            position--;
        }
        return entries.get(position);
    }

    /**
     * Returns a newer submission or restores the draft after the newest entry.
     * Leaves the current input unchanged when browsing has already ended.
     */
    String next(String currentInput) {
        requireNonNull(currentInput);
        if (position == entries.size()) {
            return currentInput;
        }
        position++;
        return position == entries.size() ? draft : entries.get(position);
    }

    /**
     * Ends browsing after an edit or submission, without changing stored commands.
     */
    void resetNavigation() {
        position = entries.size();
        draft = "";
    }
}
