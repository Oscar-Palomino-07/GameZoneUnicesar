package com.gamezone.ui;

import java.util.List;

/**
 * Console-based user interface of GameZone Unicesar.
 *
 * <p>Shows the main menu and gives access to one submenu for each module of the
 * application. The list of submenus is given when the menu is created, so
 * adding a module to the application does not change this class. Every
 * submenu delegates to the services of its module, so the user interface never
 * accesses the persistence layer directly.</p>
 */
public class ConsoleMenu {

    private final ConsoleInput input;
    private final List<Menu> menus;

    /**
     * Creates the main menu.
     *
     * @param input the reader of the user input
     * @param menus the submenus, in the order in which they are offered to the
     *              user, starting with option 1
     */
    public ConsoleMenu(ConsoleInput input, List<Menu> menus) {
        this.input = input;
        this.menus = menus;
    }

    /**
     * Runs the main loop of the application until the user chooses to exit.
     */
    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("=== GAMEZONE UNICESAR ===");
            for (int i = 0; i < menus.size(); i++) {
                System.out.println((i + 1) + ". " + menus.get(i).getLabel());
            }
            System.out.println("0. Exit");
            int option = input.readInt("Choose an option: ");
            if (option == 0) {
                running = false;
            } else if (option >= 1 && option <= menus.size()) {
                menus.get(option - 1).run();
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
        System.out.println("Goodbye!");
    }
}
