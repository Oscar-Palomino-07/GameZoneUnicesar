package com.gamezone;

import com.gamezone.persistence.PersonRepository;
import com.gamezone.persistence.ProductRepository;
import com.gamezone.persistence.ReturnRepository;
import com.gamezone.persistence.SaleRepository;
import com.gamezone.service.PersonService;
import com.gamezone.service.ProductService;
import com.gamezone.service.ReturnService;
import com.gamezone.service.SaleService;
import com.gamezone.ui.ConsoleMenu;

/**
 * Entry point of the GameZone Unicesar application.
 *
 * <p>Wires the four application layers together: the three repositories are
 * created first, the services are built on top of them (which loads the
 * previously stored data into memory) and finally the console menu is
 * launched with the three services.</p>
 */
public class Main {

    public static void main(String[] args) {
        PersonRepository personRepository = new PersonRepository();
        ProductRepository productRepository = new ProductRepository();
        SaleRepository saleRepository = new SaleRepository();
        ReturnRepository returnRepository = new ReturnRepository();

        PersonService personService = new PersonService(personRepository);
        ProductService productService = new ProductService(productRepository);
        SaleService saleService = new SaleService(saleRepository, personService, productService);
        ReturnService returnService = new ReturnService(returnRepository, saleService, personService, productService);

        ConsoleMenu menu = new ConsoleMenu(productService, personService, saleService, returnService);
        menu.start();
    }
}