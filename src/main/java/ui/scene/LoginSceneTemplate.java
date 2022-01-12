package ui.scene;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

import static ui.MainTemplate._REGISTER_SCENE_FXML;
import static ui.MainTemplate._SCENE_FXML;


public class LoginSceneTemplate extends BaseSceneTemplate implements Initializable {
    @FXML
    StackPane rootPane;
    @FXML
    JFXTextField username;
    @FXML
    JFXPasswordField password;
    @FXML
    JFXButton loginButton;
    @FXML
    JFXButton registerButton;

    @FXML
    private void login() {
        SceneUtils.getInstance().switchScreen(this.rootPane, _SCENE_FXML, 100);
    }

    @FXML
    private void register() {
        SceneUtils.getInstance().switchScreen(this.rootPane, _REGISTER_SCENE_FXML, 100);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(()->username.requestFocus());
    }
}
