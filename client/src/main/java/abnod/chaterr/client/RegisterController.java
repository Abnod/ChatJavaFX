package abnod.chaterr.client;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField loginField, nickField;
    @FXML
    private PasswordField passwordField;
    ChatController chatController;

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
    }

    public void register() {
    }

    public void backToLogin() {
        chatController.openLoginScreen();
    }
}
