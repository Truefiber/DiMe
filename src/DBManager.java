import org.apache.log4j.Logger;

import java.sql.*;
import java.util.Date;

/**
 * Created by gkartashevskyy on 7/30/2014.
 */
public class DBManager {

    private static Statement st = null;
    private static Connection dbConnection;
    private static String url = "jdbc:postgresql://localhost:5432/dimeDB";
    private static String user = "postgres";
    private static String password = "56TGhhj&";

    private static final String SERVER_CONNECTIONS_TABLE = "serverconnections";
    private static final String CLIENT_CONNECTIONS_TABLE = "clientconnections";
    private static final String MESSAGES_TABLE = "messages";

    private static final String STATUS_FIELD = "status";
    private static final String CLIENT_FIELD = "client";
    private static final String SENDTO_FIELD = "sendtoclient";
    private static final String TIME_FIELD = "event_time";
    private static final String MESSAGE_FIELD = "message";

    public static final boolean SERVER_CLIENT_JOINED = true;
    public static final boolean SERVER_CLIENT_LEFT = false;

    public static final boolean CLIENT_SENDTO_ADD = true;
    public static final boolean CLIENT_SENDTO_DELETED = false;


    static Logger log = Logger.getLogger("DBConnector");

    public static Statement getDBStatement() {



        try {
            dbConnection = DriverManager.getConnection(url, user, password);
            st = dbConnection.createStatement();
        } catch (SQLException e) {
            log.info("DB Connection failed " + e);
        }

        log.info("DB Connection succeed");

        return st;

    }

    public static String getServerInsert(String client, boolean isJoined) {

        String insert = "INSERT INTO " + SERVER_CONNECTIONS_TABLE + "(" +
                TIME_FIELD + ", " + CLIENT_FIELD + ", " + STATUS_FIELD + ") " +
                "VALUES ('" + new Timestamp(new Date().getTime()) + "', '" + client + "', " +
                "" + isJoined + ")";

        log.info(insert);


        return insert;
    }

    public static String getClientInsert(String client, String sendto, boolean isAdded) {

        String insert = "INSERT INTO " + CLIENT_CONNECTIONS_TABLE + "(" +
                TIME_FIELD + ", " + CLIENT_FIELD + ", " + SENDTO_FIELD + ", " +
                "" + STATUS_FIELD + ") " +
                "VALUES ('" + new Timestamp(new Date().getTime()) + "', '" + client + "', '" +
                "" + sendto + "', " + isAdded + ")";





        return insert;

    }

    public static String getMessageInsert(String client, String message) {
        String insert = "INSERT INTO " + MESSAGES_TABLE + "(" +
                TIME_FIELD + ", " + CLIENT_FIELD + ", " + MESSAGE_FIELD + ") " +
                "VALUES ('" + new Timestamp(new Date().getTime()) + "', '" + client + "', '" +
                "" + message + "')";

        return insert;

    }
}
