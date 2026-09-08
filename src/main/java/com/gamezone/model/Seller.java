package com.gamezone.model;

/**
 * Represents a seller who works at GameZone Unicesar.
 *
 * <p>A seller is an employee in charge of attending customers and recording
 * sales. In addition to the attributes shared by every person, a seller
 * stores the employee code and the work shift assigned by the store.</p>
 */
public class Seller extends Person {

    private String employeeCode;
    private String shift;

    /**
     * Creates a seller with its personal data, employee code and work shift.
     *
     * @param id           the identifier of the seller
     * @param firstName    the first name of the seller
     * @param lastName     the last name of the seller
     * @param phone        the contact phone number
     * @param employeeCode the employee code assigned by the store
     * @param shift        the work shift assigned to the seller
     */
    public Seller(String id, String firstName, String lastName, String phone, String employeeCode, String shift) {
        super(id, firstName, lastName, phone);
        this.employeeCode = employeeCode;
        this.shift = shift;
    }

    /**
     * Returns the employee code assigned by the store.
     *
     * @return the seller's employee code
     */
    public String getEmployeeCode() {
        return employeeCode;
    }

    /**
     * Updates the employee code of the seller.
     *
     * @param employeeCode the new employee code
     */
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    /**
     * Returns the work shift assigned to the seller.
     *
     * @return the seller's work shift
     */
    public String getShift() {
        return shift;
    }

    /**
     * Updates the work shift assigned to the seller.
     *
     * @param shift the new work shift
     */
    public void setShift(String shift) {
        this.shift = shift;
    }
}