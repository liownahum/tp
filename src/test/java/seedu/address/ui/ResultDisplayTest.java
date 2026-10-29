package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestUtil.runOnFxThread;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.ResultDisplay.FeedbackType;

/** Tests feedback through the real FXML and command-entry wiring. */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class ResultDisplayTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void initializeToolkit() throws Exception {
        FxTestUtil.initializeToolkit();
    }

    @Test
    public void feedback_outcomeTransitions_replaceLabelAndColourWithoutChangingMessage() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            Region root = display.getRoot();
            layout(root, 400);
            Label label = (Label) root.lookup("#feedbackType");
            TextArea area = (TextArea) root.lookup("#resultDisplay");
            assertEquals("Information", label.getText());

            display.setFeedbackToUser("Saved", FeedbackType.SUCCESS);
            root.applyCss();
            Paint successColour = label.getTextFill();
            assertEquals("Success", label.getText());
            assertEquals("Saved", area.getText());
            assertFalse(area.isEditable());

            display.setFeedbackToUser("Cannot save\nTry another location", FeedbackType.ERROR);
            root.applyCss();
            assertEquals("Error", label.getText());
            assertNotEquals(successColour, label.getTextFill());
            assertEquals("Cannot save\nTry another location", area.getText());
            assertFalse(root.getStyleClass().contains("feedback-success"));

            display.setFeedbackToUser("Startup notice");
            assertEquals("Information", label.getText());
            assertFalse(root.getStyleClass().contains("feedback-error"));
            assertEquals("Startup notice", area.getText());
        });
    }

    @Test
    public void layout_longMultilineFeedback_wrapsAndScrollsAtNarrowWidths() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            String message = "Long command guidance with details. ".repeat(40) + "\n" + "x".repeat(250);
            display.setFeedbackToUser(message, FeedbackType.ERROR);
            Region root = display.getRoot();
            layout(root, 400);
            TextArea area = (TextArea) root.lookup("#resultDisplay");
            Text rendered = (Text) area.lookup(".text");
            assertNotNull(rendered);
            assertTrue(area.isWrapText());
            assertEquals(message, rendered.getText());
            assertTrue(rendered.getLayoutBounds().getHeight() > area.getHeight());
            ScrollBar horizontal = scrollBar(area, Orientation.HORIZONTAL);
            ScrollBar vertical = scrollBar(area, Orientation.VERTICAL);
            assertNotNull(horizontal);
            assertNotNull(vertical);
            assertFalse(horizontal.isVisible());
            assertTrue(vertical.isVisible());
            area.selectAll();
            assertEquals(message, area.getSelectedText());

            root.resize(800, 90);
            root.layout();
            display.setFeedbackToUser("Short", FeedbackType.SUCCESS);
            assertEquals(0, area.getCaretPosition());
            assertEquals(0, area.getScrollTop());
            assertEquals("Short", area.getText());
        });
    }

    @Test
    public void commandEntry_successParseErrorExecutionErrorAndSaveFailure_showCorrectOutcome() throws Exception {
        runOnFxThread(() -> {
            JsonAddressBookStorage addressBookStorage =
                    new JsonAddressBookStorage(temporaryFolder.resolve("members.json")) {
                        @Override
                        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                            throw new IOException("Test save failure");
                        }
                    };
            StorageManager storage = new StorageManager(addressBookStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, new LogicManager(new ModelManager(), storage),
                        temporaryFolder.resolve("members.json"));
                window.fillInnerParts();
                Region root = (Region) stage.getScene().getRoot();
                root.applyCss();
                root.layout();
                Label label = (Label) root.lookup("#feedbackType");
                TextArea area = (TextArea) root.lookup("#resultDisplay");
                TextField input = (TextField) root.lookup("#commandTextField");
                enter(input, "list");
                assertEquals("Success", label.getText());
                enter(input, "unknown");
                assertEquals("Error", label.getText());
                enter(input, "delete 1");
                assertEquals("Error", label.getText());
                enter(input, "clear");
                assertEquals("Error", label.getText());
                assertTrue(area.getText().contains("Test save failure"));
                enter(input, "list");
                assertEquals("Success", label.getText());
                window.showStartupMessage("Recovered directory");
                assertEquals("Information", label.getText());
            } finally {
                stage.close();
            }
        });
    }

    private static ScrollBar scrollBar(TextArea area, Orientation orientation) {
        return area.lookupAll(".scroll-bar").stream().map(node -> (ScrollBar) node)
                .filter(bar -> bar.getOrientation() == orientation).findFirst().orElseThrow();
    }

    private static void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static void layout(Region root, double width) {
        Scene scene = new Scene(root, width, 90);
        scene.getStylesheets().add(ResultDisplay.class.getResource("/view/DarkTheme.css").toExternalForm());
        root.resize(width, 90);
        root.applyCss();
        root.layout();
    }
}
