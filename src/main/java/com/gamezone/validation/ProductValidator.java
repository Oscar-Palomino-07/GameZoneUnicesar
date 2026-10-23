package com.gamezone.validation;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.ResourceNotFoundException;
import com.gamezone.model.Product;

import java.util.List;

/**
 * Provides static validation methods for the product module.
 *
 * <p>This class belongs to the validation layer and verifies input data,
 * stock availability and product existence without accessing persistence
 * or external services.</p>
 */
public final class ProductValidator {

    private ProductValidator() {
    }

    /**
     * Validates the core attributes required to register a product.
     *
     * @param id    the product identifier; must not be null or blank
     * @param title the product title; must not be null or blank
     * @param price the unit price; must be strictly positive
     * @param stock the initial stock quantity; must not be negative
     * @throws InvalidDataException when any attribute fails format or range validation
     */
    public static void validateProductData(String id, String title, double price, int stock) {
        if (id == null || id.isBlank()) {
            throw new InvalidDataException("id", "el identificador no puede estar vacío");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidDataException("title", "el título no puede estar vacío");
        }
        if (price <= 0) {
            throw new InvalidDataException("price", "el precio debe ser positivo");
        }
        if (stock < 0) {
            throw new InvalidDataException("stock", "el stock no puede ser negativo");
        }
    }

    /**
     * Validates that the given product has enough stock to cover the requested
     * quantity.
     *
     * @param product           the product whose stock is checked
     * @param requestedQuantity the number of units requested
     * @throws ResourceNotFoundException when the product reference is null
     * @throws InvalidDataException      when the requested quantity is not positive
     * @throws BusinessRuleException     when the product stock is less than the requested quantity
     */
    public static void validateStockAvailability(Product product, int requestedQuantity) {
        if (product == null) {
            throw new ResourceNotFoundException("producto", "desconocido");
        }
        if (requestedQuantity <= 0) {
            throw new InvalidDataException("cantidad", "la cantidad solicitada debe ser positiva");
        }
        if (product.getStock() < requestedQuantity) {
            throw new BusinessRuleException("Stock insuficiente para el producto '"
                    + product.getTitle() + "' (ID: " + product.getId()
                    + "). Stock disponible: " + product.getStock()
                    + ", solicitado: " + requestedQuantity);
        }
    }

    /**
     * Validates that a searched product exists (is not {@code null}).
     *
     * @param product   the product reference obtained from a search
     * @param productId the identifier that was searched for
     * @throws ResourceNotFoundException when {@code product} is {@code null}
     */
    public static void validateProductExists(Product product, String productId) {
        if (product == null) {
            throw new ResourceNotFoundException("producto", productId);
        }
    }

    /**
     * Validates that no existing product already uses the given identifier.
     *
     * @param existing the list of currently registered products
     * @param newId    the candidate identifier for the new product
     * @throws BusinessRuleException when a product with {@code newId} already exists
     */
    public static void validateUniqueProduct(List<? extends Product> existing, String newId) {
        if (existing == null || newId == null) {
            return;
        }
        for (Product product : existing) {
            if (product != null && newId.equals(product.getId())) {
                throw new BusinessRuleException(
                        "Ya existe un producto registrado con el identificador " + newId);
            }
        }
    }

    /**
     * Validates that a required text field is neither null nor blank.
     *
     * @param fieldName the name of the field being checked
     * @param value     the string value to validate
     * @throws InvalidDataException when {@code value} is null or blank
     */
    public static void validateRequiredField(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidDataException(fieldName, "el campo es obligatorio y no puede estar vacío");
        }
    }

    /**
     * Validates that a stock quantity is not negative.
     *
     * @param quantity the stock quantity to check
     * @throws InvalidDataException when {@code quantity} is negative
     */
    public static void validateNonNegativeStock(int quantity) {
        if (quantity < 0) {
            throw new InvalidDataException("stock", "la cantidad de stock no puede ser negativa");
        }
    }
}
