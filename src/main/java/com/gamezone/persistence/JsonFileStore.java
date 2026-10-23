package com.gamezone.persistence;

import com.google.gson.Gson;

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
 * Writes a list of objects to a JSON file and reads it back.
 *
 * <p>It is the technical task that every repository of the system shares, so
 * it lives in one class instead of being copied in each repository. The store
 * does not decide what happens when a file cannot be read or written: it
 * reports the failure to the repository, which keeps its own error behavior
 * (throwing {@code PersistenceException} or printing a message and going on).
 * Each repository creates the store with its own {@link Gson}, because the
 * adapters it needs depend on the objects it stores.</p>
 */
class JsonFileStore {

    private final Gson gson;

    /**
     * Creates a store that serializes with the given Gson instance.
     *
     * @param gson the Gson instance configured by the repository
     */
    JsonFileStore(Gson gson) {
        this.gson = gson;
    }

    /**
     * Writes the given list to the given JSON file, overwriting its previous
     * contents and creating the folder of the file when it does not exist yet.
     *
     * @param fileName the path of the destination file
     * @param items    the objects to write
     * @param type     the Gson type of the list
     * @throws IOException when the folder or the file cannot be written
     */
    void write(String fileName, List<?> items, Type type) throws IOException {
        Path path = Paths.get(fileName);
        Path folder = path.getParent();
        if (folder != null) {
            Files.createDirectories(folder);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(items, type, writer);
        }
    }

    /**
     * Reads a list from the given JSON file.
     *
     * @param fileName the path of the source file
     * @param type     the Gson type of the list
     * @param <T>      the type of the stored objects
     * @return the stored objects, or an empty list when the file does not
     *         exist yet or is empty
     * @throws IOException                         when the file exists but
     *                                             cannot be read
     * @throws com.google.gson.JsonSyntaxException when the file content is
     *                                             not valid JSON for the type
     */
    <T> List<T> read(String fileName, Type type) throws IOException {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<T> items = gson.fromJson(reader, type);
            return items != null ? items : new ArrayList<>();
        }
    }
}
