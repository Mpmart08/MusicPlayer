package app.musicplayer.view;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Album;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.Resources;
import app.musicplayer.util.Scrollable;
import app.musicplayer.util.SliderSkin;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainController implements Initializable {

	private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;
    private double expandedHeight = 50;
    private double collapsedHeight = 0;
    private Scrollable subViewController;
    private SliderSkin skin;
    private Stage volumePopup;
    private Slider volumeSlider;
    private Region frontVolumeTrack;

    @FXML private BorderPane mainWindow;
    @FXML private ScrollPane subViewRoot;
    @FXML private VBox sideBar;
    @FXML private ImageView sideBarSlideButton;    
    @FXML private ImageView nowPlayingArtwork;
    @FXML private Label nowPlayingTitle;
    @FXML private Label nowPlayingArtist;
    @FXML private Slider timeSlider;
    @FXML private Region frontSliderTrack;    
    @FXML private Label timePassed;
    @FXML private Label timeRemaining;

    @FXML private HBox letterBox;
    @FXML private Separator letterSeparator;
    
    @FXML private Pane backButton;
    @FXML private Pane playButton;
    @FXML private Pane pauseButton;
    @FXML private Pane skipButton;
    @FXML private Pane loopButton;
    @FXML private Pane shuffleButton;
    @FXML private Pane volumeButton;
    @FXML private HBox controlBox;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	controlBox.getChildren().remove(2);
    	
    	frontSliderTrack.prefWidthProperty().bind(timeSlider.widthProperty().multiply(timeSlider.valueProperty().divide(timeSlider.maxProperty())));
    	
    	skin = new SliderSkin(timeSlider);
    	timeSlider.setSkin(skin);
    	
    	createVolumePopup();
    	
    	PseudoClass active = PseudoClass.getPseudoClass("active");
    	loopButton.setOnMouseClicked(x -> {
    		MusicPlayer.toggleLoop();
    		loopButton.pseudoClassStateChanged(active, MusicPlayer.isLoopActive());
    	});
    	shuffleButton.setOnMouseClicked(x -> {
    		MusicPlayer.toggleShuffle();
    		shuffleButton.pseudoClassStateChanged(active, MusicPlayer.isShuffleActive());
    	});
    	
        timeSlider.valueChangingProperty().addListener(
            (slider, wasChanging, isChanging) -> {

                if (wasChanging) {

                    int seconds = (int) Math.round(timeSlider.getValue() / 4.0);
                    timeSlider.setValue(seconds * 4);
                    MusicPlayer.seek(seconds);
                }
            }
        );

        timeSlider.valueProperty().addListener(
            (slider, oldValue, newValue) -> {

                double previous = oldValue.doubleValue();
                double current = newValue.doubleValue();
                if (!timeSlider.isValueChanging() && current != previous + 1 && !isTimeSliderPressed()) {

                    int seconds = (int) Math.round(current / 4.0);
                    timeSlider.setValue(seconds * 4);
                    MusicPlayer.seek(seconds);
                }
            }
        );
        
        unloadLettersAnimation.setOnFinished(x -> {
        	letterBox.setPrefHeight(0);
        	letterSeparator.setPrefHeight(0);
		});
        
        for (Node node : letterBox.getChildren()) {
        	Label label = (Label)node;
        	label.prefWidthProperty().bind(letterBox.widthProperty().subtract(50).divide(26).subtract(1));
        }
        
        updateNowPlayingButton();
        initializeTimeSlider();
        initializeTimeLabels();
        
        // Loads the default view: artists.
        loadView("artists");
    } // End initialize()

    @FXML
    private void selectView(Event e) {

        HBox eventSource = ((HBox)e.getSource());

        Optional<Node> previous = sideBar.getChildren().stream()
            .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

        if (previous.isPresent()) {
            HBox previousItem = (HBox)previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        }

        ObservableList<String> styles = eventSource.getStyleClass();

        if (styles.get(0).equals("sideBarItem")) {
            styles.setAll("sideBarItemSelected");
            loadView(eventSource.getId());
        } else if (styles.get(0).equals("bottomBarItem")) {
            loadView(eventSource.getId());
        }
    }
    
    @FXML
    private void navigateToCurrentSong() {
    	
    	Optional<Node> previous = sideBar.getChildren().stream()
                .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

        if (previous.isPresent()) {
            HBox previousItem = (HBox)previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        }
        
        sideBar.getChildren().get(2).getStyleClass().setAll("sideBarItemSelected");
            
        ArtistsMainController artistsMainController = (ArtistsMainController) loadView("ArtistsMain");
        Song song = MusicPlayer.getNowPlaying();
        Artist artist = Library.getArtist(song.getArtist());
        Album album = Library.getAlbum(song.getAlbum());
        artistsMainController.selectArtist(artist);
        artistsMainController.selectAlbum(album);
        artistsMainController.selectSong(song);
    }

    @FXML
    private void slideSideBar() {

        if (isSideBarExpanded) {
            collapseSideBar();
        } else {
            expandSideBar();
        }
    }

    @FXML
    public void playPause() {

        if (MusicPlayer.isPlaying()) {
            MusicPlayer.pause();
        } else {
            MusicPlayer.play();
        }
    }

    @FXML
    private void back() {

        MusicPlayer.back();
    }

    @FXML
    private void skip() {

        MusicPlayer.skip();
    }
    
    @FXML
    private void letterClicked(Event e) {
    	
    	Label eventSource = ((Label)e.getSource());
    	char letter = eventSource.getText().charAt(0);
    	subViewController.scroll(letter);
    }
    
    @FXML
    private void volumeClick() {
    	if (!volumePopup.isShowing()) {
    		volumePopup.show();
    		popupShowAnimation.play();
    	}
    }
    
    public Slider getVolumeSlider() {
    	return volumeSlider;
    }
    
    public boolean isTimeSliderPressed() {
    	return skin.getThumb().isPressed() || skin.getTrack().isPressed();
    }
    
    public Scrollable getSubViewController() {
    	
    	return subViewController;
    }
    
    public ScrollPane getScrollPane() {
    	return this.subViewRoot;
    }

    public Scrollable loadView(String viewName) {

        try {
        	
        	boolean loadLetters = false;
        	boolean unloadLetters = false;
        	
        	switch (viewName.toLowerCase()) {
        	case "artists":
        	case "artistsmain":
        	case "albums":
        	case "songs":
        		if (subViewController instanceof ArtistsController
        			|| subViewController instanceof ArtistsMainController
        			|| subViewController instanceof AlbumsController
        			|| subViewController instanceof SongsController) {
        			loadLetters = false;
        			unloadLetters = false;
        		} else {
        			loadLetters = true;
        			unloadLetters = false;
        		}
        		break;
        	default:
        		if (subViewController instanceof ArtistsController
        			|| subViewController instanceof ArtistsMainController
        			|| subViewController instanceof AlbumsController
        			|| subViewController instanceof SongsController) {
        			loadLetters = false;
        			unloadLetters = true;
        		} else {
        			loadLetters = false;
        			unloadLetters = false;
        		}
        		break;
        	}
	        
	        final boolean loadLettersFinal = loadLetters;
	        final boolean unloadLettersFinal = unloadLetters;
        	
            String fileName = viewName.substring(0, 1).toUpperCase() + viewName.substring(1) + ".fxml";
            
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fileName));
            Node view = (Node) loader.load();
            
            CountDownLatch latch = new CountDownLatch(1);
            
            Task<Void> task = new Task<Void>() {
	        	@Override protected Void call() throws Exception {
	        		subViewRoot.setVisible(false);
		        	subViewRoot.setContent(view);
		        	latch.countDown();
		        	return null;
	        	}
	        };
	        
	        task.setOnSucceeded(x -> {
	        	subViewRoot.setVisible(true);
	        	if (loadLettersFinal) {
	        		loadLettersAnimation.play();
	        	}
	        	if (loadViewAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    loadViewAnimation.stop();
                }
                loadViewAnimation.play();
	        });
	        
	        Thread thread = new Thread(() -> {
	        	Platform.runLater(task);
	        	try {
					latch.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
	        });
            
            unloadViewAnimation.setOnFinished(x -> {
            	thread.start();
            });
            
            if (subViewRoot.getContent() != null) {
            	if (unloadLettersFinal) {
            		unloadLettersAnimation.play();
            	}
	            if (unloadViewAnimation.statusProperty().get() == Animation.Status.RUNNING) {
	            	unloadViewAnimation.stop();
	            }
	            unloadViewAnimation.play();
        	} else {
        		subViewRoot.setContent(view);
        		if (loadLettersFinal) {
        			loadLettersAnimation.play();
        		}
        		if (loadViewAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    loadViewAnimation.stop();
                }
                loadViewAnimation.play();
        	}
            
            subViewController = loader.getController();
            return subViewController;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void updatePlayPauseIcon(boolean isPlaying) {

    	controlBox.getChildren().remove(1);
    	if (isPlaying) {
           	controlBox.getChildren().add(1, pauseButton);
        } else {
          	controlBox.getChildren().add(1, playButton);
        }
    }

    public void updateNowPlayingButton() {

        Song song = MusicPlayer.getNowPlaying();
        if (song != null) {
            nowPlayingTitle.setText(song.getTitle());
            nowPlayingArtist.setText(song.getArtist());
            nowPlayingArtwork.setImage(song.getArtwork());
        } else {
            nowPlayingTitle.setText("");
            nowPlayingArtist.setText("");
            nowPlayingArtwork.setImage(null);
        }
    }

    public void initializeTimeSlider() {

        Song song = MusicPlayer.getNowPlaying();
        if (song != null) {
            timeSlider.setMin(0);
            timeSlider.setMax(song.getLengthInSeconds() * 4);
            timeSlider.setValue(0);
            timeSlider.setBlockIncrement(1);
        } else {
            timeSlider.setMin(0);
            timeSlider.setMax(1);
            timeSlider.setValue(0);
            timeSlider.setBlockIncrement(1);
        }
    }

    public void updateTimeSlider() {

        timeSlider.increment();
    }

    public void initializeTimeLabels() {

        Song song = MusicPlayer.getNowPlaying();
        if (song != null) {
            timePassed.setText("0:00");
            timeRemaining.setText(song.getLength());
        } else {
            timePassed.setText("");
            timeRemaining.setText("");
        }
    }

    public void updateTimeLabels() {

        timePassed.setText(MusicPlayer.getTimePassed());
        timeRemaining.setText(MusicPlayer.getTimeRemaining());
    }

    private void collapseSideBar() {
        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

            setVisibility(false);
            collapseAnimation.play();
        }
    }

    private void expandSideBar() {
        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

            expandAnimation.play();
        }
    }

    private void setVisibility(boolean isVisible) {
        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setVisible(isVisible);
                    }
                }
            }
        }
    }

    private void setSlideDirection() {
        isSideBarExpanded = !isSideBarExpanded;
        sideBarSlideButton.setImage(new Image(this.getClass().getResource(Resources.IMG
                + (isSideBarExpanded ? "LeftArrowIcon.png" : "RightArrowIcon.png")).toString()));
    }
    
    private void createVolumePopup() {
    	
    	try {
    		
    		Stage stage = MusicPlayer.getStage();
        	FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "VolumePopup.fxml"));
        	StackPane view = (StackPane) loader.load();
        	volumeSlider = (Slider) view.getChildren().get(2);
        	frontVolumeTrack = (Region) view.getChildren().get(1);
        	SliderSkin skin = new SliderSkin(volumeSlider);
        	volumeSlider.setSkin(skin);
        	frontVolumeTrack.prefWidthProperty().bind(volumeSlider.widthProperty().subtract(30).multiply(volumeSlider.valueProperty().divide(volumeSlider.maxProperty())));
        	volumeSlider.setValue(volumeSlider.getMax() - 1);
        	volumeSlider.setValue(volumeSlider.getMax());
        	Stage popup = new Stage();
        	popup.setScene(new Scene(view));
        	popup.initStyle(StageStyle.UNDECORATED);
        	popup.initOwner(stage);
        	popup.setX(stage.getWidth() - 270);
        	popup.setY(stage.getHeight() - 120);
        	popup.focusedProperty().addListener((x, wasFocused, isFocused) -> {
        		if (wasFocused && !isFocused) {
        			popupHideAnimation.play();
        		}
        	});
        	popupHideAnimation.setOnFinished(x -> {
        		popup.hide();
        	});
        	
        	popup.show();
        	popup.hide();
        	volumePopup = popup;
        	
    	} catch (Exception ex) {
    		
    		ex.printStackTrace();
    	}
    }
    
    // ANIMATIONS
    
    private Animation popupShowAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(250));
        }
    	
        protected void interpolate(double frac) {
        	volumePopup.setOpacity(frac);
        }
    };
    
    private Animation popupHideAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(250));
        }
        protected void interpolate(double frac) {
            volumePopup.setOpacity(1.0 - frac);
        }
    };
    
    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> setSlideDirection());
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (1.0 - frac);
            sideBar.setPrefWidth(curWidth);
        }
    };

    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> {setVisibility(true); setSlideDirection();});
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
            sideBar.setPrefWidth(curWidth);
        }
    };

    private Animation loadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(1000));
        }
        protected void interpolate(double frac) {
            subViewRoot.setVvalue(0);
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            if (frac < 0.25) {
                subViewRoot.getContent().setTranslateY(expandedHeight - curHeight * 4);
            } else {
                subViewRoot.getContent().setTranslateY(collapsedHeight);
            }
            subViewRoot.getContent().setOpacity(frac);
        }
    };
    
    private Animation unloadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(100));
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1 - frac);
            subViewRoot.getContent().setTranslateY(expandedHeight - curHeight);
            subViewRoot.getContent().setOpacity(1 - frac);
        }
    };
    
    private Animation loadLettersAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(1000));
        }
        protected void interpolate(double frac) {
        	letterBox.setPrefHeight(50);
    		letterBox.setOpacity(frac);
    		letterSeparator.setPrefHeight(25);
    		letterSeparator.setOpacity(frac);
        }
    };
    
    private Animation unloadLettersAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(100));
        }
        protected void interpolate(double frac) {
    		letterBox.setOpacity(1.0 - frac);
    		letterSeparator.setOpacity(1.0 - frac);
        }
    };
}
