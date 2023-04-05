package app.model;

import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Date;

/**
 * Class to store a Game
 */
public class Game {
    private int gid;
    private String title;
    private String esrb_rating;
    private int[] ratings;
    private String[] genres;
    private Company developer;
    private Company publisher;
    private Time playtime;
    private Date date_release;
    private double price;

    public Game(int gid, String title, String esrb_rating, int[] ratings, String[] genres, Company developer, Company publisher, Time playtime, Date date_release, double price) {
        this.gid = gid;
        this.title = title;
        this.esrb_rating = esrb_rating;

        this.ratings = ratings;
        this.genres = genres;
        this.developer = (developer != null) ? developer : new Company(0, "Untitled Company");
        this.publisher = (publisher != null) ? publisher : new Company(0, "Untitled Company");
        this.playtime = playtime;
        this.date_release = date_release;
        this.price = price;
    }

    public int gid() {
        return gid;
    }

    public String title() {
        return title;
    }

    public String esrb_rating() {
        return esrb_rating;
    }

    public int[] ratings() {
        return ratings;
    }

    public String[] genres() {
        return genres;
    }

    public Company developer() {
        return developer;
    }

    public Company publisher() {
        return publisher;
    }

    public Time playtime() {
        return playtime;
    }

    public Date date_release() {
        return date_release;
    }

    public double price() {
        return price;
    }
}
