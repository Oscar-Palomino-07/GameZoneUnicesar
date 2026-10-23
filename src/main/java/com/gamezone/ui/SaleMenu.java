package com.gamezone.ui;

import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.service.SaleService;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu of the sales module: registers sales and queries the sales history.
 * Every option delegates to the {@link SaleService}.
 */
public class SaleMenu implements Menu {

    private final SaleService saleService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the sales menu.
     *
     * @param saleService   the service used for sale operations
     * @param input         the reader of the user input
     * @param errorReporter the reporter that turns errors into messages
     */
    public SaleMenu(SaleService saleService, ConsoleInput input, ConsoleErrorReporter errorReporter) {
        this.saleService = saleService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "Sales";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== SALES ===");
            System.out.println("1. Register sale");
            System.out.println("2. View all sales");
            System.out.println("3. View sales by customer");
            System.out.println("4. View sales by seller");
            System.out.println("5. Ver detalle de una venta (recibo)");
            System.out.println("0. Back");
            int option = input.readInt("Choose an option: ");
            switch (option) {
                case 1:
                    registerSale();
                    break;
                case 2:
                    viewAllSales();
                    break;
                case 3:
                    viewSalesByCustomer();
                    break;
                case 4:
                    viewSalesBySeller();
                    break;
                case 5:
                    viewSaleDetail();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void registerSale() {
        String customerId = input.readText("Customer id: ");
        String sellerId = input.readText("Seller id: ");
        String productIdsInput = input.readText("Product ids (comma separated): ");
        List<String> productIds = input.parseIds(productIdsInput);
        String accessoryIdsInput = input.readText("Accessory ids (comma separated, optional): ");
        productIds.addAll(input.parseIds(accessoryIdsInput));
        List<String> extendedWarrantyIds = askExtendedWarranties(productIds);
        errorReporter.runGuarded(() -> {
            Sale sale = saleService.registerSale(customerId, sellerId, productIds, extendedWarrantyIds);
            System.out.println("Venta " + sale.getId() + " registrada.");
            System.out.println(sale.generateReceipt());
        });
    }

    /**
     * Asks the user, for every distinct console included in the sale, whether
     * the extended warranty must be added.
     *
     * @param itemIds the identifiers of the items entered for the sale
     * @return the identifiers of the consoles for which the user accepted the
     *         extended warranty
     */
    private List<String> askExtendedWarranties(List<String> itemIds) {
        List<String> extendedWarrantyIds = new ArrayList<>();
        for (Product item : saleService.findItemsEligibleForExtendedWarranty(itemIds)) {
            String answer = input.readText("¿Desea garantía extendida para la consola " + item.getTitle()
                    + " (" + item.getId() + ")? (s/n): ");
            if (answer.equalsIgnoreCase("s")) {
                extendedWarrantyIds.add(item.getId());
            }
        }
        return extendedWarrantyIds;
    }

    private void viewAllSales() {
        List<Sale> sales = saleService.viewAllSales();
        if (sales.isEmpty()) {
            System.out.println("No sales registered.");
            return;
        }
        for (Sale sale : sales) {
            printSale(sale);
        }
    }

    private void viewSalesByCustomer() {
        String customerId = input.readText("Customer id: ");
        List<Sale> sales = saleService.viewSalesByCustomer(customerId);
        if (sales.isEmpty()) {
            System.out.println("No sales found for customer " + customerId + ".");
            return;
        }
        for (Sale sale : sales) {
            printSale(sale);
        }
    }

    private void viewSalesBySeller() {
        String sellerId = input.readText("Seller id: ");
        List<Sale> sales = saleService.viewSalesBySeller(sellerId);
        if (sales.isEmpty()) {
            System.out.println("No sales found for seller " + sellerId + ".");
            return;
        }
        for (Sale sale : sales) {
            printSale(sale);
        }
    }

    private void viewSaleDetail() {
        String saleId = input.readText("Id de la venta: ");
        Sale sale = saleService.findById(saleId);
        if (sale == null) {
            System.out.println("No se encontró la venta " + saleId + ".");
            return;
        }
        System.out.println(sale.generateReceipt());
    }

    private void printSale(Sale sale) {
        System.out.println("Sale " + sale.getId()
                + " | date: " + sale.getDate()
                + " | customer: " + sale.getCustomer().getFirstName() + " " + sale.getCustomer().getLastName()
                + " | seller: " + sale.getSeller().getFirstName() + " " + sale.getSeller().getLastName()
                + " | items: " + sale.getProducts().size()
                + " | total: $" + sale.calculateTotal()
                + (sale.hasDiscount()
                        ? " | promoción: " + sale.getAppliedPromotionName() + " (-$" + sale.getDiscountAmount() + ")"
                        : "")
                + (sale.canBeReturned() ? " [returnable]" : " [return expired]"));
    }
}
