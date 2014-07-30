import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

/**
 * Created by gkartashevskyy on 7/28/2014.
 */
public class ClientConnection extends Thread {
    private ObjectOutputStream messageOut;       //Stream for messages to client
    private ObjectInputStream messageIn;         //Stream for messages from client
    private String clientName;
    private List<ClientConnection> connectedToClients = new ArrayList<ClientConnection>();
    private DimeServer server;
    private Socket socket;
    static Logger log = Logger.getLogger("ClientConnection");





    public ClientConnection(Socket clientSocket, DimeServer server) throws IOException {
        super();
        this.server = server;
        socket = clientSocket;
        messageOut = new ObjectOutputStream(socket.getOutputStream());
        messageIn = new ObjectInputStream(socket.getInputStream());
        messageOut.flush();
        log.info("Constructed");

    }

    @Override
    public void run() {

        try {
            enterClientName();

            server.addClient(this);
            this.setName("Thread " + clientName);
            chat();
        } catch (IOException e) {
            log.info("Session terminated");
        }
        finally {
            close();

        }
    }

    public String getClientName() {
        return clientName;
    }

    private void enterClientName() throws IOException {

        do {
            sendMessageToClient("Input your NickName");
            clientName = getMessage();


        } while (server.connectedClientsNames().contains(clientName));


    }

    //Server communication with client
    public void sendMessageToClient(String ownMessage) {
        try {
            messageOut.writeObject("Server: " + ownMessage + "\n");
            messageOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getMessage() throws IOException{
        String incomingMessage = "";
        try {
            incomingMessage = (String)messageIn.readObject();
        } catch (EOFException e) {
            log.info("Session is over" + e);
            this.interrupt();
        } catch (ClassNotFoundException e) {
            log.info("Wrong class");
        }

        return incomingMessage;
    }

    private void chat() throws IOException {
        String message = "";




            while (!this.isInterrupted()) {
                message = getMessage();


                if (message.length() >= 6 && message.substring(0, 6).equals("server")) {
                    log.info("Chat Server");
                    serviceMessage(message);
                } else {
                    sendMessage(message);
                }


            }


    }

    public ObjectOutputStream getOutputStream() {
        return messageOut;
    }

    //Sending message to send list
    private void sendMessage(String outMessage){
        if (connectedToClients.size() == 0) {
            if (!socket.isClosed()) {
                sendMessageToClient("You are alone in this world. Add someone");
            }
            return;
        }

        server.addMessageToDB(clientName, outMessage);



        for (ClientConnection client : connectedToClients) {
            try {


                    client.getOutputStream().writeObject(clientName + ": " + outMessage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    public void sendListOfClients() {
        log.info("sendListOfClients");
        Set<String> listOfClients = new HashSet<String>(server.connectedClientsNames());
        listOfClients.remove(clientName);

        String[] clients = new String[listOfClients.size()];
        listOfClients.toArray(clients);
        try {
            messageOut.writeObject(clients);
        } catch (IOException e) {
            e.printStackTrace();
        }
//
    }

    //Add participant to send list
    private void addParticipantByName(String name) {
        ClientConnection client = server.getClientByName(name);

        if (client == null) {
            sendMessageToClient("There is no user with this ID");
            return;
        }

        log.info("Add " + client);
        connectedToClients.add(client);
        sendMessageToClient(name + " added to send list");
        server.addClientConnectionChangeToDB(clientName, name, DBManager.CLIENT_SENDTO_ADD);



    }

    private void close() {
        try {
            log.info("Cleaning");
            messageIn.close();
            messageOut.close();
            socket.close();
            server.removeByName(clientName);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ClientConnection> getConnectedClients() {
        return connectedToClients;
    }

    private void serviceMessage(String message) {
        if (message.contains("add")) {
            addParticipantByName(message.substring(11));

        } else if (message.contains("exit")) {
            this.interrupt();
        } else if (message.contains("delete")) {
            log.info("Delete " + message);
            removeParticipantByName(message.substring(14));
        }

    }

    private void removeParticipantByName(String user) {

        connectedToClients.remove(server.getClientByName(user));
        server.addClientConnectionChangeToDB(clientName, user, DBManager.CLIENT_SENDTO_DELETED);
    }

}
