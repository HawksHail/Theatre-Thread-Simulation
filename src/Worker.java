
/**
 * @author Zion Mantey
 *
 */
public abstract class Worker implements Runnable {

    final Theater theater;
    final int id;

    /**
     * Constructs a Worker object with an ID
     *
     * @param id ID of the Worker
     * @param theater theater this Worker is working at
     */
    public Worker(int id, Theater theater) {
        this.theater = theater;
        this.id = id;
    }

    /**
     * The work that the worker does for each customer
     */
    abstract void work();

    /**
     * Returns the name of the worker
     *
     * @return name of the worker
     */
    public abstract String getTitle();

    /**
     * Prints the string for creating the object with format depending on the amount in the theater
     */
    abstract void printCreated();

    /**
     * Returns the Worker ID
     *
     * @return the ID of this Worker instance
     */
    public int getId() {
        return id;
    }

    public String toString() {
        return getTitle() + " " + id;
    }

    /**
     * Starts the Worker events
     */
    @Override
    public void run() {
        printCreated();
        theater.getInitSem().release();
        while (true) {
            work();
        }
    }

}
