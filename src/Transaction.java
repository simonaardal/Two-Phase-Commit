import java.io.Serializable;


/**
 * Transaction object used to send between Coordinator and Participant
 */
public class Transaction implements Serializable {
    public String id;
    public int value;

    /**
     *
     * @param id Transaction ID
     * @param value Transaction Value, can be both negative and positive
     */
    public Transaction(String id, int value) {
        this.id = id;
        this.value = value;
    }

    /**
     * @return String with id and value
     */
    @Override
    public String toString() {
        return "Transaction id: " + id + " | value: " + value;
    }
}
