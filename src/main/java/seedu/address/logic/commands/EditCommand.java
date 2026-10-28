package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GROUP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INSTRUMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_STATUS + "STATUS] "
            + "[" + PREFIX_MEMBER_TYPE + "TYPE] "
            + "[" + PREFIX_ROLE + "ROLE] "
            + "[" + PREFIX_INSTRUMENT + "INSTRUMENT:YEARS]... "
            + "[" + PREFIX_GROUP + "GROUP]...\n"
            + "An empty " + PREFIX_ROLE + " clears the role, and an empty " + PREFIX_INSTRUMENT
            + " clears all instruments. An empty " + PREFIX_GROUP + " clears all groups.\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_STATUS + "active-alumnus";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";

    private static final Logger logger = LogsCenter.getLogger(EditCommand.class);

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        if (editPersonDescriptor.getGroups().isPresent()) {
            // LogicManager persists the edit after this command returns, so this records the request only.
            logger.info(() -> String.format("Participation group edit requested: %d groups before, %d groups after",
                    personToEdit.getGroups().size(), editedPerson.getGroups().size()));
        }
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Status updatedStatus = editPersonDescriptor.getStatus().orElse(personToEdit.getStatus());
        MemberType updatedMemberType = editPersonDescriptor.getMemberType().orElse(personToEdit.getMemberType());
        Optional<Role> updatedRole = editPersonDescriptor.getRole().orElse(personToEdit.getRole());
        Set<Instrument> updatedInstruments = editPersonDescriptor.getInstruments()
                .orElse(personToEdit.getInstruments());
        Set<Group> updatedGroups = editPersonDescriptor.getGroups().orElse(personToEdit.getGroups());

        return new Person(personToEdit.getId(), updatedName, updatedPhone, updatedEmail, updatedStatus,
                updatedMemberType, updatedRole, updatedInstruments, updatedGroups);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        private Email email;
        private Status status;
        private MemberType memberType;
        // null when the role is not edited; Optional.empty() when the role is cleared.
        private Optional<Role> role;
        private Set<Instrument> instruments;
        private Set<Group> groups;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code instruments} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setStatus(toCopy.status);
            setMemberType(toCopy.memberType);
            setRole(toCopy.role);
            setInstruments(toCopy.instruments);
            setGroups(toCopy.groups);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, status, memberType, role, instruments, groups);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Optional<Status> getStatus() {
            return Optional.ofNullable(status);
        }

        public void setMemberType(MemberType memberType) {
            this.memberType = memberType;
        }

        public Optional<MemberType> getMemberType() {
            return Optional.ofNullable(memberType);
        }

        /**
         * Sets the role to edit to. {@code Optional.empty()} clears the person's role,
         * while {@code null} leaves the role unedited.
         */
        public void setRole(Optional<Role> role) {
            this.role = role;
        }

        /**
         * Returns the edited role, which is itself empty when the role is cleared.
         * Returns {@code Optional#empty()} if the role is not edited.
         */
        public Optional<Optional<Role>> getRole() {
            return Optional.ofNullable(role);
        }

        /**
         * Sets {@code instruments} to this object's {@code instruments}.
         * A defensive copy of {@code instruments} is used internally.
         */
        public void setInstruments(Set<Instrument> instruments) {
            this.instruments = (instruments != null) ? new HashSet<>(instruments) : null;
        }

        /**
         * Returns an unmodifiable instrument set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code instruments} is null.
         */
        public Optional<Set<Instrument>> getInstruments() {
            return (instruments != null) ? Optional.of(Collections.unmodifiableSet(instruments)) : Optional.empty();
        }

        /**
         * Sets the replacement groups. An empty set clears all existing groups.
         */
        public void setGroups(Set<Group> groups) {
            this.groups = (groups != null) ? new HashSet<>(groups) : null;
        }

        /**
         * Returns the replacement groups, or empty when groups are not being edited.
         */
        public Optional<Set<Group>> getGroups() {
            return (groups != null) ? Optional.of(Collections.unmodifiableSet(groups)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(status, otherEditPersonDescriptor.status)
                    && Objects.equals(memberType, otherEditPersonDescriptor.memberType)
                    && Objects.equals(role, otherEditPersonDescriptor.role)
                    && Objects.equals(instruments, otherEditPersonDescriptor.instruments)
                    && Objects.equals(groups, otherEditPersonDescriptor.groups);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("status", status)
                    .add("memberType", memberType)
                    .add("role", role)
                    .add("instruments", instruments)
                    .add("groups", groups)
                    .toString();
        }
    }
}
