
/**
 * A movie with a title and a ticket count
 *
 * @author Zion Mantey
 *
 */
public class Movie {

    final String title;
    private int ticketsAvaliable;

    /**
     * Constructs a Movie object with a given title and number of seats
     *
     * @param title title of the movie
     * @param ticketsAvaliable number of seats avaliable
     */
    public Movie(String title, int ticketsAvaliable) {
        this.title = title;
        this.ticketsAvaliable = ticketsAvaliable;
    }

    /**
     * Attempts to purchase a ticket for the movie
     *
     * @param number number of tickets to purchase
     * @return if the tickets were purchased successfully
     */
    public synchronized boolean purchaseTicket(int number) {
        if (ticketsAvaliable >= number) {
            ticketsAvaliable -= number;
            return true;
        }
        return false;
    }
/**
 * Returns the movie as a string
 * @return title and seat availability in a string
 */
    public String toString() {
        return title + " " + ticketsAvaliable;
    }

}
