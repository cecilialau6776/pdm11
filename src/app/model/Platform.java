package app.model;

/**
 * Record to store a Platform
 * @param pid The platform id, unique identifier
 * @param name
 */
public record Platform(
        int pid,
        String name
) {
}
