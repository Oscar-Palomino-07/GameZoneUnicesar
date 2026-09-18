package com.gamezone.service;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Product;
import com.gamezone.model.Promotion;
import com.gamezone.persistence.PromotionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Provides the business operations for the promotion module of GameZone
 * Unicesar. Promotion identifiers are generated automatically following the
 * PR-N sequence, and every change is persisted immediately through the
 * {@link PromotionRepository}.
 */
public class PromotionService {

    // Prefix of the promotion identifiers: PR-1, PR-2, PR-3...
    private static final String ID_PREFIX = "PR-";

    // Allowed values for the target category of a category promotion.
    private static final String VIDEOGAME_CATEGORY = "VIDEOGAME";
    private static final String CONSOLE_CATEGORY = "CONSOLE";

    private final PromotionRepository repository;
    // In-memory list of promotions, loaded once when the service is created.
    private final List<Promotion> promotions;

    /**
     * Creates the promotion service and loads the stored promotions.
     *
     * @param repository the repository used to persist and load promotions
     */
    public PromotionService(PromotionRepository repository) {
        this.repository = repository;
        this.promotions = new ArrayList<>(repository.loadAll());
    }

    /**
     * Registers a promotion that applies a percentage discount to the whole sale.
     *
     * @param name       the promotion name shown on the receipt
     * @param percentage the discount percentage, between 0 and 100
     * @param startDate  the first day the promotion is active
     * @param endDate    the last day the promotion is active
     * @return the registered promotion
     * @throws IllegalArgumentException when any value is invalid
     */
    public PercentageDiscount registerPercentage(String name, double percentage,
                                                 LocalDate startDate, LocalDate endDate) {
        validateCommonFields(name, startDate, endDate);
        validatePercentage(percentage);
        PercentageDiscount promotion = new PercentageDiscount(nextPromotionId(), name.trim(),
                startDate, endDate, percentage);
        promotions.add(promotion);
        save();
        return promotion;
    }

    /**
     * Registers a promotion that applies a percentage discount only to the
     * products of one category.
     *
     * @param name           the promotion name shown on the receipt
     * @param percentage     the discount percentage, between 0 and 100
     * @param targetCategory VIDEOGAME or CONSOLE, case insensitive
     * @param startDate      the first day the promotion is active
     * @param endDate        the last day the promotion is active
     * @return the registered promotion
     * @throws IllegalArgumentException when any value is invalid
     */
    public CategoryDiscount registerCategory(String name, double percentage, String targetCategory,
                                             LocalDate startDate, LocalDate endDate) {
        validateCommonFields(name, startDate, endDate);
        validatePercentage(percentage);
        String category = normalizeCategory(targetCategory);
        CategoryDiscount promotion = new CategoryDiscount(nextPromotionId(), name.trim(),
                startDate, endDate, percentage, category);
        promotions.add(promotion);
        save();
        return promotion;
    }

    /**
     * Registers a promotion that applies a percentage discount to the whole
     * sale when it includes a minimum number of products.
     *
     * @param name            the promotion name shown on the receipt
     * @param percentage      the discount percentage, between 0 and 100
     * @param minimumQuantity the minimum number of products, greater than zero
     * @param startDate       the first day the promotion is active
     * @param endDate         the last day the promotion is active
     * @return the registered promotion
     * @throws IllegalArgumentException when any value is invalid
     */
    public BulkPurchaseDiscount registerBulkPurchase(String name, double percentage, int minimumQuantity,
                                                     LocalDate startDate, LocalDate endDate) {
        validateCommonFields(name, startDate, endDate);
        validatePercentage(percentage);
        if (minimumQuantity <= 0) {
            throw new IllegalArgumentException("La cantidad mínima de productos debe ser mayor que cero.");
        }
        BulkPurchaseDiscount promotion = new BulkPurchaseDiscount(nextPromotionId(), name.trim(),
                startDate, endDate, percentage, minimumQuantity);
        promotions.add(promotion);
        save();
        return promotion;
    }

    /**
     * Returns all registered promotions, active or not.
     *
     * @return an unmodifiable view of all registered promotions
     */
    public List<Promotion> listAllPromotions() {
        return Collections.unmodifiableList(promotions);
    }

    /**
     * Returns the promotions that are active on the given date, that is, the
     * promotions whose validity window contains that date.
     *
     * @param date the date to check
     * @return the active promotions, or an empty list when none is active
     */
    public List<Promotion> listActivePromotions(LocalDate date) {
        List<Promotion> result = new ArrayList<>();
        for (Promotion promotion : promotions) {
            // The date rule belongs to the model; the service only supplies the date.
            if (promotion.isActive(date)) {
                result.add(promotion);
            }
        }
        return result;
    }

    /**
     * Finds a promotion by its identifier.
     *
     * @param id the identifier to look for
     * @return the matching promotion, or {@code null} if it is not registered
     */
    public Promotion findById(String id) {
        for (Promotion promotion : promotions) {
            if (promotion.getId().equals(id)) {
                return promotion;
            }
        }
        return null;
    }

    /**
     * Chooses, among the active promotions, the one that gives the greatest
     * discount to the given set of products. Promotions are not cumulative, so
     * only one promotion is returned.
     *
     * @param products the products of the sale to evaluate
     * @return the promotion with the greatest discount, or an empty
     *         {@link Optional} when no promotion applies or the greatest
     *         discount is zero
     * @throws IllegalArgumentException when the product list is {@code null}
     */
    public Optional<Promotion> bestPromotionFor(List<Product> products) {
        if (products == null) {
            throw new IllegalArgumentException("La lista de productos es obligatoria para buscar promociones.");
        }
        Promotion bestPromotion = null;
        double maxDiscount = 0.0;
        for (Promotion promotion : listActivePromotions(LocalDate.now())) {
            // Polymorphism: each promotion type calculates its own discount.
            double discount = promotion.calculateDiscount(products);
            // Strictly greater, so a zero discount never becomes the best promotion.
            if (discount > maxDiscount) {
                maxDiscount = discount;
                bestPromotion = promotion;
            }
        }
        return Optional.ofNullable(bestPromotion);
    }

    // Validates the attributes shared by every promotion.
    private void validateCommonFields(String name, LocalDate startDate, LocalDate endDate) {
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

    // Checks that a percentage is between 0 and 100.
    private void validatePercentage(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }
    }

    // Converts the category to upper case and checks it is VIDEOGAME or CONSOLE.
    private String normalizeCategory(String category) {
        String value = category == null ? "" : category.trim().toUpperCase();
        if (!value.equals(VIDEOGAME_CATEGORY) && !value.equals(CONSOLE_CATEGORY)) {
            throw new IllegalArgumentException("La categoría debe ser VIDEOGAME o CONSOLE.");
        }
        return value;
    }

    // Returns the next identifier of the PR-N sequence, based on the highest existing number.
    private String nextPromotionId() {
        int max = 0;
        for (Promotion promotion : promotions) {
            String id = promotion.getId();
            if (id.startsWith(ID_PREFIX)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(ID_PREFIX.length())));
                } catch (NumberFormatException ignored) {
                    // An identifier without a number does not change the sequence.
                }
            }
        }
        return ID_PREFIX + (max + 1);
    }

    // Persists the current list of promotions.
    private void save() {
        repository.saveAll(promotions);
    }
}