package app.model;

/**
 * THe profile of a user within the app
 * @param username
 * @param numCollections
 * @param numFollowers
 * @param numFollowing
 * @param topTen
 */
public record UserProfile(
        String username,
        int numCollections,
        int numFollowers,
        int numFollowing,
        Game[] topTen
) {
    public String display() {
        String format = "%s has:" +
                "\t%d collections\n" +
                "\t%d followers\n" +
                "\t%d following";
        return String.format(format, username, numCollections, numFollowers, numFollowing);
    }
}
