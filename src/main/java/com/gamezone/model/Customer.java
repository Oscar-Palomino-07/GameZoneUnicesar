package com.gamezone.model;

/**
 * Represents a customer who buys products at GameZone Unicesar.
 *
 * <p>A customer is a person who attends the store to make purchases. In
 * addition to the attributes shared by every person, a customer stores the
 * email address used for contact. The purchase history of a customer is not
 * stored in this class but is derived by querying the recorded sales in the
 * sales module.</p>
 */
public class Customer extends Person {

    private String email;

    /**
     * Creates a customer with its personal data and email address.
     *
     * @param id        the identifier of the customer
     * @param firstName the first name of the customer
     * @param lastName  the last name of the customer
     * @param phone     the contact phone number
     * @param email     the email address of the customer
     */
    public Customer(String id, String firstName, String lastName, String phone, String email) {
        super(id, firstName, lastName, phone);
        this.email = email;
    }

    /**
     * Returns the email address of the customer.
     *
     * @return the customer's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the email address of the customer.
     *
     * @param email the new email
     */
    public void setEmail(String email) {
        this.email = email;
    }
}