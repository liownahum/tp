package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

/** Tests persistent counts against live model and command changes. */
@EnabledIfEnvironmentVariable(named = "HIVEMIND_UI_TESTS", matches = "true")
public class PersonListPanelCountTest {
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
    public void count_directorySizeChanges_selectsSingularOrPlural() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice").build();
            Person bob = new PersonBuilder().withName("Bob").build();
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(),
                    model.getAddressBook().getPersonList());
            Label count = (Label) panel.getRoot().lookup("#resultCount");
            assertEquals("Showing 0 of 0 members", count.getText());

            model.addPerson(alice);
            assertEquals("Showing 1 of 1 member", count.getText());
            model.updateFilteredPersonList(person -> false);
            assertEquals("Showing 0 of 1 member", count.getText());
            model.addPerson(bob);
            assertEquals("Showing 2 of 2 members", count.getText());
            model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice"));
            assertEquals("Showing 1 of 2 members", count.getText());
            model.deletePerson(bob);
            assertEquals("Showing 1 of 1 member", count.getText());
            model.deletePerson(alice);
            assertEquals("Showing 0 of 0 members", count.getText());
        });
    }

    @Test
    public void commands_findListDeleteClearUndoAndRedo_keepCountAccurate() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Logic logic = logic(model, false);
            PersonListPanel panel = new PersonListPanel(logic.getFilteredPersonList(), logic.getPersonList());
            layout(panel);
            assertCount(panel, 0, 0);
            assertDoesNotThrow(() -> logic.execute("add n/Alice p/11111 e/alice@example.com s/member"));
            assertCount(panel, 1, 1);
            assertDoesNotThrow(() -> logic.execute("add n/Bob p/22222 e/bob@example.com s/inactive-alumnus"));
            assertCount(panel, 2, 2);
            assertDoesNotThrow(() -> logic.execute("find Alice"));
            assertCount(panel, 1, 2);
            assertDoesNotThrow(() -> logic.execute("filter s/member"));
            assertCount(panel, 1, 2);
            assertDoesNotThrow(() -> logic.execute("find Nobody"));
            assertCount(panel, 0, 2);
            assertDoesNotThrow(() -> logic.execute("list"));
            assertCount(panel, 2, 2);
            assertDoesNotThrow(() -> logic.execute("delete 1"));
            assertCount(panel, 1, 1);
            assertDoesNotThrow(() -> logic.execute("undo"));
            assertCount(panel, 2, 2);
            assertDoesNotThrow(() -> logic.execute("redo"));
            assertCount(panel, 1, 1);
            assertDoesNotThrow(() -> logic.execute("clear"));
            assertCount(panel, 0, 0);
            assertDoesNotThrow(() -> logic.execute("undo"));
            assertCount(panel, 1, 1);
            assertDoesNotThrow(() -> logic.execute("redo"));
            assertCount(panel, 0, 0);
        });
    }

    @Test
    public void sourceChanges_unchangedResultsAndDirectoryResets_updateTotal() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice").build();
            Person bob = new PersonBuilder().withName("Bob").build();
            model.addPerson(alice);
            model.addPerson(bob);
            model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice"));
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(),
                    model.getAddressBook().getPersonList());
            Region root = layout(panel);
            assertCount(panel, 1, 2);
            model.deletePerson(bob);
            assertCount(panel, 1, 1);
            model.updateFilteredPersonList(person -> false);
            assertCount(panel, 0, 1);
            model.setAddressBook(new AddressBook());
            assertCount(panel, 0, 0);
            AddressBook replacement = new AddressBook();
            replacement.addPerson(bob);
            model.setAddressBook(replacement);
            assertCount(panel, 0, 1);

            root.applyCss();
            root.layout();
            Label count = (Label) root.lookup("#resultCount");
            Text rendered = (Text) count.lookup(".text");
            assertNotNull(rendered);
            assertEquals(count.getText(), rendered.getText());
            assertTrue(count.localToScene(count.getBoundsInLocal()).getMaxX() <= root.getWidth());
            ListView<?> list = (ListView<?>) root.lookup("#personListView");
            assertTrue(count.getBoundsInParent().getMaxY() <= list.getBoundsInParent().getMinY());
        });
    }

    @Test
    public void command_saveFailure_restoresCountAndFilter() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Alice").build());
            model.addPerson(new PersonBuilder().withName("Bob").build());
            Logic logic = logic(model, true);
            PersonListPanel panel = new PersonListPanel(logic.getFilteredPersonList(), logic.getPersonList());
            assertDoesNotThrow(() -> logic.execute("find Alice"));
            assertCount(panel, 1, 2);
            assertThrows(CommandException.class, () -> logic.execute("delete 1"));
            assertCount(panel, 1, 2);
        });
    }

    private static void assertCount(PersonListPanel panel, int displayed, int total) {
        Label count = (Label) panel.getRoot().lookup("#resultCount");
        assertNotNull(count);
        String noun = total == 1 ? "member" : "members";
        assertEquals(String.format("Showing %d of %d %s", displayed, total, noun), count.getText());
    }
}
