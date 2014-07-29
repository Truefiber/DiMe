import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Gennadiy on 27.07.2014.
 */
public class DimeServer {


    private ObjectOutputStream messageOutput;
    private ObjectInputStream messageInput;
    private ServerSocket server;
    private Socket connection;



    private Map<String, ClientConnection> mapOfConnectedClients = new HashMap<String, ClientConnection>();
    static Logger log = Logger.getLogger("DimeServer");



    public void startServer(int portNumber) {


        try{
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
        log.debug("Added client " + client.getClientName());

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




}
