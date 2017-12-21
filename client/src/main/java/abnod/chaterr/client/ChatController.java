package abnod.chaterr.client;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    private final String SERVER_IP = "localhost";
    private final int SERVER_PORT = 8189;
    @FXML
    private TextField inputField;
    @FXML
    private ListView<String> chatWindow, userWindow;
    @FXML
    private AnchorPane authBox, textBox;
    @FXML
    private VBox helloBox, changeBox, login, register;
    @FXML
    private Text nickHello;
    @FXML
    private LoginController loginController;
    @FXML
    private RegisterController registerController;
    @FXML
    private Label label;

    private ObservableList<String> chatMessages, userList;

    private Stage stage;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private FadeTransition ft;

    private String nickName;
    private boolean autorized;

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
        createForms();
        setLoginScreen();
        chatMessages = FXCollections.observableArrayList();
        userList = FXCollections.observableArrayList();
        chatWindow.setItems(chatMessages);
        createAnimation();
    }

    public void sendMessage() throws IOException {
        String msg = inputField.getText();
        inputField.clear();
        inputField.requestFocus();
        if (!msg.equals("")) {
            JSONObject jsonObject = new JSONObject();
            //msg.length() > 5 == /w (3 char) + nick(1 char min) + ,(separator) + ' '(1 char min)
            if (msg.startsWith("/w ") && msg.length() > 5 && msg.contains(",")) {
                msg = msg.substring(3); //remove '/w '
                if (msg.length() > msg.indexOf(",")) {
                    String toName = msg.substring(0, msg.indexOf(","));
                    if (toName.equals(nickName)) {
                        viewMessage("you cannot send private message to yourself", "System");
                        return;
                    }
                    jsonObject.put("type", "whisper");
                    jsonObject.put("to", toName);
                    jsonObject.put("message", msg.substring(msg.indexOf(",") + 1));
                }
            } else {
                jsonObject.put("type", "message");
                jsonObject.put("message", msg);
            }
            outputStream.writeObject(jsonObject);
            outputStream.flush();
        }
    }

    public void sendAuth(String login, String password) {
        try {
            if (socket == null || socket.isClosed()) {
                connect();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "login");
            jsonObject.put("login", login);
            jsonObject.put("password", password);
            outputStream.writeObject(jsonObject);
        } catch (IOException e) {
            setLoginText("Server not available");
        }
    }

    public void sendReg(String login, String password, String nick) {
        try {
            if (socket == null || socket.isClosed()) {
                connect();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "register");
            jsonObject.put("login", login);
            jsonObject.put("password", password);
            jsonObject.put("nick", nick);
            outputStream.writeObject(jsonObject);
        } catch (IOException e) {
            setLoginText("Server not available");
        }
    }

    private void authorize() {
        if (autorized) {
            nickHello.setText(nickName);
            ft.play();
        } else {
            authBox.setOpacity(1);
            authBox.setVisible(true);
            helloBox.setTranslateY(0);
            textBox.setDisable(true);
            helloBox.setVisible(true);
            setLoginScreen();
        }
    }

    private void connect() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
        Thread inputThread = new Thread(() -> {
            try {
                userWindow.setItems(userList);
                while (true) {
                    JSONObject jsonObject = (JSONObject) inputStream.readObject();
                    String s = (String) jsonObject.get("auth");
                    switch (s) {
                        case "ok": {
                            autorized = true;
                            viewMessage("Login successful", "Server");
                            viewMessage("For private message write: /w nickname, message", "System");
                            nickName = (String) jsonObject.get("nickName");
                            authorize();
                            break;
                        }
                        case "password": {
                            setLoginText("Wrong password");
                            break;
                        }
                        case "busy": {
                            setLoginText("Account already in use");
                            break;
                        }
                        case "notexist": {
                            setLoginText("User not exist");
                            break;
                        }
                        case "error": {
                            setLoginText("Database error");
                            break;
                        }
                        case "nick": {
                            setLoginText("Nickname already in use");
                            break;
                        }
                        case "login": {
                            setLoginText("Login already in use");
                            break;
                        }
                    }
                    if (autorized) {
                        break;
                    }
                }
                while (true) {
                    JSONObject jsonObject = (JSONObject) inputStream.readObject();
                    String type = (String) jsonObject.get("type");
                    if (type != null) {
                        switch (type) {
                            case "addUser": {
                                String nick = (String) jsonObject.get("nickName");
                                usersAdd(nick);
                                break;
                            }
                            case "rmvUser": {
                                String nick = (String) jsonObject.get("nickName");
                                usersRemove(nick);
                                break;
                            }
                            case "message":
                                String message = (String) jsonObject.get("message");
                                String sender = (String) jsonObject.get("sender");
                                viewMessage(message, sender);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                setLoginText("Connection to server lost");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                autorized = false;
                Platform.runLater(() -> {
                    authorize();
                    userList.clear();
                        }
                );
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

    private void viewMessage(String s, String sender) {
        String message = sender + ": " + s;
        Platform.runLater(() -> chatMessages.add(message));
    }

    private void usersAdd(String s) {
        if (!userList.contains(s)) {
            Platform.runLater(() -> userList.add(s));
        }
    }

    private void usersRemove(String s) {
        Platform.runLater(() -> userList.remove(s));
    }

    public void minimize() {
        stage.setIconified(true);
    }

    public void close() {
        System.exit(0);
    }

    private void createForms() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            login = loader.load();
            loginController = loader.getController();
            loginController.setChatController(this);

            loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            register = loader.load();
            registerController = loader.getController();
            registerController.setChatController(this);

            label = new Label();
            label.getStyleClass().setAll("errorLabel");
            label.setAlignment(Pos.CENTER);
            label.setWrapText(true);
            label.setMinHeight(50);
            label.setMaxHeight(50);
            label.setMinWidth(300);
            label.setMaxWidth(300);
        } catch (IOException e) {
            System.out.println("cannot load inner fxml");
            e.printStackTrace();
        }
    }

    private void createAnimation() {
        ft = new FadeTransition();
        ft.setNode(authBox);
        ft.setDuration(new Duration(500));
        ft.setFromValue(0.5);
        ft.setToValue(0.0);
        ft.setCycleCount(1);

        TranslateTransition tt = new TranslateTransition();
        tt.setDelay(new Duration(2000));
        tt.setNode(helloBox);
        tt.setDuration(new Duration(1000));
        tt.setFromY(0);
        tt.setCycleCount(1);

        TranslateTransition tt2 = new TranslateTransition();
        tt2.setDelay(new Duration(2000));
        tt2.setNode(textBox);
        tt2.setDuration(new Duration(1000));
        tt2.setCycleCount(1);

        ft.setOnFinished(event -> {
            if (authBox.isVisible()) {
                tt.setToY(helloBox.getHeight());
                tt2.setToY(0);
                tt2.setFromY(-textBox.getHeight());
                authBox.setVisible(false);
                tt.play();
                tt2.play();
                textBox.setDisable(false);
            }
        });
        tt.setOnFinished(event -> {
            helloBox.setVisible(false);
        });
    }

    public void setLoginScreen() {
        changeBox.getChildren().setAll(login, label);
        setLoginText("");
    }

    public void setRegScreen() {
        changeBox.getChildren().setAll(register, label);
        setLoginText("");
    }

    public void setLoginText(String text) {
        Platform.runLater(() -> {
            label.setText(text);
        });
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

}
