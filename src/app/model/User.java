package app.model;

import java.sql.Date;

/**
 * Record to store a User
 * @param username
 * @param password
 * @param email
 * @param firstname
 * @param lastname
 * @param last_access_date
 * @param creation_date
 */
public record User(
        String username,
        String password,
        String email,
        String firstname,
        String lastname,
        Date last_access_date,
        Date creation_date
) {
}
