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

    private void sendMessageToClient(String ownMessage) {
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




            while (!message.equals("Exit") && !this.isInterrupted()) {
                message = getMessage();


                if (message.length() >= 6 && message.substring(0, 6).equals("server")) {
                    serviceMessage(message);
                } else {
                    sendMessage(message);
                }


            }


    }

    public ObjectOutputStream getOutputStream() {
        return messageOut;
    }

    private void sendMessage(String outMessage){
        if (connectedToClients.size() == 0) {
            if (!socket.isClosed()) {
                sendMessageToClient("You are alone in this world. Add someone");
            }
            return;
        }



        for (ClientConnection client : connectedToClients) {
            try {


                    client.getOutputStream().writeObject(clientName + ": " + outMessage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    private void sendListOfClients() {
        log.info("sendListOfClients");
        Set<String> listOfClients = new HashSet<String>(server.connectedClientsNames());
        listOfClients.remove(clientName);
        sendMessageToClient("List of clients:");
        for (String client : listOfClients) {
            sendMessageToClient(client);
        }
    }

    private void addParticipantByName(String name) {
        ClientConnection client = server.getClientByName(name);

        if (client == null) {
            sendMessageToClient("There is no user with this ID");
            return;
        }

        log.info("Add " + client);
        connectedToClients.add(client);


    }

    private void close() {
        try {
            log.info("Cleaning");
            messageIn.close();
            messageOut.close();
            socket.close();
            sendMessage(clientName + " disconnected");
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
        } else if (message.contains("list")) {
            sendListOfClients();
        }

    }

}
