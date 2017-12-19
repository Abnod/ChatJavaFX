package abnod.chaterr.client;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    ChatController chatController;

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }

    public void sendAuth() {
        if (!loginField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            chatController.sendAuth(loginField.getText(), passwordField.getText());
        } else {
            chatController.viewMessage("login and password fields cannot be empty");
        }
    }

    public void openRegistration() {
        chatController.openRegistrationScreen();
    }
}
