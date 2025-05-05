package bd.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection connect() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/InformationRetrieval"; // Placeholder for the database URL
        String user = "postgres"; // Placeholder for the username
        String password = "abc123=0"; // Placeholder for the password
        return DriverManager.getConnection(url, user, password);
    }
}
