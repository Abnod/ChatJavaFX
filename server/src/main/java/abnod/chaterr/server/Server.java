package abnod.chaterr.server;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

class Server {

    private Vector<ClientHandler> clients;
    JSONObject jsonObject;

    Server() {

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            clients = new Vector<>();

            System.out.println("Server launched.");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
                System.out.printf("client with ip %s connected", socket.getInetAddress().getHostAddress());
                System.out.println();
            }
        } catch (IOException e) {
            System.out.println("socket exception");
            e.printStackTrace();
        }
    }

    void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage(clientHandler.getNickName() + " joined chat", "Server");

        for (ClientHandler clientHandlerLoop : clients) {
            jsonObject = new JSONObject();
            jsonObject.put("type", "addUser");
            if (clientHandlerLoop.getNickName().equals(clientHandler.getNickName())) {
                jsonObject.put("nickName", clientHandler.getNickName());
                broadcastServiceMessage(jsonObject);
            } else {
                jsonObject.put("nickName", clientHandlerLoop.getNickName());
                clientHandler.sendMessage(jsonObject);
            }
        }
    }

    void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        jsonObject = new JSONObject();
        jsonObject.put("type", "rmvUser");
        jsonObject.put("nickName", clientHandler.getNickName());
        broadcastServiceMessage(jsonObject);
        broadcastMessage(clientHandler.getNickName() + " leave chat", "Server");
    }

    private void broadcastServiceMessage(JSONObject jsonObject) {
        for (ClientHandler client : clients) {
            client.sendMessage(jsonObject);
        }
    }

    void broadcastMessage(String message, String senderNickName) {
        jsonObject = new JSONObject();
        for (ClientHandler client : clients) {
            jsonObject.put("type", "message");
            jsonObject.put("sender", senderNickName);
            jsonObject.put("message", message);
            client.sendMessage(jsonObject);
        }
    }

    void sendPrivateMessage(String message, ClientHandler sender, String nameTo) {
        boolean privateSent = false;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "message");
        jsonObject.put("message", message);
        for (ClientHandler client : clients) {
            if (client.getNickName().equals(nameTo)) {
                jsonObject.put("sender", sender.getNickName() + " whispers to you");
                client.sendMessage(jsonObject);
                privateSent = true;
            }
            if (privateSent) {
                jsonObject.put("sender", "You whispers to " + client.getNickName());
                sender.sendMessage(jsonObject);
                break;
            }
        }
        if (!privateSent) {
            jsonObject.put("message", "message recipient not found");
            jsonObject.put("sender", "Server");
            sender.sendMessage(jsonObject);
        }
    }

    void close() {
        broadcastMessage("Server Shutdown", "Server");
        System.exit(0);
    }

    boolean isNickNameNotInUse(String name) {
        for (ClientHandler clientHandler : clients) {
            if (name.equals(clientHandler.getNickName())) {
                return false;
            }
        }
        return true;
    }
}
