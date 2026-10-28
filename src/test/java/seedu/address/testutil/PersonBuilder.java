package seedu.address.testutil;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import seedu.address.model.person.Email;
import seedu.address.model.person.Group;
import seedu.address.model.person.Instrument;
import seedu.address.model.person.MemberType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Status;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";

    private UUID id;
    private Name name;
    private Phone phone;
    private Email email;
    private Status status;
    private MemberType memberType;
    private Optional<Role> role;
    private Set<Instrument> instruments;
    private Set<Group> groups;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        id = UUID.randomUUID();
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        status = Status.MEMBER;
        memberType = MemberType.STUDENT;
        role = Optional.empty();
        instruments = new HashSet<>();
        groups = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        id = personToCopy.getId();
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        status = personToCopy.getStatus();
        memberType = personToCopy.getMemberType();
        role = personToCopy.getRole();
        instruments = new HashSet<>(personToCopy.getInstruments());
        groups = new HashSet<>(personToCopy.getGroups());
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code NAME:YEARS} {@code instruments} into a {@code Set<Instrument>} and sets it to the
     * {@code Person} that we are building.
     */
    public PersonBuilder withInstruments(String ... instruments) {
        this.instruments = SampleDataUtil.getInstrumentSet(instruments);
        return this;
    }

    /**
     * Sets the participation groups of the person being built.
     */
    public PersonBuilder withGroups(String... groups) {
        this.groups = new HashSet<>();
        for (String group : groups) {
            this.groups.add(new Group(group));
        }
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the membership status of the person being built.
     */
    public PersonBuilder withStatus(String status) {
        this.status = Status.fromString(status);
        return this;
    }

    /**
     * Sets whether the person is a student or teacher.
     */
    public PersonBuilder withMemberType(String memberType) {
        this.memberType = MemberType.fromString(memberType);
        return this;
    }

    /**
     * Sets the person's optional role.
     */
    public PersonBuilder withRole(String role) {
        this.role = Optional.of(new Role(role));
        return this;
    }

    /**
     * Removes the person's role.
     */
    public PersonBuilder withoutRole() {
        this.role = Optional.empty();
        return this;
    }

    /**
     * Gives a copied set of details a distinct member-record identity.
     */
    public PersonBuilder withNewId() {
        id = UUID.randomUUID();
        return this;
    }

    /**
     * Keeps the identity of the selected member when building expected edited details.
     */
    public PersonBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public Person build() {
        return new Person(id, name, phone, email, status, memberType, role, instruments, groups);
    }

}
