package musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.File;
import javafx.scene.image.Image;

public class MusicPlayer extends Application {

    private static boolean isPlaying = false;
    private static Song selectedSong;

    public static void main(String[] args) {

        Application.launch(MusicPlayer.class);
    }

    @Override
    public void start(Stage stage) {

        Library.getSongs();
        Library.getArtists();
        Library.getAlbums();
        Library.getPlaylists();

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

    public static void play() {

        isPlaying = true;
    }

    public static void pause() {

        isPlaying = false;
    }

    public static boolean isPlaying() {

        return isPlaying;
    }

    public static void setSelectedSong(Song song) {

        selectedSong = song;
    }

}