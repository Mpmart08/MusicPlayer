package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
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
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Separator;
import javafx.util.Callback;
import javafx.scene.control.ListCell;

public class ArtistsController implements Initializable {

    public class ArtistCell extends ListCell<Artist>{

        HBox cell = new HBox();
        ImageView artistImage = new ImageView();
        Label title = new Label();

        public ArtistCell(){

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
        protected void updateItem(Artist artist, boolean empty){

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

    public class AlbumCell extends ListCell<Album>{

        ImageView albumArtwork = new ImageView();

        public AlbumCell(){

            super();
            setAlignment(Pos.CENTER);
            albumArtwork.setFitWidth(130);
            albumArtwork.setFitHeight(130);
            albumArtwork.setPreserveRatio(true);
            albumArtwork.setSmooth(true);
            albumArtwork.setCache(true);
        }

        @Override
        protected void updateItem(Album album, boolean empty){

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

    private Album selectedAlbum;
    private Artist selectedArtist;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        titleColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.5));
        lengthColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.25));
        playsColumn.prefWidthProperty().bind(songTable.widthProperty().multiply(0.25));

        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));

        albumList.setCellFactory(listView -> new AlbumCell());
        artistList.setCellFactory(listView -> new ArtistCell());

        ObservableList<Artist> artists = FXCollections.observableArrayList(Library.getArtists());
        Collections.sort(artists);
        artistList.setItems(artists);

        artistList.setOnMouseClicked(event -> {

            selectedArtist = artistList.getSelectionModel().getSelectedItem();
            showAllSongs(selectedArtist);
            artistLabel.setText(selectedArtist.getTitle());
            separator.setVisible(true);
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

            if (index >= 0 && index < Library.getArtists().size()) {
                Artist artist = artists.get(index);
                selectedArtist = artist;
                showAllSongs(selectedArtist);
                artistLabel.setText(selectedArtist.getTitle());
            }
        });

        albumList.setOnMouseClicked(event -> {

            Album album = albumList.getSelectionModel().getSelectedItem();
            selectAlbum(album);
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
                    MusicPlayer.setNowPlayingList(Library.getSongs());
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });
            return row ;
        });
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
            songTable.setItems(songs);
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

            if (first.getAlbum().compareTo(second.getAlbum()) != 0) {
                return first.getAlbum().compareTo(second.getAlbum());
            } else {
                return first.compareTo(second);
            }
        });
        Collections.sort(albums);
        selectedAlbum = null;
        albumList.setItems(albums);
        songTable.setItems(songs);
        songTable.setVisible(true);
        albumLabel.setText("All Songs");
    }
}