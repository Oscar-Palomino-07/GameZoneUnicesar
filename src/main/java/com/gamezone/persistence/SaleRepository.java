package com.gamezone.persistence;

import com.gamezone.exceptions.PersistenceException;
import com.gamezone.model.Product;
import com.gamezone.model.Sale;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles file-based persistence for the sales module of GameZone Unicesar.
 * Sales are stored in a JSON file under the {@code data} directory,
 * serialized with Gson. If the file does not exist yet, the repository loads
 * an empty list.
 *
 * <p>The repository registers two shared serializers because the default Gson
 * behavior is not enough for this module: {@link LocalDateAdapter} writes
 * {@link LocalDate} values as ISO-8601 strings and {@link ProductDeserializer}
 * rebuilds every {@link Product} as its concrete subtype when a file is
 * loaded. The reading and the writing of the file are done by the shared
 * {@link JsonFileStore}.</p>
 */
public class SaleRepository {

    private static final String SALES_FILE = "data/sales.json";

    private static final Type SALES_TYPE = new TypeToken<List<Sale>>() {
    }.getType();

    private final JsonFileStore store;

    /**
     * Creates the sale repository and configures the Gson instance with the
     * serializers required for {@link LocalDate} and {@link Product}.
     */
    public SaleRepository() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(Product.class, new ProductDeserializer());
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        this.store = new JsonFileStore(builder.create());
    }

    /**
     * Saves all sales to the sales JSON file, overwriting its previous
     * contents and creating the data directory and the file when they do not
     * exist yet.
     *
     * @param sales the list of sales to persist
     * @throws PersistenceException when the file cannot be written
     */
    public void saveAll(List<Sale> sales) {
        try {
            store.write(SALES_FILE, sales, SALES_TYPE);
        } catch (IOException e) {
            throw new PersistenceException(SALES_FILE, "escritura", e);
        }
    }

    /**
     * Loads all sales from the sales JSON file.
     *
     * @return the stored sales, or an empty list if the file does not exist
     *         yet
     * @throws PersistenceException when the file exists but cannot be read
     *         or parsed
     */
    public List<Sale> loadAll() {
        try {
            return store.read(SALES_FILE, SALES_TYPE);
        } catch (IOException | JsonSyntaxException e) {
            throw new PersistenceException(SALES_FILE, "lectura", e);
        }
    }
}
