package app.musicplayer.view;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Album;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Library;
import app.musicplayer.model.MostPlayedPlaylist;
import app.musicplayer.model.Playlist;
import app.musicplayer.model.RecentlyPlayedPlaylist;
import app.musicplayer.model.Song;
import app.musicplayer.util.CustomSliderSkin;
import app.musicplayer.util.Resources;
import app.musicplayer.util.SubView;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainController implements Initializable {

	private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;
    private double expandedHeight = 50;
    private double collapsedHeight = 0;
    private SubView subViewController;
    private CustomSliderSkin sliderSkin;
    private Stage volumePopup;
    private VolumePopupController volumePopupController;

    @FXML private BorderPane mainWindow;
    @FXML private ScrollPane subViewRoot;
    @FXML private VBox sideBar;
    @FXML private VBox playlistBox;
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
    	
    	sliderSkin = new CustomSliderSkin(timeSlider);
    	timeSlider.setSkin(sliderSkin);
    	
    	createVolumePopup();
    	
    	PseudoClass active = PseudoClass.getPseudoClass("active");
    	loopButton.setOnMouseClicked(x -> {
    		sideBar.requestFocus();
    		MusicPlayer.toggleLoop();
    		loopButton.pseudoClassStateChanged(active, MusicPlayer.isLoopActive());
    	});
    	shuffleButton.setOnMouseClicked(x -> {
    		sideBar.requestFocus();
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
        initializePlaylists();
        
        // Loads the default view: artists.
        loadView("artists");
    } // End initialize()
    
    private void initializePlaylists() {
    	
    	for (Playlist playlist : Library.getPlaylists()) {
    		try {
    			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "PlaylistCell.fxml"));
				HBox cell = (HBox) loader.load();
				Label label = (Label) cell.getChildren().get(1);
				label.setText(playlist.getTitle());
				
				cell.setOnMouseClicked(x -> {
					selectView(x);
					((PlaylistsController) subViewController).selectPlaylist(playlist);
				});
				
				cell.setOnDragDetected(event -> {
					PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
					cell.pseudoClassStateChanged(pressed, false);
    	        	Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
    	        	ClipboardContent content = new ClipboardContent();
    	            content.putString("Playlist");
    	            db.setContent(content);
    	        	MusicPlayer.setDraggedItem(playlist);
    	        	db.setDragView(cell.snapshot(null, null));
    	            event.consume();
    	        });
				
				PseudoClass hover = PseudoClass.getPseudoClass("hover");
				
				cell.setOnDragEntered(event -> {
					if (!(playlist instanceof MostPlayedPlaylist)
							&& !(playlist instanceof RecentlyPlayedPlaylist)
							&& event.getGestureSource() != cell
							&& event.getDragboard().hasString()) {
						
						cell.pseudoClassStateChanged(hover, true);
						//cell.getStyleClass().setAll("sideBarItemSelected");
					}
				});
				
				cell.setOnDragExited(event -> {
					if (!(playlist instanceof MostPlayedPlaylist)
							&& !(playlist instanceof RecentlyPlayedPlaylist)
							&& event.getGestureSource() != cell
							&& event.getDragboard().hasString()) {
						
						cell.pseudoClassStateChanged(hover, false);
						//cell.getStyleClass().setAll("sideBarItem");
					}
				});
				
				cell.setOnDragOver(event -> {
					if (!(playlist instanceof MostPlayedPlaylist)
							&& !(playlist instanceof RecentlyPlayedPlaylist)
							&& event.getGestureSource() != cell
							&& event.getDragboard().hasString()) {
						
						event.acceptTransferModes(TransferMode.ANY);
					}
					event.consume();
				});
				
				cell.setOnDragDropped(event -> {
					switch (event.getDragboard().getString()) {
		            case "Artist":
		            	Artist artist = (Artist) MusicPlayer.getDraggedItem();
			            for (Album album : artist.getAlbums()) {
			            	for (Song song : album.getSongs()) {
			            		if (!playlist.getSongs().contains(song)) {
					            	playlist.addSong(song);
			            		}
				            }
			            }
			            break;
		            case "Album":
		            	Album album = (Album) MusicPlayer.getDraggedItem();
			            for (Song song : album.getSongs()) {
			            	if (!playlist.getSongs().contains(song)) {
				            	playlist.addSong(song);
		            		}
			            }
			            break;
		            case "Playlist":
		            	Playlist list = (Playlist) MusicPlayer.getDraggedItem();
			            for (Song song : list.getSongs()) {
			            	if (!playlist.getSongs().contains(song)) {
				            	playlist.addSong(song);
		            		}
			            }
			            break;
		            case "Song":
		            	Song song = (Song) MusicPlayer.getDraggedItem();
		            	if (!playlist.getSongs().contains(song)) {
			            	playlist.addSong(song);
	            		}
			            break;
		            }
					
					event.consume();
				});
				
				playlistBox.getChildren().add(cell);
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
    	}
    }
    
    @FXML
    private void newPlaylist() {
    	
    	if (!newPlaylistAnimation.getStatus().equals(Status.RUNNING)) {
    		
    		try {
        		
    			FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "PlaylistCell.fxml"));
    			HBox cell = (HBox) loader.load();
    			
    			Label label = (Label) cell.getChildren().get(1);
    			label.setVisible(false);
    			HBox.setMargin(label, new Insets(0, 0, 0, 0));
    			
    			TextField textBox = new TextField();
    			textBox.setPrefHeight(30);
    			cell.getChildren().add(textBox);
    			HBox.setMargin(textBox, new Insets(10, 10, 10, 9));
    			
    			textBox.focusedProperty().addListener((obs, oldValue, newValue) -> {
    				if (oldValue && !newValue) {
    					String text = textBox.getText().equals("") ? "New Playlist" : textBox.getText();
    					text = checkDuplicatePlaylist(text, 0);
    					label.setText(text);
        				cell.getChildren().remove(textBox);
        				HBox.setMargin(label, new Insets(10, 10, 10, 10));
        				label.setVisible(true);
        				Library.addPlaylist(text);
    				}
    			});
    			
    			textBox.setOnKeyPressed(x -> {
    				if (x.getCode() == KeyCode.ENTER)  {
    		            sideBar.requestFocus();
    		        }
    			});
    			
    			cell.setOnMouseClicked(x -> {
    				selectView(x);
    				Playlist playlist = Library.getPlaylist(label.getText());
    				((PlaylistsController) subViewController).selectPlaylist(playlist);
    			});
    			
    			cell.setOnDragDetected(event -> {
    				PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
					cell.pseudoClassStateChanged(pressed, false);
    				Playlist playlist = Library.getPlaylist(label.getText());
    	        	Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
    	        	ClipboardContent content = new ClipboardContent();
    	            content.putString("Playlist");
    	            db.setContent(content);
    	        	MusicPlayer.setDraggedItem(playlist);
    	        	SnapshotParameters sp = new SnapshotParameters();
    	        	sp.setTransform(Transform.scale(1.5, 1.5));
    	        	db.setDragView(cell.snapshot(sp, null));
    	            event.consume();
    	        });
    			
    			PseudoClass hover = PseudoClass.getPseudoClass("hover");
				
    			cell.setOnDragEntered(event -> {
    				Playlist playlist = Library.getPlaylist(label.getText());
					if (!(playlist instanceof MostPlayedPlaylist)
							&& !(playlist instanceof RecentlyPlayedPlaylist)
							&& event.getGestureSource() != cell
							&& event.getDragboard().hasString()) {
						
						cell.pseudoClassStateChanged(hover, true);
						//cell.getStyleClass().setAll("sideBarItemSelected");
					}
				});
				
				cell.setOnDragExited(event -> {
					Playlist playlist = Library.getPlaylist(label.getText());
					if (!(playlist instanceof MostPlayedPlaylist)
							&& !(playlist instanceof RecentlyPlayedPlaylist)
							&& event.getGestureSource() != cell
							&& event.getDragboard().hasString()) {
						
						cell.pseudoClassStateChanged(hover, false);
						//cell.getStyleClass().setAll("sideBarItem");
					}
				});
				
				cell.setOnDragOver(event -> {
					Playlist playlist = Library.getPlaylist(label.getText());
					if (!(playlist instanceof MostPlayedPlaylist)
							&& !(playlist instanceof RecentlyPlayedPlaylist)
							&& event.getGestureSource() != cell
							&& event.getDragboard().hasString()) {
						
						event.acceptTransferModes(TransferMode.ANY);
					}
					event.consume();
				});
				
				cell.setOnDragDropped(event -> {
					Playlist playlist = Library.getPlaylist(label.getText());
		            switch (event.getDragboard().getString()) {
		            case "Artist":
		            	Artist artist = (Artist) MusicPlayer.getDraggedItem();
			            for (Album album : artist.getAlbums()) {
			            	for (Song song : album.getSongs()) {
			            		if (!playlist.getSongs().contains(song)) {
					            	playlist.addSong(song);
			            		}
				            }
			            }
			            break;
		            case "Album":
		            	Album album = (Album) MusicPlayer.getDraggedItem();
			            for (Song song : album.getSongs()) {
			            	if (!playlist.getSongs().contains(song)) {
				            	playlist.addSong(song);
		            		}
			            }
			            break;
		            case "Playlist":
		            	Playlist list = (Playlist) MusicPlayer.getDraggedItem();
			            for (Song song : list.getSongs()) {
			            	if (!playlist.getSongs().contains(song)) {
				            	playlist.addSong(song);
		            		}
			            }
			            break;
		            case "Song":
		            	Song song = (Song) MusicPlayer.getDraggedItem();
		            	if (!playlist.getSongs().contains(song)) {
			            	playlist.addSong(song);
	            		}
			            break;
		            }
			        event.consume();
				});
    			
    			cell.setPrefHeight(0);
    			cell.setOpacity(0);
    			
    			playlistBox.getChildren().add(1, cell);
    			
    			textBox.requestFocus();
    			
    		} catch (Exception e) {
    			
    			e.printStackTrace();
    		}
        	
        	newPlaylistAnimation.play();
    	}
    }
    
    private String checkDuplicatePlaylist(String text, int i) {
    	
    	for (Playlist playlist : Library.getPlaylists()) {
    		if (playlist.getTitle().equals(text)) {
    			
    			int index = text.lastIndexOf(' ') + 1;
    			if (index != 0) {
    				try {
    					i = Integer.parseInt(text.substring(index));
    				} catch (Exception ex) {
    					// do nothing
    				}
    			}
    			
    			i++;
    			
    			if (i == 1) {
    				text = checkDuplicatePlaylist(text + " " + i, i);
    			} else {
    				text = checkDuplicatePlaylist(text.substring(0, index) + i, i);
    			}
    			break;
    		}
    	}
    	
    	return text;
    }

    @FXML
    private void selectView(Event e) {

        HBox eventSource = ((HBox)e.getSource());
        
        eventSource.requestFocus();

        Optional<Node> previous = sideBar.getChildren().stream()
            .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();

        if (previous.isPresent()) {
            HBox previousItem = (HBox) previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        } else {
        	previous = playlistBox.getChildren().stream()
                    .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();
        	if (previous.isPresent()) {
                HBox previousItem = (HBox) previous.get();
                previousItem.getStyleClass().setAll("sideBarItem");
            }
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
            HBox previousItem = (HBox) previous.get();
            previousItem.getStyleClass().setAll("sideBarItem");
        } else {
        	previous = playlistBox.getChildren().stream()
                    .filter(x -> x.getStyleClass().get(0).equals("sideBarItemSelected")).findFirst();
        	if (previous.isPresent()) {
                HBox previousItem = (HBox) previous.get();
                previousItem.getStyleClass().setAll("sideBarItem");
            }
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
    private void slideSideBar(Event e) {

    	sideBar.requestFocus();
    	
        if (isSideBarExpanded) {
            collapseSideBar();
        } else {
            expandSideBar();
        }
    }

    @FXML
    public void playPause() {

    	sideBar.requestFocus();
    	
        if (MusicPlayer.isPlaying()) {
            MusicPlayer.pause();
        } else {
            MusicPlayer.play();
        }
    }

    @FXML
    private void back() {

    	sideBar.requestFocus();
        MusicPlayer.back();
    }

    @FXML
    private void skip() {

    	sideBar.requestFocus();
        MusicPlayer.skip();
    }
    
    @FXML
    private void letterClicked(Event e) {
    	
    	sideBar.requestFocus();
    	Label eventSource = ((Label)e.getSource());
    	char letter = eventSource.getText().charAt(0);
    	subViewController.scroll(letter);
    }
    
    public void volumeClick() {
    	if (!volumePopup.isShowing()) {
    		volumePopup.show();
    		popupShowAnimation.play();
    	}
    }
    
    public Slider getVolumeSlider() {
    	return volumePopupController.getSlider();
    }
    
    public boolean isTimeSliderPressed() {
    	return sliderSkin.getThumb().isPressed() || sliderSkin.getTrack().isPressed();
    }
    
    public SubView getSubViewController() {
    	
    	return subViewController;
    }
    
    public ScrollPane getScrollPane() {
    	return this.subViewRoot;
    }

    public SubView loadView(String viewName) {
    	// If new songs have been added to the music directory while the app is running.
    	if (!Library.getNewSongs().isEmpty()) {
    		// TODO: DEBUG
    		System.out.println("MC586_New songs added to library while app was running!");
    		
            // Adds the new song to the xml file.
    		Library.editCreateXMLFile();
    		
            // Updates the array lists containing songs, albums, and artists in the library.
            Library.updateSongsList();
            Library.updateAlbumsList();
            Library.updateArtistsList();
            
            // Clears the new songs array list to prevent duplicate songs from being added to the library.
            Library.clearNewSongs();
            
        // Else if new songs have been deleted from the music directory while the app is running.
    	}

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
	        		Platform.runLater(() -> {
	        			Library.getSongs().stream().filter(x -> x.getSelected()).forEach(x -> x.setSelected(false));
	        			subViewRoot.setVisible(false);
			        	subViewRoot.setContent(view);
			        	subViewRoot.getContent().setOpacity(0);
			        	latch.countDown();
	        		});
		        	return null;
	        	}
	        };
	        
	        task.setOnSucceeded(x -> {
	        	new Thread(() -> {
	        		try {
						latch.await();
					} catch (Exception e) {
						e.printStackTrace();
					}
		        	Platform.runLater(() -> {
		        		subViewRoot.setVisible(true);
		        		if (loadLettersFinal) {
			        		loadLettersAnimation.play();
			        	}
			        	loadViewAnimation.play();
		        	});
	        	}).start();
	        });
	        
	        Thread thread = new Thread(task);
            
            unloadViewAnimation.setOnFinished(x -> {
            	thread.start();
            });
            
            if (subViewRoot.getContent() != null) {
            	if (unloadLettersFinal) {
            		unloadLettersAnimation.play();
            	}
	            unloadViewAnimation.play();
        	} else {
        		subViewRoot.setContent(view);
        		if (loadLettersFinal) {
        			loadLettersAnimation.play();
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

            collapseAnimation.play();
        }
    }

    private void expandSideBar() {
        if (expandAnimation.statusProperty().get() == Animation.Status.STOPPED
            && collapseAnimation.statusProperty().get() == Animation.Status.STOPPED) {

        	expandAnimation.play();
        }
    }

    private void setSlideDirection() {
        isSideBarExpanded = !isSideBarExpanded;
    }
    
    private void createVolumePopup() {
    	
    	try {
    		
    		Stage stage = MusicPlayer.getStage();
        	FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Resources.FXML + "VolumePopup.fxml"));
        	HBox view = (HBox) loader.load();
        	volumePopupController = loader.getController();
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
            setInterpolator(Interpolator.EASE_BOTH);
        }
    	
        protected void interpolate(double frac) {
        	volumePopup.setOpacity(frac);
        }
    };
    
    private Animation popupHideAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
            volumePopup.setOpacity(1.0 - frac);
        }
    };
    
    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
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
            setInterpolator(Interpolator.EASE_BOTH);
            setOnFinished(x -> {setSlideDirection();});
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
            sideBar.setPrefWidth(curWidth);
        }
    };

    private Animation loadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
            subViewRoot.setVvalue(0);
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            subViewRoot.getContent().setTranslateY(expandedHeight - curHeight);
            subViewRoot.getContent().setOpacity(frac);
        }
    };
    
    private Animation unloadViewAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1 - frac);
            subViewRoot.getContent().setTranslateY(expandedHeight - curHeight);
            subViewRoot.getContent().setOpacity(1 - frac);
        }
    };
    
    private Animation loadLettersAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
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
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
    		letterBox.setOpacity(1.0 - frac);
    		letterSeparator.setOpacity(1.0 - frac);
        }
    };
    
    private Animation newPlaylistAnimation = new Transition() {
    	{
            setCycleDuration(Duration.millis(500));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
    		HBox cell = (HBox) playlistBox.getChildren().get(1);
    		if (frac < 0.5) {
    			cell.setPrefHeight(frac * 100);
    		} else {
    			cell.setPrefHeight(50);
    			cell.setOpacity((frac - 0.5) * 2);
    		}
        }
    };
}
