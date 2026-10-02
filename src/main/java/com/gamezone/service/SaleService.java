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
    private final PromotionService promotionService;
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
     * @param promotionService the service used to find the best promotion
     *                        for a sale
     */
    public SaleService(SaleRepository saleRepository, PersonService personService, ProductService productService,
            AccessoryService accessoryService, WarrantyService warrantyService,
            PromotionService promotionService) {
        this.saleRepository = saleRepository;
        this.personService = personService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.warrantyService = warrantyService;
        this.promotionService = promotionService;
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
     * Registers a new sale for the given customer, seller and items, applying
     * the best promotion and managing the warranties of the consoles it
     * includes.
     *
     * <p>The operations always run in this order, because the order changes
     * the result (for example, the discount is calculated before the
     * warranties are added so it never applies to their cost):</p>
     * <ol>
     *   <li>Validate that the sale has at least one item.</li>
     *   <li>Resolve every item as a product or an accessory and validate its
     *       stock.</li>
     *   <li>Create the sale and calculate its subtotal.</li>
     *   <li>Find the best active promotion and register the discount,
     *       calculated only over the subtotal of the items.</li>
     *   <li>Generate the basic warranty of every console and the requested
     *       extended warranties, adding their cost.</li>
     *   <li>Calculate the final total: subtotal - discount + cost of the
     *       extended warranties.</li>
     *   <li>Update the inventory through {@link ProductService} or
     *       {@link AccessoryService}, according to the type of each item.</li>
     *   <li>Persist the sale.</li>
     * </ol>
     *
     * <p>Nothing is modified in the inventory or in the sales registry until
     * every previous step has succeeded, so a rejected sale leaves no trace.
     * Every console receives an automatic basic warranty at no cost; when the
     * identifier of a console is also listed in
     * {@code productIdsWithExtendedWarranty}, an extended warranty is assigned
     * to each unit of that console.</p>
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

        // Step 2: resolve each item as a product or an accessory and validate
        // its stock. Nothing is modified until every validation has passed.
        List<Product> products = resolveAndValidateItems(productIds);
        Set<String> extendedWarrantyIds = validateExtendedWarrantyRequest(productIds, productIdsWithExtendedWarranty);

        // Step 3: create the sale; its subtotal is the sum of the item prices.
        Sale sale = new Sale(nextSaleId(), customer, seller, products);

        // Step 4: best active promotion, with the discount calculated only
        // over the subtotal of the items.
        applyBestPromotion(sale);

        // Steps 5 and 6: warranties and final total (subtotal - discount +
        // cost of the extended warranties).
        sale.setWarrantyExtraCost(assignWarranties(sale, extendedWarrantyIds));

        // Step 7: update the inventory through the service that owns each item.
        for (Product item : products) {
            discountStock(item);
        }

        // Step 8: persist the sale. The sale only becomes visible once every
        // previous step has succeeded.
        sales.add(sale);
        save();
        return sale;
    }

    /**
     * Resolves every requested item as a product or an accessory and checks
     * that the available stock covers the units requested for each one. It
     * only reads: nothing is modified, so a rejected request leaves the
     * inventory untouched.
     *
     * @param productIds the identifiers of the requested items, repeated once
     *                   per unit
     * @return the resolved items, one entry per requested unit and in the
     *         same order as the identifiers
     * @throws IllegalArgumentException when an item cannot be found or its
     *         stock cannot cover the requested units
     */
    private List<Product> resolveAndValidateItems(List<String> productIds) {
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
        List<Product> items = new ArrayList<>();
        for (String productId : productIds) {
            items.add(resolveItem(productId));
        }
        return items;
    }

    /**
     * Looks for the active promotion that grants the largest discount to the
     * sale and, when there is one, records its name and discount amount in the
     * sale. The discount is calculated only over the prices of the items, so
     * it runs before the cost of the extended warranties is added. Only one
     * promotion is applied per sale.
     *
     * @param sale the sale that was just created
     */
    private void applyBestPromotion(Sale sale) {
        promotionService.bestPromotionFor(sale.getProducts()).ifPresent(promotion -> {
            sale.setAppliedPromotionName(promotion.getName());
            sale.setDiscountAmount(promotion.calculateDiscount(sale.getProducts()));
        });
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
     * Assigns the warranties of the sale being registered: an automatic basic
     * warranty for every console and an extended warranty for the consoles
     * that requested it. It returns the additional cost of the extended
     * warranties so the caller can include it in the total of the sale.
     *
     * @param sale               the sale being registered
     * @param extendedWarrantyIds the identifiers of the consoles that must
     *                           receive an extended warranty
     * @return the total cost of the extended warranties, zero when none was
     *         requested
     */
    private double assignWarranties(Sale sale, Set<String> extendedWarrantyIds) {
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
        return extraCost;
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