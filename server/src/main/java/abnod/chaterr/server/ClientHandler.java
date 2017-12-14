package abnod.chaterr.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler {
    private Server server;
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String nickName;
    private boolean autorized = false;
    private DBHandler dbHandler;

    ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            dbHandler = new DBHandler(server, this);

            Thread threadDisconnectTimer = new Thread(()->{
                try {
                    Thread.sleep(120000);
                    if (!autorized){
                        socket.close();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("socket already closed");
                }
            });
            threadDisconnectTimer.setDaemon(true);
            threadDisconnectTimer.start();

            Thread thread = new Thread(() -> {
                try {
                    dbHandler.connect();
                    while (true) {
                        String message = inputStream.readUTF();
                        if (message.startsWith("/autho")) {
                            String[] auth = message.split(" ");
                            String pong = dbHandler.getUserPassword(auth[1], auth[2]);
                            sendMessage(pong);
                            sendMessage(getNickName());
                            if (pong.equals("/authok")) {
                                autorized = true;
                                break;
                            }
                        }
                    }
                    server.subscribe(this);
                    dbHandler.close();
                    while (true) {
                        String message = inputStream.readUTF();
                        if (message.equals("/shutdown")) {
                            server.close();
                        } else if (message.startsWith("/w ")) {
                            server.sendPrivateMessage(message, this);
                        } else {
                            server.broadcastMessage(message, getNickName());
                        }
                    }
                } catch (IOException e) {
                    System.out.println("client lost connection");
                } finally {
                    if (autorized){
                        server.unsubscribe(this);
                    }
                    try {
                        socket.close();
                        inputStream.close();
                        outputStream.close();
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

    void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            System.out.println("ex4");
            e.printStackTrace();
        }
    }

    String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
