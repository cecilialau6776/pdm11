package app.model;

import java.sql.Time;

/**
 * Record to store a Collection
 * @param collid The collection id, unique identifier
 * @param coll_username The username of the user who this collection belongs to
 * @param coll_name
 */
public record Collection(
        int collid,
        String coll_username,
        String coll_name,
        Game[] games,
        Time total_playtime
) {
}
