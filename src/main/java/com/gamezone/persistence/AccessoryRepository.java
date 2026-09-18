package com.gamezone.persistence;

import com.gamezone.model.Accessory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file-based persistence for the accessory module of GameZone Unicesar.
 * Accessories are stored in a JSON file under the {@code data} directory,
 * serialized with Gson. If the file does not exist yet, the repository loads
 * an empty list.
 */
public class AccessoryRepository {

    // Rutas: la carpeta y el archivo donde se guardan los accesorios.
    private static final String DATA_DIRECTORY = "data";
    private static final String ACCESSORIES_FILE = "data/accessories.json";

    // TypeToken: le dice a Gson que el archivo contiene una LISTA de Accessory.
    // Hace falta porque Java "olvida" el <Accessory> de List<Accessory> al
    // compilar, y sin esto Gson no sabría de qué tipo son los elementos.
    // Es lo mismo que haces en tu PersonRepository con CUSTOMERS_TYPE.
    private static final Type ACCESSORIES_TYPE = new TypeToken<List<Accessory>>() {
    }.getType();

    // El objeto Gson que convierte Java <-> JSON.
    // En el paso 7 esta línea se reemplaza por un constructor que le
    // registra el deserializador.
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves all accessories to the accessories JSON file, overwriting its
     * previous contents.
     *
     * @param accessories the list of accessories to persist
     */
    public void saveAll(List<Accessory> accessories) {
        // Método público corto: solo delega en el método privado genérico.
        writeList(ACCESSORIES_FILE, accessories, ACCESSORIES_TYPE);
    }

    /**
     * Loads all accessories from the accessories JSON file.
     *
     * @return the list of stored accessories, or an empty list if the file
     *         does not exist or cannot be parsed
     */
    public List<Accessory> loadAll() {
        return readList(ACCESSORIES_FILE, ACCESSORIES_TYPE);
    }

    /**
     * Serializes the given list to the given JSON file, creating the data
     * directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the list of accessories to persist
     * @param type     the Gson type of the generic list
     */
    private void writeList(String fileName, List<?> items, Type type) {
        try {
            Path path = Paths.get(fileName);
            // Crea la carpeta data/ si no existe (si ya existe, no hace nada).
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            // try-with-resources: el writer se cierra solo al terminar.
            // newBufferedWriter SOBRESCRIBE el archivo con la lista completa.
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(items, type, writer); // lista Java → texto JSON
            }
        } catch (IOException e) {
            // Error de disco: se avisa, pero el programa no se cae.
            System.err.println("Error saving accessories to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Deserializes a list of accessories from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the generic list
     * @param <T>      the type stored in the file
     * @return the stored accessories, or an empty list if the file does not
     *         exist or cannot be parsed
     */
    private <T> List<T> readList(String fileName, Type type) {
        Path path = Paths.get(fileName);
        // Regla del enunciado: si el archivo no existe, lista vacía.
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<T> items = gson.fromJson(reader, type); // texto JSON → lista Java
            // Si el archivo está vacío, Gson devuelve null: lo cambiamos por lista vacía.
            return items != null ? items : new ArrayList<>();
        } catch (IOException | JsonSyntaxException e) {
            // JsonSyntaxException: el archivo tiene un JSON mal escrito.
            System.err.println("Error loading accessories from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}