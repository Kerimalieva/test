import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.mysql.cj.conf.PropertyKey.PASSWORD;


public class DatabaseConnection{
    public static final String URL = "jdbc:mysql://localhost:3306/customer_management";
    public static final String USER = "root";
    public static final String PASSWORD = "Zarina0708.210104075";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


}