package com.gamezone.service;

import com.gamezone.model.BasicWarranty;
import com.gamezone.model.Console;
import com.gamezone.model.ExtendedWarranty;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Warranty;
import com.gamezone.persistence.SaleRepository;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.persistence.WarrantyRepository.WarrantyRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides the business operations for the warranty module of GameZone
 * Unicesar. Warranty identifiers are generated automatically following the
 * W-N sequence, and every new warranty is persisted immediately through the
 * {@link WarrantyRepository}.
 *
 * <p>Stored warranties only keep the identifiers of the sale and of the
 * covered product, so this service is the one that rebuilds those references:
 * the sale is located with the {@link SaleRepository} and the product with
 * the {@link ProductService}. Resolving them here, and not in the repository,
 * keeps the dependency direction {@code ui → service → persistence → model}:
 * the repositories know nothing about the sales flow, therefore they cannot
 * depend on a service and no cycle can be formed between them. This is also
 * why this service must be built before {@link SaleService}, which needs it
 * to assign the warranties of the consoles it sells.</p>
 */
public class WarrantyService {

    // Prefix of the warranty identifiers: W-1, W-2, W-3...
    private static final String ID_PREFIX = "W-";

    private final WarrantyRepository warrantyRepository;
    private final SaleRepository saleRepository;
    private final ProductService productService;
    // In-memory list of warranties, loaded once when the service is created.
    private final List<Warranty> warranties;

    /**
     * Creates the warranty service and rebuilds the stored warranties from
     * their identifiers.
     *
     * @param warrantyRepository the repository used to persist and load the
     *                           stored warranty records
     * @param saleRepository    the repository used to locate the sale each
     *                           warranty refers to
     * @param productService    the service used to locate the covered product,
     *                           whether it is a video game, a console or an
     *                           accessory
     */
    public WarrantyService(WarrantyRepository warrantyRepository, SaleRepository saleRepository,
            ProductService productService) {
        this.warrantyRepository = warrantyRepository;
        this.saleRepository = saleRepository;
        this.productService = productService;
        this.warranties = rebuildWarranties();
    }

    /**
     * Rebuilds the stored warranties, resolving the sale and the product of
     * every record. A record whose sale or product can no longer be found is
     * skipped, because a warranty cannot exist without both of them.
     *
     * @return the warranties that could be rebuilt
     */
    private List<Warranty> rebuildWarranties() {
        List<Warranty> rebuilt = new ArrayList<>();
        List<WarrantyRecord> records = warrantyRepository.loadAll();
        if (records.isEmpty()) {
            return rebuilt;
        }
        List<Sale> sales = saleRepository.loadAll();
        for (WarrantyRecord record : records) {
            Sale sale = findSale(sales, record.getSaleId());
            Product product = productService.findById(record.getProductId());
            if (sale == null || product == null) {
                System.err.println("Skipping warranty " + record.getId()
                        + ": sale or product not found (sale=" + record.getSaleId()
                        + ", product=" + record.getProductId() + ").");
                continue;
            }
            LocalDate startDate = LocalDate.parse(record.getStartDate());
            if (WarrantyRepository.isExtended(record.getType())) {
                rebuilt.add(new ExtendedWarranty(record.getId(), product, sale, startDate));
            } else {
                rebuilt.add(new BasicWarranty(record.getId(), product, sale, startDate));
            }
        }
        return rebuilt;
    }

    /**
     * Finds a sale by its identifier among the stored sales.
     *
     * @param sales  the stored sales
     * @param saleId the identifier to look for
     * @return the matching sale, or {@code null} when it does not exist
     */
    private Sale findSale(List<Sale> sales, String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
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

    /**
     * Finds the warranty of a product within a specific sale. When the product
     * has both a basic and an extended warranty in that sale, the extended one
     * is returned because it offers the widest coverage.
     *
     * @param productId the identifier of the product
     * @param saleId    the identifier of the sale
     * @return the matching warranty, or {@code null} if none exists
     */
    public Warranty findWarrantyByProduct(String productId, String saleId) {
        Warranty found = null;
        for (Warranty warranty : warranties) {
            boolean sameProduct = warranty.getProduct().getId().equals(productId);
            boolean sameSale = warranty.getSale().getId().equals(saleId);
            if (sameProduct && sameSale) {
                if (warranty instanceof ExtendedWarranty) {
                    return warranty;
                }
                if (found == null) {
                    found = warranty;
                }
            }
        }
        return found;
    }

    /**
     * Cancels the warranties of one returned unit of a product in the given
     * sale, because a returned console cannot keep an active warranty. Each
     * unit of a console receives one basic warranty and, when requested, one
     * extended warranty, so every call removes at most one of each; calling it
     * once per returned unit keeps the warranties of the units the customer
     * kept. The removal is persisted immediately.
     *
     * <p>The basic warranty is free, so it refunds nothing; the extended
     * warranty refunds the additional cost the customer paid for it.</p>
     *
     * @param productId the identifier of the returned product
     * @param saleId    the identifier of the sale in which it was bought
     * @return the refundable amount of the cancelled warranties: zero for the
     *         basic warranty plus the additional cost of the extended one
     * @throws IllegalArgumentException when an identifier is missing
     */
    public double cancelWarranties(String productId, String saleId) {
        if (productId == null || productId.isBlank() || saleId == null || saleId.isBlank()) {
            throw new IllegalArgumentException("El producto y la venta son obligatorios para anular garantías.");
        }
        Warranty basic = null;
        Warranty extended = null;
        for (Warranty warranty : warranties) {
            boolean sameProduct = warranty.getProduct().getId().equals(productId);
            boolean sameSale = warranty.getSale().getId().equals(saleId);
            if (!sameProduct || !sameSale) {
                continue;
            }
            if (warranty instanceof ExtendedWarranty) {
                if (extended == null) {
                    extended = warranty;
                }
            } else if (basic == null) {
                basic = warranty;
            }
        }
        double refund = 0.0;
        if (basic != null) {
            warranties.remove(basic);
        }
        if (extended != null) {
            warranties.remove(extended);
            refund += extended.getAdditionalCost();
        }
        if (basic != null || extended != null) {
            save();
        }
        return refund;
    }

    /**
     * Returns all registered warranties.
     *
     * @return an unmodifiable view of all registered warranties
     */
    public List<Warranty> listAllWarranties() {
        return Collections.unmodifiableList(warranties);
    }

    /**
     * Returns the warranties that are active on the current date.
     *
     * @return the active warranties, or an empty list when none is active
     */
    public List<Warranty> listActiveWarranties() {
        LocalDate today = LocalDate.now();
        List<Warranty> result = new ArrayList<>();
        for (Warranty warranty : warranties) {
            // The date rule belongs to the model; the service only supplies today's date.
            if (warranty.isActive(today)) {
                result.add(warranty);
            }
        }
        return result;
    }

    /**
     * Returns the warranties whose end date falls between today and the given
     * number of days ahead, both inclusive. Warranties that already expired
     * are not included.
     *
     * @param daysAhead how many days ahead to look, zero or greater
     * @return the warranties that expire soon, or an empty list when none match
     * @throws IllegalArgumentException when {@code daysAhead} is negative
     */
    public List<Warranty> listWarrantiesExpiringSoon(int daysAhead) {
        if (daysAhead < 0) {
            throw new IllegalArgumentException("El número de días no puede ser negativo.");
        }
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(daysAhead);
        List<Warranty> result = new ArrayList<>();
        for (Warranty warranty : warranties) {
            LocalDate endDate = warranty.getEndDate();
            // Included when today <= endDate <= limit.
            if (!endDate.isBefore(today) && !endDate.isAfter(limit)) {
                result.add(warranty);
            }
        }
        return result;
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

    // Persists the current list of warranties, extracting only the identifiers
    // and the start date of each one.
    private void save() {
        List<WarrantyRecord> records = new ArrayList<>();
        for (Warranty warranty : warranties) {
            String type = warranty instanceof ExtendedWarranty
                    ? WarrantyRepository.extendedType()
                    : WarrantyRepository.basicType();
            records.add(new WarrantyRecord(type, warranty.getId(), warranty.getProduct().getId(),
                    warranty.getSale().getId(), warranty.getStartDate().toString()));
        }
        warrantyRepository.saveAll(records);
    }
}