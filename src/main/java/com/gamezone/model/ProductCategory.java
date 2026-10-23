package com.gamezone.model;

/**
 * Lists the categories of the products sold by GameZone Unicesar.
 *
 * <p>It is the single place where the categories are defined: each product
 * type returns its category from this enum through
 * {@link Product#getCategory()}, and the promotion module validates the
 * target category of a category promotion against it. The name of each
 * constant is the text stored in the data files, so adding a category only
 * requires adding a constant here and the product type that uses it.</p>
 */
public enum ProductCategory {

    /** Video games. */
    VIDEOGAME,

    /** Consoles. */
    CONSOLE,

    /** Accessories: controllers, cables and memories. */
    ACCESSORY;

    /**
     * Tells whether a text is the name of one of the categories. The
     * comparison is exact, so the caller normalizes the text first.
     *
     * @param name the text to check
     * @return {@code true} when the text is the name of a category
     */
    public static boolean isValid(String name) {
        for (ProductCategory category : values()) {
            if (category.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
