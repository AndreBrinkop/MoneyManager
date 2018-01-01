package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.security.auth.login.CredentialException;
import java.io.IOException;

import static gui.MoneyManager.getEncryptionKeyFromConsoleInput;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("gui/main.fxml"));
        fxmlLoader.setControllerFactory(c -> {
            try {
                return new MainViewController(getEncryptionKeyFromConsoleInput());
            } catch (CredentialException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });

        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Money Manager");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
