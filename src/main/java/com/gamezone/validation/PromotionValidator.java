package com.gamezone.validation;

import com.gamezone.model.ProductCategory;

import java.time.LocalDate;

/**
 * Provides static validation methods for the promotion module.
 *
 * <p>This class belongs to the validation layer and verifies the data of a
 * percentage, category or bulk purchase promotion before
 * {@code PromotionService} registers it, without accessing persistence or
 * services. It keeps the {@link IllegalArgumentException} and the messages
 * that the promotion module has always shown to the user.</p>
 */
public final class PromotionValidator {

    private PromotionValidator() {
    }

    /**
     * Validates the attributes shared by every promotion.
     *
     * @param name      the promotion name; must not be null or blank
     * @param startDate the first day of the promotion; must not be null
     * @param endDate   the last day of the promotion; must not be null nor
     *                  before the start date
     * @throws IllegalArgumentException when the name is blank, a date is
     *         missing or the start date is after the end date
     */
    public static void validateCommonData(String name, LocalDate startDate, LocalDate endDate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la promoción es obligatorio.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }

    /**
     * Validates that a discount percentage is between 0 and 100.
     *
     * @param percentage the discount percentage
     * @throws IllegalArgumentException when the percentage is out of range
     */
    public static void validatePercentage(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }
    }

    /**
     * Validates the minimum number of products of a bulk purchase promotion.
     *
     * @param minimumQuantity the minimum number of products; must be greater
     *                        than zero
     * @throws IllegalArgumentException when the quantity is not positive
     */
    public static void validateMinimumQuantity(int minimumQuantity) {
        if (minimumQuantity <= 0) {
            throw new IllegalArgumentException("La cantidad mínima de productos debe ser mayor que cero.");
        }
    }

    /**
     * Converts the target category of a category promotion to upper case and
     * checks that it is one of the {@link ProductCategory} values.
     *
     * @param category the category typed by the user
     * @return the category in upper case
     * @throws IllegalArgumentException when the value is not a product
     *         category
     */
    public static String normalizeCategory(String category) {
        String value = category == null ? "" : category.trim().toUpperCase();
        if (!ProductCategory.isValid(value)) {
            throw new IllegalArgumentException("La categoría debe ser VIDEOGAME, CONSOLE o ACCESSORY.");
        }
        return value;
    }
}
