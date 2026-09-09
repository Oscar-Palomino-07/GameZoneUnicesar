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
 * the queries required by the sales module, such as locating a customer or
 * a seller by its identifier.</p>
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
        this.customers = new ArrayList<>(repository.loadAllCustomers());
        this.sellers = new ArrayList<>(repository.loadAllSellers());
    }

    /**
     * Registers a new customer in the system.
     *
     * <p>The data is validated before the customer is added: no field may
     * be blank and the id must be unique. Once registered, the customer
     * registry is immediately persisted.</p>
     *
     * @param id        the identifier of the customer
     * @param firstName the first name of the customer
     * @param lastName  the last name of the customer
     * @param phone     the contact phone number
     * @param email     the email address of the customer
     * @throws IllegalArgumentException when any field is blank or the id
     *         already belongs to another customer
     */
    public void registerCustomer(String id, String firstName, String lastName, String phone, String email) {
        if (isBlank(id) || isBlank(firstName) || isBlank(lastName) || isBlank(phone) || isBlank(email)) {
            throw new IllegalArgumentException("All customer fields are required.");
        }
        if (findCustomerById(id).isPresent()) {
            throw new IllegalArgumentException("A customer with id " + id + " already exists.");
        }
        customers.add(new Customer(id, firstName, lastName, phone, email));
        save();
    }

    /**
     * Returns the list of all registered customers.
     *
     * @return an unmodifiable view of the customer registry
     */
    public List<Customer> listAllCustomers() {
        return Collections.unmodifiableList(customers);
    }

    /**
     * Returns the list of all registered sellers.
     *
     * @return an unmodifiable view of the seller registry
     */
    public List<Seller> listAllSellers() {
        return Collections.unmodifiableList(sellers);
    }

    /**
     * Searches for a customer by its identifier.
     *
     * @param id the identifier to look for
     * @return the customer with the given id, or an empty
     *         {@link Optional} when no customer matches
     */
    public Optional<Customer> findCustomerById(String id) {
        return customers.stream()
                .filter(customer -> customer.getId().equals(id))
                .findFirst();
    }

    /**
     * Searches for a seller by its identifier.
     *
     * @param id the identifier to look for
     * @return the seller with the given id, or an empty
     *         {@link Optional} when no seller matches
     */
    public Optional<Seller> findSellerById(String id) {
        return sellers.stream()
                .filter(seller -> seller.getId().equals(id))
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
        repository.saveAllCustomers(customers);
        repository.saveAllSellers(sellers);
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