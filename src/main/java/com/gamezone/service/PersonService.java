package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.gamezone.persistence.PersonRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Exposes the business operations related to the people of the store.
 *
 * <p>It validates the rules that apply to customers and sellers, keeps the
 * in-memory registries synchronized with the persistence layer and provides
 * the queries required by the sales module, such as locating a customer by
 * identification or a seller by employee code.</p>
 *
 * <p>This class belongs to the service layer and is the only one allowed to
 * invoke the person repository.</p>
 */
public class PersonService {

    private final PersonRepository repository;
    private final List<Customer> customers;
    private final List<Seller> sellers;

    /**
     * Creates the person service, loading the stored customers and sellers
     * from the repository into memory.
     *
     * @param repository the repository used to persist people
     */
    public PersonService(PersonRepository repository) {
        this.repository = repository;
        this.customers = new ArrayList<>(repository.loadCustomers());
        this.sellers = new ArrayList<>(repository.loadSellers());
    }

    /**
     * Registers a new customer in the system.
     *
     * <p>The data is validated before the customer is added: no field may
     * be blank and the identification must be unique. Once registered, the
     * customer registry is immediately persisted.</p>
     *
     * @param name           the full name of the customer
     * @param identification the government identification document
     * @param phone          the contact phone number
     * @param email          the email address of the customer
     * @throws IllegalArgumentException when any field is blank or the
     *         identification already belongs to another customer
     */
    public void registerCustomer(String name, String identification, String phone, String email) {
        if (isBlank(name) || isBlank(identification) || isBlank(phone) || isBlank(email)) {
            throw new IllegalArgumentException("All customer fields are required.");
        }
        if (findCustomerByIdentification(identification).isPresent()) {
            throw new IllegalArgumentException("A customer with identification " + identification + " already exists.");
        }
        customers.add(new Customer(name, identification, phone, email));
        save();
    }

    /**
     * Returns the list of registered customers.
     *
     * @return an unmodifiable view of the customer registry
     */
    public List<Customer> listCustomers() {
        return Collections.unmodifiableList(customers);
    }

    /**
     * Returns the list of registered sellers.
     *
     * @return an unmodifiable view of the seller registry
     */
    public List<Seller> listSellers() {
        return Collections.unmodifiableList(sellers);
    }

    /**
     * Searches for a customer by its government identification document.
     *
     * @param identification the identification to look for
     * @return the customer with the given identification, or an empty
     *         {@link Optional} when no customer matches
     */
    public Optional<Customer> findCustomerByIdentification(String identification) {
        return customers.stream()
                .filter(customer -> customer.getIdentification().equals(identification))
                .findFirst();
    }

    /**
     * Searches for a seller by its employee code.
     *
     * @param employeeCode the employee code to look for
     * @return the seller with the given employee code, or an empty
     *         {@link Optional} when no seller matches
     */
    public Optional<Seller> findSellerByEmployeeCode(String employeeCode) {
        return sellers.stream()
                .filter(seller -> seller.getEmployeeCode().equals(employeeCode))
                .findFirst();
    }

    /**
     * Persists the current state of both registries into the data files.
     *
     * <p>This method is invoked automatically after every operation that
     * changes the in-memory data, guaranteeing that the information is
     * conserved between executions.</p>
     */
    public void save() {
        repository.saveCustomers(customers);
        repository.saveSellers(sellers);
    }

    /**
     * Tells whether a field should be considered blank or invalid.
     *
     * @param value the value to check
     * @return {@code true} when the value is null or only contains spaces
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}