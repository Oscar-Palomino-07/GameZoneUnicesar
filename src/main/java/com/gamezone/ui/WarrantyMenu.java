package com.gamezone.ui;

import com.gamezone.model.Warranty;
import com.gamezone.service.WarrantyService;

import java.time.LocalDate;
import java.util.List;

/**
 * Submenu of the warranties module: queries the warranties of the sales. Every
 * option delegates to the {@link WarrantyService}.
 */
public class WarrantyMenu implements Menu {

    private final WarrantyService warrantyService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the warranties menu.
     *
     * @param warrantyService the service used for warranty queries
     * @param input           the reader of the user input
     * @param errorReporter   the reporter that turns errors into messages
     */
    public WarrantyMenu(WarrantyService warrantyService, ConsoleInput input, ConsoleErrorReporter errorReporter) {
        this.warrantyService = warrantyService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "Gestión de garantías";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== GESTIÓN DE GARANTÍAS ===");
            System.out.println("1. Consultar la garantía de un producto en una venta");
            System.out.println("2. Listar todas las garantías");
            System.out.println("3. Listar garantías vigentes");
            System.out.println("4. Listar garantías próximas a vencer");
            System.out.println("0. Volver");
            int option = input.readInt("Seleccione una opción: ");
            switch (option) {
                case 1:
                    viewWarrantyByProduct();
                    break;
                case 2:
                    printWarranties(warrantyService.listAllWarranties(), "No hay garantías registradas.");
                    break;
                case 3:
                    printWarranties(warrantyService.listActiveWarranties(), "No hay garantías vigentes.");
                    break;
                case 4:
                    viewWarrantiesExpiringSoon();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    private void viewWarrantyByProduct() {
        String saleId = input.readText("Id de la venta: ");
        String productId = input.readText("Id del producto: ");
        Warranty warranty = warrantyService.findWarrantyByProduct(productId, saleId);
        if (warranty == null) {
            System.out.println("No se encontró una garantía para el producto " + productId
                    + " en la venta " + saleId + ".");
            return;
        }
        System.out.println(warranty.generateWarrantyCertificate());
    }

    private void viewWarrantiesExpiringSoon() {
        int daysAhead = input.readInt("¿Cuántos días de anticipación? ");
        errorReporter.runGuarded(() -> printWarranties(warrantyService.listWarrantiesExpiringSoon(daysAhead),
                "No hay garantías que venzan en los próximos " + daysAhead + " días."));
    }

    private void printWarranties(List<Warranty> warranties, String emptyMessage) {
        if (warranties.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Warranty warranty : warranties) {
            System.out.println(warranty.getId()
                    + " | " + warranty.getWarrantyType()
                    + " | Producto: " + warranty.getProduct().getTitle() + " (" + warranty.getProduct().getId() + ")"
                    + " | Venta: " + warranty.getSale().getId()
                    + " | Inicio: " + warranty.getStartDate()
                    + " | Vence: " + warranty.getEndDate()
                    + " | Estado: " + (warranty.isActive(LocalDate.now()) ? "Vigente" : "Expirada"));
        }
    }
}
