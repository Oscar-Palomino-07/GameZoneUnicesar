package com.gamezone.exceptions;

/**
 * Abstract root exception for all domain, validation and persistence errors
 * in the GameZone Unicesar system.
 *
 * <p>Every custom exception in the application extends this class directly or
 * indirectly, providing a unified unchecked hierarchy and an associated error
 * code to classify the failure.</p>
 */
public abstract class GameZoneException extends RuntimeException {

    private final String errorCode;

    /**
     * Creates a new exception with the specified detail message and a default
     * error code.
     *
     * @param message the detail message describing the error
     */
    public GameZoneException(String message) {
        this(message, "GAMEZONE_ERROR");
    }

    /**
     * Creates a new exception with the specified detail message and error code.
     *
     * @param message   the detail message describing the error
     * @param errorCode the code identifying the category of the error
     */
    public GameZoneException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Creates a new exception with the specified detail message, error code
     * and underlying cause.
     *
     * @param message   the detail message describing the error
     * @param errorCode the code identifying the category of the error
     * @param cause     the underlying cause of this exception
     */
    public GameZoneException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code associated with this exception.
     *
     * @return the error code string
     */
    public String getErrorCode() {
        return errorCode;
    }
}
