package com.gamezone.model;

/**
 * Represents a person interacting with GameZone Unicesar, either as a
 * customer or as a seller.
 *
 * <p>Stores the attributes shared by every person in the system: the
 * identifier, the first name, the last name and the contact phone number.
 * This class is abstract because a generic person without a specific role
 * cannot exist in the domain; every person must be either a customer or
 * a seller.</p>
 */
public abstract class Person {

    private String id;
    private String firstName;
    private String lastName;
    private String phone;

    /**
     * Creates a person with the attributes shared by all roles.
     *
     * @param id        the identifier of the person
     * @param firstName the first name of the person
     * @param lastName  the last name of the person
     * @param phone     the contact phone number
     */
    public Person(String id, String firstName, String lastName, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    /**
     * Returns the identifier of the person.
     *
     * @return the person's id
     */
    public String getId() {
        return id;
    }

    /**
     * Updates the identifier of the person.
     *
     * @param id the new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the first name of the person.
     *
     * @return the person's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Updates the first name of the person.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the person.
     *
     * @return the person's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Updates the last name of the person.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the contact phone number of the person.
     *
     * @return the person's phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Updates the contact phone number of the person.
     *
     * @param phone the new phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
}