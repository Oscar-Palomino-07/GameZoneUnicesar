package com.gamezone.service;

import com.gamezone.model.Accessory;
import com.gamezone.model.Console;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SaleRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exposes the business operations related to the sales of the store.
 *
 * <p>It applies the rules that govern the registration of a sale: the sale
 * must contain at least one product, the customer, the seller and every
 * product must be registered, and the available stock must cover the quantity
 * requested for each product. When a sale is accepted, the stock of the
 * purchased products is discounted through the {@link ProductService} and the
 * sale is persisted immediately. Every console in the sale also receives an
 * automatic basic warranty, and the extended warranty can be requested for it
 * through the {@link WarrantyService}.</p>
 *
 * <p>This class belongs to the service layer and is the only one allowed to
 * invoke the sale repository.</p>
 */
public class SaleService {

    private final SaleRepository saleRepository;
    private final PersonService personService;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final WarrantyService warrantyService;
    private final List<Sale> sales;

    /**
     * Creates the sale service, loading the stored sales from the repository
     * into memory.
     *
     * @param saleRepository  the repository used to persist sales
     * @param personService   the service used to locate customers and sellers
     * @param productService  the service used to locate products and update
     *                        their stock
     * @param accessoryService the service used to locate accessories and
     *                        update their stock
     * @param warrantyService the service used to assign the warranties of the
     *                        consoles included in a sale
     */
    public SaleService(SaleRepository saleRepository, PersonService personService, ProductService productService,
            AccessoryService accessoryService, WarrantyService warrantyService) {
        this.saleRepository = saleRepository;
        this.personService = personService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.warrantyService = warrantyService;
        this.sales = new ArrayList<>(saleRepository.loadAll());
    }

    /**
     * Registers a new sale for the given customer, seller and products.
     *
     * <p>The customer and the seller are located through the person service
     * and every product is located through the product service. The quantity
     * requested for each product is validated against the available stock
     * before the sale is created. The sale identifier is generated
     * automatically following the V-N sequence of the stored sales.</p>
     *
     * @param customerId the identifier of the purchasing customer
     * @param sellerId   the identifier of the seller attending the customer
     * @param productIds the identifiers of the purchased products
     * @return the created sale
     * @throws IllegalArgumentException when the product list is empty, when
     *         the customer, the seller or a product cannot be found, or when
     *         the available stock of a product cannot cover the sale
     */
    public Sale registerSale(String customerId, String sellerId, List<String> productIds) {
        return registerSale(customerId, sellerId, productIds, Collections.emptyList());
    }

    /**
     * Registers a new sale for the given customer, seller and products,
     * managing the warranties of the consoles it includes.
     *
     * <p>Every console in the sale receives an automatic basic warranty at no
     * cost. When the identifier of a console is also listed in
     * {@code productIdsWithExtendedWarranty}, an extended warranty is assigned
     * to each unit of that console and its additional cost is added to the
     * total of the sale. All the validations (including the ones about the
     * extended warranty request) run before any stock is discounted, so a
     * rejected sale leaves the inventory untouched.</p>
     *
     * @param customerId                    the identifier of the purchasing
     *                                      customer
     * @param sellerId                      the identifier of the seller
     *                                      attending the customer
     * @param productIds                    the identifiers of the purchased
     *                                      items, repeated once per unit
     * @param productIdsWithExtendedWarranty the identifiers of the consoles
     *                                      for which the extended warranty was
     *                                      requested; {@code null} or empty
     *                                      when none was requested
     * @return the created sale
     * @throws IllegalArgumentException when the product list is empty, when
     *         the customer, the seller or a product cannot be found, when the
     *         available stock cannot cover the sale, or when the extended
     *         warranty is requested for an item that is not a console of the
     *         sale
     */
    public Sale registerSale(String customerId, String sellerId, List<String> productIds,
            List<String> productIdsWithExtendedWarranty) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("A sale must include at least one product.");
        }
        Customer customer = personService.findCustomerById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        Seller seller = personService.findSellerById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerId));

        Map<String, Integer> quantities = new HashMap<>();
        for (String productId : productIds) {
            quantities.merge(productId, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            Product item = resolveItem(entry.getKey());
            if (item == null) {
                throw new IllegalArgumentException("Product not found: " + entry.getKey());
            }
            if (item.getStock() < entry.getValue()) {
                throw new IllegalArgumentException("Not enough stock for product: " + entry.getKey());
            }
        }
        Set<String> extendedWarrantyIds = validateExtendedWarrantyRequest(productIds, productIdsWithExtendedWarranty);

        List<Product> products = new ArrayList<>();
        for (String productId : productIds) {
            Product item = resolveItem(productId);
            products.add(item);
            discountStock(item);
        }

        Sale sale = new Sale(nextSaleId(), customer, seller, products);
        sales.add(sale);
        // The sale is saved first because the warranty repository rebuilds
        // its references by looking the sale up in the stored sales.
        save();
        assignWarranties(sale, extendedWarrantyIds);
        return sale;
    }

    /**
     * Checks that every item with an extended warranty request is a console
     * that belongs to the sale.
     *
     * @param productIds     the identifiers of the items in the sale
     * @param requestedIds   the identifiers with an extended warranty request,
     *                       possibly {@code null}
     * @return the distinct identifiers that will receive an extended warranty
     * @throws IllegalArgumentException when a requested item is not part of
     *         the sale or is not a console
     */
    private Set<String> validateExtendedWarrantyRequest(List<String> productIds, List<String> requestedIds) {
        Set<String> extendedWarrantyIds = new HashSet<>();
        if (requestedIds == null) {
            return extendedWarrantyIds;
        }
        for (String requestedId : requestedIds) {
            if (!productIds.contains(requestedId)) {
                throw new IllegalArgumentException(
                        "La garantía extendida solo aplica a productos incluidos en la venta: " + requestedId);
            }
            if (!(resolveItem(requestedId) instanceof Console)) {
                throw new IllegalArgumentException("Solo las consolas admiten garantía extendida: " + requestedId);
            }
            extendedWarrantyIds.add(requestedId);
        }
        return extendedWarrantyIds;
    }

    /**
     * Assigns the warranties of a freshly created sale: an automatic basic
     * warranty for every console and an extended warranty for the consoles
     * that requested it. The additional cost of the extended warranties is
     * added to the total of the sale.
     *
     * @param sale               the sale that was just registered
     * @param extendedWarrantyIds the identifiers of the consoles that must
     *                           receive an extended warranty
     */
    private void assignWarranties(Sale sale, Set<String> extendedWarrantyIds) {
        double extraCost = 0.0;
        for (Product product : sale.getProducts()) {
            if (product instanceof Console) {
                warrantyService.assignBasicWarranty(product, sale, sale.getDate());
                if (extendedWarrantyIds.contains(product.getId())) {
                    extraCost += warrantyService.assignExtendedWarranty(product, sale, sale.getDate())
                            .getAdditionalCost();
                }
            }
        }
        if (!extendedWarrantyIds.isEmpty()) {
            sale.setWarrantyExtraCost(extraCost);
            save();
        }
    }

    /**
     * Resolves a sale item either from the product inventory or from the
     * accessory inventory, so a single sale can include video games, consoles
     * and accessories.
     *
     * @param itemIdthe identifier of the item to resolve
     * @return the matching item, or {@code null} when no product or accessory
     *         matches the identifier
     */
    private Product resolveItem(String itemId) {
        Product item = productService.findById(itemId);
        if (item == null) {
            item = accessoryService.findById(itemId);
        }
        return item;
    }

    /**
     * Discounts one unit of stock from a sold item, delegating to the
     * inventory service that owns the item (products or accessories).
     *
     * @param item the sold item whose stock must be decreased
     */
    private void discountStock(Product item) {
        int newStock = item.getStock() - 1;
        if (item instanceof Accessory) {
            accessoryService.updateStock(item.getId(), newStock);
        } else {
            productService.updateStock(item.getId(), newStock);
        }
    }

    /**
     * @return an unmodifiable view of the complete sales history
     */
    public List<Sale> viewAllSales() {
        return Collections.unmodifiableList(sales);
    }

    /**
     * Returns the sales registered for a specific customer.
     *
     * @param customerId the identifier of the customer to look for
     * @return the sales of the customer, or an empty list when no sale
     *         matches
     */
    public List<Sale> viewSalesByCustomer(String customerId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getCustomer().getId().equals(customerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Returns the sales attended by a specific seller.
     *
     * @param sellerId the identifier of the seller to look for
     * @return the sales of the seller, or an empty list when no sale matches
     */
    public List<Sale> viewSalesBySeller(String sellerId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getSeller().getId().equals(sellerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    /**
     * Persists the current state of the sales registry into the data file.
     *
     * <p>This method is invoked automatically after every operation that
     * changes the in-memory data, guaranteeing that the information is
     * conserved between executions.</p>
     */
    public void save() {
        saleRepository.saveAll(sales);
    }

    /**
     * Generates the next sale identifier following the V-N sequence, taking
     * the largest numeric suffix among the stored sales as reference.
     *
     * @return the next available sale identifier
     */
    private String nextSaleId() {
        int max = 0;
        for (Sale sale : sales) {
            String id = sale.getId();
            if (id.startsWith("V-")) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(2)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "V-" + (max + 1);
    }

    public Sale findById(String saleId) {
        for (Sale sale : sales) {
            if (sale.getId().equals(saleId)) {
                return sale;
            }
        }
        return null;
    }
}