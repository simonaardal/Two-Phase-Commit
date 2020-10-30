import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Class playing the role as a coordinator in a Two-Phase-Commit
 */
public class Coordinator {
    static List<ParticipantHandler> participants = new ArrayList<>();
    static List<String> votes = new ArrayList<>();
    static final int PORTNR = 1337;
    static LoggerManager logger;


    /**
     * Main method to run the program
     *
     * @param args Java main argument
     */
    public static void main(String[] args) {
        try {
            ExecuteController executeController = new ExecuteController();
            Scanner in = new Scanner(System.in);
            System.out.println("Enter Transaction Amount");
            int amount = in.nextInt();
            String id_transaction = UUID.randomUUID().toString();
            in.nextLine();

            final ServerSocket socket = new ServerSocket(PORTNR);
            Thread ParticipantHandlerThread = new Thread(() -> {
                while (true) {
                    try {
                        Socket connection = socket.accept();
                        ParticipantHandler ParticipantHandler = new ParticipantHandler(connection, executeController);
                        participants.add(ParticipantHandler);
                    } catch (IOException ignored) {
                    }
                }
            });
            ParticipantHandlerThread.start();

            System.out.println("Press Enter to start protocol");
            in.nextLine();
            in.close();
            logger = new LoggerManager(true);
            LoggerManager.clearFiles();
            ParticipantHandlerThread.stop();

            System.out.println("Starting Two-Phase Commit Protocol");

            logger.writeToFile(participants.toString());
            System.out.println("Logging participants done. Proceeding to Phase one");

            participants.forEach(participant -> participant.Transaction_Phase_One(new Transaction(id_transaction, amount)));

            System.out.println("Phase 1 complete. Starting Phase 2");

            String decision = decide();
            logger.writeToFile(decision);

            System.out.println("All Participants have voted. Decision is " + decision + " based on the following votes:");

            participants.forEach(participant -> participant.Transaction_Phase_Two(decision));
            System.out.println(votes.toString());

            System.out.println("Waiting for connected participants to acknowledge the decision");

            executeController.joinThreads();


            boolean all_sent_done = false;
            while (!all_sent_done) {
                all_sent_done = true;
                for (ParticipantHandler ch : participants) {
                    if (!ch.sent_done) {
                        ch.Transaction_Phase_Two_Remainder();
                        all_sent_done = false;
                    }
                }
                executeController.joinThreads();
            }
            logger.writeToFile("DONE");
            System.out.println("Logging complete. Clearing resources...");
            ParticipantHandlerThread.interrupt();
            participants.clear();
            votes.clear();
            socket.close();
            System.out.println("Finished");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String decide() throws InterruptedException {
        Thread count = new Thread(() -> {
            while (participants.size() != votes.size()) {
                System.out.print("");
            }
        });
        count.start();
        count.join();
        if (votes.contains("NO")) return "ABORT";
        return "COMMIT";
    }


}