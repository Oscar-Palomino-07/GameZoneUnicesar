package com.gamezone.exceptions;

/**
 * Exception thrown when a requested resource (product, customer, seller or
 * sale) cannot be found by its identifier in the system.
 */
public class ResourceNotFoundException extends GameZoneException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    private final String resourceType;
    private final String resourceId;

    /**
     * Creates a new resource-not-found exception for the given resource type
     * and identifier, building a descriptive Spanish message for the user.
     *
     * @param resourceType the type of resource searched (e.g. "producto", "cliente")
     * @param resourceId   the identifier that was not found
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super("El " + resourceType + " con identificador " + resourceId + " no fue encontrado",
                DEFAULT_ERROR_CODE);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * Creates a new resource-not-found exception with a custom message.
     *
     * @param message the detail message describing the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message, DEFAULT_ERROR_CODE);
        this.resourceType = null;
        this.resourceId = null;
    }

    /**
     * Returns the type of resource that could not be found.
     *
     * @return the resource type, or {@code null} if created with a raw message
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns the identifier of the resource that could not be found.
     *
     * @return the resource identifier, or {@code null} if created with a raw message
     */
    public String getResourceId() {
        return resourceId;
    }
}
