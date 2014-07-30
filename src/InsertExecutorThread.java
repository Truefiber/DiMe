import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;

/**
 * Created by gkartashevskyy on 7/30/2014.
 */
public class InsertExecutorThread extends Thread {

    private BlockingQueue<String> insertQueue;
    private Statement dbStatement;

    static Logger log = Logger.getLogger("InsertExecutorThread");

    public InsertExecutorThread(BlockingQueue<String> queue, Statement statement) {
        super("Insert Thread");
        insertQueue = queue;
        dbStatement = statement;
    }

    @Override
    public void run() {

        while (!this.isInterrupted()) {
            try {
                String nextQuery = insertQueue.take();
                executeInsert(nextQuery);
            } catch (InterruptedException e) {
                log.info("Taking from BlockingQueue interrupted " + e);
            }
        }
    }

    private void executeInsert(String insert) {
        try {
            dbStatement.execute(insert);
        } catch (SQLException e) {
            log.info("Insertion failed, insert " + insert + ", exception " + e);
        }


    }
}
