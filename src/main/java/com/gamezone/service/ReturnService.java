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
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("A return must include at least one product.");
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

        List<Product> productsToReturn = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String prodId = entry.getKey();
            int qtyToReturn = entry.getValue();

            // Validar que el producto existe en el sistema
            Product systemProduct = productService.findById(prodId);
            if (systemProduct == null) {
                throw new IllegalArgumentException("Product not found in system: " + prodId);
            }

            // Validar que el producto pertenecía a la venta y verificar cantidad comprada
            long qtyBought = ventaOriginal.getProducts().stream()
                    .filter(p -> p.getId().equals(prodId))
                    .count();

            if (qtyBought < qtyToReturn) {
                throw new IllegalArgumentException("Cannot return more items than purchased for product: " + prodId);
            }

            // Añadir a la lista de devolución e incrementar el stock en tienda
            for (int i = 0; i < qtyToReturn; i++) {
                productsToReturn.add(systemProduct);
            }
            productService.restoreStock(prodId, qtyToReturn);
        }

        // 4. Crear la devolución (Cambiamos el nombre de la variable 'return' por
        // 'returnObj')
        Return returnObj = new Return(nextReturnId(), customer, seller, productsToReturn);
        returns.add(returnObj);
        save(); // Recuerda tener este método implementado para persistir en tu repositorio

        return returnObj;
    }

    public List<Return> viewAllReturns() {
        return Collections.unmodifiableList(returns);
    }

    public List<Return> viewReturnsByCustomer(String customerId) {
            List<Return> result = new ArrayList<>();
            for (Return re : returns) {
                if (re.getCustomer().getId().equals(customerId)) {
                    result.add(re);
                }
            }
            return result;
        }

    public List<Return> viewReturnsBySeller(String sellerId) {
        List<Return> result = new ArrayList<>();
        for (Return re : returns) {
            if (re.getSeller().getId().equals(sellerId)) {
                result.add(re);
            }
        }
        return result;
    }
    
    public double generateMonthlyBalance(int month, int year) {
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

            return totalSales - totalReturns;
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
