
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ticket taker in the movie theater
 *
 * @author Zion Mantey
 *
 */
public class TicketTaker extends Worker {

    public static final int TIME = 15000;

    /**
     * Constructs a TicketTaker object with an ID
     *
     * @param id ID of the TicketTaker
     * @param theater theater this TicketTaker is working at
     */
    public TicketTaker(int id, Theater theater) {
        super(id, theater);
    }

    /**
     * Waits for a customer to arrive, then takes their ticket and admits them into the theater
     */
    @Override
    void work() {
        try {
            theater.getTicketCustomerReadySem().acquire();   //wait for customer to be ready
            Customer c = theater.getTicketQ().remove();
            Thread.sleep(TIME / Theater.TIME_SCALE);
            System.out.println("Ticket taken from " + c);
            c.release();  //tell customer transaction done
            theater.getTicketSem().release();    //transaction done, ready for next customer
        } catch (InterruptedException ex) {
            Logger.getLogger(TicketTaker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getTitle() {
        return "Ticket taker";
    }

    @Override
    void printCreated() {
        if (Theater.MAX_TICKET_TAKERS != 1) {
            System.out.println(this.getTitle() + " " + id + " created");
        } else {
            System.out.println(this.getTitle() + " created");
        }
    }

}
