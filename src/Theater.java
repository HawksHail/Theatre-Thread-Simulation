
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simulates a movie theater using semaphores to ensure correct order of events
 *
 * @author Zion Mantey
 */
public class Theater {

    //constants 
    public static final int MAX_CUSTOMERS = 50;
    public static final int MAX_BOX_AGENTS = 2;
    public static final int MAX_TICKET_TAKERS = 1;
    public static final int MAX_CONCESSION_WORKERS = 1;
    public static final int TIME_SCALE = 60;

    //list of movies in the theater
    private final ArrayList<Movie> movies;

    //lists of all the threads
    private final ArrayList<Thread> customerThreads;

    //queues to manage customer lines
    private final Queue<Customer> boxQ;
    private final Queue<Customer> ticketQ;
    private final Queue<Customer> concessionQ;

    //used to initilize all workers before opening theater
    private final Semaphore initSem;
    //manages how many customers and box agents are ready
    private final Semaphore boxSem;
    private final Semaphore boxCustomerReadySem;
    //manages how many customers and ticket takers are ready
    private final Semaphore ticketSem;
    private final Semaphore ticketCustomerReadySem;
    //manages how many customers and concession workers are ready
    private final Semaphore concessionSem;
    private final Semaphore concessionCustomerReadySem;

    /**
     * Constructs a Theater and creates all empty lists.
     */
    public Theater() {
        this.initSem = new Semaphore(-(MAX_BOX_AGENTS + MAX_CONCESSION_WORKERS + MAX_TICKET_TAKERS) + 1, true);  //set semaphore to negative amount of workers + 1
        this.concessionCustomerReadySem = new Semaphore(0, true);
        this.concessionSem = new Semaphore(Theater.MAX_CONCESSION_WORKERS, true);
        this.ticketCustomerReadySem = new Semaphore(0, true);
        this.ticketSem = new Semaphore(Theater.MAX_TICKET_TAKERS, true);
        this.boxCustomerReadySem = new Semaphore(0, true);
        this.boxSem = new Semaphore(Theater.MAX_BOX_AGENTS, true);
        this.movies = new ArrayList<>();
        this.customerThreads = new ArrayList<>(MAX_CUSTOMERS);
        this.boxQ = new ConcurrentLinkedQueue<>();
        this.ticketQ = new ConcurrentLinkedQueue<>();
        this.concessionQ = new ConcurrentLinkedQueue<>();
    }

    ArrayList<Movie> getMovies() {
        return movies;
    }

    Queue<Customer> getBoxQ() {
        return boxQ;
    }

    Queue<Customer> getTicketQ() {
        return ticketQ;
    }

    Queue<Customer> getConcessionQ() {
        return concessionQ;
    }

    Semaphore getInitSem() {
        return initSem;
    }

    Semaphore getBoxSem() {
        return boxSem;
    }

    Semaphore getBoxCustomerReadySem() {
        return boxCustomerReadySem;
    }

    Semaphore getTicketSem() {
        return ticketSem;
    }

    Semaphore getTicketCustomerReadySem() {
        return ticketCustomerReadySem;
    }

    Semaphore getConcessionSem() {
        return concessionSem;
    }

    Semaphore getConcessionCustomerReadySem() {
        return concessionCustomerReadySem;
    }

    /**
     * Loads the movie file, then creates all worker threads and waits for all to finish.
     *
     * @param movieFile filename of the movie file
     */
    public void initTheater(String movieFile) {
        if (!initMovies(movieFile)) {
            System.out.println("Movie file could not be opened");
            System.exit(1);
        }
        initBoxAgents();
        initTicketTakers();
        initConcessionWorkers();
        try {
            initSem.acquire();  //once all workers are initialized, this will resume
        } catch (InterruptedException ex) {
            Logger.getLogger(Theater.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Theater is open");
    }

    /**
     * Loads all the movies in the file to the movies array
     *
     * @param filename filename of the movie file
     * @return if file was loaded successfully
     */
    private boolean initMovies(String filename) {
        File file = new File(filename);
        Scanner inFile;
        if (!file.exists()) { //check if file exists
            return false;
        }
        try {   //load data from file
            inFile = new Scanner(file);
            while (inFile.hasNextLine()) {
                String line[] = inFile.nextLine().trim().split("\t");
                movies.add(new Movie(line[0], Integer.valueOf(line[1])));
            }
            inFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Theater.class.getName()).log(Level.SEVERE, null, ex);
        }
        //System.out.println("Movies loaded");
        return true;
    }

    /**
     * Creates all of the Box Agent threads and stores them in the boxAgentThreads ArrayList
     *
     * @return always returns true
     */
    private boolean initBoxAgents() {
        for (int i = 0; i < MAX_BOX_AGENTS; i++) {
            Thread thread = new Thread(new BoxOfficeAgent(i, this));
            thread.start();
        }
        return true;
    }

    /**
     * Creates all of the Ticket Taker threads and stores them in the ticketTakerThreads ArrayList
     *
     * @return always returns true
     */
    private boolean initTicketTakers() {
        for (int i = 0; i < MAX_TICKET_TAKERS; i++) {
            Thread thread = new Thread(new TicketTaker(i, this));
            thread.start();
        }
        return true;
    }

    /**
     * Creates all of the Concession Workers threads and stores them in the concessionWorkerThreads ArrayList
     *
     * @return always returns true
     */
    private boolean initConcessionWorkers() {
        for (int i = 0; i < MAX_CONCESSION_WORKERS; i++) {
            Thread thread = new Thread(new ConcessionWorker(i, this));
            thread.start();
        }
        return true;
    }

    /**
     * Creates all of the Customers threads and stores them in the customerThreads ArrayList
     *
     * @return always returns true
     */
    public boolean initCustomers() {
        for (int i = 0; i < MAX_CUSTOMERS; i++) {
            customerThreads.add(new Thread(new Customer(i, this)));
            customerThreads.get(i).start();
        }
        joinCustomers();
        return true;
    }

    /**
     * Joins all of the customer threads
     */
    private void joinCustomers() {
        try {
            for (int i = 0; i < customerThreads.size(); i++) {
                customerThreads.get(i).join();
                System.out.println("Joined customer " + i);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Theater.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Starts the movie theater simulation
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Theater theater = new Theater();
        if (args.length >= 1) {
            theater.initTheater(args[0]);
        } else {
            System.out.println("Missing parameter movie filename");
            return;
        }
        theater.initCustomers();
        System.exit(0);
    }

}
