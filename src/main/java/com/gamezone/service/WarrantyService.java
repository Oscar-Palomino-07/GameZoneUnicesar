package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.Console;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.WarrantyRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the business operations for the warranty module of GameZone
 * Unicesar. Warranty identifiers are generated automatically following the
 * W-N sequence, and every new warranty is persisted immediately through the
 * {@link WarrantyRepository}.
 */
public class WarrantyService {

    // Prefix of the warranty identifiers: W-1, W-2, W-3...
    private static final String ID_PREFIX = "W-";

    private final WarrantyRepository repository;
    // In-memory list of warranties, loaded once when the service is created.
    private final List<Warranty> warranties;

    /**
     * Creates the warranty service and loads the stored warranties.
     *
     * @param repository the repository used to persist and load warranties
     */
    public WarrantyService(WarrantyRepository repository) {
        this.repository = repository;
        this.warranties = new ArrayList<>(repository.loadAll());
    }

    /**
     * Creates and stores the automatic 6-month basic warranty for a console
     * included in a sale. The basic warranty has no additional cost.
     *
     * @param product   the console covered by the warranty
     * @param sale      the sale in which the console was bought
     * @param startDate the start date of the coverage, usually the sale date
     * @return the created basic warranty
     * @throws IllegalArgumentException when any argument is missing, the
     *         product is not a console or the product is not part of the sale
     */
    public BasicWarranty assignBasicWarranty(Product product, Sale sale, LocalDate startDate) {
        validateWarrantyRequest(product, sale, startDate);
        BasicWarranty warranty = new BasicWarranty(nextWarrantyId(), product, sale, startDate);
        warranties.add(warranty);
        save();
        return warranty;
    }

    /**
     * Creates and stores a 12-month extended warranty for a console included
     * in a sale. Its additional cost is available through
     * {@link Warranty#getAdditionalCost()}.
     *
     * @param product   the console covered by the warranty
     * @param sale      the sale in which the console was bought
     * @param startDate the start date of the coverage, usually the sale date
     * @return the created extended warranty
     * @throws IllegalArgumentException when any argument is missing, the
     *         product is not a console or the product is not part of the sale
     */
    public ExtendedWarranty assignExtendedWarranty(Product product, Sale sale, LocalDate startDate) {
        validateWarrantyRequest(product, sale, startDate);
        ExtendedWarranty warranty = new ExtendedWarranty(nextWarrantyId(), product, sale, startDate);
        warranties.add(warranty);
        save();
        return warranty;
    }

    // Checks the data needed to create a warranty: nothing missing, the product
    // is a console and the product belongs to the sale.
    private void validateWarrantyRequest(Product product, Sale sale, LocalDate startDate) {
        if (product == null || sale == null || startDate == null) {
            throw new IllegalArgumentException("El producto, la venta y la fecha de inicio son obligatorios.");
        }
        if (!(product instanceof Console)) {
            throw new IllegalArgumentException("Solo las consolas admiten garantía: " + product.getId());
        }
        boolean belongsToSale = false;
        for (Product saleProduct : sale.getProducts()) {
            if (saleProduct.getId().equals(product.getId())) {
                belongsToSale = true;
                break;
            }
        }
        if (!belongsToSale) {
            throw new IllegalArgumentException("El producto " + product.getId()
                    + " no pertenece a la venta " + sale.getId() + ".");
        }
    }

    // Returns the next identifier of the W-N sequence, based on the highest existing number.
    private String nextWarrantyId() {
        int max = 0;
        for (Warranty warranty : warranties) {
            String id = warranty.getId();
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

    // Persists the current list of warranties.
    private void save() {
        repository.saveAll(warranties);
    }
}