package com.gamezone.model;

/**
 * Represents a person interacting with GameZone Unicesar.
 *
 * <p>Stores the attributes shared by every type of person in the system,
 * such as the full name, the government identification document and the
 * contact phone number. This class is abstract because a generic person
 * without a specific role cannot exist in the domain: every person must
 * either be a customer or a seller.</p>
 */
public abstract class Person {

    private String name;
    private String identification;
    private String phone;

    /**
     * Creates a person with the common attributes shared by all roles.
     *
     * @param name           the full name of the person
     * @param identification the government identification document
     * @param phone          the contact phone number
     */
    public Person(String name, String identification, String phone) {
        this.name = name;
        this.identification = identification;
        this.phone = phone;
    }

    /**
     * Returns the full name of the person.
     *
     * @return the person's name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the full name of the person.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the government identification document of the person.
     *
     * @return the person's identification
     */
    public String getIdentification() {
        return identification;
    }

    /**
     * Updates the government identification document of the person.
     *
     * @param identification the new identification
     */
    public void setIdentification(String identification) {
        this.identification = identification;
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

    /**
     * Returns the role that the person plays in the store.
     *
     * <p>Each concrete subclass defines the role it represents, which is
     * how the system distinguishes customers from sellers.</p>
     *
     * @return a string identifying the role of the person
     */
    public abstract String getRole();
}