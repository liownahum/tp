package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Exercises command submission and recall through the actual FXML text field.
 * Requires a graphical session and {@code HIVEMIND_UI_TESTS=true}.
 */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class CommandBoxTest {
    @BeforeAll
    public static void initializeToolkit() throws Exception {
        CompletableFuture<Void> started = new CompletableFuture<>();
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            started.complete(null);
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException e) {
            Platform.runLater(initialize);
        }
        started.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void arrows_emptyHistory_preserveTextAndCaretWithoutExecution() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.field.setText("unfinished");
            fixture.field.positionCaret(3);
            fixture.press(KeyCode.UP);
            fixture.press(KeyCode.DOWN);
            assertEquals("unfinished", fixture.field.getText());
            assertEquals(3, fixture.field.getCaretPosition());
            assertTrue(fixture.executed.isEmpty());
        });
    }

    @Test
    public void arrows_browseSubmissions_restoreDraftAndDoNotExecute() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.submit("list");
            fixture.submit("find alex");
            assertEquals("", fixture.field.getText());
            fixture.field.setText("edit 1 n/");

            fixture.press(KeyCode.UP);
            assertEquals("find alex", fixture.field.getText());
            assertEquals("find alex".length(), fixture.field.getCaretPosition());
            fixture.press(KeyCode.UP);
            fixture.press(KeyCode.UP);
            assertEquals("list", fixture.field.getText());
            fixture.press(KeyCode.DOWN);
            assertEquals("find alex", fixture.field.getText());
            fixture.press(KeyCode.DOWN);
            fixture.press(KeyCode.DOWN);
            assertEquals("edit 1 n/", fixture.field.getText());
            assertEquals(List.of("list", "find alex"), fixture.executed);
        });
    }

    @Test
    public void edit_recalledText_preservesHistoryAndStartsNewDraft() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.submit("list");
            fixture.submit("find alex");
            fixture.press(KeyCode.UP);
            fixture.field.setText("find david");
            fixture.press(KeyCode.UP);
            assertEquals("find alex", fixture.field.getText());
            fixture.press(KeyCode.DOWN);
            assertEquals("find david", fixture.field.getText());
            fixture.enter();
            assertEquals(List.of("list", "find alex", "find david"), fixture.executed);
            fixture.press(KeyCode.UP);
            assertEquals("find david", fixture.field.getText());
            fixture.press(KeyCode.UP);
            assertEquals("find alex", fixture.field.getText());
            fixture.press(KeyCode.DOWN);
            fixture.press(KeyCode.DOWN);
            assertEquals("", fixture.field.getText());
        });
    }

    @Test
    public void submit_failure_retainsInputAndCanBeRecalledWithoutExecution() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.submit("list");
            fixture.submit("bad command");
            assertEquals("bad command", fixture.field.getText());
            assertTrue(fixture.field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            fixture.press(KeyCode.UP);
            fixture.press(KeyCode.UP);
            assertEquals("list", fixture.field.getText());
            assertFalse(fixture.field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            fixture.press(KeyCode.DOWN);
            assertEquals("bad command", fixture.field.getText());
            assertEquals(List.of("list", "bad command"), fixture.executed);
            fixture.enter();
            assertEquals(List.of("list", "bad command", "bad command"), fixture.executed);
            assertTrue(fixture.field.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void submit_blank_ignoresInputAndKeepsHistory() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.submit("list");
            fixture.submit("   ");
            assertEquals("   ", fixture.field.getText());
            assertEquals(List.of("list"), fixture.executed);
            fixture.press(KeyCode.UP);
            assertEquals("list", fixture.field.getText());
            fixture.press(KeyCode.DOWN);
            assertEquals("   ", fixture.field.getText());
        });
    }

    @Test
    public void arrows_modified_doNotRecallCommands() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.submit("list");
            fixture.field.setText("draft");
            for (int modifier = 0; modifier < 4; modifier++) {
                for (KeyCode key : List.of(KeyCode.UP, KeyCode.DOWN)) {
                    fixture.field.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key,
                            modifier == 0, modifier == 1, modifier == 2, modifier == 3));
                    assertEquals("draft", fixture.field.getText());
                }
            }
            fixture.press(KeyCode.UP);
            assertEquals("list", fixture.field.getText());
            assertEquals(List.of("list"), fixture.executed);
        });
    }

    @Test
    public void submit_rawTextAndRepeats_preservesExactHistory() throws Exception {
        runOnFxThread(() -> {
            Fixture fixture = new Fixture();
            fixture.submit("  find Alex  ");
            fixture.submit("list");
            fixture.submit("list");
            fixture.press(KeyCode.UP);
            fixture.press(KeyCode.UP);
            assertEquals("list", fixture.field.getText());
            fixture.press(KeyCode.UP);
            assertEquals("  find Alex  ", fixture.field.getText());
            assertEquals(List.of("  find Alex  ", "list", "list"), fixture.executed);
        });
    }

    @Test
    public void constructor_newCommandBox_hasIndependentEmptyHistory() throws Exception {
        runOnFxThread(() -> {
            Fixture first = new Fixture();
            first.submit("list");
            Fixture second = new Fixture();
            second.press(KeyCode.UP);
            assertEquals("", second.field.getText());
            assertTrue(second.executed.isEmpty());
        });
    }

    /**
     * Runs assertions on the JavaFX application thread with a bounded wait.
     */
    private static void runOnFxThread(Runnable assertions) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                assertions.run();
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(10, TimeUnit.SECONDS);
    }

    /**
     * Records submissions through the real command input without touching member data.
     */
    private static class Fixture {
        private final List<String> executed = new ArrayList<>();
        private final CommandBox commandBox = new CommandBox(command -> {
            executed.add(command);
            if (command.startsWith("bad")) {
                throw new ParseException("Invalid test command");
            }
            return new CommandResult("Success");
        });
        private final TextField field = (TextField) commandBox.getRoot().lookup("#commandTextField");

        void submit(String command) {
            field.setText(command);
            enter();
        }

        void enter() {
            Event.fireEvent(field, new ActionEvent());
        }

        void press(KeyCode key) {
            Event.fireEvent(field, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, false, false, false, false));
        }
    }
}
