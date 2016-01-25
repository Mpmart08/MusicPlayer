package app.musicplayer.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Album;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.ClippedTableCell;
import app.musicplayer.util.PlayingTableCell;
import app.musicplayer.util.Scrollable;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ArtistsMainController implements Initializable, Scrollable {

    public class ArtistCell extends ListCell<Artist> {

        HBox cell = new HBox();
        ImageView artistImage = new ImageView();
        Label title = new Label();

        public ArtistCell() {

            super();
            artistImage.setFitWidth(40);
            artistImage.setFitHeight(40);
            artistImage.setPreserveRatio(true);
            artistImage.setSmooth(true);
            artistImage.setCache(true);
            title.setTextOverrun(OverrunStyle.CLIP);
            cell.getChildren().addAll(artistImage, title);
            cell.setAlignment(Pos.CENTER_LEFT);
            HBox.setMargin(artistImage, new Insets(0, 10, 0, 0));
            this.setPrefWidth(248);
        }

        @Override
        protected void updateItem(Artist artist, boolean empty) {

            super.updateItem(artist, empty);

            if (empty){

                setGraphic(null);

            } else {

                title.setText(artist.getTitle());
                artistImage.setImage(artist.getArtistImage());
                setGraphic(cell);
            }
        }
    }

    public class AlbumCell extends ListCell<Album> {

        ImageView albumArtwork = new ImageView();

        public AlbumCell() {

            super();
            setAlignment(Pos.CENTER);
            setPrefHeight(140);
            setPrefWidth(140);
            albumArtwork.setFitWidth(130);
            albumArtwork.setFitHeight(130);
            albumArtwork.setPreserveRatio(true);
            albumArtwork.setSmooth(true);
            albumArtwork.setCache(true);
        }

        @Override
        protected void updateItem(Album album, boolean empty) {

            super.updateItem(album, empty);

            if (empty){

                setGraphic(null);

            } else {

                albumArtwork.setImage(album.getArtwork());
                setGraphic(albumArtwork);
            }
        }
    }

    @FXML private ScrollPane scrollPane;
    @FXML private ListView<Artist> artistList;
    @FXML private ListView<Album> albumList;
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;
    @FXML private Label artistLabel;
    @FXML private Label albumLabel;
    @FXML private Separator separator;
    @FXML private VBox subViewRoot;

    private Album selectedAlbum;
    private Artist selectedArtist;
    private double expandedHeight = 50;
    private double collapsedHeight = 0;

    private Animation artistLoadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(1000));
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            if (frac < 0.25) {
                subViewRoot.setTranslateY(expandedHeight - curHeight * 4);
            } else {
                subViewRoot.setTranslateY(collapsedHeight);
            }
            subViewRoot.setOpacity(frac);
        }
    };

    private Animation albumLoadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(1000));
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            if (frac < 0.25) {
                songTable.setTranslateY(expandedHeight - curHeight * 4);
            } else {
                songTable.setTranslateY(collapsedHeight);
            }
            songTable.setOpacity(frac);
        }
    };
    
    @Override
    public void scroll(char letter) {
    	ObservableList<Artist> artistListItems = artistList.getItems();
    	int selectedCell = 0;

    	for (int i = 0; i < artistListItems.size(); i++) {
    		// Removes article from artist title and compares it to selected letter.
    		String artistTitle = artistListItems.get(i).getTitle();
    		char firstLetter = removeArticle(artistTitle).charAt(0);
    		if (firstLetter < letter) {
        		selectedCell++;
    		}
    	}
    	
    	double startVvalue = scrollPane.getVvalue();
    	double finalVvalue = (double) (selectedCell * 50) / (artistList.getHeight() - scrollPane.getHeight());
    	
    	Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }
            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                scrollPane.setVvalue(vValue);
                
                // TODO: DEBUG
                System.out.println("V value: " + vValue);
            }
        };
        scrollAnimation.play();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        titleColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().subtract(50).multiply(0.25));

        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("length"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));

        albumList.setCellFactory(listView -> new AlbumCell());
        artistList.setCellFactory(listView -> new ArtistCell());

        ObservableList<Artist> artists = FXCollections.observableArrayList(Library.getArtists());
        Collections.sort(artists);
        
        // Sets the artist list height to the height required to fit the list view with all the artists.
        // This is important so that the scrolling is done in the scroll pane which is important for the scroll animation.
        artistList.setPrefHeight(50*artists.size());
        artistList.setMinHeight(artistList.getPrefHeight());
        
        artistList.setItems(artists);

        artistList.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                Thread thread = new Thread(() -> {
                    ObservableList<Song> songs = FXCollections.observableArrayList();
                    ObservableList<Album> albums = FXCollections.observableArrayList();
                    for (Album album : selectedArtist.getAlbums()) {
                        albums.add(album);
                        for (Song song : album.getSongs()) {
                            songs.add(song);
                        }
                    }

                    Collections.sort(songs, (first, second) -> {

                        Album firstAlbum = albums.stream().filter(x -> x.getTitle().equals(first.getAlbum())).findFirst().get();
                        Album secondAlbum = albums.stream().filter(x -> x.getTitle().equals(second.getAlbum())).findFirst().get();
                        if (firstAlbum.compareTo(secondAlbum) != 0) {
                            return firstAlbum.compareTo(secondAlbum);
                        } else {
                            return first.compareTo(second);
                        }
                    });

                    Song song = songs.get(0);
                    MusicPlayer.setNowPlayingList(songs);
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                });

                thread.start();

            } else {

                if (selectedArtist != artistList.getSelectionModel().getSelectedItem()) {

                    selectedArtist = artistList.getSelectionModel().getSelectedItem();
                    showAllSongs(selectedArtist);
                    artistLabel.setText(selectedArtist.getTitle());
                    albumList.setMaxWidth(albumList.getItems().size() * 150 + 2);
                    albumList.scrollTo(0);

                    if (artistLoadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                        artistLoadAnimation.stop();
                    }
                    artistLoadAnimation.play();

                } else {

                    showAllSongs(selectedArtist);
                    if (albumLoadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                        albumLoadAnimation.stop();
                    }
                    albumLoadAnimation.play();
                }
            }
        });

        artistList.setOnKeyPressed(event -> {

            KeyCode key = event.getCode();
            int index = -1;
            switch (key) {
                case DOWN:
                    index = artistList.getSelectionModel().getSelectedIndex() + 1;
                    break;
                case UP:
                    index = artistList.getSelectionModel().getSelectedIndex() - 1;
                    break;
			default:
				break;
            }

            if (index >= 0 && index < artists.size()) {
                Artist artist = artists.get(index);
                selectedArtist = artist;
                showAllSongs(selectedArtist);
                artistLabel.setText(selectedArtist.getTitle());
                albumList.setMaxWidth(albumList.getItems().size() * 150 + 2);
                albumList.scrollTo(0);
                if (artistLoadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    artistLoadAnimation.stop();
                }
                artistLoadAnimation.play();
            }
        });

        albumList.setOnMouseClicked(event -> {

            Album album = albumList.getSelectionModel().getSelectedItem();

            if (event.getClickCount() == 2) {

                if (album != selectedAlbum) {
                    selectAlbum(album);
                }

                ArrayList<Song> songs = selectedAlbum.getSongs();

                Collections.sort(songs);

                MusicPlayer.setNowPlayingList(songs);
                MusicPlayer.setNowPlaying(songs.get(0));
                MusicPlayer.play();

            } else {

                selectAlbum(album);
                if (albumLoadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    albumLoadAnimation.stop();
                }
                albumLoadAnimation.play();
            }
        });

        albumList.setOnKeyPressed(event -> {

            KeyCode key = event.getCode();
            int index = -1;
            switch (key) {
                case LEFT:
                    index = albumList.getSelectionModel().getSelectedIndex() - 1;
                    break;
                case RIGHT:
                    index = albumList.getSelectionModel().getSelectedIndex() + 1;
                    break;
			default:
				break;
            }

            if (index >= 0 && index < selectedArtist.getAlbums().size()) {
                Album album = albumList.getItems().get(index);
                selectAlbum(album);
                if (albumLoadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    albumLoadAnimation.stop();
                }
                albumLoadAnimation.play();
            }
        });

        songTable.setRowFactory(x -> {

            TableRow<Song> row = new TableRow<Song>();

            PseudoClass playing = PseudoClass.getPseudoClass("playing");

            ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) -> {
                row.pseudoClassStateChanged(playing, newValue.booleanValue());
            };

            row.itemProperty().addListener((obs, previousSong, currentSong) -> {
            	if (previousSong != null) {
            		previousSong.playingProperty().removeListener(changeListener);
            	}
                if (currentSong != null) {
                    currentSong.playingProperty().addListener(changeListener);
                    row.pseudoClassStateChanged(playing, currentSong.getPlaying());
                } else {
                    row.pseudoClassStateChanged(playing, false);
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    Song song = row.getItem();
                    ArrayList<Song> songs = new ArrayList<Song>();

                    if (selectedAlbum != null) {

                        songs.addAll(selectedAlbum.getSongs());

                    } else {

                        for (Album album : selectedArtist.getAlbums()) {
                        	songs.addAll(album.getSongs());
                        }
                    }

                    Collections.sort(songs, (first, second) -> {

                        Album firstAlbum = Library.getAlbum(first.getAlbum());
                        Album secondAlbum = Library.getAlbum(second.getAlbum());
                        if (firstAlbum.compareTo(secondAlbum) != 0) {
                            return firstAlbum.compareTo(secondAlbum);
                        } else {
                            return first.compareTo(second);
                        }
                    });

                    MusicPlayer.setNowPlayingList(songs);

                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });

            return row ;
        });
    }

    public void selectAlbum(Album album) {

        if (selectedAlbum == album) {

            albumList.getSelectionModel().clearSelection();
            showAllSongs(artistList.getSelectionModel().getSelectedItem());

        } else {
        	
            selectedAlbum = album;
            albumList.getSelectionModel().select(selectedAlbum);
            ObservableList<Song> songs = FXCollections.observableArrayList();
            songs.addAll(album.getSongs());
            Collections.sort(songs);
            songTable.getSelectionModel().clearSelection();
            songTable.setItems(songs);
            songTable.scrollTo(0);
            albumLabel.setText(album.getTitle());
        }
    }

    private void showAllSongs(Artist artist) {

        ObservableList<Album> albums = FXCollections.observableArrayList();
        ObservableList<Song> songs = FXCollections.observableArrayList();

        for (Album album : artist.getAlbums()) {

            albums.add(album);

            for (Song song : album.getSongs()) {

                songs.add(song);
            }
        }

        Collections.sort(songs, (first, second) -> {

            Album firstAlbum = albums.stream().filter(x -> x.getTitle().equals(first.getAlbum())).findFirst().get();
            Album secondAlbum = albums.stream().filter(x -> x.getTitle().equals(second.getAlbum())).findFirst().get();
            if (firstAlbum.compareTo(secondAlbum) != 0) {
                return firstAlbum.compareTo(secondAlbum);
            } else {
                return first.compareTo(second);
            }
        });

        Collections.sort(albums);

        selectedAlbum = null;
        albumList.getSelectionModel().clearSelection();
        albumList.setItems(albums);
        songTable.setItems(songs);
        songTable.getSelectionModel().clearSelection();
        songTable.scrollTo(0);
        songTable.setVisible(true);
        albumLabel.setText("All Songs");
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

    public void selectArtist(Artist artist) {

        selectedArtist = artist;
        artistList.getSelectionModel().select(artist);
        artistList.scrollTo(artistList.getSelectionModel().getSelectedIndex());
        showAllSongs(artist);
        albumList.setMaxWidth(albumList.getItems().size() * 150 + 2);
        artistLabel.setText(artist.getTitle());
        separator.setVisible(true);
    }
    
    public void selectSong(Song song) {
    	
    	songTable.getSelectionModel().select(song);
    	songTable.scrollTo(song);
    }
}