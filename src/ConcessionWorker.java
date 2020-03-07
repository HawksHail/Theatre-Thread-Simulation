
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A concession worker in the movie theater
 *
 * @author Zion Mantey
 *
 */
public class ConcessionWorker extends Worker {

    public static final int TIME = 180000;

    /**
     * Constructs a ConcessionWorker object with an ID
     *
     * @param id ID of the ConcessionWorker
     * @param theater theater this ConcessionWorker is working at
     */
    public ConcessionWorker(int id, Theater theater) {
        super(id, theater);
    }

    /**
     * Waits for a customer to arrive, then sells them food and drinks
     */
    @Override
    void work() {
        try {
            theater.getConcessionCustomerReadySem().acquire();   //wait for customer to be ready
            Customer c = theater.getConcessionQ().remove();
            System.out.println("Order for " + c.getFood() + " from " + c);
            Thread.sleep(TIME / Theater.TIME_SCALE);
            System.out.println(c.getFood() + " given to " + c);
            c.release();  //tell customer transaction done
            theater.getConcessionSem().release();    //transaction done, ready for next customer
        } catch (InterruptedException ex) {
            Logger.getLogger(ConcessionWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getTitle() {
        return "Concession stand worker";
    }

    @Override
    void printCreated() {
        if (Theater.MAX_CONCESSION_WORKERS != 1) {
            System.out.println(this.getTitle() + " " + id + " created");
        } else {
            System.out.println(this.getTitle() + " created");
        }
    }

}
