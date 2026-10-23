package com.gamezone.service;

import com.gamezone.model.Product;

/**
 * Mediates between the use cases that sell or return items and the two
 * inventories of the store: the products ({@link ProductService}) and the
 * accessories ({@link AccessoryService}).
 *
 * <p>The sales and the returns only know this class. It finds an item in the
 * product inventory first and in the accessory inventory afterwards, and it
 * changes the stock in the inventory that owns the item, so no use case needs
 * to check the type of an item or to know which of the two services holds
 * it.</p>
 */
public class InventoryService {

    private final ProductService productService;
    private final AccessoryService accessoryService;

    /**
     * Creates the inventory service.
     *
     * @param productService   the service of the product inventory
     * @param accessoryService the service of the accessory inventory
     */
    public InventoryService(ProductService productService, AccessoryService accessoryService) {
        this.productService = productService;
        this.accessoryService = accessoryService;
    }

    /**
     * Finds an item either in the product inventory or in the accessory
     * inventory, so a single sale or return can include video games, consoles
     * and accessories.
     *
     * @param itemId the identifier of the item to find
     * @return the matching item, or {@code null} when no product or accessory
     *         matches the identifier
     */
    public Product findItem(String itemId) {
        Product item = productService.findById(itemId);
        if (item == null) {
            item = accessoryService.findById(itemId);
        }
        return item;
    }

    /**
     * Discounts units of stock from an item, delegating to the inventory that
     * owns it.
     *
     * @param itemId   the identifier of the item whose stock decreases
     * @param quantity the number of units to discount
     */
    public void decreaseStock(String itemId, int quantity) {
        if (productService.findById(itemId) != null) {
            productService.decreaseStock(itemId, quantity);
        } else {
            accessoryService.decreaseStock(itemId, quantity);
        }
    }

    /**
     * Puts units of stock back into an item, delegating to the inventory that
     * owns it.
     *
     * @param itemId   the identifier of the item whose stock is restored
     * @param quantity the number of units to add back
     */
    public void restoreStock(String itemId, int quantity) {
        if (productService.findById(itemId) != null) {
            productService.restoreStock(itemId, quantity);
        } else {
            accessoryService.restoreStock(itemId, quantity);
        }
    }
}
