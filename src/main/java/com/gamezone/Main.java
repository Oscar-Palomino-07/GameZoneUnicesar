package com.gamezone;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.persistence.AccessoryRepository;
import com.gamezone.persistence.PersonRepository;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.persistence.PromotionRepository;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SaleRepository;
import com.gamezone.persistence.WarrantyRepository;
import com.gamezone.service.AccessoryService;
import com.gamezone.service.MonthlyBalanceService;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.PromotionService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.service.WarrantyService;
import com.gamezone.ui.AccessoryMenu;
import com.gamezone.ui.ConsoleErrorReporter;
import com.gamezone.ui.ConsoleInput;
import com.gamezone.ui.ConsoleMenu;
import com.gamezone.ui.Menu;
import com.gamezone.ui.PersonMenu;
import com.gamezone.ui.ProductMenu;
import com.gamezone.ui.PromotionMenu;
import com.gamezone.ui.ReportMenu;
import com.gamezone.ui.ReturnMenu;
import com.gamezone.ui.SaleMenu;
import com.gamezone.ui.WarrantyMenu;

import java.util.Arrays;
import java.util.List;

/**
 * Entry point of the GameZone Unicesar application.
 *
 * <p>Wires the four application layers together: the repositories are created
 * first, then the services that only need repositories, then the services that
 * need other services, and finally the console menu is launched with them.</p>
 *
 * <p>The construction order is deliberate. The warranty service is built
 * before the sale service because it needs the sale repository and the product
 * service to rebuild the stored warranties, while the sale service needs the
 * warranty service to assign the warranty of every console it sells. The
 * warranty repository depends on no service at all, so nothing has to be
 * created lazily and no dependency cycle has to be broken at runtime.</p>
 */
public class Main {

    /**
     * Starts the application. A failure while loading the data files is shown
     * to the user as a single message, because the system cannot work safely
     * with data it could not read.
     *
     * @param args the command line arguments, not used
     */
    public static void main(String[] args) {
        try {
            createMenu().start();
        } catch (PersistenceException e) {
            System.out.println("Ocurrió un problema al acceder a los datos del sistema. Contacte al administrador.");
        }
    }

    /**
     * Creates the repositories and the services in dependency order and builds
     * the console menu that uses them.
     *
     * @return the console menu ready to start
     * @throws PersistenceException when a data file cannot be read
     */
    private static ConsoleMenu createMenu() {
        // Repositories: they only read and write the data files.
        PersonRepository personRepository = new PersonRepository();
        ProductRepository productRepository = new ProductRepository();
        SaleRepository saleRepository = new SaleRepository();
        ReturnRepository returnRepository = new ReturnRepository();
        AccessoryRepository accessoryRepository = new AccessoryRepository();
        WarrantyRepository warrantyRepository = new WarrantyRepository();
        PromotionRepository promotionRepository = new PromotionRepository();

        // Services built on top of the repositories.
        PersonService personService = new PersonService(personRepository);
        ProductService productService = new ProductService(productRepository);
        AccessoryService accessoryService = new AccessoryService(accessoryRepository);
        PromotionService promotionService = new PromotionService(promotionRepository);

        // The warranty service resolves the stored sale and product references,
        // so the sale repository and the product service must exist first.
        WarrantyService warrantyService = new WarrantyService(warrantyRepository, saleRepository, productService);

        // The sale service assigns the warranties of every console it sells.
        SaleService saleService = new SaleService(saleRepository, personService, productService, accessoryService,
                warrantyService, promotionService);
        ReturnService returnService = new ReturnService(returnRepository, saleService, personService, productService,
                accessoryService, warrantyService);

        // The menus share one reader of the input and one reporter of errors;
        // the order of the list is the order of the options of the main menu.
        ConsoleInput input = new ConsoleInput();
        ConsoleErrorReporter errorReporter = new ConsoleErrorReporter();
        List<Menu> menus = Arrays.asList(
                new ProductMenu(productService, input, errorReporter),
                new PersonMenu(personService, input, errorReporter),
                new SaleMenu(saleService, input, errorReporter),
                new ReturnMenu(returnService, input, errorReporter),
                new AccessoryMenu(accessoryService, input, errorReporter),
                new ReportMenu(new MonthlyBalanceService(saleService, returnService), input, errorReporter),
                new WarrantyMenu(warrantyService, input, errorReporter),
                new PromotionMenu(promotionService, input, errorReporter));
        return new ConsoleMenu(input, menus);
    }
}