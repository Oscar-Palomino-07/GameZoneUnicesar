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
     * @param name           the full name of the customer
     * @param identification the government identification document
     * @param phone          the contact phone number
     * @param email          the email address of the customer
     */
    public Customer(String name, String identification, String phone, String email) {
        super(name, identification, phone);
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

    /**
     * Returns the role that this person plays in the store.
     *
     * @return the string {@code "Customer"}
     */
    @Override
    public String getRole() {
        return "Customer";
    }
}