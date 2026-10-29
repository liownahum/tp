package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {
    private final CommandHistory history = new CommandHistory();

    @Test
    public void navigate_emptyHistory_preservesInput() {
        assertEquals("unfinished", history.previous("unfinished"));
        assertEquals("unfinished", history.next("unfinished"));
        history.add(" \t ");
        assertEquals("unfinished", history.previous("unfinished"));
    }

    @Test
    public void navigate_multipleEntries_clampsAndRestoresDraft() {
        history.add("list");
        history.add("find alex");
        history.add("sort name");

        assertEquals("sort name", history.previous("edit 1 n/"));
        assertEquals("find alex", history.previous("sort name"));
        assertEquals("list", history.previous("find alex"));
        assertEquals("list", history.previous("list"));
        assertEquals("find alex", history.next("list"));
        assertEquals("sort name", history.next("find alex"));
        assertEquals("edit 1 n/", history.next("sort name"));
        assertEquals("edit 1 n/", history.next("edit 1 n/"));
    }

    @Test
    public void navigate_singleEntry_restoresEmptyDraft() {
        history.add("list");
        assertEquals("list", history.previous(""));
        assertEquals("", history.next("list"));
        assertEquals("", history.next(""));
    }

    @Test
    public void add_repeatedCommands_preservesExactSubmissions() {
        history.add("  find Alex  ");
        history.add("list");
        history.add("list");
        assertEquals("list", history.previous("draft"));
        assertEquals("list", history.previous("list"));
        assertEquals("  find Alex  ", history.previous("list"));
    }

    @Test
    public void resetNavigation_editedRecall_becomesNewDraftWithoutChangingHistory() {
        history.add("find alex");
        history.add("list");
        assertEquals("list", history.previous("original draft"));
        assertEquals("find alex", history.previous("list"));
        history.resetNavigation();

        assertEquals("list", history.previous("find david"));
        assertEquals("find david", history.next("list"));
        assertEquals("list", history.previous("find david"));
        assertEquals("find alex", history.previous("list"));
    }

    @Test
    public void add_whileBrowsing_resetsPositionAndDraft() {
        history.add("list");
        assertEquals("list", history.previous("old draft"));
        history.add("find alex");
        assertEquals("new draft", history.next("new draft"));
        assertEquals("find alex", history.previous("new draft"));
        assertEquals("new draft", history.next("find alex"));
    }

    @Test
    public void add_overCapacity_evictsOldestAndPreservesNavigation() {
        for (int i = 0; i < 105; i++) {
            history.add("find " + i);
        }
        String current = "draft";
        for (int i = 104; i >= 5; i--) {
            current = history.previous(current);
            assertEquals("find " + i, current);
        }
        assertEquals("find 5", history.previous(current));
        for (int i = 6; i <= 104; i++) {
            current = history.next(current);
            assertEquals("find " + i, current);
        }
        assertEquals("draft", history.next(current));
    }

    @Test
    public void nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.add(null));
        assertThrows(NullPointerException.class, () -> history.previous(null));
        assertThrows(NullPointerException.class, () -> history.next(null));
    }
}
