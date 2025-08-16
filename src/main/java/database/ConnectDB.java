package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    private static  final String DB_URL = "jdbc:sqlite:mydb.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if(connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    public static void closeConnection() throws SQLException {
        if(connection != null && !connection.isClosed()) {
            connection.close();
        }else {
            System.out.println("Connection closed failed");
        }
    }



}
