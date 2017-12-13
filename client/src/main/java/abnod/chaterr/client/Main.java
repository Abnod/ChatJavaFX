package abnod.chaterr.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/chat.fxml"));
        primaryStage.setTitle("ChatterR");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setWidth(600);
        primaryStage.setHeight(600);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(600);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        new MoveAndResizeController(primaryStage, root);
    }
}