package abnod.chaterr.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private Connection connection;
    private PreparedStatement statement;
    private String nickName;

    public ClientHandler(Socket socket, Server server){
        try {
            this.socket = socket;
            this.server = server;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            Thread thread = new Thread(()-> {
               try{
                   connectDB();
                   while (true){
                       String message = inputStream.readUTF();
                       if(message.startsWith("/autho")){
                           String[] auth = message.split(" ");
                           String pong = getUserPassword(auth[1], auth[2]);
                           sendMessage(pong);
                           if(pong.equals("/authok")){
                               break;
                           }
                       }
                   }
                   server.subscribe(this);
                   closeDB();
                   while (true){
                       String message = inputStream.readUTF();
                       if (message.equals("/shutdown")){server.close();}
                       else if (message.startsWith("/w ")){server.sendPrivateMessage(message, this);}
                       else {server.broadcastMessage(message, getNickName());}
                   }
               } catch (IOException e) {
                   System.out.println("client lost connection");
               }finally{
                   server.unsubscribe(this);
                   try {
                       socket.close();
                   } catch (IOException e) {
                       System.out.println("can't close socket");
                   }
               }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            System.out.println("client handler error");
        }
    }

    public void sendMessage(String message){
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            System.out.println("ex4");
            e.printStackTrace();
        }
    }

    public String getUserPassword(String login, String password){
        ResultSet rs;
        try {
            statement.setString(1, login);
            rs = statement.executeQuery();
            if (rs.next()){
                if(rs.getString(1).equals(password)){
                    if(server.isNickNameNotInUse(rs.getString(2))){
                        nickName = rs.getString(2);
                        return "/authok";
                    } else {return "/authbusy";}
                } else {return "/authpassword";}
            } else {return "/authnotexist";}
        } catch (SQLException e) {
            System.out.println("ex1");
            e.printStackTrace();
        }
        return null;
    }

    public void connectDB(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jetbrains3?user=root&password=root");
            statement = connection.prepareStatement("SELECT password, nick FROM users WHERE login = ?;");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection error");
            e.printStackTrace();
        }
    }

    public void closeDB(){
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("database closing error");
        }
    }

    public String getNickName() {
        return nickName;
    }

}
