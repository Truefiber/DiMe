import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Gennadiy on 27.07.2014.
 */
public class DimeServer extends JFrame {

    private JTextField inputField;
    private JTextArea conversationArea;
    private ObjectOutputStream messageOutput;
    private ObjectInputStream messageInput;
    private ServerSocket server;
    private Socket connection;

    public DimeServer() {
        super("Dio Messenger");
        inputField = new JTextField();
        inputField.setEditable(false);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());              //Text of message is used as the command string for the action event
                inputField.setText("");

            }
        });
        add(inputField, BorderLayout.SOUTH);
        conversationArea = new JTextArea();
        add(new JScrollPane(conversationArea));
        setSize(400, 400);
        setVisible(true);
    }

    public void startServer() {
        try{
            server = new ServerSocket(6789, 100);
            while (true) {
                try {
                    establishConnection();
                    raiseStreams();
                    chat();
                } catch (EOFException e) {
                    showMessage("Session is over");
                } finally {
                    cleanUp();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void establishConnection() throws IOException{
        showMessage("Wait a few seconds");
        connection = server.accept();
        showMessage("Connected to " + connection.getInetAddress().getHostName());
    }

    private void raiseStreams() throws IOException{
        messageOutput = new ObjectOutputStream(connection.getOutputStream());
        messageInput = new ObjectInputStream(connection.getInputStream());
        messageOutput.flush();
        showMessage("Streams are ready");
    }

    private void chat() throws IOException{
        String message = "You can chat";
        sendMessage(message);
        inputField.setEditable(true);

        while (!message.equals("Exit")) {
            try {
                message = (String) messageInput.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException e) {
                showMessage("Wrong class");
            }

        }
    }

    private void cleanUp() {
        showMessage("Cleaning garbage");
        inputField.setEditable(false);
        try {
            messageOutput.close();
            messageInput.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            messageOutput.writeObject(message);
            messageOutput.flush();
            showMessage("\n" + message);
        } catch (IOException e) {
            showMessage("Error while sending message");
        }
    }

    private void showMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                conversationArea.append(message);

            }
        });


    }




}
