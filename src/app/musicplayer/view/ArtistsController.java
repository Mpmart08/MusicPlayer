package app.musicplayer.view;

import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

import app.musicplayer.MusicPlayer;
import app.musicplayer.model.Artist;
import app.musicplayer.model.Library;
import app.musicplayer.model.Song;
import app.musicplayer.util.SubView;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ArtistsController implements Initializable, SubView {

    @FXML private FlowPane grid;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ObservableList<Artist> artists = Library.getArtists();
        Collections.sort(artists);

        int limit = (artists.size() < 25) ? artists.size() : 25;

        for (int i = 0; i < limit; i++) {

            Artist artist = artists.get(i);
            grid.getChildren().add(createCell(artist));
        }

        int rows = (artists.size() % 5 == 0) ? artists.size() / 5 : artists.size() / 5 + 1;
        grid.prefHeightProperty().bind(grid.widthProperty().divide(5).add(16).multiply(rows));

        new Thread(() -> {

        	try {
        		Thread.sleep(1000);
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        	
            for (int j = 25; j < artists.size(); j++) {
                Artist artist = artists.get(j);
                Platform.runLater(() -> grid.getChildren().add(createCell(artist)));
            }

        }).start();
    }
    
    private VBox createCell(Artist artist) {

        VBox cell = new VBox();
        Label title = new Label(artist.getTitle());
        ImageView image = new ImageView(artist.getArtistImage());
        image.imageProperty().bind(artist.artistImageProperty());
        VBox imageBox = new VBox();

        title.setTextOverrun(OverrunStyle.CLIP);
        title.setWrapText(true);
        title.setPadding(new Insets(10, 0, 10, 0));
        title.setAlignment(Pos.TOP_LEFT);
        title.setPrefHeight(66);
        title.prefWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));

        image.fitWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        image.fitHeightProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        image.setPreserveRatio(true);
        image.setSmooth(true);

        imageBox.prefWidthProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        imageBox.prefHeightProperty().bind(grid.widthProperty().subtract(100).divide(5).subtract(1));
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(image);

        cell.getChildren().addAll(imageBox, title);
        cell.setPadding(new Insets(10, 10, 0, 10));
        cell.getStyleClass().add("artist-cell");
        cell.setAlignment(Pos.CENTER);
        cell.setOnMouseClicked(event -> {

            MainController mainController = MusicPlayer.getMainController();
            ArtistsMainController artistsMainController = (ArtistsMainController) mainController.loadView("ArtistsMain");

            VBox artistCell = (VBox) event.getSource();
            String artistTitle = ((Label) artistCell.getChildren().get(1)).getText();
            Artist a = Library.getArtist(artistTitle);
            artistsMainController.selectArtist(a);
        });
        
        cell.setOnDragDetected(event -> {
        	PseudoClass pressed = PseudoClass.getPseudoClass("pressed");
        	cell.pseudoClassStateChanged(pressed, false);
        	Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
        	ClipboardContent content = new ClipboardContent();
            content.putString("Artist");
            db.setContent(content);
        	MusicPlayer.setDraggedItem(artist);
        	db.setDragView(cell.snapshot(null, null), cell.widthProperty().divide(2).get(), cell.heightProperty().divide(2).get());
            event.consume();
        });

        return cell;
    }
    
    @Override
    public void play() {}
    
    @Override
    public void scroll(char letter) {
    	
    	int index = 0;
    	double cellHeight = 0;
    	ObservableList<Node> children = grid.getChildren();

        for (Node node : children) {

            VBox cell = (VBox) node;
            cellHeight = cell.getHeight();
            Label label = (Label) cell.getChildren().get(1);
            char firstLetter = removeArticle(label.getText()).charAt(0);
            if (firstLetter < letter) {
                index++;
            }
        }
    	
    	ScrollPane scrollpane = MusicPlayer.getMainController().getScrollPane();
    	
    	double row = (index / 5) * cellHeight;
    	double finalVvalue = row / (grid.getHeight() - scrollpane.getHeight());
    	double startVvalue = scrollpane.getVvalue();
    	
    	Animation scrollAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(500));
            }
            protected void interpolate(double frac) {
                double vValue = startVvalue + ((finalVvalue - startVvalue) * frac);
                scrollpane.setVvalue(vValue);
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
    	return null;
    }
}
