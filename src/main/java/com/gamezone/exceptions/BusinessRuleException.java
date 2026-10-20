package com.gamezone.exceptions;

/**
 * Exception thrown when an operation violates a domain business rule, such as
 * insufficient stock, duplicate entity registration or an empty sale.
 */
public class BusinessRuleException extends GameZoneException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    /**
     * Creates a new business-rule exception with a message describing the
     * violated rule.
     *
     * @param message the detail message describing the business rule violation
     */
    public BusinessRuleException(String message) {
        super(message, DEFAULT_ERROR_CODE);
    }

    /**
     * Creates a new business-rule exception with a custom message and error
     * code.
     *
     * @param message   the detail message describing the business rule violation
     * @param errorCode the specific error code for the violated rule
     */
    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode);
    }
}
