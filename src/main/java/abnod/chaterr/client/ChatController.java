package abnod.chaterr.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    private final String SERVER_IP = "10.0.0.58";
    private final int SERVER_PORT = 8189;
    @FXML
    private TextField inputField;
    @FXML
    private ListView <String> chatWindow;
    @FXML
    private ListView<String> userWindow;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox textBox;
    @FXML
    private HBox authBox;
    private ObservableList<String> chatMessages = FXCollections.observableArrayList();
    private ObservableList<String> userList = FXCollections.observableArrayList();
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean autorized;

    public void sendMessage() throws IOException {
        if (!inputField.getText().equals("")){
            outputStream.writeUTF(inputField.getText());
            inputField.clear();
            inputField.requestFocus();
        }
    }

    public void sendAuth() {
        try {
            connect();
            outputStream.writeUTF("/autho " + loginField.getText() + " " + passwordField.getText());
        } catch (IOException e) {
            viewMessage("Server not available");
        }
    }

    private void authorize() {
        if (autorized) {
            textBox.setVisible(true);
            authBox.setVisible(false);
        } else {
            textBox.setVisible(false);
            authBox.setVisible(true);
        }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chatWindow.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(final ListView<String> list) {
                return new ListCell<String>() {
                    {
                        Text text = new Text();
                        text.wrappingWidthProperty().bind(list.widthProperty().subtract(15));
                        text.textProperty().bind(itemProperty());

                        setPrefWidth(0);
                        setGraphic(text);
                    }
                };
            }
        });
    }

    private void connect() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());

        Thread inputThread = new Thread(() -> {
            try {
                chatWindow.setItems(chatMessages);
                userWindow.setItems(userList);
                while (true) {
                    String s = inputStream.readUTF();
                    switch (s) {
                        case "/authok": {
                            autorized = true;
                            viewMessage("Server: Login successful");
                            authorize();
                            break;
                        }
                        case "/authpassword": {
                            viewMessage("Server: Wrong password");
                            break;
                        }
                        case "/authbusy": {
                            viewMessage("Server: Account already in use");
                            break;
                        }
                        case "/authnotexist": {
                            viewMessage("Server: User not exist");
                            break;
                        }
                    }
                    if (autorized) {
                        break;
                    }
                }
                while (true) {
                    String s = inputStream.readUTF();
                    if(s.startsWith("/usradd")){
                        String usradd = s.substring(9);
                        usersAdd(usradd);
                    } else if (s.startsWith("/usrrmv")){
                        String usrrmv = s.substring(9);
                        usersRemove(usrrmv);
                    } else {viewMessage(s);}
                }
            } catch (IOException e) {
                viewMessage("Connection to server lost");
            } finally {
                autorized = false;
                authorize();
                userList.clear();
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("ex2");
                    e.printStackTrace();
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private void viewMessage(String s) {
        Platform.runLater(() -> chatMessages.add(s));
//        chatWindow.setItems(chatMessages);
    }

    private void usersAdd(String s) {
        if(!userList.contains(s)){
            Platform.runLater(() -> userList.add(s));
        }
    }

    private void usersRemove(String s) {
        Platform.runLater(() -> userList.remove(s));
    }

    public void close() {
        System.exit(0);
    }
}
