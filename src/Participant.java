import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Class playing the role as a Participant in two-phase-commit
 */
public class Participant {
    /**
     * Main method to start the participant
     * @param args Java main argument
     * @throws IOException Creating a file might throw an IO-exception
     * @throws InterruptedException The use of threads may cause an InterruptedExcetion
     * @throws ClassNotFoundException Reads object from other classes
     */
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        final long TIMEOUT_PERIOD = 20 * 1000;

        Scanner sc = new Scanner(System.in);

        System.out.println("Write the balance of the account");
        double balance = sc.nextDouble();
        System.out.println("For automatic participant select true");
        boolean automatic = sc.nextBoolean();
        ResourceManager resourceManager = new ResourceManager(balance);

        LoggerManager logger = new LoggerManager(false);
        ExecuteController executeController = new ExecuteController();

        InetAddress serverMachine = Inet4Address.getLocalHost();
        Socket connection = new Socket(serverMachine, Coordinator.PORTNR);

        System.out.println("Connection created at " + serverMachine + ":" + Coordinator.PORTNR);

        InputStreamReader readerConnection = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(readerConnection);
        PrintWriter writer = new PrintWriter(connection.getOutputStream(), true);
        ObjectInputStream objectInputStream = new ObjectInputStream(connection.getInputStream());


        executeController.execute(new Thread(() -> {
            try {
                System.out.println(reader.readLine());
            } catch (IOException ignored) {
            }
        }), TIMEOUT_PERIOD, 0, null);
        executeController.joinThreads();

        if (executeController.transaction_started) {
            Object obj = objectInputStream.readObject();
            Transaction transaction = (Transaction) obj;

            System.out.println("Got transaction with value " + transaction.value + ". Current balance in resource_manager is " + resourceManager.balance);
            boolean voted;
            if (automatic) {
                voted = resourceManager.add(transaction.value);
            } else {
                System.out.println("Voting... PREPARED/NO");
                sc.nextLine(); //Preventing SCANNER BUG
                String inp = sc.nextLine();
                voted = inp.equalsIgnoreCase("PREPARED");
                System.out.println("Voted: " + voted);

                if (voted) {
                    resourceManager.previous_balance = resourceManager.balance;
                    resourceManager.balance += transaction.value;
                }
            }


            String vote = voted ? "PREPARED" : "NO";
            System.out.println(voted ? "Transaction added to wallet. Voting PREPARED" : "Transaction not possible. Voting NO");
            logger.writeToFile(vote);
            writer.println(vote);

            System.out.println("Current balance is: " + resourceManager.balance);
            System.out.println("Waiting for decision from Coordinator");

            String decision = reader.readLine();
            System.out.println("Got decision from Coordinator: " + decision);
            logger.writeToFile(decision);

            if (decision.equals("ABORT") && voted) {
                resourceManager.rollback();
                System.out.println("Rolling back transaction");
            }
            System.out.println("Current balance is: " + resourceManager.balance);

            Thread done_phase = new Thread(() -> {
                try {
                    while (true) {
                        String data = reader.readLine();
                        if (data != null) {
                            System.out.println(data);
                        }
                    }

                } catch (IOException ignored) {
                }
            });
            done_phase.start();

            if (automatic) {
                writer.println("DONE");
            } else {
                System.out.println("Please let the coordinator know that you are DONE");
                sc.next(); //SCANNER BUG

                String done_sta = sc.nextLine();
                writer.println(done_sta);
            }
            done_phase.interrupt();
            objectInputStream.close();
        }
        writer.close();
        readerConnection.close();
        sc.close();
        connection.close();
        System.out.println("Connection closed");
    }
}