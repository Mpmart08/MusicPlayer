package musicplayer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.util.Duration;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.scene.layout.Region;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MainController {

    private boolean isSideBarExpanded = true;
    private double expandedWidth = 250;
    private double collapsedWidth = 50;
    private boolean isPaused = true;
    private MediaPlayer mediaPlayer;

    private Animation collapseAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> setSlideDirection());
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (1.0 - frac);
            slide(curWidth);
        }
    };

    private Animation expandAnimation = new Transition() {
        {
            setCycleDuration(Duration.millis(250));
            setOnFinished(x -> {setVisibility(true); setSlideDirection();});
        }
        protected void interpolate(double frac) {
            double curWidth = collapsedWidth + (expandedWidth - collapsedWidth) * (frac);
            slide(curWidth);
        }
    };

    @FXML
    private BorderPane mainWindow;

    @FXML
    private VBox sideBar;

    @FXML
    private ImageView sideBarSlideButton;

    @FXML
    private ImageView playPauseButton;

    @FXML
    private Region currentSong;

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
            loadView(eventSource);
        } else if (styles.get(0).equals("bottomBarItem")) {
            loadView(eventSource);
        }
    }

    private void loadView(HBox eventSource) {

        try {

            String fileName = Resources.FXML + eventSource.getId() + ".fxml";
            Node view = (Node)FXMLLoader.load(this.getClass().getResource(fileName));
            mainWindow.setCenter(view);

        } catch (Exception ex) {

            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void slideSideBar() {

        if (isSideBarExpanded) {
            collapseSideBar();
        } else {
            expandSideBar();
        }
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

    private void slide(double curWidth) {

        sideBar.setPrefWidth(curWidth);
        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setTranslateX(-expandedWidth + curWidth);
                    }
                }
            }
        }
    }

    private void setVisibility(boolean isVisible) {

        for (Node n : sideBar.getChildren()) {
            if (n instanceof HBox) {
                for (Node m : ((HBox)n).getChildren()) {
                    if (m instanceof Label) {
                        m.setOpacity(isVisible ? 1 : 0);
                    }
                }
            }
        }
    }

    private void setSlideDirection() {

        isSideBarExpanded = !isSideBarExpanded;
        sideBarSlideButton.setImage(new Image(this.getClass().getResource(Resources.IMG
            + (isSideBarExpanded
            ? "leftArrowIcon.png"
            : "rightArrowIcon.png")
        ).toString()));
    }

    @FXML
    private void playPause() throws Exception {

        if (isPaused) {
            String path = "C:\\Users\\Mpmar\\Music\\Nobuo Uematsu\\Final Fantasy X Original Soundtrack Disc\\02 To Zanarkand.mp3";
            Media file = new Media(Paths.get(path).toUri().toString());
            if (mediaPlayer == null) mediaPlayer = new MediaPlayer(file);
            mediaPlayer.play();

            AudioFile audioFile = AudioFileIO.read(new File(path));
            Tag tag = audioFile.getTag();
            byte[] bytes = tag.getFirstArtwork().getBinaryData();
            InputStream in = new ByteArrayInputStream(bytes);
            Image img = new Image(in, 80, 80, true, false);
            BackgroundImage image = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
            currentSong.setBackground(new Background(image));
        } else {
            mediaPlayer.pause();
        }
        isPaused = !isPaused;
        playPauseButton.setImage(new Image(this.getClass().getResource(Resources.IMG
            + (isPaused
            ? "playIcon.png"
            : "pauseIcon.png")
        ).toString()));
    }

}