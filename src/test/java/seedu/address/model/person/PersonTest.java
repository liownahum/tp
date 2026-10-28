package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_INSTRUMENT_PIANO;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getInstruments().remove(0));
        assertThrows(UnsupportedOperationException.class, () -> person.getGroups().add(new Group("Training")));
    }

    @Test
    public void constructor_duplicateInstrumentNames_throwsIllegalArgumentException() {
        Set<Instrument> instruments = Set.of(new Instrument("Violin", 3), new Instrument("violin", 5));
        assertThrows(IllegalArgumentException.class, Instrument.MESSAGE_DUPLICATE_NAMES, () -> new Person(
                ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getStatus(), ALICE.getMemberType(),
                ALICE.getRole(), instruments));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withInstruments(VALID_INSTRUMENT_PIANO).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // Editing a name keeps the same record identity.
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // Changing name case or spacing also preserves identity.
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // A separately added member may have identical details but a different ID.
        Person anotherAlice = new PersonBuilder(ALICE).withNewId().build();
        assertFalse(ALICE.isSamePerson(anotherAlice));
        assertEquals(ALICE, anotherAlice);
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different membership fields -> returns false
        editedAlice = new PersonBuilder(ALICE).withStatus("active-alumnus").build();
        assertFalse(ALICE.equals(editedAlice));
        editedAlice = new PersonBuilder(ALICE).withMemberType("teacher").build();
        assertFalse(ALICE.equals(editedAlice));
        editedAlice = new PersonBuilder(ALICE).withRole("Section Leader").build();
        assertFalse(ALICE.equals(editedAlice));

        // different instruments -> returns false
        editedAlice = new PersonBuilder(ALICE).withInstruments(VALID_INSTRUMENT_PIANO).build();
        assertFalse(ALICE.equals(editedAlice));

        // different participation groups -> returns false
        editedAlice = new PersonBuilder(ALICE).withGroups("Chamber Orchestra").build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", status=" + ALICE.getStatus()
                + ", memberType=" + ALICE.getMemberType() + ", role=" + ALICE.getRole()
                + ", instruments=" + ALICE.getInstruments() + ", groups=" + ALICE.getGroups() + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void hashCode_equalPersons_returnsSameHashCode() {
        Person aliceCopy = new PersonBuilder(ALICE).build();

        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());
    }

    @Test
    public void hasIdenticalDetails_identicalCopy_returnsTrue() {
        Person alice = new PersonBuilder(ALICE).withInstruments("Violin:8").withGroups("Band").build();
        Person copy = new PersonBuilder(alice).build();

        assertTrue(alice.hasIdenticalDetails(copy));
    }

    @Test
    public void hasIdenticalDetails_onlyInstrumentCaseDiffers_returnsFalse() {
        Person alice = new PersonBuilder(ALICE).withInstruments("Violin:8").withGroups("Band").build();
        Person edited = new PersonBuilder(alice).withInstruments("violin:8").build();

        // equals ignores the capitalisation of instrument names, hasIdenticalDetails does not
        assertTrue(alice.equals(edited));
        assertFalse(alice.hasIdenticalDetails(edited));
    }

    @Test
    public void hasIdenticalDetails_onlyGroupCaseDiffers_returnsFalse() {
        Person alice = new PersonBuilder(ALICE).withInstruments("Violin:8").withGroups("Band").build();
        Person edited = new PersonBuilder(alice).withGroups("band").build();

        // equals ignores the capitalisation of group names, hasIdenticalDetails does not
        assertTrue(alice.equals(edited));
        assertFalse(alice.hasIdenticalDetails(edited));
    }

    @Test
    public void hasIdenticalDetails_nullOrDifferentPerson_returnsFalse() {
        assertFalse(ALICE.hasIdenticalDetails(null));
        assertFalse(ALICE.hasIdenticalDetails(BOB));
    }
}
