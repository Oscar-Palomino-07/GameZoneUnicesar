package com.gamezone.ui;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Reads and parses what the user types in the console.
 *
 * <p>Every menu of the application reads through the same instance, so there is
 * a single {@link Scanner} over the standard input and no typed line is lost
 * between menus.</p>
 */
public class ConsoleInput {

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Shows a prompt and reads a line of text.
     *
     * @param prompt the text shown to the user
     * @return the typed line without surrounding spaces, or an empty text when
     *         the input has ended
     */
    public String readText(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /**
     * Shows a prompt and reads an integer, asking again until the user types a
     * valid one.
     *
     * @param prompt the text shown to the user
     * @return the typed integer, or zero when the input has ended
     */
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            } catch (NoSuchElementException e) {
                return 0;
            }
        }
    }

    /**
     * Shows a prompt and reads a decimal number, asking again until the user
     * types a valid one.
     *
     * @param prompt the text shown to the user
     * @return the typed number, or zero when the input has ended
     */
    public double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            } catch (NoSuchElementException e) {
                return 0.0;
            }
        }
    }

    /**
     * Reads a date typed by the user in ISO format.
     *
     * @param prompt the text shown to the user
     * @return the parsed date, or {@code null} when the text is not a valid
     *         date (after telling the user so)
     */
    public LocalDate readDate(String prompt) {
        String text = readText(prompt);
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            System.out.println("Fecha inválida: use el formato AAAA-MM-DD.");
            return null;
        }
    }

    /**
     * Splits a comma separated text into identifiers, ignoring the empty ones.
     *
     * @param input the text typed by the user
     * @return the identifiers, trimmed and in the order in which they appear
     */
    public List<String> parseIds(String input) {
        List<String> ids = new ArrayList<>();
        for (String item : input.split(",")) {
            if (!item.trim().isEmpty()) {
                ids.add(item.trim());
            }
        }
        return ids;
    }
}
