package musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.logging.LogManager;

public class MusicPlayer extends Application {

    private static Song selectedSong;
    private static MediaPlayer mediaPlayer;
    private static LinkedList<Song> nowPlayingStack;

    public static void main(String[] args) {

        Application.launch(MusicPlayer.class);
    }

    @Override
    public void start(Stage stage) {

        LogManager.getLogManager().reset();

        Library.getSongs();
        Library.getArtists();
        Library.getAlbums();
        Library.getPlaylists();

        nowPlayingStack = new LinkedList<Song>();

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

        if (mediaPlayer == null) {

            String path = selectedSong.getLocation();
            Media song = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(song);
            nowPlayingStack.addFirst(selectedSong);
        
        } else if (selectedSong != nowPlayingStack.getFirst()) {

            mediaPlayer.stop();
            String path = selectedSong.getLocation();
            Media song = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(song);
            nowPlayingStack.addFirst(selectedSong);
        }

        if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {

            mediaPlayer.play();
        }
    }

    public static void pause() {

        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {

            mediaPlayer.pause();
        }
    }

    public static boolean isPlaying() {

        if (mediaPlayer == null) {

            return false;
        }

        return mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public static Song getSelectedSong() {

        return selectedSong;
    }

    public static void setSelectedSong(Song song) {

        selectedSong = song;
    }

    public static LinkedList<Song> getNowPlayingStack() {

        return nowPlayingStack;
    }
}