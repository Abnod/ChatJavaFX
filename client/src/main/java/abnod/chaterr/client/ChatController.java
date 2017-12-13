package abnod.chaterr.client;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
    private TextField inputField, loginField;
    @FXML
    private ListView<String> chatWindow, userWindow;
    @FXML
    private PasswordField passwordField;
    @FXML
    private VBox textBox, authBox;
    private ObservableList<String> chatMessages, userList;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private FadeTransition ft;

    private boolean autorized;

    //todo
    // login(transp)-hello(swipe)-chat

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chatWindow.setCellFactory(list -> new ListCell<String>() {
            {
                Text text = new Text();
                text.wrappingWidthProperty().bind(list.widthProperty().subtract(15));
                text.textProperty().bind(itemProperty());

                setPrefWidth(0);
                setGraphic(text);
            }
        });
        chatMessages = FXCollections.observableArrayList();
        userList = FXCollections.observableArrayList();
        chatWindow.setItems(chatMessages);
        try {
            connect();
        } catch (IOException e) {
            viewMessage("server not found");
        }

        ft = new FadeTransition();
        ft.setNode(authBox);
        ft.setDuration(new Duration(500));
        ft.setFromValue(0.5);
        ft.setToValue(0.0);
        ft.setCycleCount(1);
        ft.setAutoReverse(true);
        ft.setOnFinished(event -> {
            if (authBox.isVisible()) {
                authBox.setVisible(false);
                textBox.setDisable(false);
            }
        });
    }

    public void sendMessage() throws IOException {
        if (!inputField.getText().equals("")) {
            outputStream.writeUTF(inputField.getText());
            inputField.clear();
            inputField.requestFocus();
        }
    }

    public void sendAuth() {
        try {
            if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
                if (socket.isClosed()) {
                    connect();
                }
                outputStream.writeUTF("/autho " + loginField.getText() + " " + passwordField.getText());
            } else {
                viewMessage("login and password fields cannot be empty");
            }
        } catch (IOException e) {
            viewMessage("Server not available");
        }
    }

    private void authorize() {
        if (autorized) {
            ft.play();
        } else {
            authBox.setOpacity(1);
            textBox.setDisable(true);
            authBox.setVisible(true);
        }

    }

    private void connect() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        Thread inputThread = new Thread(() -> {
            try {
                userWindow.setItems(userList);
                while (true) {
                    String s = inputStream.readUTF();
                    switch (s) {
                        case "/authok": {
                            autorized = true;
                            viewMessage("Server: Login successful");
                            viewMessage("Server: for private message write: /w nickname message");
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
                    if (s.startsWith("/usradd")) {
                        String usradd = s.substring(9);
                        usersAdd(usradd);
                    } else if (s.startsWith("/usrrmv")) {
                        String usrrmv = s.substring(9);
                        usersRemove(usrrmv);
                    } else {
                        viewMessage(s);
                    }
                }
            } catch (IOException e) {
                viewMessage("Connection to server lost");
            } finally {
                autorized = false;
                authorize();
                Platform.runLater(() -> userList.clear());
                try {
                    socket.close();
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    System.out.println("cannot close socket");
                    e.printStackTrace();
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private void viewMessage(String s) {
        Platform.runLater(() -> chatMessages.add(s));
    }

    private void usersAdd(String s) {
        if (!userList.contains(s)) {
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
