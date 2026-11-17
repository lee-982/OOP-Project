import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens the JDBC connection to MySQL. Change USER/PASSWORD to match your setup. */
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/chama_db";
    private static final String USER = "root";
    private static final String PASSWORD = "@.rchitecture!";   // <-- put your MySQL password here

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
