package com.gamezone.ui;

import com.gamezone.exceptions.BusinessRuleException;
import com.gamezone.exceptions.InvalidDataException;
import com.gamezone.exceptions.PersistenceException;
import com.gamezone.exceptions.ResourceNotFoundException;

/**
 * Turns the errors raised by a menu action into messages for the user.
 *
 * <p>Every menu runs its actions through the same reporter, so each category of
 * the exception hierarchy has one message that is written in a single
 * place.</p>
 */
public class ConsoleErrorReporter {

    /**
     * Runs a menu action and turns every error it can raise into a message for
     * the user, one message per category of error.
     *
     * <p>The catch blocks go from the most specific exception to the most
     * general one, so each category of the exception hierarchy gets its own
     * message and the user never sees a technical message or a stack trace.
     * The last block keeps the original behavior for the modules that still
     * report their errors with the standard Java exceptions.</p>
     *
     * @param action the menu action to run
     */
    public void runGuarded(Runnable action) {
        try {
            action.run();
        } catch (ResourceNotFoundException e) {
            System.out.println("No se encontró el recurso solicitado: " + e.getMessage());
        } catch (BusinessRuleException e) {
            System.out.println("No se puede completar la operación: " + e.getMessage());
        } catch (InvalidDataException e) {
            System.out.println("Los datos ingresados no son válidos: " + e.getMessage());
        } catch (PersistenceException e) {
            System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
