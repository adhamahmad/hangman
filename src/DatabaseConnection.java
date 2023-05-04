import java.net.Socket;
import java.sql.*;

public class DatabaseConnection {
            
    String url = "jdbc:mysql://localhost:3306/hangman";
    String user = "root";
    String dbPassword = "admin";
    
    public int login(String username, String password){
        try {
            // Load the MySQL Connector/J JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(url, user, dbPassword);
            System.out.println("Connected to the database");

            // Execute a query
            PreparedStatement statement = connection.prepareStatement("SELECT * from user WHERE  username = ?");
            statement.setString(1,username);         
            ResultSet resultSet = statement.executeQuery();

            // Process the results
            if (resultSet.next()) {
                // Execute a query
                PreparedStatement statement2 = connection.prepareStatement("SELECT * from user WHERE  username = ? AND password = ?");
                statement2.setString(1,username);   
                statement2.setString(2,password);          
                ResultSet resultSet2 = statement2.executeQuery();
                if (resultSet2.next()){
                     // Close the connection
                     connection.close();
                    return 200;
                }else{
                    connection.close();
                    return 401; //wrong pass
                }
            }else{
                connection.close();
                return 404; //username not found
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    };

    public int register(String username, String password,String name){
        try {
            // Load the MySQL Connector/J JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(url, user, dbPassword);
            System.out.println("Connected to the database");

            // Execute a query
            PreparedStatement statement = connection.prepareStatement("INSERT INTO user (username, name, password, score) VALUES (?, ?, ?, ?)");
            statement.setString(1,username);
            statement.setString(2,name);      
            statement.setString(3,password); 
            statement.setInt(4,0);    
            Integer rowsAffected = statement.executeUpdate();
            if(rowsAffected > 0){ //sucess
                connection.close();
                return 200;
            }else{
                connection.close();
                return 405; // username not unique
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getScore(String username) {
        try {
            // Load the MySQL Connector/J JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(url, user, dbPassword);
            System.out.println("Connected to the database");

            // Execute a query
            PreparedStatement statement = connection.prepareStatement("SELECT score FROM user WHERE username = ?");
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                int score = rs.getInt("score");
                connection.close();
                return score;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void updateScore(int score,String username){
        try {
            // Load the MySQL Connector/J JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(url, user, dbPassword);
            System.out.println("Connected to the database");

            // Execute a query
            PreparedStatement statement = connection.prepareStatement("UPDATE user SET score = ? WHERE username = ?");
            statement.setInt(1,score);
            statement.setString(2,username);
            statement.executeUpdate();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}