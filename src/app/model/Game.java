package app.model;

import java.sql.Time;

/**
 * Record to store a Game
 * @param gid The game id, unique identifier
 * @param title
 * @param esrb_rating
 * @param ratings
 * @param genres
 * @param developer
 * @param publisher
 * @param playtime
 */
public record Game(
        int gid,
        String title,
        String esrb_rating,
        int[] ratings,
        String[] genres,
        Company developer,
        Company publisher,
        Time playtime
) {
}
