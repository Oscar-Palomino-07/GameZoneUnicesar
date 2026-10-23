package com.gamezone.ui;

import com.gamezone.model.Customer;
import com.gamezone.model.Seller;
import com.gamezone.service.PersonService;

import java.util.List;

/**
 * Submenu of the people module: registers customers and lists customers and
 * sellers. Every option delegates to the {@link PersonService}.
 */
public class PersonMenu implements Menu {

    private final PersonService personService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the people menu.
     *
     * @param personService the service used for person operations
     * @param input         the reader of the user input
     * @param errorReporter the reporter that turns errors into messages
     */
    public PersonMenu(PersonService personService, ConsoleInput input, ConsoleErrorReporter errorReporter) {
        this.personService = personService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "People";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== PEOPLE ===");
            System.out.println("1. Register customer");
            System.out.println("2. List customers");
            System.out.println("3. List sellers");
            System.out.println("0. Back");
            int option = input.readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    listCustomers();
                    break;
                case 3:
                    listSellers();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void registerCustomer() {
        String id = input.readText("Customer id: ");
        String firstName = input.readText("First name: ");
        String lastName = input.readText("Last name: ");
        String phone = input.readText("Phone: ");
        String email = input.readText("Email: ");
        errorReporter.runGuarded(() -> {
            personService.registerCustomer(id, firstName, lastName, phone, email);
            System.out.println("Customer registered.");
        });
    }

    private void listCustomers() {
        List<Customer> customers = personService.listAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        for (Customer customer : customers) {
            System.out.println(customer.getId() + " | " + customer.getFirstName() + " " + customer.getLastName()
                    + " | " + customer.getPhone() + " | " + customer.getEmail());
        }
    }

    private void listSellers() {
        List<Seller> sellers = personService.listAllSellers();
        if (sellers.isEmpty()) {
            System.out.println("No sellers registered.");
            return;
        }
        for (Seller seller : sellers) {
            System.out.println(seller.getId() + " | " + seller.getFirstName() + " " + seller.getLastName()
                    + " | " + seller.getPhone() + " | code: " + seller.getEmployeeCode()
                    + " | shift: " + seller.getShift());
        }
    }
}
