package com.gamezone.ui;

import com.gamezone.model.Return;
import com.gamezone.service.ReturnService;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu of the returns module: registers returns and queries them. Every
 * option delegates to the {@link ReturnService}.
 */
public class ReturnMenu implements Menu {

    private final ReturnService returnService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the returns menu.
     *
     * @param returnService the service used for return operations
     * @param input         the reader of the user input
     * @param errorReporter the reporter that turns errors into messages
     */
    public ReturnMenu(ReturnService returnService, ConsoleInput input, ConsoleErrorReporter errorReporter) {
        this.returnService = returnService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "Returns";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== RETURNS ===");
            System.out.println("1. Register return");
            System.out.println("2. View all returns");
            System.out.println("3. View returns by customer");
            System.out.println("4. View returns by sale");
            System.out.println("0. Back");
            int option = input.readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerReturn();
                    break;
                case 2:
                    viewAllReturns();
                    break;
                case 3:
                    viewReturnsByCustomer();
                    break;
                case 4:
                    viewReturnsBySale();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void registerReturn() {
        String saleId = input.readText("Sale id to return: ");
        String productIdsInput = input.readText("Product ids to return (comma separated): ");
        List<String> productIds = new ArrayList<>();
        for (String item : productIdsInput.split(",")) {
            if (!item.trim().isEmpty()) {
                productIds.add(item.trim());
            }
        }
        errorReporter.runGuarded(() -> {
            Return ret = returnService.registerReturn(saleId, productIds);
            System.out.println("Devolución " + ret.getId() + " registrada.");
            System.out.println(ret.generateReturnReceipt());
        });
    }

    private void viewAllReturns() {
        List<Return> returns = returnService.viewAllReturns();
        if (returns.isEmpty()) {
            System.out.println("No returns registered.");
            return;
        }
        for (Return ret : returns) {
            printReturn(ret);
        }
    }

    private void viewReturnsByCustomer() {
        String customerId = input.readText("Customer id: ");
        List<Return> returns = returnService.viewReturnsByCustomer(customerId);
        if (returns.isEmpty()) {
            System.out.println("No returns found for customer " + customerId + ".");
            return;
        }
        for (Return ret : returns) {
            printReturn(ret);
        }
    }

    private void viewReturnsBySale() {
        String saleId = input.readText("Sale id: ");
        List<Return> returns = returnService.viewReturnsBySale(saleId);
        if (returns.isEmpty()) {
            System.out.println("No returns found for sale " + saleId + ".");
            return;
        }
        for (Return ret : returns) {
            printReturn(ret);
        }
    }

    private void printReturn(Return ret) {
        System.out.println("Return " + ret.getId()
                + " | date: " + ret.getDate()
                + " | customer: " + ret.getCustomer().getFirstName() + " " + ret.getCustomer().getLastName()
                + " | items: " + ret.getProducts().size()
                + " | refund: $" + ret.calculateRefundAmount());
    }
}
