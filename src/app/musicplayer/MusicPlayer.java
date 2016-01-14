package app.musicplayer;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;

import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.Resources;
import app.musicplayer.view.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class MusicPlayer extends Application {

    private static MainController mainController;
    private static Song selectedSong;
    private static MediaPlayer mediaPlayer;
    private static ArrayList<Song> nowPlayingList;
    private static int nowPlayingIndex;
    private static Song nowPlaying;
    private static Timer timer;
    private static int timerCounter;
    private static int secondsPlayed;
    
    private Stage stage;
    private BorderPane view;

    public static void main(String[] args) {
        Application.launch(MusicPlayer.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
    	
    	// Suppresses warning caused by converting music library data into xml file. 
        LogManager.getLogManager().reset();
        timer = new Timer();
        timerCounter = 0;
        secondsPlayed = 0;
        
        // Calls the function to check in the library.xml file exists. If it doesn not, the file is created.
        checkLibraryXML();

        // Retrieves song, album, artist, and playlist data from library.
        Library.getSongs();        
        Library.getAlbums();
        Library.getArtists();
        Library.getPlaylists();
        
        this.stage = stage;
        this.stage.setTitle("Music Player");
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        
        // Calls the function to initialize the main layout.
        initMain();
    }
    
    /**
     * Initializes the main layout.
     */
    public void initMain() {
    	try {
    		// Load main layout from fxml file.
    		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "Main.fxml"));
    		view = (BorderPane) loader.load();
    		
    		// Shows the scene containing the layout.
    		Scene scene = new Scene(view);
    		stage.setScene(scene);
    		
    		// Gives the controller access to the music player main application.
    		mainController = loader.getController();
    		
    		stage.show();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

    private static class SongSkipper implements Runnable {
        @Override
        public void run() {
            skip();
        }
    }

    private static class TimeUpdater extends TimerTask {
        private int length = (int) getNowPlaying().getLengthInSeconds() * 4;

        @Override
        public void run() {
            Platform.runLater(() -> {
                if (timerCounter < length) {
                    if (++timerCounter % 4 == 0) {
                        mainController.updateTimeLabels();
                        secondsPlayed++;
                    } // End if
                    mainController.updateTimeSlider();
                } // End if
            });
        } // End run()
     }// End TimeUpdater

    /**
     * Plays selected song.
     */
    public static void play() {
        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            mediaPlayer.play();
            timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
            mainController.updatePlayPauseIcon();
        }
    }

    /**
     * Pauses selected song.
     */
    public static void pause() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            timer.cancel();
            timer = new Timer();
            mainController.updatePlayPauseIcon();
        }
    }

    // TODO: COMPLETE
    /**
     * 
     * 
     * @param seconds
     */
    public static void seek(int seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(seconds * 1000));
            timerCounter = seconds * 4;
            mainController.updateTimeLabels();
        }
    }

    /**
     * Skips song.
     */
    public static void skip() {
        if (nowPlayingIndex < nowPlayingList.size() - 1) {
            setNowPlaying(nowPlayingList.get(nowPlayingIndex + 1));
            play();
        } else {
            updatePlayCount();
            timer.cancel();
            timer = new Timer();
            mediaPlayer.stop();
            mediaPlayer = null;
            nowPlayingList = null;
            nowPlayingIndex = 0;
            nowPlaying.setPlaying(false);
            nowPlaying = null;
            mainController.initializeTimeSlider();
            mainController.initializeTimeLabels();
            mainController.updateNowPlayingButton();
            mainController.updatePlayPauseIcon();
        }
    }

    // TODO: COMPLETE
    /**
     * 
     */
    public static void back() {
        if (timerCounter > 20 || nowPlayingIndex == 0) {
            mainController.initializeTimeSlider();
            seek(0);
        } else {
            setNowPlaying(nowPlayingList.get(nowPlayingIndex - 1));
            play();
        }
    }

    /**
     * Checks if a song is playing.
     */
    public static boolean isPlaying() {
        return mediaPlayer == null || mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }
    
    // GETTERS AND SETTERS

    /**
     * Gets currently selected song.
     * @return selected song
     */
    public static Song getSelectedSong() {
        return selectedSong;
    }
    
    /**
     * Sets currently selected song.
     * @param song selected song
     */
    public static void setSelectedSong(Song song) {
        selectedSong = song;
    }

    /**
     * Gets currently playing song list.
     * @return arraylist of songs
     */
    public static ArrayList<Song> getNowPlayingList() {
        return nowPlayingList == null ? new ArrayList<Song>() : new ArrayList<Song>(nowPlayingList);
    }

    public static void setNowPlayingList(List<Song> list) {
        nowPlayingList = new ArrayList<Song>(list);
    }

    public static Song getNowPlaying() {
        return nowPlaying;
    }

    public static void setNowPlaying(Song song) {

        if (nowPlayingList.contains(song)) {

            updatePlayCount();
            nowPlayingIndex = nowPlayingList.indexOf(song);
            if (nowPlaying != null) {
                nowPlaying.setPlaying(false);
            }
            nowPlaying = song;
            nowPlaying.setPlaying(true);
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer();
            timerCounter = 0;
            secondsPlayed = 0;
            String path = song.getLocation();
            Media media = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.setOnEndOfMedia(new SongSkipper());
            Platform.runLater(() -> {
                mainController.updateNowPlayingButton();
                mainController.initializeTimeSlider();
                mainController.initializeTimeLabels();
            });
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
        long totalSeconds = getNowPlaying().getLengthInSeconds();
        long secondsRemaining = totalSeconds - secondsPassed;
        long minutes = secondsRemaining / 60;
        long seconds = secondsRemaining % 60;
        return Long.toString(minutes) + ":" + (seconds < 10 ? "0" + seconds : Long.toString(seconds));
    }

    /**
     * Gets main controller object.
     * @return MainController
     */
    public static MainController getMainController() {
        return mainController;
    }
    
    private void checkLibraryXML() {
    	// Finds the jar file and the path of its parent folder.
    	File musicPlayerJAR = null;
		try {
			musicPlayerJAR = new File(MusicPlayer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    	String jarFilePath = musicPlayerJAR.getParentFile().getPath();
    	
    	// Assigns the filepath to the XML filepath set in Resources.java
    	Resources.XML = jarFilePath;
    	
    	// Specifies library.xml file and its location.
    	File libraryXML = new File(Resources.XML + "/library.xml");
    	
    	// If the library.xml file does not exist, the file is created from the user specified music library location.
    	if (!libraryXML.exists()) {
    		createLibraryXML();
    	}
    }
    
    private void createLibraryXML() {    	
		// Creates alert box.
		Alert initialSetupAlert = new Alert(AlertType.INFORMATION);
		initialSetupAlert.setTitle("Welcome!");
		
		initialSetupAlert.setHeaderText(null);
		
		initialSetupAlert.setContentText("Use the button below to navigate to the music folder in your computer.");
		
		// Creates a button and adds it to the alert box.
		ButtonType importMusicButton = new ButtonType("Import Music Library");
		initialSetupAlert.getButtonTypes().setAll(importMusicButton);
		
		// Opens a file explorer to select music location.
		Optional<ButtonType> result = initialSetupAlert.showAndWait();
		try {
			// If user clicks the import music button.
			if (result.get() == importMusicButton){
				DirectoryChooser directoryChooser = new DirectoryChooser();
			    // Show file explorer.
			    String musicDirectory = directoryChooser.showDialog(stage).getPath();
			    // Creates library.xml file from user music library.
			    Library.importMusic(musicDirectory);
			}
		} catch (Exception e) {
			// If the user closes the alert box, the program exits.
			initialSetupAlert.close();
			System.exit(0);
		}
    }

    private static void updatePlayCount() {
        if (nowPlaying != null) {
            int length = (int) nowPlaying.getLengthInSeconds();
            if ((100 * secondsPlayed / length) > 50) {
                nowPlaying.played();
            }
        }
    }
}
