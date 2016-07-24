package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Album;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.*;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AlbumsController implements Initializable, SubView {
	
    @FXML private ListView<ArrayList<Album>> grid;
    @FXML private VBox songBox;
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    @FXML private Label artistLabel;
    @FXML private Label albumLabel;
    @FXML private Separator horizontalSeparator;
    @FXML private Separator verticalSeparator;

    private ScrollBar scrollBar;
    private boolean isAlbumDetailCollapsed = true;
    
    // Initializes values used for animations.
    private double expandedHeight = 0;
    private double collapsedHeight = 0;
    
    private VBox selectedCell;
    private Album selectedAlbum;
    private Song selectedSong;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		songTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		ObservableList<Album> albums = Library.getAlbums();
		Collections.sort(albums);

        grid.setCellFactory(x -> new AlbumListCell<>(this));

        ObservableList<ArrayList<Album>> rows = FXCollections.observableArrayList();
        ArrayList<Album> row = new ArrayList<>();
        int col;

        for (int i = 0; i < albums.size(); i++) {
            col = i % 5;
            if (col == 0) {
                row = new ArrayList<>();
                rows.add(row);
            }
            row.add(albums.get(i));
        }

        grid.setItems(rows);
        
		// Sets the song table to be invisible when the view is initialized.
        songBox.setVisible(false);

        grid.heightProperty().addListener((obs, oldValue, newValue) -> {
        	expandedHeight = newValue.doubleValue() / 2.0;
        	if (!isAlbumDetailCollapsed) {
        		songBox.setPrefHeight(expandedHeight);
        	}
        });
        
        // Sets preferred column width.
        titleColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        
        songTable.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        	songTable.requestFocus();
        	event.consume();
        });
        
        // Sets the playing properties for the songs in the song table.
        songTable.setRowFactory(x -> {
            TableRow<Song> tableRow = new TableRow<Song>();

            PseudoClass playing = PseudoClass.getPseudoClass("playing");

            ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) -> {
                tableRow.pseudoClassStateChanged(playing, newValue);
            };

            tableRow.itemProperty().addListener((obs, previousSong, currentSong) -> {
            	if (previousSong != null) {
            		previousSong.playingProperty().removeListener(changeListener);
            	}
            	if (currentSong != null) {
                    currentSong.playingProperty().addListener(changeListener);
                    tableRow.pseudoClassStateChanged(playing, currentSong.getPlaying());
                } else {
                    tableRow.pseudoClassStateChanged(playing, false);
                }
            });

            tableRow.setOnMouseClicked(event -> {
            	TableViewSelectionModel<Song> sm = songTable.getSelectionModel();
                if (event.getClickCount() == 2 && !tableRow.isEmpty()) {
                    play();
                } else if (event.isShiftDown()) {
                	ArrayList<Integer> indices = new ArrayList<Integer>(sm.getSelectedIndices());
                	if (indices.size() < 1) {
                		if (indices.contains(tableRow.getIndex())) {
                    		sm.clearSelection(tableRow.getIndex());
                    	} else {
                    		sm.select(tableRow.getItem());
                    	}
                	} else {
                		sm.clearSelection();
	                	indices.sort((first, second) -> first.compareTo(second));
	                	int max = indices.get(indices.size() - 1);
	                	int min = indices.get(0);
	                	if (min < tableRow.getIndex()) {
	                		for (int i = min; i <= tableRow.getIndex(); i++) {
	                			sm.select(i);
	                		}
	                	} else {
	                		for (int i = tableRow.getIndex(); i <= max; i++) {
	                			sm.select(i);
	                		}
	                	}
                	}
                	
                } else if (event.isControlDown()) {
                	if (sm.getSelectedIndices().contains(tableRow.getIndex())) {
                		sm.clearSelection(tableRow.getIndex());
                	} else {
                		sm.select(tableRow.getItem());
                	}
                } else {
                	if (sm.getSelectedIndices().size() > 1) {
                		sm.clearSelection();
                    	sm.select(tableRow.getItem());
                	} else if (sm.getSelectedIndices().contains(tableRow.getIndex())) {
                		sm.clearSelection();
                	} else {
                		sm.clearSelection();
                    	sm.select(tableRow.getItem());
                	}
                }
            });
            
            tableRow.setOnDragDetected(event -> {
            	Dragboard db = tableRow.startDragAndDrop(TransferMode.ANY);
            	ClipboardContent content = new ClipboardContent();
            	if (songTable.getSelectionModel().getSelectedIndices().size() > 1) {
            		content.putString("List");
                    db.setContent(content);
                	MusicPlayer.setDraggedItem(songTable.getSelectionModel().getSelectedItems());
            	} else {
            		content.putString("Song");
                    db.setContent(content);
                	MusicPlayer.setDraggedItem(tableRow.getItem());
            	}
            	ImageView image = new ImageView(tableRow.snapshot(null, null));
            	Rectangle2D rectangle = new Rectangle2D(0, 0, 250, 50);
            	image.setViewport(rectangle);
            	db.setDragView(image.snapshot(null, null), 125, 25);
                event.consume();
            });
            return tableRow ;
        });
        
        songTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        	if (oldSelection != null) {
        		oldSelection.setSelected(false);
        	}
        	if (newSelection != null && songTable.getSelectionModel().getSelectedIndices().size() == 1) {
        		newSelection.setSelected(true);
        		selectedSong = newSelection;
        	}
        });
        
        // Plays selected song when enter key is pressed.
        songTable.setOnKeyPressed(event -> {
        	if (event.getCode().equals(KeyCode.ENTER)) {
        		play();
        	}
        });

        horizontalSeparator.setOnMouseDragged(e -> {

            expandedHeight = MusicPlayer.getStage().getHeight() - e.getSceneY() - 75;

            if (expandedHeight > grid.getHeight() * 0.75) {
                expandedHeight = grid.getHeight() * 0.75;
            } else if (expandedHeight < grid.getHeight() * 0.25) {
                expandedHeight = grid.getHeight() * 0.25;
            }

            songBox.setPrefHeight(expandedHeight);
            e.consume();
        });
	}

    @Override
    public void dispose() {

    }

    public boolean isAlbumDetailCollapsed() { return isAlbumDetailCollapsed; }

    public void selectAlbum(Album album, VBox cell) {
        selectedAlbum = album;
        selectedCell = cell;
    }

    public Album getSelectedAlbum() { return selectedAlbum; }

    public VBox getSelectedCell() { return selectedCell; }

    public void setHeader(String artist, String album) {
        artistLabel.setText(artist);
        albumLabel.setText(album);
    }
    
    public void expandAlbumDetail() {
    	isAlbumDetailCollapsed = false;
    	songBox.setVisible(true);
    }
    
    public void collapseAlbumDetail() {
    	isAlbumDetailCollapsed = true;
    	songTable.getItems().clear();
    	songBox.setVisible(false);
    }
    
    public void populateSongTable(VBox cell, Album selectedAlbum) {
    	// Retrieves albums songs and stores them as an observable list.
    	ObservableList<Song> albumSongs = FXCollections.observableArrayList(selectedAlbum.getSongs());
    	
        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ControlPanelTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        // Sets each column item.
        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));
        
        // Adds songs to table.
        songTable.setItems(albumSongs);
        double height = (albumSongs.size() + 1) * 50 + 2;
        Animation songTableLoadAnimation = new Transition() {
        	{
        		setCycleDuration(Duration.millis(250));
                setInterpolator(Interpolator.EASE_BOTH);
        	}
        	
        	protected void interpolate(double frac) {
        		songTable.setMinHeight(frac * height);
                songTable.setPrefHeight(frac * height);
        	}
        };
        songTableLoadAnimation.play();
    }
    
    @Override
    public void play() {
    	
    	Song song = selectedSong;
        ObservableList<Song> songList = songTable.getItems();
        if (MusicPlayer.isShuffleActive()) {
        	Collections.shuffle(songList);
        	songList.remove(song);
        	songList.add(0, song);
        }
        MusicPlayer.setNowPlayingList(songList);
        MusicPlayer.setNowPlaying(song);
        MusicPlayer.play();
    }

    @Override
    public void scroll(char letter) {

        int index = 0;
        double cellHeight = ((AlbumListCell) grid.lookup(".album-list-cell")).getHeight();

        ObservableList<ArrayList<Album>> children = grid.getItems();

        for (ArrayList<Album> row : children) {
            for (Album album : row) {
                char firstLetter = removeArticle(album.getTitle()).charAt(0);
                if (firstLetter < letter) {
                    index++;
                }
            }
        }

        if (scrollBar == null) {
            scrollBar = (ScrollBar) grid.lookup(".scroll-bar");
        }

        double row = index / 5;
        double startVvalue = scrollBar.getValue();
        double finalVvalue = (row * cellHeight) / (grid.getItems().size() * cellHeight - scrollBar.getHeight());

        Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }
            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                scrollBar.setValue(vValue);
            }
        };
        scrollAnimation.play();
    }
    
    private String removeArticle(String title) {

        String arr[] = title.split(" ", 2);

        if (arr.length < 2) {
            return title;
        } else {

            String firstWord = arr[0];
            String theRest = arr[1];

            switch (firstWord) {
                case "A":
                case "An":
                case "The":
                    return theRest;
                default:
                    return title;
            }
        }
    }
    
    public Song getSelectedSong() {
    	return selectedSong;
    }

    public ListView<ArrayList<Album>> getGrid() {
        return grid;
    }

    public Animation getExpandAnimation() {
        return expandAnimation;
    }

    public Animation getCollapseAnimation() {
        return collapseAnimation;
    }

    public Animation getTableExpandAnimation() {
        return tableExpandAnimation;
    }

    public Animation getTableCollapseAnimation() {
        return tableCollapseAnimation;
    }
    
    // Animation to display song table when an album is clicked and the song table is collapsed.
    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            songBox.setPrefHeight(curHeight);
            songBox.setOpacity(frac);
        }
    };
    
    // Animation to hide song table when an album is clicked and the song table is expanded.
    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> collapseAlbumDetail());
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
        	double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (1.0 - frac);
            songBox.setPrefHeight(curHeight);
            songBox.setOpacity(1.0 - frac);
            songTable.setMinHeight(1 - frac);
            songTable.setPrefHeight(1 - frac);
        }
    };

    private Animation tableCollapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> collapseAlbumDetail());
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
        	double curLocation = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            artistLabel.setTranslateY(curLocation);
            albumLabel.setTranslateY(curLocation);
            verticalSeparator.setTranslateY(curLocation);
        	songTable.setTranslateY(curLocation);
        	artistLabel.setOpacity(1.0 - frac);
            albumLabel.setOpacity(1.0 - frac);
            verticalSeparator.setOpacity(1.0 - frac);
        	songTable.setOpacity(1.0 - frac);
        }
    };

    private Animation tableExpandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setInterpolator(Interpolator.EASE_BOTH);
        }
        protected void interpolate(double frac) {
        	double curLocation = collapsedHeight + (expandedHeight - collapsedHeight) * (1.0 - frac);
        	artistLabel.setTranslateY(curLocation);
            albumLabel.setTranslateY(curLocation);
            verticalSeparator.setTranslateY(curLocation);
            songTable.setTranslateY(curLocation);
            artistLabel.setOpacity(frac);
            albumLabel.setOpacity(frac);
            verticalSeparator.setOpacity(frac);
        	songTable.setOpacity(frac);
        }
    };
}
