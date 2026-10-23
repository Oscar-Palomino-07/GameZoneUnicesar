package com.gamezone.exceptions;

/**
 * Exception thrown when an input value does not satisfy format, range or type
 * constraints, such as blank mandatory strings, negative prices or negative
 * quantities.
 */
public class InvalidDataException extends GameZoneException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_DATA";

    private final String fieldName;
    private final String problemDescription;

    /**
     * Creates a new invalid-data exception for the specified field and problem
     * description, building a descriptive Spanish message for the user.
     *
     * @param fieldName          the name of the invalid field
     * @param problemDescription the description of why the value is invalid
     */
    public InvalidDataException(String fieldName, String problemDescription) {
        super("El campo '" + fieldName + "' no es válido: " + problemDescription,
                DEFAULT_ERROR_CODE);
        this.fieldName = fieldName;
        this.problemDescription = problemDescription;
    }

    /**
     * Creates a new invalid-data exception with a custom detail message.
     *
     * @param message the detail message describing the invalid input
     */
    public InvalidDataException(String message) {
        super(message, DEFAULT_ERROR_CODE);
        this.fieldName = null;
        this.problemDescription = null;
    }

    /**
     * Returns the name of the field that failed validation.
     *
     * @return the invalid field name, or {@code null} if created with a raw message
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the description of the validation problem.
     *
     * @return the problem description, or {@code null} if created with a raw message
     */
    public String getProblemDescription() {
        return problemDescription;
    }
}
