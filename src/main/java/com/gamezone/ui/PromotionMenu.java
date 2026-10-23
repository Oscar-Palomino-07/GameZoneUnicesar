package com.gamezone.ui;

import com.gamezone.model.Promotion;
import com.gamezone.service.PromotionService;

import java.time.LocalDate;
import java.util.List;

/**
 * Submenu of the promotions module: registers promotions and lists them. Every
 * option delegates to the {@link PromotionService}.
 */
public class PromotionMenu implements Menu {

    private final PromotionService promotionService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the promotions menu.
     *
     * @param promotionService the service used for promotion operations
     * @param input            the reader of the user input
     * @param errorReporter    the reporter that turns errors into messages
     */
    public PromotionMenu(PromotionService promotionService, ConsoleInput input,
                         ConsoleErrorReporter errorReporter) {
        this.promotionService = promotionService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "Gestión de promociones";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== GESTIÓN DE PROMOCIONES ===");
            System.out.println("1. Registrar promoción por porcentaje");
            System.out.println("2. Registrar promoción por categoría");
            System.out.println("3. Registrar promoción por volumen de compra");
            System.out.println("4. Listar todas las promociones");
            System.out.println("5. Listar promociones vigentes");
            System.out.println("0. Volver");
            int option = input.readInt("Seleccione una opción: ");
            switch (option) {
                case 1:
                    registerPercentagePromotion();
                    break;
                case 2:
                    registerCategoryPromotion();
                    break;
                case 3:
                    registerBulkPurchasePromotion();
                    break;
                case 4:
                    printPromotions(promotionService.listAllPromotions(), "No hay promociones registradas.");
                    break;
                case 5:
                    printPromotions(promotionService.listActivePromotions(LocalDate.now()),
                            "No hay promociones vigentes hoy.");
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Opción inválida. Intente de nuevo.");
            }
        }
    }

    private void registerPercentagePromotion() {
        String name = input.readText("Nombre de la promoción: ");
        double percentage = input.readDouble("Porcentaje de descuento (0-100): ");
        LocalDate startDate = input.readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate endDate = input.readDate("Fecha de fin (AAAA-MM-DD): ");
        if (startDate == null || endDate == null) {
            return;
        }
        errorReporter.runGuarded(() -> {
            Promotion promotion = promotionService.registerPercentage(name, percentage, startDate, endDate);
            System.out.println("Promoción " + promotion.getId() + " registrada.");
        });
    }

    private void registerCategoryPromotion() {
        String name = input.readText("Nombre de la promoción: ");
        double percentage = input.readDouble("Porcentaje de descuento (0-100): ");
        String category = readPromotionCategory();
        if (category == null) {
            return;
        }
        LocalDate startDate = input.readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate endDate = input.readDate("Fecha de fin (AAAA-MM-DD): ");
        if (startDate == null || endDate == null) {
            return;
        }
        errorReporter.runGuarded(() -> {
            Promotion promotion = promotionService.registerCategory(name, percentage, category, startDate, endDate);
            System.out.println("Promoción " + promotion.getId() + " registrada.");
        });
    }

    // Lets the user pick the target category of a category promotion.
    private String readPromotionCategory() {
        System.out.println("Categoría:");
        System.out.println("1. Videojuegos");
        System.out.println("2. Consolas");
        System.out.println("3. Accesorios");
        switch (input.readInt("Seleccione una opción: ")) {
            case 1:
                return "VIDEOGAME";
            case 2:
                return "CONSOLE";
            case 3:
                return "ACCESSORY";
            default:
                System.out.println("Categoría no válida.");
                return null;
        }
    }

    private void registerBulkPurchasePromotion() {
        String name = input.readText("Nombre de la promoción: ");
        double percentage = input.readDouble("Porcentaje de descuento (0-100): ");
        int minimumQuantity = input.readInt("Cantidad mínima de productos: ");
        LocalDate startDate = input.readDate("Fecha de inicio (AAAA-MM-DD): ");
        LocalDate endDate = input.readDate("Fecha de fin (AAAA-MM-DD): ");
        if (startDate == null || endDate == null) {
            return;
        }
        errorReporter.runGuarded(() -> {
            Promotion promotion = promotionService.registerBulkPurchase(name, percentage, minimumQuantity,
                    startDate, endDate);
            System.out.println("Promoción " + promotion.getId() + " registrada.");
        });
    }

    private void printPromotions(List<Promotion> promotions, String emptyMessage) {
        if (promotions.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Promotion promotion : promotions) {
            System.out.println(promotion.getId()
                    + " | " + promotion.getName()
                    + " | " + promotion.getDiscountPercentage() + "%"
                    + " | Vigencia: " + promotion.getStartDate() + " a " + promotion.getEndDate()
                    + " | Estado: " + (promotion.isActive() ? "Vigente" : "No vigente"));
        }
    }
}
