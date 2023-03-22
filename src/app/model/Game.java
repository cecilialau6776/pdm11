package app.model;

/**
 * Record to store a Game
 * @param gid The game id, unique identifier
 * @param title
 * @param esrb_rating
 */
public record Game(
        int gid,
        String title,
        String esrb_rating,
        String genre
) {
}
