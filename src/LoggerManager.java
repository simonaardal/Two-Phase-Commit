import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * A utils-class containing methods to write to file, create file etc
 */
public class LoggerManager {
    File file;

    /**
     * Constructor of LoggerManager
     *
     * @param coordinator boolean indicate if the class associated with the logger-manager is a coordinator or not
     */
    public LoggerManager(boolean coordinator) {
        this.file = createFile(coordinator);
    }

    /**
     * Method to write to the file created
     *
     * @param msg The message to be written to the file
     * @return A boolean indicating if the process was a success or not
     */
    public boolean writeToFile(String msg) {
        try {
            FileWriter myWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(myWriter);
            bufferedWriter.write(java.time.LocalTime.now() + " " + new Date().toString() + " " + msg + "\n");
            bufferedWriter.close();
            myWriter.close();
            return true;
        } catch (IOException e) {
            System.out.println("Error: Failed to write to file");
            return false;
        }
    }

    /**
     * Creating a file in the folder
     *
     * @param coordinator Gives the file a specic start of the filename
     * @return The file (object) created
     */
    public File createFile(boolean coordinator) {
        try {
            File file = new File("log/" + (coordinator ? "coordinator" : "participant") + "-" + UUID.randomUUID().toString() + ".txt");
            if (file.createNewFile()) {
                System.out.println("Log created for " + (coordinator ? "coordinator: " : "participant: ") + file.getName());
            } else {
                System.out.println("File already exists.");
            }
            return file;
        } catch (IOException e) {
            System.out.println("Error: Failed in creation of file");
        }
        return null;
    }

    /**
     * Clear all files in the specific directory
     */
    public static void clearFiles() {
        Arrays.stream(Objects.requireNonNull(new File("log/").listFiles())).forEach(File::delete);
    }
}
