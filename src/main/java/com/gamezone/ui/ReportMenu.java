package com.gamezone.ui;

import com.gamezone.service.MonthlyBalanceService;

/**
 * Submenu of the reports module: shows the monthly balance of the store. Every
 * option delegates to the {@link MonthlyBalanceService}.
 */
public class ReportMenu implements Menu {

    private final MonthlyBalanceService monthlyBalanceService;
    private final ConsoleInput input;
    private final ConsoleErrorReporter errorReporter;

    /**
     * Creates the reports menu.
     *
     * @param monthlyBalanceService the service that calculates the monthly balance
     * @param input                 the reader of the user input
     * @param errorReporter         the reporter that turns errors into messages
     */
    public ReportMenu(MonthlyBalanceService monthlyBalanceService, ConsoleInput input,
                      ConsoleErrorReporter errorReporter) {
        this.monthlyBalanceService = monthlyBalanceService;
        this.input = input;
        this.errorReporter = errorReporter;
    }

    @Override
    public String getLabel() {
        return "Reports";
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("=== REPORTS ===");
            System.out.println("1. Balance mensual (ventas - devoluciones)");
            System.out.println("0. Back");
            int option = input.readInt("Choose an option: ");
            switch (option) {
                case 1:
                    monthlyBalance();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void monthlyBalance() {
        int month = input.readInt("Mes (1-12): ");
        int year  = input.readInt("Año (ej. 2026): ");
        errorReporter.runGuarded(() -> {
            double totalSales = monthlyBalanceService.calculateMonthlySales(month, year);
            double totalReturns = monthlyBalanceService.calculateMonthlyReturns(month, year);
            double balance = monthlyBalanceService.generateMonthlyBalance(month, year);
            System.out.printf("Balance mensual %02d/%d%n", month, year);
            System.out.printf("  Total de ventas:       $%.2f%n", totalSales);
            System.out.printf("  Total de devoluciones: $%.2f%n", totalReturns);
            System.out.printf("  Balance neto:          $%.2f%n", balance);
        });
    }
}
