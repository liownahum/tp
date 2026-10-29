package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void getPersonList_isUnmodifiableAndRemainsLiveAcrossResets() {
        var persons = logic.getPersonList();
        assertThrows(UnsupportedOperationException.class, () -> persons.add(AMY));
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false);
        assertEquals(List.of(AMY), persons);
        assertTrue(logic.getFilteredPersonList().isEmpty());
        model.setAddressBook(new AddressBook());
        assertTrue(persons.isEmpty());
        model.addPerson(BOB);
        assertEquals(List.of(BOB), persons);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_helpWhenSavingDisabled_returnsHelpResult() throws Exception {
        setUpLogicWithSavingDisabled();

        CommandResult result = logic.execute("help");

        assertTrue(result.isShowHelp());
    }

    @Test
    public void execute_exitWhenSavingDisabled_returnsExitResult() throws Exception {
        setUpLogicWithSavingDisabled();

        CommandResult result = logic.execute("exit");

        assertTrue(result.isExit());
    }

    @Test
    public void execute_listWhenSavingDisabled_showsAllPersons() throws Exception {
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false);
        setUpLogicWithSavingDisabled();

        CommandResult result = logic.execute("list");

        assertEquals(ListCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(AMY), logic.getFilteredPersonList());
    }

    @Test
    public void execute_findWhenSavingDisabled_showsMatchingPersons() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BENSON);
        setUpLogicWithSavingDisabled();

        CommandResult result = logic.execute("find Amy");

        assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1), result.getFeedbackToUser());
        assertEquals(List.of(AMY), logic.getFilteredPersonList());
    }

    @Test
    public void execute_filterWhenSavingDisabled_showsMatchingPersons() throws Exception {
        model.addPerson(AMY);
        model.addPerson(BENSON);
        setUpLogicWithSavingDisabled();

        CommandResult result = logic.execute("filter s/member");

        assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1), result.getFeedbackToUser());
        assertEquals(List.of(AMY), logic.getFilteredPersonList());
    }

    @Test
    public void execute_addCommand_savesPersonToFile() throws Exception {
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + STATUS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withInstruments().build();

        logic.execute(addCommand);

        Path addressBookPath = temporaryFolder.resolve("addressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(addressBookPath);
        ReadOnlyAddressBook savedAddressBook = storage.readAddressBook().orElseThrow();

        assertEquals(List.of(expectedPerson), savedAddressBook.getPersonList());
    }

    @Test
    public void execute_undoAfterAdd_removesPersonFromModelAndFile() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);

        assertCommandSuccess(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_SUCCESS, new ModelManager());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(), storage.readAddressBook().orElseThrow().getPersonList());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_redoAfterUndo_restoresPersonInModelAndFile() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        logic.execute(UndoCommand.COMMAND_WORD);

        Person expectedPerson = new PersonBuilder(AMY).withInstruments().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandSuccess(RedoCommand.COMMAND_WORD, RedoCommand.MESSAGE_SUCCESS, expectedModel);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(expectedPerson), storage.readAddressBook().orElseThrow().getPersonList());
        assertFalse(model.canRedoAddressBook());
    }

    @Test
    public void execute_redoWhenSaveFails_keepsHistory() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        Model modelWithAmy = new ModelManager(model.getAddressBook(), new UserPrefs());
        logic.execute(UndoCommand.COMMAND_WORD);
        setStorageThatThrowsOnSave(DUMMY_IO_EXCEPTION);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, expectedMessage, new ModelManager());
        assertCommandFailure(RedoCommand.COMMAND_WORD, CommandException.class, expectedMessage, new ModelManager());
        assertFalse(model.canUndoAddressBook());

        // the retried redo succeeds once saving works again
        setUp();
        assertCommandSuccess(RedoCommand.COMMAND_WORD, RedoCommand.MESSAGE_SUCCESS, modelWithAmy);
    }

    @Test
    public void execute_undoAfterFailedRedo_nothingToUndo() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        logic.execute(UndoCommand.COMMAND_WORD);
        setStorageThatThrowsOnSave(DUMMY_IO_EXCEPTION);
        assertThrows(CommandException.class, () -> logic.execute(RedoCommand.COMMAND_WORD));
        setUp();

        assertCommandException(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_undoAfterReadOnlyCommand_undoesEarlierChange() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        logic.execute(ListCommand.COMMAND_WORD);

        assertCommandSuccess(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_SUCCESS, new ModelManager());
    }

    @Test
    public void execute_undoAfterFailedSave_nothingToUndo() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
        setUp();

        assertCommandException(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void execute_undoWhenSaveFails_keepsChangeAndHistory() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setStorageThatThrowsOnSave(DUMMY_IO_EXCEPTION);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertCommandFailure(UndoCommand.COMMAND_WORD, CommandException.class, expectedMessage, expectedModel);
        assertCommandFailure(UndoCommand.COMMAND_WORD, CommandException.class, expectedMessage, expectedModel);

        // the retried undo succeeds once saving works again
        setUp();
        assertCommandSuccess(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_SUCCESS, new ModelManager());
        assertFalse(model.canUndoAddressBook());
    }

    @Test
    public void execute_addAfterFailedUndo_undoRestoresStateBeforeAdd() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setStorageThatThrowsOnSave(DUMMY_IO_EXCEPTION);
        assertThrows(CommandException.class, () -> logic.execute(UndoCommand.COMMAND_WORD));
        setUp();

        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB);

        assertCommandSuccess(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_undoMultipleWhenSaveFails_keepsAllChangesAndHistory() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setStorageThatThrowsOnSave(DUMMY_IO_EXCEPTION);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertCommandFailure(UndoCommand.COMMAND_WORD + " 2", CommandException.class, expectedMessage, expectedModel);
        assertEquals(2, model.getUndoableChangeCount());
        assertEquals(0, model.getRedoableChangeCount());

        // the retried undo succeeds once saving works again
        setUp();
        assertCommandSuccess(UndoCommand.COMMAND_WORD + " 2",
                String.format(UndoCommand.MESSAGE_SUCCESS_MULTIPLE, 2), new ModelManager());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(List.of(), storage.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_redoMultipleWhenSaveFails_keepsHistory() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB);
        Model modelWithAmyAndBob = new ModelManager(model.getAddressBook(), new UserPrefs());
        logic.execute(UndoCommand.COMMAND_WORD + " 2");
        setStorageThatThrowsOnSave(DUMMY_IO_EXCEPTION);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertCommandFailure(RedoCommand.COMMAND_WORD + " 2", CommandException.class, expectedMessage,
                new ModelManager());
        assertEquals(2, model.getRedoableChangeCount());

        setUp();
        assertCommandSuccess(RedoCommand.COMMAND_WORD + " 2",
                String.format(RedoCommand.MESSAGE_SUCCESS_MULTIPLE, 2), modelWithAmyAndBob);
    }

    @Test
    public void execute_unchangedEditAfterUndo_cannotRedo() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + STATUS_DESC_BOB);
        logic.execute(UndoCommand.COMMAND_WORD);

        // an edit that changes nothing is still a new change, so the undone add can no longer be redone
        logic.execute(EditCommand.COMMAND_WORD + " 1" + NAME_DESC_AMY);

        assertCommandException(RedoCommand.COMMAND_WORD, RedoCommand.MESSAGE_FAILURE);
        assertEquals(1, model.getUndoableChangeCount());
    }

    @Test
    public void execute_failedCommandAfterUndo_canStillRedo() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY);
        logic.execute(UndoCommand.COMMAND_WORD);

        assertThrows(CommandException.class, () -> logic.execute("delete 9"));

        assertEquals(1, model.getRedoableChangeCount());
    }

    @Test
    public void execute_undoCaseOnlyInstrumentEdit_restoresCapitalisation() throws Exception {
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + STATUS_DESC_AMY
                + " i/Violin:8");
        logic.execute(EditCommand.COMMAND_WORD + " 1 i/violin:8");
        assertEquals("violin", model.getFilteredPersonList().get(0).getInstruments().iterator().next().getName());

        CommandResult result = logic.execute(UndoCommand.COMMAND_WORD);

        assertEquals(UndoCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals("Violin", model.getFilteredPersonList().get(0).getInstruments().iterator().next().getName());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageSaveFails_doesNotApplyDelete() {
        model.addPerson(AMY);
        model.addPerson(BOB);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        setStorageThatThrowsOnSave(DUMMY_IO_EXCEPTION);

        String deleteCommand = "delete 1";
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertCommandFailure(deleteCommand, CommandException.class, expectedMessage, expectedModel);
        assertCommandFailure(deleteCommand, CommandException.class, expectedMessage, expectedModel);
    }

    @Test
    public void execute_ioException_restoresFilteredListAfterEdit() throws Exception {
        String expectedMessage = String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());

        assertFailedEditRestoresFilteredList(DUMMY_IO_EXCEPTION, expectedMessage);
    }

    @Test
    public void execute_accessDeniedException_restoresFilteredListAfterEdit() throws Exception {
        String expectedMessage = String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage());

        assertFailedEditRestoresFilteredList(DUMMY_AD_EXCEPTION, expectedMessage);
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Sets up logic with address book saving disabled.
     */
    private void setUpLogicWithSavingDisabled() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        storage.disableAddressBookSaving();
        logic = new LogicManager(model, storage);
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        setStorageThatThrowsOnSave(e);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + STATUS_DESC_AMY;
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    /**
     * Verifies that a failed edit restores the address book and the filtered list.
     *
     * @param exception the exception thrown when saving the address book
     * @param expectedMessage the expected error message
     */
    private void assertFailedEditRestoresFilteredList(IOException exception, String expectedMessage)
            throws Exception {
        model.addPerson(AMY);
        model.addPerson(BOB);

        logic.execute("find Bob");
        setStorageThatThrowsOnSave(exception);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("edit 1 p/99999999"));

        assertEquals(List.of(BOB), logic.getFilteredPersonList());
        assertEquals(List.of(AMY, BOB), model.getAddressBook().getPersonList());
    }

    /**
     * Replaces the current storage with one that throws the given exception whenever an address book is saved.
     */
    private void setStorageThatThrowsOnSave(IOException exception) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException when saving.
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw exception;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);
    }
}
