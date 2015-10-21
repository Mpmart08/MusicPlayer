package musicplayer;

import musicplayer.Resources;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.File;
import javafx.scene.image.Image;

public class MainView extends Application {

    public static void main(String[] args) {

        Application.launch(MainView.class);
    }

    @Override
    public void start(Stage stage) {

        try {

            BorderPane view = (BorderPane) FXMLLoader.load(this.getClass().getResource(Resources.FXML + "main.fxml"));
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.getIcons().add(new Image(this.getClass().getResource(Resources.IMG + "songsIcon.png").toString()));
            stage.setTitle("Music Player");
            stage.show();

        } catch (Exception ex) {

            System.out.println(ex.getMessage());
        }
    }

}