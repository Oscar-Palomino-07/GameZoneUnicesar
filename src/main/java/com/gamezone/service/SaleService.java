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

public class SaleService {

    private final SaleRepository saleRepository;
    private final PersonService personService;
    private final ProductService productService;
    private final List<Sale> sales;

    public SaleService(SaleRepository saleRepository, PersonService personService, ProductService productService) {
        this.saleRepository = saleRepository;
        this.personService = personService;
        this.productService = productService;
        this.sales = new ArrayList<>(saleRepository.loadAll());
    }

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

    public List<Sale> viewAllSales() {
        return Collections.unmodifiableList(sales);
    }

    public List<Sale> viewSalesByCustomer(String customerId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getCustomer().getId().equals(customerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    public List<Sale> viewSalesBySeller(String sellerId) {
        List<Sale> result = new ArrayList<>();
        for (Sale sale : sales) {
            if (sale.getSeller().getId().equals(sellerId)) {
                result.add(sale);
            }
        }
        return result;
    }

    public void save() {
        saleRepository.saveAll(sales);
    }

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
}