
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A customer in the movie theater
 *
 * @author Zion Mantey
 *
 */
public class Customer implements Runnable {

    public enum Food {
        Popcorn, Soda, Both;
    }

    private static final Random rand = new Random();
    private final Theater theater;
    private final int id;
    private final Movie movie;
    private boolean gotTicket = false;
    private final Semaphore ready;
    private Food food;

    /**
     * Constructs a Customer object with an ID
     *
     * @param id ID of the customer
     * @param theater theater this customer is visiting
     */
    public Customer(int id, Theater theater) {
        this.ready = new Semaphore(0);
        this.id = id;
        this.theater = theater;
        ArrayList<Movie> movies = theater.getMovies();
        this.movie = movies.get(rand.nextInt(movies.size()));
    }

    /**
     * Calls aquire on the customers ready semaphore
     *
     * @throws InterruptedException
     */
    public void acquire() throws InterruptedException {
        ready.acquire();
    }

    /**
     * Calls release on the customers ready semaphore
     */
    public void release() {
        ready.release();
    }

    /**
     * Returns the chosen movie
     *
     * @return the movie of this Customer instance
     */
    public Movie getMovie() {
        return movie;
    }

    /**
     * Returns the Customers ID
     *
     * @return the ID of this Customer instance
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the Customers food choice
     *
     * @return the food choice of this Customer instance
     */
    public Food getFood() {
        return food;
    }

    /**
     * Sets that the Customer received a ticket
     */
    public void setGotTicket() {
        gotTicket = true;
    }

    /**
     * Attempts to buy a ticket from a Box office agent
     *
     * @return if received ticket successfully
     */
    private boolean buyTicket() {
        try {
            theater.getBoxSem().acquire();   //wait for a box agent to be avaliable
            theater.getBoxQ().add(this);
            theater.getBoxCustomerReadySem().release(); //tell box agent you are ready to buy ticket
            ready.acquire();    //wait for box agent to give ticket
        } catch (InterruptedException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return gotTicket;
    }

    /**
     * Gives ticket to ticket taker and enters lobby of theater
     *
     * @return always true
     */
    private boolean enterLobby() {
        System.out.println(this + " in line to see ticket taker");
        try {
            theater.getTicketSem().acquire();    //wait for ticket taker to be avaliable
            theater.getTicketQ().add(this);
            theater.getTicketCustomerReadySem().release();   //tell ticket taker you are ready
            ready.acquire();    //wait for ticket taker to take ticket
        } catch (InterruptedException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Decides if to visit concession stand and what to buy
     *
     * @return if the customer visited the concession stand
     */
    private boolean visitConcessions() {
        if (rand.nextFloat() > 0.5) {   //decide to visit concessions
            return false;
        }
        switch (rand.nextInt(3)) {  //decide what to buy
            case 0:
                food = Food.Popcorn;
                break;
            case 1:
                food = Food.Soda;
                break;
            case 2:
                food = Food.Both;
                break;
        }
        System.out.println(this + " in line to buy " + food);
        try {
            theater.getConcessionSem().acquire();    //wait for concession worker to be avaliable
            theater.getConcessionQ().add(this);
            theater.getConcessionCustomerReadySem().release();   //tell concession worker you are ready
            ready.acquire();    //wait for concession worker to give food
        } catch (InterruptedException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public String toString() {
        return "Customer " + id;
    }

    /**
     * Starts the customers events
     */
    @Override
    public void run() {
        System.out.println(this + " created, buying ticket to " + movie.title);
        //attempt to buy ticket
        if (!buyTicket()) {   //if movie sold out, leave
            System.out.println(this + " did not get a ticket to " + movie.title + " and left");
            return;
        }
        enterLobby();   //go to ticket taker and enter
        visitConcessions(); //go to concession stand
        System.out.println(this + " entered theater to see " + movie.title);
    }

}
