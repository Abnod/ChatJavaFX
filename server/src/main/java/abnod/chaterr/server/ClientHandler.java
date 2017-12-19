package abnod.chaterr.server;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;

class ClientHandler {
    private Server server;
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String nickName;
    private boolean autorized = false;
    private DBHandler dbHandler;
    private JSONObject jsonObject = new JSONObject();

    ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            dbHandler = new DBHandler(server, this);

            Thread threadDisconnectTimer = new Thread(() -> {
                try {
                    Thread.sleep(120000);
                    if (!autorized) {
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
                        jsonObject = (JSONObject) inputStream.readObject();
                        String type = (String) jsonObject.get("type");

                        if (type.equals("login")) {
                            String login = (String) jsonObject.get("login");
                            String password = (String) jsonObject.get("password");
                            jsonObject.clear();

                            String pong = dbHandler.getUserPassword(login, password);
                            jsonObject.put("auth", pong);
                            jsonObject.put("nickName", getNickName());
                            sendMessage(jsonObject);

                            if (pong.equals("ok")) {
                                autorized = true;
                                break;
                            }
                        } else if (type.equals("register")) {
                            //todo
                        }
                    }
                    server.subscribe(this);
                    dbHandler.close();

                    while (true) {
                        jsonObject = (JSONObject) inputStream.readObject();
                        System.out.println(jsonObject);
                        String type = (String) jsonObject.get("type");
                        switch (type) {
                            case "shutdown":
                                server.close();
                                break;
                            case "whisper": {
                                String nameTo = (String) jsonObject.get("to");
                                String message = (String) jsonObject.get("message");
                                server.sendPrivateMessage(message, this, nameTo);
                                break;
                            }
                            case "message": {
                                String message = (String) jsonObject.get("message");
                                server.broadcastMessage(message, getNickName());
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("client lost connection");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (autorized) {
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

    void sendMessage(JSONObject message) {
        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("ex4");
            e.printStackTrace();
        }
    }

    String getNickName() {
        return nickName;
    }

    void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
