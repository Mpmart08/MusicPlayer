package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ListView;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import java.util.Collections;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Separator;
import javafx.scene.control.ListCell;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.util.Duration;

public class ArtistsMainController implements Initializable, Refreshable {

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
            title.setMaxWidth(190);
            cell.getChildren().addAll(artistImage, title);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setMargin(artistImage, new Insets(0, 10, 0, 0));
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

	@FXML private ListView<Artist> artistList;
    @FXML private ListView<Album> albumList;
    @FXML private TableView<Song> songTable;
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
    public void initialize(URL location, ResourceBundle resources) {

        titleColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.25));

        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));

        albumList.setCellFactory(listView -> new AlbumCell());
        artistList.setCellFactory(listView -> new ArtistCell());

        ObservableList<Artist> artists = FXCollections.observableArrayList(Library.getArtists());
        Collections.sort(artists);
        artistList.setItems(artists);

        artistList.setOnMouseClicked(event -> {

            if (event.getClickCount() == 2) {

                Thread thread = new Thread(() -> {
                    ObservableList<Song> songs = FXCollections.observableArrayList();
                    for (int albumId : selectedArtist.getAlbumIds()) {
                        for (int songId : Library.getAlbum(albumId).getSongIds()) {
                            songs.add(Library.getSong(songId));
                        }
                    }
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

                ArrayList<Song> nowPlayingList = MusicPlayer.getNowPlayingList();
                ArrayList<Song> songs = new ArrayList<Song>();

                for (int songId : selectedAlbum.getSongIds()) {

                    songs.add(Library.getSong(songId));
                }

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
            }

            if (index >= 0 && index < selectedArtist.getAlbumIds().size()) {
                Album album = albumList.getItems().get(index);
                selectAlbum(album);
                if (albumLoadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    albumLoadAnimation.stop();
                }
                albumLoadAnimation.play();
            }
        });

        songTable.getSelectionModel().selectedItemProperty().addListener(

            (list, oldSelection, newSelection) -> {

                MusicPlayer.setSelectedSong(newSelection);
            }
        );

        songTable.setRowFactory(x -> {
            TableRow<Song> row = new TableRow<Song>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {

                    Song song = row.getItem();
                    ArrayList<Song> songs = MusicPlayer.getNowPlayingList();

                    if (!songs.contains(song)) {

                        songs.clear();

                        if (selectedAlbum != null) {

                            for (int songId : selectedAlbum.getSongIds()) {

                                songs.add(Library.getSong(songId));
                            }

                        } else {

                            for (int albumId : selectedArtist.getAlbumIds()) {

                                Album album = Library.getAlbum(albumId);

                                for (int songId : album.getSongIds()) {

                                    Song s = Library.getSong(songId);
                                    songs.add(s);
                                }
                            }
                        }

                        MusicPlayer.setNowPlayingList(songs);
                    }

                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });
            return row ;
        });
    }


    @Override
    public void refresh() {

        songTable.getColumns().get(0).setVisible(false);
        songTable.getColumns().get(0).setVisible(true);
    }

    private void selectAlbum(Album album) {

        if (selectedAlbum == album) {

            albumList.getSelectionModel().clearSelection();
            showAllSongs(artistList.getSelectionModel().getSelectedItem());

        } else {

            selectedAlbum = album;
            ObservableList<Song> songs = FXCollections.observableArrayList();

            for (int songId : album.getSongIds()) {

                Song song = Library.getSong(songId);
                songs.add(song);
            }

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

        for (int albumId : artist.getAlbumIds()) {

            Album album = Library.getAlbum(albumId);
            albums.add(album);

            for (int songId : album.getSongIds()) {

                Song song = Library.getSong(songId);
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

    public void selectArtist(Artist artist) {

        selectedArtist = artist;
        artistList.getSelectionModel().select(artist);
        artistList.scrollTo(artistList.getSelectionModel().getSelectedIndex());
        showAllSongs(artist);
        albumList.setMaxWidth(albumList.getItems().size() * 150 + 2);
        artistLabel.setText(artist.getTitle());
        separator.setVisible(true);
    }
}