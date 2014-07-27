import javax.swing.*;

/**
 * Created by Gennadiy on 27.07.2014.
 */
public class UseServer {
    public static void main(String[] args) {
        DimeServer dimeServer = new DimeServer();
        dimeServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dimeServer.startServer();

    }
}
