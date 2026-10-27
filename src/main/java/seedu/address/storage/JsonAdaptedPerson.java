package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
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
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MESSAGE_NULL_INSTRUMENT = "Person's instrument list contains null instrument(s).";

    private final String name;
    private final String phone;
    private final String email;
    private final String status;
    private final String memberType;
    private final String role;
    private final List<JsonAdaptedInstrument> instruments = new ArrayList<>();
    private final List<String> groups = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("status") String status,
            @JsonProperty("memberType") String memberType, @JsonProperty("role") String role,
            @JsonProperty("instruments") List<JsonAdaptedInstrument> instruments,
            @JsonProperty("groups") List<String> groups) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.memberType = memberType;
        this.role = role;
        if (instruments != null) {
            this.instruments.addAll(instruments);
        }
        if (groups != null) {
            this.groups.addAll(groups);
        }
    }

    /**
     * Creates an adapted person without groups for callers using the previous data shape.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String status,
            String memberType, String role, List<JsonAdaptedInstrument> instruments) {
        this(name, phone, email, status, memberType, role, instruments, null);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        status = source.getStatus().toString();
        memberType = source.getMemberType().toString();
        role = source.getRole().map(Role::toString).orElse(null);
        instruments.addAll(source.getInstruments().stream()
                .map(JsonAdaptedInstrument::new)
                .collect(Collectors.toList()));
        groups.addAll(source.getGroups().stream()
                .map(Group::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Instrument> personInstruments = new ArrayList<>();
        for (JsonAdaptedInstrument instrument : instruments) {
            if (instrument == null) {
                throw new IllegalValueException(MESSAGE_NULL_INSTRUMENT);
            }
            personInstruments.add(instrument.toModelType());
        }
        if (Instrument.hasDuplicateNames(personInstruments)) {
            throw new IllegalValueException(Instrument.MESSAGE_DUPLICATE_NAMES);
        }

        final Set<Group> modelGroups = new HashSet<>();
        for (String group : groups) {
            if (group == null || !Group.isValidName(group)) {
                throw new IllegalValueException(Group.MESSAGE_CONSTRAINTS);
            }
            modelGroups.add(new Group(group));
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (status == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Status.class.getSimpleName()));
        }
        if (!Status.isValidStatus(status)) {
            throw new IllegalValueException(Status.MESSAGE_CONSTRAINTS);
        }
        final Status modelStatus = Status.fromString(status);

        if (memberType == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    MemberType.class.getSimpleName()));
        }
        if (!MemberType.isValidMemberType(memberType)) {
            throw new IllegalValueException(MemberType.MESSAGE_CONSTRAINTS);
        }
        final MemberType modelMemberType = MemberType.fromString(memberType);

        if (role != null && role.isBlank()) {
            throw new IllegalValueException(Role.MESSAGE_CONSTRAINTS);
        }
        // Earlier versions saved roles made only of Unicode spaces such as U+00A0, which are no longer valid.
        // Load them as having no role, so that existing data files still load.
        final Optional<Role> modelRole = role == null || !Role.isValidRole(role)
                ? Optional.empty() : Optional.of(new Role(role));

        final Set<Instrument> modelInstruments = Set.copyOf(personInstruments);
        return new Person(modelName, modelPhone, modelEmail, modelStatus, modelMemberType, modelRole,
                modelInstruments, modelGroups);
    }

}
