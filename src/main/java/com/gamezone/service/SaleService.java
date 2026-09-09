package com.gamezone.service;

import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;
import com.gamezone.persistence.SaleRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exposes the business operations related to the sales of the store.
 *
 * <p>It applies the rules that govern the registration of a sale: the sale
 * must contain at least one product, the customer, the seller and every
 * product must be registered, and the available stock must cover the quantity
 * requested for each product. When a sale is accepted, the stock of the
 * purchased products is discounted through the {@link ProductService} and the
 * sale is persisted immediately.</p>
 *
 * <p>This class belongs to the service layer and is the only one allowed to
 * invoke the sale repository.</p>
 */
public class SaleService {

    private final SaleRepository saleRepository;
    private final PersonService personService;
    private final ProductService productService;
    private final List<Sale> sales;

    /**
     * Creates the sale service, loading the stored sales from the repository
     * into memory.
     *
     * @param saleRepository the repository used to persist sales
     * @param personService  the service used to locate customers and sellers
     * @param productService the service used to locate products and update
     *                       their stock
     */
    public SaleService(SaleRepository saleRepository, PersonService personService, ProductService productService) {
        this.saleRepository = saleRepository;
        this.personService = personService;
        this.productService = productService;
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
            Product product = productService.findById(entry.getKey());
            if (product == null) {
                throw new IllegalArgumentException("Product not found: " + entry.getKey());
            }
            if (product.getStock() < entry.getValue()) {
                throw new IllegalArgumentException("Not enough stock for product: " + entry.getKey());
            }
        }

        List<Product> products = new ArrayList<>();
        for (String productId : productIds) {
            Product product = productService.findById(productId);
            products.add(product);
            productService.updateStock(product.getId(), product.getStock() - 1);
        }

        Sale sale = new Sale(nextSaleId(), customer, seller, products);
        sales.add(sale);
        save();
        return sale;
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