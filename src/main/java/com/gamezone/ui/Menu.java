package com.gamezone.ui;

/**
 * A submenu of the console interface, one for each module of the application.
 *
 * <p>The main menu only knows this interface, so adding a module means adding
 * a new implementation to the list of menus and nothing else.</p>
 */
public interface Menu {

    /**
     * Returns the text that the main menu shows for this module.
     *
     * @return the label of the module, without the option number
     */
    String getLabel();

    /**
     * Runs the submenu until the user chooses to go back.
     */
    void run();
}
