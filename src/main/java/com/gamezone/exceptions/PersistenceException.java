package com.gamezone.exceptions;

/**
 * Exception thrown when an error occurs while reading from or writing to a
 * persistence file, preserving the original underlying cause for traceability.
 */
public class PersistenceException extends GameZoneException {

    private static final String DEFAULT_ERROR_CODE = "PERSISTENCE_ERROR";

    /**
     * Creates a new persistence exception with a descriptive message and the
     * original underlying cause.
     *
     * @param message the detail message describing the file and operation that failed
     * @param cause   the original exception that caused the persistence failure
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, cause);
    }

    /**
     * Creates a new persistence exception specifying the file name, the
     * operation being performed and the original underlying cause.
     *
     * @param fileName  the name or path of the file involved in the error
     * @param operation the operation being performed (e.g. "lectura", "escritura")
     * @param cause     the original exception that caused the persistence failure
     */
    public PersistenceException(String fileName, String operation, Throwable cause) {
        super("Error de " + operation + " en el archivo '" + fileName + "': "
                + (cause != null ? cause.getMessage() : "causa desconocida"),
                DEFAULT_ERROR_CODE, cause);
    }

    /**
     * Creates a new persistence exception with a descriptive message.
     *
     * @param message the detail message describing the persistence failure
     */
    public PersistenceException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }
}
