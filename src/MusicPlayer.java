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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;

public class MusicPlayer extends Application {

    private static MainController mainController;
    private static Song selectedSong;
    private static MediaPlayer mediaPlayer;
    private static ArrayList<Song> nowPlayingList;
    private static Song nowPlaying;
    private static Timer timer;

    public static void main(String[] args) {

        Application.launch(MusicPlayer.class);
    }

    @Override
    public void start(Stage stage) throws Exception {

        LogManager.getLogManager().reset();
        timer = new Timer();

        Library.getSongs();
        Library.getAlbums();
        Library.getArtists();
        Library.getPlaylists();

        try {

            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "main.fxml"));
            BorderPane view = (BorderPane) loader.load();
            mainController = loader.getController();
            Scene scene = new Scene(view);
            stage.setScene(scene);
            stage.getIcons().add(new Image(this.getClass().getResource(Resources.IMG + "songsIcon.png").toString()));
            stage.setTitle("Music Player");
            stage.show();

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private static class TimeUpdater extends TimerTask {

        @Override
        public void run() {

           mainController.updateTimeSlider();
        }
     }

    public static void play() {

        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {

            mediaPlayer.play();
            timer.scheduleAtFixedRate(new TimeUpdater(), 0, 1000);
            mainController.updatePlayPauseIcon();
        }
    }

    public static void pause() {

        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {

            mediaPlayer.pause();
            timer.cancel();
            timer = new Timer();
            mainController.updatePlayPauseIcon();
        }
    }

    public static boolean isPlaying() {

        return mediaPlayer == null || mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public static Song getSelectedSong() {

        return selectedSong;
    }

    public static void setSelectedSong(Song song) {

        selectedSong = song;
    }

    public static ArrayList<Song> getNowPlayingList() {

        return new ArrayList<Song>(nowPlayingList);
    }

    public static void setNowPlayingList(List<Song> list) {

        nowPlayingList = new ArrayList<Song>(list);
    }

    public static Song getNowPlaying() {

        return nowPlaying;
    }

    public static void setNowPlaying(Song song) {

        if (nowPlayingList.contains(song)) {

            nowPlaying = song;
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            String path = selectedSong.getLocation();
            Media media = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mainController.updateNowPlayingButton();
            mainController.initializeTimeSlider();
        }
    }
}