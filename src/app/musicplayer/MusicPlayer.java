package app.musicplayer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.LogManager;

import app.musicplayer.model.Album;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.Resources;
import app.musicplayer.view.ImportMusicDialogController;
import app.musicplayer.view.MainController;
import app.musicplayer.view.NowPlayingController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class MusicPlayer extends Application {

    private static MainController mainController;
    private static MediaPlayer mediaPlayer;
    private static ArrayList<Song> nowPlayingList;
    private static int nowPlayingIndex;
    private static Song nowPlaying;
    private static Timer timer;
    private static int timerCounter;
    private static int secondsPlayed;
    private static boolean isLoopActive = false;
    private static boolean isShuffleActive = false;
    
    private static Stage stage;
    private static BorderPane view;

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

        MusicPlayer.stage = stage;
        MusicPlayer.stage.setTitle("Music Player");
        MusicPlayer.stage.getIcons().add(new Image(this.getClass().getResource(Resources.IMG + "Icon.png").toString()));
        MusicPlayer.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        try {
    		// Load main layout from fxml file.
    		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "SplashScreen.fxml"));
    		VBox view = (VBox) loader.load();
    		
    		// Shows the scene containing the layout.
    		Scene scene = new Scene(view);
    		stage.setScene(scene);
    		stage.setMaximized(true);
    		stage.show();
    		
            // Calls the function to check in the library.xml file exists. If it does not, the file is created.
            checkLibraryXML();
    		
        } catch (Exception ex) {
        	System.exit(0);
        	ex.printStackTrace();
        }
        
        Thread thread = new Thread(() -> {
        	// Retrieves song, album, artist, and playlist data from library.
            Library.getSongs();        
            Library.getAlbums();
            Library.getArtists();
            Library.getPlaylists();
            nowPlayingList = Library.loadPlayingList();
            
            if (nowPlayingList.isEmpty()) {
            	
            	Artist artist = Library.getArtists().get(0);
            	
            	for (Album album : artist.getAlbums()) {
            		nowPlayingList.addAll(album.getSongs());
            	}
            	
            	Collections.sort(nowPlayingList, (first, second) -> {
                    Album firstAlbum = Library.getAlbum(first.getAlbum());
                    Album secondAlbum = Library.getAlbum(second.getAlbum());
                    if (firstAlbum.compareTo(secondAlbum) != 0) {
                        return firstAlbum.compareTo(secondAlbum);
                    } else {
                        return first.compareTo(second);
                    }
                });
            }
            
            nowPlaying = nowPlayingList.get(0);
            nowPlayingIndex = 0;
            nowPlaying.setPlaying(true);
            timer = new Timer();
            timerCounter = 0;
            secondsPlayed = 0;
            String path = nowPlaying.getLocation();
            Media media = new Media(Paths.get(path).toUri().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.setOnEndOfMedia(new SongSkipper());
            
            File imgFolder = new File(Resources.JAR + "/img");
        	if (!imgFolder.exists()) {
        		
        		Thread thread1 = new Thread(() -> {
        			for (Artist artist : Library.getArtists()) {
            			artist.downloadArtistImage();
            		}
        		});
        		
        		Thread thread2 = new Thread(() -> {
        			for (Album album : Library.getAlbums()) {
            			album.downloadArtwork();
            		}
        		});
        		
        		thread1.start();
        		thread2.start();
        	}

            // Calls the function to initialize the main layout.
            Platform.runLater(() -> {
            	initMain();
            });
        });
        
        thread.start();
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
    		double width = stage.getScene().getWidth();
    		double height = stage.getScene().getHeight();

    		view.setPrefWidth(width);
    		view.setPrefHeight(height);
    		
    		Scene scene = new Scene(view);
    		stage.setScene(scene);
    		
    		// Gives the controller access to the music player main application.
    		mainController = loader.getController();
    		mediaPlayer.volumeProperty().bind(mainController.getVolumeSlider().valueProperty().divide(200));
    		
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
                    }
                    if (!mainController.isTimeSliderPressed()) {
                    	mainController.updateTimeSlider();
                    }
                }
            });
        } // End run()
    }// End TimeUpdater
    
    public static Stage getStage() {
    	return stage;
    }
    
    public static void toggleLoop() {
    	
    	isLoopActive = !isLoopActive;
    }
    
    public static void toggleShuffle() {
    	
    	isShuffleActive = !isShuffleActive;
    	
    	if (isShuffleActive) {
    		Collections.shuffle(nowPlayingList);
    	} else {
    		Collections.sort(nowPlayingList, (first, second) -> {
    			int result = Library.getAlbum(first.getAlbum()).compareTo(Library.getAlbum(second.getAlbum()));
    			if (result != 0) {
    				return result;
    			}
    			result = Library.getAlbum(first.getAlbum()).compareTo(Library.getAlbum(second.getAlbum()));
    			if (result != 0) {
    				return result;
    			}
    			result = first.compareTo(second);
    			return result;
    		});
    	}
    	
    	nowPlayingIndex = nowPlayingList.indexOf(nowPlaying);
    	
    	if (mainController.getSubViewController() instanceof NowPlayingController) {
    		mainController.loadView("nowPlaying");
    	}
    }
    
    public static boolean isLoopActive() {
    	
    	return isLoopActive;
    }
    
    public static boolean isShuffleActive() {
    	
    	return isShuffleActive;
    }

    /**
     * Plays selected song.
     */
    public static void play() {
        if (mediaPlayer != null && !isPlaying()) {
            mediaPlayer.play();
            timer.scheduleAtFixedRate(new TimeUpdater(), 0, 250);
            mainController.updatePlayPauseIcon(true);
        }
    }

    /**
     * Pauses selected song.
     */
    public static void pause() {
        if (isPlaying()) {
            mediaPlayer.pause();
            timer.cancel();
            timer = new Timer();
            mainController.updatePlayPauseIcon(false);
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
        	boolean isPlaying = isPlaying();
        	mainController.updatePlayPauseIcon(isPlaying);
            setNowPlaying(nowPlayingList.get(nowPlayingIndex + 1));
            if (isPlaying) {
            	play();
            }
        } else if (isLoopActive) {
        	boolean isPlaying = isPlaying();
        	mainController.updatePlayPauseIcon(isPlaying);
        	nowPlayingIndex = 0;
        	setNowPlaying(nowPlayingList.get(nowPlayingIndex));
        	if (isPlaying) {
            	play();
            }
        } else {
        	mainController.updatePlayPauseIcon(false);
            nowPlayingIndex = 0;
            setNowPlaying(nowPlayingList.get(nowPlayingIndex));
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
            if (isPlaying()) {
            	play();
            }
        }
    }

    /**
     * Checks if a song is playing.
     */
    public static boolean isPlaying() {
        return mediaPlayer != null && MediaPlayer.Status.PLAYING.equals(mediaPlayer.getStatus());
    }
    
    // GETTERS AND SETTERS

    /**
     * Gets currently playing song list.
     * @return arraylist of songs
     */
    public static ArrayList<Song> getNowPlayingList() {
        return nowPlayingList == null ? new ArrayList<Song>() : new ArrayList<Song>(nowPlayingList);
    }

    public static void setNowPlayingList(List<Song> list) {
        nowPlayingList = new ArrayList<Song>(list);
        Library.savePlayingList();
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
            mediaPlayer.volumeProperty().bind(mainController.getVolumeSlider().valueProperty().divide(200));
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
    	Resources.JAR = jarFilePath + "/";
    	
    	// Specifies library.xml file and its location.
    	File libraryXML = new File(Resources.JAR + "library.xml");
    	
    	// If the library.xml file does not exist, the file is created from the user specified music library location.
    	if (!libraryXML.exists()) {
    		createLibraryXML();
    	}
    }
    
    private void createLibraryXML() {    	
    	try {
			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "ImportMusicDialog.fxml"));
			BorderPane importView = (BorderPane) loader.load();
			
			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Music Player Configuration");
			// Forces user to focus on dialog.
			dialogStage.initModality(Modality.WINDOW_MODAL);
			// Sets minimal decorations for dialog.
			dialogStage.initStyle(StageStyle.UTILITY);
			// Prevents the alert from being re-sizable.
			dialogStage.setResizable(false);
			dialogStage.initOwner(stage);
			
			// Sets the import music dialog scene in the stage.
			dialogStage.setScene(new Scene(importView));

			// Set the dialog into the controller.
			ImportMusicDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			
	        // Show the dialog and wait until the user closes it.
	        dialogStage.showAndWait();
	        
	        // Checks if the music was imported successfully. Closes the application otherwise.
	        boolean musicImported = controller.isMusicImported();
	        if (!musicImported) {
	        	System.exit(0);
	        }
		} catch (IOException e) {
			e.printStackTrace();
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
