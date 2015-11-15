package musicplayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import javafx.util.Duration;
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
    private static int nowPlayingIndex;
    private static Timer timer;
    private static int timerCounter;

    public static void main(String[] args) {

        Application.launch(MusicPlayer.class);
    }

    @Override
    public void start(Stage stage) throws Exception {

        LogManager.getLogManager().reset();
        timer = new Timer();
        timerCounter = 0;

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

    private static class SongSkipper implements Runnable {

        @Override
        public void run() {

            if (nowPlayingIndex < nowPlayingList.size()) {
                setNowPlaying(nowPlayingList.get(nowPlayingIndex + 1));
                play();
            }
        }
    }

    private static class TimeUpdater extends TimerTask {

        int length = (int) getNowPlaying().getLength().getSeconds() * 4;

        @Override
        public void run() {

            Platform.runLater(() -> {

                if (timerCounter < length) {
                    if (++timerCounter % 4 == 0) {
                        mainController.updateTimeLabels();
                    }
                    mainController.updateTimeSlider();
                }
            });
        }
     }

    public static void play() {

        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {

            mediaPlayer.play();
            timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
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

    public static void seek(int seconds) {

        mediaPlayer.seek(new Duration(seconds * 1000));
        timerCounter = seconds * 4;
        mainController.updateTimeLabels();
    }

    public static void skip() {

        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {

            mediaPlayer.play();
            timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
            mainController.updatePlayPauseIcon();
        }
    }

    public static void back() {

        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {

            mediaPlayer.play();
            timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
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

        return nowPlayingList.get(nowPlayingIndex);
    }

    public static void setNowPlaying(Song song) {

        if (nowPlayingList.contains(song)) {

            nowPlayingIndex = nowPlayingList.indexOf(song);
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timerCounter = 0;
            String path = song.getLocation();
            Media media = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(new SongSkipper());
            mainController.updateNowPlayingButton();
            mainController.initializeTimeSlider();
            mainController.initializeTimeLabels();
        }
    }

    public static String getTimePassed() {

        int secondsPassed = timerCounter / 4;
        int minutes = secondsPassed / 60;
        int seconds = secondsPassed % 60;
        return Integer.toString(minutes) + ":" + (seconds < 10 ? "0" + seconds : Integer.toString(seconds));
    }

    public static String getTimeRemaining() {

        long secondsPassed = timerCounter / 4;
        long totalSeconds = getNowPlaying().getLength().getSeconds();
        long secondsRemaining = totalSeconds - secondsPassed;
        long minutes = secondsRemaining / 60;
        long seconds = secondsRemaining % 60;
        return Long.toString(minutes) + ":" + (seconds < 10 ? "0" + seconds : Long.toString(seconds));
    }

    public static MainController getMainController() {

        return mainController;
    }
}