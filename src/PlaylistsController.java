package musicplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;
import javafx.scene.control.TableRow;
import javafx.css.PseudoClass;
import javafx.beans.value.ChangeListener;

public class PlaylistsController implements Initializable, Refreshable {

    @FXML private ListView<Playlist> playlistList;
    @FXML private TableView<Song> tableView;
    @FXML private TableColumn<Song, Boolean> playingColumn;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, String> lengthColumn;
    @FXML private TableColumn<Song, Integer> playsColumn;

    private Playlist selectedPlaylist;
    private double expandedHeight = 50;
    private double collapsedHeight = 0;

    private Animation loadAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(1000));
        }
        protected void interpolate(double frac) {
            double curHeight = collapsedHeight + (expandedHeight - collapsedHeight) * (frac);
            if (frac < 0.25) {
                tableView.setTranslateY(expandedHeight - curHeight * 4);
            } else {
                tableView.setTranslateY(collapsedHeight);
            }
            tableView.setOpacity(frac);
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<Playlist> playlists = Library.getPlaylists();
        playlistList.setItems(playlists);

        selectedPlaylist = playlists.get(0);
        ObservableList<Song> songs = FXCollections.observableArrayList(selectedPlaylist.getSongs());
        tableView.setItems(songs);

        titleColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.35));
        artistColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.35));
        lengthColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        playsColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));

        playingColumn.setCellFactory(x -> new PlayingTableCell<Song, Boolean>());
        titleColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        artistColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        lengthColumn.setCellFactory(x -> new ClippedTableCell<Song, String>());
        playsColumn.setCellFactory(x -> new ClippedTableCell<Song, Integer>());

        playingColumn.setCellValueFactory(new PropertyValueFactory<Song, Boolean>("playing"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("title"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("artist"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<Song, String>("lengthAsString"));
        playsColumn.setCellValueFactory(new PropertyValueFactory<Song, Integer>("playCount"));

        tableView.getSelectionModel().selectedItemProperty().addListener(

            (list, oldSelection, newSelection) -> {

                MusicPlayer.setSelectedSong(newSelection);
            }
        );

        tableView.setRowFactory(x -> {

            TableRow<Song> row = new TableRow<Song>();

            PseudoClass playing = PseudoClass.getPseudoClass("playing");

            ChangeListener<Boolean> changeListener = (obs, oldValue, newValue) -> {
                row.pseudoClassStateChanged(playing, newValue.booleanValue());
            };

            row.itemProperty().addListener((obs, previousSong, currentSong) -> {
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
                    MusicPlayer.setNowPlayingList(Library.getSongs());
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                }
            });
            return row ;
        });

        playlistList.setOnMouseClicked(event -> {

            Playlist playlist = playlistList.getSelectionModel().getSelectedItem();

            if (event.getClickCount() == 2) {

                Thread thread = new Thread(() -> {
                    ObservableList<Song> playlistSongs = playlist.getSongs();
                    Song song = playlistSongs.get(0);
                    MusicPlayer.setNowPlayingList(playlistSongs);
                    MusicPlayer.setNowPlaying(song);
                    MusicPlayer.play();
                });

                thread.start();

            } else if (selectedPlaylist != playlist) {

                selectPlaylist(playlist);
                if (loadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    loadAnimation.stop();
                }
                loadAnimation.play();
            }
        });

        playlistList.setOnKeyPressed(event -> {

            KeyCode key = event.getCode();
            int index = -1;
            switch (key) {
                case DOWN:
                    index = playlistList.getSelectionModel().getSelectedIndex() + 1;
                    break;
                case UP:
                    index = playlistList.getSelectionModel().getSelectedIndex() - 1;
                    break;
            }

            if (index >= 0 && index < playlists.size()) {
                Playlist playlist = playlists.get(index);
                selectPlaylist(playlist);
                tableView.scrollTo(0);
                if (loadAnimation.statusProperty().get() == Animation.Status.RUNNING) {
                    loadAnimation.stop();
                }
                loadAnimation.play();
            }
        });
    }

    @Override
    public void refresh() {

        tableView.getColumns().get(0).setVisible(false);
        tableView.getColumns().get(0).setVisible(true);
    }

    private void selectPlaylist(Playlist playlist) {

        selectedPlaylist = playlist;
        ObservableList<Song> songs = playlist.getSongs();
        tableView.getSelectionModel().clearSelection();
        tableView.setItems(songs);
        tableView.scrollTo(0);
    }
}