
import java.io.*;
import java.net.Socket;
import java.util.UUID;

/**
 * Handles Participants for the Coordinator
 */
public class ParticipantHandler {
    private Socket socket;
    private InputStreamReader readerConnection;
    private BufferedReader reader;
    private PrintWriter writer;
    String id;
    public ExecuteController executeController;
    public boolean voted = false;
    public boolean sent_done = false;

    static final long TIMEOUT_PERIOD = 20 * 1000;

    /**
     * Handles Participants for the Coordinator
     *
     * @param socket Java socket
     * @param executeController Custom thread timeout handler
     * @throws IOException IOException
     */
    public ParticipantHandler(Socket socket, ExecuteController executeController) throws IOException {
        this.id = UUID.randomUUID().toString();
        this.socket = socket;
        this.readerConnection = new InputStreamReader(this.socket.getInputStream());
        this.reader = new BufferedReader(readerConnection);
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.executeController = executeController;
    }

    /**
     * Execute Phase One
     *
     * @param transaction Transaction Object to be sent to the Participant
     */
    public void Transaction_Phase_One(Transaction transaction) {
        executeController.execute(new Thread(() -> {
            try {
                {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
                    writer.println("REQUEST-TO-PREPARE (PREPARED/NO)");
                    objectOutputStream.writeObject(transaction);
                }
                String data;
                while ((data = reader.readLine()) == null) ;
                Coordinator.votes.add(data);
                voted = true;
            } catch (IOException e) {
                Coordinator.votes.add("NO");
            }
        }), TIMEOUT_PERIOD, 1, this);
    }

    /**
     * Execute Phase Two
     * @param decision Decision can be "COMMIT"/"ABORT"
     */
    public void Transaction_Phase_Two(String decision) {
        executeController.execute(
                new Thread(() -> {
                    try {
                        writer.println(decision);
                        reader.readLine();
                        sent_done = true;
                    } catch (IOException ignored) {
                    }
                }),
                TIMEOUT_PERIOD,
                2, this
        );
    }

    /**
     * Send Remainder if Participant is not sending "DONE"
     */
    public void Transaction_Phase_Two_Remainder() {
        executeController.execute(
                new Thread(() -> {
                    try {
                        System.out.println("Sending remainder to participant");
                        writer.println("REMAINDER: Participant is required to send DONE");
                        reader.readLine();
                        sent_done = true;
                    } catch (IOException ignored) {
                    }
                }),
                TIMEOUT_PERIOD,
                2, this
        );
    }
}
