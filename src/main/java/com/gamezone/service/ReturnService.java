package com.gamezone.service;

import com.gamezone.model.*;
import com.gamezone.persistence.ReturnRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * Exposes the business operations related to the returns of the store.
 *
 * <p>It validates every return against its original sale: the sale must
 * exist and be within the 30-day return period, every returned item must be
 * part of the sale, and the units returned for an item, adding the earlier
 * returns of the same sale, can never exceed the units purchased. Only when
 * every item passes these rules is the stock restored, through
 * {@link ProductService} or {@link AccessoryService} according to the type of
 * each item, the warranties of every returned console are cancelled through
 * {@link WarrantyService}, and the return persisted. The monthly report of
 * sales, returns and net balance is built by {@link MonthlyBalanceService}.</p>
 *
 * <p>This class belongs to the service layer and is the only one allowed to
 * invoke the return repository.</p>
 */
public class ReturnService {

    private final SaleService saleService;
    private final PersonService personService;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final WarrantyService warrantyService;
    private final ReturnRepository returnRepository;
    private final List<Return> returns;

    /**
     * Creates the return service, loading the stored returns from the
     * repository into memory.
     *
     * @param returnRepository the repository used to persist returns
     * @param saleService      the service used to locate the original sales
     * @param personService    the service used to locate customers and sellers
     * @param productService   the service used to locate products and restore
     *                         their stock
     * @param accessoryService the service used to locate accessories and
     *                         restore their stock
     * @param warrantyService  the service used to cancel the warranties of
     *                         the returned consoles
     */
    public ReturnService(ReturnRepository returnRepository, SaleService saleService, PersonService personService,
            ProductService productService, AccessoryService accessoryService, WarrantyService warrantyService) {
        this.saleService = saleService;
        this.returnRepository = returnRepository;
        this.personService = personService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.warrantyService = warrantyService;
        this.returns = new ArrayList<>(returnRepository.loadAll());
    }

    /**
     * Registers the return of some or all the items of a sale.
     *
     * <p>All the validations run before anything is modified, so a rejected
     * return leaves the inventory and the returns registry untouched. The
     * return identifier is generated automatically following the D-N
     * sequence of the stored returns.</p>
     *
     * @param saleId     the identifier of the original sale
     * @param productIds the identifiers of the returned items, repeated once
     *                   per unit
     * @return the registered return
     * @throws IllegalArgumentException when an identifier is missing, the sale
     *         does not exist or its return period expired, or an item does
     *         not exist, is not part of the sale or exceeds the purchased
     *         units
     * @throws IllegalStateException when the requested units, added to the
     *         units already returned for the same sale, exceed the purchased
     *         units
     */
    public Return registerReturn(String saleId, List<String> productIds) {
        if (saleId == null || saleId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la venta es obligatorio.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("La devolución debe incluir al menos un producto.");
        }
        for (String pid : productIds) {
            if (pid == null || pid.isBlank()) {
                throw new IllegalArgumentException("La lista de productos no puede contener identificadores vacíos.");
            }
        }

        Sale originalSale = saleService.findById(saleId);
        if (originalSale == null) {
            throw new IllegalArgumentException("No se encontró la venta original: " + saleId);
        }
        if (!originalSale.canBeReturned()) {
            throw new IllegalArgumentException(
                    "El plazo de devolución venció. Solo se aceptan devoluciones dentro de los 30 días siguientes a la venta.");
        }

        Customer customer = originalSale.getCustomer();
        Seller seller = originalSale.getSeller();

        Map<String, Integer> quantities = new HashMap<>();
        for (String productId : productIds) {
            quantities.merge(productId, 1, Integer::sum);
        }

        Map<String, Integer> alreadyReturned = countAlreadyReturnedByProduct(originalSale);

        List<Product> productsToReturn = new ArrayList<>();
        Map<Product, Integer> itemsToRestock = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String productId = entry.getKey();
            int qtyToReturn = entry.getValue();

            Product systemProduct = resolveItem(productId);
            if (systemProduct == null) {
                throw new IllegalArgumentException("El producto no existe en el sistema: " + productId);
            }

            int qtyBought = originalSale.countUnits(productId);

            if (qtyBought == 0) {
                throw new IllegalArgumentException(
                        "El producto " + productId + " no forma parte de la venta " + saleId + ".");
            }
            if (qtyToReturn > qtyBought) {
                throw new IllegalArgumentException(
                        "No se pueden devolver más unidades de las compradas del producto " + productId
                        + " (compradas: " + qtyBought + ", solicitadas: " + qtyToReturn + ").");
            }

            int previouslyReturned = alreadyReturned.getOrDefault(productId, 0);
            if (previouslyReturned + qtyToReturn > qtyBought) {
                throw new IllegalStateException(
                        "El producto " + productId + " de la venta " + saleId + " ya tiene "
                        + previouslyReturned + " unidad(es) devuelta(s) de " + qtyBought
                        + " compradas; no se pueden devolver " + qtyToReturn + " más.");
            }

            for (int i = 0; i < qtyToReturn; i++) {
                productsToReturn.add(systemProduct);
            }
            itemsToRestock.put(systemProduct, qtyToReturn);
        }

        // The stock is restored only after every item passed its validations,
        // so a rejected return leaves the inventory untouched.
        for (Map.Entry<Product, Integer> entry : itemsToRestock.entrySet()) {
            restoreStock(entry.getKey(), entry.getValue());
        }

        // A returned console cannot keep an active warranty: its warranties are
        // cancelled once per returned unit and the extended cost is refunded.
        double warrantyRefund = 0.0;
        for (Product product : productsToReturn) {
            if (product.supportsWarranty()) {
                warrantyRefund += warrantyService.cancelWarranties(product.getId(), saleId);
            }
        }

        Return returnObj = new Return(nextReturnId(), saleId, customer, seller, productsToReturn,
                originalSale.calculateSubtotal(), originalSale.getDiscountAmount());
        returnObj.setWarrantyRefund(warrantyRefund);
        returns.add(returnObj);
        save();

        return returnObj;
    }

    /**
     * Resolves a returned item either from the product inventory or from the
     * accessory inventory, so a sale that mixes video games, consoles and
     * accessories can be returned in full.
     *
     * @param itemId the identifier of the returned item
     * @return the matching item, or {@code null} when neither inventory matches
     */
    private Product resolveItem(String itemId) {
        Product item = productService.findById(itemId);
        if (item == null) {
            item = accessoryService.findById(itemId);
        }
        return item;
    }

    /**
     * Puts the returned units back into inventory, delegating to the service
     * that owns the item type.
     *
     * @param item     the returned item whose stock must be restored
     * @param quantity the number of units to add back to the stock
     */
    private void restoreStock(Product item, int quantity) {
        if (item instanceof Accessory) {
            accessoryService.restoreStock(item.getId(), quantity);
        } else {
            productService.restoreStock(item.getId(), quantity);
        }
    }

    /**
     * Counts, per item, the units already returned for the given sale. Old
     * returns stored without a sale identifier are matched by customer and
     * seller.
     *
     * @param sale the original sale
     * @return the units already returned, keyed by item identifier
     */
    private Map<String, Integer> countAlreadyReturnedByProduct(Sale sale) {
        Set<String> saleProductIds = new HashSet<>();
        for (Product p : sale.getProducts()) {
            saleProductIds.add(p.getId());
        }

        Map<String, Integer> countMap = new HashMap<>();

        for (Return ret : returns) {
            if (ret.getSaleId() != null) {
                if (!ret.getSaleId().equals(sale.getId())) {
                    continue;
                }
            } else {
                if (!ret.getCustomer().getId().equals(sale.getCustomer().getId())) {
                    continue;
                }
                if (!ret.getSeller().getId().equals(sale.getSeller().getId())) {
                    continue;
                }
            }

            for (Product p : ret.getProducts()) {
                if (saleProductIds.contains(p.getId())) {
                    countMap.merge(p.getId(), 1, Integer::sum);
                }
            }
        }
        return countMap;
    }

    /**
     * @return an unmodifiable view of all the registered returns
     */
    public List<Return> viewAllReturns() {
        return Collections.unmodifiableList(returns);
    }

    /**
     * Returns the returns registered for a specific customer.
     *
     * @param customerId the identifier of the customer to look for
     * @return the returns of the customer, or an empty list when none matches
     * @throws IllegalArgumentException when the identifier is missing
     */
    public List<Return> viewReturnsByCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("El identificador del cliente es obligatorio.");
        }
        List<Return> result = new ArrayList<>();
        for (Return re : returns) {
            if (re.getCustomer() != null && re.getCustomer().getId().equals(customerId)) {
                result.add(re);
            }
        }
        return result;
    }

    /**
     * Returns the returns registered for a specific sale.
     *
     * @param saleId the identifier of the sale to look for
     * @return the returns of the sale, or an empty list when none matches
     * @throws IllegalArgumentException when the identifier is missing
     */
    public List<Return> viewReturnsBySale(String saleId) {
        if (saleId == null || saleId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la venta es obligatorio.");
        }
        List<Return> result = new ArrayList<>();
        for (Return re : returns) {
            if (saleId.equals(re.getSaleId())) {
                result.add(re);
            }
        }
        return result;
    }

    /**
     * Persists the current state of the returns registry into the data file.
     */
    public void save() {
        returnRepository.saveAll(returns);
    }

    /**
     * Generates the next return identifier following the D-N sequence,
     * taking the largest numeric suffix among the stored returns as
     * reference.
     *
     * @return the next available return identifier
     */
    private String nextReturnId() {
        int max = 0;
        for (Return re : returns) {
            String id = re.getId();
            if (id.startsWith("D-")) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(2)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "D-" + (max + 1);
    }

}
