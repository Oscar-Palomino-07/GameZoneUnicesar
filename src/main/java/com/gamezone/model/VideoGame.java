package com.gamezone.model;

/**
 * Represents a video game product sold by GameZone Unicesar.
 * Extends {@link Product} and adds the attributes particular to video games:
 * target platform, genre and recommended age rating.
 */
public class VideoGame extends Product {

    private String platform;
    private String genre;
    private String ageRating;

    /**
     * Creates a video game with its common and particular attributes.
     *
     * @param id        the unique product identifier
     * @param title     the video game title
     * @param price     the unit price of the video game
     * @param stock     the quantity available in inventory
     * @param platform  the platform the game is developed for
     * @param genre     the game genre
     * @param ageRating the recommended age rating of the game
     */
    public VideoGame(String id, String title, double price, int stock,
                     String platform, String genre, String ageRating) {
        super(id, title, price, stock);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    /**
     * @return the platform the game is developed for
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * @return the game genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @return the recommended age rating of the game
     */
    public String getAgeRating() {
        return ageRating;
    }

    /**
     * Builds a description that integrates the common attributes with the
     * attributes particular to video games.
     *
     * @return a string describing the video game
     */
    @Override
    public String getDescription() {
        return "VideoGame: " + getTitle()
                + " | platform: " + platform
                + " | genre: " + genre
                + " | age rating: " + ageRating
                + " | price: $" + getPrice()
                + " | stock: " + getStock();
    }
}