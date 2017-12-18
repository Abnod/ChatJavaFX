package abnod.chaterr.server;

import java.sql.*;

class DBHandler {

    private Connection connection;
    private ClientHandler clientHandler;
    private PreparedStatement statement;
    private Server server;

    DBHandler(Server server, ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.server = server;
    }

    void connect(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jetbrains3?user=root&password=root");
            statement = connection.prepareStatement("SELECT password, nick FROM users WHERE login = ?;");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection error");
            e.printStackTrace();
        }
    }

    String getUserPassword(String login, String password) {
        ResultSet rs;
        try {
            statement.setString(1, login);
            rs = statement.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).equals(password)) {
                    if (server.isNickNameNotInUse(rs.getString(2))) {
                        clientHandler.setNickName(rs.getString(2));
                        return "ok";
                    } else {
                        return "busy";
                    }
                } else {
                    return "password";
                }
            } else {
                return "notexist";
            }
        } catch (SQLException e) {
            System.out.println("ex1");
            e.printStackTrace();
        }
        return null;
    }

    void close(){
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("database closing error");
        }
    }
}
