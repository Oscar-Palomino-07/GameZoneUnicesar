package com.gamezone.service;

import com.gamezone.model.*;
import com.gamezone.persistence.ReturnRepository;

import java.time.LocalDate;
import java.util.*;

public class ReturnService {

    private final SaleService saleService;
    private final PersonService personService;
    private final ProductService productService;
    private final AccessoryService accessoryService;
    private final ReturnRepository returnRepository;
    private final List<Return> returns;

    public ReturnService(ReturnRepository returnRepository, SaleService saleService, PersonService personService,
            ProductService productService, AccessoryService accessoryService) {
        this.saleService = saleService;
        this.returnRepository = returnRepository;
        this.personService = personService;
        this.productService = productService;
        this.accessoryService = accessoryService;
        this.returns = new ArrayList<>(returnRepository.loadAll());
    }

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

            long qtyBought = originalSale.getProducts().stream()
                    .filter(p -> p.getId().equals(productId))
                    .count();

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

        Return returnObj = new Return(nextReturnId(), saleId, customer, seller, productsToReturn);
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

    public List<Return> viewAllReturns() {
        return Collections.unmodifiableList(returns);
    }

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
     * Calculates the total sold in the given month. Each sale contributes its
     * final total, that is, the subtotal minus the discount of the applied
     * promotion plus the cost of the extended warranties.
     *
     * @param month the month to report, from 1 to 12
     * @param year  the year to report, 2000 or later
     * @return the sum of the final totals of the sales registered in the month
     * @throws IllegalArgumentException when the month or the year is invalid
     */
    public double calculateMonthlySales(int month, int year) {
        validatePeriod(month, year);
        double totalSales = 0.0;
        for (Sale sale : saleService.viewAllSales()) {
            if (isInPeriod(sale.getDate(), month, year)) {
                totalSales += sale.calculateTotal();
            }
        }
        return totalSales;
    }

    public double generateMonthlyBalance(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Mes inválido: " + month + ". Debe estar entre 1 y 12.");
        }
        if (year < 2000) {
            throw new IllegalArgumentException(
                    "Año inválido: " + year + ". Debe ser 2000 o posterior.");
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

    /**
     * Checks that the month and the year of a report are valid.
     *
     * @param month the month to check, from 1 to 12
     * @param year  the year to check, 2000 or later
     * @throws IllegalArgumentException when the month or the year is invalid
     */
    private void validatePeriod(int month, int year) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mes inválido: " + month + ". Debe estar entre 1 y 12.");
        }
        if (year < 2000) {
            throw new IllegalArgumentException("Año inválido: " + year + ". Debe ser 2000 o posterior.");
        }
    }

    /**
     * Tells whether a date belongs to the given month and year.
     *
     * @param date  the date to check
     * @param month the month of the period
     * @param year  the year of the period
     * @return {@code true} when the date falls inside the period
     */
    private boolean isInPeriod(LocalDate date, int month, int year) {
        return date != null && date.getMonthValue() == month && date.getYear() == year;
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
