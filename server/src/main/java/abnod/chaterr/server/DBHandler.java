package abnod.chaterr.server;

import java.sql.*;

class DBHandler {

    private Connection connection;
    private ClientHandler clientHandler;
    private PreparedStatement statementLogin;
    private PreparedStatement statementRegister;
    private Server server;

    DBHandler(Server server, ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.server = server;
    }

    void connect(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jetbrains3?user=root&password=root");
            statementLogin = connection.prepareStatement("SELECT password, nick FROM users WHERE login = ?;");
            statementRegister = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUES (?, ?, ?);");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection error");
            e.printStackTrace();
        }
    }

    String getUserPassword(String login, String password) {
        ResultSet rs;
        try {
            statementLogin.setString(1, login);
            rs = statementLogin.executeQuery();
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
        return "error";
    }

    String registerUser(String login, String password, String nick){
        try {
            statementRegister.setString(1, login);
            statementRegister.setString(2, password);
            statementRegister.setString(3, nick);
            if (statementRegister.executeUpdate() > 0){
                clientHandler.setNickName(nick);
                return "ok";
            }
        } catch (SQLIntegrityConstraintViolationException e){
            String msg = e.getMessage();
            msg = msg.substring(msg.indexOf("key '")+5, msg.length()-1);
            if (msg.equalsIgnoreCase("primary")){
                return "login";
            } else if (msg.equalsIgnoreCase("nick")){
                return "nick";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "error";
    }

    void close(){
        try {
            statementLogin.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("database closing error");
        }
    }
}
