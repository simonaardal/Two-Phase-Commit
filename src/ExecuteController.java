import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A utils-class containing methods to run and join threads (tasks)
 */
public class ExecuteController {
    ArrayList<Thread> runningThreads = new ArrayList<>();
    public boolean transaction_started = true;
    static ReentrantLock lock = new ReentrantLock();

    /**
     * Default constructor
     */
    public ExecuteController() {

    }

    /**
     * Method to run a thread. The thread will run for the given timeout, and then be interrupted
     *
     * @param task    The task to be run
     * @param timeout The time the task will run
     * @param phase   The phase of the protocol the task runs in
     * @param ph      The ParticipantHandler associated with the task
     */
    public void execute(Thread task, long timeout, int phase, ParticipantHandler ph) {
        Thread thread = new Thread(() -> {
            task.start();
            try {
                task.join(timeout);
            } catch (InterruptedException ignored) {
            }
            if (task.isAlive()) {
                task.interrupt();
                if (phase == 0) {
                    transaction_started = false;
                    System.out.println("Transaction timeout from Coordinator. Disconnecting...");
                }
                if (phase == 1) {
                    lock.lock();
                    if (ph != null) Coordinator.participants.remove(ph);
                    Coordinator.votes.add("NO");
                    lock.unlock();
                    System.out.println("Transaction timeout from Participant. Voting NO");
                }
            }
        });
        runningThreads.add(thread);
        thread.start();
    }


    /**
     * Joining all threads which are in the list of the running threads
     *
     * @throws InterruptedException InterruptedException
     */
    public void joinThreads() throws InterruptedException {
        for (Thread t : runningThreads) {
            t.join();
        }
        runningThreads.clear();
    }
}
