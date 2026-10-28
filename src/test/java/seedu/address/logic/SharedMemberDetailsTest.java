package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises shared contact details through command parsing, history and real JSON storage.
 */
public class SharedMemberDetailsTest {
    @TempDir
    public Path testFolder;

    @Test
    public void add_sharedNameOrPhone_warnsForEveryMatchAndAdds() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = logicFor(model);
        logic.execute("add n/Alex Same p/91234567 e/one@example.com s/member");
        logic.execute("add n/Different Name p/92345678 e/two@example.com s/member");

        String feedback = logic.execute("add n/Alex Same p/92345678 e/three@example.com s/member")
                .getFeedbackToUser();

        assertEquals(3, model.getAddressBook().getPersonList().size());
        assertEquals(2, feedback.lines().filter(line -> line.startsWith("Warning:")).count());
        assertTrue(feedback.contains("one@example.com"));
        assertTrue(feedback.contains("two@example.com"));
        assertTrue(feedback.contains("New person added:"));
    }

    @Test
    public void identicalDetails_sortEditUndoDeleteAndReload_preserveSelectedMember() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = logicFor(model);
        String add = "add n/Alex Same p/91234567 e/alex@example.com s/member";
        logic.execute(add);
        String feedback = logic.execute(add).getFeedbackToUser();
        assertTrue(feedback.contains("Warning: possible duplicate"));

        List<Person> members = model.getAddressBook().getPersonList();
        UUID firstId = members.get(0).getId();
        UUID secondId = members.get(1).getId();
        assertNotEquals(firstId, secondId);

        logic.execute("sort status");
        logic.execute("edit 2 r/Second Member");
        assertTrue(members.get(0).getRole().isEmpty());
        assertEquals("Second Member", members.get(1).getRole().orElseThrow().value);
        assertEquals(secondId, members.get(1).getId());
        logic.execute("undo");
        logic.execute("sort name");
        logic.execute("delete 2");
        assertEquals(firstId, members.getFirst().getId());
        assertEquals(1, members.size());
        assertEquals(List.of(firstId), model.getFilteredPersonList().stream().map(Person::getId).toList());
        logic.execute("undo");

        ModelManager reloaded = new ModelManager(storage().readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(firstId, secondId), reloaded.getAddressBook().getPersonList().stream()
                .map(Person::getId).toList());
        LogicManager restarted = logicFor(reloaded);
        restarted.execute("find Alex");
        restarted.execute("delete 2");
        assertEquals(firstId, reloaded.getAddressBook().getPersonList().getFirst().getId());
        assertFalse(reloaded.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getId().equals(secondId)));
    }

    @Test
    public void add_internationalNames_surviveJsonReload() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = logicFor(model);
        List<String> names = List.of("Zo\u00e9 Tan", "\u5f20\u4f1f", "Anne-Marie", "O'Neil Tan");
        for (int i = 0; i < names.size(); i++) {
            logic.execute("add n/" + names.get(i) + " p/9123456" + i + " e/member" + i
                    + "@example.com s/member");
        }

        assertEquals(names, storage().readAddressBook().orElseThrow().getPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    @Test
    public void identicalDetails_caseOnlyEditUndoRedo_preservesSelectedMember() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = logicFor(model);
        String add = "add n/Alex Same p/91234567 e/alex@example.com s/member i/Piano:5 g/Choir";
        logic.execute(add);
        logic.execute(add);
        List<Person> members = model.getAddressBook().getPersonList();
        List<UUID> ids = members.stream().map(Person::getId).toList();

        logic.execute("edit 2 i/piano:5 g/choir");
        assertEquals("Piano", members.getFirst().getInstruments().iterator().next().getName());
        assertEquals("piano", members.get(1).getInstruments().iterator().next().getName());
        logic.execute("undo");
        assertEquals("Piano", members.get(1).getInstruments().iterator().next().getName());
        assertEquals("Choir", members.get(1).getGroups().iterator().next().getName());
        logic.execute("redo");
        assertEquals("piano", members.get(1).getInstruments().iterator().next().getName());
        assertEquals("choir", members.get(1).getGroups().iterator().next().getName());
        assertEquals("Choir", members.getFirst().getGroups().iterator().next().getName());
        assertEquals(ids, members.stream().map(Person::getId).toList());
        assertEquals(ids, storage().readAddressBook().orElseThrow().getPersonList().stream()
                .map(Person::getId).toList());
    }

    @Test
    public void importSharedNames_undoRedoAndReload_preserveRecordIds() throws Exception {
        ModelManager model = new ModelManager();
        LogicManager logic = logicFor(model);
        logic.execute("add n/Alex Same p/91234567 e/one@example.com s/member");
        UUID firstId = model.getAddressBook().getPersonList().getFirst().getId();
        logic.execute("find Missing");

        Path importFile = Path.of("imports", "shared-records-" + UUID.randomUUID() + ".csv");
        Files.createDirectories(importFile.getParent());
        try {
            Files.writeString(importFile, "name,phone,email,status,memberType,role,instruments,groups\n"
                    + "Alex Same,92345678,two@example.com,member,student,,,\n");
            logic.execute("import " + importFile.getFileName());
            assertEquals(2, model.getFilteredPersonList().size());
            List<UUID> ids = model.getAddressBook().getPersonList().stream().map(Person::getId).toList();
            assertEquals(firstId, ids.getFirst());
            assertNotEquals(firstId, ids.get(1));

            logic.execute("undo");
            assertEquals(List.of(firstId), model.getAddressBook().getPersonList().stream()
                    .map(Person::getId).toList());
            logic.execute("redo");
            assertEquals(ids, storage().readAddressBook().orElseThrow().getPersonList().stream()
                    .map(Person::getId).toList());
            logic.execute("delete 2");
            assertEquals(firstId, model.getAddressBook().getPersonList().getFirst().getId());
            assertEquals(1, model.getAddressBook().getPersonList().size());
        } finally {
            Files.deleteIfExists(importFile);
        }
    }

    private LogicManager logicFor(ModelManager model) {
        return new LogicManager(model, storage());
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(testFolder.resolve("members.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }
}
