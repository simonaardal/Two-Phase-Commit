import java.util.UUID;

/**
 * ResourceManager for a single Participant, mimics a database
 */
public class ResourceManager {
    String id;
    double balance;
    double previous_balance;

    /**
     * Generates ResourceManager with UUID
     *
     * @param balance Amount of "money" the ResourceManager holds
     */
    public ResourceManager(double balance) {
        this.id = UUID.randomUUID().toString();
        this.balance = balance;
    }

    /**
     * Adds amount to balance
     *
     * @param amount Amount of "money" to add
     * @return boolean based if transaction went through
     */
    public boolean add(double amount) {
        if (balance + amount < 0) {
            return false;
        } else {
            previous_balance = balance;
            balance += amount;
            return true;
        }
    }

    /**
     *  Roll back to previous balance
     */
    public void rollback() {
        balance = previous_balance;
    }

    /**
     * @return Custom string with id and balance
     */
    public String toString() {
        return "RM ID: " + id + " | " + "balance: " + balance;
    }
}
