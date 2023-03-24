package app.model;

import java.sql.Time;
import java.sql.Date;

/**
 * Record to store a Game
 * @param gid The game id, unique identifier
 * @param title
 * @param esrb_rating
 * @param ratings
 * @param genre
 * @param developer
 * @param publisher
 * @param playtime
 */
public record Game(
        int gid,
        String title,
        String esrb_rating,
        int[] ratings,
        String genre,
        Company developer,
        Company publisher,
        Time playtime,
        Date date_release,
        double price

) {
}
