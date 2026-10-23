package com.gamezone.service;

import com.gamezone.model.Return;
import com.gamezone.model.Sale;

import java.time.LocalDate;

/**
 * Builds the monthly report of the store: the total sold, the total refunded
 * and the net balance of a month.
 *
 * <p>The report is a responsibility of its own that combines the sales and
 * the returns, so it lives in this class instead of in {@link ReturnService}.
 * It only reads: the sales come from {@link SaleService} and the returns from
 * {@link ReturnService}, and each sale and return computes its own amount.</p>
 *
 * <p>This class belongs to the service layer.</p>
 */
public class MonthlyBalanceService {

    private final SaleService saleService;
    private final ReturnService returnService;

    /**
     * Creates the monthly balance service.
     *
     * @param saleService   the service that provides the registered sales
     * @param returnService the service that provides the registered returns
     */
    public MonthlyBalanceService(SaleService saleService, ReturnService returnService) {
        this.saleService = saleService;
        this.returnService = returnService;
    }

    /**
     * Calculates the total sold in the given month. Each sale contributes its
     * final total, that is, the subtotal minus the discount of the applied
     * promotion plus the cost of the extended warranties.
     *
     * @param month the month to report, from 1 to 12
     * @param year  the year to report, 2000 or later
     * @return the sum of the final totals of the sales registered in the month
     * @throws IllegalArgumentException when the month or the year is invalid
     */
    public double calculateMonthlySales(int month, int year) {
        validatePeriod(month, year);
        double totalSales = 0.0;
        for (Sale sale : saleService.viewAllSales()) {
            if (isInPeriod(sale.getDate(), month, year)) {
                totalSales += sale.calculateTotal();
            }
        }
        return totalSales;
    }

    /**
     * Calculates the total refunded in the given month, adding the refund
     * amount of every return registered in that month.
     *
     * @param month the month to report, from 1 to 12
     * @param year  the year to report, 2000 or later
     * @return the sum of the refund amounts of the returns registered in the
     *         month
     * @throws IllegalArgumentException when the month or the year is invalid
     */
    public double calculateMonthlyReturns(int month, int year) {
        validatePeriod(month, year);
        double totalReturns = 0.0;
        for (Return returnObj : returnService.viewAllReturns()) {
            if (isInPeriod(returnObj.getDate(), month, year)) {
                totalReturns += returnObj.calculateRefundAmount();
            }
        }
        return totalReturns;
    }

    /**
     * Generates the net balance of the given month: the total sold minus the
     * total refunded. The balance can be negative, for example when the
     * returns of the month belong to sales registered in the previous month.
     *
     * @param month the month to report, from 1 to 12
     * @param year  the year to report, 2000 or later
     * @return the difference between {@link #calculateMonthlySales(int, int)}
     *         and {@link #calculateMonthlyReturns(int, int)}
     * @throws IllegalArgumentException when the month or the year is invalid
     */
    public double generateMonthlyBalance(int month, int year) {
        return calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year);
    }

    /**
     * Checks that the month and the year of a report are valid.
     *
     * @param month the month to check, from 1 to 12
     * @param year  the year to check, 2000 or later
     * @throws IllegalArgumentException when the month or the year is invalid
     */
    private void validatePeriod(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mes inválido: " + month + ". Debe estar entre 1 y 12.");
        }
        if (year < 2000) {
            throw new IllegalArgumentException("Año inválido: " + year + ". Debe ser 2000 o posterior.");
        }
    }

    /**
     * Tells whether a date belongs to the given month and year.
     *
     * @param date  the date to check
     * @param month the month of the period
     * @param year  the year of the period
     * @return {@code true} when the date falls inside the period
     */
    private boolean isInPeriod(LocalDate date, int month, int year) {
        return date != null && date.getMonthValue() == month && date.getYear() == year;
    }
}
