package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestUtil.runOnFxThread;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Tests empty-state guidance using the real ListView placeholder. */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class PersonListPanelEmptyStateTest {
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void initializeToolkit() throws Exception {
        FxTestUtil.initializeToolkit();
    }

    private Logic logic(ModelManager model, boolean failSaving) {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("members.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        if (failSaving) {
                            throw new IOException("Test save failure");
                        }
                        super.saveAddressBook(addressBook);
                    }
                };
        return new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    private static Region layout(PersonListPanel panel) {
        Region root = panel.getRoot();
        Scene scene = new Scene(root, 320, 300);
        scene.getStylesheets().add(PersonListPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
        root.resize(320, 300);
        root.applyCss();
        root.layout();
        return root;
    }

    @Test
    public void commands_noMembersNoMatchesAndVisibleResults_showAppropriateGuidance() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Logic logic = logic(model, false);
            PersonListPanel panel = new PersonListPanel(logic.getFilteredPersonList(), logic.getPersonList());
            Region root = layout(panel);
            assertEmpty(panel, true);
            assertDoesNotThrow(() -> logic.execute("add n/Alice p/11111 e/alice@example.com s/member"));
            assertPopulated(panel);
            assertDoesNotThrow(() -> logic.execute("find Nobody"));
            assertEmpty(panel, false);
            assertDoesNotThrow(() -> logic.execute("list"));
            assertPopulated(panel);
            assertDoesNotThrow(() -> logic.execute("filter s/inactive-alumnus"));
            assertEmpty(panel, false);
            assertDoesNotThrow(() -> logic.execute("clear"));
            assertEmpty(panel, true);
            assertDoesNotThrow(() -> logic.execute("undo"));
            assertPopulated(panel);
            assertDoesNotThrow(() -> logic.execute("redo"));
            assertEmpty(panel, true);

            root.applyCss();
            root.layout();
            Label guidance = (Label) root.lookup("#emptyState");
            Text rendered = (Text) guidance.lookup(".text");
            assertNotNull(rendered);
            assertEquals(guidance.getText(), rendered.getText());
            assertTrue(guidance.isWrapText());
            assertTrue(rendered.getLayoutBounds().getHeight() > guidance.getFont().getSize());
            assertTrue(guidance.localToScene(guidance.getBoundsInLocal()).getMinX() >= 0);
            assertTrue(guidance.localToScene(guidance.getBoundsInLocal()).getMaxX() <= root.getWidth());
        });
    }

    @Test
    public void sourceChanges_whileResultsStayEmpty_switchGuidanceInBothDirections() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice").build();
            model.addPerson(alice);
            model.updateFilteredPersonList(person -> false);
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(),
                    model.getAddressBook().getPersonList());
            layout(panel);
            assertEmpty(panel, false);
            model.deletePerson(alice);
            assertEmpty(panel, true);
            AddressBook replacement = new AddressBook();
            replacement.addPerson(alice);
            model.setAddressBook(replacement);
            assertEmpty(panel, false);
            model.setAddressBook(new AddressBook());
            assertEmpty(panel, true);
        });
    }

    @Test
    public void command_saveFailure_restoresNoMatchGuidance() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Alice").build());
            Logic logic = logic(model, true);
            PersonListPanel panel = new PersonListPanel(logic.getFilteredPersonList(), logic.getPersonList());
            layout(panel);
            assertDoesNotThrow(() -> logic.execute("find Nobody"));
            assertEmpty(panel, false);
            assertThrows(CommandException.class, () -> logic.execute("clear"));
            assertEmpty(panel, false);
        });
    }

    private static void assertEmpty(PersonListPanel panel, boolean directoryEmpty) {
        Region root = panel.getRoot();
        root.applyCss();
        root.layout();
        ListView<?> list = (ListView<?>) root.lookup("#personListView");
        Label guidance = (Label) list.getPlaceholder();
        assertNotNull(guidance);
        assertTrue(list.getItems().isEmpty());
        assertNotNull(guidance.getParent());
        assertTrue(guidance.getParent().isVisible());
        if (directoryEmpty) {
            assertTrue(guidance.getText().startsWith("No members yet."));
            assertTrue(guidance.getText().contains("add"));
            assertTrue(guidance.getText().contains("help"));
        } else {
            assertEquals("No matching members. Enter list to show everyone.", guidance.getText());
        }
    }

    private static void assertPopulated(PersonListPanel panel) {
        Region root = panel.getRoot();
        root.applyCss();
        root.layout();
        ListView<?> list = (ListView<?>) root.lookup("#personListView");
        assertFalse(list.getItems().isEmpty());
        assertFalse(list.getPlaceholder().getParent().isVisible());
    }
}
