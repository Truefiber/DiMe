import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Gennadiy on 27.07.2014.
 */
public class DimeServer {


    private BlockingQueue<String> insertQueryQueue = new ArrayBlockingQueue<String>(1000);
    private Statement dimeStatement;

    private InsertExecutorThread insertThread;

    private ServerSocket server;
    private Socket connection;



    private Map<String, ClientConnection> mapOfConnectedClients = new HashMap<String, ClientConnection>();
    static Logger log = Logger.getLogger("DimeServer");



    public void startServer(int portNumber) {


        try{

            dimeStatement = DBManager.getDBStatement();
            insertThread = new InsertExecutorThread(insertQueryQueue, dimeStatement);
            insertThread.start();
            server = new ServerSocket(portNumber, 100);
            log.info("Server started");
            while (true) {

                    establishConnection();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public synchronized void addClient(ClientConnection client) {

        mapOfConnectedClients.put(client.getClientName(), client);
        updateListOfClients();
        log.info("Added client " + client.getClientName());

        insertQueryQueue.add(DBManager.getServerInsert(client.getClientName(),
                DBManager.SERVER_CLIENT_JOINED));

    }

    public Set<String> connectedClientsNames() {
        return mapOfConnectedClients.keySet();
    }

    public ClientConnection getClientByName(String name) {
        return mapOfConnectedClients.get(name);
    }


    //Remove this client from connected clients and ensure other clients will not send anything to
    // this client
    public synchronized void removeByName(String name) {

        ClientConnection deletedClient = mapOfConnectedClients.remove(name);

        insertQueryQueue.add(DBManager.getServerInsert(name,
                DBManager.SERVER_CLIENT_LEFT));

        for (Map.Entry<String, ClientConnection> entry : mapOfConnectedClients.entrySet()) {
            ClientConnection nextClient = entry.getValue();
            nextClient.sendListOfClients();
            List<ClientConnection> list = nextClient.getConnectedClients();
            if (list.remove(deletedClient)) {
                nextClient.sendMessageToClient(deletedClient.getClientName() + " disconnected");
            }

        }



    }


    private void establishConnection() throws IOException{
        connection = server.accept();
        new ClientConnection(connection, this).start();

    }

    private void updateListOfClients() {
        for (Map.Entry<String, ClientConnection> entry : mapOfConnectedClients.entrySet()) {
            ClientConnection nextClient = entry.getValue();
            nextClient.sendListOfClients();

        }
    }

    //Update DB with changes in SendTo list of client
    public void addClientConnectionChangeToDB(String client, String sendTo,
                                             boolean isAdded) {

        insertQueryQueue.add(DBManager.getClientInsert(client, sendTo,
                isAdded));


    }

    //Add every outgoing message to db except service message
    //If sendto list is empty, message as considered as not sent and will not be inserted in db
    public void addMessageToDB(String client, String message) {

        insertQueryQueue.add(DBManager.getMessageInsert(client, message));

    }










}
