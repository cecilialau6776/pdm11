package app.model;

/**
 * Record to store a Company
 * @param compid The company id, unique identifier
 * @param name
 */
public record Company(
        int compid,
        String name
) {
}
