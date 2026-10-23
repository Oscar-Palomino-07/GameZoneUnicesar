package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Person;

import java.util.List;

/**
 * Provides static validation methods for the person module (customers and
 * sellers).
 *
 * <p>This class belongs to the validation layer and verifies mandatory person
 * fields, email format, existence and identifier uniqueness without accessing
 * persistence or services.</p>
 */
public final class PersonValidator {

    private PersonValidator() {
    }

    /**
     * Validates the mandatory attributes of a person and verifies that the
     * email address has a valid format (contains {@code '@'} and {@code '.'}).
     *
     * @param id    the person identifier; must not be null or blank
     * @param name  the person name; must not be null or blank
     * @param email the email address; must not be null, blank, and must contain '@' and '.'
     * @param phone the contact phone number; must not be null or blank
     * @throws InvalidDataException when any mandatory field is blank or the email format is invalid
     */
    public static void validatePersonData(String id, String name, String email, String phone) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("id", "el identificador de la persona no puede estar vacío");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("name", "el nombre no puede estar vacío");
        }
        if (phone == null || phone.isBlank()) {
            throw new InvalidDataException("phone", "el teléfono no puede estar vacío");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("email", "el correo electrónico no puede estar vacío");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new InvalidDataException("email", "el correo electrónico debe contener '@' y '.'");
        }
    }

    /**
     * Validates the mandatory attributes of a person when first name and last
     * name are provided separately.
     *
     * @param id        the person identifier; must not be null or blank
     * @param firstName the person first name; must not be null or blank
     * @param lastName  the person last name; must not be null or blank
     * @param email     the email address; must contain '@' and '.'
     * @param phone     the contact phone number; must not be null or blank
     * @throws InvalidDataException when any field is blank or the email format is invalid
     */
    public static void validatePersonData(String id, String firstName, String lastName,
                                          String email, String phone) {
        if (firstName == null || firstName.isBlank()) {
            throw new InvalidDataException("firstName", "el nombre no puede estar vacío");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new InvalidDataException("lastName", "el apellido no puede estar vacío");
        }
        validatePersonData(id, firstName.trim() + " " + lastName.trim(), email, phone);
    }

    /**
     * Validates that a searched person exists (is not {@code null}).
     *
     * @param person   the person reference obtained from a search
     * @param personId the identifier that was searched for
     * @throws ResourceNotFoundException when {@code person} is {@code null}
     */
    public static void validatePersonExists(Person person, String personId) {
        if (person == null) {
            throw new ResourceNotFoundException("cliente o vendedor", personId);
        }
    }

    /**
     * Validates that no person in the given list already has the specified
     * identifier.
     *
     * @param existing the list of currently registered people
     * @param newId    the identifier to check for uniqueness
     * @throws BusinessRuleException when a person with {@code newId} is already registered
     */
    public static void validateUniquePerson(List<? extends Person> existing, String newId) {
        if (existing == null || newId == null) {
            return;
        }
        for (Person person : existing) {
            if (person != null && newId.equals(person.getId())) {
                throw new BusinessRuleException(
                        "Ya existe una persona registrada con el identificador " + newId);
            }
        }
    }
}
