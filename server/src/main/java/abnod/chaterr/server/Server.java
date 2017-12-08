package abnod.chaterr.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

class Server {

    private Vector<ClientHandler> clients;

    Server(){

        try(ServerSocket serverSocket = new ServerSocket(8189)){
            clients = new Vector<>();
            while (true){
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            System.out.println("socket exception");
            e.printStackTrace();
        }
    }

    void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastMessage(clientHandler.getNickName() + " joined chat", "Server");
        for(ClientHandler clientHandler1 : clients){
            if (clientHandler1.getNickName().equals(clientHandler.getNickName())){
                broadcastMessage(clientHandler.getNickName(), "/usradd");
            } else {clientHandler.sendMessage("/usradd: " + clientHandler1.getNickName());}
        }
    }

    void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastMessage(clientHandler.getNickName(), "/usrrmv");
        broadcastMessage(clientHandler.getNickName() + " leave chat", "Server");
    }

    void broadcastMessage(String message, String senderNickName){
        String msg = senderNickName + ": " + message;
        for (ClientHandler client : clients){
            client.sendMessage(msg);
        }
    }

    void sendPrivateMessage (String message, ClientHandler sender){
        String[] msg = message.split(" ");
        String text = message.substring(message.indexOf(msg[2]));
        boolean privateSent = false;
        for (ClientHandler client : clients){
            if(client.getNickName().equals(msg[1])){
                client.sendMessage(sender.getNickName() + " whispers to you: " + text);
                privateSent = true;
            }
            if(privateSent){
                sender.sendMessage("You whispers to " + client.getNickName() + ": " + text);
                break;
            }
        }
    }

    void close(){
        broadcastMessage("Server Shutdown", "Server");
        System.exit(0);
    }

    boolean isNickNameNotInUse(String name){
        for (ClientHandler clientHandler : clients){
            if (name.equals(clientHandler.getNickName())){
                return false;
            }
        }
        return true;
    }
}
