package abnod.chaterr.client;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField loginField, nickField;
    @FXML
    private PasswordField passwordField, passwordFieldPrompt;

    private ChatController chatController;
    private Pattern pattern;
    private Matcher matcher;

    public void setChatController(ChatController chatController) {
        this.chatController = chatController;
        pattern = Pattern.compile("([A-Za-z0-9]+[.-]?[A-Za-z0-9]+[A-Za-z0-9]+)+");
    }

    public void register() {
        String login = loginField.getText();
        String nick = nickField.getText();
        String pass = passwordField.getText();

        if (!loginField.getText().isEmpty() && !nickField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            if (!passwordField.getText().equalsIgnoreCase(passwordFieldPrompt.getText())){
                chatController.setLoginText("Passwords do not match");
            } else {
                if (login.length() < 3) {
                    chatController.setLoginText("Login must be at least three characters");
                } else if (pass.length() < 3) {
                    chatController.setLoginText("Password must be at least three characters");
                } else if (nick.length() < 3) {
                    chatController.setLoginText("Nick must be at least three characters");
                } else {
                    matcher = pattern.matcher(login);
                    if (matcher.matches()) {
                        matcher = pattern.matcher(pass);
                        if (matcher.matches()) {
                            matcher = pattern.matcher(nick);
                            if (matcher.matches()) {
                                chatController.setLoginText("");
                                chatController.sendReg(login, pass, nick);
                            } else {
                                chatController.setLoginText("Nick must  begin & end with a letter or number, also can contain '.' & '-' symbols");
                            }
                        } else {
                            chatController.setLoginText("Password must begin & end with a letter or number, also can contain '.' & '-' symbols");
                        }
                    } else {
                        chatController.setLoginText("Login must begin & end with a letter or number, also can contain '.' & '-' symbols");
                    }
                }
            }
        } else {
            chatController.setLoginText("Fields cannot be empty!");
        }
    }

    public void backToLogin() {
        chatController.setLoginScreen();
    }
}
