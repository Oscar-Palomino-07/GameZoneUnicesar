package com.gamezone.service;

import com.gamezone.model.*;
import com.gamezone.persistence.ReturnRepository;

import java.util.*;

public class ReturnService {

    private final SaleService saleService;
    private final PersonService personService;
    private final ProductService productService;
    private final ReturnRepository returnRepository;
    private final List<Return> returns;

    public ReturnService(ReturnRepository returnRepository, SaleService saleService, PersonService personService,
            ProductService productService) {
        this.saleService = saleService;
        this.returnRepository = returnRepository;
        this.personService = personService;
        this.productService = productService;
        this.returns = new ArrayList<>(returnRepository.loadAll());
    }

    public Return registerReturn(String saleId, List<String> productIds) {
        if (saleId == null || saleId.isBlank()) {
            throw new IllegalArgumentException("Sale ID must not be null or empty.");
        }
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("A return must include at least one product.");
        }
        for (String pid : productIds) {
            if (pid == null || pid.isBlank()) {
                throw new IllegalArgumentException("Product ID list must not contain null or blank entries.");
            }
        }

        Sale ventaOriginal = saleService.findById(saleId);
        if (ventaOriginal == null) {
            throw new IllegalArgumentException("Original sale not found: " + saleId);
        }
        if (!ventaOriginal.canBeReturned()) {
            throw new IllegalArgumentException(
                    "Return period expired. Returns are only accepted within 30 days of the sale.");
        }

        Customer customer = ventaOriginal.getCustomer();
        Seller seller = ventaOriginal.getSeller();

        Map<String, Integer> quantities = new HashMap<>();
        for (String productId : productIds) {
            quantities.merge(productId, 1, Integer::sum);
        }

        Map<String, Integer> alreadyReturned = countAlreadyReturnedByProduct(ventaOriginal);

        List<Product> productsToReturn = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String prodId = entry.getKey();
            int qtyToReturn = entry.getValue();

            Product systemProduct = productService.findById(prodId);
            if (systemProduct == null) {
                throw new IllegalArgumentException("Product not found in system: " + prodId);
            }

            long qtyBought = ventaOriginal.getProducts().stream()
                    .filter(p -> p.getId().equals(prodId))
                    .count();

            if (qtyBought == 0) {
                throw new IllegalArgumentException(
                        "Product '" + prodId + "' was not part of the original sale: " + saleId);
            }
            if (qtyToReturn > qtyBought) {
                throw new IllegalArgumentException(
                        "Cannot return more items than purchased for product: " + prodId
                        + " (purchased=" + qtyBought + ", requested=" + qtyToReturn + ").");
            }

            int previouslyReturned = alreadyReturned.getOrDefault(prodId, 0);
            if (previouslyReturned + qtyToReturn > qtyBought) {
                throw new IllegalStateException(
                        "Double-return detected for product '" + prodId + "' in sale '" + saleId + "': "
                        + previouslyReturned + " unit(s) already returned, "
                        + qtyBought + " purchased, but " + qtyToReturn + " more requested.");
            }

            for (int i = 0; i < qtyToReturn; i++) {
                productsToReturn.add(systemProduct);
            }
            productService.restoreStock(prodId, qtyToReturn);
        }

        Return returnObj = new Return(nextReturnId(), customer, seller, productsToReturn);
        returns.add(returnObj);
        save();

        return returnObj;
    }

    private Map<String, Integer> countAlreadyReturnedByProduct(Sale sale) {
        Set<String> saleProductIds = new HashSet<>();
        for (Product p : sale.getProducts()) {
            saleProductIds.add(p.getId());
        }

        Map<String, Integer> countMap = new HashMap<>();

        for (Return ret : returns) {
            if (!ret.getCustomer().getId().equals(sale.getCustomer().getId())) continue;
            if (!ret.getSeller().getId().equals(sale.getSeller().getId())) continue;

            for (Product p : ret.getProducts()) {
                if (saleProductIds.contains(p.getId())) {
                    countMap.merge(p.getId(), 1, Integer::sum);
                }
            }
        }
        return countMap;
    }

    public List<Return> viewAllReturns() {
        return Collections.unmodifiableList(returns);
    }

    public List<Return> viewReturnsByCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID must not be null or empty.");
        }
        List<Return> result = new ArrayList<>();
        for (Return re : returns) {
            if (re.getCustomer().getId().equals(customerId)) {
                result.add(re);
            }
        }
        return result;
    }

    public List<Return> viewReturnsBySeller(String sellerId) {
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Seller ID must not be null or empty.");
        }
        List<Return> result = new ArrayList<>();
        for (Return re : returns) {
            if (re.getSeller().getId().equals(sellerId)) {
                result.add(re);
            }
        }
        return result;
    }

    public double generateMonthlyBalance(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Invalid month: " + month + ". Must be between 1 and 12.");
        }
        if (year < 2000) {
            throw new IllegalArgumentException(
                    "Invalid year: " + year + ". Must be 2000 or later.");
        }

        double totalSales = 0;
        double totalReturns = 0;

        for (Sale sale : saleService.viewAllSales()) {
            if (sale.getDate().getMonthValue() == month && sale.getDate().getYear() == year) {
                totalSales += sale.calculateTotal();
            }
        }

        for (Return returnObj : returns) {
            if (returnObj.getDate().getMonthValue() == month && returnObj.getDate().getYear() == year) {
                totalReturns += returnObj.calculateRefundAmount();
            }
        }

        double balance = totalSales - totalReturns;

        if (balance < 0) {
            throw new IllegalStateException(
                    String.format(
                            "Negative balance detected for %02d/%d: sales=%.2f, returns=%.2f. "
                            + "Total refunds exceed total sales, which suggests inconsistent return data.",
                            month, year, totalSales, totalReturns));
        }

        return balance;
    }

    public void save() {
        returnRepository.saveAll(returns);
    }

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
