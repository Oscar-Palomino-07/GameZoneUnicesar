package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Customer;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.gamezone.model.Seller;

import java.util.List;

/**
 * Provides static validation methods for the sales module.
 *
 * <p>This class belongs to the validation layer and verifies that a sale has
 * valid participants, at least one product and that searched sales exist,
 * without accessing persistence or external services.</p>
 */
public final class SaleValidator {

    private SaleValidator() {
    }

    /**
     * Validates that the customer and seller are not {@code null} and that the
     * product list is neither {@code null} nor empty.
     *
     * @param customer the purchasing customer; must not be {@code null}
     * @param seller   the attending seller; must not be {@code null}
     * @param products the list of products included in the sale; must not be empty
     * @throws InvalidDataException  when {@code customer} or {@code seller} is {@code null}
     * @throws BusinessRuleException when {@code products} is {@code null} or empty
     */
    public static void validateSaleData(Customer customer, Seller seller, List<Product> products) {
        if (customer == null) {
            throw new InvalidDataException("customer", "el cliente de la venta no puede ser nulo");
        }
        if (seller == null) {
            throw new InvalidDataException("seller", "el vendedor de la venta no puede ser nulo");
        }
        if (products == null || products.isEmpty()) {
            throw new BusinessRuleException("La venta debe incluir al menos un producto.");
        }
    }

    /**
     * Validates that a searched sale exists (is not {@code null}).
     *
     * @param sale   the sale reference obtained from a search
     * @param saleId the identifier that was searched for
     * @throws ResourceNotFoundException when {@code sale} is {@code null}
     */
    public static void validateSaleExists(Sale sale, String saleId) {
        if (sale == null) {
            throw new ResourceNotFoundException("venta", saleId);
        }
    }

    /**
     * Validates that the list of product identifiers requested for a sale is
     * neither {@code null} nor empty before resolving the items.
     *
     * @param productIds the list of product identifiers requested for the sale
     * @throws BusinessRuleException when {@code productIds} is {@code null} or empty
     */
    public static void validateProductIdsNotEmpty(List<String> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessRuleException("La venta debe incluir al menos un producto.");
        }
    }
}
