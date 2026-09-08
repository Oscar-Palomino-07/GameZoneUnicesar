package com.gamezone.persistence;

import com.gamezone.model.Console;
import com.gamezone.model.VideoGame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file-based persistence for the product module of GameZone Unicesar.
 * Video games and consoles are stored separately in two plain text files under
 * the {@code data} directory, using tab-separated fields and one product per
 * line. If a file does not exist yet, the repository loads an empty list.
 */
public class ProductRepository {

    private static final String DATA_DIRECTORY = "data";
    private static final String VIDEO_GAMES_FILE = "data/videogames.txt";
    private static final String CONSOLES_FILE = "data/consoles.txt";

    /**
     * Saves all video games to the video games text file, overwriting its
     * previous contents.
     *
     * @param videoGames the list of video games to persist
     */
    public void saveAllVideoGames(List<VideoGame> videoGames) {
        List<String> lines = new ArrayList<>();
        for (VideoGame game : videoGames) {
            lines.add(String.join("\t",
                    game.getId(), game.getTitle(),
                    String.valueOf(game.getPrice()),
                    String.valueOf(game.getStock()),
                    game.getPlatform(), game.getGenre(), game.getAgeRating()));
        }
        writeLines(VIDEO_GAMES_FILE, lines);
    }

    /**
     * Loads all video games from the video games text file.
     *
     * @return the list of stored video games, or an empty list if the file
     *         does not exist
     */
    public List<VideoGame> loadAllVideoGames() {
        List<VideoGame> videoGames = new ArrayList<>();
        for (String[] fields : readLines(VIDEO_GAMES_FILE)) {
            if (fields.length == 7) {
                videoGames.add(new VideoGame(
                        fields[0], fields[1],
                        Double.parseDouble(fields[2]),
                        Integer.parseInt(fields[3]),
                        fields[4], fields[5], fields[6]));
            }
        }
        return videoGames;
    }

    /**
     * Saves all consoles to the consoles text file, overwriting its previous
     * contents.
     *
     * @param consoles the list of consoles to persist
     */
    public void saveAllConsoles(List<Console> consoles) {
        List<String> lines = new ArrayList<>();
        for (Console console : consoles) {
            lines.add(String.join("\t",
                    console.getId(), console.getTitle(),
                    String.valueOf(console.getPrice()),
                    String.valueOf(console.getStock()),
                    console.getBrand(), console.getModel(), console.getGeneration()));
        }
        writeLines(CONSOLES_FILE, lines);
    }

    /**
     * Loads all consoles from the consoles text file.
     *
     * @return the list of stored consoles, or an empty list if the file does
     *         not exist
     */
    public List<Console> loadAllConsoles() {
        List<Console> consoles = new ArrayList<>();
        for (String[] fields : readLines(CONSOLES_FILE)) {
            if (fields.length == 7) {
                consoles.add(new Console(
                        fields[0], fields[1],
                        Double.parseDouble(fields[2]),
                        Integer.parseInt(fields[3]),
                        fields[4], fields[5], fields[6]));
            }
        }
        return consoles;
    }

    /**
     * Writes the given lines to the given file, one line per product. Creates
     * the data directory and the file when they do not exist yet.
     *
     * @param fileName the path of the destination file
     * @param lines    the text lines to persist
     */
    private void writeLines(String fileName, List<String> lines) {
        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(Paths.get(DATA_DIRECTORY));
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving products to " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Reads every non-empty line of the given file as a tab-separated array
     * of fields.
     *
     * @param fileName the path of the source file
     * @return the parsed lines, or an empty list if the file does not exist
     */
    private List<String[]> readLines(String fileName) {
        List<String[]> lines = new ArrayList<>();
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return lines;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line.split("\t", -1));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading products from " + fileName + ": " + e.getMessage());
        }
        return lines;
    }
}