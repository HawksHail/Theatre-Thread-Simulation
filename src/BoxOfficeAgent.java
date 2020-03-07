
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A box office agent in the movie theater
 *
 * @author Zion Mantey
 *
 */
public class BoxOfficeAgent extends Worker {

    public static final int TIME = 90000;

    /**
     * Constructs a BoxOfficeAgent object with an ID
     *
     * @param id ID of the BoxOfficeAgent
     * @param theater theater this BoxOfficeAgent is working at
     */
    public BoxOfficeAgent(int id, Theater theater) {
        super(id, theater);
    }

    /**
     * Waits for a customer to arrive, then sells them a ticket if possible
     */
    @Override
    void work() {
        try {
            theater.getBoxCustomerReadySem().acquire(); //wait for customer to be ready
            Customer c = theater.getBoxQ().remove();
            System.out.println(this + " serving " + c);
            Thread.sleep(TIME / Theater.TIME_SCALE);
            if (c.getMovie().purchaseTicket(1)) { //attempt to sell ticket
                c.setGotTicket();
                System.out.println(this + " sold ticket for " + c.getMovie().title + " to " + c);
            } else {
                System.out.println(this + " was unable to sell ticket for " + c.getMovie().title + " to " + c);
            }
            c.release();   //tell customer transaction done
            theater.getBoxSem().release();   //transaction is done, ready for next customer
        } catch (InterruptedException ex) {
            Logger.getLogger(BoxOfficeAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getTitle() {
        return "Box office agent";
    }

    @Override
    void printCreated() {
        if (Theater.MAX_BOX_AGENTS != 1) {
            System.out.println(this.getTitle() + " " + id + " created");
        } else {
            System.out.println(this.getTitle() + " created");
        }
    }

}
